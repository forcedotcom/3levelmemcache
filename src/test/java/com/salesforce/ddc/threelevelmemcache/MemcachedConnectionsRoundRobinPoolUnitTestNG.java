/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
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
            public MemcachedClient answer(InvocationOnMock invocation) throws Throwable {
                MemcachedClient client = mock(MemcachedClient.class);
                when(client.waitForQueues(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(
                        true);
                when(client.getAvailableServers()).thenReturn(Collections.EMPTY_LIST);
                Mockito.doAnswer(new Answer() {
                    
                    private int callCount = 0;
                    
                    @Override
                    public Map answer(InvocationOnMock invocation) throws Throwable {
                        return Collections.singletonMap("size", callCount++);
                    }
                    
                }).when(client).getStats();
                return client;
            }
        });
        
        
        int poolSize = 100;
        MemcachedConnectionsRoundRobinPool rrp = new MemcachedConnectionsRoundRobinPool(
                factory, poolSize, 1);
        
        /*Test every time gives diff object*/
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
            Integer callCount = (Integer) (((Map) client.getStats()).get("size"));
            assertTrue(callCount > 1, "" + (Map) client.getStats());
        }
        /*Close*/
        rrp.close();
    }
}
