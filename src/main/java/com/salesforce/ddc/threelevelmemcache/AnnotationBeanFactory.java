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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

public abstract class AnnotationBeanFactory<T> implements
	ApplicationContextAware {

    private final Class annotationClass;

    public AnnotationBeanFactory(Class annotationClass) {
	super();
	this.annotationClass = annotationClass;
    }

    private ApplicationContext applicationContext;

    protected abstract T getDefaultBean();

    public T getObject() {
	Map<String, Object> beansWithAnnotation = applicationContext
		.getBeansWithAnnotation(annotationClass);
	if (MapUtils.isEmpty(beansWithAnnotation)) {
	    return getDefaultBean();
	}

	if (beansWithAnnotation.size() > 1) {
	    Assert.isTrue(false, "More then one bean with annotation "
		    + annotationClass + " has been found. Beans="
		    + beansWithAnnotation.keySet());
	}

	Entry<String, Object> entry = beansWithAnnotation.entrySet().iterator()
		.next();
	Object bean = entry.getValue();
	return (T) bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
	    throws BeansException {
	this.applicationContext = applicationContext;

    }

}
