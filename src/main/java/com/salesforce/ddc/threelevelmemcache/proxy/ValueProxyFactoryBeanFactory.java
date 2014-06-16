/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.proxy;

import com.salesforce.ddc.threelevelmemcache.AnnotationBeanFactory;
import com.salesforce.ddc.threelevelmemcache.exposed.proxy.ValueProxyFactory;


public class ValueProxyFactoryBeanFactory extends
        AnnotationBeanFactory<ValueProxyFactory> {
    
    public ValueProxyFactoryBeanFactory (Class annotationClass) {
        super(annotationClass);
    }
    
    @Override
    protected ValueProxyFactory getDefaultBean() {
        return new ValueProxyFactory() {
            
            @Override
            public Object createProxy(Object target) {
                return target;
            }
            
        };
    }
    
}
