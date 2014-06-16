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

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.OperationFactory;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationQueueFactory;
import net.spy.memcached.protocol.ascii.AsciiOperationFactory;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

public class MemcachedClientFactoryBean {

    private static Log log = LogFactory
	    .getLog(MemcachedClientFactoryBean.class);

    private String servers;

    protected List<URI> baseList = new ArrayList<URI>();

    private String bucketName = "default";

    private boolean isBinary;

    protected int operationTimeout;

    protected int opQueueMaxBlockTime;

    protected int queueSize = DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN;

    static {
	setupViewmode();
	setupLogger();
    }

    public static void setupViewmode() {
	System.setProperty("viewmode", "production");
    }

    public static void setupLogger() {
	System.setProperty("net.spy.log.LoggerImpl",
		"net.spy.memcached.compat.log.Log4JLogger");
    }

    public MemcachedClientIF getObject() throws Exception {
	try {
	    CouchbaseConnectionFactoryBuilder connectionFactoryBuilder = new CouchbaseConnectionFactoryBuilder();
	    connectionFactoryBuilder
		    .setFailureMode(CouchbaseConnectionFactory.DEFAULT_FAILURE_MODE);
	    connectionFactoryBuilder
		    .setHashAlg(CouchbaseConnectionFactory.DEFAULT_HASH);

	    connectionFactoryBuilder.setOpQueueFactory(new CustomQueueFactory(
		    queueSize));

	    if (opQueueMaxBlockTime > 0) {
		connectionFactoryBuilder
			.setOpQueueMaxBlockTime(opQueueMaxBlockTime);
	    }
	    if (operationTimeout > 0) {
		connectionFactoryBuilder.setOpTimeout(operationTimeout);
	    }
	    if (baseList.size() > 0) {
		return getCouchBaseConnection(connectionFactoryBuilder);
	    } else if (StringUtils.isNotBlank(servers)) {
		return getPlainMemcachedConnection(connectionFactoryBuilder);
	    } else {
		return createEmptyConnection();
	    }
	} catch (NullPointerException npe) {
	    log.error("Can't connect to membase baseList:" + baseList
		    + ", bucketName:" + bucketName);

	    throw new RuntimeException(
		    "Can't create memcache client, check your properties", npe);
	}

    }

    private class CustomQueueFactory implements OperationQueueFactory {

	private final int queueSize;

	public CustomQueueFactory(int queueSize) {
	    this.queueSize = queueSize;
	}

	@Override
	public BlockingQueue<Operation> create() {
	    // the default is 16384, we've gone up by two orders of binary
	    // magnitude
	    // memory usage is approximately key size + value size + a bit more
	    return new ArrayBlockingQueue<Operation>(queueSize);
	}

    }

    protected CouchbaseClient getCouchBaseConnection(
	    CouchbaseConnectionFactoryBuilder connectionFactoryBuilder)
	    throws IOException {
	CouchbaseClient couchbaseClient = new CouchbaseClient(
		connectionFactoryBuilder.buildCouchbaseConnection(baseList,
			bucketName, StringUtils.EMPTY));
	couchbaseClient.addObserver(new ConnectionObserver() {

	    @Override
	    public void connectionLost(SocketAddress arg0) {
		log.info("Connection lost to " + arg0);

	    }

	    @Override
	    public void connectionEstablished(SocketAddress arg0, int arg1) {
		log.info("Connection established " + arg0 + ", arg1=" + arg1);

	    }
	});
	return couchbaseClient;
    }

    protected MemcachedClient getPlainMemcachedConnection(
	    CouchbaseConnectionFactoryBuilder connectionFactoryBuilder)
	    throws IOException {
	return new MemcachedClient(connectionFactoryBuilder.setOpFact(
		getOperationFactory()).build(), AddrUtil.getAddresses(servers));
    }

    private OperationFactory getOperationFactory() {
	if (isBinary) {
	    return new BinaryOperationFactory();
	} else {
	    return new AsciiOperationFactory();
	}
    }

    private MemcachedClientIF createEmptyConnection() {
	return new EmptyMemcachedConnection();
    }

    public void setServers(final String servers) {
	this.servers = servers;
    }

    public void setBinary(boolean isBinary) {
	this.isBinary = isBinary;
    }

    public void setMembaseURL(String commaSeparatedUrls) {
	if (StringUtils.isBlank(commaSeparatedUrls)) {
	    return;
	}
	try {
	    String[] urls = StringUtils.split(commaSeparatedUrls, ",");
	    for (String url : urls) {
		url = StringUtils.trimToNull(url);
		if (url != null) {
		    baseList.add(new URI(url));
		}
	    }
	} catch (URISyntaxException e) {
	    log.error("Bad membase URLs specified:" + commaSeparatedUrls, e);
	}
	// shuffle the URIs to prevent load on 1 node
	Collections.shuffle(baseList);
    }

    public void setBucketName(String bucketName) {
	this.bucketName = bucketName;
    }

    public void setOperationTimeout(String operationTimeout) {
	this.operationTimeout = NumberUtils.toInt(operationTimeout,
		NumberUtils.INTEGER_MINUS_ONE);
    }

    public void setOpQueueMaxBlockTime(String opQueueMaxBlockTime) {
	this.opQueueMaxBlockTime = NumberUtils.toInt(opQueueMaxBlockTime,
		NumberUtils.INTEGER_MINUS_ONE);
    }

    public void setQueueSize(String queueSize) {
	this.queueSize = NumberUtils.toInt(queueSize,
		DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN);
    }
}
