package com.kriztech.book.Model.PayUHashModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Result {

    @SerializedName("payment_hash")
    @Expose
    private String paymentHash;
    @SerializedName("get_merchant_ibibo_codes_hash")
    @Expose
    private String getMerchantIbiboCodesHash;
    @SerializedName("vas_for_mobile_sdk_hash")
    @Expose
    private String vasForMobileSdkHash;
    @SerializedName("payment_related_details_for_mobile_sdk_hash")
    @Expose
    private String paymentRelatedDetailsForMobileSdkHash;

    public String getPaymentHash() {
        return paymentHash;
    }

    public void setPaymentHash(String paymentHash) {
        this.paymentHash = paymentHash;
    }

    public String getGetMerchantIbiboCodesHash() {
        return getMerchantIbiboCodesHash;
    }

    public void setGetMerchantIbiboCodesHash(String getMerchantIbiboCodesHash) {
        this.getMerchantIbiboCodesHash = getMerchantIbiboCodesHash;
    }

    public String getVasForMobileSdkHash() {
        return vasForMobileSdkHash;
    }

    public void setVasForMobileSdkHash(String vasForMobileSdkHash) {
        this.vasForMobileSdkHash = vasForMobileSdkHash;
    }

    public String getPaymentRelatedDetailsForMobileSdkHash() {
        return paymentRelatedDetailsForMobileSdkHash;
    }

    public void setPaymentRelatedDetailsForMobileSdkHash(String paymentRelatedDetailsForMobileSdkHash) {
        this.paymentRelatedDetailsForMobileSdkHash = paymentRelatedDetailsForMobileSdkHash;
    }

}