package com.b5m.plugin.activityloader;

import android.app.Application;
import android.content.Context;
import android.util.Log;


/**
 * Created by boguang on 14-12-8.
 */
public class MyApplication extends Application {

    public static ClassLoader ORIGINAL_LOADER;
    public static ClassLoader CUSTOM_LOADER = null;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Context mBase = new Smith<Context>(this, "mBase").get();
            Object mPackageInfo = new Smith<Object>(mBase, "mPackageInfo").get();
            Smith<ClassLoader> sClassLoader = new Smith<ClassLoader>(mPackageInfo, "mClassLoader");
            ClassLoader mClassLoader = sClassLoader.get();
            ORIGINAL_LOADER = mClassLoader;
            MyClassLoader myClassLoader = new MyClassLoader(mClassLoader);
            sClassLoader.set(myClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyClassLoader extends ClassLoader {

        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException {

            if (null != CUSTOM_LOADER) {
                if (className.startsWith("com.b5m.")) {
                    Log.i("ClassLoader", "load class (" + className + ")");
                }
                try {
                    Class<?> c = CUSTOM_LOADER.loadClass(className);
                    if (null != c)
                        return c;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return loadClass(className, false);
        }
    }
}
