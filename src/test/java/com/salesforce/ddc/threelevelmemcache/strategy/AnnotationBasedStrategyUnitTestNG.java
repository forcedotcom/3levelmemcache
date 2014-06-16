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
	return new Object[][] { { "a", new InCache(), true },
		{ "b", new NonCache(), false }, { null, "ANYTHING", false },
		{ "asdsad", null, false }, };
    }

    @Test(groups = "unit", dataProvider = "dataFortestdoNotUseFirstLevelCache")
    public void testdoNotUseFirstLevelCache(String key, Serializable input,
	    boolean expected) {
	Assert.assertEquals(strategy.isCacheable(key, input), expected);
    }
}
