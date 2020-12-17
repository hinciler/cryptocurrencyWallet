package com.sikke.app.activities;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sikke.app.BaseActivity;
import com.sikke.app.R;

import static com.sikke.app.AppConstants.URL_HELP;

public class WebviewActivity extends BaseActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);



        WebView helpWebView = this.findViewById(R.id.webView);
        WebSettings settings = helpWebView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");


        Bundle b = getIntent().getExtras();
        String url = b.getString("url");

        String actionBarTitle = url;


        if(url.equals(URL_HELP)){
            actionBarTitle = this.getResources().getString(R.string.nav_help);
        }

        setupActionBar(actionBarTitle);

        helpWebView.getSettings().setJavaScriptEnabled(true);
        helpWebView.loadUrl(url);
        helpWebView.setWebViewClient(new HelpBrowser());
    }

    private void setupActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private class HelpBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            return true;
        }
    }
}
