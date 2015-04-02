package com.b5m.jsbridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loadJSWebviewActivity();
    }

    private void loadJSWebviewActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, JSWebviewActivity.class);
                intent.putExtra("page","home");
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 1000);
    }
}
