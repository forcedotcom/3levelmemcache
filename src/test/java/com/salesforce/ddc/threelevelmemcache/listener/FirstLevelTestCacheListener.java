/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.listener;

import org.springframework.stereotype.Component;

import com.salesforce.ddc.threelevelmemcache.exposed.listener.FirstLevelCacheListener;


@Component
@FirstLevelCacheListener
public class FirstLevelTestCacheListener extends AbstractTestCacheListener {
}
