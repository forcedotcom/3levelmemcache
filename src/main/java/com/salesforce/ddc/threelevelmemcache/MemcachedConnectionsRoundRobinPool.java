/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
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
    
    private static final Log log = LogFactory.getLog(MemcachedConnectionsRoundRobinPool.class);
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
    
    public MemcachedConnectionsRoundRobinPool (PoolableObjectFactory factory, int size,
                                               int timeBetweenKeepAliveRunsSecs) {
        this._factory = factory;
        this.size = size;
        this.timeBetweenKeepAliveRunsSecs = timeBetweenKeepAliveRunsSecs;
        fill();
        createKeepAliveTimer();
        log.info("Initialized.");
    }
    
    public MemcachedConnectionsRoundRobinPool (PoolableObjectFactory factory) {
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
            }
            catch (Exception e) {
                log.error("Error creating the connection:", e);
                throw new RuntimeException(
                        "Critical error. Wasn't able to create Memcached Connection.", e);
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
            }
            catch (Exception e) {
                log.warn("Exception:", e);
            }
        }
        
        for (int i = 0; i < size; i++) {
            try {
                futures[i].get(30, TimeUnit.SECONDS);
            }
            catch (Exception e) {
                log.warn("Error occured but continue closing connections i=" + i
                        + " Exception:" + e.getMessage());
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
        
        KeepAliveTimerTask (final MemcachedConnectionsRoundRobinPool pool)
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
                    }
                    catch (Exception e) {
                        log.error("Error checking the connection:", e);
                    }
                }
                success = true;
            }
            catch (Exception e) {
                log.error("Stop timer due to exception:", e);
                cancel();
            }
            finally {
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
