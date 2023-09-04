package com.kriztech.book.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.BookDetails;
import com.kriztech.book.Activity.ViewAllBook;
import com.kriztech.book.Adapter.AlsoLikeAdapter;
import com.kriztech.book.Adapter.AuthorAdapter;
import com.kriztech.book.Adapter.BannerAdapter;
import com.kriztech.book.Adapter.ContinueReadAdapter;
import com.kriztech.book.Adapter.FeatureAdapter;
import com.kriztech.book.Adapter.FreebookAdapter;
import com.kriztech.book.Adapter.NewArrivalAdapter;
import com.kriztech.book.Adapter.PaidBookAdapter;
import com.kriztech.book.Model.AuthorModel.AuthorModel;
import com.kriztech.book.Model.BannerModel.BannerModel;
import com.kriztech.book.Model.BookModel.BookModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.TemplateView;
import com.kenilt.loopingviewpager.widget.LoopingViewPager;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends Fragment implements View.OnClickListener, Paginate.Callbacks {

    public Home() {
    }

    private PrefManager prefManager;
    private View root;

    private ShimmerFrameLayout shimmer;
    private LinearLayout ly_top_reading_viewAll, lyNewArrivalViewAll, ly_author_viewAll, ly_freebook_viewAll, ly_paidbook_viewAll, ly_continue_viewAll,
            lyNativeAdView, lyContinueRead, lySellCountContinueRead, ly_paid_book, ly_free_book, ly_author, lyNewArrivalBook, ly_top_reading_Book, ly_category, ly_you_may_also_like,
            lyFreeOne, lyFreeTwo, lySellCountFreeBook1, lySellCountFreeBook2, lyPaidOne, lySellCountPaidBook1, lySellCountPaidBook2, lyPaidTwo, lyContinueOne,
            lyAds, lyFullAdView;
    private ImageView ivFreeBook1, ivFreeBook2, ivPaidBook1, ivPaidBook2, ivContinueRead;
    private TextView txt_free_bookname_1, txtSellCountFreeBook1, txtSellCountFreeBook2, txt_free_description_1, txt_free_book_cat_1, txt_free_bookname_2, txt_free_description_2,
            txt_free_book_cat_2, txt_paid_bookname_1, txtSellCountPaidBook1, txtSellCountPaidBook2, txt_paid_description_1, txt_paid_book_cat_1, txt_paid_bookname_2,
            txt_paid_description_2, txt_paid_book_cat_2, txt_continue_bookname, txt_continue_desc, txt_continue_read_cat, txtSellCountContinueRead;
    private RecyclerView rv_newarrival, rvAlsoLike, rv_freebook, rv_paidbook, rv_feature_item, rv_author, rv_continue;
    private LoopingViewPager mViewPager;
    private DotsIndicator dotsIndicator;

    private BannerAdapter bannerAdapter;
    private NewArrivalAdapter newArrivalAdapter;
    private AlsoLikeAdapter alsoLikeAdapter;
    private FreebookAdapter freebookAdapter;
    private PaidBookAdapter paidBookAdapter;
    private FeatureAdapter featureAdapter;
    private AuthorAdapter authorAdapter;
    private ContinueReadAdapter continueReadAdapter;

    private List<com.kriztech.book.Model.BannerModel.Result> bannerList;
    private List<com.kriztech.book.Model.BookModel.Result> newArrivalList;
    private List<com.kriztech.book.Model.BookModel.Result> alsoLikeList;
    private List<com.kriztech.book.Model.BookModel.Result> freebookList;
    private List<com.kriztech.book.Model.BookModel.Result> paidbookList;
    private List<com.kriztech.book.Model.BookModel.Result> featureList;
    private List<com.kriztech.book.Model.AuthorModel.Result> authorList;
    private List<com.kriztech.book.Model.BookModel.Result> continueReadList;

    private TemplateView nativeTemplate = null, nativeTemplate2 = null;
    private NativeBannerAd fbNativeBannerAd = null, fbNativeBannerAd2 = null;
    private NativeAdLayout fbNativeTemplate = null, fbNativeFullTemplate = null;
    private com.facebook.ads.NativeAd fbNativeAd = null;

    private Timer timer;
    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        init();
        AdInit();
        DisplayBanner();
        FeatureItem();
        NewArrival();
        AuthorList();
        freebook();
        paidbook();

        alsoLikeList = new ArrayList<>();
        setupPagination();
        AlsoLike(page);

        return root;
    }

    private void init() {
        try {
            prefManager = new PrefManager(getActivity());
            shimmer = root.findViewById(R.id.shimmer);

            nativeTemplate = root.findViewById(R.id.nativeTemplate);
            nativeTemplate2 = root.findViewById(R.id.nativeTemplate2);
            fbNativeTemplate = root.findViewById(R.id.fbNativeTemplate);
            fbNativeFullTemplate = root.findViewById(R.id.fbNativeFullTemplate);
            lyNativeAdView = root.findViewById(R.id.lyNativeAdView);
            lyFullAdView = root.findViewById(R.id.lyFullAdView);
            lyAds = root.findViewById(R.id.lyAds);
            mViewPager = root.findViewById(R.id.viewPager);
            dotsIndicator = root.findViewById(R.id.dotsIndicator);

            lyFreeOne = root.findViewById(R.id.lyFreeOne);
            lyFreeTwo = root.findViewById(R.id.lyFreeTwo);
            lySellCountFreeBook1 = root.findViewById(R.id.lySellCountFreeBook1);
            lySellCountFreeBook2 = root.findViewById(R.id.lySellCountFreeBook2);
            ivFreeBook1 = root.findViewById(R.id.ivFreeBook1);
            ivFreeBook2 = root.findViewById(R.id.ivFreeBook2);
            txt_free_bookname_1 = root.findViewById(R.id.txt_free_bookname_1);
            txt_free_bookname_2 = root.findViewById(R.id.txt_free_bookname_2);
            txtSellCountFreeBook1 = root.findViewById(R.id.txtSellCountFreeBook1);
            txtSellCountFreeBook2 = root.findViewById(R.id.txtSellCountFreeBook2);
            txt_free_book_cat_1 = root.findViewById(R.id.txt_free_book_cat_1);
            txt_free_book_cat_2 = root.findViewById(R.id.txt_free_book_cat_2);
            txt_free_description_1 = root.findViewById(R.id.txt_free_description_1);
            txt_free_description_2 = root.findViewById(R.id.txt_free_description_2);

            lyPaidOne = root.findViewById(R.id.lyPaidOne);
            lyPaidTwo = root.findViewById(R.id.lyPaidTwo);
            lySellCountPaidBook1 = root.findViewById(R.id.lySellCountPaidBook1);
            lySellCountPaidBook2 = root.findViewById(R.id.lySellCountPaidBook2);
            ivPaidBook1 = root.findViewById(R.id.ivPaidBook1);
            ivPaidBook2 = root.findViewById(R.id.ivPaidBook2);
            txt_paid_bookname_1 = root.findViewById(R.id.txt_paid_bookname_1);
            txt_paid_bookname_2 = root.findViewById(R.id.txt_paid_bookname_2);
            txtSellCountPaidBook1 = root.findViewById(R.id.txtSellCountPaidBook1);
            txtSellCountPaidBook2 = root.findViewById(R.id.txtSellCountPaidBook2);
            txt_paid_book_cat_1 = root.findViewById(R.id.txt_paid_book_cat_1);
            txt_paid_book_cat_2 = root.findViewById(R.id.txt_paid_book_cat_2);
            txt_paid_description_1 = root.findViewById(R.id.txt_paid_description_1);
            txt_paid_description_2 = root.findViewById(R.id.txt_paid_description_2);

            lyContinueRead = root.findViewById(R.id.lyContinueRead);
            lyContinueOne = root.findViewById(R.id.lyContinueOne);
            lySellCountContinueRead = root.findViewById(R.id.lySellCountContinueRead);
            ivContinueRead = root.findViewById(R.id.ivContinueRead);
            txt_continue_bookname = root.findViewById(R.id.txt_continue_bookname);
            txt_continue_desc = root.findViewById(R.id.txt_continue_desc);
            txt_continue_read_cat = root.findViewById(R.id.txt_continue_read_cat);
            txtSellCountContinueRead = root.findViewById(R.id.txtSellCountContinueRead);

            rv_newarrival = root.findViewById(R.id.rv_newarrival);
            rv_feature_item = root.findViewById(R.id.rv_feature_item);
            rv_author = root.findViewById(R.id.rv_author);
            rv_continue = root.findViewById(R.id.rv_continue);
            rv_freebook = root.findViewById(R.id.rv_freebook);
            rv_paidbook = root.findViewById(R.id.rv_paidbook);
            rvAlsoLike = root.findViewById(R.id.rvAlsoLike);

            ly_paid_book = root.findViewById(R.id.ly_paid_book);
            ly_free_book = root.findViewById(R.id.ly_free_book);
            ly_author = root.findViewById(R.id.ly_author);
            lyNewArrivalBook = root.findViewById(R.id.lyNewArrivalBook);
            ly_top_reading_Book = root.findViewById(R.id.ly_top_reading_Book);
            ly_category = root.findViewById(R.id.ly_category);
            ly_you_may_also_like = root.findViewById(R.id.ly_you_may_also_like);

            ly_top_reading_viewAll = root.findViewById(R.id.ly_top_reading_viewAll);
            lyNewArrivalViewAll = root.findViewById(R.id.lyNewArrivalViewAll);
            ly_author_viewAll = root.findViewById(R.id.ly_author_viewAll);
            ly_continue_viewAll = root.findViewById(R.id.ly_continue_viewAll);
            ly_paidbook_viewAll = root.findViewById(R.id.ly_paidbook_viewAll);
            ly_freebook_viewAll = root.findViewById(R.id.ly_freebook_viewAll);

            ly_top_reading_viewAll.setOnClickListener(this);
            lyNewArrivalViewAll.setOnClickListener(this);
            ly_author_viewAll.setOnClickListener(this);
            ly_paidbook_viewAll.setOnClickListener(this);
            ly_freebook_viewAll.setOnClickListener(this);
            ly_continue_viewAll.setOnClickListener(this);
            lyFreeOne.setOnClickListener(this);
            lyFreeTwo.setOnClickListener(this);
            lyPaidOne.setOnClickListener(this);
            lyPaidTwo.setOnClickListener(this);
            lyContinueOne.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void AdInit() {
        Log.e("fb_native_status", "" + prefManager.getValue("fb_native_status"));
        Log.e("native_ad", "" + prefManager.getValue("native_ad"));
        if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            lyNativeAdView.setVisibility(View.VISIBLE);
            nativeTemplate.setVisibility(View.VISIBLE);
            fbNativeTemplate.setVisibility(View.GONE);
            Utils.NativeAds(getActivity(), nativeTemplate, "" + prefManager.getValue("native_adid"));
        } else if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            lyNativeAdView.setVisibility(View.VISIBLE);
            fbNativeTemplate.setVisibility(View.VISIBLE);
            nativeTemplate.setVisibility(View.GONE);
            Utils.FacebookNativeAdSmall(getActivity(), fbNativeBannerAd, fbNativeTemplate, "" + prefManager.getValue("fb_native_id"));
        } else {
            lyNativeAdView.setVisibility(View.GONE);
            nativeTemplate.setVisibility(View.GONE);
            fbNativeTemplate.setVisibility(View.GONE);
        }

        Log.e("fb_native_full_status", "" + prefManager.getValue("fb_native_full_status"));
        if (prefManager.getValue("fb_native_full_status").equalsIgnoreCase("on")) {
            lyFullAdView.setVisibility(View.VISIBLE);
            nativeTemplate2.setVisibility(View.GONE);
            fbNativeFullTemplate.setVisibility(View.VISIBLE);
            Utils.FacebookNativeAdLarge(getActivity(), fbNativeAd, fbNativeFullTemplate, "" + prefManager.getValue("fb_native_full_id"));
        } else if (prefManager.getValue("fb_native_status").equalsIgnoreCase("on")) {
            lyFullAdView.setVisibility(View.VISIBLE);
            nativeTemplate2.setVisibility(View.GONE);
            fbNativeFullTemplate.setVisibility(View.VISIBLE);
            Utils.FacebookNativeAdSmall(getActivity(), fbNativeBannerAd, fbNativeFullTemplate, "" + prefManager.getValue("fb_native_id"));
        } else if (prefManager.getValue("native_ad").equalsIgnoreCase("yes")) {
            lyFullAdView.setVisibility(View.VISIBLE);
            nativeTemplate2.setVisibility(View.VISIBLE);
            fbNativeFullTemplate.setVisibility(View.GONE);
            Utils.NativeAds(getActivity(), nativeTemplate2, "" + prefManager.getValue("native_adid"));
        } else {
            lyFullAdView.setVisibility(View.GONE);
            nativeTemplate2.setVisibility(View.GONE);
            fbNativeFullTemplate.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("onStart", "called");
        ContinueRead();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.ly_top_reading_viewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.app_bestBook));
                startActivity(intent);
                break;

            case R.id.lyNewArrivalViewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.New_Arrival_Book));
                startActivity(intent);
                break;

            case R.id.ly_author_viewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.Authors));
                startActivity(intent);
                break;

            case R.id.ly_paidbook_viewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.Paid_Book));
                startActivity(intent);
                break;

            case R.id.ly_freebook_viewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.Free_Book));
                startActivity(intent);
                break;

            case R.id.ly_continue_viewAll:
                intent = new Intent(getActivity(), ViewAllBook.class);
                intent.putExtra("title", "" + getResources().getString(R.string.Continue_Reading));
                startActivity(intent);
                break;

            case R.id.lyFreeOne:
                intent = new Intent(getActivity(), BookDetails.class);
                intent.putExtra("docID", "" + freebookList.get(0).getId());
                intent.putExtra("authorID", "" + freebookList.get(0).getAuthorId());
                getActivity().startActivity(intent);
                break;

            case R.id.lyFreeTwo:
                intent = new Intent(getActivity(), BookDetails.class);
                intent.putExtra("docID", "" + freebookList.get(1).getId());
                intent.putExtra("authorID", "" + freebookList.get(1).getAuthorId());
                getActivity().startActivity(intent);
                break;

            case R.id.lyPaidOne:
                intent = new Intent(getActivity(), BookDetails.class);
                intent.putExtra("docID", paidbookList.get(0).getId());
                intent.putExtra("authorID", "" + paidbookList.get(0).getAuthorId());
                getActivity().startActivity(intent);
                break;

            case R.id.lyPaidTwo:
                intent = new Intent(getActivity(), BookDetails.class);
                intent.putExtra("docID", paidbookList.get(1).getId());
                intent.putExtra("authorID", "" + paidbookList.get(1).getAuthorId());
                getActivity().startActivity(intent);
                break;

            case R.id.lyContinueOne:
                intent = new Intent(getActivity(), BookDetails.class);
                intent.putExtra("docID", "" + continueReadList.get(0).getId());
                intent.putExtra("authorID", "" + continueReadList.get(0).getAuthorId());
                getActivity().startActivity(intent);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /* get_ads_banner API */
    private void DisplayBanner() {
        Utils.shimmerShow(shimmer);
        Call<BannerModel> call = BaseURL.getVideoAPI().get_ads_banner();
        call.enqueue(new Callback<BannerModel>() {
            @Override
            public void onResponse(Call<BannerModel> call, Response<BannerModel> response) {
                Utils.shimmerHide(shimmer);
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        bannerList = new ArrayList<>();
                        bannerList = response.body().getResult();
                        Log.e("bannerList", "" + bannerList.size());
                        SetBanner();
                        lyAds.setVisibility(View.VISIBLE);

                    } else {
                        lyAds.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("get_ads_banner", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<BannerModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                Log.e("get_ads_banner", "onFailure => " + t.getMessage());
                lyAds.setVisibility(View.GONE);
            }
        });
    }

    private void SetBanner() {
        bannerAdapter = new BannerAdapter(getActivity(), bannerList);
        mViewPager.setAdapter(bannerAdapter);
        bannerAdapter.notifyDataSetChanged();
        dotsIndicator.setViewPager(mViewPager);

        if (bannerList.size() > 0) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    mViewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        }
                    });
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, getResources().getInteger(R.integer.bannerChangeTime), getResources().getInteger(R.integer.bannerChangeTime));
        }
    }

    /* popularbooklist API */
    private void FeatureItem() {
        Utils.shimmerShow(shimmer);
        Call<BookModel> call = BaseURL.getVideoAPI().popularbooklist("1");
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            featureList = new ArrayList<>();
                            featureList = response.body().getResult();
                            Log.e("featureList", "" + featureList.size());

                            if (featureList.size() > 0) {
                                featureAdapter = new FeatureAdapter(getActivity(), featureList, "Home", "" + prefManager.getValue("currency_symbol"));
                                rv_feature_item.setLayoutManager(new GridLayoutManager(getActivity(), 2,
                                        LinearLayoutManager.HORIZONTAL, false));
                                rv_feature_item.setAdapter(featureAdapter);
                                featureAdapter.notifyDataSetChanged();

                                ly_top_reading_Book.setVisibility(View.VISIBLE);
                            } else {
                                ly_top_reading_Book.setVisibility(View.GONE);
                            }

                        } else {
                            ly_top_reading_Book.setVisibility(View.GONE);
                        }
                    } else {
                        ly_top_reading_Book.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.e("popularbookList", "Exception => " + e);
                    ly_top_reading_Book.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Log.e("popularbookList", "onFailure => " + t.getMessage());
                ly_top_reading_Book.setVisibility(View.GONE);
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* newarriaval API */
    private void NewArrival() {
        Utils.shimmerShow(shimmer);

        Call<BookModel> call = BaseURL.getVideoAPI().newarriaval("1");
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                newArrivalList = new ArrayList<>();
                                newArrivalList = response.body().getResult();
                                Log.e("newArrivalList", "" + newArrivalList.size());

                                newArrivalAdapter = new NewArrivalAdapter(getActivity(), newArrivalList, "Home");
                                rv_newarrival.setLayoutManager(new GridLayoutManager(getActivity(), 3,
                                        LinearLayoutManager.VERTICAL, false));
                                rv_newarrival.setAdapter(newArrivalAdapter);
                                newArrivalAdapter.notifyDataSetChanged();

                                lyNewArrivalBook.setVisibility(View.VISIBLE);
                            } else {
                                lyNewArrivalBook.setVisibility(View.GONE);
                            }
                        } else {
                            lyNewArrivalBook.setVisibility(View.GONE);
                        }
                    } else {
                        lyNewArrivalBook.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("newarriaval", "Exception => " + e);
                    lyNewArrivalBook.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Log.e("newarriaval", "onFailure => " + t.getMessage());
                lyNewArrivalBook.setVisibility(View.GONE);
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* autherlist API */
    private void AuthorList() {
        Utils.shimmerShow(shimmer);

        Call<AuthorModel> call = BaseURL.getVideoAPI().autherlist("1");
        call.enqueue(new Callback<AuthorModel>() {
            @Override
            public void onResponse(Call<AuthorModel> call, Response<AuthorModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                authorList = new ArrayList<>();
                                authorList = response.body().getResult();
                                Log.e("authorList", "" + authorList.size());

                                authorAdapter = new AuthorAdapter(getActivity(), authorList, "Home");
                                rv_author.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                                rv_author.setAdapter(authorAdapter);
                                authorAdapter.notifyDataSetChanged();

                                ly_author.setVisibility(View.VISIBLE);
                            } else {
                                ly_author.setVisibility(View.GONE);
                            }
                        } else {
                            ly_author.setVisibility(View.GONE);
                        }

                    } else {
                        ly_author.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("authorList", "Exception => " + e);
                    ly_author.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<AuthorModel> call, Throwable t) {
                Log.e("autherlist", "onFailure => " + t.getMessage());
                ly_author.setVisibility(View.GONE);
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* free_paid_booklist API */
    private void freebook() {
        Utils.shimmerShow(shimmer);

        Call<BookModel> call = BaseURL.getVideoAPI().free_paid_booklist("0", "1");
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                freebookList = new ArrayList<>();
                                freebookList = response.body().getResult();
                                Log.e("freebookList", "" + freebookList.size());

                                rv_freebook.setVisibility(View.VISIBLE);
                                ly_free_book.setVisibility(View.VISIBLE);

                                txt_free_bookname_1.setText("" + freebookList.get(0).getTitle());
                                txt_free_description_1.setText(Html.fromHtml(freebookList.get(0).getDescription()));
                                txt_free_book_cat_1.setText("" + freebookList.get(0).getCategoryName());
                                if (!TextUtils.isEmpty(freebookList.get(0).getImage())) {
                                    Picasso.get().load(freebookList.get(0).getImage()).into(ivFreeBook1);
                                } else {
                                    Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivFreeBook1);
                                }
                                if (freebookList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                    lySellCountFreeBook1.setVisibility(View.VISIBLE);
                                    txtSellCountFreeBook1.setText("" + Utils.changeToK(Long.parseLong("" + freebookList.get(0).getTotalSell())));
                                } else {
                                    lySellCountFreeBook1.setVisibility(View.GONE);
                                }

                                if (freebookList.size() > 1) {
                                    lyFreeTwo.setVisibility(View.VISIBLE);

                                    txt_free_bookname_2.setText("" + freebookList.get(1).getTitle());
                                    txt_free_description_2.setText(Html.fromHtml(freebookList.get(1).getDescription()));
                                    txt_free_book_cat_2.setText("" + freebookList.get(1).getCategoryName());
                                    if (!TextUtils.isEmpty(freebookList.get(1).getImage())) {
                                        Picasso.get().load(freebookList.get(1).getImage()).into(ivFreeBook2);
                                    } else {
                                        Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivFreeBook2);
                                    }
                                    if (freebookList.get(1).getIsPaid().equalsIgnoreCase("1")) {
                                        lySellCountFreeBook2.setVisibility(View.VISIBLE);
                                        txtSellCountFreeBook2.setText("" + Utils.changeToK(Long.parseLong("" + freebookList.get(1).getTotalSell())));
                                    } else {
                                        lySellCountFreeBook2.setVisibility(View.GONE);
                                    }

                                } else {
                                    lyFreeTwo.setVisibility(View.GONE);
                                }

                                if (freebookList.size() > 2) {
                                    freebookAdapter = new FreebookAdapter(getActivity(), freebookList, "Home");
                                    rv_freebook.setLayoutManager(new GridLayoutManager(getActivity(), 3,
                                            LinearLayoutManager.VERTICAL, false));
                                    rv_freebook.setAdapter(freebookAdapter);
                                    freebookAdapter.notifyDataSetChanged();
                                } else {
                                    rv_freebook.setVisibility(View.GONE);
                                }

                            } else {
                                ly_free_book.setVisibility(View.GONE);
                            }
                        } else {
                            ly_free_book.setVisibility(View.GONE);
                        }
                    } else {
                        ly_free_book.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("free_booklist =>", "Exception => " + e);
                    ly_free_book.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                Log.e("free_booklist", "onFailure => " + t.getMessage());
                ly_free_book.setVisibility(View.GONE);
            }
        });
    }

    /* free_paid_booklist API */
    private void paidbook() {
        Utils.shimmerShow(shimmer);

        Call<BookModel> call = BaseURL.getVideoAPI().free_paid_booklist("1", "1");
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                paidbookList = new ArrayList<>();
                                paidbookList = response.body().getResult();
                                Log.e("paidbookList", "" + paidbookList.size());

                                rv_paidbook.setVisibility(View.VISIBLE);
                                ly_paid_book.setVisibility(View.VISIBLE);

                                txt_paid_bookname_1.setText("" + paidbookList.get(0).getTitle());
                                txt_paid_description_1.setText(Html.fromHtml(paidbookList.get(0).getDescription()));
                                txt_paid_book_cat_1.setText("" + paidbookList.get(0).getCategoryName());
                                if (!TextUtils.isEmpty(paidbookList.get(0).getImage())) {
                                    Picasso.get().load(paidbookList.get(0).getImage()).into(ivPaidBook1);
                                } else {
                                    Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivPaidBook1);
                                }
                                if (paidbookList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                    lySellCountPaidBook1.setVisibility(View.VISIBLE);
                                    txtSellCountPaidBook1.setText("" + Utils.changeToK(Long.parseLong("" + paidbookList.get(0).getTotalSell())));
                                } else {
                                    lySellCountPaidBook1.setVisibility(View.GONE);
                                }

                                if (paidbookList.size() > 1) {
                                    lyPaidTwo.setVisibility(View.VISIBLE);

                                    txt_paid_bookname_2.setText("" + paidbookList.get(1).getTitle());
                                    txt_paid_description_2.setText(Html.fromHtml(paidbookList.get(1).getDescription()));
                                    txt_paid_book_cat_2.setText("" + paidbookList.get(1).getCategoryName());
                                    if (!TextUtils.isEmpty(paidbookList.get(1).getImage())) {
                                        Picasso.get().load(paidbookList.get(1).getImage()).into(ivPaidBook2);
                                    } else {
                                        Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivPaidBook2);
                                    }
                                    if (paidbookList.get(1).getIsPaid().equalsIgnoreCase("1")) {
                                        lySellCountPaidBook2.setVisibility(View.VISIBLE);
                                        txtSellCountPaidBook2.setText("" + Utils.changeToK(Long.parseLong("" + paidbookList.get(1).getTotalSell())));
                                    } else {
                                        lySellCountPaidBook2.setVisibility(View.GONE);
                                    }

                                } else {
                                    lyPaidTwo.setVisibility(View.GONE);
                                }

                                if (paidbookList.size() > 2) {
                                    paidBookAdapter = new PaidBookAdapter(getActivity(), paidbookList,
                                            "Home", prefManager.getValue("currency_symbol"));
                                    rv_paidbook.setLayoutManager(new GridLayoutManager(getActivity(), 3,
                                            LinearLayoutManager.VERTICAL, false));
                                    rv_paidbook.setAdapter(paidBookAdapter);
                                    paidBookAdapter.notifyDataSetChanged();
                                } else {
                                    rv_paidbook.setVisibility(View.GONE);
                                }

                            } else {
                                ly_paid_book.setVisibility(View.GONE);
                            }
                        } else {
                            ly_paid_book.setVisibility(View.GONE);
                        }

                    } else {
                        ly_paid_book.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("paid_booklist", "Exception => " + e);
                    ly_paid_book.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                ly_paid_book.setVisibility(View.GONE);
                Log.e("paid_booklist", "onFailure => " + t.getMessage());
            }
        });
    }

    /* continue_read API */
    private void ContinueRead() {
        Call<BookModel> call = BaseURL.getVideoAPI().continue_read("" + prefManager.getLoginId(), "1");
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                continueReadList = new ArrayList<>();
                                continueReadList = response.body().getResult();
                                Log.e("ContinueList", "" + continueReadList.size());

                                txt_continue_bookname.setText("" + continueReadList.get(0).getTitle());
                                txt_continue_desc.setText(Html.fromHtml(continueReadList.get(0).getDescription()));
                                txt_continue_read_cat.setText("" + continueReadList.get(0).getCategoryName());
                                if (!TextUtils.isEmpty(continueReadList.get(0).getImage())) {
                                    Picasso.get().load(continueReadList.get(0).getImage()).into(ivContinueRead);
                                } else {
                                    Picasso.get().load(R.drawable.no_image_potr).placeholder(R.drawable.no_image_potr).into(ivContinueRead);
                                }
                                if (continueReadList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                    lySellCountContinueRead.setVisibility(View.VISIBLE);
                                    txtSellCountContinueRead.setText("" + Utils.changeToK(Long.parseLong("" + continueReadList.get(0).getTotalSell())));
                                } else {
                                    lySellCountContinueRead.setVisibility(View.GONE);
                                }

                                if (continueReadList.size() > 1) {
                                    continueReadAdapter = new ContinueReadAdapter(getActivity(), continueReadList, "Home");
                                    rv_continue.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));
                                    rv_continue.setAdapter(continueReadAdapter);
                                    continueReadAdapter.notifyDataSetChanged();

                                    lyContinueRead.setVisibility(View.VISIBLE);
                                    rv_continue.setVisibility(View.VISIBLE);
                                } else {
                                    rv_continue.setVisibility(View.GONE);
                                }

                            } else {
                                Log.e("continue_reading", "message => " + response.body().getMessage());
                                lyContinueRead.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e("continue_reading", "message => " + response.body().getMessage());
                            lyContinueRead.setVisibility(View.GONE);
                        }

                    } else {
                        Log.e("continue_reading", "message => " + response.body().getMessage());
                        lyContinueRead.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("continue_reading", "Exception => " + e);
                    lyContinueRead.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                lyContinueRead.setVisibility(View.GONE);
                Log.e("continue_reading", "onFailure => " + t.getMessage());
            }
        });
    }

    /* alsolike API */
    private void AlsoLike(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Log.e("pageNo ==>", "" + pageNo);
        Call<BookModel> call = BaseURL.getVideoAPI().alsolike("" + pageNo);
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    Log.e("alsolike", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("alsolike", "totalPages => " + totalPages);

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {
                                alsoLikeList = response.body().getResult();
                                Log.e("alsoLikeList", "" + alsoLikeList.size());

                                rvAlsoLike.setVisibility(View.VISIBLE);
                                loading = false;
                                alsoLikeAdapter.addBook(alsoLikeList);
                                ly_you_may_also_like.setVisibility(View.VISIBLE);
                            } else {
                                ly_you_may_also_like.setVisibility(View.GONE);
                                loading = false;
                            }
                        } else {
                            ly_you_may_also_like.setVisibility(View.GONE);
                            loading = false;
                        }

                    } else {
                        ly_you_may_also_like.setVisibility(View.GONE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("alsoLike", "Exception => " + e);
                    ly_you_may_also_like.setVisibility(View.GONE);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    ly_you_may_also_like.setVisibility(View.GONE);
                }
                loading = false;
                Log.e("alsoLike", "onFailure => " + t.getMessage());
            }
        });
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;
        Log.e("==>page", "" + totalPages);

        alsoLikeAdapter = new AlsoLikeAdapter(getActivity(), alsoLikeList);
        rvAlsoLike.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        rvAlsoLike.setAdapter(alsoLikeAdapter);
        alsoLikeAdapter.notifyDataSetChanged();

        Utils.Pagination(rvAlsoLike, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        Log.e("onLoadMore", "page => " + page);
        loading = true;
        page++;
        AlsoLike(page);
    }

    @Override
    public boolean isLoading() {
        Log.e("isLoading =>", "" + loading);
        return loading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        Log.e("page =>", "" + page);
        Log.e("totalPages =>", "" + totalPages);
        if (totalPages < page) {
            return false;
        } else {
            return page == totalPages;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause", "called");
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbNativeBannerAd2 != null) {
            fbNativeBannerAd2.destroy();
        }
        if (fbNativeAd != null) {
            fbNativeAd.destroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();

        Log.e("onDestroy", "called");
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbNativeBannerAd2 != null) {
            fbNativeBannerAd2.destroy();
        }
        if (fbNativeAd != null) {
            fbNativeAd.destroy();
        }
    }

}