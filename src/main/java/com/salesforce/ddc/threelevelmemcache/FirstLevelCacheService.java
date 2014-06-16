/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salesforce.ddc.threelevelmemcache.exposed.AdminCacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.CacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;
import com.salesforce.ddc.threelevelmemcache.exposed.strategy.CachingStrategy;
import com.salesforce.ddc.threelevelmemcache.strategy.DefaultAnnotationBasedCachingStrategy;


/**
 * In-memory JVM cache
 * 
 * @author Alexander Khimich
 */

public class FirstLevelCacheService implements CacheService, AdminCacheService {
    
    /**
     * Logger
     */
    private static Log log = LogFactory.getLog(FirstLevelCacheService.class);
    /**
     * The cache map.
     */
    protected int size = 1000;
    protected AtomicLong items = new AtomicLong(0l);
    protected ConcurrentHashMap<Object, Serializable> cache = new ConcurrentHashMap<Object, Serializable>(
            size, 0.75F, 50);
    protected Queue<String> keys = new ConcurrentLinkedQueue<String>();
    protected CacheListener listener;
    protected CachingStrategy cachingStrategy = new DefaultAnnotationBasedCachingStrategy();
    
    
    public FirstLevelCacheService (int size) {
        this.size = size;
    }
    
    public FirstLevelCacheService () {
    }
    
    @Override
    public void append(Object key, String obj) {
        if (key == null || size == 0 || !cachingStrategy.isCacheable(key, obj)) {
            return;
        }
        
        String normalizedKey = getKey(key);
        String indexList = (String) cache.get(normalizedKey);
        if (StringUtils.isNotBlank(indexList)) {
            cache.replace(normalizedKey,
                    new StringBuffer(indexList).append(",").append(obj).toString());
        }
        
        if (listener != null)
            listener.append(key, obj);
    }
    
    protected String getKey(Object unprefixedKey) {
        return unprefixedKey.toString();
    }
    
    @Override
    public Serializable get(Object key) {
        if (key == null) {
            return null;
        }
        
        try {
            return cache.get(getKey(key));
        }
        finally {
            if (listener != null)
                listener.get(key);
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public List<Serializable> getBatch(List keys) {
        List<Serializable> result = new ArrayList<Serializable>();
        for (Object key : keys) {
            result.add(get(key));
        }
        return result;
    }
    
    @Override
    public Map<SocketAddress, Map<String, String>> getStats() {
        Map<SocketAddress, Map<String, String>> stats = new HashMap<SocketAddress, Map<String, String>>();
        SocketAddress server = new InetSocketAddress(0);
        Map<String, String> s = new LinkedHashMap<String, String>();
        s.put("maxSize", String.valueOf(size));
        s.put("items", String.valueOf(items));
        s.put("isSynchronousPut", String.valueOf(isSynchronousPut()));
        stats.put(server, s);
        return stats;
    }
    
    @Override
    public boolean isSynchronousPut() {
        return true;
    }
    
    @Override
    public void put(Object key, Serializable obj) {
        
        if (key == null || size == 0 || !cachingStrategy.isCacheable(key, obj)) {
            return;
        }
        
        try {
            String _key = getKey(key);
            // if exist - just update in the cache.
            if (cache.put(_key, obj) == null) {
                keys.add(_key);
                if (items.addAndGet(1) > size) {
                    free();
                }
            }
        }
        finally {
            if (listener != null)
                listener.put(key, obj);
        }
    }
    
    protected void free() {
        if (cache.remove(keys.poll()) != null) {
            items.addAndGet(-1);
        }
    }
    
    @Override
    public void putBatch(List<? extends Object> keys, List<? extends Serializable> objs) {
        for (int i = 0; i < keys.size(); i++) {
            put(keys.get(i), objs.get(i));
        }
    }
    
    @Override
    public void putBatch(Map<? extends Object, ? extends Serializable> objs) {
        for (Map.Entry<? extends Object, ? extends Serializable> entry : objs.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void remove(Object key) {
        try {
            if (size == 0) {
                return;
            }
            String _key = getKey(key);
            Serializable value = cache.remove(_key);
            keys.remove(_key);
            if (value != null) {
                items.addAndGet(-1);
            }
        }
        finally {
            if (listener != null)
                listener.remove(key);
        }
    }
    
    @Override
    public void removeBatch(Collection<Object> keys) {
        for (Object key : keys) {
            remove(key);
        }
    }
    
    @Override
    public void setSynchronousPut(boolean synchronousPut) {
    }
    
    @Override
    public void shutdown() {
        this.cache.clear();
        this.keys.clear();
        this.items = new AtomicLong(0);
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void clearAll() {
        cache.clear();
        keys.clear();
        items = new AtomicLong(0);
    }
    
    @Override
    public long size() {
        return cache.size();
    }
    
    @Override
    public long incr(Object key) {
        String _key = getKey(key);
        /*
         * Ak: to avoid synchronization problems remove from FirstLevelCache, so
         * next time real value comes from memcached.
         */
        cache.remove(_key);
        return -1;
    }
    
    @Override
    public long decr(Object key) {
        String _key = getKey(key);
        /*
         * Ak: to avoid synchronization problems remove from FirstLevelCache, so
         * next time real value comes from memcached.
         */
        cache.remove(_key);
        return -1;
    }
    
    @Override
    public boolean isConnected() {
        return true;
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
        String _key = getKey(key);
        /*
         * Ak: to avoid synchronization problems remove from FirstLevelCache, so
         * next time real value comes from memcached.
         */
        cache.remove(_key);
        return Boolean.TRUE;
    }
    
    /*
     * (non-Javadoc)
     * @see com.salesforce.jigsaw.cache.CacheService#incr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long incr(Object key, int by, long defaultValue, int expiration) {
        // simple remove the key from the cache as in incr(key);
        return incr(key);
    }
    
    /*
     * (non-Javadoc)
     * @see com.salesforce.jigsaw.cache.CacheService#decr(java.lang.Object, int,
     * long, int)
     */
    @Override
    public long decr(Object key, int by, long defaultValue, int expiration) {
        // simple remove the key from the cache as in decr(key);
        return decr(key);
    }
    
    /*
     * (non-Javadoc)
     * @see com.salesforce.jigsaw.cache.CacheService#put(java.lang.Object, int,
     * java.io.Serializable)
     */
    @Override
    public void put(Object key, int expiration, Serializable obj) {
        put(key, obj);
    }
    
    
    public CachingStrategy getCachingStrategy() {
        return cachingStrategy;
    }
    
    
    public void setCachingStrategy(CachingStrategy cachingStrategy) {
        this.cachingStrategy = cachingStrategy;
    }
    
}
