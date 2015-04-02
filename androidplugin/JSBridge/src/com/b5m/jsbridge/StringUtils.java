package com.b5m.jsbridge;

import java.net.URI;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Created by boguang on 15/3/30.
 */
public class StringUtils {

    public static HashMap<String, String> getQuery(String query) {
        HashMap<String, String> dic = new HashMap<String, String>(){};
        if (query != null) {
            String[] splits = query.split("[&]");
            if (splits.length <= 1)
                return dic;
            for (int i = 0, total = splits.length; i < total; i++) {
                String[] equals = splits[i].split("[=]");
                dic.put(equals[0], equals[1]);
            }
        }
        return dic;
    }

    public static String getString(HashMap<String, String> map) {
        String resutl = "";



        return resutl;
    }

    public static String jsScheme() {
        return "js";
    }

    public static String urlScheme() {
        return "b5mjs";
    }

    public static String jsapiMethodPrefix() {
        return "jsapi_";
    }

    public static String b5mjsapiMethodPrefix() {
        return "b5mjs_";
    }

}
