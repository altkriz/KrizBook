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

import com.kriztech.book.Model.CategoryModel.CategoryModel;
import com.kriztech.book.Model.CategoryModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.ScalingUtilities;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
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

public class AuthorBookUpload extends AppCompatActivity implements View.OnClickListener {

    private PrefManager prefManager;
    private PermissionUtils takeDocPermissionUtils, takeCoverPermissionUtils;

    private TextView txtBack, txtToolbarTitle, txtSampleBookName, txtFullBookName, txtBookCoverName;
    private EditText edtBooktitle, edtBookDesc, edtBookPrice;
    private Spinner spinCategory;
    private LinearLayout lyToolbar, lyBack, lyUploadSample, lyEmptySampleBook, lySelectedSampleBook, lyRemoveSampleBook, lyViewSampleBook,
            lyUploadFull, lyEmptyFullBook, lySelectedFullBook, lyRemoveFullBook, lyViewFullBook, lyBookCost, lyUploadCover, lyEmptyCover, lySelectedCover, lyRemoveCover, lySubmit;
    private RadioGroup rgBookCost;
    private RadioButton rbPaid, rbFree;

    private ArrayAdapter categoryAdapter;
    private List<Result> categoryList;
    private ArrayList<String> allCategory;

    private static final int PICKFILE_RESULT_CODE = 248;
    private String strBookCover = "", strCategoryID = "", strBooktitle = "", strBookDesc = "", strBookPrice = "", strIsPaid, strSampleBook, strFullBook, clickType = "";
    private RequestBody authorID, title, categoryID, description, isPaid, price;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(AuthorBookUpload.this);
        setContentView(R.layout.activity_author_book_upload);
        PrefManager.forceRTLIfSupported(getWindow(), AuthorBookUpload.this);
        prefManager = new PrefManager(AuthorBookUpload.this);
        takeDocPermissionUtils = new PermissionUtils(AuthorBookUpload.this, mDocPermissionResult);
        takeCoverPermissionUtils = new PermissionUtils(AuthorBookUpload.this, mCoverPermissionResult);

        Init();
        GetCategory();
        txtToolbarTitle.setText("" + getResources().getString(R.string.upload_book));

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
            lyViewSampleBook.setVisibility(View.GONE);

