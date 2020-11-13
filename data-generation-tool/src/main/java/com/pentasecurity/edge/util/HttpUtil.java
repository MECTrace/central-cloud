package com.pentasecurity.edge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
	static Logger logger = LoggerFactory.getLogger("mainLogger");

	static public String post(String url, String json) {
        String responseString = "";

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            responseString = response.body().string();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return responseString;
    }

    static public String get(String url) {
        String responseString = "";

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();

            responseString = response.body().string();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return responseString;
    }
}