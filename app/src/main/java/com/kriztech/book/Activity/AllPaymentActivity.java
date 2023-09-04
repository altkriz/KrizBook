package com.kriztech.book.Activity;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.kriztech.book.Model.PayTmModel.PayTmModel;
import com.kriztech.book.Model.PayTmModel.Paytm;
import com.kriztech.book.Model.PaymentOptionModel.PaymentOptionModel;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPaymentActivity extends AppCompatActivity implements PaymentResultListener, View.OnClickListener,
        PurchasesUpdatedListener, PaytmPaymentTransactionCallback {

    private PrefManager prefManager;
    private static final String TAG = AllPaymentActivity.class.getSimpleName();

    private LinearLayout lyToolbar, lyBack, lyInApp, lyRazorpay, lyPaypal, lyFlutterwave, lyPayumoney, lyPaytm, lyWalletBalance, lyPayNow, lyBottom;
    private TextView txtToolbarTitle, txtWalletBalance, txtPayableAmount;
    private CheckBox cbWallet;

    private PaymentOptionModel optionModel;

    private static PayPalConfiguration config;
    private PayPalPayment thingToBuy;

    private PayUmoneySdkInitializer.PaymentParam paymentParam;

    private BillingClient billingClient;
    public static String PRODUCT_ID;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private boolean flag = false, walletCheck = false, isPayWithWallet = false;
    private String name = "", price = "", productPackage = "", finalPaymentType = "", paymentType = "", currencyCode = "", shortDescription = "", itemID = "",
            paymentId = "", state = "1", createTime = "", authorId = "", TYPE = "", fixedWalletAmount = "", walletAmount = "", gatewayAmout = "", payWith = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(this);
        setContentView(R.layout.activity_all_payment);
        PrefManager.forceRTLIfSupported(getWindow(), AllPaymentActivity.this);

        init();
        txtToolbarTitle.setText("" + getResources().getString(R.string.payment_details));
        GetPGOptions();

        Checkout.preload(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            TYPE = bundle.getString("TYPE");
            paymentType = bundle.getString("paymentType");
            itemID = bundle.getString("itemId");
            productPackage = bundle.getString("productPackage");
            price = bundle.getString("price");
            name = bundle.getString("title");
            shortDescription = bundle.getString("desc");
            createTime = bundle.getString("date");
            authorId = bundle.getString("author");
            walletAmount = bundle.getString("walletBalance");
            currencyCode = prefManager.getValue("currency_code");

            txtPayableAmount.setText(prefManager.getValue("currency_symbol") + " " + price);

            if (TYPE.equalsIgnoreCase("Package")) {
                lyWalletBalance.setVisibility(View.GONE);
                lyBottom.setVisibility(View.VISIBLE);
                walletAmount = "0";
            } else {
                txtWalletBalance.setText("" + walletAmount);
            }
            finalPaymentType = paymentType;
            fixedWalletAmount = walletAmount;
            gatewayAmout = "" + price;

            Log.e("TYPE", "" + TYPE);
            Log.e("paymentType", "" + paymentType);
            Log.e("finalPaymentType", "" + finalPaymentType);
            Log.e("itemID", "" + itemID);
            Log.e("productPackage", "" + productPackage);
            Log.e("price", "" + price);
            Log.e("name", "" + name);
            Log.e("short_description", "" + shortDescription);
            Log.e("date", "" + createTime);
            Log.e("author_id", "" + authorId);
            Log.e("walletAmount", "" + walletAmount);
            Log.e("gatewayAmout", "" + gatewayAmout);
            Log.e("currencyCode", "" + currencyCode);

            //Create PaymentID
            makePaymentID();
        }

        if (walletAmount.equalsIgnoreCase("0")) {
            lyBottom.setVisibility(View.VISIBLE);
        }

        cbWallet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    walletCheck = true;
                    if (Integer.parseInt(walletAmount) >= Integer.parseInt(price)) {
                        isPayWithWallet = true;
                    } else {
                        isPayWithWallet = false;
                    }
                    if (isPayWithWallet) {
                        lyInApp.setBackgroundResource(0);
                        lyPaypal.setBackgroundResource(0);
                        lyRazorpay.setBackgroundResource(0);
                        lyPaytm.setBackgroundResource(0);
                        lyFlutterwave.setBackgroundResource(0);
                        lyPayumoney.setBackgroundResource(0);
                    }
                } else {
                    walletCheck = false;
                    isPayWithWallet = false;
                    gatewayAmout = "" + price;
                }
                lyBottom.setVisibility(View.VISIBLE);
            }
        });

        //In-App purchase
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();

    }

    private void init() {
        try {
            prefManager = new PrefManager(this);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            txtPayableAmount = findViewById(R.id.txtPayableAmount);
            txtWalletBalance = findViewById(R.id.txtWalletBalance);
            lyWalletBalance = findViewById(R.id.lyWalletBalance);
            cbWallet = findViewById(R.id.cbWallet);
            lyBottom = findViewById(R.id.lyBottom);
            lyPayNow = findViewById(R.id.lyPayNow);

            lyInApp = findViewById(R.id.lyInApp);
            lyRazorpay = findViewById(R.id.lyRazorpay);
            lyPaypal = findViewById(R.id.lyPaypal);
            lyFlutterwave = findViewById(R.id.lyFlutterwave);
            lyPayumoney = findViewById(R.id.lyPayumoney);
            lyPaytm = findViewById(R.id.lyPaytm);

            lyBack.setOnClickListener(this);
            lyInApp.setOnClickListener(this);
            lyRazorpay.setOnClickListener(this);
            lyPaypal.setOnClickListener(this);
            lyFlutterwave.setOnClickListener(this);
            lyPayumoney.setOnClickListener(this);
            lyPaytm.setOnClickListener(this);
            lyWalletBalance.setOnClickListener(this);
            lyPayNow.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    /* get_paymentoption API */
    private void GetPGOptions() {

        Call<PaymentOptionModel> call = BaseURL.getVideoAPI().get_paymentoption();
        call.enqueue(new Callback<PaymentOptionModel>() {
            @Override
            public void onResponse(Call<PaymentOptionModel> call, Response<PaymentOptionModel> response) {
                Utils.ProgressbarHide();
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e(TAG, "get_paymentoption status ==> " + response.body().getStatus());
                        optionModel = new PaymentOptionModel();
                        optionModel = response.body();
                        Log.e(TAG, "optionModel ==> " + optionModel.getFlutterwave().getName());
                        Log.e(TAG, "optionModel ==> " + optionModel.getInapppurchage().getName());

                        SetPGOptions();
                    } else {
                        Log.e(TAG, "get_paymentoption Message ==> " + response.body().getStatus());
                    }
                } catch (Exception e) {
                    Log.e("get_paymentoption", "onFailure ==> " + e);
                }
            }

            @Override
            public void onFailure(Call<PaymentOptionModel> call, Throwable t) {
                Log.e("get_paymentoption", "onFailure ==> " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    private void SetPGOptions() {
        /* InAppPurchage */
        if (optionModel.getInapppurchage().getVisibility().equalsIgnoreCase("1")) {
            lyInApp.setVisibility(View.VISIBLE);
            if (optionModel.getInapppurchage().getIsLive().equalsIgnoreCase("1")) {
                Constant.InApp_isLive = true;
            } else {
                Constant.InApp_isLive = false;
            }
        } else {
            lyInApp.setVisibility(View.GONE);
        }

        /* PayPal */
        if (optionModel.getPaypal().getVisibility().equalsIgnoreCase("1")) {
            lyPaypal.setVisibility(View.VISIBLE);
            if (optionModel.getPaypal().getIsLive().equalsIgnoreCase("1")) {
                Constant.PAYPAL_CLIENT_ID = "" + optionModel.getPaypal().getLiveKey1();
                Constant.CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
            } else {
                Constant.PAYPAL_CLIENT_ID = "" + optionModel.getPaypal().getTestKey1();
                Constant.CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
            }

            //PayPal
            Log.e("PayPal", "Environment ==>> " + Constant.CONFIG_ENVIRONMENT);
            Log.e("PayPal", "Client ID ==>> " + Constant.PAYPAL_CLIENT_ID);

            config = new PayPalConfiguration()
                    .environment(Constant.CONFIG_ENVIRONMENT)
                    .clientId(Constant.PAYPAL_CLIENT_ID)
                    .merchantName("" + getResources().getString(R.string.app_name))
                    .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                    .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
            Intent intent = new Intent(this, PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            startService(intent);
        } else {
            lyPaypal.setVisibility(View.GONE);
        }

        /* RazorPay */
        if (optionModel.getRazorpay().getVisibility().equalsIgnoreCase("1")) {
            lyRazorpay.setVisibility(View.VISIBLE);
        } else {
            lyRazorpay.setVisibility(View.GONE);
        }

        /* Paytm */
        if (optionModel.getPaytm().getVisibility().equalsIgnoreCase("1")) {
            lyPaytm.setVisibility(View.VISIBLE);
            if (optionModel.getPaytm().getIsLive().equalsIgnoreCase("1")) {
                Constant.PayTmMerchant_ID = "" + optionModel.getPaytm().getLiveKey1();
                Constant.PayTmMerchant_Key = "" + optionModel.getPaytm().getLiveKey2();
                Constant.PayTm_isLive = true;
            } else {
                Constant.PayTmMerchant_ID = "" + optionModel.getPaytm().getTestKey1();
                Constant.PayTmMerchant_Key = "" + optionModel.getPaytm().getTestKey2();
                Constant.PayTm_isLive = false;
            }
        } else {
            lyPaytm.setVisibility(View.GONE);
        }

        /* Flutterwave */
        if (optionModel.getFlutterwave().getVisibility().equalsIgnoreCase("1")) {
            lyFlutterwave.setVisibility(View.VISIBLE);
            if (optionModel.getFlutterwave().getIsLive().equalsIgnoreCase("1")) {
                Constant.FWPublic_Key = "" + optionModel.getFlutterwave().getLiveKey1();
                Constant.FWEncryption_Key = "" + optionModel.getFlutterwave().getLiveKey2();
                Constant.FW_isLive = true;
            } else {
                Constant.FWPublic_Key = "" + optionModel.getFlutterwave().getTestKey1();
                Constant.FWEncryption_Key = "" + optionModel.getFlutterwave().getTestKey2();
                Constant.FW_isLive = false;
            }
        } else {
            lyFlutterwave.setVisibility(View.GONE);
        }

        /* PayUMoney */
        if (optionModel.getPayumoney().getVisibility().equalsIgnoreCase("1")) {
            lyPayumoney.setVisibility(View.VISIBLE);
            if (optionModel.getPayumoney().getIsLive().equalsIgnoreCase("1")) {
                Constant.PayU_isDebug = false;
                Constant.PayUMerchant_ID = "" + optionModel.getPayumoney().getLiveKey1();
                Constant.PayUMerchant_Key = "" + optionModel.getPayumoney().getLiveKey2();
                Constant.PayUMerchant_Salt = "" + optionModel.getPayumoney().getLiveKey3();
            } else {
                Constant.PayU_isDebug = true;
                Constant.PayUMerchant_ID = "" + optionModel.getPayumoney().getTestKey1();
                Constant.PayUMerchant_Key = "" + optionModel.getPayumoney().getTestKey2();
                Constant.PayUMerchant_Salt = "" + optionModel.getPayumoney().getTestKey3();
            }
        } else {
            lyPayumoney.setVisibility(View.GONE);
        }
    }

    private void makePaymentID() {
        paymentId = "" + Utils.generateRandomOrderID() + "" + prefManager.getLoginId();
        Log.e("paymentId", "=>>> " + paymentId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyInApp:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "InApp";
                    lyInApp.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyPaypal.setBackgroundResource(0);
                    lyRazorpay.setBackgroundResource(0);
                    lyPaytm.setBackgroundResource(0);
                    lyFlutterwave.setBackgroundResource(0);
                    lyPayumoney.setBackgroundResource(0);
                }
                break;

            case R.id.lyPaypal:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "Paypal";
                    lyPaypal.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyInApp.setBackgroundResource(0);
                    lyRazorpay.setBackgroundResource(0);
                    lyPaytm.setBackgroundResource(0);
                    lyFlutterwave.setBackgroundResource(0);
                    lyPayumoney.setBackgroundResource(0);
                }
                break;

            case R.id.lyRazorpay:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "Razorpay";
                    lyRazorpay.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyInApp.setBackgroundResource(0);
                    lyPaypal.setBackgroundResource(0);
                    lyPaytm.setBackgroundResource(0);
                    lyFlutterwave.setBackgroundResource(0);
                    lyPayumoney.setBackgroundResource(0);
                }
                break;

            case R.id.lyPaytm:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "Paytm";
                    lyPaytm.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyInApp.setBackgroundResource(0);
                    lyRazorpay.setBackgroundResource(0);
                    lyPaypal.setBackgroundResource(0);
                    lyFlutterwave.setBackgroundResource(0);
                    lyPayumoney.setBackgroundResource(0);
                }
                break;

            case R.id.lyFlutterwave:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "Flutterwave";
                    lyFlutterwave.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyInApp.setBackgroundResource(0);
                    lyRazorpay.setBackgroundResource(0);
                    lyPaytm.setBackgroundResource(0);
                    lyPaypal.setBackgroundResource(0);
                    lyPayumoney.setBackgroundResource(0);
                }
                break;

            case R.id.lyPayumoney:
                if (!isPayWithWallet) {
                    lyBottom.setVisibility(View.VISIBLE);
                    payWith = "PayUMoney";
                    lyPayumoney.setBackground(getResources().getDrawable(R.drawable.round_bor_colored));
                    lyInApp.setBackgroundResource(0);
                    lyRazorpay.setBackgroundResource(0);
                    lyPaytm.setBackgroundResource(0);
                    lyFlutterwave.setBackgroundResource(0);
                    lyPaypal.setBackgroundResource(0);
                }
                break;

            case R.id.lyPayNow:
                if (isPayWithWallet && walletCheck) {
                    MakeItFinal();
                    Purchasebook();
                } else {
                    UsePaymentGateways();
                }
                break;

            case R.id.lyWalletBalance:
                if (walletCheck) {
                    cbWallet.setChecked(false);
                } else {
                    cbWallet.setChecked(true);
                }
                break;

            case R.id.lyBack:
                finish();
                break;

            default:
                Toasty.warning(AllPaymentActivity.this, "" + getResources().getString(R.string.please_select_any_payment_method), Toasty.LENGTH_SHORT).show();
                break;
        }
    }

    private void UsePaymentGateways() {
        MakeItFinal();

        if (!TextUtils.isEmpty(payWith)) {

            Log.e("payWith", "" + payWith);
            if (payWith.equalsIgnoreCase("InApp")) {
                BillingAssign();
            } else if (payWith.equalsIgnoreCase("Paypal")) {
                PayPal();
            } else if (payWith.equalsIgnoreCase("Razorpay")) {
                startPayment(String.valueOf((Double.parseDouble(gatewayAmout)) * 100), name);
            } else if (payWith.equalsIgnoreCase("Paytm")) {
                generateCheckSum();
            } else if (payWith.equalsIgnoreCase("Flutterwave")) {
                FlutterWave();
            } else if (payWith.equalsIgnoreCase("PayUMoney")) {
                payUMoney();
            }

        } else {
            Toasty.warning(AllPaymentActivity.this, "" + getResources().getString(R.string.please_select_any_payment_method), Toasty.LENGTH_SHORT).show();
        }
    }

    private void MakeItFinal() {
        if (walletCheck) {
            walletAmount = fixedWalletAmount;
            if (Integer.parseInt(price) > Integer.parseInt(walletAmount)) {
                gatewayAmout = "" + (Integer.parseInt(price) - Integer.parseInt(walletAmount));
            } else if (Integer.parseInt(walletAmount) >= Integer.parseInt(price)) {
                walletAmount = "" + price;
                gatewayAmout = "0";
            } else {
                gatewayAmout = "" + price;
            }
        } else {
            walletAmount = "0";
            gatewayAmout = "" + price;
        }
        Log.e("walletAmount", "" + walletAmount);
        Log.e("gatewayAmout", "" + gatewayAmout);
    }

    /* add_transaction API */
    private void Purchasebook() {
        if (Integer.parseInt(walletAmount) > 0 && Integer.parseInt(gatewayAmout) > 0) {
            finalPaymentType = "3";
        } else if (Integer.parseInt(walletAmount) > 0 && Integer.parseInt(gatewayAmout) == 0) {
            finalPaymentType = "2";
        } else if (Integer.parseInt(walletAmount) == 0 && Integer.parseInt(gatewayAmout) > 0) {
            finalPaymentType = "1";
        }
        Log.e("finalPaymentType", "" + finalPaymentType);

        if (!((Activity) AllPaymentActivity.this).isFinishing()) {
            Utils.ProgressBarShow(AllPaymentActivity.this);
        }

        Call<SuccessModel> call = null;
        if (TYPE.equalsIgnoreCase("Book")) {
            call = BaseURL.getVideoAPI().add_purchase(itemID, prefManager.getLoginId(), price, currencyCode, name, state, authorId, paymentId, finalPaymentType, walletAmount, gatewayAmout);

        } else if (TYPE.equalsIgnoreCase("Magazine")) {
            call = BaseURL.getVideoAPI().add_magazine_purchase(itemID, prefManager.getLoginId(), price, currencyCode, name, state, authorId, paymentId, finalPaymentType, walletAmount, gatewayAmout);

        } else if (TYPE.equalsIgnoreCase("Package")) {
            call = BaseURL.getVideoAPI().add_package_transaction("" + prefManager.getLoginId(), price, itemID, paymentId, state);
        }
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    if (response.body().getStatus() == 200) {
                        flag = true;
                        Log.e("add_purchase status", "" + response.body().getStatus());
                    } else {
                        Log.e("add_purchase status", "" + response.body().getStatus());
                        flag = false;
                    }
                    Utils.AlertDialog(AllPaymentActivity.this, response.body().getMessage(), flag, true);

                } catch (Exception e) {
                    Log.e("add_purchase", "onFailure => " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_purchase", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(AllPaymentActivity.this, t.getMessage(), false, true);
            }
        });
    }


    /*==================== In-App purchase START =====================*/

    public void BillingAssign() {
        if (billingClient.isReady()) {
            if (Constant.InApp_isLive) {
                PRODUCT_ID = "" + productPackage;
            } else {
                PRODUCT_ID = "" + Constant.PRODUCT_ID;
            }
            Log.e("PRODUCT_ID", "==>>> " + PRODUCT_ID);
            initiatePurchase(PRODUCT_ID);
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(AllPaymentActivity.this).enablePendingPurchases().setListener(AllPaymentActivity.this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        PRODUCT_ID = "" + productPackage;
                        initiatePurchase(PRODUCT_ID);
                    } else {
                        Toasty.error(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toasty.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }

    }

    private void initiatePurchase(final String PRODUCT_ID) {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            Log.e("Billing", "onSkuDetailsResponse => " + skuDetailsList.get(0));
            Log.e("Billing", "Title => " + skuDetailsList.get(0).getTitle());
            Log.e("Billing", "ResponseCode => " + billingResult.getResponseCode());

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size() > 0) {
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList.get(0))
                            .build();
                    billingClient.launchBillingFlow(AllPaymentActivity.this, flowParams);
                } else {
                    //try to add item/product id "c1" "c2" "c3" inside managed product in google play console
                    Toasty.warning(getApplicationContext(), "Purchase item " + PRODUCT_ID + " not found.", Toasty.LENGTH_SHORT).show();
                }
            } else {
                Toasty.error(getApplicationContext(), " Error " + billingResult.getDebugMessage(), Toasty.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        billingClient.endConnection();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        Log.e("onPurchasesUpdated", "ResponseCode => " + billingResult.getResponseCode());
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

            billingClient.queryPurchasesAsync(INAPP, (billingResult1, list) -> handlePurchases(list));

        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toasty.info(getApplicationContext(), "Purchase cancelled", Toasty.LENGTH_SHORT).show();
        }
        // Handle any other error messages
        else {
            Toasty.error(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toasty.LENGTH_SHORT).show();
        }
    }

    void handlePurchases(List<Purchase> purchases) {

        for (Purchase purchase : purchases) {
            Log.e("PurchaseState", "" + purchase.getPurchaseState());
            //if item is purchased
            if (purchase.getSkus().contains(PRODUCT_ID) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                Log.e("OriginalJson", "" + purchase.getOriginalJson());
                Log.e("Signature", "" + purchase.getSignature());

                Purchasebook();
                // else purchase is valid
                //if item is purchased and not consumed
                Log.e("isAcknowledged", "" + purchase.isAcknowledged());
                if (!purchase.isAcknowledged()) {
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingClient.consumeAsync(consumeParams, consumeListener);
                }
            }
            //if purchase is pending
            else if (purchase.getSkus().contains(PRODUCT_ID) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                Toasty.info(getApplicationContext(), "Purchase is pending. Please complete transaction", Toasty.LENGTH_SHORT).show();
            }
            //if purchase is refunded or unknown
            else if (purchase.getSkus().contains(PRODUCT_ID) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                Toasty.info(getApplicationContext(), "Purchase status unknown", Toasty.LENGTH_SHORT).show();
            }
        }

    }

    ConsumeResponseListener consumeListener = (billingResult, purchaseToken) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Toasty.success(getApplicationContext(), "Item consumed", Toasty.LENGTH_SHORT).show();
        }
    };

    /*==================== In-App purchase END =====================*/


    /*=================== RazorPay START ===================*/

    private void startPayment(String amount, String name) {
        Log.e("amount_pay", "" + amount);
        Log.e("Name", "" + name);
        Checkout checkout = new Checkout();
        //checkout.setImage(R.drawable.logo);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();
            options.put("name", name);
            options.put("amount", amount);
            options.put("description", "" + shortDescription);
            options.put("currency", "" + prefManager.getValue("currency_code"));
            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("error", "Error in starting RazorPay Checkout", e);
            Log.e("error_msg", "msg" + e.getMessage());
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.e("on_success", "" + s);
        Toasty.success(getApplicationContext(), "Payment Successful", Toasty.LENGTH_SHORT).show();
        Toasty.success(this, "Payment Successful: " + s, Toasty.LENGTH_SHORT).show();
        Log.e("successfully", "Payment Successful");

        Purchasebook();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("onPaymentError", "i==> " + i);
        Log.e("onPaymentError", "s==> " + s);
    }

    /*=================== RazorPay END ===================*/


    /*=================== Paypal START ===================*/

    private void PayPal() {
        Log.e("paypal_payment", "paypal_payment");
        thingToBuy = new PayPalPayment(new BigDecimal(Double.parseDouble(gatewayAmout)), "" + prefManager.getValue("currency_code"),
                "" + prefManager.getValue("fullname"), PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(AllPaymentActivity.this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    public void onFuturePaymentPressed(View pressed) {
        Intent intent = new Intent(AllPaymentActivity.this,
                PayPalFuturePaymentActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FUTURE_PAYMENT);
    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {
    }

    public void onFuturePaymentPurchasePressed(View pressed) {
        // Get the Application Correlation ID from the SDK
        String correlationId = PayPalConfiguration
                .getApplicationCorrelationId(this);

        Log.e("FuturePaymentExample", "Application Correlation ID: "
                + correlationId);
        // TODO: Send correlationId and transaction details to your server for
        // processing with
        // PayPal...
        Toasty.info(getApplicationContext(),
                "App Correlation ID received from SDK", Toasty.LENGTH_LONG)
                .show();
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));

        billingClient.endConnection();
        super.onDestroy();
    }

    /*=================== Paypal END ===================*/

    //onActivityResult for "FlutterWave", "PayPal" & "PayUMoney"
    @SuppressLint({"MissingSuperCall", "LongLogTag"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //PayPal Result
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject().toString(4));

                        Log.e("confirm1", "" + confirm.toJSONObject().toString(4));
                        Log.e("confirm2", "" + confirm.getPayment().toJSONObject().toString(4));

                        gatewayAmout = confirm.getPayment().toJSONObject().getString("amount");
                        currencyCode = confirm.getPayment().toJSONObject().getString("currency_code");
                        shortDescription = confirm.getPayment().toJSONObject().getString("short_description");
                        paymentId = confirm.getProofOfPayment().toJSONObject().getString("id");
                        state = confirm.getProofOfPayment().toJSONObject().getString("state");
                        createTime = confirm.getProofOfPayment().toJSONObject().getString("create_time");

                        Log.e("gatewayAmout", "" + gatewayAmout);
                        Log.e("currencyCode", "" + currencyCode);
                        Log.e("short_description", "" + shortDescription);
                        Log.e("payment_id", "" + paymentId);
                        Log.e("state", "" + state);
                        Log.e("create_time", "" + createTime);

                        Purchasebook();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                System.out.println("The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                System.out.println("An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.e("FuturePaymentExample", auth.toJSONObject()
                                .toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.e("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toasty.info(getApplicationContext(), "Future Payment code received from PayPal", Toasty.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.e("FuturePaymentExample", "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }

        //Flutter Wave Result
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            Log.e("data", "" + data);
            String message = data.getStringExtra("response");
            Log.e("FlutterWave message ==>", "" + message);

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                JSONObject jsonRootObject = null;
                String currency_code = "";
                try {
                    assert message != null;
                    jsonRootObject = new JSONObject(message);
                    currency_code = jsonRootObject.getJSONObject("data").getString("currency");
                    String chargeResponseMessage = jsonRootObject.getJSONObject("data").getString("chargeResponseMessage");
                    Log.e("chargeResponseMessage ==>", "" + chargeResponseMessage);
                    Log.e("currency_code ==>", "" + currency_code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("gatewayAmout", "" + gatewayAmout);
                Log.e("currencyCode", "" + currencyCode);
                Log.e("short_description", "" + shortDescription);
                Log.e("payment_id", "" + paymentId);
                Log.e("state", "" + state);
                Log.e("create_time", "" + createTime);

                Purchasebook();

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                try {
                    assert message != null;
                    JSONObject jsonRootObject = new JSONObject(message);
                    String chargeResponseMessage = jsonRootObject.getJSONObject("data").getString("chargeResponseMessage");

                    Utils.AlertDialog(AllPaymentActivity.this, chargeResponseMessage, false, true);
                } catch (Exception e) {
                    String chargeResponseMessage = "" + getResources().getString(R.string.sorry_we_could_not_verify_your_payment_try_later);

                    Utils.AlertDialog(AllPaymentActivity.this, chargeResponseMessage, false, true);
                    Log.e("Exception", "" + e.getMessage());
                }
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toasty.info(AllPaymentActivity.this, "CANCELLED ", Toasty.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e("data-else", "" + data);
            super.onActivityResult(requestCode, resultCode, data);
        }

        //PayUMoney Result
        // Result Code is -1 send from Payumoney activity
        Log.e("AllPaymentActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                Log.e("gatewayAmout", "" + gatewayAmout);
                Log.e("currencyCode", "" + currencyCode);
                Log.e("short_description", "" + shortDescription);
                Log.e("payment_id", "" + paymentId);
                Log.e("state", "" + state);
                Log.e("create_time", "" + createTime);

                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //Success TransactionHistory
                    Log.e("resultCode", "" + resultCode);
                    Log.e("requestCode", "" + requestCode);

                    Purchasebook();

                } else {
                    //Failure TransactionHistory
                    Log.e("resultCode", "" + resultCode);
                    Log.e("requestCode", "" + requestCode);
                }

                // Response from PayUMoney
                String payuResponse = transactionResponse.getPayuResponse();
                Log.e("PayUResponse ==>", "" + payuResponse);

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
                Log.e("MerchantResponse ==>", "" + merchantResponse);

            }
        }
    }

    /*=================== Flutter Wave START ===================*/

    public void FlutterWave() {
        Log.e("Environment", "==>> " + Constant.FW_isLive);
        Log.e("Flutterwave", "PublicKey ==>> " + Constant.PayUMerchant_Key);
        Log.e("Flutterwave", "EncryptionKey ==>> " + Constant.PayUMerchant_ID);

        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();

        new RaveUiManager(AllPaymentActivity.this)
                .setAmount(Double.parseDouble(gatewayAmout))
                .setCurrency("" + prefManager.getValue("currency_code"))
                .setEmail("" + prefManager.getValue("Email"))
                .setfName("" + prefManager.getValue("fullname"))
                .setNarration("narration")
                .setPublicKey(Constant.FWPublic_Key)
                .setEncryptionKey(Constant.FWEncryption_Key)
                .setTxRef(ts)
                .setPhoneNumber("" + prefManager.getValue("Phone"), true)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptMpesaPayments(true)
                .acceptAchPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptUgMobileMoneyPayments(true)
                .acceptZmMobileMoneyPayments(true)
                .acceptRwfMobileMoneyPayments(true)
                .acceptSaBankPayments(true)
                .acceptUkPayments(true)
                .acceptBankTransferPayments(true)
                .acceptUssdPayments(true)
                .acceptBarterPayments(true)
                .acceptFrancMobileMoneyPayments(false, null)
                .allowSaveCardFeature(true)
                .onStagingEnv(Constant.FW_isLive)
                .isPreAuth(true)
                .shouldDisplayFee(true)
                .showStagingLabel(true)
                .initialize();
    }

    /*=================== Flutter Wave END ===================*/


    /*=================== PayUMoney START ===================*/

    private void payUMoney() {
        Log.e("Environment", "==>> " + Constant.PayU_isDebug);
        Log.e("PayU", "MKey ==>> " + Constant.PayUMerchant_Key);
        Log.e("PayU", "MID ==>> " + Constant.PayUMerchant_ID);
        Log.e("PayU", "MSaltKey ==>> " + Constant.PayUMerchant_Salt);

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new
                PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount("" + gatewayAmout)
                .setTxnId("" + prefManager.getLoginId() + "" + System.currentTimeMillis())
                .setPhone("" + prefManager.getValue("Phone"))
                .setProductName("" + name)
                .setFirstName("" + prefManager.getValue("fullname"))
                .setEmail("" + prefManager.getValue("Email"))
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(Constant.PayU_isDebug) // Integration environment - true (Debug)/ false(Production)
                .setKey("" + Constant.PayUMerchant_Key)
                .setMerchantId("" + Constant.PayUMerchant_ID);

        try {
            paymentParam = builder.build();

//            if (!Constant.PayU_isDebug) {
//                //below method for Live Payments
//                generateHashFromServer(paymentParam);
//            } else {
            //below method for Test Payments
            calculateServerSideHashAndInitiatePayment1(paymentParam);
            PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, AllPaymentActivity.this,
                    R.style.AppTheme_default, false);
//            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PayUMoney", "Exception => " + e);
            Utils.AlertDialog(AllPaymentActivity.this, "" + e.getMessage(), false, true);
        }
    }

    /**
     * Thus function calculates the hash for transaction
     *
     * @param paymentParam payment params of transaction
     * @return payment params along with calculated merchant hash
     */
    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1
    (final PayUmoneySdkInitializer.PaymentParam paymentParam) {

        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5)).append("||||||");
        stringBuilder.append(Constant.PayUMerchant_Salt);

        String hash = hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);

        return paymentParam;
    }

    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte[] messageDigest = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    /**
     * This method generates hash from server.
     *
     * @param paymentParam payments params used for hash generation
     */
    public void generateHashFromServer(PayUmoneySdkInitializer.PaymentParam paymentParam) {
        //nextButton.setEnabled(false); // lets not allow the user to click the button again and again.

        HashMap<String, String> params = paymentParam.getParams();

        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayUmoneyConstants.KEY, params.get(PayUmoneyConstants.KEY)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.AMOUNT, params.get(PayUmoneyConstants.AMOUNT)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.TXNID, params.get(PayUmoneyConstants.TXNID)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.EMAIL, params.get(PayUmoneyConstants.EMAIL)));
        postParamsBuffer.append(concatParams("productinfo", params.get(PayUmoneyConstants.PRODUCT_INFO)));
        postParamsBuffer.append(concatParams("firstname", params.get(PayUmoneyConstants.FIRSTNAME)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.UDF1, params.get(PayUmoneyConstants.UDF1)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.UDF2, params.get(PayUmoneyConstants.UDF2)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.UDF3, params.get(PayUmoneyConstants.UDF3)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.UDF4, params.get(PayUmoneyConstants.UDF4)));
        postParamsBuffer.append(concatParams(PayUmoneyConstants.UDF5, params.get(PayUmoneyConstants.UDF5)));
        postParamsBuffer.append(concatParams("SALT", Constant.PayUMerchant_Salt));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();

        // lets make an api call
        GetHashesFromServerTask getHashesFromServerTask = new GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }

    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    /**
     * This AsyncTask generates hash from server.
     */
    private class GetHashesFromServerTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.ProgressBarShow(AllPaymentActivity.this);
        }

        @Override
        protected String doInBackground(String... postParams) {

            String merchantHash = "";
            try {
                //TODO Below url is just for testing purpose, merchant needs to replace this with their server side hash generation url
                URL url = new URL("https://payu.herokuapp.com/get_hash");

                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                InputStream responseInputStream = conn.getInputStream();
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                Iterator<String> payuHashIterator = response.keys();
                while (payuHashIterator.hasNext()) {
                    String key = payuHashIterator.next();
                    switch (key) {
                        /**
                         * This hash is mandatory and needs to be generated from merchant's server side
                         *
                         */
                        case "payment_hash":
                            merchantHash = response.getString(key);
                            Log.e("merchantHash ==>", "" + merchantHash);
                            break;
                        default:
                            break;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return merchantHash;
        }

        @Override
        protected void onPostExecute(String merchantHash) {
            super.onPostExecute(merchantHash);
            Utils.ProgressbarHide();

            if (merchantHash.isEmpty() || merchantHash.equals("")) {
                Toasty.error(AllPaymentActivity.this, "Could not generate hash", Toasty.LENGTH_SHORT).show();
            } else {
                paymentParam.setMerchantHash(merchantHash);
                PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, AllPaymentActivity.this,
                        R.style.AppTheme_default, false);
            }
        }
    }

    /*=================== PayUMoney END ===================*/


    /*=================== PayTm START ===================*/

    private void generateCheckSum() {
        String orderID, custID;
        orderID = prefManager.getLoginId() + "_" + Utils.generateString();
        custID = Utils.generateString();

        final Paytm paytm;
        //containing all the values required
        if (Constant.PayTm_isLive) {
            paytm = new Paytm(
                    Constant.PayTmMerchant_ID,
                    orderID,
                    custID,
                    Constant.CHANNEL_ID,
                    "" + gatewayAmout + ".00",
                    Constant.LIVE_WEBSITE,
                    Constant.LIVE_CALLBACK_URL + orderID,
                    Constant.INDUSTRY_TYPE_ID
            );
        } else {
            paytm = new Paytm(
                    Constant.PayTmMerchant_ID,
                    orderID,
                    custID,
                    Constant.CHANNEL_ID,
                    "" + gatewayAmout + ".00",
                    Constant.TEST_WEBSITE,
                    Constant.TEST_CALLBACK_URL + orderID,
                    Constant.INDUSTRY_TYPE_ID
            );
        }

        Utils.ProgressBarShow(AllPaymentActivity.this);
        //creating a call object from the apiService
        Call<PayTmModel> call = BaseURL.getVideoAPI().getPaymentToken(
                paytm.getmId(),
                paytm.getOrderId(),
                paytm.getCustId(),
                paytm.getChannelId(),
                paytm.getTxnAmount(),
                paytm.getWebsite(),
                paytm.getCallBackUrl(),
                paytm.getIndustryTypeId()
        );
        //making the call to generate checksum
        call.enqueue(new Callback<PayTmModel>() {
            @Override
            public void onResponse(Call<PayTmModel> call, Response<PayTmModel> response) {
                Utils.ProgressbarHide();
                try {
                    //once we get the checksum we will initiailize the payment.
                    //the method is taking the checksum we got and the paytm object as the parameter
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("=>PaytmChecksum", "" + response.body().getResult().getPaytmChecksum());
                        initializePaytmPayment(response.body().getResult().getPaytmChecksum(), paytm);
                    }
                } catch (Exception e) {
                    Log.e("getPaymentToken", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<PayTmModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("getPaymentToken", "onFailure => " + t.getMessage());
            }
        });
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {
        try {
            // choosing between PayTM staging and production
            PaytmPGService pgService;
            if (Constant.PayTm_isLive) {
                pgService = PaytmPGService.getProductionService();
            } else {
                pgService = PaytmPGService.getStagingService();
            }

            //creating a hashmap and adding all the values required
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("MID", paytm.getmId());
            paramMap.put("ORDER_ID", paytm.getOrderId());
            paramMap.put("CUST_ID", paytm.getCustId());
            paramMap.put("CHANNEL_ID", paytm.getChannelId());
            paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
            paramMap.put("WEBSITE", paytm.getWebsite());
            paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
            paramMap.put("CHECKSUMHASH", checksumHash);
            paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());

            //creating a paytm order object using the hashmap
            PaytmOrder order = new PaytmOrder(paramMap);

            //intializing the paytm service
            pgService.initialize(order, null);

            //finally starting the payment transaction
            pgService.startPaymentTransaction(this, true, true, this);

        } catch (Exception e) {
            Log.e("Exception", "" + e.getMessage());
        }
    }

    //all these overriden method is to detect the payment result accordingly
    @Override
    public void onTransactionResponse(Bundle bundle) {
        Log.e("PaytmPayment", "Response => " + bundle.toString());
        Log.e("PaytmPayment", "STATUS => " + bundle.getString("STATUS"));
        Log.e("PaytmPayment", "RESPMSG => " + bundle.getString("RESPMSG"));
        if (bundle.getString("STATUS").equalsIgnoreCase("TXN_SUCCESS")) {
            Toasty.success(AllPaymentActivity.this, "" + bundle.getString("RESPMSG"), Toasty.LENGTH_LONG).show();
            paymentId = bundle.getString("TXNID");
            Log.e("PaytmPayment", "paymentId --> " + paymentId);

            Purchasebook();
        } else {
            Toasty.info(AllPaymentActivity.this, "" + bundle.getString("RESPMSG"), Toasty.LENGTH_LONG).show();
        }
    }

    @Override
    public void networkNotAvailable() {
        Log.e("responce", "Network not available");
        Toasty.error(this, "Network error!!!", Toasty.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Log.e("Failed", "" + s);
        Toasty.error(this, s, Toasty.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        Log.e("ErrorOccured", "" + s);
        Toasty.error(this, s, Toasty.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Log.e("WebPageError", "" + s);
        Toasty.error(this, s, Toasty.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toasty.warning(this, "Transaction cancelled.", Toasty.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Log.e("cancel", "" + s);
        Toasty.warning(this, "Transaction cancelled. " + s + bundle.toString(), Toasty.LENGTH_LONG).show();
    }

    /*=================== PayTm END ===================*/

}