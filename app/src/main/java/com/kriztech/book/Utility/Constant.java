package com.kriztech.book.Utility;

import com.kriztech.book.R;

public class Constant {

    /* App Base URL */
    public static String BASE_URL = "https://book.kriztech.in";


    /* OneSignal App ID */
    public static final String ONESIGNAL_APP_ID = "7d51c6e6-bc83-412d-8e55-3c8adceeac85";

    /* App Screen capture feature boolean */
    // After setting "setScreenCapture" boolean to "true", no one take screenshots and record video of this app
    // if set this boolean to "false", this feature will be disabled.
    public static boolean setScreenCapture = true;


    public static String strSearch = "", authorInfo = "";
    // set the profile image max size for now it is 1000 * 1000
    public static final int PROFILE_IMAGE_SIZE = 1000;
    //Select pic or not
    public static boolean isSelectPic = false;


    /* InApp Purchage */
    public static boolean InApp_isLive;
    public static String PRODUCT_ID = "android.test.purchased";

    /* Paypal Credintial */
    public static String CONFIG_ENVIRONMENT = "";
    public static String PAYPAL_CLIENT_ID = "AWUPvXSLwpbSPTkQ5S6WIjrOv33DUSwtXUbjCyZDnh7eGnGuYwp9ABL6GKAn7bG-0wTnUrMViQh7L5Q_";

    /* Test/Live FlutterWave */
    public static String FWPublic_Key = "";
    public static String FWEncryption_Key = "";
    public static boolean FW_isLive;

    /* Test/Live PayUMoney */
    public static String PayUMerchant_ID = "";
    public static String PayUMerchant_Key = "";
    public static String PayUMerchant_Salt = "";
    public static boolean PayU_isDebug;

    /* Test/Live PayTm */
    public static String PayTmMerchant_ID = "";
    public static String PayTmMerchant_Key = "";
    public static boolean PayTm_isLive;

    /* PayTm Parameters */
    public static final String CHANNEL_ID = "WAP";
    public static final String INDUSTRY_TYPE_ID = "Retail"; //Paytm industry type, get it from paytm credential
    public static final String LIVE_WEBSITE = "DEFAULT";
    public static final String TEST_WEBSITE = "WEBSTAGING";
    public static final String TEST_CALLBACK_URL = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=";
    public static final String LIVE_CALLBACK_URL = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=";


    /*Type for Login : 1-normal, 2-facebook, 3-mobile otp, 4-gmail*/
    public static final String typeNormal = "1";
    public static final String typeFacebook = "2";
    public static final String typeOTP = "3";
    public static final String typeGmail = "4";

    /*Payment OrderID pattern*/
    public static long fixFourDigit = 1317;
    public static long fixSixDigit = 161613;
    public static long orderIDSequence = 0L;

    public static int[] gradientBG = {R.drawable.gradient_package, R.drawable.gradient_cat_sky, R.drawable.gradient_cat_orange, R.drawable.gradient_cat_blue};

}