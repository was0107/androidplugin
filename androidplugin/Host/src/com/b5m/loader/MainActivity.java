package com.b5m.loader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.b5m.app.MyActivity;
import com.b5m.loader.model.FileSpec;
import com.b5m.loader.model.SiteSpec;

/**
 * **
 * 主Activity容器，负责启动并装载Fragment
 * <p>
 * 启动前所有依赖的资源必须加载完毕（由urlMapping和LoaderActivity负责）
 * <p>
 * Intent参数：<br>
 * _site:SiteSpec，指定的site地图<br>
 * _code:String，ClassLoader所需要载入的FileID，如果为空则使用APK自带ClassLoader<br>
 * _fragment:String，Fragment的类名<br>
 *
 * Created by boguang on 14/12/25.
 */
public class MainActivity extends MyActivity {


    private SiteSpec site;
    private FileSpec file;
    private String fragmentName;
    private boolean loaded;
    private MyClassLoader classLoader;
    private MyResources myResources;
    private AssetManager assetManager;
    private Resources resources;
    private Resources.Theme theme;

    private FrameLayout rootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        int error = 0;
        do {
            site = intent.getParcelableExtra("_site");
            if (null == site) {
                error = 201;
                break;
            }

            fragmentName = intent.getStringExtra("_fragment");
            if (TextUtils.isEmpty(fragmentName)) {
                error = 202;
                break;
            }

            String code = intent.getStringExtra("_code");
            if (TextUtils.isEmpty(code)) {
                loaded = true;
                break;
            }

            file = site.getFile(code);
            if (null == file) {
                error = 205;
                break;
            }

            classLoader = MyClassLoader.getClassLoader(site,file);

            loaded = classLoader != null;
            if (!loaded) {
                error = 210;
                break;
            }

        }while (false);

        super.onCreate(savedInstanceState);


        rootView = new FrameLayout(this);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        rootView.setId(android.R.id.primary);
        setContentView(rootView);

        if (!loaded) {
            TextView textView = new TextView(this);
            textView.setText("无法载入页面：" + error);
            textView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            ));
            rootView.addView(textView);

        }

        if (savedInstanceState != null)
            return;

        Fragment fragment = null;
        try {
            fragment = (Fragment)getClassLoader().loadClass(fragmentName).newInstance();

        } catch (Exception e ) {
            e.printStackTrace();
            loaded = false;
            error = 211;
            classLoader = null;
            TextView textView = new TextView(this);
            textView.setText("无法载入页面：" + error + "\n" + e);
            textView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            ));
            rootView.addView(textView);
            return;
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.primary, fragment);
        ft.commit();
    }

    public SiteSpec getSite() {
        return site;
    }

    public FileSpec getFile() {
        return file;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    @Override
    public ClassLoader getClassLoader() {
        return (null != classLoader) ? classLoader : super.getClassLoader();
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @Override
    public Resources.Theme getTheme() {
        return theme == null ? super.getTheme() : theme;
    }

    public MyResources getOverrideResource() {
        return myResources;
    }



    public void setOverrideResources(MyResources overrideResources) {

        if (overrideResources == null) {
            this.myResources = null;
            this.resources = null;
            this.theme = null;
            this.assetManager = null;
        } else {
            this.myResources = overrideResources;
            this.resources = overrideResources.getResources();
            this.assetManager = overrideResources.getAssets();
            Resources.Theme tm = overrideResources.getResources().newTheme();
            tm.setTo(getTheme());
            this.theme = tm;
        }
    }
}
