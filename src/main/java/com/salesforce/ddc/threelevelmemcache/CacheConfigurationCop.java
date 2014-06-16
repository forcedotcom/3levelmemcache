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
	if (StringUtils.isNotBlank(primaryURL)
		&& StringUtils.isNotBlank(secondaryURL)
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
		&& StringUtils.equalsIgnoreCase(secondaryNodeList,
			primaryNodeList)) {
	    Assert.isTrue(
		    false,
		    "You can't have the same node list for primary and secondary server. Set the primary and leave the secondary blank.");
	}

	if (StringUtils.isBlank(primaryURL)
		&& StringUtils.isBlank(secondaryURL)
		&& StringUtils.isBlank(secondaryNodeList)
		&& StringUtils.isBlank(primaryNodeList)) {
	    Assert.isTrue(
		    false,
		    "The primary memcached server has not been specified. Application will not work without cache.");
	}

	if (StringUtils.isNotBlank(primaryURL)
		&& StringUtils.isNotBlank(secondaryURL)
		&& StringUtils.isBlank(primaryBucket)
		&& StringUtils.isBlank(secondaryBucket)) {
	    Assert.isTrue(
		    false,
		    "The primary memcached server has not been specified. Application will not work without cache.");
	}

	if (StringUtils.isBlank(primaryURL)
		&& StringUtils.isNotBlank(secondaryURL)
		&& StringUtils.isBlank(primaryBucket)
		&& StringUtils.isNotBlank(secondaryBucket)) {
	    Assert.isTrue(
		    false,
		    "The primary memcached server has not been specified, but secondary was. We don't allow empty primary.");
	}
    }

}
