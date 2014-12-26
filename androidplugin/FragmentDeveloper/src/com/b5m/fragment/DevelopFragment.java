package com.b5m.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.Button;
import com.b5m.fragmentdeveloper.R;

/**
 * Created by boguang on 14/12/16.
 */
public class DevelopFragment  extends Fragment{
    WebView webView;
    Button button;
    int counter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.developer_fragment,container,false);
        webView = (WebView)view.findViewById(R.id.webview);
        button = (Button)view.findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setText(String.valueOf(++counter));
            }
        });

        if (null != savedInstanceState)
        {
            counter = savedInstanceState.getInt("counter");
        }
        button.setText(String.valueOf(counter));
        view.removeViews(0,view.getChildCount() -1 );
        configWebview();
        webView.loadUrl("http://m.b5m.com");
        return view;
    }

    private void configWebview() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // TODO Auto-generated method stub
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("counter",counter);
    }
}
