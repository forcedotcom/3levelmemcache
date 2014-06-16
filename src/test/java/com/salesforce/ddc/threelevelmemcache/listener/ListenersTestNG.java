/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.listener;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.CacheService;


@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
@ContextConfiguration(locations = {
    "classpath:threelevelmemcache-context.xml"
})
@DirtiesContext
public class ListenersTestNG extends AbstractTestNGSpringContextTests {
    
    @Autowired
    CacheService cacheService;
    @Autowired
    CloudTestCacheListener cloudTestCacheListener;
    @Autowired
    PrimaryTestCacheListener primaryTestCacheListener;
    @Autowired
    SecondaryTestCacheListener secondaryTestCacheListener;
    @Autowired
    FirstLevelTestCacheListener firstLevelTestCacheListener;
    
    @Test(groups = "functional")
    public void testListeners() throws Exception {
        cloudTestCacheListener.reset();
        primaryTestCacheListener.reset();
        secondaryTestCacheListener.reset();
        firstLevelTestCacheListener.reset();
        cacheService.put("a", "a");
        cacheService.append("a", "b");
        cacheService.get("a");
        cacheService.remove("a");
        
        
        assertsEverything(cloudTestCacheListener);
        assertsEverything(firstLevelTestCacheListener);
        
        Assert.assertEquals(primaryTestCacheListener.getAppends(), Arrays.asList("a"),
                "Some appends are missing");
        Assert.assertEquals(primaryTestCacheListener.getGets(), Arrays.asList(),
                "Gets should be served from FirstLevelCache");
        Assert.assertEquals(primaryTestCacheListener.getPuts(), Arrays.asList("a"),
                "Some puts are missing");
        Assert.assertEquals(primaryTestCacheListener.getRemove(), Arrays.asList("a"),
                "Some removes are missing");
        
        Assert.assertEquals(primaryTestCacheListener.getAppends(), Arrays.asList("a"),
                "Some appends are missing");
        Assert.assertEquals(primaryTestCacheListener.getGets(), Arrays.asList(),
                "Gets should be served from FirstLevelCache");
        Assert.assertEquals(primaryTestCacheListener.getPuts(), Arrays.asList("a"),
                "Some puts are missing");
        Assert.assertEquals(primaryTestCacheListener.getRemove(), Arrays.asList("a"),
                "Some removes are missing");
        
        
    }
    
    private void assertsEverything(AbstractTestCacheListener cacheListener) {
        Assert.assertEquals(cacheListener.getAppends(), Arrays.asList("a"),
                "Some appends are missing");
        Assert.assertEquals(cacheListener.getGets(), Arrays.asList("a"),
                "Some gets are missing");
        Assert.assertEquals(cacheListener.getPuts(), Arrays.asList("a"),
                "Some puts are missing");
        Assert.assertEquals(cacheListener.getRemove(), Arrays.asList("a"),
                "Some removes are missing");
    }
}
