/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.CacheService;
import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;
import com.salesforce.ddc.threelevelmemcache.exposed.proxy.ValueProxyFactory;


/**
 * @author Alexander Khimich
 */
@Test(groups = "unit")
public class CloudCacheServiceUnitTestNG {
    
    @Test
    public void testProxyListNPE() {
        CloudCacheService cloudCacheService = new CloudCacheService(null, null, null);
        List<Serializable> data = null;
        cloudCacheService.proxyList(data);
    }
    
    @Test
    public void testProxyList() {
        CloudCacheService cloudCacheService = new CloudCacheService(null, null, null);
        ValueProxyFactory proxyFactory = Mockito.mock(ValueProxyFactory.class);
        cloudCacheService.setProxyFactory(proxyFactory);
        List data = Arrays.asList(new Integer(1), new Integer(2));
        cloudCacheService.proxyList(data);
        Mockito.verify(proxyFactory).createProxy(new Integer(1));
        Mockito.verify(proxyFactory).createProxy(new Integer(2));
    }
    
    @Test
    public void testStats() {
        FirstLevelCacheService cacheService = new FirstLevelCacheService();
        cacheService.put("a", "b");
        AssertJUnit.assertEquals(cacheService.getStats().size(), 1);
        AssertJUnit.assertEquals(cacheService.isConnected(), true);
        AssertJUnit.assertEquals(cacheService.isSynchronousPut(), true);
    }
    
    @Test
    public void testPut() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        cacheService.put("a", "b");
        Mockito.verify(listener).put(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(jvm).put(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString());
        Mockito.verify(primary).isConnected();
        Mockito.verify(primary).put(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString());
        
        Mockito.verify(secondary).put(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyString());
        Mockito.verify(secondary).isConnected();
        
    }
    
    @Test
    public void testAdd() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        Mockito.when(secondary.add("a", "b")).thenReturn(true);
        
        AssertJUnit.assertEquals(cacheService.add("a", "b"), true);
        Mockito.verify(listener).put("a", "b");
        Mockito.verify(jvm).add("a", "b");
        Mockito.verify(primary).isConnected();
        Mockito.verify(primary).add("a", "b");
        
        Mockito.verify(secondary).isConnected();
        Mockito.verify(secondary).add("a", "b");
        
        // If secondary is off
        Mockito.reset(primary, jvm, secondary, listener);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(false);
        
        Mockito.when(primary.add("a", "b")).thenReturn(false);
        AssertJUnit.assertEquals(cacheService.add("a", "b"), false);
        Mockito.verify(listener).put("a", "b");
        Mockito.verify(jvm).add("a", "b");
        Mockito.verify(primary).isConnected();
        Mockito.verify(primary).add("a", "b");
        
