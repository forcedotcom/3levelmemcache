/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.exposed.util;

import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.OperationStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class WaitResponseUtils {
    
    private static final Log log = LogFactory.getLog(WaitResponseUtils.class);
    
    private WaitResponseUtils () {
        
    }
    
    public static boolean waitForResponse(OperationFuture<Boolean> rv, int tries,
            String operation) throws Exception {
        OperationStatus status;
        int backoffexp = 0;
        try {
            do {
                if (backoffexp > tries) {
                    throw new RuntimeException("Could not perform a " + operation
                            + " after " + tries + " tries.");
                }
                status = getStatusWithretriesOnTimeout(rv, 3);
                
                if (status == null) {
                    throw new IllegalStateException("Couchbase returned status as null.");
                }
                
                if (status.isSuccess()) {
                    return Boolean.TRUE;
                }
                else {
                    if ("remove".equalsIgnoreCase(operation)
                            && ("NOT_FOUND".equals(status.getMessage()) || "Not found".equals(status.getMessage()))) {
                        log.debug("Remove operation failed, object not found it the cache.");
                    }
                    else {
                        log.warn(operation + " failed with status=" + status);
                    }
                }
                if (backoffexp > 0) {
                    double backoffMillis = Math.pow(2, backoffexp);
                    backoffMillis = Math.min(1000, backoffMillis); // 1 sec max
                    Thread.sleep((int) backoffMillis);
                    log.info(operation + " backing off, tries so far=" + backoffexp);
                }
                backoffexp++;
            } while ("Temporary failure".equals(status.getMessage()));
        }
        catch (InterruptedException ex) {
            log.error("Interrupted while trying to set.  Exception:" + ex.getMessage());
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
            }
            catch (Exception exception) {
                if (exception.getCause() instanceof CheckedOperationTimeoutException
                        || exception instanceof CheckedOperationTimeoutException) {
                    log.warn("Try[" + (tries - i) + "] Wait for response. Of:" + rv);
                    shouldRetry = true;
                }
            }
            
        } while (++i < tries && shouldRetry);
        
        return rv.getStatus();
    }
    
}
