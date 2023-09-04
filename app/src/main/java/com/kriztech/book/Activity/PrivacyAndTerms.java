package com.kriztech.book.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdView;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;

public class PrivacyAndTerms extends AppCompatActivity {

    private PrefManager prefManager;

    private TextView txtPolicyTermsDesc, txtAppname, txtToolbarTitle, txtBack;
    private LinearLayout lyBack, lyToolbar, lyFbAdView;
    private RelativeLayout rlAdView;

    private TemplateView nativeTemplate = null;
    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;
    private NativeBannerAd fbNativeBannerAd = null;
    private NativeAdLayout fbNativeTemplate = null;

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(PrivacyAndTerms.this);
        setContentView(R.layout.activity_privacy_terms);
        PrefManager.forceRTLIfSupported(getWindow(), PrivacyAndTerms.this);

        Intent intent = getIntent();
        if (intent.hasExtra("Title")) {
            title = intent.getStringExtra("Title");
            Log.e("title ==>", "" + title);
        }

        init();
        AdInit();
        NativeAdInit();

        txtAppname.setText("" + Html.fromHtml(prefManager.getValue("app_name")));

        if (title.equalsIgnoreCase("" + getResources().getString(R.string.terms_and_conditions))) {
            txtPolicyTermsDesc.setText("" + Html.fromHtml(prefManager.getValue("terms_and_condition")));
            txtToolbarTitle.setText("" + getResources().getString(R.string.terms_and_conditions));
        } else {
            txtPolicyTermsDesc.setText("" + Html.fromHtml(prefManager.getValue("privacy_policy")));
            txtToolbarTitle.setText("" + getResources().getString(R.string.Privacy_policy));
        }

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(PrivacyAndTerms.this);

            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);
            lyBack = findViewById(R.id.lyBack);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtAppname = findViewById(R.id.txtAppname);
            txtPolicyTermsDesc = findViewById(R.id.txtPolicyTermsDesc);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(PrivacyAndTerms.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(PrivacyAndTerms.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    private void NativeAdInit() {
        Log.e("fb_native_status", "" + prefManager.getValue("fb_native_status"));
        Log.e("native_ad", "" + prefManager.getValue("native_ad"));
        if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            nativeTemplate.setVisibility(View.VISIBLE);
            Utils.NativeAds(PrivacyAndTerms.this, nativeTemplate, "" + prefManager.getValue("native_adid"));
        } else {
            nativeTemplate.setVisibility(View.GONE);
        }

        if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            fbNativeTemplate.setVisibility(View.VISIBLE);
            nativeTemplate.setVisibility(View.GONE);
            Utils.FacebookNativeAdSmall(PrivacyAndTerms.this, fbNativeBannerAd, fbNativeTemplate, "" + prefManager.getValue("fb_native_id"));
        } else {
            fbNativeTemplate.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
    }

}