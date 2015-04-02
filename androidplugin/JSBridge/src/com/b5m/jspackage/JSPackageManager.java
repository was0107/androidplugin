package com.b5m.jspackage;

import android.content.Context;
import android.content.res.AssetManager;


/**
 * Created by boguang on 15/3/31.
 */
public class JSPackageManager {
    private static JSPackageManager manager;
    private static String prefix = "file:///android_asset/h5pages/";

    public static JSPackageManager sharedInstance() {
        if (null == manager)
            manager = new JSPackageManager();
        return manager;
    }

    public String URL4Page(Context context, String page) {
        String result = page + ".html";

        //TODO YOU CAN LOAD DEBUT PATH HERE


        //TODO YOU CAN LOAD PRESET PATH HERE


        //TODO YOU CAN LOAD DOWNLOAD FILE PATH HERE


        //LOAD THE BUNDLE PATH HERE
        AssetManager assetManager = context.getAssets();
        try {
            for (String ass : assetManager.list("h5pages")) {
                if (ass.equalsIgnoreCase(result)) return prefix + ass;
            }
        }catch (Exception e ) {
            //e.printStackTrace();
        }

        //LOAD THE ERR PAGE HERE
        return prefix+"404.html";
    }
}
