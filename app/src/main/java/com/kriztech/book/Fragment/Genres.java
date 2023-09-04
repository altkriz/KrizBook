package com.kriztech.book.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.CategoryAdapter;
import com.kriztech.book.Model.CategoryModel.CategoryModel;
import com.kriztech.book.Model.CategoryModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.paginate.Paginate;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Genres extends Fragment implements Paginate.Callbacks {

    PrefManager prefManager;
    ShimmerFrameLayout shimmer;

    private View root;

    private LinearLayout ly_dataNotFound;
    private RecyclerView ry_category;
    List<Result> CategoryList;
    CategoryAdapter categoryAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    public Genres() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_genres, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());

        shimmer = root.findViewById(R.id.shimmer);

        ry_category = root.findViewById(R.id.ry_category);
        ly_dataNotFound = root.findViewById(R.id.ly_dataNotFound);

        CategoryList = new ArrayList<>();
        setupPagination();
        GetCategory(page);

        return root;
    }

    private void GetCategory(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Call<CategoryModel> call = BaseURL.getVideoAPI().categorylist("" + pageNo);
        call.enqueue(new Callback<CategoryModel>() {
            @Override
            public void onResponse(Call<CategoryModel> call, Response<CategoryModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
//                        totalPages = response.body().getTotalPage();
//                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            CategoryList = response.body().getResult();
                            Log.e("CategoryList", "" + CategoryList.size());

                            ry_category.setVisibility(View.VISIBLE);
                            loading = false;
                            categoryAdapter.addCategory(CategoryList);
                            ly_dataNotFound.setVisibility(View.GONE);
                        } else {
                            ry_category.setVisibility(View.GONE);
                            ly_dataNotFound.setVisibility(View.VISIBLE);
                            loading = false;
                        }

                    } else {
                        ry_category.setVisibility(View.GONE);
                        ly_dataNotFound.setVisibility(View.VISIBLE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("categorylist", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<CategoryModel> call, Throwable t) {
                Log.e("categorylist", "onFailure => " + t.getMessage());
                if (!loading) {
                    ry_category.setVisibility(View.GONE);
                    ly_dataNotFound.setVisibility(View.VISIBLE);
                }
                loading = false;
                Utils.shimmerHide(shimmer);
            }
        });
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        categoryAdapter = new CategoryAdapter(getActivity(), CategoryList, "Home");
        ry_category.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        ry_category.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();

        Utils.Pagination(ry_category, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        GetCategory(page);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.shimmerHide(shimmer);
    }

}
