package com.pentasecurity.edge.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

public class EdgeLogUtil {
	static public void log(String objId, String mid1, String from, String to, String data, boolean isOnTrace) {
		try {
			DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			String date = format.format(Calendar.getInstance().getTime());
			String message = date+" "+String.format("%10s %10s %30s %s", objId, from, to, data);
			String mid2 = isOnTrace ? "traceOn" : "traceOff";

			log("./logs/"+objId+"."+mid1+"."+mid2+".all", message);
			log("./logs/"+objId+"."+mid1+"."+mid2+".raw", data);
			log("./logs/"+objId+".all."+mid2+".all", message);
			log("./logs/"+objId+".all."+mid2+".raw", data);
			log("./logs/all."+mid1+"."+mid2+".all", message);
			log("./logs/all."+mid1+"."+mid2+".raw", data);
			log("./logs/all.all."+mid2+".all", message);
			log("./logs/all.all."+mid2+".raw", data);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	static public void log(String objId, String msg, boolean isOnTrace) {
		try {
			DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			String date = format.format(Calendar.getInstance().getTime());
			String message = date+" "+String.format("%10s %s", objId, msg);
			String mid2 = isOnTrace ? "traceOn" : "traceOff";

			log("./logs/"+objId+".msg.all", message);
			log("./logs/"+objId+".all."+mid2+".all", message);
			log("./logs/all.msg."+mid2+".all", message);
			log("./logs/all.all."+mid2+".all", message);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	static private void log(String filename, String message) {
		try {
			File file = new File(filename);
			FileUtils.writeByteArrayToFile(file, message.getBytes(), true);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}