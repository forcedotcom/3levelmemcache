/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.FirstLevelCacheService;


@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
@ContextConfiguration(locations = {
    "classpath:threelevelmemcache-context.xml"
})
public class CachingStrategyTestNG extends AbstractTestNGSpringContextTests {
    
    @Autowired
    FirstLevelCacheService cacheService;
    
    
    @Test(groups = "functional")
    public void testDeployment() throws Exception {
        
        Assert.assertNotNull(cacheService.getCachingStrategy(),
                "default strategy should be provided");
        boolean equals = cacheService.getCachingStrategy().getClass().equals(
                DefaultAnnotationBasedCachingStrategy.class);
        Assert.assertTrue(equals);
    }
    
}
