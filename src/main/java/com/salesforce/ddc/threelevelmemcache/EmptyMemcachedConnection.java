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

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.BroadcastOpFactory;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.NodeLocator;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.transcoders.Transcoder;

import org.apache.commons.lang.math.NumberUtils;

public class EmptyMemcachedConnection implements MemcachedClientIF {

    private static final OperationFuture<Boolean> BOOL = new EmptyImmediateFuture<Boolean>(
	    Boolean.TRUE);
    private static final OperationFuture<Object> OBJ = new EmptyImmediateFuture<Object>(
	    null);
    private static final OperationFuture<Long> LONG = new EmptyImmediateFuture<Long>(
	    NumberUtils.LONG_ZERO);

    @Override
    public Collection<SocketAddress> getAvailableServers() {
	return Collections.emptySet();
    }

    @Override
    public Collection<SocketAddress> getUnavailableServers() {
	return Collections.emptySet();
    }

    @Override
    public Transcoder<Object> getTranscoder() {
	return null;
    }

    @Override
    public NodeLocator getNodeLocator() {
	return null;
    }

    @Override
    public Future<Boolean> append(long cas, String key, Object val) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> append(long cas, String key, T val,
	    Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public Future<Boolean> append(String arg0, Object arg1) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> append(String arg0, T arg1, Transcoder<T> arg2) {
	return BOOL;
    }

    @Override
    public Future<Boolean> prepend(long cas, String key, Object val) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> prepend(long cas, String key, T val,
	    Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public Future<Boolean> prepend(String arg0, Object arg1) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> prepend(String arg0, T arg1, Transcoder<T> arg2) {
	return BOOL;
    };

    @Override
    public <T> Future<CASResponse> asyncCAS(String key, long casId, T value,
	    Transcoder<T> tc) {
	return null;
    }

    @Override
    public Future<CASResponse> asyncCAS(String key, long casId, Object value) {
	return null;
    }

    @Override
    public CASResponse cas(String key, long casId, Object value)
	    throws OperationTimeoutException {
	return null;
    }

    @Override
    public <T> Future<Boolean> add(String key, int exp, T o, Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public Future<Boolean> add(String key, int exp, Object o) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> set(String key, int exp, T o, Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public Future<Boolean> set(String key, int exp, Object o) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> replace(String key, int exp, T o,
	    Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public Future<Boolean> replace(String key, int exp, Object o) {
	return BOOL;
    }

    @Override
    public <T> Future<T> asyncGet(String key, Transcoder<T> tc) {
	return null;
    }

    @Override
    public Future<Object> asyncGet(String key) {
	return OBJ;
    }

    @Override
    public Future<CASValue<Object>> asyncGetAndTouch(String key, int exp) {
	return null;
    }

    @Override
    public <T> Future<CASValue<T>> asyncGetAndTouch(String key, int exp,
	    Transcoder<T> tc) {
	return null;
    }

    @Override
    public CASValue<Object> getAndTouch(String key, int exp) {
	return null;
    }

    @Override
    public <T> CASValue<T> getAndTouch(String key, int exp, Transcoder<T> tc) {

	return null;
    }

    @Override
    public <T> Future<CASValue<T>> asyncGets(String key, Transcoder<T> tc) {

	return null;
    }

    @Override
    public Future<CASValue<Object>> asyncGets(String key) {

	return null;
    }

    @Override
    public <T> CASValue<T> gets(String key, Transcoder<T> tc)
	    throws OperationTimeoutException {

	return null;
    }

    @Override
    public CASValue<Object> gets(String key) throws OperationTimeoutException {

	return null;
    }

    @Override
    public <T> T get(String key, Transcoder<T> tc)
	    throws OperationTimeoutException {
	return null;
    }

    @Override
    public Object get(String key) throws OperationTimeoutException {
	return null;
    }

    @Override
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
	    Iterator<Transcoder<T>> tcs) {

	return null;
    }

    @Override
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
	    Transcoder<T> tc) {

	return null;
    }

    @Override
    public BulkFuture<Map<String, Object>> asyncGetBulk(Collection<String> keys) {

	return null;
    }

    @Override
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Transcoder<T> tc,
	    String... keys) {

	return null;
    }

    @Override
    public BulkFuture<Map<String, Object>> asyncGetBulk(String... keys) {

	return null;
    }

    @Override
    public <T> Map<String, T> getBulk(Collection<String> keys, Transcoder<T> tc)
	    throws OperationTimeoutException {
	return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getBulk(Collection<String> keys)
	    throws OperationTimeoutException {
	return Collections.emptyMap();
    }

    @Override
    public <T> Map<String, T> getBulk(Transcoder<T> tc, String... keys)
	    throws OperationTimeoutException {
	return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getBulk(String... keys)
	    throws OperationTimeoutException {
	return Collections.emptyMap();
    }

    @Override
    public <T> Future<Boolean> touch(String key, int exp, Transcoder<T> tc) {
	return BOOL;
    }

    @Override
    public <T> Future<Boolean> touch(String key, int exp) {
	return BOOL;
    }

    @Override
    public Map<SocketAddress, String> getVersions() {
	return Collections.emptyMap();
    }

    @Override
    public Map<SocketAddress, Map<String, String>> getStats() {
	return Collections.emptyMap();
    }

    @Override
    public Map<SocketAddress, Map<String, String>> getStats(String prefix) {
	return Collections.emptyMap();
    }

    @Override
    public long incr(String key, int by) throws OperationTimeoutException {
	return 0;
    }

    @Override
    public long decr(String key, int by) throws OperationTimeoutException {
	return 0;
    }

    @Override
    public long incr(String key, int by, long def, int exp)
	    throws OperationTimeoutException {
	return 0;
    }

    @Override
    public long decr(String key, int by, long def, int exp)
	    throws OperationTimeoutException {
	return 0;
    }

    @Override
    public Future<Long> asyncIncr(String key, int by) {
	return LONG;
    }

    @Override
    public Future<Long> asyncDecr(String key, int by) {
	return LONG;
    }

    @Override
    public long incr(String key, int by, long def)
	    throws OperationTimeoutException {
	return 0;
    }

    @Override
    public long decr(String key, int by, long def)
	    throws OperationTimeoutException {
	return 0;
    }

    @Override
    public Future<Boolean> delete(String key) {
	return BOOL;
    }

    @Override
    public Future<Boolean> delete(String arg0, long arg1) {
	return BOOL;
    }

    @Override
    public Future<Boolean> flush(int delay) {
	return BOOL;
    }

    @Override
    public Future<Boolean> flush() {
	return BOOL;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean shutdown(long timeout, TimeUnit unit) {
	return true;
    }

    @Override
    public boolean waitForQueues(long timeout, TimeUnit unit) {
	return true;
    }

    @Override
    public boolean addObserver(ConnectionObserver obs) {
	return false;
    }

    @Override
    public boolean removeObserver(ConnectionObserver obs) {
	return false;
    }

    @Override
    public Set<String> listSaslMechanisms() {
	return null;
    }

    public static class EmptyImmediateFuture<T> extends OperationFuture<T> {

	private final T value;

	public EmptyImmediateFuture(T value) {
	    super("", new CountDownLatch(0), 0, Executors
		    .newSingleThreadExecutor());
	    this.value = value;
	    this.status = new OperationStatus(true, "");
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
	    return false;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
	    return value;

	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
		ExecutionException, TimeoutException {
	    return value;
	}

	@Override
	public boolean isCancelled() {
	    return false;
	}

	@Override
	public boolean isDone() {
	    return true;
	}
    }

    @Override
    public Future<Long> asyncDecr(String arg0, long arg1) {
	return LONG;
    }

    @Override
    public BulkFuture<Map<String, Object>> asyncGetBulk(Iterator<String> arg0) {
	return null;
    }

    @Override
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> arg0,
	    Iterator<Transcoder<T>> arg1) {
	return null;
    }

    @Override
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> arg0,
	    Transcoder<T> arg1) {
	return null;
    }

    @Override
    public Future<Long> asyncIncr(String arg0, long arg1) {
	return LONG;
    }

    @Override
    public <T> CASResponse cas(String arg0, long arg1, int arg2, T arg3,
	    Transcoder<T> arg4) {
	return null;
    }

    @Override
    public long decr(String arg0, long arg1) {
	return 0;
    }

    @Override
    public long decr(String arg0, long arg1, long arg2) {
	return 0;
    }

    @Override
    public long decr(String arg0, long arg1, long arg2, int arg3) {
	return 0;
    }

    @Override
    public Map<String, Object> getBulk(Iterator<String> arg0) {
	return Collections.emptyMap();
    }

    @Override
    public <T> Map<String, T> getBulk(Iterator<String> arg0, Transcoder<T> arg1) {
	return Collections.emptyMap();
    }

    @Override
    public long incr(String arg0, long arg1) {
	return 0;
    }

    @Override
    public long incr(String arg0, long arg1, long arg2) {
	return 0;
    }

    @Override
    public long incr(String arg0, long arg1, long arg2, int arg3) {
	return 0;
    }

    public Future<CASValue<Object>> asyncGetAndLock(String arg0, int arg1) {
	return null;
    }

    public <T> Future<CASValue<T>> asyncGetAndLock(String arg0, int arg1,
	    Transcoder<T> arg2) {
	return null;
    }

    public <T> CASResponse cas(String arg0, long arg1, T arg2,
	    Transcoder<T> arg3) throws OperationTimeoutException {
	return null;
    }

    public CASValue<Object> getAndLock(String arg0, int arg1) {
	return null;
    }

    public <T> CASValue<T> getAndLock(String arg0, int arg1, Transcoder<T> arg2) {
	return null;
    }

    @Override
    public Future<CASResponse> asyncCAS(String key, long casId, int exp,
	    Object value) {
	return null;
    }

    @Override
    public CASResponse cas(String key, long casId, int exp, Object value) {
	return null;
    }

    @Override
    public <T> OperationFuture<CASResponse> asyncCAS(String arg0, long arg1,
	    int arg2, T arg3, Transcoder<T> arg4) {
	return null;
    }

    @Override
    public CountDownLatch broadcastOp(BroadcastOpFactory arg0) {
	return null;
    }

    @Override
    public CountDownLatch broadcastOp(BroadcastOpFactory arg0,
	    Collection<MemcachedNode> arg1) {
	return null;
    }
}