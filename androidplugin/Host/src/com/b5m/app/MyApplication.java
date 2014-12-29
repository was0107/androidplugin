package com.b5m.app;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.b5m.loader.LoaderActivity;
import com.b5m.loader.MainActivity;
import com.b5m.loader.MyClassLoader;
import com.b5m.loader.RepositoryManager;
import com.b5m.loader.model.FileSpec;
import com.b5m.loader.model.FragmentSpec;
import com.b5m.loader.model.SiteSpec;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

/**
 * Created by boguang on 14/12/23.
 */
public class MyApplication extends Application {
    public static final String PRIMARY_SCHEME = "app";



    private static MyApplication instance;
    private RepositoryManager repoManager;

    public static MyApplication instance() {
        if (instance == null) {
            throw new IllegalStateException("Application has not been created");
        }
        return instance;
    }

    public RepositoryManager getRepoManager() {
        if (null == repoManager)
            repoManager = new RepositoryManager(this);
        return repoManager;
    }

    public MyApplication() {
        instance = this;
    }

    public SiteSpec readSite()
    {
        File dir = new File(getFilesDir(),"repo");
        File local = new File(dir,"site.txt");
        try {
            FileInputStream is = new FileInputStream(local);
            byte[] bytes = new byte[is.available()];
            int l = is.read(bytes);
            String string = new String(bytes,0,l,"UTF-8");
            JSONObject jsonObject = new JSONObject(string);
            return new SiteSpec(jsonObject);
        } catch (Exception e) {

        }
        return new SiteSpec("empty.0","0",new FileSpec[0], new FragmentSpec[0]);
    }


    public Intent urlMap(Intent intent) {
        do {
            if (intent.getComponent() != null)
                break;

            // only process my scheme uri
            Uri uri = intent.getData();
            if (uri == null)
                break;
            if (uri.getScheme() == null)
                break;
            if (!(PRIMARY_SCHEME.equalsIgnoreCase(uri.getScheme())))
                break;

            SiteSpec site = null;
            if (intent.hasExtra("_site")) {
                site = intent.getParcelableExtra("_site");
            }
            if (site == null) {
                site = readSite();
                intent.putExtra("_site",site);
            }
            // i'm responsible
            intent.setClass(this, LoaderActivity.class);

            String host = uri.getHost();
            if (TextUtils.isEmpty(host))
                break;
            host = host.toLowerCase(Locale.US);
            FragmentSpec fragment = site.getFragment(host);
            if (fragment == null)
                break;
            intent.putExtra("_fragment", fragment.getName());

            // class loader
            ClassLoader classLoader;
            if (TextUtils.isEmpty(fragment.getCode())) {
                classLoader = getClassLoader();
            } else {
                intent.putExtra("_code", fragment.getCode());
                FileSpec fs = site.getFile(fragment.getCode());
                if (fs == null)
                    break;
                classLoader = MyClassLoader.getClassLoader(site, fs);
                if (classLoader == null)
                    break;
            }

            intent.setClass(this, MainActivity.class);


        }while (false);

        return  intent;
    }


    @Override
    public void startActivity(Intent intent) {
        intent = urlMap(intent);
        super.startActivity(intent);
    }

}
