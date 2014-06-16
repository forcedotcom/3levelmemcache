/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 */

package com.salesforce.ddc.threelevelmemcache.exposed;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;


/**
 * Interface describes CacheService
 * 
 * @author Alexander Khimich
 */
public interface CacheService {
    
    
    /**
     * Gets a value from cache. If maxTimeToGet is greater than 0, null will be
     * returned if getting from cache takes more than maxTimeToGet seconds Do
     * not use for fetching counters.
     * 
     * @param key
     *            key
     * @return an object or null if not found or timeout was reached
     */
    Serializable get(Object key);
    
    /**
     * Adds a value identified by key to the cache. Returns true if successful,
     * and false if object already exists or add operation failed
     * 
     * @param key
     *            key
     * @param obj
     *            value
     */
    boolean add(Object key, Serializable obj);
    
    /**
     * Puts a value identified by key to the cache.
     * 
     * @param key
     *            key
     * @param obj
     *            value
     */
    void put(Object key, Serializable obj);
    
    /**
     * Puts a value identified by key to the cache.
     * 
     * @param key
     * @param expiration
     * @param obj
     * @return
     */
    void put(Object key, int expiration, Serializable obj);
    
    /**
     * Do append of index
     * 
     * @param key
     * @param obj
     */
    void append(Object key, String obj);
    
    /**
     * Do increment +1 of counter
     * 
     * @param key
     * @param obj
     * @return the new value, or -1 if we were unable to increment or add
     */
    long incr(Object key);
    
    /**
     * Do increment +by of counter
     * 
     * @param key
     * @param by
     * @param defaultValue
     * @return the new value
     */
    long incr(Object key, int by, long defaultValue, int expiration);
    
    /**
     * Do decrement counter
     * 
     * @param key
     * @param obj
     * @return the new value, or -1 if we were unable to dencrement or add
     */
    long decr(Object key);
    
    /**
     * @param key
     * @param by
     * @param defaultValue
     * @param expiration
     * @return the new value
     */
    long decr(Object key, int by, long defaultValue, int expiration);
    
    /**
     * Puts bunch of serializable objects into cache.
     * 
     * @param keys
     *            array of keys
     * @param objs
     *            array of stored objects, keys[i] corresponds objs[i]
     */
    void putBatch(List<? extends Object> keys, List<? extends Serializable> objs);
    
    /**
     * Puts bunch of serializable objects into cache.
     * 
     * @param objs
     *            map of stored pairs <key,object>
     */
    void putBatch(Map<? extends Object, ? extends Serializable> objs);
    
    /**
     * Takes bunch of serializable objects from cache.
     * 
     * @param keys
     *            array of keys
     * @return array of stored objects, result[i] corresponds keys[i], item can
     *         be null if stored object is not found
     */
    List<? extends Serializable> getBatch(List<? extends Object> keys);
    
    /**
     * Removes entry from cache.
     * 
     * @param key
     *            key of entry
     */
    void remove(Object key);
    
    /**
     * Removes batch of entries from cache.
     * 
     * @param keys
     *            entries' keys
     */
    void removeBatch(Collection<Object> keys);
    
    /**
     * Shutdowns cache service immediately.
     */
    void shutdown();
    
    /**
     * Flush cache if need.
     */
    void flush();
    
    /**
     * Get total cache size
     * 
     * @return
     */
    long size();
    
    /**
     * @return
     */
    boolean isSynchronousPut();
    
    /**
     * Use only for memcached server to make sure remote server is reachable
     * 
     * @return
     */
    boolean isConnected();
    
    void setSynchronousPut(boolean synchronousPut);
    
    /**
     * Get Cache Statistic map
     * 
     * @return
     */
    Map<SocketAddress, Map<String, String>> getStats();
    
    
    CacheListener getCacheListener();
    
    void setListener(CacheListener listener);
    
}
