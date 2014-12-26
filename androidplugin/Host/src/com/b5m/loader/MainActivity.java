package com.b5m.loader;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.widget.FrameLayout;
import com.b5m.app.MyActivity;

/**
 * Created by boguang on 14/12/25.
 */
public class MainActivity extends MyActivity {
    private MyResources myResources;
    private AssetManager assetManager;
    private Resources resources;
    private Resources.Theme theme;

    private FrameLayout rootView;





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