        Mockito.verify(secondary).isConnected();
    }
    
    @Test
    public void testAppend() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        cacheService.append("a", "b");
        Mockito.verify(listener).append(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(jvm).append(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(primary).isConnected();
        Mockito.verify(primary).append(Mockito.anyString(), Mockito.anyString());
        
        Mockito.verify(secondary).append(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(secondary).isConnected();
    }
    
    @Test
    public void testsetSynchronousPut() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        
        cacheService.setSynchronousPut(true);
        Mockito.verify(jvm).setSynchronousPut(true);
        Mockito.verify(primary).setSynchronousPut(true);
        Mockito.verify(secondary).setSynchronousPut(true);
    }
    
    @Test
    public void testShutdown() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        
        cacheService.shutdown();
        Mockito.verify(jvm).shutdown();
        Mockito.verify(primary).shutdown();
        Mockito.verify(secondary).shutdown();
    }
    
    @Test
    public void testSize() {
        
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(null, primary, secondary);
        AssertJUnit.assertEquals(cacheService.size(), 0);
        cacheService = new CloudCacheService(new FirstLevelCacheService(2), primary,
                secondary);
        cacheService.put("a", "b");
        AssertJUnit.assertEquals(cacheService.size(), 1);
    }
    
    
    @Test
    public void testRemove() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        cacheService.remove("a");
        Mockito.verify(listener).remove("a");
        Mockito.verify(jvm).remove("a");
        Mockito.verify(primary).remove("a");
        Mockito.verify(secondary).remove("a");
        
    }
    
    @Test
    public void testGet() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        ValueProxyFactory proxyFactory = Mockito.mock(ValueProxyFactory.class);
        cacheService.setProxyFactory(proxyFactory);
        Mockito.when(proxyFactory.createProxy("b")).thenReturn("b");
        Mockito.when(proxyFactory.createProxy("c")).thenReturn("c");
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        
        Mockito.when(primary.get("a")).thenReturn("b");
        
        AssertJUnit.assertEquals(cacheService.get("a"), "b");
        Mockito.verify(jvm).get("a");
        Mockito.verify(jvm).put("a", "b");
        Mockito.reset(jvm, listener);
        
        cacheService.get("a");
        Mockito.verify(jvm).get("a");
        Mockito.verify(listener).get("a");
        
        // when primary is off
        Mockito.reset(jvm, primary, secondary, listener);
        Mockito.when(primary.isConnected()).thenReturn(false);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        Mockito.when(secondary.get("a")).thenReturn("c");
        AssertJUnit.assertEquals(cacheService.get("a"), "c");
        
    }
    
    @Test
    public void testGetBatchWithCacheNotFound() {
        CacheService jvm = new JVMCacheService();
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(secondary.isConnected()).thenReturn(false); // Set
                                                                 // secondary
                                                                 // and
                                                                 // primary
                                                                 // cache to
                                                                 // null
        Mockito.when(primary.isConnected()).thenReturn(false);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        try {
            cacheService.getBatch((List) Arrays.asList("b3")); // NPE handled in
                                                               // the code.
                                                               // Fails if not
        }
        catch (NullPointerException e) {
            Assert.fail("NullPointerException thrown..");
        }
    }
    
    @Test
    public void testGetBatchAndProxy() {
        CacheService jvm = new JVMCacheService();
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(primary.getBatch(Mockito.anyList())).thenReturn(
                (List) Arrays.asList("b3"));
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        // check is proxy is created
        ValueProxyFactory pf = Mockito.mock(ValueProxyFactory.class);
        Mockito.when(pf.createProxy("b")).thenReturn("b");
        Mockito.when(pf.createProxy("b2")).thenReturn("b2");
        Mockito.when(pf.createProxy("b3")).thenReturn("b3");
        cacheService.setProxyFactory(pf);
        
        cacheService.put("a", "b");
        cacheService.put("a2", "b2");
        AssertJUnit.assertEquals(cacheService.getBatch(Arrays.asList("a", "a3", "a2")),
                Arrays.asList("b", "b3", "b2"));
        Mockito.verify(primary).getBatch(Arrays.asList("a3"));// only nulls will
                                                              // be queries
                                                              // from
                                                              // primary
        
        Mockito.verify(pf).createProxy("b");
        Mockito.verify(pf).createProxy("b2");
        Mockito.verify(pf).createProxy("b3");
        
        // if first level cahce is null
        Mockito.reset(pf, secondary, primary);
        
        Mockito.when(pf.createProxy("b")).thenReturn("b");
        Mockito.when(pf.createProxy("b2")).thenReturn("b2");
        Mockito.when(pf.createProxy("b3")).thenReturn("b3");
        Mockito.when(primary.isConnected()).thenReturn(true);
        cacheService = new CloudCacheService(null, primary, secondary);
        cacheService.setProxyFactory(pf);
        // if secondary is off
        Mockito.when(secondary.isConnected()).thenReturn(false);
        Mockito.when(primary.getBatch(Mockito.anyList())).thenReturn(
                (List) Arrays.asList("b", "b3", "b2"));
        
        AssertJUnit.assertEquals(cacheService.getBatch(Arrays.asList("a", "a3", "a2")),
                Arrays.asList("b", "b3", "b2"));
        Mockito.verify(pf).createProxy("b");
        Mockito.verify(pf).createProxy("b2");
        Mockito.verify(pf).createProxy("b3");
        Mockito.verify(secondary, Mockito.never()).getBatch(Mockito.anyList());
        
        // Primary is on
        // if secondary is off
        Mockito.reset(pf, secondary, primary);
        Mockito.when(pf.createProxy("b")).thenReturn("b");
        Mockito.when(pf.createProxy("b2")).thenReturn("b2");
        Mockito.when(pf.createProxy("b3")).thenReturn("b3");
        Mockito.when(secondary.isConnected()).thenReturn(false);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(primary.getBatch(Mockito.anyList())).thenReturn(
                (List) Arrays.asList("b", "b3", "b2"));
        
        AssertJUnit.assertEquals(cacheService.getBatch(Arrays.asList("a", "a3", "a2")),
                Arrays.asList("b", "b3", "b2"));
        Mockito.verify(pf).createProxy("b");
        Mockito.verify(pf).createProxy("b2");
        Mockito.verify(pf).createProxy("b3");
        
    }
    
    @Test
    public void testPutBatch() {
        CacheService jvm = new JVMCacheService();
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        Map map = new HashMap<Object, Serializable>();
        map.put("a", "b");
        map.put("a2", "b2");
        cacheService.putBatch(map);
        AssertJUnit.assertEquals(cacheService.get("a"), "b");
        AssertJUnit.assertEquals(cacheService.get("a2"), "b2");
        Mockito.verify(listener, Mockito.times(2)).put(Mockito.anyString(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.times(2)).get(Mockito.anyString());
        Mockito.verify(listener, Mockito.times(0)).remove(Mockito.anyString());
        Mockito.verify(listener, Mockito.times(0)).append(Mockito.anyString(),
                Mockito.anyString());
    }
    
    @Test
    public void testRemoveBatch() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        cacheService.removeBatch((Collection) Arrays.asList("a", "b"));
        Mockito.verify(listener).remove("a");
        Mockito.verify(listener).remove("b");
        Mockito.verify(jvm).remove("a");
        Mockito.verify(jvm).remove("b");
        Mockito.verify(primary).remove("a");
        Mockito.verify(primary).remove("b");
        Mockito.verify(secondary).remove("a");
        Mockito.verify(secondary).remove("b");
    }
    
    @Test
    public void testIncrDecr() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        AssertJUnit.assertEquals(cacheService.incr("a"), 0);
        AssertJUnit.assertEquals(cacheService.decr("a"), 0);
        Mockito.verify(jvm).incr("a");
        Mockito.verify(primary).incr("a");
        Mockito.verify(secondary).incr("a");
        Mockito.verify(jvm).decr("a");
        Mockito.verify(primary).decr("a");
        Mockito.verify(secondary).decr("a");
    }
    
    @Test
    public void testIncrDecrWithExpirationFromPrimary() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        
        Mockito.when(
                primary.incr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(20l);
        Mockito.when(
                primary.decr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(-20l);
        
        Mockito.when(
                secondary.incr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(2l);
        Mockito.when(
                secondary.decr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(-2l);
        
        AssertJUnit.assertEquals(cacheService.incr("a", 1, 0, 0), 2);
        AssertJUnit.assertEquals(cacheService.decr("a", 2, 2, 2), -2l);
        
        Mockito.verify(jvm).incr("a", 1, 0, 0);
        Mockito.verify(primary).incr("a", 1, 0, 0);
        Mockito.verify(secondary).incr("a", 1, 0, 0);
        
        Mockito.verify(jvm).decr("a", 2, 2, 2);
        Mockito.verify(primary).decr("a", 2, 2, 2);
        Mockito.verify(secondary).decr("a", 2, 2, 2);
        
        Mockito.verify(listener, Mockito.never()).get(Mockito.anyObject());
        Mockito.verify(listener, Mockito.never()).put(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).append(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).remove(Mockito.anyString());
        
        Mockito.verify(jvm, Mockito.never()).add(Mockito.anyObject(), Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).put(Mockito.anyObject(), Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).append(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).remove(Mockito.anyString());
    }
    
    @Test
    public void testIncrDecrWithExpirationFromSecondary() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        
        Mockito.when(primary.isConnected()).thenReturn(false);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        
        Mockito.when(
                secondary.incr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(2l);
        Mockito.when(
                secondary.decr(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyInt())).thenReturn(-2l);
        
        AssertJUnit.assertEquals(cacheService.incr("a", 1, 0, 0), 2);
        AssertJUnit.assertEquals(cacheService.decr("a", 2, 2, 2), -2l);
        
        Mockito.verify(jvm).incr("a", 1, 0, 0);
        Mockito.verify(primary, Mockito.never()).incr("a", 1, 0, 0);
        Mockito.verify(secondary).incr("a", 1, 0, 0);
        
        Mockito.verify(jvm).decr("a", 2, 2, 2);
        Mockito.verify(primary, Mockito.never()).decr("a", 2, 2, 2);
        Mockito.verify(secondary).decr("a", 2, 2, 2);
        
        Mockito.verify(listener, Mockito.never()).get(Mockito.anyObject());
        Mockito.verify(listener, Mockito.never()).put(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).append(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).remove(Mockito.anyString());
        
        Mockito.verify(jvm, Mockito.never()).add(Mockito.anyObject(), Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).put(Mockito.anyObject(), Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).append(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(jvm, Mockito.never()).remove(Mockito.anyString());
    }
    
    @Test
    public void testIncrDecrDefaultResponse() {
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        
        Mockito.when(primary.isConnected()).thenReturn(false);
        Mockito.when(secondary.isConnected()).thenReturn(false);
        
        CloudCacheService cacheService = new CloudCacheService(null, primary, secondary);
        CacheListener listener = Mockito.mock(CacheListener.class);
        cacheService.setListener(listener);
        AssertJUnit.assertEquals(cacheService.incr("a", 1, 0, 0), -1);
        AssertJUnit.assertEquals(cacheService.decr("a", 2, 2, 2), -1);
        
        Mockito.verify(primary, Mockito.never()).incr("a", 1, 0, 0);
        Mockito.verify(secondary, Mockito.never()).incr("a", 2, 2, 2);
        
        Mockito.verify(primary, Mockito.never()).decr("a", 1, 0, 0);
        Mockito.verify(secondary, Mockito.never()).decr("a", 2, 2, 2);
        
        Mockito.verify(listener, Mockito.never()).get(Mockito.anyObject());
        Mockito.verify(listener, Mockito.never()).put(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).append(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(listener, Mockito.never()).remove(Mockito.anyString());
        
    }
    
    @Test
    public void testIsConnected() {
        CacheService jvm = Mockito.mock(CacheService.class);
        CacheService primary = Mockito.mock(CacheService.class);
        CacheService secondary = Mockito.mock(CacheService.class);
        CloudCacheService cacheService = new CloudCacheService(jvm, primary, secondary);
        
        Mockito.when(primary.isConnected()).thenReturn(true);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        AssertJUnit.assertEquals(cacheService.isConnected(), true);
        Mockito.verify(primary).isConnected();
        
        Mockito.reset(primary, secondary);
        Mockito.when(primary.isConnected()).thenReturn(false);
        Mockito.when(secondary.isConnected()).thenReturn(true);
        AssertJUnit.assertEquals(cacheService.isConnected(), true);
        Mockito.verify(primary, Mockito.times(2)).isConnected();
        Mockito.verify(secondary).isConnected();
        
        Mockito.reset(primary, secondary);
        Mockito.when(primary.isConnected()).thenReturn(false);
        Mockito.when(secondary.isConnected()).thenReturn(false);
        AssertJUnit.assertEquals(cacheService.isConnected(), false);
        Mockito.verify(primary, Mockito.times(2)).isConnected();
        Mockito.verify(secondary).isConnected();
        
        Assert.assertNotNull(cacheService.toString());
        
    }
}
