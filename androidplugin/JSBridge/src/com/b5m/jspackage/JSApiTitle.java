package com.b5m.jspackage;

import java.util.HashMap;

/**
 * Created by boguang on 15/3/30.
 */
public class JSApiTitle extends JSInterface{

    public void jsapi_setTitle(HashMap<String, String> query) {
        activity.getTitleTextView().setText(jsonQuery(query.get("args")).optString("title",""));
    }
}
