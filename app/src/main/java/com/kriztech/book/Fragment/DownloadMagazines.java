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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.PDFShow;
import com.kriztech.book.Adapter.MyDownloadsAdapter;
import com.kriztech.book.Interface.ItemClick;
import com.kriztech.book.Model.DownloadedItemModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.DownloadEpub;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;
import com.orhanobut.hawk.Hawk;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class DownloadMagazines extends Fragment implements ItemClick, Paginate.Callbacks {

    private static final String TAG = DownloadMagazines.class.getSimpleName();
    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;

    private ShimmerFrameLayout shimmer;
    private RecyclerView rvDocuments;
    private LinearLayout lyNoData, lyFbAdView;
    private TextView txtNoData;
    private ImageView ivNoData;
    private RelativeLayout rlAdView;

    //List<Result> magazineList;
    private List<DownloadedItemModel> magazineList = new ArrayList<>();
    private List<DownloadedItemModel> myMagazines;
    private MyDownloadsAdapter myDownloadsAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1, clickPosition;
    private Paginate paginate;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.subfragment_mydocuments, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        takePermissionUtils = new PermissionUtils(getActivity(), mPermissionResult);
        prefManager = new PrefManager(getActivity());

        shimmer = root.findViewById(R.id.shimmer);
        rlAdView = root.findViewById(R.id.rlAdView);
        lyFbAdView = root.findViewById(R.id.lyFbAdView);
        rvDocuments = root.findViewById(R.id.rvDocuments);
        lyNoData = root.findViewById(R.id.lyNoData);
        ivNoData = root.findViewById(R.id.ivNoData);
        txtNoData = root.findViewById(R.id.txtNoData);
        txtNoData.setVisibility(View.VISIBLE);
        txtNoData.setText("" + getResources().getString(R.string.no_magazines_available));
        Picasso.get().load(R.drawable.ic_no_docs).placeholder(R.drawable.ic_no_docs).into(ivNoData);

        AdInit();

        magazineList.clear();
        if (myMagazines == null) {
            magazineList = new ArrayList<>();
        }
        myMagazines = Hawk.get("my_magazines" + prefManager.getLoginId());
        Log.e("myMagazines", "" + myMagazines);

        if (myMagazines != null) {
            if (myMagazines.size() > 0) {
                for (int i = 0; i < myMagazines.size(); i++) {
                    magazineList.add(myMagazines.get(i));
                }
            }
            Log.e("=>myMagazines", "" + myMagazines.size());

            if (myMagazines.size() > 0) {
                myDownloadsAdapter = new MyDownloadsAdapter(getActivity(), magazineList, "Magazine", "DOWNLOAD",
                        DownloadMagazines.this, prefManager.getValue("currency_symbol"));
                rvDocuments.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                rvDocuments.setAdapter(myDownloadsAdapter);
                myDownloadsAdapter.notifyDataSetChanged();

                lyNoData.setVisibility(View.GONE);
                rvDocuments.setVisibility(View.VISIBLE);
            } else {
                lyNoData.setVisibility(View.VISIBLE);
                rvDocuments.setVisibility(View.GONE);
            }
        } else {
            lyNoData.setVisibility(View.VISIBLE);
            rvDocuments.setVisibility(View.GONE);
        }

//        setupPagination();
//        DownloadedMagazine(page);

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

    private void DownloadedMagazine(int pageNo) {
//        if (!loading) {
//            Utils.shimmerShow(shimmer);
//        }
//
//        Call<DownloadModel> call = BaseURL.getVideoAPI().alldownload(prefManager.getLoginId(), "magazine", "" + pageNo);
//        call.enqueue(new Callback<DownloadModel>() {
//            @Override
//            public void onResponse(Call<DownloadModel> call, Response<DownloadModel> response) {
//                try {
//                    if (response.code() == 200 && response.body().getStatus() == 200) {
//                        totalPages = response.body().getTotalPage();
//                        Log.e("totalPages", "" + totalPages);
//
//                        if (response.body().getResult().size() > 0) {
//                            magazineList = response.body().getResult();
//                            Log.e("magazineList", "" + magazineList.size());
//
//                            rvDocuments.setVisibility(View.VISIBLE);
//                            loading = false;
//                            myDownloadAdapter.addBook(magazineList);
//                            lyNoData.setVisibility(View.GONE);
//                        } else {
//                            rvDocuments.setVisibility(View.GONE);
//                            lyNoData.setVisibility(View.VISIBLE);
//                            loading = false;
//                        }
//
//                    } else {
//                        rvDocuments.setVisibility(View.GONE);
//                        lyNoData.setVisibility(View.VISIBLE);
//                        loading = false;
//                    }
//                } catch (Exception e) {
//                    Log.e("magazine download", "Exception => " + e);
//                }
//                Utils.shimmerHide(shimmer);
//            }
//
//            @Override
//            public void onFailure(Call<DownloadModel> call, Throwable t) {
//                Log.e("magazine download", "onFailure => " + t.getMessage());
//                Utils.shimmerHide(shimmer);
//                if (!loading) {
//                    rvDocuments.setVisibility(View.GONE);
//                    lyNoData.setVisibility(View.VISIBLE);
//                }
//                loading = false;
//            }
//        });
    }

    @Override
    public void OnClick(String id, int position) {
        clickPosition = position;
        Log.e("=>>id", "" + id);
        Log.e("=>>clickPosition", "" + clickPosition);

        if (Functions.isConnectedToInternet(getActivity())) {
            if (takePermissionUtils.isStoragePermissionGranted()) {
                ReadMagazines(position);
            } else {
                takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_video));
            }
        } else {
            Toasty.warning(getActivity(), "" + getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(), key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(getActivity(), getString(R.string.we_need_storage_permission_for_save_video));
                    } else if (allPermissionClear) {
                        Log.e("mPermissionResult", "isPermissionGranted => " + takePermissionUtils.isStoragePermissionGranted());
                        ReadMagazines(clickPosition);
                    }
                }
            });

    private void ReadMagazines(int position) {
        try {
            Log.e("url", "" + magazineList.get(position).getUrl());
            Log.e("epub or not ?", "" + magazineList.get(position).getUrl().contains(".EPUB"));
            if (magazineList.get(position).getUrl().contains(".epub") || magazineList.get(position).getUrl().contains(".EPUB")) {

                if (Utils.checkFileAvailability(getActivity(), "" + magazineList.get(position).getUrl(), "magazine")) {
                    DownloadEpub downloadEpub = new DownloadEpub(getActivity());
                    Log.e("path_pr", "" + magazineList.get(position).getUrl());
                    Log.e("path_pr_id", "" + magazineList.get(position).getId());
                    downloadEpub.pathEpub("" + magazineList.get(position).getUrl(), "" + magazineList.get(position).getId(), "magazine",
                            "" + magazineList.get(position).getSecretKey());
                } else {
                    DownloadEpub downloadEpub = new DownloadEpub(getActivity());
                    Log.e("path_pr", "" + magazineList.get(position).getOriginalUrl());
                    Log.e("path_pr_id", "" + magazineList.get(position).getId());
                    downloadEpub.pathEpub("" + magazineList.get(position).getOriginalUrl(), "" + magazineList.get(position).getId(), "magazine", false);
                }

            } else if (magazineList.get(position).getUrl().contains(".pdf") || magazineList.get(position).getUrl().contains(".PDF")) {
                if (Utils.checkFileAvailability(getActivity(), "" + magazineList.get(position).getUrl(), "magazine")) {
                    startActivity(new Intent(getActivity(), PDFShow.class)
                            .putExtra("link", "" + magazineList.get(position).getUrl())
                            .putExtra("toolbarTitle", "" + magazineList.get(position).getTitle())
                            .putExtra("secretKey", "" + magazineList.get(position).getSecretKey())
                            .putExtra("filePassword", "" + magazineList.get(position).getFilePassword())
                            .putExtra("type", "file"));
                } else {
                    startActivity(new Intent(getActivity(), PDFShow.class)
                            .putExtra("link", "" + magazineList.get(position).getOriginalUrl())
                            .putExtra("toolbarTitle", "" + magazineList.get(position).getTitle())
                            .putExtra("type", "link"));
                }
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

//        myDownloadNewAdapter = new MyDownloadAdapter(getActivity(),
//                magazineList, "Magazine", "DOWNLOAD", DownloadMagazines.this, prefManager.getValue("currency_symbol"));
//        rvDocuments.setLayoutManager(new GridLayoutManager(getActivity(), 1));
//        rvDocuments.setAdapter(myDownloadNewAdapter);
//        myDownloadNewAdapter.notifyDataSetChanged();

        Utils.Pagination(rvDocuments, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        DownloadedMagazine(page);
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