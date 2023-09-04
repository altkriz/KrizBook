package com.kriztech.book.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class DownloadedItemModel implements Parcelable {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("is_paid")
    @Expose
    private String isPaid;
    @SerializedName("sample_url")
    @Expose
    private String sampleUrl;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("original_url")
    @Expose
    private String originalUrl;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("category_id")
    @Expose
    private String categoryId;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("readcnt")
    @Expose
    private String readcnt;
    @SerializedName("download")
    @Expose
    private String download;
    @SerializedName("is_feature")
    @Expose
    private String isFeature;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("category_name")
    @Expose
    private String categoryName;
    @SerializedName("category_image")
    @Expose
    private String categoryImage;
    @SerializedName("author_name")
    @Expose
    private String authorName;
    @SerializedName("author_image")
    @Expose
    private String authorImage;
    @SerializedName("is_download")
    @Expose
    private Integer isDownload;
    @SerializedName("avg_rating")
    @Expose
    private String avgRating;
    @SerializedName("transaction_date")
    @Expose
    private String transactionDate;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("downloadid")
    @Expose
    private long downloadid;
    @SerializedName("file_password")
    @Expose
    private String filePassword;
    @SerializedName("secretKey")
    @Expose
    private String secretKey;

    private int typeView = 1;

    public DownloadedItemModel(String userId, String id, String authorId, String title, String description,
                               String isPaid, String sampleUrl, String url, String originalUrl, String price, String categoryId, String image,
                               String readcnt, String download, String isFeature, String status, String createdAt, String updatedAt,
                               String categoryName, String categoryImage, String authorName, String authorImage, Integer isDownload,
                               String avgRating, String transactionDate, String type, long downloadid, String filePassword, String secretKey) {
        this.userId = userId;
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.isPaid = isPaid;
        this.sampleUrl = sampleUrl;
        this.url = url;
        this.originalUrl = originalUrl;
        this.price = price;
        this.categoryId = categoryId;
        this.image = image;
        this.readcnt = readcnt;
        this.download = download;
        this.isFeature = isFeature;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
        this.authorName = authorName;
        this.authorImage = authorImage;
        this.isDownload = isDownload;
        this.avgRating = avgRating;
        this.transactionDate = transactionDate;
        this.type = type;
        this.downloadid = downloadid;
        this.filePassword = filePassword;
        this.secretKey = secretKey;
    }

    public DownloadedItemModel() {
    }

    protected DownloadedItemModel(Parcel in) {
        if (in.readByte() == 0) {
            userId = null;
        } else {
            userId = in.readString();
        }
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readString();
        }
        authorId = in.readString();
        title = in.readString();
        description = in.readString();
        isPaid = in.readString();
        sampleUrl = in.readString();
        url = in.readString();
        price = in.readString();
        categoryId = in.readString();
        image = in.readString();
        readcnt = in.readString();
        download = in.readString();
        isFeature = in.readString();
        status = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        categoryName = in.readString();
        categoryImage = in.readString();
        authorName = in.readString();
        authorImage = in.readString();
        isDownload = in.readInt();
        avgRating = in.readString();
        transactionDate = in.readString();
        type = in.readString();
        downloadid = in.readLong();
        filePassword = in.readString();
        secretKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (userId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(userId);
        }
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(id);
        }
        dest.writeString(authorId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(isPaid);
        dest.writeString(sampleUrl);
        dest.writeString(url);
        dest.writeString(price);
        dest.writeString(categoryId);
        dest.writeString(image);
        dest.writeString(readcnt);
        dest.writeString(download);
        dest.writeString(isFeature);
        dest.writeString(status);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeString(categoryName);
        dest.writeString(categoryImage);
        dest.writeString(authorName);
        dest.writeString(authorImage);
        dest.writeInt(isDownload);
        dest.writeString(avgRating);
        dest.writeString(transactionDate);
        dest.writeString(type);
        dest.writeLong(downloadid);
        dest.writeString(filePassword);
        dest.writeString(secretKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadedItemModel> CREATOR = new Creator<DownloadedItemModel>() {
        @Override
        public DownloadedItemModel createFromParcel(Parcel in) {
            return new DownloadedItemModel(in);
        }

        @Override
        public DownloadedItemModel[] newArray(int size) {
            return new DownloadedItemModel[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public String getSampleUrl() {
        return sampleUrl;
    }

    public void setSampleUrl(String sampleUrl) {
        this.sampleUrl = sampleUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getReadcnt() {
        return readcnt;
    }

    public void setReadcnt(String readcnt) {
        this.readcnt = readcnt;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getIsFeature() {
        return isFeature;
    }

    public void setIsFeature(String isFeature) {
        this.isFeature = isFeature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
    }

    public Integer getIsDownload() {
        return isDownload;
    }

    public void setIsDownload(Integer isDownload) {
        this.isDownload = isDownload;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDownloadid() {
        return downloadid;
    }

    public void setDownloadid(long downloadid) {
        this.downloadid = downloadid;
    }

    public String getFilePassword() {
        return filePassword;
    }

    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getTypeView() {
        return typeView;
    }

    public DownloadedItemModel setTypeView(int typeView) {
        this.typeView = typeView;
        return this;
    }

}