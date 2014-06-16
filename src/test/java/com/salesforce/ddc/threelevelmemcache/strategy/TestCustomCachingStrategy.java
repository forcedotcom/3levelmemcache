/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.strategy;

import java.io.Serializable;

import com.salesforce.ddc.threelevelmemcache.exposed.strategy.CachingStrategy;
import com.salesforce.ddc.threelevelmemcache.exposed.strategy.FirstLevelCacheStrategy;


@FirstLevelCacheStrategy
public class TestCustomCachingStrategy implements CachingStrategy {
    
    @Override
    public boolean isCacheable(Object key, Serializable value) {
        return false;
    }
    
}
