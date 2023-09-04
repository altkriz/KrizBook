package com.kriztech.book.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kriztech.book.Adapter.TabPagerAdapter;
import com.kriztech.book.Fragment.AuthorBooks;
import com.kriztech.book.Fragment.AuthorInfo;
import com.kriztech.book.Fragment.AuthorMagazines;
import com.kriztech.book.Model.AuthorModel.AuthorModel;
import com.kriztech.book.Model.ReadDowncntModel.ReadDowncntModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorPortfolio extends AppCompatActivity {

    private static final String TAG = AuthorPortfolio.class.getSimpleName();
    private PrefManager prefManager;
    private ShimmerFrameLayout shimmer;

    private TextView txtToolbarTitle, txtAuthorName, txtAuthorEmailID, txtAuthorAddress, txtTotalView, txtTotalDownload;
    private RoundedImageView ivAuthor;
    private RelativeLayout rlAdView;
    private LinearLayout lyToolbar, lyBack, lyFbAdView, lyAuthorEdit;
    private TabLayout tabLayout;
    private ViewPager tabViewPager;

    private String authorID = "", authorStatus = "0";
    public static String totalBooks = "0", totalMagazines = "0";
    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(this);
        setContentView(R.layout.activity_authorportfolio);
        PrefManager.forceRTLIfSupported(getWindow(), AuthorPortfolio.this);

        init();
        txtToolbarTitle.setText("" + getResources().getString(R.string.author_portfolio));
        AdInit();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            authorID = bundle.getString("authorID");
            Log.e(TAG, "authorID ==>> " + authorID);

            ReadDownloadCountByAuthor();

            if (authorID.equals("" + prefManager.getAuthorId())) {
                lyAuthorEdit.setVisibility(View.VISIBLE);
            } else {
                lyAuthorEdit.setVisibility(View.GONE);
            }
        }

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lyAuthorEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthorPortfolio.this, AuthorUpdate.class);
                intent.putExtra("authorID", "" + authorID);
                intent.putExtra("viewFrom", "AuthorProfile");
                startActivity(intent);
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(AuthorPortfolio.this);

            shimmer = findViewById(R.id.shimmer);
            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            txtAuthorName = findViewById(R.id.txtAuthorName);
            txtAuthorEmailID = findViewById(R.id.txtAuthorEmailID);
            txtAuthorAddress = findViewById(R.id.txtAuthorAddress);
            ivAuthor = findViewById(R.id.ivAuthor);
            txtTotalView = findViewById(R.id.txtTotalView);
            txtTotalDownload = findViewById(R.id.txtTotalDownload);

            lyAuthorEdit = findViewById(R.id.lyAuthorEdit);

            tabLayout = findViewById(R.id.tabLayout);
            tabViewPager = findViewById(R.id.tabViewPager);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(AuthorPortfolio.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(AuthorPortfolio.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GetAuthor();
    }

    /* get_author API */
    private void GetAuthor() {
        Utils.shimmerShow(shimmer);

        Call<AuthorModel> call = BaseURL.getVideoAPI().get_author("" + authorID);
        call.enqueue(new Callback<AuthorModel>() {
            @Override
            public void onResponse(Call<AuthorModel> call, Response<AuthorModel> response) {
                try {
                    Log.e("get_author", "Status ==>> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                txtAuthorName.setText("" + response.body().getResult().get(0).getName());
                                txtAuthorEmailID.setText("" + response.body().getResult().get(0).getEmail());
                                txtAuthorAddress.setText("" + response.body().getResult().get(0).getAddress());

                                authorStatus = "" + response.body().getResult().get(0).getStatus();
                                Constant.authorInfo = "" + response.body().getResult().get(0).getBio();
                                totalBooks = "" + response.body().getBook().getTotalBook();
                                totalMagazines = "" + response.body().getMagazine().getTotalMagazine();
                                txtTotalView.setText("" + (Integer.parseInt("" + response.body().getBook().getReadCount())
                                        + Integer.parseInt("" + response.body().getMagazine().getReadCount())));
                                txtTotalDownload.setText("" + (Integer.parseInt("" + response.body().getBook().getDownload())
                                        + Integer.parseInt("" + response.body().getMagazine().getDownload())));

                                if (!TextUtils.isEmpty(response.body().getResult().get(0).getImage())) {
                                    Picasso.get().load("" + response.body().getResult().get(0).getImage())
                                            .placeholder(getResources().getDrawable(R.drawable.ic_author)).into(ivAuthor);
                                } else {
                                    Picasso.get().load(R.drawable.ic_author).placeholder(R.drawable.ic_author).into(ivAuthor);
                                }

                                setupViewPager(tabViewPager);
                                tabLayout.setupWithViewPager(tabViewPager);
                                tabViewPager.setCurrentItem(1);
                                tabViewPager.setOffscreenPageLimit(1);

                            } else {
                                Log.e("get_author", "Message => " + response.body().getMessage());
                            }
                        }
                    } else {
                        Log.e("get_author", "Message ==>> " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("get_author", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<AuthorModel> call, Throwable t) {
                Log.e("get_author", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    //Tab With ViewPager
    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AuthorInfo(), "" + getResources().getString(R.string.about));
        adapter.addFragment(new AuthorBooks(authorID, authorStatus), "" + getResources().getString(R.string.books));
        adapter.addFragment(new AuthorMagazines(authorID, authorStatus), "" + getResources().getString(R.string.magazines));
        viewPager.setAdapter(adapter);
    }

    /* readcount_by_author API */
    private void ReadDownloadCountByAuthor() {
        Call<ReadDowncntModel> call = BaseURL.getVideoAPI().readcnt_by_author("" + authorID);
        call.enqueue(new Callback<ReadDowncntModel>() {
            @Override
            public void onResponse(Call<ReadDowncntModel> call, Response<ReadDowncntModel> response) {
                try {
                    Log.e("readcount_by_author", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        txtTotalView.setText("" + response.body().getResult().get(0).getReadcount());
                        txtTotalDownload.setText("" + response.body().getResult().get(0).getDownload());
                    } else {
                        txtTotalView.setText("0");
                        txtTotalDownload.setText("0");
                        Log.e("readcount_by_author", "Message => " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("readcount_by_author", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<ReadDowncntModel> call, Throwable t) {
                Log.e("readcount_by_author", "onFailure =>" + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.shimmerHide(shimmer);
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
            fbAdView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.shimmerHide(shimmer);
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
            fbAdView = null;
        }
    }

}