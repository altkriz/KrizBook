package com.kriztech.book.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.AuthorBookEdit;
import com.kriztech.book.Activity.AuthorBookUpload;
import com.kriztech.book.Activity.AuthorPortfolio;
import com.kriztech.book.Adapter.AuthorDocAdapter;
import com.kriztech.book.Interface.AddNewItem;
import com.kriztech.book.Interface.OnEditVisibilityClick;
import com.kriztech.book.Model.BookModel.BookModel;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorBooks extends Fragment implements OnEditVisibilityClick, AddNewItem, Paginate.Callbacks {

    private static final String TAG = AuthorBooks.class.getSimpleName();
    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private RecyclerView rvDocuments;
    private ImageView ivNoData;
    private TextView txtAddDocType, txtNoData;
    private LinearLayout lyContent, lyAddDocument, lyNoData;

    private List<Result> bookList;
    private AuthorDocAdapter authorDocAdapter;

    private boolean loading = false;
    private int page = 1, totalPages = 1;
    private Paginate paginate;
    private String authorID = "", authorStatus = "", visibilityStatus = "";

    public AuthorBooks(String authorID, String authorStatus) {
        this.authorID = authorID;
        this.authorStatus = authorStatus;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.subfragment_document, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());

        Log.e(TAG, "savedAuthorID ==>>> " + prefManager.getAuthorId());
        Log.e(TAG, "authorID ==>>> " + authorID);
        Log.e(TAG, "authorStatus ==>>> " + authorStatus);

        Init(root);

        bookList = new ArrayList<>();
        setupPagination();
        BooksByAuthor(page);

        lyAddDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDoc("Books");
            }
        });

        return root;
    }

    private void Init(View root) {
        try {
            shimmer = root.findViewById(R.id.shimmer);
            rvDocuments = root.findViewById(R.id.rvDocuments);
            lyContent = root.findViewById(R.id.lyContent);
            lyAddDocument = root.findViewById(R.id.lyAddDocument);
            txtAddDocType = root.findViewById(R.id.txtAddDocType);
            lyNoData = root.findViewById(R.id.lyNoData);
            txtNoData = root.findViewById(R.id.txtNoData);
            ivNoData = root.findViewById(R.id.ivNoData);
        } catch (Exception e) {
            Log.e("Init", "Exception => " + e);
        }
    }

    /* book_by_author API */
    private void BooksByAuthor(int pageNo) {
        Utils.shimmerShow(shimmer);

        Call<BookModel> call = BaseURL.getVideoAPI().book_by_author("" + authorID, "" + pageNo);
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    Log.e("book_by_author", "Status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        totalPages = response.body().getTotalPage();
                        Log.e("totalPages", "" + totalPages);

                        if (response.body().getResult().size() > 0) {
                            bookList = response.body().getResult();
                            Log.e("bookList", "" + bookList.size());

                            AuthorPortfolio.totalBooks = "" + bookList.size();
                            Log.e("totalBooks", "=>>> " + AuthorPortfolio.totalBooks);

                            lyContent.setVisibility(View.VISIBLE);
                            loading = false;
                            authorDocAdapter.addBook(bookList);

                            lyAddDocument.setVisibility(View.GONE);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            SetNoORAddDataLayout();
                            lyContent.setVisibility(View.GONE);
                            loading = false;
                        }
                    } else {
                        SetNoORAddDataLayout();
                        lyContent.setVisibility(View.GONE);
                        loading = false;
                        Log.e("book_by_author", "Message => " + response.body().getMessage());
                    }

                } catch (Exception e) {
                    Log.e("books_by_author", "Exception => " + e);
                    SetNoORAddDataLayout();
                    lyContent.setVisibility(View.GONE);
                    loading = false;
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Log.e("books_by_author", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
                if (!loading) {
                    SetNoORAddDataLayout();
                    lyContent.setVisibility(View.GONE);
                }
                loading = false;
            }
        });
    }

    private void SetNoORAddDataLayout() {
        Log.e("NoORAddDataLayout", "savedAuthorID ==>>> " + prefManager.getAuthorId());
        Log.e("NoORAddDataLayout", "authorID ==>>> " + authorID);
        if (prefManager.getAuthorId().equals("" + authorID)) {
            lyAddDocument.setVisibility(View.VISIBLE);
            lyNoData.setVisibility(View.GONE);
        } else {
            lyAddDocument.setVisibility(View.GONE);
            lyNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void OnEditClick(String id, int position) {
        Log.e("OnEditClick", "id ==>> " + id);
        Log.e("OnEditClick", "position ==>> " + position);

        if (Utils.checkLoginUser(getActivity())) {
            if (Utils.checkLoginAuthor(getActivity(), "AuthorProfile")) {
                Constant.isSelectPic = false;
                Intent intent = new Intent(getActivity(), AuthorBookEdit.class);
                intent.putExtra("docID", "" + id);
                startActivity(intent);
            }
        }
    }

    @Override
    public void OnVisibilityClick(String id, int position) {
        Log.e("OnVisibilityClick", "id ==>> " + id);
        Log.e("OnVisibilityClick", "position ==>> " + position);

        SetDocVisibilityDialog("" + id, position);
    }

    private void SetDocVisibilityDialog(String docID, int docPos) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.doc_visibility_set_dialog);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        final RadioGroup rgDocVisibility = bottomSheetDialog.findViewById(R.id.rgDocVisibility);
        final AppCompatRadioButton rcbActive = bottomSheetDialog.findViewById(R.id.rcbActive);
        final AppCompatRadioButton rcbInActive = bottomSheetDialog.findViewById(R.id.rcbInActive);
        final LinearLayout lyClickUpdate = bottomSheetDialog.findViewById(R.id.lyClickUpdate);
        final TextView txtVisibilityDesc = bottomSheetDialog.findViewById(R.id.txtVisibilityDesc);

        if (bookList.get(docPos).getStatus().equalsIgnoreCase("1")) {
            rcbActive.setChecked(true);
            rcbInActive.setChecked(false);
            txtVisibilityDesc.setText("" + getResources().getString(R.string.update_doc_visibility_desc_inactive));
        } else {
            rcbActive.setChecked(false);
            rcbInActive.setChecked(true);
            txtVisibilityDesc.setText("" + getResources().getString(R.string.update_doc_visibility_desc_active));
        }

        rgDocVisibility.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.e("checkedId", "=> " + checkedId);
                if (checkedId == R.id.rcbActive) {
                    visibilityStatus = "1";
                } else {
                    visibilityStatus = "0";
                }
                Log.e("visibilityStatus", "==> " + visibilityStatus);
            }
        });

        lyClickUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.setDismissWithAnimation(true);
                    bottomSheetDialog.dismiss();
                }

                Log.e("docID", "==> " + docID);
                Log.e("docPos", "==> " + docPos);
                Log.e("visibilityStatus", "==> " + visibilityStatus);
                bookList.get(docPos).setStatus("" + visibilityStatus);
                authorDocAdapter.notifyDataSetChanged();

                UpdateBookVisibility("" + docID, "" + visibilityStatus);
            }
        });

    }

    /* update_book_status API */
    private void UpdateBookVisibility(String bookID, String visibilityStatus) {
        Call<SuccessModel> call = BaseURL.getVideoAPI().update_book_status("" + prefManager.getAuthorId(), "" + bookID, "" + visibilityStatus);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("update_book_status", "Status ==>> " + response.body().getStatus());
                    Log.e("update_book_status", "Message ==>> " + response.body().getMessage());
                } catch (Exception e) {
                    Log.e("update_book_status", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("update_book_status", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        loading = false;

        authorDocAdapter = new AuthorDocAdapter(getActivity(), bookList, "Books", AuthorBooks.this, AuthorBooks.this,
                "" + prefManager.getAuthorId(), "" + authorID, "" + authorStatus);
        rvDocuments.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvDocuments.setAdapter(authorDocAdapter);
        authorDocAdapter.notifyDataSetChanged();

        txtAddDocType.setText("" + getResources().getString(R.string.add_books));
        txtNoData.setText("" + getResources().getString(R.string.no_books_available));
        Picasso.get().load(R.drawable.ic_no_docs).placeholder(R.drawable.ic_no_docs).into(ivNoData);

        Utils.Pagination(rvDocuments, this);
    }

    @Override
    public void onLoadMore() {
        Log.e("Paginate", "onLoadMore");
        loading = true;
        page++;
        BooksByAuthor(page);
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
    public void addNewDoc(String docType) {
        Log.e("addNewDoc", "docType ==>> " + docType);

        if (Utils.checkLoginAuthor(getActivity(), "AuthorProfile")) {
            Constant.isSelectPic = false;
            startActivity(new Intent(getActivity(), AuthorBookUpload.class));
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