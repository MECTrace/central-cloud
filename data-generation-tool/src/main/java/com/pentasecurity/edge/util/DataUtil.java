package com.pentasecurity.edge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUtil {
	static Logger logger = LoggerFactory.getLogger("mainLogger");

	static final String chars = "abcdefghijklmnopqrstuvwzyg0123456789";

	static public String make(int length) {
        String str = "";

        try {
            StringBuilder builder = new StringBuilder();

            for(int i=0;i<length;i++) {
            	int index = (int)Math.floor(chars.length() * Math.random());
            	char c = chars.charAt(index);
               	builder.append(c);
            }

            str = builder.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return str;
    }
}