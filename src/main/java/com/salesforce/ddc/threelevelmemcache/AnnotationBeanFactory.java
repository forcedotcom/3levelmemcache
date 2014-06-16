/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;


public abstract class AnnotationBeanFactory<T> implements ApplicationContextAware {
    
    private final Class annotationClass;
    
    public AnnotationBeanFactory (Class annotationClass) {
        super();
        this.annotationClass = annotationClass;
    }
    
    private ApplicationContext applicationContext;
    
    protected abstract T getDefaultBean();
    
    public T getObject() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationClass);
        if (MapUtils.isEmpty(beansWithAnnotation)) {
            return getDefaultBean();
        }
        
        if (beansWithAnnotation.size() > 1) {
            Assert.isTrue(false, "More then one bean with annotation " + annotationClass
                    + " has been found. Beans=" + beansWithAnnotation.keySet());
        }
        
        
        Entry<String, Object> entry = beansWithAnnotation.entrySet().iterator().next();
        Object bean = entry.getValue();
        return (T) bean;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
        
    }
    
}
