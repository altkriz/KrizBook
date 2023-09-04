package com.kriztech.book.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.BookmarkAdapter;
import com.kriztech.book.Interface.ItemClick;
import com.kriztech.book.Model.BookmarkModel.BookmarkModel;
import com.kriztech.book.Model.BookmarkModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookMarkBooks extends Fragment implements ItemClick, Paginate.Callbacks {

    private static final String TAG = BookMarkBooks.class.getSimpleName();
    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private RecyclerView rvDocuments;
    private ImageView ivNoData;
    private TextView txtNoData;
    private LinearLayout lyNoData, lyContent;

    private List<Result> bookList;
    private BookmarkAdapter bookmarkAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.subfragment_bookmark, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());

        shimmer = root.findViewById(R.id.shimmer);
        rvDocuments = root.findViewById(R.id.rvDocuments);
        ivNoData = root.findViewById(R.id.ivNoData);
        txtNoData = root.findViewById(R.id.txtNoData);
        lyNoData = root.findViewById(R.id.lyNoData);
        lyContent = root.findViewById(R.id.lyContent);

        bookList = new ArrayList<>();
        setupPagination();
        BookmarkBooks(page);

        return root;
    }

    private void BookmarkBooks(int pageNo) {
        if (!loading) {
            Utils.shimmerShow(shimmer);
        }

        Call<BookmarkModel> call = BaseURL.getVideoAPI().allBookmark(prefManager.getLoginId(), "book", "" + pageNo);
        call.enqueue(new Callback<BookmarkModel>() {
            @Override
            public void onResponse(Call<BookmarkModel> call, Response<BookmarkModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            bookList = response.body().getResult();
                            Log.e("bookList", "" + bookList.size());

                            lyContent.setVisibility(View.VISIBLE);
                            loading = false;
                            bookmarkAdapter.addBook(bookList);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            lyContent.setVisibility(View.GONE);
                            lyNoData.setVisibility(View.VISIBLE);
                            loading = false;
                        }

                    } else {
                        lyContent.setVisibility(View.GONE);
                        lyNoData.setVisibility(View.VISIBLE);
                        loading = false;
                    }
                } catch (Exception e) {
                    Log.e("book bookmark", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookmarkModel> call, Throwable t) {
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    lyContent.setVisibility(View.GONE);
                    lyNoData.setVisibility(View.VISIBLE);
                }
                loading = false;
            }
        });
    }

    @Override
    public void OnClick(String id, int position) {
        Log.e("id", "" + id);
        Log.e("position", "" + position);

        RemoveBookMark(id);
    }

    private void RemoveBookMark(String ID) {
        Utils.ProgressBarShow(getActivity());

        Call<SuccessModel> call = BaseURL.getVideoAPI().add_bookmark("" + prefManager.getLoginId(), "" + ID);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    Toasty.success(getActivity(), "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    bookList = new ArrayList<>();
                    setupPagination();
                    BookmarkBooks(1);
                } catch (Exception e) {
                    Log.e("add_bookmark", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("add_bookmark", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        bookmarkAdapter = new BookmarkAdapter(getActivity(), bookList, "Book", BookMarkBooks.this);
        rvDocuments.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvDocuments.setAdapter(bookmarkAdapter);
        bookmarkAdapter.notifyDataSetChanged();

        txtNoData.setVisibility(View.VISIBLE);
        txtNoData.setText("" + getResources().getString(R.string.no_books_available));
        Picasso.get().load(R.drawable.ic_no_docs).placeholder(R.drawable.ic_no_docs).into(ivNoData);

        Utils.Pagination(rvDocuments, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        BookmarkBooks(page);
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
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (paginate != null) {
            paginate.unbind();
        }
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

}