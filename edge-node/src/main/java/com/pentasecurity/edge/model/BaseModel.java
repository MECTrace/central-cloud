package com.pentasecurity.edge.model;

import com.google.gson.Gson;

public class BaseModel {
    static public <T extends BaseModel> T fromJson(String json, Class<T> clazz) {
        Gson gson = new Gson();
        T obj = null;

        try {
            obj = gson.fromJson(json, clazz);
        } catch (Exception e) {

        }

        if ( obj == null ) {
        	try {
                obj = clazz.newInstance();
        	} catch (Exception e) {
        		
        	}
        }

        return obj;
    }

    public String toJson() {
        Gson gson = new Gson();
        String json = null;

        try {
            json = gson.toJson(this);
        } catch (Exception e) {

        }

        return json;
    }
}