/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.strategy;

import com.salesforce.ddc.threelevelmemcache.AnnotationBeanFactory;
import com.salesforce.ddc.threelevelmemcache.exposed.strategy.CachingStrategy;


public class CachingStrategyAnnotationBeanFactory extends
        AnnotationBeanFactory<CachingStrategy> {
    
    private static final DefaultAnnotationBasedCachingStrategy defaultAnnotationBasedCachingStrategy = new DefaultAnnotationBasedCachingStrategy();
    
    public CachingStrategyAnnotationBeanFactory (Class annotationClass) {
        super(annotationClass);
    }
    
    @Override
    protected CachingStrategy getDefaultBean() {
        return defaultAnnotationBasedCachingStrategy;
    }
    
}
