package com.b5m.jsbridge;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by boguang on 15/3/30.
 */
public class JSWebViewClient extends WebViewClient {
    private JSWebviewActivity activity;
    private URIManager uriManager;

    public JSWebViewClient(JSWebviewActivity activity) {
        this.activity = activity;
        uriManager = new URIManager(this.activity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        boolean flag = uriManager.dealWithURI(url);
        Log.i("shouldOverrideUrlLoading", url);
        return flag;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }
}

