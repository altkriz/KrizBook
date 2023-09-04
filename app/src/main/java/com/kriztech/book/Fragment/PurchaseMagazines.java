package com.kriztech.book.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.PDFShow;
import com.kriztech.book.Adapter.MyPurchaseAdapter;
import com.kriztech.book.Interface.ItemClick;
import com.kriztech.book.Model.DownloadModel.DownloadModel;
import com.kriztech.book.Model.DownloadModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.DownloadEpub;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseMagazines extends Fragment implements ItemClick, Paginate.Callbacks {

    private static final String TAG = PurchaseMagazines.class.getSimpleName();
    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private RecyclerView rvDocuments;
    private LinearLayout lyNoData, lyFbAdView;
    private TextView txtNoData;
    private ImageView ivNoData;
    private RelativeLayout rlAdView;

    private List<Result> magazineList;
    private MyPurchaseAdapter myPurchaseAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.subfragment_mydocuments, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());

        shimmer = root.findViewById(R.id.shimmer);
        rlAdView = root.findViewById(R.id.rlAdView);
        lyFbAdView = root.findViewById(R.id.lyFbAdView);
        rvDocuments = root.findViewById(R.id.rvDocuments);
        lyNoData = root.findViewById(R.id.lyNoData);
        ivNoData = root.findViewById(R.id.ivNoData);
        txtNoData = root.findViewById(R.id.txtNoData);

        AdInit();
        magazineList = new ArrayList<>();
        setupPagination();
        PurchaseMagazine(page);

        return root;
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(getActivity(), mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(getActivity(), fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    private void PurchaseMagazine(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Call<DownloadModel> call = BaseURL.getVideoAPI().purchaseMagazineList(prefManager.getLoginId(), "magazine", "" + pageNo);
        call.enqueue(new Callback<DownloadModel>() {
            @Override
            public void onResponse(Call<DownloadModel> call, Response<DownloadModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            magazineList = response.body().getResult();
                            Log.e("magazineList", "" + magazineList.size());

                            rvDocuments.setVisibility(View.VISIBLE);
                            loading = false;
                            myPurchaseAdapter.addBook(magazineList);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            rvDocuments.setVisibility(View.GONE);
                            lyNoData.setVisibility(View.VISIBLE);
                            loading = false;
                        }

                    } else {
                        rvDocuments.setVisibility(View.GONE);
                        lyNoData.setVisibility(View.VISIBLE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("purchaseMagazineList", "Exception => " + e);
                    rvDocuments.setVisibility(View.GONE);
                    lyNoData.setVisibility(View.VISIBLE);
                    loading = false;
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<DownloadModel> call, Throwable t) {
                Log.e("purchaseMagazineList", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    rvDocuments.setVisibility(View.GONE);
                    lyNoData.setVisibility(View.VISIBLE);
                }
                loading = false;
            }
        });
    }

    @Override
    public void OnClick(String id, int position) {
        ReadMagazine(position);
    }

    private void ReadMagazine(int position) {
        try {
            Log.e("url", "" + magazineList.get(position).getUrl());
            Log.e("epub or not ?", "" + magazineList.get(position).getUrl().contains(".EPUB"));
            if (magazineList.get(position).getUrl().contains(".epub") || magazineList.get(position).getUrl().contains(".EPUB")) {

                DownloadEpub downloadEpub = new DownloadEpub(getActivity());
                Log.e("path_pr", "" + magazineList.get(position).getUrl());
                Log.e("path_pr_id", "" + magazineList.get(position).getId());
                downloadEpub.pathEpub(magazineList.get(position).getUrl(), magazineList.get(position).getId(), "magazine", false);

            } else if (magazineList.get(position).getUrl().contains(".pdf") || magazineList.get(position).getUrl().contains(".PDF")) {

                startActivity(new Intent(getActivity(), PDFShow.class)
                        .putExtra("link", "" + magazineList.get(position).getUrl())
                        .putExtra("toolbarTitle", "" + magazineList.get(position).getTitle())
                        .putExtra("type", "link"));
            }
        } catch (Exception e) {
            Log.e("Exception-Read", "" + e.getMessage());
        }
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        myPurchaseAdapter = new MyPurchaseAdapter(getActivity(),
                magazineList, "Magazine", "TRANSACTION", PurchaseMagazines.this, prefManager.getValue("currency_symbol"));
        rvDocuments.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        rvDocuments.setAdapter(myPurchaseAdapter);
        myPurchaseAdapter.notifyDataSetChanged();

        txtNoData.setVisibility(View.VISIBLE);
        txtNoData.setText("" + getResources().getString(R.string.no_magazines_available));
        Picasso.get().load(R.drawable.ic_no_docs).placeholder(R.drawable.ic_no_docs).into(ivNoData);

        Utils.Pagination(rvDocuments, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        PurchaseMagazine(page);
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

    @Override
    public void onPause() {
        super.onPause();
        if (paginate != null) {
            paginate.unbind();
        }
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
    public void onDestroy() {
        super.onDestroy();
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.shimmerHide(shimmer);
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
    }

}

