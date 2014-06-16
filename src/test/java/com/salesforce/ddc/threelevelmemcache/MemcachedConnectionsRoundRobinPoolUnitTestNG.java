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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.pool.PoolableObjectFactory;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MemcachedConnectionsRoundRobinPoolUnitTestNG {

    @Test(groups = "unit")
    public void testRoundRobin() throws Exception {
	PoolableObjectFactory factory = mock(PoolableObjectFactory.class);
	when(factory.makeObject()).thenAnswer(new Answer<MemcachedClient>() {

	    @Override
	    public MemcachedClient answer(InvocationOnMock invocation)
		    throws Throwable {
		MemcachedClient client = mock(MemcachedClient.class);
		when(
			client.waitForQueues(Mockito.anyLong(),
				Mockito.any(TimeUnit.class))).thenReturn(true);
		when(client.getAvailableServers()).thenReturn(
			Collections.EMPTY_LIST);
		Mockito.doAnswer(new Answer() {

		    private int callCount = 0;

		    @Override
		    public Map answer(InvocationOnMock invocation)
			    throws Throwable {
			return Collections.singletonMap("size", callCount++);
		    }

		}).when(client).getStats();
		return client;
	    }
	});

	int poolSize = 100;
	MemcachedConnectionsRoundRobinPool rrp = new MemcachedConnectionsRoundRobinPool(
		factory, poolSize, 1);

	/* Test every time gives diff object */
	MemcachedClient previousClient = (MemcachedClient) rrp.borrowObject();
	for (int i = 0; i < 10000; i++) {
	    MemcachedClient client = (MemcachedClient) rrp.borrowObject();
	    Assert.assertNotSame(previousClient, client);
	    previousClient = client;
	}
	Thread.sleep(2000);// wait 2 sec, give time for timer to be executed

	for (int i = 0; i < poolSize; i++) {
	    MemcachedClient client = (MemcachedClient) rrp.borrowObject();
	    // make sure every client was called more than one time
	    Integer callCount = (Integer) (((Map) client.getStats())
		    .get("size"));
	    assertTrue(callCount > 1, "" + (Map) client.getStats());
	}
	/* Close */
	rrp.close();
    }
}
