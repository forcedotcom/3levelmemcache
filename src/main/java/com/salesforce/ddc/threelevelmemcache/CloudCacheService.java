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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;

import com.salesforce.ddc.threelevelmemcache.exposed.AdminCacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.CacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;
import com.salesforce.ddc.threelevelmemcache.exposed.proxy.ValueProxyFactory;

/**
 * The Cloud cache consists of three levels of cache: a local JVM cache, a
 * primary cluster of replicated memcached servers and a secondary cluster of
 * replicated memcached servers. The Cloud cache supports automatic failover and
 * hot swapping of primary and secondary clusters.
 * 
 * @author Alexander Khimich
 */
public class CloudCacheService implements CacheService, AdminCacheService {

    private static Log log = LogFactory.getLog(CloudCacheService.class);
    /**
     * Config for hot swapping from primary to secondary caches
     */
    private static boolean isPrimaryOn = true, isSecondaryOn = true;

    private final CacheService primary, secondary;
    private final CacheService firstLevelCache;

    private CacheListener listener;

    private ValueProxyFactory proxyFactory;

    private long l1Hits, l1Missess;

    public CloudCacheService(CacheService jvm, CacheService primary,
	    CacheService secondary) {
	super();
	this.primary = primary;
	this.secondary = secondary;
	this.firstLevelCache = jvm;
    }

    @Override
    public void append(Object key, String obj) {
	appendNormal(key, obj);

	if (listener != null) {
	    listener.append(key, obj);
	}
    }

    private void appendNormal(final Object key, final String obj) {
	// put in fl
	if (firstLevelCache != null) {
	    firstLevelCache.append(key, obj);

	}
	// put in primary
	if (canUsePrimary()) {
	    primary.append(key, obj);
	}
	// put in secondary
	if (canUseSecondary()) {
	    secondary.append(key, obj);
	}
    }

    @Override
    public Serializable get(Object key) {
	Serializable value = null;
	// get from l1
	if (firstLevelCache != null) {
	    value = firstLevelCache.get(key);
	}
	// try to get from other caches
	if (value == null) {
	    l1Missess++;
	    // get from primary
	    if (canUsePrimary()) {
		value = primary.get(key);

		if (log.isDebugEnabled()) {
		    log.debug("Get from Primary:" + key + "=" + value);
		}
	    }

	    if (value == null) {

		log.debug("Primary cache returned null for key:" + key);
		if (canUseSecondary()) {
		    value = secondary.get(key);

		    if (log.isDebugEnabled()) {
			log.debug("Get from Secondary:" + key + "=" + value);
		    }
		}

	    }
	    // if found in memcache - put in local
	    if (firstLevelCache != null && value != null) {
		firstLevelCache.put(key, value);
	    }
	} else {
	    l1Hits++;
	}
	// notify listener
	if (listener != null) {
	    listener.get(key);
	}
	// proxying the object
	if (proxyFactory != null && value != null) {
	    value = (Serializable) proxyFactory.createProxy(value);
	}
	return value;
    }

    @Override
    public List<Serializable> getBatch(List keys) {
	List<Serializable> results = null;
	if (firstLevelCache != null) {
	    final int size = keys.size();
	    results = (List<Serializable>) firstLevelCache.getBatch(keys);
	    int j = 0;
	    List nullKeys = new ArrayList();
	    for (int i = 0; i < size; i++) {
		if (results.get(i) == null) {
		    nullKeys.add(keys.get(i));
		}
	    }
	    List<Serializable> nullKeyresult = null;
	    if (canUsePrimary()) {
		nullKeyresult = (List<Serializable>) primary.getBatch(nullKeys);
	    } else if (canUseSecondary()) {
		nullKeyresult = (List<Serializable>) secondary
			.getBatch(nullKeys);
	    } else {
		log.info("Wasn able to access primary and secondary cache.");
	    }

	    if (nullKeyresult != null) {
		Iterator<Serializable> iterator = nullKeyresult.iterator();
		for (int i = 0; i < results.size(); i++) {
		    if (results.get(i) == null) {
			results.set(i, iterator.next());
		    }
		}

	    }
	} else {
	    if (canUsePrimary()) {
		results = (List<Serializable>) primary.getBatch(keys);
	    } else if (canUseSecondary()) {
		results = (List<Serializable>) secondary.getBatch(keys);
	    }
	}

	/* proxy the list */
	proxyList(results);
	return results;
    }

    protected void proxyList(List<Serializable> data) {
	if (proxyFactory != null && data != null) {
	    for (int i = 0; i < data.size(); i++) {
		Serializable proxy = (Serializable) proxyFactory
			.createProxy(data.get(i));
		data.set(i, proxy);
	    }
	}
    }

