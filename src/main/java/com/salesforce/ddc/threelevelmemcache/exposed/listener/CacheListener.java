/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.exposed.listener;

import java.io.Serializable;


public interface CacheListener {
    
    public void put(Object key, Serializable obj);
    
    public void get(Object key);
    
    public void append(Object key, String obj);
    
    public void remove(Object key);
    
}
