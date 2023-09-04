package com.kriztech.book.Model.TransactionModel;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Result {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("book_id")
    @Expose
    private String bookId;
    @SerializedName("magazine_id")
    @Expose
    private String magazineId;
    @SerializedName("voucher_id")
    @Expose
    private String voucherId;
    @SerializedName("voucher_amount")
    @Expose
    private String voucherAmount;
    @SerializedName("book_chapter_id")
    @Expose
    private String bookChapterId;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("author_commission_amount")
    @Expose
    private String authorCommissionAmount;
    @SerializedName("amount")
    @Expose
    private String amount;
    @SerializedName("total_amount")
    @Expose
    private String totalAmount;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("transaction_id")
    @Expose
    private String transactionId;
    @SerializedName("transcation_amount")
    @Expose
    private String transcationAmount;
    @SerializedName("wallet_amount")
    @Expose
    private String walletAmount;
    @SerializedName("settle")
    @Expose
    private String settle;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("author_name")
    @Expose
    private String authorName;
    @SerializedName("transaction_date")
    @Expose
    private String transactionDate;
    @SerializedName("book_detail")
    @Expose
    private BookDetail bookDetail;
    @SerializedName("magazine_detail")
    @Expose
    private MagazineDetail magazineDetail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getMagazineId() {
        return magazineId;
    }

    public void setMagazineId(String magazineId) {
        this.magazineId = magazineId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherAmount() {
        return voucherAmount;
    }

    public void setVoucherAmount(String voucherAmount) {
        this.voucherAmount = voucherAmount;
    }

    public String getBookChapterId() {
        return bookChapterId;
    }

    public void setBookChapterId(String bookChapterId) {
        this.bookChapterId = bookChapterId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorCommissionAmount() {
        return authorCommissionAmount;
    }

    public void setAuthorCommissionAmount(String authorCommissionAmount) {
        this.authorCommissionAmount = authorCommissionAmount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTranscationAmount() {
        return transcationAmount;
    }

    public void setTranscationAmount(String transcationAmount) {
        this.transcationAmount = transcationAmount;
    }

    public String getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(String walletAmount) {
        this.walletAmount = walletAmount;
    }

    public String getSettle() {
        return settle;
    }

    public void setSettle(String settle) {
        this.settle = settle;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BookDetail getBookDetail() {
        return bookDetail;
    }

    public void setBookDetail(BookDetail bookDetail) {
        this.bookDetail = bookDetail;
    }

    public MagazineDetail getMagazineDetail() {
        return magazineDetail;
    }

    public void setMagazineDetail(MagazineDetail magazineDetail) {
        this.magazineDetail = magazineDetail;
    }

}