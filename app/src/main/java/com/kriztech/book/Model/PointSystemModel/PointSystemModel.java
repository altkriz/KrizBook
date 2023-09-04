package com.kriztech.book.Model.PointSystemModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class PointSystemModel {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("daily_login")
    @Expose
    private List<DailyLogin> dailyLogin = null;
    @SerializedName("free_coin")
    @Expose
    private List<FreeCoin> freeCoin = null;
    @SerializedName("message")
    @Expose
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<DailyLogin> getDailyLogin() {
        return dailyLogin;
    }

    public void setDailyLogin(List<DailyLogin> dailyLogin) {
        this.dailyLogin = dailyLogin;
    }

    public List<FreeCoin> getFreeCoin() {
        return freeCoin;
    }

    public void setFreeCoin(List<FreeCoin> freeCoin) {
        this.freeCoin = freeCoin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}