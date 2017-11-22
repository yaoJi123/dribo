package com.yao.dribo.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.yao.dribo.R;

import butterknife.BindView;
import butterknife.ButterKnife;



public class AuthActivity extends AppCompatActivity{
    public static final String KEY_URL = "url";
    public static final String KEY_CODE = "code";

    @BindView(R.id.prograss_bar) ProgressBar progressBar;
    @BindView(R.id.webview) WebView webView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_webview);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Log into Dribo");

        progressBar.setMax(100);

        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                if(url.startsWith(Auth.REDIRECT_URI)) {
                    Uri uri = Uri.parse(url);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(KEY_CODE, uri.getQueryParameter(KEY_CODE));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            public void onPageFinished (WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        String url = getIntent().getStringExtra(KEY_URL);
        webView.loadUrl(url);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
