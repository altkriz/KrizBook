package com.kriztech.book.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.Model.GeneralSettings.GeneralSettings;
import com.kriztech.book.R;
import com.kriztech.book.Utility.ConnectivityReceiver;
import com.kriztech.book.Utility.MyApp;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.google.android.material.snackbar.Snackbar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private PrefManager prefManager;
    private Intent mainIntent;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(SplashActivity.this);
        Utils.HideNavigation(SplashActivity.this);
        MyApp.getInstance().initAppLanguage(this);
        setContentView(R.layout.activity_splash);
        PrefManager.forceRTLIfSupported(getWindow(), SplashActivity.this);

        init();
        printKeyHash();
    }

    private void init() {
        prefManager = new PrefManager(SplashActivity.this);
        checkConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyApp.getInstance().setConnectivityListener(this);
        isConnected = ConnectivityReceiver.isConnected();

        if (isConnected) {
            generalSettings();
        } else {
            showSnack(isConnected);
        }
    }

    /* general_settings API */
    private void generalSettings() {
        Call<GeneralSettings> call = BaseURL.getVideoAPI().general_settings();
        call.enqueue(new Callback<GeneralSettings>() {
            @Override
            public void onResponse(Call<GeneralSettings> call, Response<GeneralSettings> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        for (int i = 0; i < response.body().getResult().size(); i++) {
                            Log.e("" + response.body().getResult().get(i).getKey(), " => " + response.body().getResult().get(i).getValue());
                            prefManager.setValue(response.body().getResult().get(i).getKey(), response.body().getResult().get(i).getValue());
                        }

                        Log.e("=>firstTime", "" + prefManager.isFirstTimeLaunch());
                        jump();
                    }
                } catch (Exception e) {
                    Log.e("general_settings", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<GeneralSettings> call, Throwable t) {
                Log.e("general_settings", "onFailure => " + t.getMessage());
            }
        });
    }

    private void jump() {
        if (!prefManager.isFirstTimeLaunch()) {
            if (Utils.checkLoginUser(SplashActivity.this)) {
                mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            }
        } else {
            mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
        }
        startActivity(mainIntent);
        finish();
    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //onStart();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.fab),
                    "" + getResources().getString(R.string.sorry_not_connected_to_internet), Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.RED);
            snackbar.show();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplication().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(),
                        Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }

}