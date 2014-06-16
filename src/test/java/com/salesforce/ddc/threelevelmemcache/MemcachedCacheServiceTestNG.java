/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:threelevelmemcache-context.xml" })
public class MemcachedCacheServiceTestNG extends
	AbstractTestNGSpringContextTests {

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
	CASResponse casResponse = cacheService.cas(FIRST_TEST_KEY,
		casValue.getCas(), "1");
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
