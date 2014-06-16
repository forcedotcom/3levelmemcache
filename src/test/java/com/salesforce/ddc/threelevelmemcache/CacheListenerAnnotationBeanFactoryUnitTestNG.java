/*
 * Copyright (c) 2013 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test(groups = "unit")
public class CacheListenerAnnotationBeanFactoryUnitTestNG {
    
    public void testAll() throws Exception {
        CacheListenerAnnotationBeanFactory annotationBeanFactory = new CacheListenerAnnotationBeanFactory(
                Object.class);
        Assert.assertNotNull(annotationBeanFactory.getDefaultBean());
    }
    
}
