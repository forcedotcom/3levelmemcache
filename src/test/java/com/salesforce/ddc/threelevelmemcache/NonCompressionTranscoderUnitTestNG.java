/*
 * Copyright (c) 2011 by Salesforce.com Inc. All Rights Reserved. This file
 * contains proprietary information of Salesforce.com Inc. Copying, use, reverse
 * engineering, modification or reproduction of this file without prior written
 * approval is prohibited.
 */
package com.salesforce.ddc.threelevelmemcache;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.salesforce.ddc.threelevelmemcache.exposed.NonCompressionTranscoder;


@Test(groups = "unit")
public class NonCompressionTranscoderUnitTestNG extends NonCompressionTranscoder {
    
    
    public NonCompressionTranscoderUnitTestNG () {
        super();
    }
    
    public NonCompressionTranscoderUnitTestNG (int max) {
        super(max);
    }
    
    public void testAll() throws Exception {
        NonCompressionTranscoderUnitTestNG compressionTranscoder = new NonCompressionTranscoderUnitTestNG();
        Assert.assertEquals(compressionTranscoder.compressionThreshold, Integer.MAX_VALUE);
        NonCompressionTranscoderUnitTestNG compressionTranscoder2 = new NonCompressionTranscoderUnitTestNG(
                1);
        Assert.assertEquals(compressionTranscoder2.compressionThreshold,
                Integer.MAX_VALUE);
    }
}
