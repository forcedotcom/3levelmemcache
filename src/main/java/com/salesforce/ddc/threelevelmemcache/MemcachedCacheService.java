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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.OperationFuture;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salesforce.ddc.threelevelmemcache.exposed.AdminCacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.CacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.NonCompressionTranscoder;
import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;
import com.salesforce.ddc.threelevelmemcache.exposed.util.SHAKey;
import com.salesforce.ddc.threelevelmemcache.exposed.util.WaitResponseUtils;

/**
 * Cache implementation is based on memcached client.
 * 
 * @author Alexander Khimich
 */
public class MemcachedCacheService implements CacheService, AdminCacheService {

    private static Log log = LogFactory.getLog(MemcachedCacheService.class);

    private static final int DEFAULT_EXPIRE_TIME = 0;
    private static final int DEFAULT_TIMEOUT_SINGLE_OPERATION = 3000;
    private static final int DEFAULT_TIMEOUT_BULK_OPERATION = 6000;
    /**
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a
     * client is larger than that, the server will consider it to be real Unix
     * time value rather than an offset from current time. Optional expiration
     * time, either relative number of seconds from current time (up to 1
     * month), or an absolute Unix epoch time. By default, items never expire,
     * though items may be evicted due to memory pressure. Float values will be
     * rounded up to the nearest whole second.
     * <p/>
     * Default is never
     */
    private int expireTime = DEFAULT_EXPIRE_TIME;
    private int operationTimeOutMsec = DEFAULT_TIMEOUT_SINGLE_OPERATION;
    private long operationTimeOutBulkMsec = DEFAULT_TIMEOUT_BULK_OPERATION;

    private final MemcachedClientIF client;
    private boolean synchronousMode = false;
    private CacheListener listener;
    private final NonCompressionTranscoder nonCompressTranscoder = new NonCompressionTranscoder();
    protected boolean exceptionsSilentMode = true;

    public MemcachedCacheService(MemcachedClientIF client) {
	this(client, DEFAULT_EXPIRE_TIME, DEFAULT_TIMEOUT_SINGLE_OPERATION,
		DEFAULT_TIMEOUT_BULK_OPERATION);
    }

