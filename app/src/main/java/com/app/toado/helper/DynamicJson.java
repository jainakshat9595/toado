package com.app.toado.helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ghanendra on 13/06/2017.
 */

public class DynamicJson {

    public ArrayList<String> parseJsonFblikes(JSONObject data) {
        ArrayList<String> sarr = new ArrayList<>();
        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                JSONObject json = null;

                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray arry = data.getJSONArray(key);
                        int size = arry.length();
                        for (int i = 0; i < size; i++) {
                            parseJsonFblikes(arry.getJSONObject(i));
                        }

                    } else if (data.get(key) instanceof JSONObject) {
                        parseJsonFblikes(data.getJSONObject(key));
                    } else {
//                        System.out.println(data.getString("id")+"term id1 " + data.getString("name"));
                        sarr.add(data.getString("name"));
                    }
                } catch (Throwable e) {
                    try {
                        System.out.println(key + ":" + data.getString(key));
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();

                }
            }
        }
        return sarr;
    }

}
