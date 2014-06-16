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

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Cache implementation is based on inner Concurrent Map.
 */
public class JVMCacheService extends FirstLevelCacheService {

    public JVMCacheService(int size) {
	super(size);
    }

    public JVMCacheService() {
	super(Integer.MAX_VALUE);
    }

    @Override
    public void remove(Object key) {

	String _key = getKey(key);
	Serializable value = cache.remove(_key);
	if (value != null) {
	    items.addAndGet(-1);
	}

	if (listener != null)
	    listener.remove(key);
    }

    @Override
    public void put(Object key, Serializable obj) {
	String _key = getKey(key);
	cache.put(_key, obj);
	if (listener != null)
	    listener.put(key, obj);

    }

    @Override
    public void append(Object key, String obj) {
	String normalizedKey = getKey(key);
	if (cache.containsKey(normalizedKey)) {
	    String indexList = (String) cache.get(normalizedKey);
	    if (StringUtils.isNotBlank(indexList)) {
		cache.put(normalizedKey, new StringBuffer(indexList)
			.append(",").append(obj).toString());
	    }
	    if (listener != null)
		listener.append(key, obj);
	} else {
	    put(key, obj);
	}

    }

    public Map<Object, Serializable> getMap() {
	return cache;
    }

    @Override
    protected void free() {
	// nothing, never free
    }
}
