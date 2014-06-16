/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
public class CacheConfigurationCop {
    
    @Value("${memcached.membase.primary.URL}")
    protected String primaryURL;
    @Value("${memcached.membase.secondary.URL}")
    protected String secondaryURL;
    @Value("${memcached.membase.primary.bucketName}")
    protected String primaryBucket;
    @Value("${memcached.membase.secondary.bucketName}")
    protected String secondaryBucket;
    @Value("${memcached.secondary.nodeAddresses}")
    protected String secondaryNodeList;
    @Value("${memcached.primary.nodeAddresses}")
    protected String primaryNodeList;
    
    
    @PostConstruct
    protected void check() {
        if (StringUtils.isNotBlank(primaryURL) && StringUtils.isNotBlank(secondaryURL)
                && StringUtils.equalsIgnoreCase(primaryURL, secondaryURL)
                && StringUtils.equalsIgnoreCase(primaryBucket, secondaryBucket)) {
            Assert.isTrue(
                    false,
                    "You can't have the same URL and bucketName for primary and secondary server.Set the primary and leave the secondary blank.\nmemcached.membase.primary.URL="
                            + primaryURL
                            + "\nmemcached.membase.secondary.URL="
                            + secondaryURL
                            + "\nmemcached.membase.primary.bucketName="
                            + primaryBucket
                            + "\nmemcached.membase.secondary.bucketName="
                            + secondaryBucket);
        }
        
        
        if (StringUtils.isNotBlank(secondaryNodeList)
                && StringUtils.isNotBlank(primaryNodeList)
                && StringUtils.equalsIgnoreCase(secondaryNodeList, primaryNodeList)) {
            Assert.isTrue(
                    false,
                    "You can't have the same node list for primary and secondary server. Set the primary and leave the secondary blank.");
        }
        
        
        if (StringUtils.isBlank(primaryURL) && StringUtils.isBlank(secondaryURL)
                && StringUtils.isBlank(secondaryNodeList)
                && StringUtils.isBlank(primaryNodeList)) {
            Assert.isTrue(
                    false,
                    "The primary memcached server has not been specified. Application will not work without cache.");
        }
        
        if (StringUtils.isNotBlank(primaryURL) && StringUtils.isNotBlank(secondaryURL)
                && StringUtils.isBlank(primaryBucket)
                && StringUtils.isBlank(secondaryBucket)) {
            Assert.isTrue(
                    false,
                    "The primary memcached server has not been specified. Application will not work without cache.");
        }
        
        if (StringUtils.isBlank(primaryURL) && StringUtils.isNotBlank(secondaryURL)
                && StringUtils.isBlank(primaryBucket)
                && StringUtils.isNotBlank(secondaryBucket)) {
            Assert.isTrue(
                    false,
                    "The primary memcached server has not been specified, but secondary was. We don't allow empty primary.");
        }
    }
    
}