    @Override
    public void put(Object key, Serializable obj) {
	putNormal(key, 0, obj);
	if (listener != null) {
	    listener.put(key, obj);
	}
    }

    private void putNormal(Object key, int expiration, Serializable obj) {

	if (AopUtils.isAopProxy(obj)) {
	    // Do nothing with proxy object, because it was not modified and no
	    // need to proceed.
	    log.warn("Code is trying to save proxy object, need to throw exception");
	    return;
	}

	if (firstLevelCache != null) {
	    firstLevelCache.put(key, expiration, obj);
	}

	if (canUsePrimary()) {

	    if (log.isDebugEnabled()) {
		log.debug("Put to Primary:" + key + "=" + obj);
	    }

	    primary.put(key, expiration, obj);
	}
	// put secondary
	if (canUseSecondary()) {

	    if (log.isDebugEnabled()) {
		log.debug("Put to Secondary:" + key + "=" + obj);
	    }

	    secondary.put(key, expiration, obj);
	}

    }

    @Override
    public void putBatch(List<? extends Object> keys,
	    List<? extends Serializable> objs) {
	for (int i = 0; i < keys.size(); i++) {
	    put(keys.get(i), objs.get(i));
	}
    }

    @Override
    public void putBatch(Map<? extends Object, ? extends Serializable> objs) {
	for (Map.Entry<? extends Object, ? extends Serializable> entry : objs
		.entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }

    @Override
    public void remove(Object key) {
	try {
	    // remove fl
	    if (firstLevelCache != null) {
		this.firstLevelCache.remove(key);
	    }
	    // remove primary
	    if (canUsePrimary()) {
		this.primary.remove(key);
	    }
	    // remove secondary
	    if (canUseSecondary()) {
		this.secondary.remove(key);
	    }
	} finally {
	    // notify listener
	    if (listener != null) {
		listener.remove(key);
	    }
	}
    }

    @Override
    public void removeBatch(Collection<Object> keys) {
	for (Object key : keys) {
	    remove(key);
	}

    }

    @Override
    public Map<SocketAddress, Map<String, String>> getStats() {
	Map<SocketAddress, Map<String, String>> stats = new HashMap<SocketAddress, Map<String, String>>();
	SocketAddress server = new InetSocketAddress(0);
	Map<String, String> s = new LinkedHashMap<String, String>();
	s.put("misses", String.valueOf(l1Missess));
	s.put("hits", String.valueOf(l1Hits));
	s.put("isSynchronousPut", String.valueOf(isSynchronousPut()));
	s.put("isPrimaryOn", String.valueOf(isPrimaryOn));
	s.put("isSecondaryOn", String.valueOf(isSecondaryOn));
	s.put("isPrimaryConnected", String.valueOf(primary.isConnected()));
	s.put("isSecondaryConnected",
		secondary != null ? String.valueOf(secondary.isConnected())
			: "");
	stats.put(server, s);
	return stats;
    }

    @Override
    public boolean isSynchronousPut() {
	return false;
    }

    private String getKey(Object unprefixedKey) {
	return unprefixedKey.toString();
    }

    private boolean canUsePrimary() {
	boolean active = primary != null && isPrimaryOn
		&& primary.isConnected();
	if (!active) {
	    log.info("Primary cache is not active.Isconnected="
		    + primary.isConnected());
	}
	return active;
    }

    private boolean canUseSecondary() {
	boolean active = secondary != null && isSecondaryOn
		&& secondary.isConnected();
	return active;
    }

    @Override
    public void setSynchronousPut(boolean synchronousPut) {
	if (firstLevelCache != null) {
	    this.firstLevelCache.setSynchronousPut(synchronousPut);
	}
	if (canUsePrimary()) {
	    this.primary.setSynchronousPut(synchronousPut);
	}
	if (canUseSecondary()) {
	    this.secondary.setSynchronousPut(synchronousPut);
	}
    }

    @Override
    public void shutdown() {
	try {
	    if (firstLevelCache != null) {
		this.firstLevelCache.shutdown();
	    }
	    this.primary.shutdown();
	    if (secondary != null) {
		this.secondary.shutdown();
	    }
	} catch (Exception e) {
	    log.error("Exception happened during shutdown call.", e);
	} finally {
	}
    }

    @Override
    public void flush() {
    }

    @Override
    public void clearAll() {
	if (firstLevelCache != null) {
	    AdminCacheService adminCacheService = (AdminCacheService) firstLevelCache;
	    adminCacheService.clearAll();
	}

	if (canUsePrimary()) {
	    AdminCacheService adminCacheService = (AdminCacheService) primary;
	    adminCacheService.clearAll();
	}
	if (canUseSecondary()) {
	    AdminCacheService adminCacheService = (AdminCacheService) secondary;
	    adminCacheService.clearAll();
	}

    }

    @Override
    public long size() {
	if (firstLevelCache != null) {
	    return firstLevelCache.size();
	} else {
	    return 0;
	}
    }

    /**
     * Special Note: {@link #incr(Object)} deosn't call
     * {@link #incr(Object, int, long, int) with default values due to the
     * internal implementation of memcached cache service is different.
     */
    @Override
    public long incr(Object key) {
	long r = -1;
	if (firstLevelCache != null) {
	    firstLevelCache.incr(key);
	}

	if (canUsePrimary()) {
	    r = primary.incr(key);
	}
	if (canUseSecondary()) {
	    r = secondary.incr(key);
	}
	return r;
    }

    /**
     * Special Note: {@link #decr(Object)} doesn't call
     * {@link #decr(Object, int, long, int)} with default values due to the
     * internal implementation of memcached cache service is different.
     */
    @Override
    public long decr(Object key) {
	long r = -1;
	if (firstLevelCache != null) {
	    r = firstLevelCache.decr(key);
	}
	if (canUsePrimary()) {
	    r = primary.decr(key);
	}
	if (canUseSecondary()) {
	    r = secondary.decr(key);
	}
	return r;
    }

    public static boolean isPrimaryOn() {
	return isPrimaryOn;
    }

    public static void setPrimaryOn(boolean isPrimaryOn) {
	CloudCacheService.isPrimaryOn = isPrimaryOn;
    }

    public static boolean isSecondaryOn() {
	return isSecondaryOn;
    }

    public static void setSecondaryOn(boolean isSecondaryOn) {
	CloudCacheService.isSecondaryOn = isSecondaryOn;
    }

    /**
     * always return true
     */
    @Override
    public boolean isConnected() {
	if (canUsePrimary()) {
	    return true;
	}
	if (canUseSecondary()) {
	    return true;
	}
	return false;

    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("CloudCacheService [primary=").append(primary)
		.append(", secondary=").append(secondary)
		.append(", firstLevelCache=").append(firstLevelCache)
		.append(", l1Hits=").append(l1Hits).append(", l1Missess=")
		.append(l1Missess).append(", isPrimaryOn=").append(isPrimaryOn)
		.append(", isSecondaryOn=").append(isSecondaryOn).append("]");
	return builder.toString();
    }

    @Override
    public CacheListener getCacheListener() {
	return listener;
    }

    @Override
    public void setListener(CacheListener listener) {
	this.listener = listener;
    }

    @Override
    public boolean add(Object key, Serializable obj) {

	if (AopUtils.isAopProxy(obj)) {
	    // Do nothing with proxy object, because it was not modified and no
	    // need to proceed.
	    log.warn("Code is trying to save proxy object, need to throw exception");
	    return Boolean.TRUE;
	}
	try {

	    if (firstLevelCache != null) {
		firstLevelCache.add(key, obj);
	    }

	    boolean result = Boolean.FALSE;
	    if (canUsePrimary()) {
		if (log.isDebugEnabled()) {
		    log.debug("Add to Primary:" + key + "=" + obj);
		}

		result = primary.add(key, obj);
	    }
	    if (canUseSecondary()) {
		if (log.isDebugEnabled()) {
		    log.debug("Add to Secondary:" + key + "=" + obj);
		}

		result |= secondary.add(key, obj);
	    }

	    return result;
	} finally {
	    if (listener != null) {
		listener.put(key, obj);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#incr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long incr(Object key, int by, long defaultValue, int expiration) {
	long r = -1;
	if (firstLevelCache != null) {
	    firstLevelCache.incr(key, by, defaultValue, expiration);
	}

	if (canUsePrimary()) {
	    r = primary.incr(key, by, defaultValue, expiration);
	}
	if (canUseSecondary()) {
	    r = secondary.incr(key, by, defaultValue, expiration);
	}
	return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#decr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long decr(Object key, int by, long defaultValue, int expiration) {
	long r = -1;
	if (firstLevelCache != null) {
	    firstLevelCache.decr(key, by, defaultValue, expiration);
	}

	if (canUsePrimary()) {
	    r = primary.decr(key, by, defaultValue, expiration);
	}
	if (canUseSecondary()) {
	    r = secondary.decr(key, by, defaultValue, expiration);
	}
	return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.salesforce.jigsaw.cache.CacheService#put(java.lang.Object, int,
     * java.io.Serializable) NOTE: this is pretty much a copy paste of putNormal
     */
    @Override
    public void put(Object key, int expiration, Serializable obj) {
	putNormal(key, expiration, obj);
    }

    public ValueProxyFactory getProxyFactory() {
	return proxyFactory;
    }

    public void setProxyFactory(ValueProxyFactory proxyFactory) {
	this.proxyFactory = proxyFactory;
    }

    public CacheListener getListener() {
	return listener;
    }
}
