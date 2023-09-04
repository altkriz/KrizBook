package com.kriztech.book.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.kriztech.book.Model.BookModel.BookModel;
import com.kriztech.book.Model.CategoryModel.CategoryModel;
import com.kriztech.book.Model.CategoryModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.DownloadEpub;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.ScalingUtilities;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorBookEdit extends AppCompatActivity implements View.OnClickListener {

    private PrefManager prefManager;
    private PermissionUtils takeDocPermissionUtils, takeCoverPermissionUtils;
    private static final String TAG = AuthorBookEdit.class.getSimpleName();

    private ShimmerFrameLayout shimmer;
    private TextView txtBack, txtToolbarTitle, txtSampleBookName, txtFullBookName, txtBookCoverName;
    private EditText edtBooktitle, edtBookDesc, edtBookPrice;
    private Spinner spinCategory;
    private LinearLayout lyToolbar, lyBack, lyUploadSample, lyEmptySampleBook, lySelectedSampleBook, lyRemoveSampleBook, lyViewSampleBook,
            lyUploadFull, lyEmptyFullBook, lySelectedFullBook, lyRemoveFullBook, lyViewFullBook, lyBookCost,
            lyUploadCover, lyEmptyCover, lySelectedCover, lyRemoveCover, lySubmit;
    private RadioGroup rgBookCost;
    private RadioButton rbPaid, rbFree;

    private List<com.kriztech.book.Model.BookModel.Result> bookDetailsList;

    private ArrayAdapter categoryAdapter;
    private List<Result> categoryList;
    private ArrayList<String> allCategory;

    private static final int PICKFILE_RESULT_CODE = 248;
    private String docID = "", strBookCover = "", strCategoryID = "", strBooktitle = "", strBookDesc = "", strBookPrice = "", strIsPaid, strSampleBook, strFullBook,
            clickType = "", viewType = "";
    private RequestBody bookID, authorID, title, categoryID, description, isPaid, price;
    private MultipartBody.Part sampleBook, fullBook, coverPoster;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(AuthorBookEdit.this);
        setContentView(R.layout.activity_author_book_upload);
        PrefManager.forceRTLIfSupported(getWindow(), AuthorBookEdit.this);
        prefManager = new PrefManager(AuthorBookEdit.this);
        takeDocPermissionUtils = new PermissionUtils(AuthorBookEdit.this, mDocPermissionResult);
        takeCoverPermissionUtils = new PermissionUtils(AuthorBookEdit.this, mCoverPermissionResult);

        Init();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            docID = bundle.getString("docID");
            Log.e("docID", "==>>> " + docID);
        }

        GetBookDetails();
        txtToolbarTitle.setText("" + getResources().getString(R.string.update_book));

        rgBookCost.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPaid) {
                    strIsPaid = "1";
                    lyBookCost.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rbFree) {
                    strIsPaid = "0";
                    lyBookCost.setVisibility(View.GONE);
                }
            }
        });

    }

    private void Init() {
        try {
            shimmer = findViewById(R.id.shimmer);

            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

            lySubmit = findViewById(R.id.lySubmit);
            txtSampleBookName = findViewById(R.id.txtSampleBookName);
            txtFullBookName = findViewById(R.id.txtFullBookName);
            txtBookCoverName = findViewById(R.id.txtBookCoverName);
            spinCategory = findViewById(R.id.spinCategory);

            rgBookCost = findViewById(R.id.rgBookCost);
            rbPaid = findViewById(R.id.rbPaid);
            rbFree = findViewById(R.id.rbFree);
            lyBookCost = findViewById(R.id.lyBookCost);

            edtBooktitle = findViewById(R.id.edtBookTitle);
            edtBookDesc = findViewById(R.id.edtBookDesc);
            edtBookPrice = findViewById(R.id.edtBookPrice);

            lyUploadSample = findViewById(R.id.lyUploadSample);
            lyEmptySampleBook = findViewById(R.id.lyEmptySampleBook);
            lySelectedSampleBook = findViewById(R.id.lySelectedSampleBook);
            lyRemoveSampleBook = findViewById(R.id.lyRemoveSampleBook);
            lyViewSampleBook = findViewById(R.id.lyViewSampleBook);

            lyUploadFull = findViewById(R.id.lyUploadFull);
            lyEmptyFullBook = findViewById(R.id.lyEmptyFullBook);
            lySelectedFullBook = findViewById(R.id.lySelectedFullBook);
            lyRemoveFullBook = findViewById(R.id.lyRemoveFullBook);
            lyViewFullBook = findViewById(R.id.lyViewFullBook);

            lyUploadCover = findViewById(R.id.lyUploadCover);
            lyEmptyCover = findViewById(R.id.lyEmptyCover);
            lySelectedCover = findViewById(R.id.lySelectedCover);
            lyRemoveCover = findViewById(R.id.lyRemoveCover);

            lyBack.setOnClickListener(this);
            lyEmptySampleBook.setOnClickListener(this);
            lyEmptyFullBook.setOnClickListener(this);
            lyEmptyCover.setOnClickListener(this);
            lyRemoveSampleBook.setOnClickListener(this);
            lyRemoveFullBook.setOnClickListener(this);
            lyRemoveCover.setOnClickListener(this);
            lySubmit.setOnClickListener(this);
            lyViewSampleBook.setOnClickListener(this);
            lyViewFullBook.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("Init", "Exception => " + e);
        }
    }

    /* bookdetails API */
    private void GetBookDetails() {
        Utils.shimmerShow(shimmer);

        Call<BookModel> call = BaseURL.getVideoAPI().bookdetails("" + docID, "" + prefManager.getLoginId());
        call.enqueue(new Callback<BookModel>() {
            @Override
            public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult() != null) {
                            if (response.body().getResult().size() > 0) {

                                bookDetailsList = new ArrayList<>();
                                bookDetailsList = response.body().getResult();

                                GetCategory();

                                Log.e("IsBuy =>", "" + bookDetailsList.get(0).getIsBuy());
                                Log.e("IsPaid =>", "" + bookDetailsList.get(0).getIsPaid());
                                Log.e("Doc", "AuthorId => " + bookDetailsList.get(0).getAuthorId());
                                Log.e("AuthorId =>", "" + prefManager.getAuthorId());

                                edtBooktitle.setText("" + bookDetailsList.get(0).getTitle());
                                edtBookDesc.setText(Html.fromHtml(bookDetailsList.get(0).getDescription()));

                                strBooktitle = "" + bookDetailsList.get(0).getTitle();
                                strBookDesc = "" + bookDetailsList.get(0).getDescription();

                                if (bookDetailsList.get(0).getIsPaid().equalsIgnoreCase("1")) {
                                    rbPaid.setChecked(true);
                                    rbFree.setChecked(false);
                                    lyBookCost.setVisibility(View.VISIBLE);
                                    edtBookPrice.setText("" + bookDetailsList.get(0).getPrice());
                                    strBookPrice = "" + bookDetailsList.get(0).getPrice();
                                    strIsPaid = "1";
                                } else {
                                    rbPaid.setChecked(false);
                                    rbFree.setChecked(true);
                                    lyBookCost.setVisibility(View.GONE);
                                    strBookPrice = "";
                                    strIsPaid = "0";
                                }

                                if (!TextUtils.isEmpty(bookDetailsList.get(0).getSampleUrl())) {
                                    lyEmptySampleBook.setVisibility(View.GONE);
                                    lySelectedSampleBook.setVisibility(View.VISIBLE);
                                    txtSampleBookName.setText("" + Utils.getFileNameFromPath(bookDetailsList.get(0).getSampleUrl()));
                                } else {
                                    lyEmptySampleBook.setVisibility(View.VISIBLE);
                                    lySelectedSampleBook.setVisibility(View.GONE);
                                }

                                if (!TextUtils.isEmpty(bookDetailsList.get(0).getUrl())) {
                                    lyEmptyFullBook.setVisibility(View.GONE);
                                    lySelectedFullBook.setVisibility(View.VISIBLE);
                                    txtFullBookName.setText("" + Utils.getFileNameFromPath(bookDetailsList.get(0).getUrl()));
                                } else {
                                    lyEmptyFullBook.setVisibility(View.VISIBLE);
                                    lySelectedFullBook.setVisibility(View.GONE);
                                }

                                if (!TextUtils.isEmpty(bookDetailsList.get(0).getImage())) {
                                    lyEmptyCover.setVisibility(View.GONE);
                                    lySelectedCover.setVisibility(View.VISIBLE);
                                    txtBookCoverName.setText("" + Utils.getFileNameFromPath(bookDetailsList.get(0).getImage()));
                                } else {
                                    lyEmptySampleBook.setVisibility(View.VISIBLE);
                                    lySelectedSampleBook.setVisibility(View.GONE);
                                }

                            } else {
                                Log.e("bookdetails", "Message => " + response.body().getMessage());
                            }
                        } else {
                            Log.e("bookdetails", "Message ==> " + response.body().getMessage());
                        }

                    } else {
                        Log.e("bookdetails", "Message ===> " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("bookdetails", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<BookModel> call, Throwable t) {
                Log.e("bookdetails", "onFailure => " + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    /* categorylist API */
    private void GetCategory() {

        Call<CategoryModel> call = BaseURL.getVideoAPI().categorylist("");
        call.enqueue(new Callback<CategoryModel>() {
            @Override
            public void onResponse(Call<CategoryModel> call, Response<CategoryModel> response) {
                try {
                    Log.e("categorylist", "Status ==>> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        allCategory = new ArrayList<>();

                        if (response.body().getResult().size() > 0) {
                            categoryList = new ArrayList<>();
                            categoryList = response.body().getResult();
                            Log.e("categoryList", "size ==>> " + categoryList.size());

                            for (int i = 0; i < categoryList.size(); i++) {
                                allCategory.add(categoryList.get(i).getName());
                            }
                            Log.e("allCategory", "size ==>> " + allCategory.size());

                            SetCategorySpinner();
                        } else {
                            Log.e("categorylist", "Message => " + response.body().getMessage());
                        }

                    } else {
                        Log.e("categorylist", "Message ==>> " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("categorylist", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<CategoryModel> call, Throwable t) {
                Log.e("categorylist", "onFailure => " + t.getMessage());
            }
        });
    }

    private void SetCategorySpinner() {
        categoryAdapter = new ArrayAdapter(AuthorBookEdit.this, R.layout.spinner_item, allCategory);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinCategory.setAdapter(categoryAdapter);

        if (!TextUtils.isEmpty(bookDetailsList.get(0).getCategoryName())) {
            int spinnerPosition = categoryAdapter.getPosition(bookDetailsList.get(0).getCategoryName());
            Log.e("spinner", "pos ==> " + spinnerPosition);
            spinCategory.setSelection(spinnerPosition);
        }
        categoryAdapter.notifyDataSetChanged();

        spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("position", "==> " + position);
                Log.e("Category", "Spinner ==> " + spinCategory.getSelectedItem());
                Log.e("Category", "List ==> " + categoryList.get(position).getName());
                if (spinCategory.getSelectedItem().toString().equalsIgnoreCase("" + categoryList.get(position).getName())) {
                    strCategoryID = categoryList.get(position).getId();
                    Log.e("CategoryID", "==> " + strCategoryID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyBack:
                finish();
                break;

            case R.id.lyEmptySampleBook:
                viewType = "SelectBook";
                clickType = "SampleBook";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectBook();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyFullBook:
                viewType = "SelectBook";
                clickType = "FullBook";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectBook();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyCover:
                if (Utils.checkLoginUser(AuthorBookEdit.this)) {
                    if (takeCoverPermissionUtils.isStorageCameraPermissionGranted()) {
                        Constant.isSelectPic = true;
                        selectImage();
                    } else {
                        takeCoverPermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    }
                }
                break;

            case R.id.lyViewSampleBook:
                viewType = "ViewSample";
                if (Utils.checkLoginUser(AuthorBookEdit.this)) {
                    if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                        ClickToOpenBook();
                    } else {
                        takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_to_read_document_file));
                    }
                }
                break;

            case R.id.lyViewFullBook:
                viewType = "ViewFull";
                if (Utils.checkLoginUser(AuthorBookEdit.this)) {
                    if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                        ClickToOpenBook();
                    } else {
                        takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_to_read_document_file));
                    }
                }
                break;

            case R.id.lyRemoveSampleBook:
                lyEmptySampleBook.setVisibility(View.VISIBLE);
                lySelectedSampleBook.setVisibility(View.GONE);
                txtSampleBookName.setText("");
                strSampleBook = "";
                break;

            case R.id.lyRemoveFullBook:
                lyEmptyFullBook.setVisibility(View.VISIBLE);
                lySelectedFullBook.setVisibility(View.GONE);
                txtFullBookName.setText("");
                strFullBook = "";
                break;

            case R.id.lyRemoveCover:
                lyEmptyCover.setVisibility(View.VISIBLE);
                lySelectedCover.setVisibility(View.GONE);
                txtBookCoverName.setText("");
                strBookCover = "";
                break;

            case R.id.lySubmit:
                ValidateBookDetails();
                break;
        }
    }

    private void selectBook() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("application/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    private final ActivityResultLauncher<String[]> mDocPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorBookEdit.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorBookEdit.this, getString(R.string.we_need_storage_and_upload_document_file));
                    } else if (allPermissionClear) {
                        if (viewType.equalsIgnoreCase("SelectBook")) {
                            Constant.isSelectPic = true;
                            selectBook();
                        } else {
                            ClickToOpenBook();
                        }
                    }
                }
            });

    private void ValidateBookDetails() {
        strBooktitle = "" + edtBooktitle.getText().toString().trim();
        strBookPrice = "" + edtBookPrice.getText().toString().trim();
        strBookDesc = "" + edtBookDesc.getText().toString().trim();

        if (TextUtils.isEmpty(strBooktitle) && (rbPaid.isChecked() && TextUtils.isEmpty(strBookPrice)) && TextUtils.isEmpty(strBookDesc)) {
            Toasty.warning(AuthorBookEdit.this, "" + getResources().getString(R.string.fill_up_all_fields), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBooktitle)) {
            Toasty.warning(AuthorBookEdit.this, "" + getResources().getString(R.string.enter_book_title), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strCategoryID)) {
            Toasty.warning(AuthorBookEdit.this, "" + getResources().getString(R.string.select_book_category), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (rbPaid.isChecked() && TextUtils.isEmpty(strBookPrice)) {
            Toasty.warning(AuthorBookEdit.this, "" + getResources().getString(R.string.enter_book_cost), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBookDesc)) {
            Toasty.warning(AuthorBookEdit.this, "" + getResources().getString(R.string.enter_book_description), Toasty.LENGTH_SHORT).show();
            return;
        }

        Log.e("Validate", "strBooktitle :==> " + strSampleBook);
        Log.e("Validate", "strBookPrice :==> " + strFullBook);
        Log.e("Validate", "strBookDesc :==> " + strBookCover);
        Log.e("=============", "===============");
        Log.e("Validate", "SampleBook :==> " + strSampleBook);
        Log.e("Validate", "FullBook :==> " + strFullBook);
        Log.e("Validate", "BookCover :==> " + strBookCover);
        Log.e("=============", "===============");

        if (!TextUtils.isEmpty(strSampleBook) || !TextUtils.isEmpty(strFullBook) || !TextUtils.isEmpty(strBookCover)) {
            UpdateBookWithDocs();
        } else {
            UpdateBook();
        }
    }

    /* author_book_edit API with Documents & Cover Image */
    private void UpdateBookWithDocs() {

        Log.e(TAG, "AuthorId :==> " + prefManager.getAuthorId());
        Log.e(TAG, "strCategoryID :==> " + strCategoryID);
        Log.e(TAG, "strIsPaid :==> " + strIsPaid);
        Log.e(TAG, "strBookPrice :==> " + strBookPrice);
        Log.e("=============", "===============");
        Log.e(TAG, "SampleBook :==> " + strSampleBook);
        Log.e(TAG, "FullBook :==> " + strFullBook);
        Log.e(TAG, "BookCover :==> " + strBookCover);
        Log.e("===============", "===============");

        if (!TextUtils.isEmpty(strSampleBook)) {
            File fileSampleBook = new File(strSampleBook);
            // Parsing any Media type file
            RequestBody rbSampleBook = RequestBody.create(MediaType.parse("*/*"), fileSampleBook);
            sampleBook = MultipartBody.Part.createFormData("sample_url", fileSampleBook.getName(), rbSampleBook);
        } else {
            RequestBody rbSampleBook = RequestBody.create(MediaType.parse("*/*"), "");
            sampleBook = MultipartBody.Part.createFormData("sample_url", "", rbSampleBook);
        }

        if (!TextUtils.isEmpty(strFullBook)) {
            File fileFullBook = new File(strFullBook);
            RequestBody rbFullBook = RequestBody.create(MediaType.parse("*/*"), fileFullBook);
            fullBook = MultipartBody.Part.createFormData("full_book", fileFullBook.getName(), rbFullBook);
        } else {
            RequestBody rbFullBook = RequestBody.create(MediaType.parse("*/*"), "");
            fullBook = MultipartBody.Part.createFormData("full_book", "", rbFullBook);
        }

        if (!TextUtils.isEmpty(strBookCover)) {
            File fileBookCover = new File(strBookCover);
            RequestBody rbBookCover = RequestBody.create(MediaType.parse("image/*"), fileBookCover);
            coverPoster = MultipartBody.Part.createFormData("image", fileBookCover.getName(), rbBookCover);
        } else {
            RequestBody rbBookCover = RequestBody.create(MediaType.parse("image/*"), "");
            coverPoster = MultipartBody.Part.createFormData("image", "", rbBookCover);
        }

        bookID = RequestBody.create(MediaType.parse("text/plain"), "" + docID);
        authorID = RequestBody.create(MediaType.parse("text/plain"), "" + prefManager.getAuthorId());
        title = RequestBody.create(MediaType.parse("text/plain"), "" + strBooktitle);
        categoryID = RequestBody.create(MediaType.parse("text/plain"), "" + strCategoryID);
        description = RequestBody.create(MediaType.parse("text/plain"), "" + strBookDesc);
        isPaid = RequestBody.create(MediaType.parse("text/plain"), "" + strIsPaid);
        price = RequestBody.create(MediaType.parse("text/plain"), "" + strBookPrice);

        Utils.ProgressBarShow(AuthorBookEdit.this);
        Call<SuccessModel> call = BaseURL.getVideoAPI().update_book(bookID, authorID, title, categoryID, description, isPaid, price, sampleBook, fullBook, coverPoster);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("author_book_edit", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("author_book_edit", "message ==> " + response.body().getMessage());
                        Toasty.success(AuthorBookEdit.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                        Constant.isSelectPic = false;
                        finish();
                    } else {
                        Log.e("author_book_edit", "message ==> " + response.body().getMessage());
                        Toasty.info(AuthorBookEdit.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("author_book_edit", "Exception ==> " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("author_book_edit", "onFailure ==> " + t.getMessage());
            }
        });

    }

    /* author_book_edit API */
    private void UpdateBook() {

        Log.e(TAG, "AuthorId :==> " + prefManager.getAuthorId());
        Log.e(TAG, "strCategoryID :==> " + strCategoryID);
        Log.e(TAG, "strIsPaid :==> " + strIsPaid);
        Log.e(TAG, "strBookPrice :==> " + strBookPrice);

        Utils.ProgressBarShow(AuthorBookEdit.this);
        Call<SuccessModel> call = BaseURL.getVideoAPI().update_book("" + docID, "" + prefManager.getAuthorId(), "" + strBooktitle, "" + strCategoryID,
                "" + strBookDesc, "" + strIsPaid, "" + strBookPrice);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("author_book_edit", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("author_book_edit", "message ==> " + response.body().getMessage());
                        Toasty.success(AuthorBookEdit.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                        Constant.isSelectPic = false;
                        finish();
                    } else {
                        Log.e("author_book_edit", "message ==> " + response.body().getMessage());
                        Toasty.info(AuthorBookEdit.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("author_book_edit", "Exception ==> " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("author_book_edit", "onFailure ==> " + t.getMessage());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("resultCode", "onActivityResult: " + resultCode);
        Log.e("requestcode", "onActivityResult: " + requestCode);

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    Log.e("uriPath", "onActivityResult :=> " + selectedFileUri.getPath());
                    try {
                        File fileSelected;
                        fileSelected = new File(selectedFileUri.getPath());
                        Log.e("filePath", "onActivityResult :=> " + fileSelected.getPath());

                        if (clickType.equalsIgnoreCase("SampleBook")) {
                            strSampleBook = "" + Utils.getDocumentPath(AuthorBookEdit.this, selectedFileUri);
                            Log.e("onActivityResult", "SampleBook :=> " + strSampleBook);

                            lyEmptySampleBook.setVisibility(View.GONE);
                            lySelectedSampleBook.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorBookEdit.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorBookEdit.this, selectedFileUri).endsWith(".epub")) {
                                txtSampleBookName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtSampleBookName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorBookEdit.this, selectedFileUri));
                            }

                        } else if (clickType.equalsIgnoreCase("FullBook")) {
                            strFullBook = "" + Utils.getDocumentPath(AuthorBookEdit.this, selectedFileUri);
                            Log.e("onActivityResult", "FullBook :=> " + strFullBook);

                            lyEmptyFullBook.setVisibility(View.GONE);
                            lySelectedFullBook.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorBookEdit.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorBookEdit.this, selectedFileUri).endsWith(".epub")) {
                                txtFullBookName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtFullBookName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorBookEdit.this, selectedFileUri));
                            }

                        }

                    } catch (Exception e) {
                        Log.e("onActivityResult", "File select error =>> " + e);
                    }
                }
            }
        }
    }

    /* View Your Uploads */
    private void ClickToOpenBook() {
        try {
            if (Functions.isConnectedToInternet(AuthorBookEdit.this)) {
                Log.e(TAG, "viewType ==>> " + viewType);

                if (viewType.equalsIgnoreCase("ViewSample")) {
                    Log.e(TAG, "sampleURL ==>> " + bookDetailsList.get(0).getSampleUrl());
                    if (bookDetailsList.get(0).getSampleUrl().contains(".epub") || bookDetailsList.get(0).getSampleUrl().contains(".EPUB")) {
                        DownloadEpub downloadEpub = new DownloadEpub(AuthorBookEdit.this);
                        downloadEpub.pathEpub(bookDetailsList, new ArrayList<>(), "samplebook");

                    } else if (bookDetailsList.get(0).getSampleUrl().contains(".pdf") || bookDetailsList.get(0).getSampleUrl().contains(".PDF")) {
                        startActivity(new Intent(AuthorBookEdit.this, PDFShow.class)
                                .putExtra("link", bookDetailsList.get(0).getSampleUrl())
                                .putExtra("toolbarTitle", bookDetailsList.get(0).getTitle())
                                .putExtra("type", "link"));
                    }

                } else if (viewType.equalsIgnoreCase("ViewFull")) {
                    Log.e(TAG, "URL ==>> " + bookDetailsList.get(0).getUrl());
                    if (bookDetailsList.get(0).getUrl().contains(".epub") || bookDetailsList.get(0).getUrl().contains(".EPUB")) {
                        DownloadEpub downloadEpub = new DownloadEpub(AuthorBookEdit.this);
                        downloadEpub.pathEpub(bookDetailsList, new ArrayList<>(), "book");

                    } else if (bookDetailsList.get(0).getUrl().contains(".pdf") || bookDetailsList.get(0).getUrl().contains(".PDF")) {
                        startActivity(new Intent(AuthorBookEdit.this, PDFShow.class)
                                .putExtra("link", bookDetailsList.get(0).getUrl())
                                .putExtra("toolbarTitle", bookDetailsList.get(0).getTitle())
                                .putExtra("type", "link"));
                    }
                }

            } else {
                Toasty.info(AuthorBookEdit.this, getResources().getString(R.string.internet_connection), Toasty.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Exception-Read", "" + e.getMessage());
        }
    }

    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel_)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AuthorBookEdit.this, R.style.AlertDialogDanger);
        builder.setTitle(getString(R.string.add_photo));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(getString(R.string.take_photo))) {
                    openCameraIntent();
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    resultCallbackForGallery.launch(intent);
                } else if (options[item].equals(getString(R.string.cancel_))) {
                    Constant.isSelectPic = false;
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private final ActivityResultLauncher<String[]> mCoverPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorBookEdit.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorBookEdit.this, getString(R.string.we_need_storage_and_upload_document_file));
                    } else if (allPermissionClear) {
                        Constant.isSelectPic = true;
                        selectImage();
                    }
                }
            });

    ActivityResultLauncher<Intent> resultCallbackForGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("CallbackForGallery", "result => " + result.getData());
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        beginCrop(selectedImage);
                    }
                }
            });

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Log.e("photoFile", "Exception => " + ex);
            }
            if (photoFile != null) {
                Constant.isSelectPic = true;
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                resultCallbackForCamera.launch(pictureIntent);
            }
        }
    }

    private void beginCrop(Uri source) {
        Intent intent = CropImage.activity(source).setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1, 2).getIntent(AuthorBookEdit.this);
        resultCallbackForCrop.launch(intent);
    }

    ActivityResultLauncher<Intent> resultCallbackForCrop = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("CallbackForCrop", "result => " + result.getData());
                        Intent data = result.getData();
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
                        Log.e("imageName", "onActivityResult: " + cropResult.getUri().getPath());
                        String path = cropResult.getUri().getPath();
                        String filename = path.substring(path.lastIndexOf("/") + 1);

                        lyEmptyCover.setVisibility(View.GONE);
                        lySelectedCover.setVisibility(View.VISIBLE);
                        txtBookCoverName.setText(filename);
                        //   Log.e("strBookCover", "" + strBookCover);
                        // get the image uri after the image crope and resize it
                        decodeFile(getRealPathFromURI(cropResult.getUri()), Constant.PROFILE_IMAGE_SIZE, Constant.PROFILE_IMAGE_SIZE);
                    }
                }
            });

    private String getRealPathFromURI(Uri contentURI) {
        @SuppressLint("Recycle") Cursor cursor = AuthorBookEdit.this.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    ActivityResultLauncher<Intent> resultCallbackForCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e("=>resultCode", "" + result.getResultCode());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("CallbackForCamera", "result => " + result.getData());
                        Matrix matrix = new Matrix();
                        try {
                            Log.e("imageFilePath", "" + imageFilePath);
                            android.media.ExifInterface exif = new android.media.ExifInterface(imageFilePath);
                            int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                            switch (orientation) {
                                case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                                    matrix.postRotate(90);
                                    break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                                    matrix.postRotate(180);
                                    break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                                    matrix.postRotate(270);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Uri selectedImage = (Uri.fromFile(new File(imageFilePath)));
                        beginCrop(selectedImage);
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Constant.isSelectPic = false;
                    }
                }
            });

    private String decodeFile(String path, int desiredWidth, int desiredHeight) {
        Log.e("path", "==> " + path);
        Log.e("desiredWidth", "==> " + desiredWidth);
        Log.e("desiredHeight", "==> " + desiredHeight);

        String strMyImagePath = null;

        Bitmap scaledBitmap = null;
        if (path.contains(".png")) {
            Log.e("png path ==>", "" + strMyImagePath);
            try {
                // Part 1: Decode image
                Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);

                assert unscaledBitmap != null;
                if (!(unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight)) {
                    // Part 2: Scale image
                    scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);
                } else if (unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight) {
                    strBookCover = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                // Store to tmp file
                String extr = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(AuthorBookEdit.this);
                } else {
                    extr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Image/";
                }
                Log.e("jpeg", "ExternalStorageDirectory => " + extr);
                File mFolder = new File(extr);
                if (!mFolder.exists()) {
                    mFolder.mkdir();
                    Log.e("jpeg", "mFolder.mkdir() => " + mFolder.mkdir());
                }

                String s = "tmp.png";
                File f = new File(mFolder.getAbsolutePath(), s);

                strMyImagePath = f.getAbsolutePath();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 75, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("png", "FileNotFoundException => " + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("png Exception ==>", "" + e);
                    e.printStackTrace();
                }

                scaledBitmap.recycle();
            } catch (Exception e) {
                Log.e("Upload pic in PNG", "Exception => " + e);
            }

            //AddProfileImg(strMyImagePath);

        } else {
            Log.e("jpeg path ==>", "" + strMyImagePath);
            try {
                // Part 1: Decode image
                Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);

                if (!(unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight)) {
                    // Part 2: Scale image
                    scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);
                } else if (unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight) {
                    strBookCover = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                String extr = "";
                // Store to tmp file
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(AuthorBookEdit.this);
                } else {
                    extr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Image/";
                }
                Log.e("jpeg", "ExternalStorageDirectory => " + extr);
                File mFolder = new File(extr);
                if (!mFolder.exists()) {
                    mFolder.mkdir();
                    Log.e("jpeg", "mFolder.mkdir() => " + mFolder.mkdir());
                }

                String s = "tmp.jpeg";
                File f = new File(mFolder.getAbsolutePath(), s);

                strMyImagePath = f.getAbsolutePath();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e("jpeg", "FileNotFoundException ==> " + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("jpeg", "Exception => " + e);
                    e.printStackTrace();
                }

                scaledBitmap.recycle();
            } catch (Exception e) {
                Log.e("Upload pic in JPEG", "Exception => " + e);
            }

            //AddProfileImg(strMyImagePath);
        }
        if (strMyImagePath == null) {
            return path;
        }
        Log.e("strMyImagePath", "(Final) => " + strMyImagePath);
        strBookCover = strMyImagePath;
        return strMyImagePath;
    }

    String imageFilePath;

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        imageFilePath = image.getAbsolutePath();

        Log.e("imageFilePath", "" + imageFilePath);
        return image;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

}