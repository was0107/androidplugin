package com.b5m.loader;

import android.text.TextUtils;
import android.util.Log;
import com.b5m.app.MyApplication;
import com.b5m.loader.model.FileSpec;
import com.b5m.loader.model.SiteSpec;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.util.HashMap;

/**
 * Created by boguang on 14/12/25.
 */
public class MyClassLoader extends DexClassLoader {

    FileSpec file;
    MyClassLoader[] deps;

    public MyClassLoader(String dexPath, String optimizedDirectory, String libraryPath,
                         ClassLoader parent, FileSpec file, MyClassLoader[] deps) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.file = file;
        this.deps = deps;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {

        Class<?> clazz = findLoadedClass(className);
        if (null != clazz)
            return clazz;
        try {
            clazz = getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
            Log.e("Loader","ClassNotFoundException =  " + e);
            e.printStackTrace();
        }

        if (null != clazz)
            return clazz;

        if (null!= deps) {
            for(MyClassLoader cl : deps) {
                try {
                    clazz = cl.findClass(className);
                    break;
                } catch (ClassNotFoundException e) {
                    Log.e("Loader","deps ClassNotFoundException =  " + e);
                }
            }
        }


        if (null != clazz)
            return clazz;

        clazz = findClass(className);
        return clazz;
    }

    static final HashMap<String, MyClassLoader> loaders = new HashMap<String, MyClassLoader>();

    /**
     * return null if not available on the disk
     */
    public static MyClassLoader getClassLoader(SiteSpec site, FileSpec file) {
        MyClassLoader cl = loaders.get(file.getId());
        if (cl != null)
            return cl;
        String[] deps = file.getDeps();
        MyClassLoader[] ps = null;
        if (deps != null) {
            ps = new MyClassLoader[deps.length];
            for (int i = 0; i < deps.length; i++) {
                FileSpec pf = site.getFile(deps[i]);
                if (pf == null)
                    return null;
                MyClassLoader l = getClassLoader(site, pf);
                if (l == null)
                    return null;
                ps[i] = l;
            }
        }
        File dir = MyApplication.instance().getFilesDir();
        dir = new File(dir, "repo");
        if (!dir.isDirectory())
            return null;
        dir = new File(dir, file.getId());
        File path = new File(dir, TextUtils.isEmpty(file.getMd5()) ? "1.apk"
                : file.getMd5() + ".apk");
        if (!path.isFile())
            return null;
        File outdir = new File(dir, "dexout");
        outdir.mkdir();

        cl = new MyClassLoader( path.getAbsolutePath(),
                outdir.getAbsolutePath(), null, MyApplication.instance()
                .getClassLoader(),file, ps);
        loaders.put(file.getId(), cl);
        return cl;
    }


}
