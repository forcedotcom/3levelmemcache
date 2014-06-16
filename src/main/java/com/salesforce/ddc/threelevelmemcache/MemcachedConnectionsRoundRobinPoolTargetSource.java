/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import org.apache.commons.pool.ObjectPool;
import org.springframework.aop.target.CommonsPoolTargetSource;



public class MemcachedConnectionsRoundRobinPoolTargetSource extends CommonsPoolTargetSource {
    
    private int timeBetweenKeepAliveRunsSecs = 0;
    
    
    protected ObjectPool createObjectPool() {
        MemcachedConnectionsRoundRobinPool gop = new MemcachedConnectionsRoundRobinPool(this, getMaxSize(),
                getTimeBetweenKeepAliveRunsSecs());
        return gop;
    }
    
    
    public int getTimeBetweenKeepAliveRunsSecs() {
        return timeBetweenKeepAliveRunsSecs;
    }
    
    
    public void setTimeBetweenKeepAliveRunsSecs(int timeBetweenKeepAliveRunsSecs) {
        this.timeBetweenKeepAliveRunsSecs = timeBetweenKeepAliveRunsSecs;
    }
    
}
