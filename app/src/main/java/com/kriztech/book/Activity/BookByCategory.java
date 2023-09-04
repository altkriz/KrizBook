package com.kriztech.book.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.CategoryBookAdapter;
import com.kriztech.book.Model.BookModel.BookModel;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
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

public class BookByCategory extends AppCompatActivity implements Paginate.Callbacks {

    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private TextView txtToolbarTitle, txtNoData;
    private LinearLayout lyToolbar, lyBack, lyFbAdView, lyNoData;
    private RecyclerView rv_booklist;
    private RelativeLayout rlAdView;
    private ImageView ivNoData;

    private List<Result> BookList;
    private CategoryBookAdapter categoryBookAdapter;

    private String cat_id, cat_name, cat_image;
    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(BookByCategory.this);
        setContentView(R.layout.activity_categorybooklist);
        PrefManager.forceRTLIfSupported(getWindow(), BookByCategory.this);
        prefManager = new PrefManager(BookByCategory.this);

        shimmer = findViewById(R.id.shimmer);
        rv_booklist = findViewById(R.id.rv_booklist);
        lyNoData = findViewById(R.id.lyNoData);
        ivNoData = findViewById(R.id.ivNoData);
        txtNoData = findViewById(R.id.txtNoData);

        rlAdView = findViewById(R.id.rlAdView);
        lyFbAdView = findViewById(R.id.lyFbAdView);
        lyToolbar = findViewById(R.id.lyToolbar);
        lyBack = findViewById(R.id.lyBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AdInit();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            cat_id = bundle.getString("cat_id");
            cat_name = bundle.getString("cat_name");
            cat_image = bundle.getString("cat_image");
            Log.e("cat_id", "" + cat_id);
            txtToolbarTitle.setText("" + cat_name);

            BookList = new ArrayList<>();
            setupPagination();
            books_by_category(page);
        }

    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(BookByCategory.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(BookByCategory.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    private void books_by_category(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Call<BookModel> call = BaseURL.getVideoAPI().books_by_category(cat_id, "" + pageNo);
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            BookList = response.body().getResult();
                            Log.e("BookList", "" + BookList.size());

                            rv_booklist.setVisibility(View.VISIBLE);
                            loading = false;
                            categoryBookAdapter.addBook(BookList);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            lyNoData.setVisibility(View.VISIBLE);
                            rv_booklist.setVisibility(View.GONE);
                            loading = false;
                        }
                    } else {
                        lyNoData.setVisibility(View.VISIBLE);
                        rv_booklist.setVisibility(View.GONE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("books_by_category", "Exception => " + e);
                    lyNoData.setVisibility(View.VISIBLE);
                    rv_booklist.setVisibility(View.GONE);
                    loading = false;
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Log.e("books_by_category", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    lyNoData.setVisibility(View.VISIBLE);
                    rv_booklist.setVisibility(View.GONE);
                }
                loading = false;
            }
        });
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        txtNoData.setVisibility(View.VISIBLE);
        txtNoData.setText("" + getResources().getString(R.string.no_books_available));
        Picasso.get().load(R.drawable.ic_no_docs).placeholder(R.drawable.ic_no_docs).into(ivNoData);

        categoryBookAdapter = new CategoryBookAdapter(BookByCategory.this, BookList);
        rv_booklist.setLayoutManager(new GridLayoutManager(BookByCategory.this, 3));
        rv_booklist.setAdapter(categoryBookAdapter);
        categoryBookAdapter.notifyDataSetChanged();

        Utils.Pagination(rv_booklist, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        books_by_category(page);
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
            fbAdView = null;
        }
    }

}
