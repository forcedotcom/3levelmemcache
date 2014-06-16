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
package com.salesforce.ddc.threelevelmemcache.exposed.util;

import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.OperationStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class WaitResponseUtils {

    private static final Log log = LogFactory.getLog(WaitResponseUtils.class);

    private WaitResponseUtils() {

    }

    public static boolean waitForResponse(OperationFuture<Boolean> rv,
	    int tries, String operation) throws Exception {
	OperationStatus status;
	int backoffexp = 0;
	try {
	    do {
		if (backoffexp > tries) {
		    throw new RuntimeException("Could not perform a "
			    + operation + " after " + tries + " tries.");
		}
		status = getStatusWithretriesOnTimeout(rv, 3);

		if (status == null) {
		    throw new IllegalStateException(
			    "Couchbase returned status as null.");
		}

		if (status.isSuccess()) {
		    return Boolean.TRUE;
		} else {
		    if ("remove".equalsIgnoreCase(operation)
			    && ("NOT_FOUND".equals(status.getMessage()) || "Not found"
				    .equals(status.getMessage()))) {
			log.debug("Remove operation failed, object not found it the cache.");
		    } else {
			log.warn(operation + " failed with status=" + status);
		    }
		}
		if (backoffexp > 0) {
		    double backoffMillis = Math.pow(2, backoffexp);
		    backoffMillis = Math.min(1000, backoffMillis); // 1 sec max
		    Thread.sleep((int) backoffMillis);
		    log.info(operation + " backing off, tries so far="
			    + backoffexp);
		}
		backoffexp++;
	    } while ("Temporary failure".equals(status.getMessage()));
	} catch (InterruptedException ex) {
	    log.error("Interrupted while trying to set.  Exception:"
		    + ex.getMessage());
	}
	return Boolean.FALSE;
    }

    private static OperationStatus getStatusWithretriesOnTimeout(
	    OperationFuture<Boolean> rv, int tries) {
	int i = 0;
	boolean shouldRetry = false;
	do {
	    try {
		shouldRetry = false;
		return rv.getStatus();
	    } catch (Exception exception) {
		if (exception.getCause() instanceof CheckedOperationTimeoutException
			|| exception instanceof CheckedOperationTimeoutException) {
		    log.warn("Try[" + (tries - i) + "] Wait for response. Of:"
			    + rv);
		    shouldRetry = true;
		}
	    }

	} while (++i < tries && shouldRetry);

	return rv.getStatus();
    }

}
