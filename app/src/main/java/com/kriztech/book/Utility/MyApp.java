package com.kriztech.book.Utility;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.onesignal.OneSignal;
import com.orhanobut.hawk.Hawk;

public class MyApp extends Application {

    private static MyApp singleton = null;
    private static final String TAG = MyApp.class.getSimpleName();

    private PrefManager prefManager;

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;
        prefManager = new PrefManager(this);

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext());
        // Initialize the Audience Network SDK (Facebook ads)
        AudienceNetworkAds.initialize(this);

        //Initialize the Hawk for security downloads
        Hawk.init(getApplicationContext()).build();

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(Constant.ONESIGNAL_APP_ID);
        // promptForPushNotifications will show the native Android notification permission prompt.
        OneSignal.promptForPushNotifications();

        // Firebase Initialization
        FirebaseApp.initializeApp(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                Log.e(TAG, "onActivityCreated => " + activity.getLocalClassName());
                /* Below code will restrict screen capture */
                Log.e(TAG, "setScreenCapture => " + Constant.setScreenCapture);
                if (Constant.setScreenCapture) {
                    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                Log.e(TAG, "onActivityStarted => " + activity.getLocalClassName());
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                Log.e(TAG, "onActivityResumed => " + activity.getLocalClassName());
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                Log.e(TAG, "onActivityPaused => " + activity.getLocalClassName());
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Log.e(TAG, "onActivityStopped => " + activity.getLocalClassName());
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                Log.e(TAG, "onActivitySaveInstanceState => " + activity.getLocalClassName());
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.e(TAG, "onActivityDestroyed => " + activity.getLocalClassName());
            }
        });

    }

    public void initAppLanguage(Context context) {
        LocaleUtils.initialize(context, LocaleUtils.getSelectedLanguageId());
    }

    public static MyApp getPhotoApp() {
        return singleton;
    }

    public Context getContext() {
        return singleton.getContext();
    }

    public static MyApp getInstance() {
        if (singleton == null) {
            singleton = new MyApp();
        }
        return singleton;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}