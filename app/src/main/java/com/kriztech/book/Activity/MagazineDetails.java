package com.kriztech.book.Activity;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.MagazineCommentAdapter;
import com.kriztech.book.Adapter.MagazineRelatedAdapter;
import com.kriztech.book.Model.CommentModel.CommentModel;
import com.kriztech.book.Model.DownloadedItemModel;
import com.kriztech.book.Model.MagazineModel.MagazineModel;
import com.kriztech.book.Model.MagazineModel.Result;
import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.DownloadEpub;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.LocaleUtils;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.orhanobut.hawk.Hawk;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MagazineDetails extends AppCompatActivity implements View.OnClickListener, Paginate.Callbacks {

    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;
    private static final String TAG = MagazineDetails.class.getSimpleName();

    private ShimmerFrameLayout shimmer;
    private RecyclerView rv_comment, rv_related;
    private TextView txt_title, txt_price, txtSellCount, txt_by_author, txt_category, txt_descripation, txt_read, txtBuyNow, txt_bookmark, txt_drop_down, txt_drop_up, txt_show_all,
            txt_show_info, txt_hide_info, txt_publish_at, txt_author_name;
    private ImageView ivThumb;
    private LinearLayout ly_showAll_desc, ly_back, ly_share, ly_comment_viewAll, ly_add_comment, ly_copyright_info, ly_author_publish, lyDownload, lySellCount, lyReadSampleMagazine;
    private RelativeLayout rl_buy_read;
    private SimpleRatingBar ratingbar;

    private MagazineCommentAdapter magazineCommentAdapter;
    private MagazineRelatedAdapter magazineRelatedAdapter;

    private List<Result> magazineList;
    private List<com.kriztech.book.Model.CommentModel.Result> commentList;
    private List<Result> relatedList;

    private TemplateView nativeTemplate = null;
    private NativeBannerAd fbNativeBannerAd = null;
    private NativeAdLayout fbNativeTemplate = null;
    private RewardedVideoAd fbRewardedVideoAd = null;
    private RewardedAd mRewardedAd = null;
    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;

    private String docID = "", authorID = "", docCatID = "", adTYPE = "", walletBalance = "", fileImage = "", getPermissionFor = "", mainKeyAlias = "";
    private boolean loading = false;
    private int page = 1, totalPages = 1, fileLength;
    private Paginate paginate;
    private File documentFile = null, imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(MagazineDetails.this);
        setContentView(R.layout.activity_magazinedetails);
        PrefManager.forceRTLIfSupported(getWindow(), MagazineDetails.this);
        takePermissionUtils = new PermissionUtils(MagazineDetails.this, mPermissionResult);

        init();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            docID = bundle.getString("docID");
            authorID = bundle.getString("authorID");
            Log.e("docID", "=> " + docID);
            Log.e("authorID", "=> " + authorID);
        }

        //Advertisement
        Log.e("native_ad", "" + prefManager.getValue("native_ad"));
        if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            nativeTemplate.setVisibility(View.VISIBLE);
            Utils.NativeAds(MagazineDetails.this, nativeTemplate, "" + prefManager.getValue("native_adid"));
        } else {
            nativeTemplate.setVisibility(View.GONE);
        }

        Log.e("fb_native_status", "" + prefManager.getValue("fb_native_status"));
        if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            fbNativeTemplate.setVisibility(View.VISIBLE);
            Utils.FacebookNativeAdSmall(MagazineDetails.this, fbNativeBannerAd, fbNativeTemplate, "" + prefManager.getValue("fb_native_id"));
        } else {
            fbNativeTemplate.setVisibility(View.GONE);
        }

    }

    private void init() {
        try {
            prefManager = new PrefManager(MagazineDetails.this);

            shimmer = findViewById(R.id.shimmer);
            ratingbar = findViewById(R.id.ratingbar);

            nativeTemplate = findViewById(R.id.nativeTemplate);
            fbNativeTemplate = findViewById(R.id.fbNativeTemplate);
            txt_title = findViewById(R.id.txt_title);
            txt_price = findViewById(R.id.txt_price);
            txtSellCount = findViewById(R.id.txtSellCount);
            txt_by_author = findViewById(R.id.txt_by_author);
            txt_category = findViewById(R.id.txt_category);
            txt_descripation = findViewById(R.id.txt_descripation);
            ivThumb = findViewById(R.id.ivThumb);
            txt_bookmark = findViewById(R.id.txt_bookmark);

            rl_buy_read = findViewById(R.id.rl_buy_read);
            lyDownload = findViewById(R.id.lyDownload);
            lySellCount = findViewById(R.id.lySellCount);
            lyReadSampleMagazine = findViewById(R.id.lyReadSampleMagazine);
            ly_add_comment = findViewById(R.id.ly_add_comment);
            ly_comment_viewAll = findViewById(R.id.ly_comment_viewAll);
            ly_copyright_info = findViewById(R.id.ly_copyright_info);
            ly_author_publish = findViewById(R.id.ly_author_publish);
            ly_showAll_desc = findViewById(R.id.ly_showAll_desc);
            txt_drop_down = findViewById(R.id.txt_drop_down);
            txt_drop_up = findViewById(R.id.txt_drop_up);
            txt_show_all = findViewById(R.id.txt_show_all);
            txt_show_info = findViewById(R.id.txt_show_info);
            txt_hide_info = findViewById(R.id.txt_hide_info);
            txt_author_name = findViewById(R.id.txt_author_name);
            txt_publish_at = findViewById(R.id.txt_publish_at);

            rv_comment = findViewById(R.id.rv_comment);
            rv_related = findViewById(R.id.rv_related);
            txt_read = findViewById(R.id.txt_read);
            txtBuyNow = findViewById(R.id.txtBuyNow);
            ly_back = findViewById(R.id.ly_back);
            ly_share = findViewById(R.id.ly_share);

            ly_back.setOnClickListener(this);
            ly_share.setOnClickListener(this);
            txt_bookmark.setOnClickListener(this);
            txt_by_author.setOnClickListener(this);
            txt_read.setOnClickListener(this);
            txtBuyNow.setOnClickListener(this);
            ly_showAll_desc.setOnClickListener(this);
            ly_comment_viewAll.setOnClickListener(this);
            ly_copyright_info.setOnClickListener(this);
            ly_add_comment.setOnClickListener(this);
            lyDownload.setOnClickListener(this);
            lyReadSampleMagazine.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception =>", "" + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdInit();
    }

    private void AdInit() {
        Log.e("reward_ad", "" + prefManager.getValue("reward_ad"));
        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            mRewardedAd = null;
            RewardedVideoAd();
        }

        Log.e("fb_rewardvideo_status", "" + prefManager.getValue("fb_rewardvideo_status"));
        if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            fbRewardedVideoAd = null;
            FacebookRewardAd();
        }

        Log.e("interstital_ad", "" + prefManager.getValue("interstital_ad"));
        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
            mInterstitialAd = null;
            InterstitialAd();
        }

        Log.e("fb_interstiatial_status", "" + prefManager.getValue("fb_interstiatial_status"));
        if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            fbInterstitialAd = null;
            FacebookInterstitialAd();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_back:
                finish();
                break;

            case R.id.lyDownload:
            case R.id.txtBuyNow:
                if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                    if (Utils.checkLoginUser(MagazineDetails.this)) {
                        getPermissionFor = "Download";
                        if (takePermissionUtils.isStoragePermissionGranted()) {
                            ShowAdByClick("DOWNLOAD");
                        } else {
                            takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
                        }
                    }
                } else {
                    Toasty.warning(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
                }
                break;

            case R.id.ly_share:
                if (Functions.isConnectedToInternet(MagazineDetails.this)) {
//                    getPermissionFor = "Share";
//                    if (takePermissionUtils.isStoragePermissionGranted()) {
                    ShowAdByClick("SHARE");
//                    } else {
//                        takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
//                    }
                } else {
                    Toasty.warning(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
                }
                break;

            case R.id.txt_read:
                if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                    if (Utils.checkLoginUser(MagazineDetails.this)) {
                        getPermissionFor = "Read";
                        if (takePermissionUtils.isStoragePermissionGranted()) {
                            ShowAdByClick("READ");
                        } else {
                            takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
                        }
                    }
                } else {
                    Toasty.warning(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
                }
                break;

            case R.id.lyReadSampleMagazine:
                if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                    getPermissionFor = "SampleRead";
                    if (takePermissionUtils.isStoragePermissionGranted()) {
                        ShowAdByClick("SAMPLEREAD");
                    } else {
                        takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
                    }
                } else {
                    Toasty.warning(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
                }
                break;

            case R.id.txt_bookmark:
                if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                    if (Utils.checkLoginUser(MagazineDetails.this)) {
                        AddBookMark();
                    }
                } else {
                    Toasty.warning(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
                }
                break;

            case R.id.txt_by_author:
                Intent intent = new Intent(MagazineDetails.this, AuthorPortfolio.class);
                intent.putExtra("authorID", "" + magazineList.get(0).getAuthorId());
                startActivity(intent);
                break;

            case R.id.ly_showAll_desc:
                if (txt_show_all.getText().toString().equalsIgnoreCase("" + getResources().getString(R.string.show_all))) {
                    ObjectAnimator animation = ObjectAnimator.ofInt(txt_descripation, "maxLines", txt_descripation.getLineCount());
                    animation.setDuration(200).start();
                    txt_show_all.setText("" + getResources().getString(R.string.pack_up));
                    txt_drop_down.setVisibility(View.GONE);
                    txt_drop_up.setVisibility(View.VISIBLE);
                } else {
                    ObjectAnimator animation = ObjectAnimator.ofInt(txt_descripation, "maxLines", 3);
                    animation.setDuration(200).start();
                    txt_show_all.setText("" + getResources().getString(R.string.show_all));
                    txt_drop_down.setVisibility(View.VISIBLE);
                    txt_drop_up.setVisibility(View.GONE);
                }
                break;

            case R.id.ly_comment_viewAll:
                Intent cIntent = new Intent(MagazineDetails.this, CommentViewAll.class);
                cIntent.putExtra("docID", "" + docID);
                cIntent.putExtra("docType", "Magazine");
                startActivity(cIntent);
                break;

            case R.id.ly_copyright_info:
                if (txt_show_info.getVisibility() == View.VISIBLE) {
                    txt_hide_info.setVisibility(View.VISIBLE);
                    txt_show_info.setVisibility(View.GONE);
                    ly_author_publish.setVisibility(View.VISIBLE);
                } else {
                    txt_hide_info.setVisibility(View.GONE);
                    txt_show_info.setVisibility(View.VISIBLE);
                    ly_author_publish.setVisibility(View.GONE);
                }
                break;

            case R.id.ly_add_comment:
                if (Utils.checkLoginUser(MagazineDetails.this)) {
                    commentDialog();
                }
                break;
        }
    }

    private final ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(MagazineDetails.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(MagazineDetails.this, getString(R.string.we_need_storage_permission_for_save_video));
                    } else if (allPermissionClear) {
                        Log.e("mPermissionResult", "isPermissionGranted => " + takePermissionUtils.isStoragePermissionGranted());
                        if (getPermissionFor.equalsIgnoreCase("Download")) {
                            ShowAdByClick("DOWNLOAD");
                        } else if (getPermissionFor.equalsIgnoreCase("Share")) {
                            ShowAdByClick("SHARE");
                        } else if (getPermissionFor.equalsIgnoreCase("Read")) {
                            ShowAdByClick("READ");
                        } else if (getPermissionFor.equalsIgnoreCase("SampleRead")) {
                            ShowAdByClick("SAMPLEREAD");
                        }
                    }
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        MagazineDetails();
        GetProfile();
    }

    private void GetProfile() {
        Call<ProfileModel> call = BaseURL.getVideoAPI().profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        walletBalance = response.body().getResult().get(0).getCoinBalance();
                    }
                } catch (Exception e) {
                    Log.e("profile", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("docID", "" + docID);
        Log.e("Save-docID", "" + docID);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        docID = savedInstanceState.getString("docID");
        Log.e("Restore-docID", "" + docID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /* magazinedetails API */
    private void MagazineDetails() {
        Utils.shimmerShow(shimmer);

        Call<MagazineModel> call = BaseURL.getVideoAPI().magazinedetails("" + docID, "" + prefManager.getLoginId());
        call.enqueue(new Callback<MagazineModel>() {
            @Override
            public void onResponse(Call<MagazineModel> call, Response<MagazineModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {

                            magazineList = new ArrayList<Result>();
                            magazineList = response.body().getResult();
                            docCatID = response.body().getResult().get(0).getCategoryId();
                            Log.e("docCatID", "=>>> " + docCatID);

                            txt_title.setText("" + magazineList.get(0).getTitle());

                            if (magazineList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                txt_price.setText(" |  " + prefManager.getValue("currency_symbol") + " " + magazineList.get(0).getPrice());
                                lySellCount.setVisibility(View.VISIBLE);
                                txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + magazineList.get(0).getTotalSell())));
                            } else {
                                txt_price.setText(" |  " + getResources().getString(R.string.free));
                                lySellCount.setVisibility(View.GONE);
                            }

                            txt_by_author.setText(getResources().getString(R.string.by) + " " + magazineList.get(0).getAuthorName());
                            txt_category.setText("" + magazineList.get(0).getCategoryName());
                            txt_descripation.setText("" + Html.fromHtml(magazineList.get(0).getDescription()));
                            txt_author_name.setText("" + magazineList.get(0).getAuthorName());
                            txt_publish_at.setText(Utils.DateFormat2(magazineList.get(0).getUpdatedAt()));

                            Log.e("Avg_Ratings =>", "" + magazineList.get(0).getAvgRating());
                            ratingbar.setRating(Float.parseFloat(magazineList.get(0).getAvgRating()));

                            Log.e("IsBuy =>", "" + magazineList.get(0).getIsBuy());
                            Log.e("IsPaid =>", "" + magazineList.get(0).getIsPaid());
                            Log.e("Doc", "AuthorId => " + magazineList.get(0).getAuthorId());
                            Log.e("AuthorId =>", "" + prefManager.getAuthorId());

                            if (!magazineList.get(0).getAuthorId().equalsIgnoreCase("" + prefManager.getAuthorId())) {
                                if (magazineList.get(0).getIsBuy() == 0) {
                                    if (magazineList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                        txtBuyNow.setVisibility(View.VISIBLE);
                                        txt_read.setVisibility(View.GONE);
                                        lyDownload.setVisibility(View.INVISIBLE);
                                        txtBuyNow.setText("" + getResources().getString(R.string.buy_now));
                                    } else {
                                        txt_read.setVisibility(View.VISIBLE);
                                        txtBuyNow.setVisibility(View.GONE);
                                        txt_read.setText("" + getResources().getString(R.string.read_now));
                                        lyDownload.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    lyDownload.setVisibility(View.VISIBLE);
                                    txt_read.setVisibility(View.VISIBLE);
                                    txtBuyNow.setVisibility(View.GONE);
                                    txt_read.setText("" + getResources().getString(R.string.read_now));
                                }
                            } else {
                                lyDownload.setVisibility(View.VISIBLE);
                                txt_read.setVisibility(View.VISIBLE);
                                txtBuyNow.setVisibility(View.GONE);
                                txt_read.setText("" + getResources().getString(R.string.read_now));
                            }

                            if (!TextUtils.isEmpty(magazineList.get(0).getImage()))
                                Picasso.get().load(magazineList.get(0).getImage()).placeholder(R.drawable.no_image_potr).into(ivThumb);
                            else
                                Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivThumb);

                            relatedList = new ArrayList<>();
                            setupPagination();
                            RelatedItem(page);
                            Comments();
                            CheckBookMark();
                        } else {
                            Utils.shimmerHide(shimmer);
                        }
                    } else {
                        Utils.shimmerHide(shimmer);
                    }
                } catch (Exception e) {
                    Log.e("magazineDetails", "Exception => " + e);
                    Utils.shimmerHide(shimmer);
                }
            }

            @Override
            public void onFailure(Call<MagazineModel> call, Throwable t) {
                Log.e("magazineDetails", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* magazine_by_category API */
    private void RelatedItem(int pageNo) {
        Call<MagazineModel> call = BaseURL.getVideoAPI().magazine_by_category(docCatID, "" + pageNo);
        call.enqueue(new Callback<MagazineModel>() {
            @Override
            public void onResponse(Call<MagazineModel> call, Response<MagazineModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            relatedList = response.body().getResult();
                            Log.e("relatedList", "" + relatedList.size());

                            rv_related.setVisibility(View.VISIBLE);
                            loading = false;
                            magazineRelatedAdapter.addMagazine(relatedList);
                        } else {
                            rv_related.setVisibility(View.GONE);
                            loading = false;
                        }

                    } else {
                        rv_related.setVisibility(View.GONE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("magazine_by_category", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<MagazineModel> call, Throwable t) {
                Log.e("magazine_by_category", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    rv_related.setVisibility(View.GONE);
                }
                loading = false;
            }
        });
    }

    /* view_comment API */
    private void Comments() {
        Call<CommentModel> call = BaseURL.getVideoAPI().view_magazine_comment("" + docID, "1");
        call.enqueue(new Callback<CommentModel>() {
            @Override
            public void onResponse(Call<CommentModel> call, Response<CommentModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            commentList = new ArrayList<>();
                            commentList = response.body().getResult();
                            Log.e("CommentList", "" + commentList.size());

                            if (commentList.size() > 5) {
                                magazineCommentAdapter = new MagazineCommentAdapter(MagazineDetails.this, commentList, "Max_5");
                            } else {
                                magazineCommentAdapter = new MagazineCommentAdapter(MagazineDetails.this, commentList, "");
                            }
                            rv_comment.setLayoutManager(new GridLayoutManager(MagazineDetails.this, 1));
                            rv_comment.setAdapter(magazineCommentAdapter);
                            magazineCommentAdapter.notifyDataSetChanged();

                            rv_comment.setVisibility(View.VISIBLE);
                        } else {
                            rv_comment.setVisibility(View.GONE);
                        }

                    } else {
                        rv_comment.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("view_magazine_comment", "Exception => " + e);
                    rv_comment.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<CommentModel> call, Throwable t) {
                Log.e("view_magazine_comment", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                Utils.ProgressbarHide();
                rv_comment.setVisibility(View.GONE);
            }
        });
    }

    /* add_bookmark API */
    private void AddBookMark() {
        Utils.ProgressBarShow(MagazineDetails.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().add_magazine_bookmark("" + prefManager.getLoginId(), "" + docID);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("AddBookmark", "" + response.body().getMessage());
                        txt_bookmark.setText("" + getResources().getString(R.string.remove_from_library));
                    } else {
                        txt_bookmark.setText("" + getResources().getString(R.string.add_to_library));
                    }
                    Toasty.success(MagazineDetails.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("add_magazine_bookmark", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_magazine_bookmark", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    /* checkbookmark API */
    private void CheckBookMark() {
        Call<SuccessModel> call = BaseURL.getVideoAPI().check_magazine_bookmark("" + prefManager.getLoginId(), "" + docID);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("check_magazine_bookmark", "" + response.body().getMessage());
                        txt_bookmark.setText("" + getResources().getString(R.string.remove_from_library));
                    } else {
                        txt_bookmark.setText("" + getResources().getString(R.string.add_to_library));
                    }
                } catch (Exception e) {
                    Log.e("check_magazine_bookmark", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("check_magazine_bookmark", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                Utils.ProgressbarHide();
            }
        });
    }

    private void commentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MagazineDetails.this, R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.comment_dialog);
        PrefManager.forceRTLIfSupported(getWindow(), MagazineDetails.this);
        View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        final SimpleRatingBar simple_rating_bar = bottomSheetDialog.findViewById(R.id.simple_rating_bar);
        final LinearLayout ly_close_dialog = bottomSheetDialog.findViewById(R.id.ly_close_dialog);
        final LinearLayout ly_submit = bottomSheetDialog.findViewById(R.id.ly_submit);
        final EditText edt_comment = bottomSheetDialog.findViewById(R.id.edt_comment);

        ly_close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.setDismissWithAnimation(true);
                    bottomSheetDialog.dismiss();
                }
            }
        });

        ly_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edt_comment.getText().toString()) && simple_rating_bar.getRating() == 0.0) {
                    Toasty.warning(MagazineDetails.this, "" + getResources().getString(R.string.please_give_feedback_to_author), Toasty.LENGTH_SHORT).show();
                    return;
                }

                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.setDismissWithAnimation(true);
                    bottomSheetDialog.dismiss();
                }

                if (simple_rating_bar.getRating() != 0.0) {
                    Log.e("rating ==>", "" + simple_rating_bar.getRating());
                    Addrating(simple_rating_bar.getRating());
                }
                if (!TextUtils.isEmpty(edt_comment.getText().toString())) {
                    AddComments(edt_comment.getText().toString());
                }
            }
        });
    }

    /* add_comment API */
    private void AddComments(String comment) {
        Utils.ProgressBarShow(MagazineDetails.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().add_magazine_comment("" + docID, "" + prefManager.getLoginId(), "" + comment);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("Add Comments", "" + response.body().getMessage());
                    Toasty.success(MagazineDetails.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    Comments();
                } catch (Exception e) {
                    Log.e("add_magazine_comment", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_magazine_comment", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* add_magazine_rating API */
    private void Addrating(Float aFloat) {
        Utils.ProgressBarShow(MagazineDetails.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().add_magazine_rating("" + prefManager.getLoginId(), "" + docID, "" + aFloat);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("AddRating", "" + response);
                        Toasty.success(MagazineDetails.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    } else {
                        Toasty.info(MagazineDetails.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("add_magazine_rating", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_magazine_rating", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.shimmerHide(shimmer);
            }
        });
    }

    private void ReadMagazine() {
        try {
            if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                Log.e("url", "==>>> " + magazineList.get(0).getUrl());
                Log.e("url", "epub ? ==>>> " + magazineList.get(0).getUrl().contains(".EPUB"));

                if (magazineList.get(0).getUrl().contains(".epub") || magazineList.get(0).getUrl().contains(".EPUB")) {
                    if (Utils.checkLoginUser(MagazineDetails.this)) {
                        DownloadEpub downloadEpub = new DownloadEpub(MagazineDetails.this);
                        downloadEpub.pathEpub(new ArrayList<>(), magazineList, "magazine");
                    }

                } else if (magazineList.get(0).getUrl().contains(".pdf") || magazineList.get(0).getUrl().contains(".PDF")) {
                    if (Utils.checkLoginUser(MagazineDetails.this)) {
                        startActivity(new Intent(MagazineDetails.this, PDFShow.class)
                                .putExtra("link", magazineList.get(0).getUrl())
                                .putExtra("toolbarTitle", magazineList.get(0).getTitle())
                                .putExtra("type", "link"));
                    }
                }
            } else {
                Toasty.info(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Exception-Read", "" + e.getMessage());
        }
    }

    private void ReadSampleMagazine() {
        try {
            if (Functions.isConnectedToInternet(MagazineDetails.this)) {
                Log.e("sample url", "==>> " + magazineList.get(0).getSampleUrl());
                Log.e("sample url", "epub ? ==>> " + magazineList.get(0).getSampleUrl().contains(".EPUB"));

                if (magazineList.get(0).getSampleUrl().contains(".epub") || magazineList.get(0).getSampleUrl().contains(".EPUB")) {
                    DownloadEpub downloadEpub = new DownloadEpub(MagazineDetails.this);
                    downloadEpub.pathEpub(new ArrayList<>(), magazineList, "samplemagazine");

                } else if (magazineList.get(0).getSampleUrl().contains(".pdf") || magazineList.get(0).getSampleUrl().contains(".PDF")) {
                    startActivity(new Intent(MagazineDetails.this, PDFShow.class)
                            .putExtra("link", magazineList.get(0).getSampleUrl())
                            .putExtra("toolbarTitle", magazineList.get(0).getTitle())
                            .putExtra("type", "link"));
                }

            } else {
                Toasty.error(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ReadSampleBook", "Exception => " + e.getMessage());
        }
    }

    /*========= Download START =========*/
    private void CheckMagazineType() {
        if (Functions.isConnectedToInternet(MagazineDetails.this)) {
            if (Utils.checkLoginUser(MagazineDetails.this)) {

                Log.e("CheckMagazineType", "AuthorID ==>>> " + magazineList.get(0).getAuthorId());
                Log.e("CheckMagazineType", "Saved AuthorID ==>>> " + prefManager.getAuthorId());
                if (magazineList.get(0).getAuthorId().equalsIgnoreCase("" + prefManager.getAuthorId())) {
                    if (magazineList.get(0).getUrl().contains(".epub") || magazineList.get(0).getUrl().contains(".pdf"))
                        checkAndDownload("" + magazineList.get(0).getUrl());

                } else {
                    Log.e("CheckMagazineType", "IsPaid ==>>> " + magazineList.get(0).getIsPaid());
                    Log.e("CheckMagazineType", "IsBuy ==>>> " + magazineList.get(0).getIsBuy());
                    if (magazineList.get(0).getIsPaid().equalsIgnoreCase("1") && magazineList.get(0).getIsBuy() == 0) {

                        Log.e("CheckMagazineType", "userType ==>>> " + prefManager.getValue("userType"));
                        if (Utils.checkMissingData(MagazineDetails.this, "" + prefManager.getValue("userType"))) {
                            Intent intent = new Intent(MagazineDetails.this, AllPaymentActivity.class);
                            intent.putExtra("TYPE", "Magazine");
                            intent.putExtra("paymentType", "1");
                            intent.putExtra("price", "" + magazineList.get(0).getPrice());
                            intent.putExtra("itemId", "" + magazineList.get(0).getId());
                            intent.putExtra("title", "" + magazineList.get(0).getTitle());
                            intent.putExtra("desc", "" + magazineList.get(0).getCategoryName());
                            intent.putExtra("date", "" + magazineList.get(0).getCreatedAt());
                            intent.putExtra("author", "" + magazineList.get(0).getAuthorId());
                            intent.putExtra("walletBalance", "" + walletBalance);
                            startActivity(intent);
                        } else {
                            Utils.getMissingDataFromUser(MagazineDetails.this, "" + prefManager.getValue("userType"));
                        }

                    } else {
                        if (magazineList.get(0).getUrl().contains(".epub") || magazineList.get(0).getUrl().contains(".pdf")) {
                            checkAndDownload("" + magazineList.get(0).getUrl());
                        }
                    }
                }
            }
        } else {
            Toasty.info(MagazineDetails.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void checkAndDownload(String magazineURL) {
        try {
            if (magazineURL != null) {
                Log.e("=> magazineURL", "" + magazineURL);
                String saveMagazineName;
                if (magazineURL.contains(".pdf")) {
                    saveMagazineName = "fullmagazine_" + magazineList.get(0).getTitle().replaceAll("[, ;]", "").toLowerCase() + prefManager.getLoginId() + ".pdf";
                } else {
                    saveMagazineName = "fullmagazine_" + magazineList.get(0).getTitle().replaceAll("[, ;]", "").toLowerCase() + prefManager.getLoginId() + ".epub";
                }
                Log.e("checkAndDownload", "saveMagazineName => " + saveMagazineName);

                String downloadDirectory;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    downloadDirectory = Functions.getAppFolder(MagazineDetails.this) + getResources().getString(R.string.magazines) + "/";
                } else {
                    downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + getResources().getString(R.string.magazines) + "/";
                }
                Log.e("checkAndDownload", "downloadDirectory => " + downloadDirectory);

                File file = new File(downloadDirectory);
                if (!file.exists()) {
                    Log.e("checkAndDownload", "Document directory created again");
                    file.mkdirs();
                }

                File checkFile;
                checkFile = new File(file, saveMagazineName);
                Log.e("checkAndDownload", "checkFile => " + checkFile);

                if (!checkFile.exists()) {

                    /* Security Key */
                    mainKeyAlias = Utils.getRandomString();
                    Log.e("==>> mainKeyAlias", "" + mainKeyAlias);

                    documentFile = checkFile;
                    Log.e("checkAndDownload", "checkFile => " + checkFile);
                    new DownloadAndSave().execute(magazineURL);

                } else {
                    Toasty.info(MagazineDetails.this, "" + getResources().getString(R.string.already_download), Toasty.LENGTH_SHORT).show();
                }

            }
        } catch (Exception e) {
            Log.e("checkAndDownload", "Exception => " + e);
            e.printStackTrace();
        }
    }

    public class DownloadAndSave extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            Functions.showDeterminentLoader(MagazineDetails.this, false, false);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            HttpURLConnection connection = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                Log.e(TAG, "mainKeyAlias ===>>> " + mainKeyAlias);
                Log.e(TAG, "documentFile ===>>> " + documentFile);
                Log.e(TAG, "sUrl ===>>> " + sUrl[0]);

                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setChunkedStreamingMode(0);
                connection.setDoInput(true);
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }
                Log.i(TAG, "Response " + connection.getResponseCode());

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                fileLength = connection.getContentLength();
                Log.e(TAG, "fileLength ===>>> " + fileLength);

                // download the file
                input = connection.getInputStream();
                fos = new FileOutputStream(documentFile);
                bos = new BufferedOutputStream(fos);

                byte data[] = new byte[1024];
                byte[] filesBytes = LocaleUtils.encodeFile(mainKeyAlias.getBytes(), IOUtils.toByteArray(input));
                long downloaded = 0;
                int count;

//                while ((count = input.read(filesBytes)) != -1) {
//                    if (isCancelled()) {
//                        input.close();
//                        return null;
//                    }
//                    downloaded += count;
//                    // publishing the progress....
//                    if (fileLength > 0) {
//                        // only if total length is known
//                        // An Android service handler is a handler running on a specific background thread.
//                        publishProgress((int) (downloaded * 100 / fileLength));
//                    }
                bos.write(filesBytes);
//                }

            } catch (Exception e) {
                Log.e(TAG, "Exception ===>>>> " + e);
                return e.toString();
            } finally {
                try {
                    Log.e(TAG, "finally documentFile ===>>>> " + documentFile);
                    Log.e(TAG, "documentFile size ===>>>> " + documentFile.length());
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                } catch (IOException ignored) {
                    Log.e(TAG, "IOException ===>>>> " + ignored);
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            Log.e("<<==== progress", "" + progress[0] + " ====>>");
            Functions.showLoadingProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Work Done! PostExecute");
            Log.e(TAG, "onPostExecute ===>>>> " + result);
            Functions.cancelDeterminentLoader();
            if (result == null) {
                MediaScannerConnection.scanFile(MagazineDetails.this, new String[]{documentFile.getAbsolutePath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.e(TAG, "onScanCompleted path ===>>>> " + path);
                                Log.e(TAG, "onScanCompleted Uri ===>>>> " + uri);
                                storedWithSecurity();
                            }
                        });
            }
        }

    }

    private void storedWithSecurity() {
        if (documentFile != null) {
            DownloadedItemModel downloadedItemModel = new DownloadedItemModel("" + prefManager.getLoginId(), "" + magazineList.get(0).getId(),
                    "" + magazineList.get(0).getAuthorId(), "" + magazineList.get(0).getTitle(), "" + magazineList.get(0).getDescription(),
                    "" + magazineList.get(0).getIsPaid(), "" + magazineList.get(0).getSampleUrl(), "" + documentFile.getPath(),
                    "" + magazineList.get(0).getUrl(), "" + magazineList.get(0).getPrice(), "" + magazineList.get(0).getCategoryId(),
                    "" + magazineList.get(0).getImage(), "" + magazineList.get(0).getReadcnt(), "" + magazineList.get(0).getDownload(),
                    "" + magazineList.get(0).getIsFeature(), "" + magazineList.get(0).getStatus(), "" + magazineList.get(0).getCreatedAt(),
                    "" + magazineList.get(0).getUpdatedAt(), "" + magazineList.get(0).getCategoryName(),
                    "" + magazineList.get(0).getCategoryImage(), "" + magazineList.get(0).getAuthorName(),
                    "" + magazineList.get(0).getAuthorImage(), 1, "" + magazineList.get(0).getAvgRating(),
                    "" + magazineList.get(0).getTransactionDate(), "Magazines", 0, "my_magazines" + prefManager.getLoginId(),
                    "" + mainKeyAlias);

            List<DownloadedItemModel> myMagazines = Hawk.get("my_magazines" + prefManager.getLoginId());
            if (myMagazines == null) {
                myMagazines = new ArrayList<>();
            }
            for (int i = 0; i < myMagazines.size(); i++) {
                if (myMagazines.get(i).getId().equals("" + magazineList.get(0).getId())) {
                    myMagazines.remove(myMagazines.get(i));
                    Hawk.put("my_magazines" + prefManager.getLoginId(), myMagazines);
                }
            }
            myMagazines.add(downloadedItemModel);
            Hawk.put("my_magazines" + prefManager.getLoginId(), myMagazines);

            Log.e("myMagazines", "password => " + myMagazines.get(0).getFilePassword());
            Log.e("myMagazines", "magazineName => " + myMagazines.get(0).getTitle());
            AddDownload();
        } else {
            Toasty.warning(MagazineDetails.this, "" + getResources().getString(R.string.something_went_wrong), Toasty.LENGTH_SHORT).show();
            Utils.ProgressbarHide();
        }
    }

    /* add_magazine_download API */
    private void AddDownload() {
        Call<SuccessModel> call = BaseURL.getVideoAPI().add_magazine_download("" + prefManager.getLoginId(), "" + docID);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    Log.e("add_magazine_download", "" + response.body().getMessage());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(MagazineDetails.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("add_magazine_download", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_magazine_download", "Exception => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.shimmerHide(shimmer);
            }
        });
    }
    /*========= Download END =========*/

    private void CheckMagazineImage() {
        Log.e("CheckBookImage", "image => " + magazineList.get(0).getImage());
//        if (Utils.checkImage(BookList.get(0).getImage()) == true && !TextUtils.isEmpty(BookList.get(0).getImage())) {
//            //new DownloadImage().execute(BookList.get(0).getImage());
//            DownloadAndSaveImage("" + BookList.get(0).getImage());
//        } else {
        imageFile = null;
        ShareMagazine();
//        }
    }

    private void ShareMagazine() {
        String shareMessage = "\n\n" + getResources().getString(R.string.let_me_recommend_you_this_application_to_read_full_book)
                + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "" + getResources().getString(R.string.information_of_book));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "\n" + getResources().getString(R.string.magazine) + " " + magazineList.get(0).getTitle()
                + "\n" + getResources().getString(R.string.magazine_author) + " " + magazineList.get(0).getAuthorName()
                /*+ "\n\n" + magazineList.get(0).getDescription()*/ + shareMessage);

//        if (imageFile != null) {
//            Log.e("ShareBook", "imageFile Uri => " + Uri.parse(imageFile.getPath()));
//            shareIntent.setType("image/*");
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFile.getPath()));
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }

        try {
            startActivity(Intent.createChooser(shareIntent, "" + getResources().getString(R.string.share_with)));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("ShareBook", "Exception => " + ex.getMessage());
        }
    }

    private void DownloadAndSaveImage(String bookImgURL) {
        try {
            if (bookImgURL != null) {
                Log.e("=> bookImgURL", "" + bookImgURL);
                String saveBookImageName = "" + magazineList.get(0).getId() + ".jpeg";
                Log.e("DownloadAndSaveImage", "saveBookImageName => " + saveBookImageName);

                String downloadDirectory;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    downloadDirectory = Functions.getAppFolder(MagazineDetails.this) + getResources().getString(R.string.books) + "/Images/";
                } else {
                    downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + getResources().getString(R.string.books) + "/Images/";
                }
                Log.e("DownloadAndSaveImage", "downloadDirectory => " + downloadDirectory);

                File file = new File(downloadDirectory);
                if (!file.exists()) {
                    Log.e("DownloadAndSaveImage", "Image directory created again");
                    file.mkdirs();
                }

                File checkFile;
                checkFile = new File(file, saveBookImageName);
                Log.e("DownloadAndSaveImage", "checkFile => " + checkFile);

                if (!checkFile.exists()) {
                    Functions.showDeterminentLoader(MagazineDetails.this, false, false);
                    PRDownloader.initialize(getApplicationContext());
                    DownloadRequest prDownloader = PRDownloader.download(bookImgURL, downloadDirectory, saveBookImageName)
                            .build()
                            .setOnProgressListener(new OnProgressListener() {
                                @Override
                                public void onProgress(Progress progress) {
                                    int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                                    Functions.showLoadingProgress(prog);
                                }
                            });

                    String finalDownloadDirectory = downloadDirectory;
                    prDownloader.start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Functions.cancelDeterminentLoader();
                            Log.e("onDownloadComplete", "finalDownloadDirectory => " + finalDownloadDirectory);
                            Log.e("onDownloadComplete", "saveBookImageName => " + saveBookImageName);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                downloadBookImage(finalDownloadDirectory, saveBookImageName);
                            } else {
                                scanFileImage(finalDownloadDirectory, saveBookImageName);
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            Functions.cancelDeterminentLoader();
                            Log.e("onError", "error => " + error.getServerErrorMessage());
                        }
                    });
                } else {
                    imageFile = checkFile;
                    Log.e("isExists", "imageFile => " + imageFile);
                    ShareMagazine();
                }

            }
        } catch (Exception e) {
            Log.e("DownloadAndSaveImage", "Exception => " + e);
            e.printStackTrace();
        }
    }

    public void downloadBookImage(String path, String bookImageName) {
        Log.e("=>path", "" + path);
        ContentValues valuesimage;
        valuesimage = new ContentValues();
        valuesimage.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + Functions.getAppFolder(MagazineDetails.this) + getResources().getString(R.string.books) + "/Images/");
        valuesimage.put(MediaStore.MediaColumns.TITLE, bookImageName);
        valuesimage.put(MediaStore.MediaColumns.DISPLAY_NAME, bookImageName);
        valuesimage.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        valuesimage.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesimage.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        valuesimage.put(MediaStore.MediaColumns.IS_PENDING, 1);
        ContentResolver resolver = getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedImage = resolver.insert(collection, valuesimage);

        ParcelFileDescriptor pfd;
        try {
            pfd = getContentResolver().openFileDescriptor(uriSavedImage, "w");
            Log.e("=>pfd", "" + pfd);

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
            imageFile = new File(path + bookImageName);
            FileInputStream in = new FileInputStream(imageFile);

            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            pfd.close();
        } catch (Exception e) {
            Log.e("downloadBook", "Exception => " + e);
            e.printStackTrace();
            Utils.ProgressbarHide();
        }

        valuesimage.clear();
        valuesimage.put(MediaStore.MediaColumns.IS_PENDING, 0);
        getContentResolver().update(uriSavedImage, valuesimage, null, null);

        Log.e("=>imageFile", "" + imageFile);
        ShareMagazine();
    }

    public void scanFileImage(String downloadDirectory, String bookImageName) {
        MediaScannerConnection.scanFile(MagazineDetails.this, new String[]{downloadDirectory + bookImageName}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("onScanCompleted", "path => " + path);
                        Log.e("onScanCompleted", "bookImageName => " + bookImageName);
                        Log.e("onScanCompleted", "uri => " + uri.toString());
                        imageFile = new File(path);
                        Log.e("onScanCompleted", "imageFile => " + imageFile);
                        ShareMagazine();
                    }
                });
    }

    /*========= Pagination START =========*/
    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        magazineRelatedAdapter = new MagazineRelatedAdapter(MagazineDetails.this, relatedList);
        rv_related.setLayoutManager(new GridLayoutManager(MagazineDetails.this, 3));
        rv_related.setAdapter(magazineRelatedAdapter);
        magazineRelatedAdapter.notifyDataSetChanged();

        Utils.Pagination(rv_related, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        RelatedItem(page);
    }

    @Override
    public boolean isLoading() {
        Log.e("isLoading", "" + loading);
        return loading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        Log.e("page => ", "" + page);
        Log.e("totalPages => ", "" + totalPages);
        if (totalPages < page) {
            return false;
        } else {
            return page == totalPages;
        }
    }
    /*========= Pagination END =========*/

    /* Ad Section */
    private void ShowAdByClick(String Type) {
        adTYPE = Type;
        Log.e("=>adTYPE", "" + adTYPE);

        if (prefManager.getValue("reward_ad").equalsIgnoreCase("yes")) {
            if (mRewardedAd != null) {
                mRewardedAd.show(MagazineDetails.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.e("RewardItem amount =>", "" + rewardItem.getAmount());
                    }
                });
            } else {
                Log.e("mRewardedAd=>", "The ad wasn't ready yet.");
                if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                    CheckMagazineType();
                } else if (adTYPE.equalsIgnoreCase("READ")) {
                    ReadMagazine();
                } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                    ReadSampleMagazine();
                } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                    CheckMagazineImage();
                }
            }

        } else if (prefManager.getValue("fb_rewardvideo_status").equalsIgnoreCase("on")) {
            if (fbRewardedVideoAd != null && fbRewardedVideoAd.isAdLoaded()) {
                fbRewardedVideoAd.show();
            } else {
                Log.e("fbRewardedVideoAd=>", "The ad wasn't ready yet.");
                if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                    CheckMagazineType();
                } else if (adTYPE.equalsIgnoreCase("READ")) {
                    ReadMagazine();
                } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                    ReadSampleMagazine();
                } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                    CheckMagazineImage();
                }
            }

        } else if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
            if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                fbInterstitialAd.show();
            } else {
                Log.e("fbInterstitialAd=>", "The ad wasn't ready yet.");
                if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                    CheckMagazineType();
                } else if (adTYPE.equalsIgnoreCase("READ")) {
                    ReadMagazine();
                } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                    ReadSampleMagazine();
                } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                    CheckMagazineImage();
                }
            }

        } else {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MagazineDetails.this);
            } else {
                Log.e("mInterstitialAd=>", "The ad wasn't ready yet.");
                if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                    CheckMagazineType();
                } else if (adTYPE.equalsIgnoreCase("READ")) {
                    ReadMagazine();
                } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                    ReadSampleMagazine();
                } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                    CheckMagazineImage();
                }
            }
        }
    }

    private void InterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.e("Ad failed to show.", "" + adError.toString());
                    if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                        CheckMagazineType();
                    } else if (adTYPE.equalsIgnoreCase("READ")) {
                        ReadMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                        ReadSampleMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                        CheckMagazineImage();
                    }
                    mInterstitialAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.e(TAG, "Ad was shown.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.e(TAG, "Ad was dismissed.");
                    if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                        CheckMagazineType();
                    } else if (adTYPE.equalsIgnoreCase("READ")) {
                        ReadMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                        ReadSampleMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                        CheckMagazineImage();
                    }
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.e(TAG, "onAdImpression.");
                }
            };

            mInterstitialAd.load(this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.e(TAG, "onAdLoaded");
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e(TAG, "onAdFailedToLoad => " + loadAdError.getMessage());
                            mInterstitialAd = null;
                        }
                    });
        } catch (Exception e) {
            Log.e("InterstitialAd", "Exception => " + e);
        }
    }

    private void FacebookInterstitialAd() {
        try {
            fbInterstitialAd = new com.facebook.ads.InterstitialAd(this,
                    "CAROUSEL_IMG_SQUARE_APP_INSTALL#" + prefManager.getValue("fb_interstiatial_id"));
            fbInterstitialAd.loadAd(fbInterstitialAd.buildLoadAdConfig()
                    .withAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {
                            Log.e(TAG, "fbInterstitialAd displayed.");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            fbInterstitialAd = null;
                            if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                                CheckMagazineType();
                            } else if (adTYPE.equalsIgnoreCase("READ")) {
                                ReadMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                                ReadSampleMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                                CheckMagazineImage();
                            }
                            Log.e(TAG, "fbInterstitialAd dismissed.");
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            fbInterstitialAd = null;
                            Log.e(TAG, "fbInterstitialAd failed to load : " + adError.getErrorMessage());
                            if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                                CheckMagazineType();
                            } else if (adTYPE.equalsIgnoreCase("READ")) {
                                ReadMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                                ReadSampleMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                                CheckMagazineImage();
                            }
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.d(TAG, "fbInterstitialAd is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            Log.d(TAG, "fbInterstitialAd clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            Log.d(TAG, "fbInterstitialAd impression logged!");
                        }
                    })
                    .build());
        } catch (Exception e) {
            Log.e("fbInterstitialAd", "Exception =>" + e);
        }
    }

    private void RewardedVideoAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.e("Ad failed to show.", "" + adError.toString());
                            if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                                CheckMagazineType();
                            } else if (adTYPE.equalsIgnoreCase("READ")) {
                                ReadMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                                ReadSampleMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                                CheckMagazineImage();
                            }
                            mRewardedAd = null;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.e(TAG, "Ad was shown.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.e(TAG, "Ad was dismissed.");
                            if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                                CheckMagazineType();
                            } else if (adTYPE.equalsIgnoreCase("READ")) {
                                ReadMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                                ReadSampleMagazine();
                            } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                                CheckMagazineImage();
                            }
                            mRewardedAd = null;
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.e(TAG, "onAdImpression.");
                        }
                    };

            mRewardedAd.load(MagazineDetails.this, "" + prefManager.getValue("reward_adid"),
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            super.onAdLoaded(rewardedAd);
                            mRewardedAd = rewardedAd;
                            mRewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            mRewardedAd = null;
                        }
                    });

        } catch (Exception e) {
            Log.e("RewardedVideoAd", "Exception => " + e);
        }
    }

    private void FacebookRewardAd() {
        try {
            fbRewardedVideoAd = new RewardedVideoAd(MagazineDetails.this,
                    "VID_HD_16_9_15S_APP_INSTALL#" + prefManager.getValue("fb_rewardvideo_id"));

            fbRewardedVideoAd.loadAd(fbRewardedVideoAd.buildLoadAdConfig().withAdListener(new RewardedVideoAdListener() {
                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.e(TAG, "Rewarded video adError => " + adError.getErrorMessage());
                    if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                        CheckMagazineType();
                    } else if (adTYPE.equalsIgnoreCase("READ")) {
                        ReadMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                        ReadSampleMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                        CheckMagazineImage();
                    }
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e(TAG, "Rewarded video ad is loaded and ready to be displayed!");
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.e(TAG, "Rewarded video ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.e(TAG, "Rewarded video ad impression logged!");
                }

                @Override
                public void onRewardedVideoCompleted() {
                    Log.e(TAG, "Rewarded video completed!");
                }

                @Override
                public void onRewardedVideoClosed() {
                    Log.e(TAG, "Rewarded video ad closed!");
                    if (adTYPE.equalsIgnoreCase("DOWNLOAD")) {
                        CheckMagazineType();
                    } else if (adTYPE.equalsIgnoreCase("READ")) {
                        ReadMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SAMPLEREAD")) {
                        ReadSampleMagazine();
                    } else if (adTYPE.equalsIgnoreCase("SHARE")) {
                        CheckMagazineImage();
                    }
                    fbRewardedVideoAd.destroy();
                    fbRewardedVideoAd = null;
                }
            }).build());

        } catch (Exception e) {
            Log.e("FacebookRewardAd", "Exception => " + e.getMessage());
        }
    }
    /* Ad Section */

    @Override
    protected void onPause() {
        super.onPause();
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (mRewardedAd != null) {
            mRewardedAd = null;
        }
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbRewardedVideoAd != null) {
            fbRewardedVideoAd.destroy();
            fbRewardedVideoAd = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (mRewardedAd != null) {
            mRewardedAd = null;
        }
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbRewardedVideoAd != null) {
            fbRewardedVideoAd.destroy();
            fbRewardedVideoAd = null;
        }
    }

}