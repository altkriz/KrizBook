package com.kriztech.book.Webservice;

import com.kriztech.book.Model.AuthorModel.AuthorModel;
import com.kriztech.book.Model.AuthorRegistrationModel.AuthorRegistrationModel;
import com.kriztech.book.Model.AuthorBankDetailModel.AuthorBankDetailModel;
import com.kriztech.book.Model.BannerModel.BannerModel;
import com.kriztech.book.Model.BookModel.BookModel;
import com.kriztech.book.Model.BookmarkModel.BookmarkModel;
import com.kriztech.book.Model.CategoryModel.CategoryModel;
import com.kriztech.book.Model.CommentModel.CommentModel;
import com.kriztech.book.Model.DownloadModel.DownloadModel;
import com.kriztech.book.Model.GeneralSettings.GeneralSettings;
import com.kriztech.book.Model.LoginRegister.LoginRegiModel;
import com.kriztech.book.Model.MagazineModel.MagazineModel;
import com.kriztech.book.Model.NotificationModel.NotificationModel;
import com.kriztech.book.Model.PackageModel.PackageModel;
import com.kriztech.book.Model.PayTmModel.PayTmModel;
import com.kriztech.book.Model.PayUHashModel.PayUHashModel;
import com.kriztech.book.Model.PaymentOptionModel.PaymentOptionModel;
import com.kriztech.book.Model.PointSystemModel.PointSystemModel;
import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.Model.ReadDowncntModel.ReadDowncntModel;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.Model.TransactionModel.TransactionModel;
import com.kriztech.book.Model.VoucherModel.VoucherModel;
import com.kriztech.book.Model.WalletHistoryModel.WalletHistoryModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AppAPI {

    @GET("general_setting")
    Call<GeneralSettings> general_settings();

    @FormUrlEncoded
    @POST("checkStatus")
    Call<SuccessModel> checkStatus(@Field("purchase_code") String purchase_code,
                                   @Field("package_name") String package_name);

    @FormUrlEncoded
    @POST("login")
    Call<LoginRegiModel> login(@Field("email") String email_id,
                               @Field("password") String password,
                               @Field("type") String type);

    /*Type for Login : 1-normal, 2-facebook, 3-mobile otp, 4-gmail*/
    @Multipart
    @POST("login")
    Call<LoginRegiModel> login(@Part("fullname") RequestBody fullname,
                               @Part("last_name") RequestBody last_name,
                               @Part("email") RequestBody email,
                               @Part("type") RequestBody type,
                               @Part("mobile_number") RequestBody mobile_number,
                               @Part("password") RequestBody password,
                               @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("loginwithotp")
    Call<LoginRegiModel> loginwithotp(@Field("mobile") String mobile);

    @FormUrlEncoded
    @POST("registration")
    Call<SuccessModel> Registration(@Field("fullname") String full_name,
                                    @Field("email") String email_id,
                                    @Field("password") String password,
                                    @Field("mobile") String phone);

    @FormUrlEncoded
    @POST("forgotpassword")
    Call<SuccessModel> forgotpassword(@Field("email") String email_id);

    @FormUrlEncoded
    @POST("get_notification")
    Call<NotificationModel> get_notification(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("read_notification")
    Call<SuccessModel> read_notification(@Field("user_id") String user_id,
                                         @Field("notification_id") String notification_id);

    @FormUrlEncoded
    @POST("profile")
    Call<ProfileModel> profile(@Field("user_id") String user_id);

    @Multipart
    @POST("update_profile")
    Call<SuccessModel> add_profile_img(@Part("user_id") RequestBody user_id,
                                       @Part("fullname") RequestBody fullname,
                                       @Part("email") RequestBody email,
                                       @Part("password") RequestBody password,
                                       @Part("mobile") RequestBody mobile,
                                       @Part MultipartBody.Part profile_img);

    @FormUrlEncoded
    @POST("update_profile")
    Call<SuccessModel> updateprofile(@Field("user_id") String user_id,
                                     @Field("fullname") String fullname,
                                     @Field("email") String email,
                                     @Field("password") String password,
                                     @Field("mobile") String mobile);

    @FormUrlEncoded
    @POST("update_profile")
    Call<SuccessModel> updateMissingData(@Field("user_id") String user_id,
                                         @Field("fullname") String fullname,
                                         @Field("email") String email,
                                         @Field("mobile") String mobile);

    @GET("get_ads_banner")
    Call<BannerModel> get_ads_banner();

    @FormUrlEncoded
    @POST("categorylist")
    Call<CategoryModel> categorylist(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("book_by_category")
    Call<BookModel> books_by_category(@Field("category_id") String cat_id,
                                      @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("newarriaval")
    Call<BookModel> newarriaval(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("alsolike")
    Call<BookModel> alsolike(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("popularbooklist")
    Call<BookModel> popularbooklist(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("bookdetails")
    Call<BookModel> bookdetails(@Field("book_id") String book_id,
                                @Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("booksearch")
    Call<BookModel> booksearch(@Field("name") String name,
                               @Field("page_no") String page_no);


    /* ============= Author Section START ============== */

    @FormUrlEncoded
    @POST("autherlist")
    Call<AuthorModel> autherlist(@Field("page_no") String page_no);

    @Multipart
    @POST("upload_book")
    Call<SuccessModel> upload_book(@Part("author_id") RequestBody author_id,
                                   @Part("title") RequestBody title,
                                   @Part("category_id") RequestBody category_id,
                                   @Part("description") RequestBody description,
                                   @Part("is_paid") RequestBody is_paid,
                                   @Part("price") RequestBody price,
                                   @Part MultipartBody.Part sample_url,
                                   @Part MultipartBody.Part full_book,
                                   @Part MultipartBody.Part image);

    @Multipart
    @POST("upload_magazine")
    Call<SuccessModel> upload_magazine(@Part("author_id") RequestBody author_id,
                                       @Part("title") RequestBody title,
                                       @Part("category_id") RequestBody category_id,
                                       @Part("description") RequestBody description,
                                       @Part("is_paid") RequestBody is_paid,
                                       @Part("price") RequestBody price,
                                       @Part MultipartBody.Part sample_url,
                                       @Part MultipartBody.Part full_magazine,
                                       @Part MultipartBody.Part image);

    /* Book Update with Documents & Cover Image */
    @Multipart
    @POST("update_book")
    Call<SuccessModel> update_book(@Part("book_id") RequestBody book_id,
                                   @Part("author_id") RequestBody author_id,
                                   @Part("title") RequestBody title,
                                   @Part("category_id") RequestBody category_id,
                                   @Part("description") RequestBody description,
                                   @Part("is_paid") RequestBody is_paid,
                                   @Part("price") RequestBody price,
                                   @Part MultipartBody.Part sample_url,
                                   @Part MultipartBody.Part full_book,
                                   @Part MultipartBody.Part image);

    /* Book Update */
    @FormUrlEncoded
    @POST("update_book")
    Call<SuccessModel> update_book(@Field("book_id") String book_id,
                                   @Field("author_id") String author_id,
                                   @Field("title") String title,
                                   @Field("category_id") String category_id,
                                   @Field("description") String description,
                                   @Field("is_paid") String is_paid,
                                   @Field("price") String price);

    /* Magazine Update with Documents & Cover Image */
    @Multipart
    @POST("update_magazine")
    Call<SuccessModel> update_magazine(@Part("magazine_id") RequestBody magazine_id,
                                       @Part("author_id") RequestBody author_id,
                                       @Part("title") RequestBody title,
                                       @Part("category_id") RequestBody category_id,
                                       @Part("description") RequestBody description,
                                       @Part("is_paid") RequestBody is_paid,
                                       @Part("price") RequestBody price,
                                       @Part MultipartBody.Part sample_url,
                                       @Part MultipartBody.Part full_magazine,
                                       @Part MultipartBody.Part image);

    /* Magazine Update */
    @FormUrlEncoded
    @POST("update_magazine")
    Call<SuccessModel> update_magazine(@Field("magazine_id") String magazine_id,
                                       @Field("author_id") String author_id,
                                       @Field("title") String title,
                                       @Field("category_id") String category_id,
                                       @Field("description") String description,
                                       @Field("is_paid") String is_paid,
                                       @Field("price") String price);

    @Multipart
    @POST("add_author")
    Call<AuthorRegistrationModel> add_author_with_img(@Part("user_id") RequestBody user_id,
                                                      @Part("email") RequestBody email,
                                                      @Part("address") RequestBody address,
                                                      @Part("password") RequestBody password,
                                                      @Part("name") RequestBody name,
                                                      @Part("bio") RequestBody bio,
                                                      @Part MultipartBody.Part image);

    @FormUrlEncoded
    @POST("add_author")
    Call<AuthorRegistrationModel> add_author(@Field("user_id") String user_id,
                                             @Field("email") String email,
                                             @Field("address") String address,
                                             @Field("password") String password,
                                             @Field("name") String name,
                                             @Field("bio") String bio);

    @FormUrlEncoded
    @POST("update_author")
    Call<SuccessModel> update_author(@Field("user_id") String user_id,
                                     @Field("author_id") String author_id,
                                     @Field("email") String email,
                                     @Field("address") String address,
                                     @Field("password") String password,
                                     @Field("name") String name,
                                     @Field("bio") String bio);

    @Multipart
    @POST("update_author")
    Call<SuccessModel> update_author_with_img(@Part("user_id") RequestBody user_id,
                                              @Part("author_id") RequestBody author_id,
                                              @Part("email") RequestBody email,
                                              @Part("address") RequestBody address,
                                              @Part("password") RequestBody password,
                                              @Part("name") RequestBody name,
                                              @Part("bio") RequestBody bio,
                                              @Part MultipartBody.Part image);

    @FormUrlEncoded
    @POST("get_author")
    Call<AuthorModel> get_author(@Field("author_id") String author_id);

    @FormUrlEncoded
    @POST("add_bank_detail")
    Call<AuthorBankDetailModel> add_bank_detail(@Field("author_id") String author_id,
                                                @Field("account_no") String account_no,
                                                @Field("bank_holder_name") String bank_holder_name,
                                                @Field("bank_name") String bank_name,
                                                @Field("ifsc_code") String ifsc_code);

    @FormUrlEncoded
    @POST("get_bank_detail")
    Call<AuthorBankDetailModel> get_bank_detail(@Field("author_id") String author_id);

    @FormUrlEncoded
    @POST("update_bank_detail")
    Call<SuccessModel> update_bank_detail(@Field("author_id") String author_id,
                                          @Field("id") String id,
                                          @Field("account_no") String account_no,
                                          @Field("bank_holder_name") String bank_holder_name,
                                          @Field("bank_name") String bank_name,
                                          @Field("ifsc_code") String ifsc_code);

    @FormUrlEncoded
    @POST("book_by_author")
    Call<BookModel> book_by_author(@Field("author_id") String author_id,
                                   @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("magazine_by_author")
    Call<BookModel> magazine_by_author(@Field("author_id") String author_id,
                                       @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("update_book_status")
    Call<SuccessModel> update_book_status(@Field("author_id") String author_id,
                                          @Field("book_id") String book_id,
                                          @Field("status") String status);

    @FormUrlEncoded
    @POST("update_magazine_status")
    Call<SuccessModel> update_magazine_status(@Field("author_id") String author_id,
                                              @Field("magazine_id") String magazine_id,
                                              @Field("status") String status);

    /* ============= Author Section END ============== */


    /* ============= Magazine START ============== */

    @FormUrlEncoded
    @POST("popular_magazine")
    Call<MagazineModel> popular_magazine(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("top_download_magazine")
    Call<MagazineModel> top_download_magazine(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("top_magazine")
    Call<MagazineModel> top_magazine(@Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("magazinesearch")
    Call<MagazineModel> magazinesearch(@Field("name") String name,
                                       @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("magazine_by_category")
    Call<MagazineModel> magazine_by_category(@Field("category_id") String category_id,
                                             @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("magazinedetails")
    Call<MagazineModel> magazinedetails(@Field("magazine_id") String magazine_id,
                                        @Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("add_comment")
    Call<SuccessModel> add_magazine_comment(@Field("magazine_id") String magazine_id,
                                            @Field("user_id") String user_id,
                                            @Field("comment") String comment);

    @FormUrlEncoded
    @POST("view_comment")
    Call<CommentModel> view_magazine_comment(@Field("magazine_id") String magazine_id,
                                             @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_bookmark")
    Call<SuccessModel> add_magazine_bookmark(@Field("user_id") String user_id,
                                             @Field("magazine_id") String magazine_id);

    @FormUrlEncoded
    @POST("checkbookmark")
    Call<SuccessModel> check_magazine_bookmark(@Field("user_id") String user_id,
                                               @Field("magazine_id") String magazine_id);

    @FormUrlEncoded
    @POST("add_magazine_rating")
    Call<SuccessModel> add_magazine_rating(@Field("user_id") String user_id,
                                           @Field("magazine_id") String magazine_id,
                                           @Field("rating") String rating);

    @FormUrlEncoded
    @POST("add_download")
    Call<SuccessModel> add_magazine_download(@Field("user_id") String user_id,
                                             @Field("magazine_id") String magazine_id);

    @FormUrlEncoded
    @POST("alldownload")
    Call<DownloadModel> alldownload(@Field("user_id") String user_id,
                                    @Field("type") String type,
                                    @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_transaction")
    Call<SuccessModel> add_magazine_purchase(@Field("magazine_id") String magazine_id,
                                             @Field("user_id") String user_id,
                                             @Field("amount") String amount,
                                             @Field("currency_code") String currency_code,
                                             @Field("description") String short_description,
                                             @Field("state") String state,
                                             @Field("author_id") String author_id,
                                             @Field("payment_id") String payment_id,
                                             @Field("type") String type,  // 1-payment getway,  2-wallet amount
                                             @Field("wallet_amount") String wallet_amount,
                                             @Field("transcation_amount") String transcation_amount);

    @FormUrlEncoded
    @POST("purchaselist")
    Call<DownloadModel> purchaseMagazineList(@Field("user_id") String user_id,
                                             @Field("type") String type,
                                             @Field("page_no") String page_no);

    /* ============= Magazine END ============== */

    @FormUrlEncoded
    @POST("add_transaction")
    Call<SuccessModel> add_chapter_transaction(@Field("author_id") String author_id,
                                               @Field("user_id") String user_id,
                                               @Field("amount") String amount,
                                               @Field("book_chapter_id") String book_chapter_id,
                                               @Field("book_id") String book_id);

    @FormUrlEncoded
    @POST("add_transaction")
    Call<SuccessModel> add_purchase(@Field("book_id") String book_id,
                                    @Field("user_id") String user_id,
                                    @Field("amount") String amount,
                                    @Field("currency_code") String currency_code,
                                    @Field("description") String short_description,
                                    @Field("state") String state,
                                    @Field("author_id") String author_id,
                                    @Field("payment_id") String payment_id,
                                    @Field("type") String type,  // 1-payment getway,  2-wallet amount
                                    @Field("wallet_amount") String wallet_amount,
                                    @Field("transcation_amount") String transcation_amount);

    @FormUrlEncoded
    @POST("purchaselist")
    Call<DownloadModel> purchaseBookList(@Field("user_id") String user_id,
                                         @Field("type") String type,
                                         @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("get_transaction")
    Call<TransactionModel> get_transaction(@Field("user_id") String user_id,
                                           @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("related_item")
    Call<BookModel> related_item(@Field("category_id") String fcat_id,
                                 @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_download")
    Call<SuccessModel> add_download(@Field("user_id") String user_id,
                                    @Field("book_id") String book_id);

    @FormUrlEncoded
    @POST("add_continue_read")
    Call<SuccessModel> add_continue_read(@Field("user_id") String user_id,
                                         @Field("book_id") String book_id);

    @FormUrlEncoded
    @POST("continue_read")
    Call<BookModel> continue_read(@Field("user_id") String user_id,
                                  @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_comment")
    Call<SuccessModel> add_comment(@Field("book_id") String book_id,
                                   @Field("user_id") String user_id,
                                   @Field("comment") String comment);

    @FormUrlEncoded
    @POST("view_comment")
    Call<CommentModel> view_comment(@Field("book_id") String book_id,
                                    @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_bookmark")
    Call<SuccessModel> add_bookmark(@Field("user_id") String user_id,
                                    @Field("book_id") String book_id);

    @FormUrlEncoded
    @POST("all_bookmark")
    Call<BookmarkModel> allBookmark(@Field("user_id") String user_id,
                                    @Field("type") String type,
                                    @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("checkbookmark")
    Call<SuccessModel> checkbookmark(@Field("user_id") String user_id,
                                     @Field("book_id") String book_id);

    @FormUrlEncoded
    @POST("add_rating")
    Call<SuccessModel> give_rating(@Field("user_id") String user_id,
                                   @Field("book_id") String book_id,
                                   @Field("rating") String rating);

    @FormUrlEncoded
    @POST("readcount_by_author")
    Call<ReadDowncntModel> readcnt_by_author(@Field("author_id") String a_id);

    @FormUrlEncoded
    @POST("free_paid_booklist")
    Call<BookModel> free_paid_booklist(@Field("is_paid") String is_paid,
                                       @Field("page_no") String page_no);

    @GET("get_package")
    Call<PackageModel> get_package();

    @FormUrlEncoded
    @POST("get_wallet_transaction")
    Call<WalletHistoryModel> get_wallet_transaction(@Field("user_id") String user_id,
                                                    @Field("page_no") String page_no);

    @FormUrlEncoded
    @POST("add_package_transaction")
    Call<SuccessModel> add_package_transaction(@Field("user_id") String user_id,
                                               @Field("amount") String amount,
                                               @Field("package_id") String package_id,
                                               @Field("payment_id") String payment_id,
                                               @Field("state") String state);

    @GET("get_paymentoption")
    Call<PaymentOptionModel> get_paymentoption();

    @FormUrlEncoded
    @POST("add_voucher")
    Call<SuccessModel> add_voucher(@Field("user_id") String user_id,
                                   @Field("title") String title,
                                   @Field("points") String points);

    @FormUrlEncoded
    @POST("list_voucher")
    Call<VoucherModel> list_voucher(@Field("user_id") String user_id);

    @GET("earn_point")
    Call<PointSystemModel> earn_point();

    @FormUrlEncoded
    @POST("getPaymentToken")
    Call<PayTmModel> getPaymentToken(
            @Field("MID") String mId,
            @Field("order_id") String orderId,
            @Field("CUST_ID") String custId,
            @Field("CHANNEL_ID") String channelId,
            @Field("TXN_AMOUNT") String txnAmount,
            @Field("WEBSITE") String website,
            @Field("CALLBACK_URL") String callbackUrl,
            @Field("INDUSTRY_TYPE_ID") String industryTypeId);

    @FormUrlEncoded
    @POST("get_hashes")
    Call<PayUHashModel> get_hashes(
            @Field("txnid") String txnid,
            @Field("amount") String amount,
            @Field("productinfo") String productinfo,
            @Field("firstname") String firstname,
            @Field("email") String email);
}
