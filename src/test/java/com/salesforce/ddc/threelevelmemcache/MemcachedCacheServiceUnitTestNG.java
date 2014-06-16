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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.NodeLocator;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetCompletionListener;
import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationStatus;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.util.SHAKey;
import com.salesforce.ddc.threelevelmemcache.exposed.util.WaitResponseUtils;

public class MemcachedCacheServiceUnitTestNG {

    private final static String TEST_KEY = "TEST_KEY";

    @Test(groups = "unit")
    public void testMemcachedException() {
	MemcachedClientIF failingMemcachedClient = mock(MemcachedClientIF.class);
	when(failingMemcachedClient.gets(anyString())).thenThrow(
		new RuntimeException("Exception waiting for value",
			new ExecutionException("Operation timed out", null)));
	when(failingMemcachedClient.cas(anyString(), anyLong(), anyObject()))
		.thenThrow(
			new RuntimeException("Exception waiting for value",
				new ExecutionException("Operation timed out",
					null)));
	MemcachedCacheService failingCacheService = new MemcachedCacheService(
		failingMemcachedClient);

	CASValue<Object> casValue = failingCacheService.gets(TEST_KEY);
	Assert.assertNull(casValue);
	CASResponse casResponse = failingCacheService.cas(TEST_KEY, 0L, "1");
	Assert.assertNull(casResponse);
    }

    @Test(groups = "unit")
    public void testGetBulk() {
	MemcachedClientIF mc = mock(MemcachedClientIF.class);
	List<String> keys = Arrays.asList(new String[] { "A", "B", "C" });
	final Map<String, Object> mapToBeReturned = new HashMap<String, Object>();

	for (String string : keys) {
	    mapToBeReturned.put(SHAKey.sha(string), string);
	}

	when(mc.asyncGetBulk(Mockito.anyCollection())).thenReturn(
		new BulkFuture<Map<String, Object>>() {

		    @Override
		    public boolean isDone() {
			return true;
		    }

		    @Override
		    public boolean isCancelled() {
			return false;
		    }

		    @Override
		    public Map<String, Object> get(long timeout, TimeUnit unit)
			    throws InterruptedException, ExecutionException,
			    TimeoutException {
			throw new TimeoutException("Exception");

		    }

		    @Override
		    public Map<String, Object> get()
			    throws InterruptedException, ExecutionException {
			return mapToBeReturned;
		    }

		    @Override
		    public boolean cancel(boolean mayInterruptIfRunning) {
			// TODO Auto-generated method stub
			return false;
		    }

		    @Override
		    public boolean isTimeout() {
			// TODO Auto-generated method stub
			return false;
		    }

		    @Override
		    public OperationStatus getStatus() {
			return new OperationStatus(true, "");
		    }

		    @Override
		    public Map<String, Object> getSome(long timeout,
			    TimeUnit unit) throws InterruptedException,
			    ExecutionException {
			// TODO Auto-generated method stub
			return null;
		    }

		    @Override
		    public Future<Map<String, Object>> addListener(
			    BulkGetCompletionListener listener) {
			return null;
		    }

		    @Override
		    public Future<Map<String, Object>> removeListener(
			    BulkGetCompletionListener listener) {
			return null;
		    }
		});
	when(mc.delete(Mockito.anyString())).thenReturn(
		new OperationFuture<Boolean>("", new CountDownLatch(0), 1000,
			Executors.newSingleThreadExecutor()) {

		    @Override
		    public boolean isDone() {
			// TODO Auto-generated method stub
			return false;
		    }

		    @Override
		    public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		    }

		    @Override
		    public Boolean get(long timeout, TimeUnit unit)
			    throws InterruptedException, ExecutionException,
			    TimeoutException {
			throw new TimeoutException("Exception");
		    }

		    @Override
		    public Boolean get() throws InterruptedException,
			    ExecutionException {
			// TODO Auto-generated method stub
			return null;
		    }

		    @Override
		    public boolean cancel(boolean mayInterruptIfRunning) {
			// TODO Auto-generated method stub
			return false;
		    }

		    @Override
		    public OperationStatus getStatus() {
			return new OperationStatus(true, "");
		    }

		});

	MemcachedCacheService cs = new MemcachedCacheService(mc);

	List<Serializable> valuesFromCache = cs.getBatch(keys);
	for (Serializable value : valuesFromCache) {
	    Assert.assertNull(value);
	}
	cs.removeBatch((List) keys);

    }

    class ListAppender extends AppenderSkeleton {

	private final List<LoggingEvent> events = new LinkedList<LoggingEvent>();

	public List<LoggingEvent> getEvents() {
	    return events;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
	    return false;
	}

	@Override
	protected void append(LoggingEvent event) {
	    events.add(event);
	}
    }

    @Test(groups = "unit")
    public void testWaitForResponse() {

	ListAppender appender;
	Logger cLog = LogManager.getLogger(WaitResponseUtils.class);
	appender = new ListAppender();
	cLog.setLevel(Level.INFO);
	cLog.addAppender(appender);

	OperationFuture<Boolean> rv = mock(OperationFuture.class);
	Operation operation = null;
	try {
	    when(rv.getStatus()).thenThrow(
		    new RuntimeException("e",
			    new CheckedOperationTimeoutException("DUMMY",
				    operation)));
	    WaitResponseUtils.waitForResponse(rv, 3, "test");
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // this is expected test flow, ignore the expected runtime
	    // exception, do not print unnecessary stack trace
	}

	List<LoggingEvent> events = appender.getEvents();
	int i = 3;

	assertTrue(events.size() > 0, "No retries found");
	for (LoggingEvent loggingEvent : events) {
	    Assert.assertTrue(loggingEvent.getMessage().toString()
		    .startsWith("Try[" + i + "] Wait for response."));
	    i--;
	}

    }

    @Test(groups = "unit")
    public void testWaitForResponseNoRetries() {

	ListAppender appender;
	Logger cLog = LogManager.getLogger(MemcachedCacheService.class);
	appender = new ListAppender();
	cLog.setLevel(Level.INFO);
	cLog.addAppender(appender);

	MemcachedCacheService cs = new MemcachedCacheService(null);

	OperationFuture<Boolean> rv = mock(OperationFuture.class);
	Operation operation = null;
	try {
	    when(rv.getStatus()).thenThrow(new RuntimeException("e"));
	    WaitResponseUtils.waitForResponse(rv, 3, "test");
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // this is expected test flow, ignore the expected runtime
	    // exception, do not print unnecessary stack trace
	}

	List<LoggingEvent> events = appender.getEvents();
	assertTrue(events.size() == 0, "Should be No retries");
    }

    @Test(groups = "unit")
    public void testIsConnected() {
	MemcachedClientIF connection = Mockito.mock(MemcachedClientIF.class);
	NodeLocator nodelocator = Mockito.mock(NodeLocator.class);
	Mockito.when(connection.getNodeLocator()).thenReturn(nodelocator);
	MemcachedCacheService cs = new MemcachedCacheService(connection);

	Assert.assertTrue(cs.isConnected());

	Mockito.when(connection.getNodeLocator()).thenReturn(null);
	Assert.assertFalse(cs.isConnected());

    }

}
