/*
 * Copyright (c) 2011 by Salesforce.com Inc. All Rights Reserved. This file
 * contains proprietary information of Salesforce.com Inc. Copying, use, reverse
 * engineering, modification or reproduction of this file without prior written
 * approval is prohibited.
 */
package com.salesforce.ddc.threelevelmemcache.exposed;

import net.spy.memcached.transcoders.SerializingTranscoder;


public class NonCompressionTranscoder extends SerializingTranscoder {
    
    public NonCompressionTranscoder () {
        super();
        this.compressionThreshold = Integer.MAX_VALUE;
    }
    
    public NonCompressionTranscoder (int max) {
        super(max);
        this.compressionThreshold = Integer.MAX_VALUE;
    }
    
}
