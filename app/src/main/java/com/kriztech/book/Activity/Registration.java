package com.kriztech.book.Activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registration extends AppCompatActivity {

    private PrefManager prefManager;

    private LinearLayout lyBack;
    private TextView txtRegistration, txtSignup, txtUserAgreement;
    private EditText etFullname, etEmail, etPhone;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private ImageView ivIcon;
    private CheckBox checkBox;

    private String strFullname = "", strEmail = "", strPassword = "", strPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.HideNavigation(Registration.this);
        Utils.setTheme(Registration.this);
        setContentView(R.layout.activity_registration);
        PrefManager.forceRTLIfSupported(getWindow(), Registration.this);

        Init();
        setHTMLTexts();

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    tilPassword.setPasswordVisibilityToggleEnabled(true);
                } else {
                    tilPassword.setPasswordVisibilityToggleEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strFullname = "" + etFullname.getText().toString().trim();
                strEmail = "" + etEmail.getText().toString().trim();
                strPassword = "" + etPassword.getText().toString().trim();
                strPhone = "" + etPhone.getText().toString().trim();

                if (TextUtils.isEmpty(strFullname)) {
                    Toasty.warning(Registration.this, "" + getResources().getString(R.string.enter_fullname), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strEmail)) {
                    Toasty.warning(Registration.this, "" + getResources().getString(R.string.enter_email), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strPassword)) {
                    Toasty.warning(Registration.this, "" + getResources().getString(R.string.enter_password), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strPhone)) {
                    Toasty.warning(Registration.this, "" + getResources().getString(R.string.enter_phone_number), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (!Utils.isEmailValid(strEmail)) {
                    Toasty.warning(Registration.this, "" + getResources().getString(R.string.enter_valid_email), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (!checkBox.isChecked()) {
                    Toast.makeText(Registration.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                    return;
                }

                SignUp();
            }
        });

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtUserAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            }
        });

    }

    private void Init() {
        try {
            prefManager = new PrefManager(this);

            lyBack = findViewById(R.id.lyBack);

            txtRegistration = findViewById(R.id.txt_registration);
            etFullname = findViewById(R.id.et_fullname);
            etEmail = findViewById(R.id.et_email);
            etPassword = findViewById(R.id.et_password);
            tilPassword = findViewById(R.id.tilPassword);
            etPhone = findViewById(R.id.et_phone);
            txtSignup = findViewById(R.id.txt_signup);
            txtUserAgreement = findViewById(R.id.txtUserAgreement);

            checkBox = findViewById(R.id.checkBox);
            ivIcon = findViewById(R.id.iv_icon);
        } catch (Exception e) {
            Log.e("Init Exception =>", "" + e);
        }
    }

    private void setHTMLTexts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtRegistration.setText(Html.fromHtml("<p> " + getResources().getString(R.string.already_account) + " <b><font color=#ffffff>" + getResources().getString(R.string.login) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                txtRegistration.setText(Html.fromHtml("<p> " + getResources().getString(R.string.already_account) + " <b><font color=#000000>" + getResources().getString(R.string.login) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtRegistration.setText(Html.fromHtml("<p> " + getResources().getString(R.string.already_account) + " <b><font color=#ffffff>" + getResources().getString(R.string.login) + "</font></b> </p>"));
            } else {
                txtRegistration.setText(Html.fromHtml("<p> " + getResources().getString(R.string.already_account) + " <b><font color=#000000>" + getResources().getString(R.string.login) + "</font></b> </p>"));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>"));
        }
        txtUserAgreement.setClickable(true);
        txtUserAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /* registration API */
    private void SignUp() {
        Utils.ProgressBarShow(Registration.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().Registration("" + strFullname, "" + strEmail, "" + strPassword, "" + strPhone);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("Registration", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        finish();
                    } else {
                        Log.e("Registration", "Message => " + response.body().getMessage());
                        Utils.AlertDialog(Registration.this, response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("Registration", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("Registration", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(Registration.this, t.getMessage(), false, false);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.ProgressbarHide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.ProgressbarHide();
    }

}