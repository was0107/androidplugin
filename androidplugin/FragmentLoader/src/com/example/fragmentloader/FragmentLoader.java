package com.example.fragmentloader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import dalvik.system.DexClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FragmentLoader extends Activity {
    Resources.Theme theme;
    Resources resources;
    AssetManager assetManager;
    ClassLoader classLoader;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // We need to setup environment before create

        if ("com.b5m.fragment.intent.action.LOAD_FRAGMENT".equals(getIntent().getAction())) {
            try {
                String path = getIntent().getStringExtra("path");
                InputStream is = MyApplication.instance().getAssets().open(path);
                byte bytes[] = new byte[is.available()];
                is.read(bytes);
                is.close();

                File f = new File(MyApplication.instance().getFilesDir(),"dex");
                f.mkdir();
                f = new File(f, "FL_"+ Integer.toHexString(path.hashCode())+".apk");

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bytes);
                fos.close();

                File fo  = new File(MyApplication.instance().getFilesDir(),"dexout");
                fo.mkdir();
                DexClassLoader cl = new DexClassLoader(f.getAbsolutePath(),fo.getAbsolutePath(),null,super.getClassLoader());
                classLoader = cl;


                //add asset path  to the main asset
                try {
                    AssetManager am = AssetManager.class.newInstance();
                    am.getClass().getMethod("addAssetPath", String.class)
                            .invoke(am, f.getAbsolutePath());
                    assetManager = am;
                }
                catch (Exception e ) {
                    throw new RuntimeException(e);
                }

                //add resource to the main Resource
                Resources rs = super.getResources();
                resources = new Resources(assetManager,rs.getDisplayMetrics(),rs.getConfiguration());


                //add theme to the main theme
                theme = resources.newTheme();
                theme.setTo(super.getTheme());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        super.onCreate(savedInstanceState);


        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.setId(android.R.id.primary);
        setContentView(frameLayout);

        if (null != savedInstanceState)
            return;

        if ("com.b5m.fragment.intent.action.LOAD_FRAGMENT".equals(getIntent().getAction())) {

            try {
                String className = getIntent().getStringExtra("class");
                Fragment fragment = (Fragment)getClassLoader().loadClass(className).newInstance();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(android.R.id.primary,fragment);
                fragmentTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Fragment fragment = new ListAPKFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(android.R.id.primary,fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public AssetManager getAssets() {
        return (null != assetManager) ? assetManager : super.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return (null != theme) ? theme : super.getTheme();
    }

    @Override
    public Resources getResources() {
        return (null != resources) ?  resources : super.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        return (null != classLoader) ?  classLoader : super.getClassLoader();
    }
}
