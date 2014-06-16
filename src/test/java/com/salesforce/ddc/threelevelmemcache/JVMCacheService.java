/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Cache implementation is based on inner Concurrent Map.
 */
public class JVMCacheService extends FirstLevelCacheService {
    
    public JVMCacheService (int size) {
        super(size);
    }
    
    public JVMCacheService () {
        super(Integer.MAX_VALUE);
    }
    
    @Override
    public void remove(Object key) {
        
        String _key = getKey(key);
        Serializable value = cache.remove(_key);
        if (value != null) {
            items.addAndGet(-1);
        }
        
        if (listener != null)
            listener.remove(key);
    }
    
    @Override
    public void put(Object key, Serializable obj) {
        String _key = getKey(key);
        cache.put(_key, obj);
        if (listener != null)
            listener.put(key, obj);
        
    }
    
    @Override
    public void append(Object key, String obj) {
        String normalizedKey = getKey(key);
        if (cache.containsKey(normalizedKey)) {
            String indexList = (String) cache.get(normalizedKey);
            if (StringUtils.isNotBlank(indexList)) {
                cache.put(normalizedKey,
                        new StringBuffer(indexList).append(",").append(obj).toString());
            }
            if (listener != null)
                listener.append(key, obj);
        }
        else {
            put(key, obj);
        }
        
    }
    
    public Map<Object, Serializable> getMap() {
        return cache;
    }
    
    @Override
    protected void free() {
        // nothing, never free
    }
}
