package com.b5m.jsbridge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.b5m.jspackage.JSPackageManager;

/**
 * Created by boguang on 15/3/30.
 */
public class JSWebviewActivity extends Activity {

    private WebView webView;
    private Button leftButton, rightButton;
    private TextView titleTextView;
    private String page, pageQuery;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.js_activity);
        initSubviews();
        initDataes();
        configWebview();
        Intent intent = getIntent();
        page = intent.getStringExtra("page");
        pageQuery = intent.getStringExtra("pageQuery");
        loadPage();
    }

    private void initSubviews() {
        webView = (WebView)findViewById(R.id.webview);
        rightButton = (Button) findViewById(R.id.refresh);
        leftButton = (Button) findViewById(R.id.back);
        titleTextView = (TextView) findViewById(R.id.title);
    }

    private void initDataes() {
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSWebviewActivity.this.finish();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSWebviewActivity.this.loadPage();
            }
        });
    }

    private void configWebview() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSavePassword(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(new JSWebViewClient(this));
        webView.setWebChromeClient(new WebChromeClient(){});
    }

    private void loadPage() {
        String thePage = JSPackageManager.sharedInstance().URL4Page(this,page);
        webView.loadUrl(thePage);
    }

    public void jsCallback(String callbackId) {
        String js = String.format("window.B5MApp.callback(%s,%s);",callbackId, (null != pageQuery) ? pageQuery:"");
        webView.loadUrl("javascript:"+js);
    }

    public WebView getWebView() {
        return webView;
    }

    public Button getLeftButton() {
        return leftButton;
    }

    public Button getRightButton() {
        return rightButton;
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }
}