    /**
     * Creates configured instance of cache service based on memcached client.
     * 
     * @param client
     *            memcached client instance
     * @param expireTime
     *            expire time
     * @param maxTimeToGet
     *            max allowed time for get/getbutch operation
     */
    public MemcachedCacheService(MemcachedClientIF client, int expireTime,
	    int maxTimeToGet, int maxTimeToGetBulkValues) {
	this.client = client;
	this.expireTime = expireTime;
	this.operationTimeOutMsec = maxTimeToGet;
	this.operationTimeOutBulkMsec = maxTimeToGetBulkValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#get(java.lang.Object)
     */
    @Override
    public Serializable get(Object key) {
	Serializable rt = null;
	try {
	    if (key != null) {
		Future<Object> f = client.asyncGet(getKey(key));
		try {
		    rt = (Serializable) f.get(operationTimeOutMsec,
			    TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
		    log.warn("Timeout on get key='" + key + "' , timeout="
			    + operationTimeOutMsec + " mseconds. Returning "
			    + rt);
		    f.cancel(false);

		    if (!exceptionsSilentMode) {
			throw new RuntimeException("Timeout on get key:" + key,
				e);
		    }
		}
	    }
	    return rt;
	} catch (Exception alle) {
	    log.warn("For information only, there is exception in memcached get method, please ignore it,"
		    + " unable to retrieve key:"
		    + key
		    + ". Ex:"
		    + alle.getMessage());
	    if (log.isDebugEnabled()) {
		log.debug(
			"For information only, there is exception in memcached get method, please ignore it,"
				+ " unable to retrieve key:" + key, alle);
	    }

	    if (!exceptionsSilentMode) {
		throw new RuntimeException(
			"There is exception in memcached get method, unable to retrieve key:"
				+ key, alle);
	    }

	    return null;
	} finally {
	    if (listener != null)
		listener.get(key);
	}
    }

    @Override
    public boolean add(Object key, Serializable obj) {
	try {
	    if (key == null) {
		return Boolean.TRUE;
	    } else {
		String _key = getKey(key);
		OperationFuture<Boolean> f = (OperationFuture<Boolean>) client
			.add(_key, expireTime, obj);
		return WaitResponseUtils.waitForResponse(f, 3, "add");
	    }
	} catch (Exception alle) {
	    log.warn("For information only, there is exception in memcached add method, please ignore it,"
		    + " unable to add key:" + key);
	    if (log.isDebugEnabled()) {
		log.debug(
			"For information only, there is exception in memcached add method, please ignore it,"
				+ " unable to add key:" + key, alle);
	    }
	    return Boolean.FALSE;
	} finally {
	    if (listener != null)
		listener.put(key, obj);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#put(java.lang.Object,
     * java.io.Serializable)
     */
    @Override
    public void put(Object key, Serializable obj) {
	put(key, expireTime, obj);
    }

    /**
     * Returns list ordered by key sequence and filled out from map.
     * 
     * @param keys
     *            keys
     * @param map
     *            map
     * @return list ordered by key sequence and filled out from map
     */
    private List<Serializable> processBulkResult(List keys, Map map) {
	List<Serializable> result = new ArrayList<Serializable>();
	for (Object key : keys) {
	    Serializable obj = (Serializable) map.get(key);
	    result.add(obj);
	}
	return result;
    }

    /**
     * Converts key to SHA-1 hash and prefixes the hash.
     * 
     * @param unprefixedKey
     *            key
     * @return prefixed and hashed key value.
     */
    private String getKey(Object unprefixedKey) {
	return SHAKey.sha(unprefixedKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#putBatch(java.util.List,
     * java.util.List)
     */
    @Override
    public void putBatch(List<? extends Object> keys,
	    List<? extends Serializable> objs) {
	for (int i = 0; i < keys.size(); i++) {
	    put(keys.get(i), objs.get(i));
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#putBatch(java.util.Map)
     */
    @Override
    public void putBatch(Map<? extends Object, ? extends Serializable> objs) {
	for (Object key : objs.keySet()) {
	    put(key, objs.get(key));
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#getBatch(java.util.List)
     */
    @Override
    public List<Serializable> getBatch(List keys) {
	List<String> prefixedKeys = new ArrayList<String>();
	for (Object key : keys) {
	    prefixedKeys.add(getKey(key));
	    if (listener != null) {
		listener.get(key);
	    }
	}
	BulkFuture<Map<String, Object>> future = client
		.asyncGetBulk(prefixedKeys);

	Map<String, Object> map = new HashMap<String, Object>();
	try {
	    map = future.get(operationTimeOutBulkMsec, TimeUnit.MILLISECONDS);
	} catch (TimeoutException e) {
	    log.warn("Timeout on get bulkvalues keys='"
		    + StringUtils.join(keys, ",") + "' Returning null.");
	} catch (Exception e) {
	    log.error("Exception on getBatch keys='"
		    + StringUtils.join(keys, ",") + "' Returning null.");
	}
	List<Serializable> result = processBulkResult(prefixedKeys, map);
	return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#remove(java.lang.Object)
     */
    @Override
    public void remove(Object key) {
	OperationFuture<Boolean> f = (OperationFuture<Boolean>) client
		.delete(getKey(key));
	try {
	    WaitResponseUtils.waitForResponse(f, 3, "remove");
	} catch (TimeoutException e) {
	    log.warn("Timeout on remove key='" + key + "' , timeout="
		    + operationTimeOutMsec + " mseconds.");
	} catch (Exception e) {
	    log.error("error while removing key=" + key, e);
	} finally {
	    if (listener != null)
		listener.remove(key);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#removeBatch(java.util.List)
     */
    @Override
    public void removeBatch(Collection<Object> keys) {
	for (Object key : keys) {
	    remove(key);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#shutdown()
     */
    @Override
    public void shutdown() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#isSynchronousPut()
     */
    @Override
    public boolean isSynchronousPut() {
	return synchronousMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#setSynchronousPut(boolean)
     */
    @Override
    public void setSynchronousPut(boolean synchronousPut) {
	this.synchronousMode = synchronousPut;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jigsaw.core.cache.CacheService1#getStats()
     */
    @Override
    public Map<SocketAddress, Map<String, String>> getStats() {
	Map<SocketAddress, Map<String, String>> stats = new HashMap<SocketAddress, Map<String, String>>(
		client.getStats());
	SocketAddress server = new InetSocketAddress(0);
	Map<String, String> s = new HashMap<String, String>();
	s.put("isSynchronousPut", String.valueOf(isSynchronousPut()));
	s.put("isConnected", String.valueOf(isConnected()));
	stats.put(server, s);
	return stats;
    }

    public CASValue<Object> gets(Object key) {
	CASValue<Object> cas = null;
	try {
	    cas = client.gets(getKey(key));
	} catch (RuntimeException e) {
	    log.warn("OK to ignore, memcached gets threw an exception, unable to read the key:"
		    + key);
	    if (log.isDebugEnabled()) {
		log.debug("gets failed for key:" + key, e);
	    }
	}
	return cas;
    }

    public CASResponse cas(Object key, long casId, Serializable value) {
	CASResponse cas = null;
	try {
	    cas = client.cas(getKey(key), casId, value);
	} catch (RuntimeException e) {
	    log.warn("OK to ignore, memcached cas threw an exception, unable to cas the key:"
		    + key);
	    if (log.isDebugEnabled()) {
		log.debug("cas failed for key: " + key, e);
	    }
	}
	return cas;
    }

    @Override
    public void append(Object key, String obj) {
	doFullAppend(key, obj, 3);
	if (listener != null)
	    listener.append(key, obj);
    }

    /**
     * Do direct memcached append.
     * 
     * @param key
     * @param obj
     */
    private void doFullAppend(Object key, String obj, int tries) {
	if (tries <= 0) {
	    log.error("Try[" + tries + "]Can't append this:" + key
		    + " , by this value:" + obj.toString());
	    return;
	}

	String _key = getKey(key);
	try {
	    OperationFuture<Boolean> addFuture = (OperationFuture<Boolean>) client
		    .add(_key, expireTime, obj, nonCompressTranscoder);
	    if (!addFuture.get()) {
		OperationFuture<Boolean> appendFuture = (OperationFuture<Boolean>) client
			.append(0, _key, "," + obj, nonCompressTranscoder);

		if (synchronousMode) {
		    boolean appendResult = WaitResponseUtils.waitForResponse(
			    appendFuture, 3, "append");
		    if (!appendResult) {
			log.warn("Both, add and append failed for key=" + key
				+ ", appending=" + obj + ", add status="
				+ addFuture.getStatus() + ", append status="
				+ appendFuture.getStatus());
		    }
		}
	    } else {
		if (listener != null)
		    listener.put(key, obj);
	    }
	} catch (Exception e) {
	    // Log only message
	    log.warn("Try[" + tries + "]Can't append this:" + key
		    + " , by this value:" + obj.toString()
		    + " try one more time. Ex:" + e.getMessage());
	    doFullAppend(key, obj, tries - 1);
	}
    }

    @Override
    public long incr(Object key) {
	String _key = getKey(key);
	Future<Long> future = client.asyncIncr(_key, 1);
	try {
	    return future.get(operationTimeOutMsec, TimeUnit.MILLISECONDS);
	} catch (Exception e) {
	    log.error("Increment failed:key=" + key, e);
	}
	return -1;
    }

    @Override
    public long decr(Object key) {
	String _key = getKey(key);
	Future<Long> future = client.asyncDecr(_key, 1);
	try {
	    return future.get(operationTimeOutMsec, TimeUnit.MILLISECONDS);
	} catch (Exception e) {
	    log.error("Decrement failed:key=" + key, e);
	}
	return -1;
    }

    @Override
    public void flush() {
	// don't call memcache flush!!! it invalidates all cache.
	// make sure all operations are processed
	client.waitForQueues(3, TimeUnit.MINUTES);
    }

    @Override
    public void clearAll() {
	Future<Boolean> flushOp = client.flush();
	try {
	    flushOp.get(3, TimeUnit.MINUTES);
	} catch (Exception e) {
	    log.warn("clearAll failed", e);
	}
    }

    @Override
    public long size() {
	return 0;// getStats();
    }

    @Override
    public String toString() {
	return super.toString() + ", Client=" + client.toString() + ", "
		+ client.getNodeLocator() + ", ";
    }

    @Override
    public boolean isConnected() {
	return client.getNodeLocator() != null;
    }

    @Override
    public CacheListener getCacheListener() {
	return listener;
    }

    @Override
    public void setListener(CacheListener listener) {
	this.listener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#incr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long incr(Object key, int by, long defaultValue, int expiration) {
	String _key = getKey(key);
	try {
	    return client.incr(_key, by, defaultValue, expiration);
	} catch (Exception e) {
	    log.error("increment failed:key=" + key, e);
	}
	return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#decr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long decr(Object key, int by, long defaultValue, int expiration) {
	String _key = getKey(key);
	try {
	    return client.decr(_key, by, defaultValue, expiration);
	} catch (Exception e) {
	    log.error("decrement failed:key=" + key, e);
	}
	return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#put(java.lang.Object, int,
     * java.io.Serializable)
     */
    @Override
    public void put(Object key, int expiration, Serializable obj) {
	try {
	    if (key == null) {
		throw new IllegalArgumentException("Key cannot be null");
	    }

	    if (expiration < 0) {
		throw new IllegalArgumentException(
			"Expiration time cannot be minus. Exptime="
				+ expiration);
	    }

	    putWithRetries(key, expiration, obj, 3);
	} finally {
	    if (listener != null)
		listener.put(key, obj);
	}
    }

    private void putWithRetries(Object key, int expiration, Serializable obj,
	    int tries) {

	try {
	    OperationFuture<Boolean> rv = putWithSlowDownLogic(key, expiration,
		    obj);

	    if (synchronousMode) {
		if (!WaitResponseUtils.waitForResponse(rv, 3, "set")) {
		    throw new RuntimeException("Set failed");
		}
	    }
	} catch (Exception e) {
	    if (tries <= 0) {
		log.error("Try[" + tries + "] Error while putting object key="
			+ key + ", obj=" + obj);
		throw new RuntimeException(
			"Cannot put after several attempts.", e);
	    } else {
		log.warn("Try[" + tries + "]");
		try {
		    Thread.sleep(200);
		} catch (InterruptedException ie) {
		    Thread.currentThread().interrupt();
		} catch (Exception e2) {
		    // Ignore exceptions here. We're just trying to slow
		    // down input.
		}
		putWithRetries(key, expiration, obj, tries - 1);
	    }
	}

    }

    private OperationFuture<Boolean> putWithSlowDownLogic(Object key,
	    int expiration, Serializable obj) {
	OperationFuture<Boolean> rv = null;
	while (rv == null) {
	    try {
		rv = (OperationFuture<Boolean>) client.set(getKey(key),
			expiration, obj);
	    } catch (IllegalStateException ex) {
		log.debug("slow down"); // Need to slow down a bit when
					// we start getting rejections.
		try {
		    if (rv != null) {
			rv.get(250, TimeUnit.MILLISECONDS);
		    } else {
			Thread.sleep(250);
		    }
		} catch (InterruptedException ie) {
		    Thread.currentThread().interrupt();
		} catch (Exception e2) {
		    // Ignore exceptions here. We're just trying to slow
		    // down input.
		}
	    }
	}
	return rv;
    }

    public void setTimeOutSingleOperation(String timeOutSingleOperation) {
	this.operationTimeOutMsec = NumberUtils.toInt(timeOutSingleOperation,
		DEFAULT_TIMEOUT_SINGLE_OPERATION);
    }

    public void setTimeOutBulkOperation(String timeOutBulkOperation) {
	this.operationTimeOutBulkMsec = NumberUtils.toLong(
		timeOutBulkOperation, DEFAULT_TIMEOUT_BULK_OPERATION);

    }

    public void setExceptionsSilentMode(boolean exceptionsSilentMode) {
	this.exceptionsSilentMode = exceptionsSilentMode;
    }

}
