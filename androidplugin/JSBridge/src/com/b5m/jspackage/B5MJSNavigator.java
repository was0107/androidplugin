package com.b5m.jspackage;

import android.content.Intent;
import com.b5m.jsbridge.JSWebviewActivity;
import com.b5m.jsbridge.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by boguang on 15/4/2.
 */
public class B5MJSNavigator extends JSInterface{

    public void b5mjs_Open(HashMap<String, String> query) {
        boolean modal = false;
        boolean animated = true;
        if (query.containsKey("modal")) modal = new Boolean(query.get("modal")).booleanValue();
        if (query.containsKey("animated")) animated = new Boolean(query.get("animated")).booleanValue();
        String page = query.get("_page");
        query.remove("_page");
        query.remove("_method");
        b5mjs_Open(page, new JSONObject(query), modal, animated);
    }

    public void b5mjs_Open(String page, JSONObject jsonObject, boolean modal, boolean animated) {

        //TODO YOU CAN LOAD THE CORRESPONSE ACTIVTY FOR THE PAGE HERE


        //JUST LOAD THE WEBVIEWACTIVITY HERE
        Intent intent = new Intent(activity, JSWebviewActivity.class);
        intent.putExtra("page", page);
        intent.putExtra("pageQuery", jsonObject.toString());
        activity.startActivity(intent);
        //TODO SET THE ACTIVITY ANIMATION HERE

//        activity.overridePendingTransition(R.anim.activity_out_left,R.anim.activity_out_right);

    }

    @Override
    protected String methodPrefix() {
        return  StringUtils.b5mjsapiMethodPrefix();
    }

}
