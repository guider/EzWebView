package com.yanyuanquan.android.ezwebviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.yanyuanquan.android.ezwebview.EzWebView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EzWebView webView = (EzWebView) findViewById(R.id.webView);
        webView.loadUrl("http://www.importnew.com/7127.html");
    }
}
