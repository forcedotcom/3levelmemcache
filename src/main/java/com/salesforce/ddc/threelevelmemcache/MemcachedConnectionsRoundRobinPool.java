/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.ddc.threelevelmemcache;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.spy.memcached.MemcachedClientIF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

public class MemcachedConnectionsRoundRobinPool extends BaseObjectPool {

    private static final Log log = LogFactory
	    .getLog(MemcachedConnectionsRoundRobinPool.class);
    private final static int DEFAULT_SIZE = 10;

    private final int size;

    private int timeBetweenKeepAliveRunsSecs = 0;

    private MemcachedClientIF[] clients;

    private AtomicLong index = new AtomicLong(Long.MIN_VALUE);

    /** My {@link PoolableObjectFactory}. */
    private PoolableObjectFactory _factory = null;

    /**
     * Timer used to periodically check pools health. Because a {@link Timer}
     * creates a {@link Thread} this is lazily instantiated.
     */
    private Timer timer;

    static {
	System.setProperty("net.spy.log.LoggerImpl",
		"net.spy.memcached.compat.log.Log4JLogger");
    }

    public MemcachedConnectionsRoundRobinPool(PoolableObjectFactory factory,
	    int size, int timeBetweenKeepAliveRunsSecs) {
	this._factory = factory;
	this.size = size;
	this.timeBetweenKeepAliveRunsSecs = timeBetweenKeepAliveRunsSecs;
	fill();
	createKeepAliveTimer();
	log.info("Initialized.");
    }

    public MemcachedConnectionsRoundRobinPool(PoolableObjectFactory factory) {
	this(factory, DEFAULT_SIZE, 0);
    }

    private synchronized Timer getTimer() {
	if (timer == null) {
	    timer = new Timer(true);
	}
	return timer;
    }

    private void fill() {
	log.info("Initializing " + size + " connections pool.");
	clients = new MemcachedClientIF[size];
	for (int i = 0; i < size; i++) {
	    try {
		clients[i] = (MemcachedClientIF) _factory.makeObject();
	    } catch (Exception e) {
		log.error("Error creating the connection:", e);
		throw new RuntimeException(
			"Critical error. Wasn't able to create Memcached Connection.",
			e);
	    }
	}
    }

    private void createKeepAliveTimer() {
	if (timeBetweenKeepAliveRunsSecs > 0) {
	    TimerTask task = new KeepAliveTimerTask(this);
	    getTimer().schedule(task, 0l,
		    TimeUnit.SECONDS.toMillis(timeBetweenKeepAliveRunsSecs));
	}
    }

    public void close() throws Exception {
	// mark pool as closed for any actions
	super.close();

	// Stop timer
	getTimer().cancel();

	/*
	 * Close connection in ||
	 */
	log.info("Closing connections");
	ExecutorService executor = Executors.newCachedThreadPool();
	Future<Boolean>[] futures = new Future[size];
	for (int i = 0; i < size; i++) {
	    try {
		final MemcachedClientIF client = clients[i];
		futures[i] = executor.submit(new Callable<Boolean>() {

		    public Boolean call() throws Exception {
			return client.shutdown(30, TimeUnit.SECONDS);
		    }
		});
	    } catch (Exception e) {
		log.warn("Exception:", e);
	    }
	}

	for (int i = 0; i < size; i++) {
	    try {
		futures[i].get(30, TimeUnit.SECONDS);
	    } catch (Exception e) {
		log.warn("Error occured but continue closing connections i="
			+ i + " Exception:" + e.getMessage());
	    }
	}
	executor.shutdownNow();
	log.info("All connections are closed");

    }

    @Override
    public Object borrowObject() throws Exception {
	int i = (int) Math.abs(index.getAndAdd(1) % size);
	return this.clients[i];
    }

    @Override
    public void returnObject(Object obj) throws Exception {

    }

    @Override
    public void invalidateObject(Object obj) throws Exception {

    }

    private static class KeepAliveTimerTask extends TimerTask {

	private final MemcachedConnectionsRoundRobinPool pool;

	KeepAliveTimerTask(final MemcachedConnectionsRoundRobinPool pool)
		throws IllegalArgumentException {
	    if (pool == null) {
		throw new IllegalArgumentException("pool must not be null.");
	    }
	    this.pool = pool;
	}

	public void run() {
	    boolean success = false;
	    try {
		for (int i = 0; i < pool.size && !pool.isClosed(); i++) {
		    try {
			// Just get stats without any extra check
			// allows us to keep socket alive
			pool.clients[i].getStats();
		    } catch (Exception e) {
			log.error("Error checking the connection:", e);
		    }
		}
		success = true;
	    } catch (Exception e) {
		log.error("Stop timer due to exception:", e);
		cancel();
	    } finally {
		if (log.isDebugEnabled()) {
		    log.debug("Keep Alive check executed");
		}
		// detect other types of Throwable and cancel this Timer
		if (!success) {
		    cancel();
		}
	    }
	}

	public String toString() {
	    final StringBuffer sb = new StringBuffer();
	    sb.append("KeepAliveTimerTask");
	    sb.append(" pool=").append(pool);
	    sb.append('}');
	    return sb.toString();
	}
    }
}
