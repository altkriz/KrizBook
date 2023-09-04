package com.kriztech.book.Model.PaymentOptionModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class PaymentOptionModel {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("paytm")
    @Expose
    private Paytm paytm;
    @SerializedName("payumoney")
    @Expose
    private Payumoney payumoney;
    @SerializedName("flutterwave")
    @Expose
    private Flutterwave flutterwave;
    @SerializedName("razorpay")
    @Expose
    private Razorpay razorpay;
    @SerializedName("paypal")
    @Expose
    private Paypal paypal;
    @SerializedName("inapppurchage")
    @Expose
    private Inapppurchage inapppurchage;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Paytm getPaytm() {
        return paytm;
    }

    public void setPaytm(Paytm paytm) {
        this.paytm = paytm;
    }

    public Payumoney getPayumoney() {
        return payumoney;
    }

    public void setPayumoney(Payumoney payumoney) {
        this.payumoney = payumoney;
    }

    public Flutterwave getFlutterwave() {
        return flutterwave;
    }

    public void setFlutterwave(Flutterwave flutterwave) {
        this.flutterwave = flutterwave;
    }

    public Razorpay getRazorpay() {
        return razorpay;
    }

    public void setRazorpay(Razorpay razorpay) {
        this.razorpay = razorpay;
    }

    public Paypal getPaypal() {
        return paypal;
    }

    public void setPaypal(Paypal paypal) {
        this.paypal = paypal;
    }

    public Inapppurchage getInapppurchage() {
        return inapppurchage;
    }

    public void setInapppurchage(Inapppurchage inapppurchage) {
        this.inapppurchage = inapppurchage;
    }

}