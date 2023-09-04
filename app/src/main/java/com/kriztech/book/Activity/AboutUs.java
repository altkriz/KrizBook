package com.kriztech.book.Activity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;

public class AboutUs extends AppCompatActivity {

    private PrefManager prefManager;

    private RelativeLayout rlAdView;
    private LinearLayout lyBack, lyToolbar, lyFbAdView;
    private ImageView ivAppicon;
    private TextView txtBack, txtToolbarTitle, txtAppname, txtCompanyname, txtEmail, txtWebsite, txtContactNo, txtAboutus;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(this);
        setContentView(R.layout.activity_aboutus);
        PrefManager.forceRTLIfSupported(getWindow(), AboutUs.this);

        init();
        AdInit();
        setDetails();

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutUs.this.finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(AboutUs.this);

            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            ivAppicon = findViewById(R.id.ivAppicon);
            txtAppname = findViewById(R.id.txtAppname);
            txtCompanyname = findViewById(R.id.txtCompanyname);
            txtEmail = findViewById(R.id.txtEmail);
            txtWebsite = findViewById(R.id.txtWebsite);
            txtContactNo = findViewById(R.id.txtContactNo);
            txtAboutus = findViewById(R.id.txtAboutus);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(AboutUs.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(AboutUs.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    private void setDetails() {
        try {
            txtToolbarTitle.setText("" + getResources().getString(R.string.about_us));
            Picasso.get().load("" + prefManager.getValue("app_logo"))
                    .placeholder(R.drawable.app_icon).into(ivAppicon);
            txtAppname.setText("" + prefManager.getValue("app_name"));
            txtCompanyname.setText("" + prefManager.getValue("Author"));
            txtEmail.setText("" + prefManager.getValue("host_email"));
            txtWebsite.setText("" + prefManager.getValue("website"));
            txtContactNo.setText("" + prefManager.getValue("contact"));
            txtAboutus.setText(Html.fromHtml("" + prefManager.getValue("app_desripation")));
        } catch (Exception e) {
            Log.e("set_details", "Exception => " + e);
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
    }

}