/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.listener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;


@Component
public abstract class AbstractTestCacheListener implements CacheListener {
    
    private final List<Object> puts = new LinkedList<Object>();
    private final List<Object> gets = new LinkedList<Object>();
    private final List<Object> appends = new LinkedList<Object>();
    private final List<Object> remove = new LinkedList<Object>();
    
    @Override
    public void put(Object key, Serializable obj) {
        puts.add(key);
    }
    
    @Override
    public void get(Object key) {
        gets.add(key);
    }
    
    @Override
    public void append(Object key, String obj) {
        appends.add(key);
    }
    
    @Override
    public void remove(Object key) {
        remove.add(key);
    }
    
    
    public List<Object> getPuts() {
        return puts;
    }
    
    
    public List<Object> getGets() {
        return gets;
    }
    
    
    public List<Object> getAppends() {
        return appends;
    }
    
    
    public List<Object> getRemove() {
        return remove;
    }
    
    
    public void reset() {
        this.puts.clear();
        this.gets.clear();
        this.appends.clear();
        this.remove.clear();
    }
    
    
}
