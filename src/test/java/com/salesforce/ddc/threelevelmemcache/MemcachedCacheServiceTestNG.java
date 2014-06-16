/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import javax.annotation.Resource;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;


@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class
})
@ContextConfiguration(locations = {
    "classpath:threelevelmemcache-context.xml"
})
public class MemcachedCacheServiceTestNG extends AbstractTestNGSpringContextTests {
    
    private final static String FIRST_TEST_KEY = "FIRST_CAS_TEST_KEY";
    private final static String SECOND_TEST_KEY = "SECOND_CAS_TEST_KEY";
    
    @Resource(name = "cacheServiceMemcachedPrimary")
    MemcachedCacheService cacheService;
    
    @AfterMethod(groups = "functional")
    public void clean() {
        cacheService.remove(FIRST_TEST_KEY);
        cacheService.remove(SECOND_TEST_KEY);
    }
    
    @Test(groups = "functional")
    public void testCas() {
        cacheService.put(FIRST_TEST_KEY, "0");
        CASValue<Object> casValue = cacheService.gets(FIRST_TEST_KEY);
        CASResponse casResponse = cacheService.cas(FIRST_TEST_KEY, casValue.getCas(), "1");
        Assert.assertEquals(casResponse, CASResponse.OK);
        Assert.assertEquals((String) cacheService.get(FIRST_TEST_KEY), "1");
        casValue = cacheService.gets(FIRST_TEST_KEY);
        cacheService.put(FIRST_TEST_KEY, "2");
        casResponse = cacheService.cas(FIRST_TEST_KEY, casValue.getCas(), "3");
        Assert.assertEquals(casResponse, CASResponse.EXISTS);
        Assert.assertEquals((String) cacheService.get(FIRST_TEST_KEY), "2");
        casValue = cacheService.gets(FIRST_TEST_KEY);
        casResponse = cacheService.cas(SECOND_TEST_KEY, casValue.getCas(), "4");
        Assert.assertEquals(casResponse, CASResponse.NOT_FOUND);
    }
    
}