            lyUploadFull = findViewById(R.id.lyUploadFull);
            lyEmptyFullBook = findViewById(R.id.lyEmptyFullBook);
            lySelectedFullBook = findViewById(R.id.lySelectedFullBook);
            lyRemoveFullBook = findViewById(R.id.lyRemoveFullBook);
            lyViewFullBook = findViewById(R.id.lyViewFullBook);
            lyViewFullBook.setVisibility(View.GONE);

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
        } catch (Exception e) {
            Log.e("Init", "Exception => " + e);
        }
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
        categoryAdapter = new ArrayAdapter(AuthorBookUpload.this, R.layout.spinner_item, allCategory);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinCategory.setAdapter(categoryAdapter);
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
                clickType = "SampleBook";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectBook();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyFullBook:
                clickType = "FullBook";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectBook();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyCover:
                if (Utils.checkLoginUser(AuthorBookUpload.this)) {
                    if (takeCoverPermissionUtils.isStorageCameraPermissionGranted()) {
                        Constant.isSelectPic = true;
                        selectImage();
                    } else {
                        takeCoverPermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
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

    private ActivityResultLauncher<String[]> mDocPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorBookUpload.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorBookUpload.this, getString(R.string.we_need_storage_and_upload_document_file));
                    } else if (allPermissionClear) {
                        Constant.isSelectPic = true;
                        selectBook();
                    }
                }
            });

    private void ValidateBookDetails() {
        strBooktitle = "" + edtBooktitle.getText().toString().trim();
        strBookPrice = "" + edtBookPrice.getText().toString().trim();
        strBookDesc = "" + edtBookDesc.getText().toString().trim();

        if (TextUtils.isEmpty(strBooktitle) && (rbPaid.isChecked() && TextUtils.isEmpty(strBookPrice)) && TextUtils.isEmpty(strBookDesc) && TextUtils.isEmpty(strSampleBook)
                && TextUtils.isEmpty(strFullBook) && TextUtils.isEmpty(strBookCover)) {

            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.fill_up_all_fields), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBooktitle)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.enter_book_title), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strCategoryID)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.select_book_category), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (rbPaid.isChecked() && TextUtils.isEmpty(strBookPrice)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.enter_book_cost), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBookDesc)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.enter_book_description), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strSampleBook)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.select_sample_book), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strFullBook)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.select_full_book), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBookCover)) {
            Toasty.warning(AuthorBookUpload.this, "" + getResources().getString(R.string.select_book_cover), Toasty.LENGTH_SHORT).show();
            return;
        }

        Log.e("uploadfile", "SampleBook :==> " + strSampleBook);
        Log.e("uploadfile", "FullBook :==> " + strFullBook);
        Log.e("uploadfile", "BookCover :==> " + strBookCover);
        UploadBook();
    }

    /* upload_book API */
    private void UploadBook() {

        Log.e("uploadfile", "SampleBook :==> " + strSampleBook);
        Log.e("uploadfile", "FullBook :==> " + strFullBook);
        Log.e("uploadfile", "BookCover :==> " + strBookCover);

        File fileSampleBook = new File(strSampleBook);
        // Parsing any Media type file
        RequestBody rbSampleBook = RequestBody.create(MediaType.parse("*/*"), fileSampleBook);
        MultipartBody.Part sampleBook = MultipartBody.Part.createFormData("sample_url", fileSampleBook.getName(), rbSampleBook);

        File fileFullBook = new File(strFullBook);
        RequestBody rbFullBook = RequestBody.create(MediaType.parse("*/*"), fileFullBook);
        MultipartBody.Part fullBook = MultipartBody.Part.createFormData("full_book", fileFullBook.getName(), rbFullBook);

        File fileBookCover = new File(strBookCover);
        RequestBody rbBookCover = RequestBody.create(MediaType.parse("image/*"), fileBookCover);
        MultipartBody.Part coverPoster = MultipartBody.Part.createFormData("image", fileBookCover.getName(), rbBookCover);

        authorID = RequestBody.create(MediaType.parse("text/plain"), "" + prefManager.getAuthorId());
        title = RequestBody.create(MediaType.parse("text/plain"), "" + strBooktitle);
        categoryID = RequestBody.create(MediaType.parse("text/plain"), "" + strCategoryID);
        description = RequestBody.create(MediaType.parse("text/plain"), "" + strBookDesc);
        isPaid = RequestBody.create(MediaType.parse("text/plain"), "" + strIsPaid);
        price = RequestBody.create(MediaType.parse("text/plain"), "" + strBookPrice);

        Utils.ProgressBarShow(AuthorBookUpload.this);
        Call<SuccessModel> call = BaseURL.getVideoAPI().upload_book(authorID, title, categoryID, description, isPaid, price, sampleBook, fullBook, coverPoster);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("upload_book", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("upload_book", "message ==> " + response.body().getMessage());
                        Toasty.success(AuthorBookUpload.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                        Constant.isSelectPic = false;
                        finish();
                    } else {
                        Log.e("upload_book", "message ==> " + response.body().getMessage());
                        Toasty.info(AuthorBookUpload.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("upload_book", "Exception ==> " + e);
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("upload_book", "onFailure ==> " + t.getMessage());
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
                            strSampleBook = "" + Utils.getDocumentPath(AuthorBookUpload.this, selectedFileUri);
                            Log.e("onActivityResult", "SampleBook :=> " + strSampleBook);

                            lyEmptySampleBook.setVisibility(View.GONE);
                            lySelectedSampleBook.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorBookUpload.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorBookUpload.this, selectedFileUri).endsWith(".epub")) {
                                txtSampleBookName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtSampleBookName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorBookUpload.this, selectedFileUri));
                            }

                        } else if (clickType.equalsIgnoreCase("FullBook")) {
                            strFullBook = "" + Utils.getDocumentPath(AuthorBookUpload.this, selectedFileUri);
                            Log.e("onActivityResult", "FullBook :=> " + strFullBook);

                            lyEmptyFullBook.setVisibility(View.GONE);
                            lySelectedFullBook.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorBookUpload.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorBookUpload.this, selectedFileUri).endsWith(".epub")) {
                                txtFullBookName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtFullBookName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorBookUpload.this, selectedFileUri));
                            }

                        }

                    } catch (Exception e) {
                        Log.e("onActivityResult", "File select error =>> " + e);
                    }
                }
            }
        }
    }


    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel_)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AuthorBookUpload.this, R.style.AlertDialogDanger);
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

    private ActivityResultLauncher<String[]> mCoverPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorBookUpload.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorBookUpload.this, getString(R.string.we_need_storage_and_upload_document_file));
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
                .setAspectRatio(1, 2).getIntent(AuthorBookUpload.this);
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
        @SuppressLint("Recycle") Cursor cursor = AuthorBookUpload.this.getContentResolver().query(contentURI, null, null, null, null);
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
                    extr = Functions.getAppFolder(AuthorBookUpload.this);
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
                    extr = Functions.getAppFolder(AuthorBookUpload.this);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.ProgressbarHide();
    }

}