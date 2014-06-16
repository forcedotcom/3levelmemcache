/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.strategy;

import java.io.Serializable;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.NotUseFirstLevelCacheDomain;


public class AnnotationBasedStrategyUnitTestNG {
    
    @NotUseFirstLevelCacheDomain
    public static class NonCache implements Serializable {
        
    }
    
    public static class InCache implements Serializable {
        
    }
    
    DefaultAnnotationBasedCachingStrategy strategy = new DefaultAnnotationBasedCachingStrategy();
    
    @DataProvider(name = "dataFortestdoNotUseFirstLevelCache")
    Object[][] dataFortestdoNotUseFirstLevelCache() {
        return new Object[][] {
            {
                "a", new InCache(), true
            }, {
                "b", new NonCache(), false
            }, {
                null, "ANYTHING", false
            }, {
                "asdsad", null, false
            },
        };
    }
    
    @Test(groups = "unit", dataProvider = "dataFortestdoNotUseFirstLevelCache")
    public void testdoNotUseFirstLevelCache(String key, Serializable input,
            boolean expected) {
        Assert.assertEquals(strategy.isCacheable(key, input), expected);
    }
}
