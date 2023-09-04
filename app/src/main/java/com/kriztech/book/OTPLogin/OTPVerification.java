package com.kriztech.book.OTPLogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.Activity.MainActivity;
import com.kriztech.book.Activity.Profile;
import com.kriztech.book.Model.LoginRegister.LoginRegiModel;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.LocaleUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import cn.iwgang.countdownview.CountdownView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerification extends AppCompatActivity implements CountdownView.OnCountdownEndListener {

    private FirebaseAuth mAuth;

    private PrefManager prefManager;

    private LinearLayout lyResendOTP, lyBack;
    private EditText etOTP;
    private TextView txtVerifyProceed;
    private CountdownView countdownTimer;

    private String entryFrom = "", strMobile = "", strEmail = "", strUserName = "", strOTP, strDeviceToken, verificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private long countTime = 90 /*seconds*/, timerCount = 90000 /*milliseconds*/; //set same value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(OTPVerification.this);
        Utils.HideNavigation(OTPVerification.this);
        setContentView(R.layout.activity_otp_verify);
        PrefManager.forceRTLIfSupported(getWindow(), OTPVerification.this);

        // below line is for getting instance of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode(LocaleUtils.getSelectedLanguageId());

        init();

        strDeviceToken = Utils.getToken(OTPVerification.this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            entryFrom = bundle.getString("entryFrom");
            strMobile = bundle.getString("mobile");
            if (!TextUtils.isEmpty(bundle.getString("emailAddress"))) {
                strEmail = bundle.getString("emailAddress");
            } else {
                strEmail = "";
            }
            if (!TextUtils.isEmpty(bundle.getString("userName"))) {
                strUserName = bundle.getString("userName");
            } else {
                strUserName = "";
            }
            Log.e("entryFrom", "" + entryFrom);
            Log.e("mobile", "" + strMobile);
            Log.e("emailAddress", "" + strEmail);
            Log.e("userName", "" + strUserName);

            sendVerificationCode(strMobile);
            lyResendOTP.setVisibility(View.GONE);
            countdownTimer.setVisibility(View.VISIBLE);
            countdownTimer.start(timerCount);
        }

        txtVerifyProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strOTP = etOTP.getText().toString().trim();
                if (TextUtils.isEmpty(strOTP)) {
                    Toasty.warning(OTPVerification.this, "" + getResources().getString(R.string.enter_otp), Toasty.LENGTH_SHORT).show();
                    return;
                }
                verifyCode(strOTP);
            }
        });

        lyResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lyResendOTP.setVisibility(View.GONE);
                countdownTimer.setVisibility(View.VISIBLE);
                countdownTimer.start(timerCount);
                resendVerificationCode(strMobile, mResendToken);
            }
        });

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(OTPVerification.this);

            lyBack = findViewById(R.id.lyBack);
            lyResendOTP = findViewById(R.id.lyResendOTP);
            txtVerifyProceed = findViewById(R.id.txtVerifyProceed);
            etOTP = findViewById(R.id.etOTP);
            countdownTimer = findViewById(R.id.countdownTimer);
            countdownTimer.setOnCountdownEndListener(this);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        countdownTimer.stop();
        Log.e("onPause", "remainTime => " + countdownTimer.getRemainTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "remainTime => " + countdownTimer.getRemainTime());
        if (countdownTimer.getRemainTime() > 0) {
            countdownTimer.restart();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        Log.e("signInWithCredential", "SmsCode => " + credential.getSmsCode());
        Log.e("signInWithCredential", "Provider => " + credential.getProvider());
        Log.e("signInWithCredential", "SignInMethod => " + credential.getSignInMethod());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("AuthResult", "Credential => " + task.getResult().getCredential());
                            Log.e("AuthResult", "User => " + task.getResult().getUser());
                            Log.e("AuthResult", "AdditionalUserInfo => " + task.getResult().getAdditionalUserInfo());

                            if (entryFrom.equalsIgnoreCase("Payment")) {
                                UpdateMissingProfile();
                            } else {
                                SignInMobile();
                            }
                        } else {
                            Toasty.error(OTPVerification.this, "" + task.getException().getMessage(), Toasty.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void verifyCode(String code) {
        Log.e("=> verifyCode", "" + code);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void sendVerificationCode(String number) {
        Utils.ProgressBarShow(OTPVerification.this);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, countTime, TimeUnit.SECONDS, this, mCallBack);
        Utils.ProgressbarHide();
    }

    @Override
    public void onEnd(CountdownView cv) {
        countdownTimer.stop();
        countdownTimer.allShowZero();
        lyResendOTP.setVisibility(View.VISIBLE);
        countdownTimer.setVisibility(View.GONE);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        Utils.ProgressBarShow(OTPVerification.this);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, countTime, TimeUnit.SECONDS, this, mCallBack, token);
        Utils.ProgressbarHide();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            mResendToken = forceResendingToken;
            Log.e("verificationId", "" + verificationId);
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            Log.e("SmsCode", "" + code);
            if (code != null) {
                etOTP.setText("" + code);
                Utils.ProgressBarShow(OTPVerification.this);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.e("VerificationFailed", "" + e);
            Toasty.error(OTPVerification.this, "" + e.getMessage(),
                    Toasty.LENGTH_LONG).show();
            Utils.ProgressbarHide();
            OTPVerification.this.finish();
        }
    };

    private void SignInMobile() {
        if (!((Activity) OTPVerification.this).isFinishing()) {
            Utils.ProgressBarShow(OTPVerification.this);
        }

        Call<LoginRegiModel> call = BaseURL.getVideoAPI().loginwithotp(strMobile);
        call.enqueue(new Callback<LoginRegiModel>() {
            @Override
            public void onResponse(Call<LoginRegiModel> call, Response<LoginRegiModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("loginwithotp", "Message => " + response.body().getMessage());

                        prefManager.setLoginId("" + response.body().getResult().get(0).getId());
                        prefManager.setFirstTimeLaunch(false);

                        Intent intent;
                        if (response.body().getResult().get(0).getIsUpdated().equalsIgnoreCase("0")) {
                            Constant.isSelectPic = false;
                            intent = new Intent(OTPVerification.this, Profile.class);
                            intent.putExtra("from", "OTP");
                        } else {
                            intent = new Intent(OTPVerification.this, MainActivity.class);
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        Toasty.success(OTPVerification.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();

                    } else {
                        Utils.AlertDialog(OTPVerification.this, "" + response.body().getMessage(), false, true);
                    }

                } catch (Exception e) {
                    Log.e("loginwithotp", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<LoginRegiModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("loginwithotp", "onFailure => " + t.getMessage());
                Utils.AlertDialog(OTPVerification.this, "" + t.getMessage(), false, true);
            }
        });
    }

    //updateprofile API
    private void UpdateMissingProfile() {
        if (!((Activity) OTPVerification.this).isFinishing()) {
            Utils.ProgressBarShow(OTPVerification.this);
        }

        Call<SuccessModel> call = BaseURL.getVideoAPI().updateMissingData("" + prefManager.getLoginId(), "" + strUserName, "" + strEmail, "" + strMobile);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(@NonNull Call<SuccessModel> call, @NonNull Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("updateMobileEmail", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("updateMobile", "strEmail => " + strEmail);
                        Log.e("updateMobile", "strMobile => " + strMobile);

                        prefManager.setValue("Phone", "" + strMobile);

                        if (prefManager.getValue("userType").equalsIgnoreCase("3")) {
                            prefManager.setValue("Email", "" + strEmail);
                            prefManager.setValue("fullname", "" + strUserName);
                        } else if (!TextUtils.isEmpty(strEmail)) {
                            prefManager.setValue("Email", "" + strEmail);
                        } else if (!TextUtils.isEmpty(strUserName)) {
                            prefManager.setValue("fullname", "" + strUserName);
                        }

                        Toasty.success(OTPVerification.this, "" + getResources().getString(R.string.verification_success), Toasty.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Utils.AlertDialog(OTPVerification.this, "" + response.body().getMessage(), false, true);
                    }
                } catch (Exception e) {
                    Log.e("updateMobileEmail", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessModel> call, @NonNull Throwable t) {
                Log.e("updateMobileEmail", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(OTPVerification.this, "" + t.getMessage(), false, true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdownTimer.stop();
        countdownTimer.allShowZero();
    }

}