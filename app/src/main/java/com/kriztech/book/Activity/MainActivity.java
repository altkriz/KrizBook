package com.kriztech.book.Activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kriztech.book.Fragment.BookMark;
import com.kriztech.book.Fragment.Featured;
import com.kriztech.book.Fragment.Settings;
import com.kriztech.book.Fragment.Wallet;
import com.kriztech.book.Model.PointSystemModel.PointSystemModel;
import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.PushNotification.Config;
import com.kriztech.book.PushNotification.NotificationUtils;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private PrefManager prefManager;

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BottomNavigationView bottomNavigationView;
    public static MenuItem item;
    private Toolbar toolbar;
    public static AppBarLayout appbar;
    private SimpleSearchView searchView;

    private LinearLayout lyFbAdView;
    private RelativeLayout rlAdView;
    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(MainActivity.this);
        setContentView(R.layout.activity_main);
        PrefManager.forceRTLIfSupported(getWindow(), MainActivity.this);

        appbar = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefManager = new PrefManager(MainActivity.this);
        bottomNavigationView = findViewById(R.id.navigation);
        if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
            bottomNavigationView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        } else {
            bottomNavigationView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        }
        bottomNavigationView.setItemIconTintList(null);
        rlAdView = findViewById(R.id.rlAdView);
        lyFbAdView = findViewById(R.id.lyFbAdView);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("SimpleSearchView", "Submit:" + query);
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("search", "" + query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("SimpleSearchView", "Text changed:" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                Log.e("SimpleSearchView", "Text cleared");
                return false;
            }
        });

        if (bottomNavigationView != null) {
            // Set action to perform when any menu-item is selected.
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            // Write code to perform some actions.
                            Log.e("item", "" + item);
                            selectFragment(item);
                            return false;
                        }
                    });
        }

        Log.e("isSelectPic", "===>>> " + Constant.isSelectPic);
        if (!Constant.isSelectPic) {
            pushFragment(new Featured());
        } else {
            pushFragment(new Settings());
        }
        PushInit();
        AdInit();

    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(MainActivity.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(MainActivity.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("onStart", "called");
    }

    /* profile API */
    private void Profile() {
        Call<ProfileModel> call = BaseURL.getVideoAPI().profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("profile", "status => " + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            Utils.storeUserCred(MainActivity.this,
                                    "" + response.body().getResult().get(0).getId(),
                                    "" + response.body().getResult().get(0).getType(),
                                    "" + response.body().getResult().get(0).getEmail(),
                                    "" + response.body().getResult().get(0).getFullname(),
                                    "" + response.body().getResult().get(0).getMobile());

                            if (response.body().getAuthorProfile() != null) {
                                Utils.storeAuthorCred(MainActivity.this,
                                        "" + response.body().getAuthorProfile().getId(),
                                        "" + response.body().getAuthorProfile().getEmail(),
                                        "" + response.body().getAuthorProfile().getName());
                            } else {
                                Utils.storeAuthorCred(MainActivity.this, "0", "", "");
                            }
                        }

                    } else {
                        Log.e("profile", "status => " + response.body().getStatus());
                    }
                } catch (Exception e) {
                    Log.e("profile", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.e("profile", "onFailure => " + t.getMessage());
            }
        });
    }

    /* earn_point API */
    private void PointSystem() {

        Call<PointSystemModel> call = BaseURL.getVideoAPI().earn_point();
        call.enqueue(new Callback<PointSystemModel>() {
            @Override
            public void onResponse(Call<PointSystemModel> call, Response<PointSystemModel> response) {
                try {
                    Log.e("earn_point", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getFreeCoin().size() > 0) {
                            for (int i = 0; i < response.body().getFreeCoin().size(); i++) {
                                prefManager.setValue("" + response.body().getFreeCoin().get(i).getKey(), "" + response.body().getFreeCoin().get(i).getValue());
                                Log.e("FreeCoin", "Value => " + prefManager.getValue("" + response.body().getFreeCoin().get(i).getKey()));
                            }
                        }

                    }
                } catch (Exception e) {
                    Log.e("earn_point", "Exception =>" + e);
                }
            }

            @Override
            public void onFailure(Call<PointSystemModel> call, Throwable t) {
                Log.e("earn_point", "Throwable => " + t.getMessage());
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() == R.id.bottom_featured) {
            Dialog dialog = new Dialog(MainActivity.this, R.style.SheetDialog);
            dialog.setContentView(LayoutInflater.from(MainActivity.this).inflate(R.layout.exit_logout_dialog, null, false));
            PrefManager.forceRTLIfSupported(getWindow(), MainActivity.this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.overlayDark60)));
            dialog.getWindow().setAttributes(layoutParams);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            RoundedImageView rivDialog = dialog.findViewById(R.id.rivDialog);
            rivDialog.setImageResource(R.drawable.app_icon);

            TextView txtTitle = dialog.findViewById(R.id.txtTitle);
            TextView txtDescription = dialog.findViewById(R.id.txtDescription);
            Button btnNegative = dialog.findViewById(R.id.btnNegative);
            Button btnPositive = dialog.findViewById(R.id.btnPositive);

            txtTitle.setText(getResources().getString(R.string.app_name));
            txtDescription.setText("" + getResources().getString(R.string.do_you_want_to_exit));

            btnPositive.setText("" + getResources().getString(R.string.yes));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    finishAffinity();
                }
            });

            btnNegative.setText("" + getResources().getString(R.string.no));
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } else {
            Log.e("Home navigation", "");
            bottomNavigationView.setSelectedItemId(R.id.bottom_featured);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                // search action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void selectFragment(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.bottom_featured:
                toolbar.setTitle(getResources().getString(R.string.title_featured));
                pushFragment(new Featured());
                break;
            case R.id.bottom_category:
                toolbar.setTitle(getResources().getString(R.string.title_library));
                if (Utils.checkLoginUser(MainActivity.this)) {
                    pushFragment(new BookMark());
                }
                break;
            case R.id.bottom_wallet:
                toolbar.setTitle(getResources().getString(R.string.title_reward));
                if (Utils.checkLoginUser(MainActivity.this)) {
                    pushFragment(new Wallet());
                }
                break;
            case R.id.bottom_latest:
                pushFragment(new Settings());
                break;
        }
    }

    protected void pushFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (ft != null) {
                ft.replace(R.id.rootLayout, fragment);
                ft.commit();
            }
        }
    }

    public void PushInit() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (Objects.equals(intent.getAction(), Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Log.e("message ==>", "" + message);
                    //Toasty.info(getApplicationContext(), "Push notification : " + message, Toasty.LENGTH_LONG).show();
                }
            }
        };
        displayFirebaseRegId();
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        Log.e(TAG, "Firebase reg id : " + regId);
        if (!TextUtils.isEmpty(regId)) {
            Log.e(TAG, "Firebase reg id : " + regId);
        } else {
            Log.e(TAG, "Firebase Reg Id is not received yet!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());

        if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
            Profile();
            PointSystem();
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
            fbAdView = null;
        }
    }

}