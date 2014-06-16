/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.exposed.proxy;


public class ProxyModificationException extends IllegalAccessException {
    
    public ProxyModificationException () {
        super(
                "Can not modify immutable domain. Make a defensive copy using the copy constructor.");
    }
    
    public ProxyModificationException (String s) {
        super(s);
    }
    
    
}
