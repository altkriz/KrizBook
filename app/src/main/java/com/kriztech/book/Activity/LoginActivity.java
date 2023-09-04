package com.kriztech.book.Activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.Model.LoginRegister.LoginRegiModel;
import com.kriztech.book.OTPLogin.SendOTP;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;

    private EditText etEmail, etPassword;
    private TextView txtAlreadySignup, txtLogin, txtSkip, txtForgot, txtUserAgreement;
    private ImageView ivLoginIcon;
    private LinearLayout lyGmail, lyFacebook, lyOTPLogin;
    private CheckBox checkBox;

    private CallbackManager callbackManager;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String EMAIL = "email";
    private static final String PROFILE = "public_profile";

    private RewardedVideoAd fbRewardedVideoAd = null;
    private RewardedAd mRewardedAd = null;
    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;

    private String strFirstname, strLastname, fbName, fbEmail, strEmail, strPassword, storeImageName = "", loginWith = "";
    private MultipartBody.Part body;
    private RequestBody firstName, lastName, email, password, type, mobileNumber;
    private File imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(LoginActivity.this);
        Utils.HideNavigation(LoginActivity.this);
        setContentView(R.layout.activity_login);
        PrefManager.forceRTLIfSupported(getWindow(), LoginActivity.this);

        takePermissionUtils = new PermissionUtils(LoginActivity.this, mPermissionResult);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();

        Init();
        setHTMLTexts();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("loginResult1", "Token::" + loginResult.getAccessToken());
                Log.e("loginResult", "" + loginResult.getAccessToken().getToken());
                AccessToken accessToken = loginResult.getAccessToken();
                Log.e("loginResult3", "" + accessToken);
                useLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("exception", "" + exception.getMessage());
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void Init() {
        try {
            prefManager = new PrefManager(this);

            etEmail = findViewById(R.id.et_email);
            etPassword = findViewById(R.id.et_password);
            txtAlreadySignup = findViewById(R.id.txt_already_signup);
            txtLogin = findViewById(R.id.txt_login);
            txtSkip = findViewById(R.id.txt_skip);
            txtForgot = findViewById(R.id.txt_forgot);
            txtUserAgreement = findViewById(R.id.txtUserAgreement);

            ivLoginIcon = findViewById(R.id.iv_login_icon);
            checkBox = findViewById(R.id.checkBox);

            lyGmail = findViewById(R.id.lyGmail);
            lyFacebook = findViewById(R.id.lyFacebook);
            lyOTPLogin = findViewById(R.id.lyOTPLogin);

            lyGmail.setOnClickListener(this);
            lyFacebook.setOnClickListener(this);
            lyOTPLogin.setOnClickListener(this);
            txtAlreadySignup.setOnClickListener(this);
            txtLogin.setOnClickListener(this);
            txtSkip.setOnClickListener(this);
            txtForgot.setOnClickListener(this);
            txtUserAgreement.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void setHTMLTexts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtAlreadySignup.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#ffffff>" + getResources().getString(R.string.signup) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                txtAlreadySignup.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#000000>" + getResources().getString(R.string.signup) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtAlreadySignup.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#ffffff>" + getResources().getString(R.string.signup) + "</font></b> </p>"));
            } else {
                txtAlreadySignup.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#000000>" + getResources().getString(R.string.signup) + "</font></b> </p>"));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>"));
        }
        txtUserAgreement.setClickable(true);
        txtUserAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /* normal login API */
    private void SignIn() {
        if (!((Activity) LoginActivity.this).isFinishing()) {
            Utils.ProgressBarShow(LoginActivity.this);
        }

        Log.e("Type", "" + type);

        Call<LoginRegiModel> call = BaseURL.getVideoAPI().login("" + strEmail, "" + strPassword, "" + Constant.typeNormal);
        call.enqueue(new Callback<LoginRegiModel>() {
            @Override
            public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("login", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            Log.e("email =>", "" + response.body().getResult().get(0).getEmail());

                            prefManager.setFirstTimeLaunch(false);
                            Utils.storeUserCred(LoginActivity.this,
                                    "" + response.body().getResult().get(0).getId(),
                                    "" + response.body().getResult().get(0).getType(),
                                    "" + response.body().getResult().get(0).getEmail(),
                                    "" + response.body().getResult().get(0).getFullname(),
                                    "" + response.body().getResult().get(0).getMobile());
                            Log.e("LoginId ==>", "" + prefManager.getLoginId());

                            Utils.redirectToMainActivity(LoginActivity.this, "");

                        } else {
                            Log.e("login", "Message => " + response.body().getMessage());
                            Utils.AlertDialog(LoginActivity.this, response.body().getMessage(), false, false);
                        }
                    } else {
                        Log.e("login", "Message => " + response.body().getMessage());
                        Utils.AlertDialog(LoginActivity.this, response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("login", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                Log.e("login", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(LoginActivity.this, t.getMessage(), false, false);
            }
        });
    }

    /* Social login API */
    private void SignInSocial() {
        if (!((Activity) LoginActivity.this).isFinishing()) {
            Utils.ProgressBarShow(LoginActivity.this);
        }

        Log.e("Type", "" + type);

        Call<LoginRegiModel> call = BaseURL.getVideoAPI().login(firstName, lastName, email, type, mobileNumber, password, body);
        call.enqueue(new Callback<LoginRegiModel>() {
            @Override
            public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("social login", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            Log.e("email =>", "" + response.body().getResult().get(0).getEmail());

                            prefManager.setFirstTimeLaunch(false);
                            Utils.storeUserCred(LoginActivity.this,
                                    "" + response.body().getResult().get(0).getId(),
                                    "" + response.body().getResult().get(0).getType(),
                                    "" + response.body().getResult().get(0).getEmail(),
                                    "" + response.body().getResult().get(0).getFullname(),
                                    "" + response.body().getResult().get(0).getMobile());

                            Utils.redirectToMainActivity(LoginActivity.this, "");
                        } else {
                            Log.e("social login", "Message => " + response.body().getMessage());
                            Utils.AlertDialog(LoginActivity.this, response.body().getMessage(), false, false);
                        }

                    } else {
                        Log.e("social login", "Message => " + response.body().getMessage());
                        Utils.AlertDialog(LoginActivity.this, response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("social login", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                Log.e("social login", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(LoginActivity.this, t.getMessage(), false, false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtUserAgreement:
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
                break;

            case R.id.lyGmail:
                loginWith = "Google";
                Log.e("gMail", "loginWith => " + loginWith);
                if (takePermissionUtils.isStoragePermissionGranted()) {
                    if (checkBox.isChecked()) {
                        LoginManager.getInstance().logOut();
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, 101);
                    } else {
                        Toast.makeText(LoginActivity.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                    }
                } else {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
                }
                break;

            case R.id.lyFacebook:
                loginWith = "Facebook";
                Log.e("fb", "loginWith => " + loginWith);
                if (takePermissionUtils.isStoragePermissionGranted()) {
                    if (checkBox.isChecked()) {
                        mGoogleSignInClient.signOut();
                        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(EMAIL, PROFILE));
                    } else {
                        Toast.makeText(LoginActivity.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                    }
                } else {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
                }
                break;

            case R.id.lyOTPLogin:
                Log.e("OTP", "Mobile");
                startActivity(new Intent(LoginActivity.this, SendOTP.class));
                break;

            case R.id.txt_already_signup:
                startActivity(new Intent(LoginActivity.this, Registration.class));
                break;

            case R.id.txt_login:
                strEmail = etEmail.getText().toString().trim();
                strPassword = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(strEmail)) {
                    Toasty.warning(LoginActivity.this, "" + getResources().getString(R.string.enter_email), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strPassword)) {
                    Toasty.warning(LoginActivity.this, "" + getResources().getString(R.string.enter_password), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (!Utils.isEmailValid(strEmail)) {
                    Toasty.warning(LoginActivity.this, "" + getResources().getString(R.string.enter_valid_email), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (!checkBox.isChecked()) {
                    Toast.makeText(LoginActivity.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                    return;
                }

                SignIn();
                break;

            case R.id.txt_skip:
                ShowAdByClick();
                break;

            case R.id.txt_forgot:
                startActivity(new Intent(LoginActivity.this, ForgotActivity.class));
                break;
        }
    }

    private final ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(LoginActivity.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(LoginActivity.this, getString(R.string.we_need_storage_permission_for_save_video));
                    } else if (allPermissionClear) {
                        if (loginWith.equalsIgnoreCase("Google")) {
                            if (checkBox.isChecked()) {
                                LoginManager.getInstance().logOut();
                                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                startActivityForResult(signInIntent, 101);
                            } else {
                                Toast.makeText(LoginActivity.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (checkBox.isChecked()) {
                                mGoogleSignInClient.signOut();
                                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(EMAIL, PROFILE));
                            } else {
                                Toast.makeText(LoginActivity.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });

    private void useLoginInformation(AccessToken accessToken) {

        Log.e("accessToken ==>", "" + accessToken);
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                if (object != null) {

                    String f_name = object.optString("first_name");
                    String l_name = object.optString("last_name");
                    fbEmail = object.optString("email");
                    String id = object.optString("id");
                    String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                    Log.e("Firstname", "" + f_name);
                    Log.e("last_name", "" + l_name);
                    Log.e("fb_email", "" + fbEmail);
                    Log.e("id", "" + id);
                    Log.e("image_url", "" + image_url);

                    fbEmail = object.optString("email");
                    fbName = f_name + l_name;

                    if (fbEmail.length() == 0) {
                        fbEmail = fbName.trim() + "@facebook.com";
                    }
                    Log.e("name", "" + fbName);
                    Log.e("email", "" + fbEmail);

                    firstName = RequestBody.create(MediaType.parse("text/plain"), "" + fbName);
                    lastName = RequestBody.create(MediaType.parse("text/plain"), "" + l_name);
                    email = RequestBody.create(MediaType.parse("text/plain"), "" + fbEmail);
                    type = RequestBody.create(MediaType.parse("text/plain"), "" + Constant.typeFacebook);
                    mobileNumber = RequestBody.create(MediaType.parse("text/plain"), "");
                    password = RequestBody.create(MediaType.parse("text/plain"), "");

                    storeImageName = (getResources().getString(R.string.app_name) + "" + fbName.replaceAll("[, ; &]", "")).toLowerCase();
                    Log.e("facebook", "image_url => " + image_url);
                    if (image_url != null) {
                        downloadAndSave(LoginActivity.this, image_url, storeImageName);
                    } else {
                        SignInSocial();
                    }
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,email,gender,birthday");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Log.e("getDisplayName", "" + account.getDisplayName());
            Log.e("getEmail", "" + account.getEmail());
            Log.e("getIdToken", "" + account.getIdToken());
            Log.e("getPhotoUrl", "" + account.getPhotoUrl());

            strFirstname = "" + account.getDisplayName();
            strLastname = "";
            strEmail = "" + account.getEmail();

            firstName = RequestBody.create(MediaType.parse("text/plain"), "" + strFirstname);
            lastName = RequestBody.create(MediaType.parse("text/plain"), "" + strLastname);
            email = RequestBody.create(MediaType.parse("text/plain"), "" + strEmail);
            type = RequestBody.create(MediaType.parse("text/plain"), "" + Constant.typeGmail);
            mobileNumber = RequestBody.create(MediaType.parse("text/plain"), "");
            password = RequestBody.create(MediaType.parse("text/plain"), "");

            storeImageName = (getResources().getString(R.string.app_name) + "" + strFirstname.replaceAll("[, ; &]", "")).toLowerCase();
            if (account.getPhotoUrl() != null) {
                downloadAndSave(LoginActivity.this, "" + account.getPhotoUrl(), storeImageName);
            } else {
                SignInSocial();
            }
        } catch (ApiException e) {
            Log.e("ApiException", "signInResult : failed code => " + e.getStatusCode());
        }
    }

    private void downloadAndSave(Activity activity, String download_url, String imageName) {
        String downloadDirectory = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            downloadDirectory = Functions.getAppFolder(activity);
        } else {
            downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Image/";
        }

        File file = new File(downloadDirectory);
        if (!(file.exists())) {
            Log.e("downloadAndSave", "Image directory created again");
            file.mkdirs();
        }

        Functions.showDeterminentLoader(activity, false, false);
        PRDownloader.initialize(activity.getApplicationContext());
        DownloadRequest prDownloader = PRDownloader.download(download_url, downloadDirectory, imageName + ".jpeg")
                .build()
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                        Functions.showLoadingProgress(prog);
                    }
                });

        String finalDownloadDirectory = downloadDirectory;
        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Functions.cancelDeterminentLoader();
                Log.e("=>downloadDirectory", "" + finalDownloadDirectory);
                Log.e("=>imageName", "" + imageName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    downloadAEImage(activity, finalDownloadDirectory, imageName + ".jpeg");
                } else {
                    scanFile(finalDownloadDirectory, imageName + ".jpeg");
                }
            }

            @Override
            public void onError(Error error) {
                Functions.cancelDeterminentLoader();
            }
        });
    }

    private void downloadAEImage(Activity activity, String path, String imageName) {

        ContentValues valuesimage;
        valuesimage = new ContentValues();
        valuesimage.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + Functions.getAppFolder(LoginActivity.this) + "Image/");
        valuesimage.put(MediaStore.MediaColumns.TITLE, imageName);
        valuesimage.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
        valuesimage.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        valuesimage.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesimage.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        valuesimage.put(MediaStore.MediaColumns.IS_PENDING, 1);
        ContentResolver resolver = activity.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedImage = resolver.insert(collection, valuesimage);

        ParcelFileDescriptor pfd;
        try {
            pfd = activity.getContentResolver().openFileDescriptor(uriSavedImage, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
            imageFile = new File(path + imageName);
            FileInputStream in = new FileInputStream(imageFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
            pfd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        valuesimage.clear();
        valuesimage.put(MediaStore.MediaColumns.IS_PENDING, 0);
        activity.getContentResolver().update(uriSavedImage, valuesimage, null, null);

        Log.e("==>imageFile", "" + imageFile.getPath());
        if (imageFile != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        } else {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), "");
            body = MultipartBody.Part.createFormData("image", "", requestFile);
        }
        Log.e("email", "" + email);
        SignInSocial();
    }

    private void scanFile(String downloadDirectory, String imageName) {
        imageFile = new File(downloadDirectory + imageName);
        Log.e("=>imageFile", "" + imageFile.getPath());

        if (imageFile != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
        } else {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), "");
            body = MultipartBody.Part.createFormData("image", "", requestFile);
        }
        Log.e("email", "" + email);
        SignInSocial();
    }

    /*Admob & Facebook Ads START*/

    //Showing ad by TYPE
    private void ShowAdByClick() {
        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            if (mRewardedAd != null) {
                mRewardedAd.show(LoginActivity.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.e("RewardItem amount =>", "" + rewardItem.getAmount());
                    }
                });
            } else {
                Log.e("mRewardedAd=>", "The ad wasn't ready yet.");
                Utils.redirectToMainActivity(LoginActivity.this, "Skip");
            }

        } else if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            if (fbRewardedVideoAd != null && fbRewardedVideoAd.isAdLoaded()) {
                fbRewardedVideoAd.show();
            } else {
                Log.e("fbRewardedVideoAd=>", "The ad wasn't ready yet.");
                Utils.redirectToMainActivity(LoginActivity.this, "Skip");
            }

        } else if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                fbInterstitialAd.show();
            } else {
                Log.e("fbInterstitialAd=>", "The ad wasn't ready yet.");
                Utils.redirectToMainActivity(LoginActivity.this, "Skip");
            }

        } else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(LoginActivity.this);
            } else {
                Log.e("mInterstitialAd=>", "The ad wasn't ready yet.");
                Utils.redirectToMainActivity(LoginActivity.this, "Skip");
            }
        }
    }

    private void InterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.e("Ad failed to show.", "" + adError.toString());
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.e("TAG", "Ad was shown.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.e("TAG", "Ad was dismissed.");
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.e("TAG", "onAdImpression.");
                }
            };

            mInterstitialAd.load(this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.e("TAG", "onAdLoaded");
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("TAG", "onAdFailedToLoad => " + loadAdError.getMessage());
                            mInterstitialAd = null;
                        }
                    });
        } catch (Exception e) {
            Log.e("InterstitialAd", "Exception => " + e);
        }
    }

    private void FacebookInterstitialAd() {
        try {
            fbInterstitialAd = new com.facebook.ads.InterstitialAd(this,
                    "CAROUSEL_IMG_SQUARE_APP_INSTALL#" + prefManager.getValue("fb_interstiatial_id"));
            fbInterstitialAd.loadAd(fbInterstitialAd.buildLoadAdConfig()
                    .withAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {
                            Log.e("TAG", "fb ad displayed.");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                            fbInterstitialAd = null;
                            Log.e("TAG", "fb ad dismissed.");
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            Log.e("TAG", "fb ad failed to load : " + adError.getErrorMessage());
                            Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                            fbInterstitialAd = null;
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.d("TAG", "fb ad is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            Log.d("TAG", "fb ad clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            Log.d("TAG", "fb ad impression logged!");
                        }
                    })
                    .build());
        } catch (Exception e) {
            Log.e("fb Interstial", "Exception =>" + e);
        }
    }

    private void RewardedVideoAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.e("Ad failed to show.", "" + adError.toString());
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    mRewardedAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.e("TAG", "Ad was shown.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.e("TAG", "Ad was dismissed.");
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    mRewardedAd = null;
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.e("TAG", "onAdImpression.");
                }
            };

            mRewardedAd.load(LoginActivity.this, "" + prefManager.getValue("reward_adid"), adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    super.onAdLoaded(rewardedAd);
                    mRewardedAd = rewardedAd;
                    mRewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    mRewardedAd = null;
                }
            });

        } catch (Exception e) {
            Log.e("mRewardedAd", "Exception => " + e);
        }
    }

    private void FacebookRewardAd() {
        try {
            fbRewardedVideoAd = new RewardedVideoAd(LoginActivity.this,
                    "VID_HD_16_9_15S_APP_INSTALL#" + prefManager.getValue("fb_rewardvideo_id"));

            fbRewardedVideoAd.loadAd(fbRewardedVideoAd.buildLoadAdConfig().withAdListener(new RewardedVideoAdListener() {
                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.e("TAG", "Rewarded video adError => " + adError.getErrorMessage());
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e("TAG", "Rewarded video ad is loaded and ready to be displayed!");
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e("TAG", "Rewarded video ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.e("TAG", "Rewarded video ad impression logged!");
                }

                @Override
                public void onRewardedVideoCompleted() {
                    Log.e("TAG", "Rewarded video completed!");
                }

                @Override
                public void onRewardedVideoClosed() {
                    Log.e("TAG", "Rewarded video ad closed!");
                    Utils.redirectToMainActivity(LoginActivity.this, "Skip");
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                }
            }).build());

        } catch (Exception e) {
            Log.e("fbRewardedVideoAd", "Exception => " + e.getMessage());
        }
    }

    /*Admob & Facebook Ads END*/

    @Override
    protected void onPause() {
        Utils.ProgressbarHide();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPermissionResult.unregister();
        Utils.ProgressbarHide();
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (mRewardedAd != null) {
            mRewardedAd = null;
        }
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
        if (fbRewardedVideoAd != null) {
            fbRewardedVideoAd.destroy();
            fbRewardedVideoAd = null;
        }
    }

}