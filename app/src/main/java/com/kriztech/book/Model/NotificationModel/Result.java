package com.kriztech.book.Model.NotificationModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Result {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("app_id")
    @Expose
    private String appId;
    @SerializedName("included_segments")
    @Expose
    private String includedSegments;
    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("headings")
    @Expose
    private String headings;
    @SerializedName("contents")
    @Expose
    private String contents;
    @SerializedName("big_picture")
    @Expose
    private String bigPicture;
    @SerializedName("created_at")
    @Expose
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIncludedSegments() {
        return includedSegments;
    }

    public void setIncludedSegments(String includedSegments) {
        this.includedSegments = includedSegments;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHeadings() {
        return headings;
    }

    public void setHeadings(String headings) {
        this.headings = headings;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getBigPicture() {
        return bigPicture;
    }

    public void setBigPicture(String bigPicture) {
        this.bigPicture = bigPicture;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}