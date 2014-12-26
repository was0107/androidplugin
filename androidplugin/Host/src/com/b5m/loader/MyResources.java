package com.b5m.loader;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.b5m.app.MyApplication;
import com.b5m.loader.model.FileSpec;
import com.b5m.loader.model.SiteSpec;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by boguang on 14/12/25.
 */
public class MyResources {

    FileSpec file;
    String packageName;
    AssetManager asset;
    Resources res;
    MyResources deps[];

    public MyResources(FileSpec file, String packageName, AssetManager asset, Resources res, MyResources[] deps) {
        this.file = file;
        this.packageName = packageName;
        this.asset = asset;
        this.res = res;
        this.deps = deps;
    }

    public Drawable getDrawable(int id) {
        return res.getDrawable(id);
    }
    public CharSequence getText(int id) {
        return res.getText(id);
    }

    public String getString(int id) {
        return res.getString(id);
    }

    public String[] getStringArray(int id) {
        return res.getStringArray(id);
    }

    public int getColor(int id) {
        return res.getColor(id);
    }

    public ColorStateList getColorStateList(int id) {
        return res.getColorStateList(id);
    }

    public float getDimension(int id) {
        return res.getDimension(id);
    }

    public int getDimensionPixelSize(int id ) {
        return res.getDimensionPixelSize(id);
    }

    public InputStream openRawResource(int id) {
        return res.openRawResource(id);
    }

    public byte[] getRawResource(int id) {
        InputStream io = openRawResource(id);
        try {
            int n = io.available();
            ByteArrayOutputStream bos = new ByteArrayOutputStream( n > 0 ? n : 4096);
            byte[] buffer = new byte[4096];
            int l;
            while (-1 != (l = io.read(buffer)))
                bos.write(buffer,0, l);
            bos.close();
            return  bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
    /**
     * 返回独立的Resources
     * <p>
     * 对Resources进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该Resources中。
     *
     * @return
     */
    public Resources getResources() {
        return res;
    }

    /**
     * 返回独立的AssetManager
     * <p>
     * 对AssetManager进行操作时不会处理依赖关系，所有依赖包的内容均不会出现在该AssetManager中。
     *
     * @return
     */
    public AssetManager getAssets() {
        return asset;
    }

    /**
     * 同LayoutInflater.inflate(id, parent, attachToRoot)
     * <p>
     * 不会处理依赖关系，请确保id对应的layout在当前包内
     *
     * @param
     * @return
     * @throws Resources.NotFoundException
     */
    public View inflate(Context context, int id , ViewGroup viewGroup, boolean attachToRoot) {

        if (!(context instanceof MainActivity)) {
            throw new RuntimeException("unable to inflate without MainActivity context");
        }

        MainActivity ma = (MainActivity)context;
        MyResources old = ma.getOverrideResource();
        ma.setOverrideResources(this);
        try {
            View view = LayoutInflater.from(context).inflate(id,viewGroup,attachToRoot);
            return view;
        } finally {
            ma.setOverrideResources(old);
        }
    }

    static final HashMap<String, MyResources> loaders = new HashMap<String, MyResources>();

    /**
     * return null if not available on the disk
     */
    public static MyResources getResource(SiteSpec site, FileSpec file) {
        MyResources rl = loaders.get(file.getId());
        if (null != rl)
            return rl;

        String deps[] = file.getDeps();
        MyResources rs[] = null;
        if (deps != null) {
            rs = new MyResources[deps.length];
            for (int i = 0 ; i < deps.length ; i++) {
                FileSpec pf = site.getFile(file.getId());
                if (pf == null)
                    return null;
                MyResources resources = getResource(site,pf);
                if (null == resources)
                    return null;
                rs[i] = resources;
            }
        }

        File dir = MyApplication.instance().getFilesDir();
        dir = new File(dir, "repo");
        if (!dir.isDirectory()) {
            throw new RuntimeException(dir + " is not exist");
        }

        dir = new File(dir,file.getId());
        File path = new File(dir, TextUtils.isEmpty(file.getMd5()) ? "1.apk" : file.getMd5()+".apk");

        if (!path.isFile())
            throw new RuntimeException(path+ " is not exist");

        try {
            AssetManager am = (AssetManager) AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath",String.class).invoke(am, path.getAbsolutePath());


            //parse packagename from AndroidMenifest.xml

            String packageName = null;

            XmlResourceParser xml = am.openXmlResourceParser("AndroidManifest.xml");
            int eventType = xml.getEventType();

            xmlloop: while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("manifest".equals(xml.getName())) {
                            packageName = xml.getAttributeValue("http://schemas.android.com/apk/res/android","package");
                        }
                        break xmlloop;
                }
                eventType = xml.nextToken();
            }
            xml.close();

            if (null == packageName)
                throw new RuntimeException("package not found in AndroidManifest.xml [" + path + "]");


            Resources superResource = MyApplication.instance().getResources();
            Resources res = new Resources(am,superResource.getDisplayMetrics(),superResource.getConfiguration());

            rl = new MyResources(file,packageName,am,res,rs);

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }

        loaders.put(file.getId(), rl);
        return rl;
    }

    /**
     * 从当前类所在的包载入MyResource
     *
     * @param clazz
     * @return
     * @throws RuntimeException
     *             如果当前类不是动态加载包载入的
     */
    public static MyResources getResource(Class<?> clazz) {
        if (!(clazz.getClassLoader() instanceof MyClassLoader)) {
            throw new RuntimeException(clazz + " is not loaded from dynamic loader");
        }
        return getResource((MyClassLoader) clazz.getClassLoader());
    }

    static MyResources getResource(MyClassLoader mcl) {
        FileSpec file = mcl.file;
        MyResources rl = loaders.get(file.getId());
        if (rl != null)
            return rl;

        MyResources[] rs = null;
        if (mcl.deps != null) {
            rs = new MyResources[mcl.deps.length];
            for (int i = 0; i < rs.length; i++) {
                MyResources r = getResource(mcl.deps[i]);
                rs[i] = r;
            }
        }

        File dir = MyApplication.instance().getFilesDir();
        dir = new File(dir, "repo");
        if (!dir.isDirectory())
            throw new RuntimeException(dir + " not exists");
        dir = new File(dir, file.getId());
        File path = new File(dir, TextUtils.isEmpty(file.getMd5()) ? "1.apk" : file.getMd5() + ".apk");
        if (!path.isFile())
            throw new RuntimeException(path + " not exists");

        try {
            AssetManager am = (AssetManager) AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath", String.class).invoke(am, path.getAbsolutePath());

            Resources superRes = MyApplication.instance().getResources();
            Resources res = new Resources(am, superRes.getDisplayMetrics(), superRes.getConfiguration());

            // parse packageName from AndroidManifest.xml
            String packageName = null;
            XmlResourceParser xml = am.openXmlResourceParser("AndroidManifest.xml");
            int eventType = xml.getEventType();
            xmlloop: while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("manifest".equals(xml.getName())) {
                            packageName = xml.getAttributeValue(null, "package");
                            break xmlloop;
                        }
                }
                eventType = xml.nextToken();
            }
            xml.close();
            if (packageName == null) {
                throw new RuntimeException("package not found in AndroidManifest.xml [" + path + "]");
            }

            rl = new MyResources(file,packageName,am,res,rs);

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }

        loaders.put(file.getId(), rl);
        return rl;
    }
}
