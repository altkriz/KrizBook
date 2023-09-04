package com.kriztech.book.Model.AuthorModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Magazine {

    @SerializedName("read_count")
    @Expose
    private String readCount;
    @SerializedName("download")
    @Expose
    private String download;
    @SerializedName("total_magazine")
    @Expose
    private String totalMagazine;

    public String getReadCount() {
        return readCount;
    }

    public void setReadCount(String readCount) {
        this.readCount = readCount;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getTotalMagazine() {
        return totalMagazine;
    }

    public void setTotalMagazine(String totalMagazine) {
        this.totalMagazine = totalMagazine;
    }

}