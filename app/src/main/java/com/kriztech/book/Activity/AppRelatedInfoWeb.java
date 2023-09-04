package com.kriztech.book.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;

public class AppRelatedInfoWeb extends AppCompatActivity {

    private PrefManager prefManager;

    private TextView txtToolbarTitle;
    private LinearLayout lyBack;
    private WebView webView;

    private String mainTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(AppRelatedInfoWeb.this);
        setContentView(R.layout.activity_myinfo_web);
        PrefManager.forceRTLIfSupported(getWindow(), AppRelatedInfoWeb.this);
        prefManager = new PrefManager(AppRelatedInfoWeb.this);

        lyBack = findViewById(R.id.lyBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        webView = findViewById(R.id.webView);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mainTitle = bundle.getString("Title");
            Log.e("mainTitle =>", "" + mainTitle);
            txtToolbarTitle.setText("" + mainTitle);
        }

        webView.setWebViewClient(new MyBrowser());
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if (PrefManager.getInstance(AppRelatedInfoWeb.this).isNightModeEnabled() == true) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
            }
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        if (mainTitle.equalsIgnoreCase("" + getResources().getString(R.string.about_us))) {
            Log.e("about-us", "=> " + prefManager.getValue("about-us"));
            webView.loadUrl("" + prefManager.getValue("about-us"));
        } else if (mainTitle.equalsIgnoreCase("" + getResources().getString(R.string.Privacy_policy))) {
            Log.e("privacy-policy", "=> " + prefManager.getValue("privacy-policy"));
            webView.loadUrl("" + prefManager.getValue("privacy-policy"));
        } else if (mainTitle.equalsIgnoreCase("" + getResources().getString(R.string.terms_and_conditions))) {
            Log.e("terms-and-conditions", "=> " + prefManager.getValue("terms-and-conditions"));
            webView.loadUrl("" + prefManager.getValue("terms-and-conditions"));
        }

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("==>url", "" + url);
            view.loadUrl(url);
            return true;
        }
    }

}