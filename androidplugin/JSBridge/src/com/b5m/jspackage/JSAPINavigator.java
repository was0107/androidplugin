package com.b5m.jspackage;

import android.content.Intent;
import com.b5m.jsbridge.JSWebviewActivity;
import com.b5m.jsbridge.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by boguang on 15/3/31.
 */
public class JSAPINavigator extends JSInterface {
    public void jsapi_actionOpen(HashMap<String, String> query) {
        Intent intent = new Intent(activity, JSWebviewActivity.class);
        intent.putExtra("page",jsonQuery(query.get("args")).optString("page", ""));

        JSONObject jsonObject = jsonQuery(query.get("args"));
        boolean modal = false;
        boolean animated = true;

        String page = jsonObject.optString("page","");
        B5MJSNavigator b5MJSNavigator = new B5MJSNavigator();
        b5MJSNavigator.setActivity(activity);
        b5MJSNavigator.b5mjs_Open(page,jsonObject,modal,true);
    }

    public void jsapi_actionBack(HashMap<String, String> query) {
        activity.finish();
    }
    public void jsapi_actionDismiss(HashMap<String, String> query) {
        activity.finish();
    }
    public void jsapi_actionGetQuery(HashMap<String, String> query) {
        activity.jsCallback(jsonQuery(query.get("args")).optString("callbackId", "0"));
    }
}
