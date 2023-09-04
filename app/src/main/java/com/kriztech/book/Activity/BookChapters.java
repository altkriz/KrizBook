package com.kriztech.book.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.BookChapterAdapter;
import com.kriztech.book.Interface.ItemClickListener;
import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.DownloadEpub;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookChapters extends AppCompatActivity implements ItemClickListener {

    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;
    private static final String TAG = BookChapters.class.getSimpleName();

    private ShimmerFrameLayout shimmer;
    private TextView txtToolbarTitle;
    private LinearLayout lyToolbar, lyBack, lyFbAdView, ly_dataNotFound;
    private RelativeLayout rlAdView;
    private RecyclerView rv_chapters;

    private BookChapterAdapter bookChapterAdapter;

    private InterstitialAd mInterstitialAd = null;
    private com.facebook.ads.InterstitialAd fbInterstitialAd = null;
    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    private String walletBalance = "0";
    private int clickPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(BookChapters.this);
        setContentView(R.layout.activity_book_chapters);
        PrefManager.forceRTLIfSupported(getWindow(), BookChapters.this);
        takePermissionUtils = new PermissionUtils(BookChapters.this, mPermissionResult);

        init();
        GetProfile();

        Intent intent = getIntent();
        if (intent.hasExtra("Title")) {
            txtToolbarTitle.setText("" + intent.getStringExtra("Title"));
            Log.e("Chapter size ==>", "" + BookDetails.bookChapterList.size());
        }

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookChapters.this.finish();
            }
        });

        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(BookChapters.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_ad", "" + prefManager.getValue("fb_banner_ad"));
        if (prefManager.getValue("fb_banner_ad").equalsIgnoreCase("yes")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(BookChapters.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }

    }

    private void init() {
        try {
            prefManager = new PrefManager(BookChapters.this);

            shimmer = findViewById(R.id.shimmer);
            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            rv_chapters = findViewById(R.id.rv_chapters);
            ly_dataNotFound = findViewById(R.id.ly_dataNotFound);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdInit();
    }

    private void AdInit() {
        Log.e("interstital_ad", "" + prefManager.getValue("interstital_ad"));
        if (prefManager.getValue("interstital_ad").equalsIgnoreCase("yes")) {
            mInterstitialAd = null;
            InterstitialAd();
        }

        Log.e("fb_interstiatial_ad", "" + prefManager.getValue("fb_interstiatial_ad"));
        if (prefManager.getValue("fb_interstiatial_ad").equalsIgnoreCase("yes")) {
            fbInterstitialAd = null;
            FacebookInterstitialAd();
        }
    }

    /* profile API */
    private void GetProfile() {
        Utils.shimmerShow(shimmer);

        Call<ProfileModel> call = BaseURL.getVideoAPI().profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        walletBalance = response.body().getResult().get(0).getCoinBalance();
                        Log.e("=>walletBalance", "" + walletBalance);

                        SetChapter();
                    }
                } catch (Exception e) {
                    Log.e("profile", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                Log.e("profile", "onFailure => " + t.getMessage());
            }
        });
    }

    private void SetChapter() {
        if (BookDetails.bookChapterList.size() > 0) {
            bookChapterAdapter = new BookChapterAdapter(BookChapters.this, BookDetails.bookChapterList,
                    "" + prefManager.getValue("currency_symbol"), BookChapters.this);
            rv_chapters.setLayoutManager(new GridLayoutManager(BookChapters.this, 1));
            rv_chapters.setAdapter(bookChapterAdapter);
            bookChapterAdapter.notifyDataSetChanged();

            rv_chapters.setVisibility(View.VISIBLE);
            ly_dataNotFound.setVisibility(View.GONE);
        } else {
            rv_chapters.setVisibility(View.GONE);
            ly_dataNotFound.setVisibility(View.VISIBLE);
        }

        Utils.shimmerHide(shimmer);
    }

    @Override
    public void onItemClick(int position) {
        clickPos = position;
        Log.e("==>walletBalance", "" + walletBalance);
        Log.e("==>position", "" + position);
        Log.e("==>author_id", "" + BookDetails.bookDetailsList.get(0).getAuthorId());
        Log.e("==>user_id", "" + prefManager.getLoginId());
        Log.e("==>amount", "" + BookDetails.bookChapterList.get(position).getPrice());
        Log.e("==>book_chapter_id", "" + BookDetails.bookChapterList.get(position).getId());
        Log.e("==>book_id", "" + BookDetails.bookChapterList.get(position).getBookId());
        Log.e("==>book_url", "" + BookDetails.bookChapterList.get(position).getUrl());
        Log.e("==>IsBuy", "" + BookDetails.bookChapterList.get(position).getIsBuy());

        if (Utils.checkLoginUser(BookChapters.this)) {
            if (takePermissionUtils.isStoragePermissionGranted()) {
                CheckChapterPurchase(position);
            } else {
                takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
            }
        }

    }

    private void CheckChapterPurchase(int position) {
        if (Integer.parseInt(BookDetails.bookChapterList.get(position).getPrice()) > 0) {

            if (BookDetails.bookChapterList.get(position).getIsBuy() == 0) {
                if (walletBalance.equalsIgnoreCase("0")) {
                    Log.e("walletBalance =>", "" + walletBalance);
                    Toasty.info(BookChapters.this, "" + getResources().getString(R.string.your_balance_is_low_please_top_up_your_wallet), Toasty.LENGTH_LONG).show();
                } else {
                    AlertDialog(position);
                }
            } else {
                if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
                    if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                        fbInterstitialAd.show();
                    } else {
                        ReadBook(position);
                    }
                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(BookChapters.this);
                    } else {
                        ReadBook(position);
                    }
                }
            }

        } else {
            if (prefManager.getValue("fb_interstiatial_status").equalsIgnoreCase("on")) {
                if (fbInterstitialAd != null && fbInterstitialAd.isAdLoaded()) {
                    fbInterstitialAd.show();
                } else {
                    ReadBook(position);
                }
            } else {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(BookChapters.this);
                } else {
                    ReadBook(position);
                }
            }
        }
    }

    private void AlertDialog(int pos) {
        final Dialog dialog = new Dialog(BookChapters.this);
        dialog.setContentView(LayoutInflater.from(BookChapters.this).inflate(R.layout.alert_dialog, null, false));
        PrefManager.forceRTLIfSupported(getWindow(), BookChapters.this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.overlayDark60)));
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        TextView txtTitle = dialog.findViewById(R.id.txtTitle);
        TextView txtPrice = dialog.findViewById(R.id.txtPrice);
        TextView txtDescription = dialog.findViewById(R.id.txtDescription);
        Button btnNegative = dialog.findViewById(R.id.btnNegative);
        Button btnPositive = dialog.findViewById(R.id.btnPositive);

        txtPrice.setText(prefManager.getValue("currency_symbol") + "" + BookDetails.bookChapterList.get(pos).getPrice());
        txtTitle.setText("" + BookDetails.bookChapterList.get(pos).getTitle());
        txtDescription.setText("" + getResources().getString(R.string.are_you_sure_want_to_purchase));

        btnPositive.setText("" + getResources().getString(R.string.purchase_now));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PurchaseChapter(pos);
            }
        });

        btnNegative.setText("" + getResources().getString(R.string.may_be_later));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /* add_chapter_transaction API */
    private void PurchaseChapter(int pos) {
        Utils.ProgressBarShow(BookChapters.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().add_chapter_transaction("" + BookDetails.bookDetailsList.get(0).getAuthorId(),
                "" + prefManager.getLoginId(), "" + BookDetails.bookChapterList.get(pos).getPrice(),
                "" + BookDetails.bookChapterList.get(pos).getId(), "" + BookDetails.bookChapterList.get(pos).getBookId());
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("add_chapter_transaction", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("add_chapter_transaction", "Message => " + response.body().getMessage());

                        AlertDialog alertDialog = new AlertDialog.Builder(BookChapters.this, R.style.AlertDialogDanger).create();
                        alertDialog.setTitle("" + getResources().getString(R.string.app_name));
                        alertDialog.setMessage("" + response.body().getMessage());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "" + getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BookDetails.bookChapterList.get(pos).setIsBuy(1);
                                bookChapterAdapter.notifyItemChanged(pos);
                            }
                        });
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(BookChapters.this, R.style.AlertDialogDanger).create();
                        alertDialog.setTitle("" + getResources().getString(R.string.app_name));
                        alertDialog.setMessage("" + response.body().getMessage());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "" + getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                } catch (Exception e) {
                    Log.e("add_chapter_transaction", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_chapter_transaction", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(BookChapters.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(BookChapters.this, getString(R.string.we_need_storage_permission_for_save_video));
                    } else if (allPermissionClear) {
                        Log.e("mPermissionResult", "isPermissionGranted => " + takePermissionUtils.isStoragePermissionGranted());
                        CheckChapterPurchase(clickPos);
                    }
                }
            });

    private void ReadBook(int position) {
        try {
            if (Functions.isConnectedToInternet(BookChapters.this)) {
                Log.e("url", "==> " + BookDetails.bookChapterList.get(position).getUrl());
                Log.e("url_data", "epub ? ==> " + BookDetails.bookChapterList.get(position).getUrl().contains(".EPUB"));
                if (BookDetails.bookChapterList.get(position).getUrl().contains(".epub") ||
                        BookDetails.bookChapterList.get(position).getUrl().contains(".EPUB")) {

                    DownloadEpub downloadEpub = new DownloadEpub(BookChapters.this);
                    downloadEpub.pathEpub(BookDetails.bookChapterList.get(position).getUrl(),
                            BookDetails.bookChapterList.get(position).getId(), "book", false);

                } else if (BookDetails.bookChapterList.get(position).getUrl().contains(".pdf") ||
                        BookDetails.bookChapterList.get(position).getUrl().contains(".PDF")) {

                    startActivity(new Intent(BookChapters.this, PDFShow.class)
                            .putExtra("link", BookDetails.bookChapterList.get(position).getUrl())
                            .putExtra("toolbarTitle", BookDetails.bookChapterList.get(position).getTitle())
                            .putExtra("type", "link"));
                }
            } else {
                Toasty.error(BookChapters.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ReadBook", "Exception ==>> " + e.getMessage());
        }
    }

    private void InterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.e("Ad failed to show.", "" + adError.toString());
                    ReadBook(clickPos);
                    mInterstitialAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    Log.e("TAG", "Ad was shown.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    Log.e("TAG", "Ad was dismissed.");
                    ReadBook(clickPos);
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Log.e("TAG", "onAdImpression.");
                }
            };

            mInterstitialAd.load(this, "" + prefManager.getValue("interstital_adid"),
                    adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            Log.e("TAG", "onAdLoaded");
                            mInterstitialAd = interstitialAd;
                            mInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("TAG", "onAdFailedToLoad => " + loadAdError.getMessage());
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
                            Log.e("TAG", "fb ad displayed.");
                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            ReadBook(clickPos);
                            Log.e("TAG", "fb ad dismissed.");
                            fbInterstitialAd = null;
                        }

                        @Override
                        public void onError(Ad ad, com.facebook.ads.AdError adError) {
                            Log.e("TAG", "fb ad failed to load : " + adError.getErrorMessage());
                            ReadBook(clickPos);
                            fbInterstitialAd = null;
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.d("TAG", "fb ad is loaded and ready to be displayed!");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {
                            Log.d("TAG", "fb ad clicked!");
                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {
                            Log.d("TAG", "fb ad impression logged!");
                        }
                    })
                    .build());
        } catch (Exception e) {
            Log.e("fb Interstial", "Exception =>" + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.ProgressbarHide();
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        if (fbInterstitialAd != null) {
            fbInterstitialAd.destroy();
            fbInterstitialAd = null;
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
    }

}