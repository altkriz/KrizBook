package com.kriztech.book.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.WalletAdapter;
import com.kriztech.book.Model.WalletHistoryModel.Result;
import com.kriztech.book.Model.WalletHistoryModel.WalletHistoryModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;
import com.paginate.Paginate;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletHistory extends Fragment implements Paginate.Callbacks {

    private static final String TAG = WalletHistory.class.getSimpleName();
    private PrefManager prefManager;
    private View root;

    private ShimmerFrameLayout shimmer;
    private RelativeLayout rlAdView;
    private LinearLayout lyRecycler, lyNoData, lyFbAdView;
    private RecyclerView rvWalletHistory;

    private List<Result> rechargeList;
    private WalletAdapter walletAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    private AdView mAdView = null;
    private com.facebook.ads.AdView fbAdView = null;

    public WalletHistory() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.subfragment_wallethistory, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());

        init();
        AdInit();

        return root;
    }

    private void init() {
        try {
            prefManager = new PrefManager(getActivity());

            shimmer = root.findViewById(R.id.shimmer);
            lyNoData = root.findViewById(R.id.lyNoData);
            lyRecycler = root.findViewById(R.id.lyRecycler);
            rlAdView = root.findViewById(R.id.rlAdView);
            lyFbAdView = root.findViewById(R.id.lyFbAdView);

            rvWalletHistory = root.findViewById(R.id.rvWalletHistory);
        } catch (Exception e) {
            Log.e("WalletHistory", "init Exception =>" + e);
        }
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

    /* get_wallet_transaction API */
    private void RechargeHistory(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Call<WalletHistoryModel> call = BaseURL.getVideoAPI().get_wallet_transaction("" + prefManager.getLoginId(), "" + pageNo);
        call.enqueue(new Callback<WalletHistoryModel>() {
            @Override
            public void onResponse(@NonNull Call<WalletHistoryModel> call, @NonNull Response<WalletHistoryModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);
                        Log.e("get_wallet_transaction", "status => " + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            rechargeList = response.body().getResult();
                            Log.e("rechargeList", "" + rechargeList.size());

                            lyRecycler.setVisibility(View.VISIBLE);
                            loading = false;
                            walletAdapter.addData(rechargeList);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            lyRecycler.setVisibility(View.GONE);
                            loading = false;
                        }

                    } else {
                        Log.e("get_wallet_transaction", "massage => " + response.body().getMessage());
                        lyNoData.setVisibility(View.VISIBLE);
                        lyRecycler.setVisibility(View.GONE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("get_wallet_transaction", "Exception => " + e);
                    lyNoData.setVisibility(View.VISIBLE);
                    lyRecycler.setVisibility(View.GONE);
                    loading = false;
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(@NonNull Call<WalletHistoryModel> call, @NonNull Throwable t) {
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    lyNoData.setVisibility(View.VISIBLE);
                    lyRecycler.setVisibility(View.GONE);
                }
                loading = false;
                Log.e("get_wallet_transaction", "That didn't work!!! => " + t.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        rechargeList = new ArrayList<>();
        setupPagination();
        RechargeHistory(page);
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        walletAdapter = new WalletAdapter(getActivity(), rechargeList, "" + prefManager.getValue("currency_symbol"));
        rvWalletHistory.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        rvWalletHistory.setItemAnimator(new DefaultItemAnimator());
        rvWalletHistory.setAdapter(walletAdapter);
        walletAdapter.notifyDataSetChanged();

        Utils.Pagination(rvWalletHistory, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        RechargeHistory(page);
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