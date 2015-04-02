package com.b5m.jspackage;

import com.b5m.jsbridge.JSWebviewActivity;
import com.b5m.jsbridge.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by boguang on 15/3/31.
 */
public class JSInterface {

    protected JSWebviewActivity activity;

    public JSInterface setActivity(JSWebviewActivity activity) {
        this.activity = activity;
        return this;
    }

//    public JSInterface setQuery(HashMap<String, String> query) {
//        if (null == query) return this;
//        this.query = query;
//        return this;
//    }

    protected JSONObject jsonQuery(String stringQuery) {
        JSONObject jsonQuery = null;
        try {
            jsonQuery = new JSONObject(stringQuery);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonQuery;
    }

    public final Method handleMessage(String method) {
        try {
            StringBuilder builder = new StringBuilder(methodPrefix()).append(method);
            Method methodItem = this.getClass().getMethod(builder.toString(), new Class[]{HashMap.class});
            if (methodItem != null) {
                return methodItem;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    protected String methodPrefix() {
        return  StringUtils.jsapiMethodPrefix();
    }
}
