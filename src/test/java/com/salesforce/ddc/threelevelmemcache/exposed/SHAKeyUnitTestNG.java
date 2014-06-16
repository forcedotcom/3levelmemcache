/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.exposed;

import org.apache.commons.codec.digest.DigestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.util.SHAKey;


/**
 * @author Alexander Khimich
 */
@Test(groups = "unit")
public class SHAKeyUnitTestNG {
    
    public void testSHA() throws Exception {
        
        String data = "some";
        System.out.println(SHAKey.sha(data));
        Assert.assertEquals(SHAKey.sha(data), DigestUtils.shaHex(data));
        Assert.assertEquals(SHAKey.sha(null), null);
        
    }
}
