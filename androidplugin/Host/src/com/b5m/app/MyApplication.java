package com.b5m.app;

import android.app.Application;
import com.b5m.loader.RepositoryManager;

/**
 * Created by boguang on 14/12/23.
 */
public class MyApplication extends Application {
    private static MyApplication instance;
    private RepositoryManager repoManager;

    public static MyApplication instance() {
        if (instance == null) {
            throw new IllegalStateException("Application has not been created");
        }

        return instance;
    }

}
