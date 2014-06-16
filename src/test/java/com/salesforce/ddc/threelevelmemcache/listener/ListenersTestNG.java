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

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:threelevelmemcache-context.xml" })
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

	Assert.assertEquals(primaryTestCacheListener.getAppends(),
		Arrays.asList("a"), "Some appends are missing");
	Assert.assertEquals(primaryTestCacheListener.getGets(),
		Arrays.asList(), "Gets should be served from FirstLevelCache");
	Assert.assertEquals(primaryTestCacheListener.getPuts(),
		Arrays.asList("a"), "Some puts are missing");
	Assert.assertEquals(primaryTestCacheListener.getRemove(),
		Arrays.asList("a"), "Some removes are missing");

	Assert.assertEquals(primaryTestCacheListener.getAppends(),
		Arrays.asList("a"), "Some appends are missing");
	Assert.assertEquals(primaryTestCacheListener.getGets(),
		Arrays.asList(), "Gets should be served from FirstLevelCache");
	Assert.assertEquals(primaryTestCacheListener.getPuts(),
		Arrays.asList("a"), "Some puts are missing");
	Assert.assertEquals(primaryTestCacheListener.getRemove(),
		Arrays.asList("a"), "Some removes are missing");

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
