package com.kriztech.book.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.ViewAllMagazine;
import com.kriztech.book.Adapter.MagazineAdapter;
import com.kriztech.book.Adapter.MagazineCategoryAdapter;
import com.kriztech.book.Model.CategoryModel.CategoryModel;
import com.kriztech.book.Model.MagazineModel.MagazineModel;
import com.kriztech.book.Model.MagazineModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeBannerAd;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.TemplateView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Magazines extends Fragment implements View.OnClickListener {

    private PrefManager prefManager;
    private View root;

    private ShimmerFrameLayout shimmer;
    private LinearLayout lyPopularStories, lyMostViewed, lyCategory, lyTopDownloaded, lyPopularViewAll, lyMostViewViewAll,
            lyCategoryViewAll, lyTopDownloadedViewAll, lyNativeAdView;
    private RecyclerView rvPopularStories, rvMostViewed, rvCategory, rvTopDownloaded;

    private MagazineAdapter magazineAdapter;
    private MagazineCategoryAdapter magazineCategoryAdapter;

    private List<Result> popularList;
    private List<com.kriztech.book.Model.CategoryModel.Result> categoryList;
    private List<Result> mostviewedList;
    private List<Result> topDownloadedList;

    private TemplateView nativeTemplate = null;
    private NativeBannerAd fbNativeBannerAd = null;
    private NativeAdLayout fbNativeTemplate = null;
    private com.facebook.ads.NativeAd fbNativeAd = null;

    public Magazines() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_magazine, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        init();
        AdInit();
        PopularStories();
        TopDownloaded();
        MostViewed();
        GetCategory();

        return root;
    }

    private void init() {
        try {
            prefManager = new PrefManager(getActivity());
            shimmer = root.findViewById(R.id.shimmer);

            nativeTemplate = root.findViewById(R.id.nativeTemplate);
            fbNativeTemplate = root.findViewById(R.id.fbNativeTemplate);
            lyNativeAdView = root.findViewById(R.id.lyNativeAdView);

            lyPopularStories = root.findViewById(R.id.lyPopularStories);
            lyMostViewed = root.findViewById(R.id.lyMostViewed);
            lyCategory = root.findViewById(R.id.lyCategory);
            lyTopDownloaded = root.findViewById(R.id.lyTopDownloaded);
            lyPopularViewAll = root.findViewById(R.id.lyPopularViewAll);
            lyMostViewViewAll = root.findViewById(R.id.lyMostViewViewAll);
            lyCategoryViewAll = root.findViewById(R.id.lyCategoryViewAll);
            lyTopDownloadedViewAll = root.findViewById(R.id.lyTopDownloadedViewAll);

            rvPopularStories = root.findViewById(R.id.rvPopularStories);
            rvMostViewed = root.findViewById(R.id.rvMostViewed);
            rvCategory = root.findViewById(R.id.rvCategory);
            rvTopDownloaded = root.findViewById(R.id.rvTopDownloaded);

            lyPopularViewAll.setOnClickListener(this);
            lyMostViewViewAll.setOnClickListener(this);
            lyCategoryViewAll.setOnClickListener(this);
            lyTopDownloadedViewAll.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("init Exception ==>", "" + e);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyPopularViewAll:
                Intent intentPopular = new Intent(getActivity(), ViewAllMagazine.class);
                intentPopular.putExtra("title", "" + getResources().getString(R.string.Popular_Stories));
                startActivity(intentPopular);
                break;

            case R.id.lyTopDownloadedViewAll:
                Intent intentTop = new Intent(getActivity(), ViewAllMagazine.class);
                intentTop.putExtra("title", "" + getResources().getString(R.string.Top_dowloaded));
                startActivity(intentTop);
                break;

            case R.id.lyMostViewViewAll:
                Intent intentMost = new Intent(getActivity(), ViewAllMagazine.class);
                intentMost.putExtra("title", "" + getResources().getString(R.string.Most_viewed));
                startActivity(intentMost);
                break;

            case R.id.lyCategoryViewAll:
                Intent intentCategory = new Intent(getActivity(), ViewAllMagazine.class);
                intentCategory.putExtra("title", "" + getResources().getString(R.string.Magazine_category));
                startActivity(intentCategory);
                break;
        }
    }

    private void PopularStories() {
        Utils.shimmerShow(shimmer);
        Call<MagazineModel> call = BaseURL.getVideoAPI().popular_magazine("1");
        call.enqueue(new Callback<MagazineModel>() {
            @Override
            public void onResponse(Call<MagazineModel> call, Response<MagazineModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            popularList = new ArrayList<>();
                            popularList = response.body().getResult();
                            Log.e("popularList", "" + popularList.size());

                            magazineAdapter = new MagazineAdapter(getActivity(), popularList, "Popular");
                            rvPopularStories.setLayoutManager(new LinearLayoutManager(getActivity(),
                                    LinearLayoutManager.HORIZONTAL, false));
                            rvPopularStories.setAdapter(magazineAdapter);
                            magazineAdapter.notifyDataSetChanged();
                            rvPopularStories.setVisibility(View.VISIBLE);
                            lyPopularStories.setVisibility(View.VISIBLE);
                        } else {
                            rvPopularStories.setVisibility(View.GONE);
                            lyPopularStories.setVisibility(View.GONE);
                        }

                    } else {
                        rvPopularStories.setVisibility(View.GONE);
                        lyPopularStories.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("popular_magazine", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<MagazineModel> call, Throwable t) {
                Log.e("popular_magazine", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                rvPopularStories.setVisibility(View.GONE);
                lyPopularStories.setVisibility(View.GONE);
            }
        });
    }

    private void GetCategory() {
        Utils.shimmerShow(shimmer);

        Call<CategoryModel> call = BaseURL.getVideoAPI().categorylist("1");
        call.enqueue(new Callback<CategoryModel>() {
            @Override
            public void onResponse(Call<CategoryModel> call, Response<CategoryModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            categoryList = new ArrayList<>();
                            categoryList = response.body().getResult();
                            Log.e("categoryList", "" + categoryList.size());

                            magazineCategoryAdapter = new MagazineCategoryAdapter(getActivity(), categoryList,
                                    "Home");
                            rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(),
                                    LinearLayoutManager.HORIZONTAL, false));
                            rvCategory.setAdapter(magazineCategoryAdapter);
                            magazineCategoryAdapter.notifyDataSetChanged();
                            rvCategory.setVisibility(View.VISIBLE);
                            lyCategory.setVisibility(View.VISIBLE);
                        } else {
                            rvCategory.setVisibility(View.GONE);
                            lyCategory.setVisibility(View.GONE);
                        }

                    } else {
                        rvCategory.setVisibility(View.GONE);
                        lyCategory.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("category", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<CategoryModel> call, Throwable t) {
                Log.e("category", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                rvCategory.setVisibility(View.GONE);
                lyCategory.setVisibility(View.GONE);
            }
        });
    }

    private void TopDownloaded() {
        Utils.shimmerShow(shimmer);
        Call<MagazineModel> call = BaseURL.getVideoAPI().top_download_magazine("1");
        call.enqueue(new Callback<MagazineModel>() {
            @Override
            public void onResponse(Call<MagazineModel> call, Response<MagazineModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            topDownloadedList = new ArrayList<>();
                            topDownloadedList = response.body().getResult();
                            Log.e("topDownloadedList", "" + topDownloadedList.size());

                            magazineAdapter = new MagazineAdapter(getActivity(), topDownloadedList, "TopDownload");
                            rvTopDownloaded.setLayoutManager(new LinearLayoutManager(getActivity(),
                                    LinearLayoutManager.HORIZONTAL, false));
                            rvTopDownloaded.setAdapter(magazineAdapter);
                            magazineAdapter.notifyDataSetChanged();
                            rvTopDownloaded.setVisibility(View.VISIBLE);
                            lyTopDownloaded.setVisibility(View.VISIBLE);
                        } else {
                            rvTopDownloaded.setVisibility(View.GONE);
                            lyTopDownloaded.setVisibility(View.GONE);
                        }

                    } else {
                        rvTopDownloaded.setVisibility(View.GONE);
                        lyTopDownloaded.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("top_download_magazine", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<MagazineModel> call, Throwable t) {
                Log.e("top_download_magazine", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                rvTopDownloaded.setVisibility(View.GONE);
                lyTopDownloaded.setVisibility(View.GONE);
            }
        });
    }

    private void MostViewed() {
        Utils.shimmerShow(shimmer);
        Call<MagazineModel> call = BaseURL.getVideoAPI().top_magazine("1");
        call.enqueue(new Callback<MagazineModel>() {
            @Override
            public void onResponse(Call<MagazineModel> call, Response<MagazineModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            mostviewedList = new ArrayList<>();
                            mostviewedList = response.body().getResult();
                            Log.e("mostviewedList", "" + mostviewedList.size());

                            magazineAdapter = new MagazineAdapter(getActivity(), mostviewedList, "MostView");
                            rvMostViewed.setLayoutManager(new LinearLayoutManager(getActivity(),
                                    LinearLayoutManager.HORIZONTAL, false));
                            rvMostViewed.setAdapter(magazineAdapter);
                            magazineAdapter.notifyDataSetChanged();
                            rvMostViewed.setVisibility(View.VISIBLE);
                            lyMostViewed.setVisibility(View.VISIBLE);
                        } else {
                            rvMostViewed.setVisibility(View.GONE);
                            lyMostViewed.setVisibility(View.GONE);
                        }

                    } else {
                        rvMostViewed.setVisibility(View.GONE);
                        lyMostViewed.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e("top_magazine", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<MagazineModel> call, Throwable t) {
                Log.e("top_magazine", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                rvMostViewed.setVisibility(View.GONE);
                lyMostViewed.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause", "called");
        Utils.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbNativeAd != null) {
            fbNativeAd.destroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "called");
        Utils.shimmerHide(shimmer);
        if (fbNativeBannerAd != null) {
            fbNativeBannerAd.destroy();
        }
        if (fbNativeAd != null) {
            fbNativeAd.destroy();
        }
    }

}