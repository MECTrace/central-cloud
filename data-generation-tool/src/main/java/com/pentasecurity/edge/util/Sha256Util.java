package com.pentasecurity.edge.util;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sha256Util {
	static Logger logger = LoggerFactory.getLogger("mainLogger");

	static public String sha256(String message) {
        String hash = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            StringBuilder builder = new StringBuilder();
            messageDigest.update(message.getBytes());

            for(byte b : messageDigest.digest()) {
            	builder.append(String.format("%02x", b));
            }

            hash = builder.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return hash;
    }
}