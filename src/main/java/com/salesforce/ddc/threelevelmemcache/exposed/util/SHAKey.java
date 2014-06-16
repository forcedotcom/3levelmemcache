/*
 * Copyright (c) 2011 by Salesforce.com Inc.  All Rights Reserved.
 * This file contains proprietary information of Salesforce.com Inc.
 * Copying, use, reverse engineering, modification or reproduction of
 * this file without prior written approval is prohibited.
 *
 */
package com.salesforce.ddc.threelevelmemcache.exposed.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SHAKey {
    
    private static final Log log = LogFactory.getLog(SHAKey.class);
    private static MessageDigest keyEncrypt;
    
    static {
        try {
            keyEncrypt = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't create keyEncrypter", e);
        }
    }
    
    
    public static String sha(Object unprefixedKey) {
        if (unprefixedKey == null) {
            return null;
        }
        try {
            return Hex.encodeHexString(((MessageDigest) keyEncrypt.clone()).digest(unprefixedKey.toString().getBytes(
                    "UTF-8")));
        }
        catch (Exception e) {
            log.error("Can't create Memcached key for value:" + unprefixedKey
                    + " . Return default SHA key.", e);
            return DigestUtils.shaHex(unprefixedKey.toString());
        }
    }
}
