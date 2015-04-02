package com.b5m.jsbridge;

import com.b5m.jspackage.B5MJSNavigator;
import com.b5m.jspackage.JSAPINavigator;
import com.b5m.jspackage.JSApiTitle;
import com.b5m.jspackage.JSInterface;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by boguang on 15/3/30.
 */
public class URIManager {

    private JSWebviewActivity activity;

    private Set<JSInterface> jsInterfaces = new HashSet<JSInterface>();

    public URIManager(JSWebviewActivity activity) {
        this.activity = activity;
        registerAllJSAPI();
    }

    public  boolean dealWithURI(String url) {

        URI sourceURL = null;
        try {
            sourceURL = new URI(url);
        } catch (Exception e ) {
            e.printStackTrace();
        }

        String query = sourceURL.getQuery();
        String scheme = sourceURL.getScheme();

        if (StringUtils.jsScheme().equalsIgnoreCase(scheme)) {
            handleMessage("method", StringUtils.getQuery(query));
            return true;
        }
        else if (StringUtils.urlScheme().equalsIgnoreCase(scheme)) {
            String b5mjsMethod = "_method";
            HashMap<String , String> querys = StringUtils.getQuery(query);
            String host = sourceURL.getHost();
            String path = sourceURL.getPath();
            String page = host;
            if (path != null)
                page = host+path;
            querys.put("_page",page);
            querys.put(b5mjsMethod,"Open");
            handleMessage(b5mjsMethod,querys);
            return true;
        }
        return false;
    }

    private void handleMessage(String methodString, HashMap<String , String> query) {
        String strMethod = query.get(methodString);
        if (null == strMethod)
            return;
        for (JSInterface jsInterface: jsInterfaces) {
            Method method = jsInterface.handleMessage(strMethod);
            if (null != method) {
                jsInterface.setActivity(activity);
                try {
                    method.invoke(jsInterface, new Object[]{query});
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                return;
            }
        }
    }

    public void registerJSAPI(JSInterface jsInterface) {
        jsInterfaces.add(jsInterface);
    }

    private void registerAllJSAPI() {
        registerJSAPI(new JSApiTitle());
        registerJSAPI(new JSAPINavigator());
        registerJSAPI(new B5MJSNavigator());
    }
}
