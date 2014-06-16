/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import java.io.Serializable;

import com.salesforce.ddc.threelevelmemcache.exposed.listener.CacheListener;


public class CacheListenerAnnotationBeanFactory extends
        AnnotationBeanFactory<CacheListener> {
    
    public CacheListenerAnnotationBeanFactory (Class annotationClass) {
        super(annotationClass);
    }
    
    @Override
    protected CacheListener getDefaultBean() {
        return new CacheListener() {
            
            @Override
            public void remove(Object key) {
                
            }
            
            @Override
            public void put(Object key, Serializable obj) {
                
            }
            
            @Override
            public void get(Object key) {
                
            }
            
            @Override
            public void append(Object key, String obj) {
                
            }
        };
    }
    
}
