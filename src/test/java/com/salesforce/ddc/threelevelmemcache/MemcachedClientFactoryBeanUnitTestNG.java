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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

/**
 * @author alexkhimich
 */
public class MemcachedClientFactoryBeanUnitTestNG extends
	MemcachedClientFactoryBean {

    @Override
    protected MemcachedClient getPlainMemcachedConnection(
	    CouchbaseConnectionFactoryBuilder connectionFactoryBuilder)
	    throws IOException {
	return Mockito.mock(MemcachedClient.class);
    }

    @Override
    protected CouchbaseClient getCouchBaseConnection(
	    CouchbaseConnectionFactoryBuilder connectionFactoryBuilder)
	    throws IOException {

	ConnectionFactory factory = connectionFactoryBuilder.build();
	Assert.assertEquals(factory.getOperationTimeout(), 10223);
	assertEquals(factory.getOpQueueMaxBlockTime(), 10223);

	return Mockito.mock(CouchbaseClient.class);
    }

    @Test(groups = "unit")
    public void testEmptyConnectionCreation() throws Exception {

	MemcachedClientFactoryBean clientFactoryBean = new MemcachedClientFactoryBean();
	clientFactoryBean.getObject();
	Assert.assertTrue(clientFactoryBean.getObject() instanceof EmptyMemcachedConnection);

    }

    @Test(groups = "unit")
    public void testPlainConnectionCreation() throws Exception {
	MemcachedClientFactoryBeanUnitTestNG clientFactoryBean = new MemcachedClientFactoryBeanUnitTestNG();
	clientFactoryBean.setServers("127.0.0.1:11211");
	Assert.assertNotNull(clientFactoryBean.getObject());

    }

    @Test(groups = "unit")
    public void testCouchbaseConnectionCreation() throws Exception {
	MemcachedClientFactoryBeanUnitTestNG clientFactoryBean = new MemcachedClientFactoryBeanUnitTestNG();
	clientFactoryBean.setMembaseURL("@#$#@$");
	assertEquals(clientFactoryBean.baseList.size(), 0);
	clientFactoryBean.setMembaseURL("http://lll:9091/aa");
	assertEquals(clientFactoryBean.baseList.size(), 1);

	clientFactoryBean.setOperationTimeout("a1");
	assertEquals(clientFactoryBean.operationTimeout, -1);
	clientFactoryBean.setOperationTimeout("10223");
	assertEquals(clientFactoryBean.operationTimeout, 10223);

	clientFactoryBean.setOpQueueMaxBlockTime("a1");
	assertEquals(clientFactoryBean.opQueueMaxBlockTime, -1);
	clientFactoryBean.setOpQueueMaxBlockTime("10223");
	assertEquals(clientFactoryBean.opQueueMaxBlockTime, 10223);

	clientFactoryBean.setQueueSize("a1");
	assertEquals(clientFactoryBean.queueSize, 16384);
	clientFactoryBean.setQueueSize("10223");
	assertEquals(clientFactoryBean.queueSize, 10223);

    }

    @Test(groups = "unit")
    public void testCouchbaseConnectionCreationWithMultipleURLS()
	    throws Exception {
	MemcachedClientFactoryBeanUnitTestNG clientFactoryBean = new MemcachedClientFactoryBeanUnitTestNG();
	clientFactoryBean.setMembaseURL("@#$#@$");
	assertEquals(clientFactoryBean.baseList.size(), 0);
	clientFactoryBean
		.setMembaseURL("http://lll:9091/aa, http://zzz:9090/ppols, http://fff:1010/pss");
	assertEquals(clientFactoryBean.baseList.size(), 3);
	assertTrue(clientFactoryBean.baseList.contains(new URI(
		"http://lll:9091/aa")));
	assertTrue(clientFactoryBean.baseList.contains(new URI(
		"http://zzz:9090/ppols")));
	assertTrue(clientFactoryBean.baseList.contains(new URI(
		"http://fff:1010/pss")));

    }
}
