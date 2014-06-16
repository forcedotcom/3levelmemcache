/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class CacheConfigurationCopUnitTestNG extends CacheConfigurationCop {
    
    @DataProvider(name = "conf")
    protected Object[][] conf() {
        return new Object[][] {
            {
                "http://ak.com", "http://ak.com", "b", "b", "", "", false
            }, {
                "http://ak.com", "http://ak.com", "bs", "b", "", "", true
            }, {
                "http://ak1.com", "http://ak.com", "b", "b", "", "", true
            }, {
                "http://ak1.com", "", "b", "b", "", "", true
            }, {
                "", "http://ak1.com", "b", "b", "", "", true
            }, {
                "http://ak.com", "http://ak1.com", "", "", "", "", false
            }, {
                "http://ak.com", "http://ak.com", "", "", "", "", false
            }, {
                "", "", "b", "b", "", "", false
            }, {
                "", "", "b", "b1", "", "", false
            }, {
                "", "", "", "", "list1", "list2", true
            }, {
                "", "", "", "", "list1", "list1", false
            }, {
                "", "", "", "", "", "list1", true
            }, {
                "", "", "", "", "list1", "", true
            }, {
                "", "", "", "", "", "", false
            }
        };
    }
    
    @Test(groups = "unit", description = "Test fields ", dataProvider = "conf")
    public void testTheSame(String p, String s, String pb, String sb, String pnodeList,
            String snodeList, boolean allow) throws Exception {
        this.primaryBucket = pb;
        this.secondaryBucket = sb;
        
        this.primaryURL = p;
        this.secondaryURL = s;
        
        this.primaryNodeList = pnodeList;
        this.secondaryNodeList = snodeList;
        
        boolean exception = true;
        try {
            check();
        }
        catch (Exception e) {
            exception = false;
        }
        Assert.assertEquals(exception, allow,
                "This configuration should be denied on application.");
    }
}
