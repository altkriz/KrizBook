package com.kriztech.book.OTPLogin;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.Activity.Registration;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import es.dmoral.toasty.Toasty;

public class SendOTP extends AppCompatActivity {

    private PrefManager prefManager;

    private LinearLayout lyBack;
    private TextView txtSendOtp, txtRegister, txtUserAgreement;
    private TextInputEditText editTextPhone, editTextCountryCode;
    private CountryCodePicker countryCodePicker;
    private CheckBox checkBox;

    private String mobileNumber, coutryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.HideNavigation(SendOTP.this);
        Utils.setTheme(SendOTP.this);
        setContentView(R.layout.activity_otp_send);
        PrefManager.forceRTLIfSupported(getWindow(), SendOTP.this);

        init();
        setHTMLTexts();

        txtSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumber = editTextPhone.getText().toString().trim();
                coutryCode = countryCodePicker.getSelectedCountryCode();
                Log.e("coutryCode", "" + coutryCode);

                if (TextUtils.isEmpty(mobileNumber)) {
                    Toasty.warning(SendOTP.this, "" + getResources().getString(R.string.enter_your_mobile_no), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(coutryCode)) {
                    Toasty.warning(SendOTP.this, "" + getResources().getString(R.string.enter_coutry_code), Toasty.LENGTH_SHORT).show();
                    return;
                }
                if (!checkBox.isChecked()) {
                    Toast.makeText(SendOTP.this, "" + getResources().getString(R.string.msg_for_checkbox), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(SendOTP.this, OTPVerification.class);
                intent.putExtra("entryFrom", "SendOTP");
                intent.putExtra("mobile", "+" + coutryCode + mobileNumber);
                startActivity(intent);
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SendOTP.this, Registration.class));
                finish();
            }
        });

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendOTP.this.finish();
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

    private void init() {
        try {
            prefManager = new PrefManager(SendOTP.this);

            lyBack = findViewById(R.id.lyBack);
            editTextPhone = findViewById(R.id.editTextPhone);
            countryCodePicker = findViewById(R.id.edtCountryCodePicker);
            txtSendOtp = findViewById(R.id.txtSendOtp);
            txtRegister = findViewById(R.id.txtRegister);
            txtUserAgreement = findViewById(R.id.txtUserAgreement);

            checkBox = findViewById(R.id.checkBox);

            countryCodePicker.setCountryForNameCode("IN");

        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void setHTMLTexts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtRegister.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#ffffff>" + getResources().getString(R.string.register) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                txtRegister.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#000000>" + getResources().getString(R.string.register) + "</font></b> </p>",
                        Html.FROM_HTML_MODE_LEGACY));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (PrefManager.getInstance(this).isNightModeEnabled() == true) {
                txtRegister.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#ffffff>" + getResources().getString(R.string.register) + "</font></b> </p>"));
            } else {
                txtRegister.setText(Html.fromHtml("<p> " + getResources().getString(R.string.dont) + " <b><font color=#000000>" + getResources().getString(R.string.register) + "</font></b> </p>"));
            }
            txtUserAgreement.setText(Html.fromHtml("<p> By continuing , I understand and agree with <a href="
                    + prefManager.getValue("privacy-policy") + ">Privacy Policy</a> and <a href="
                    + prefManager.getValue("terms-and-conditions") + ">Terms and Conditions</a> of " + getResources().getString(R.string.app_name) + ". </p>"));
        }
        txtUserAgreement.setClickable(true);
        txtUserAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}