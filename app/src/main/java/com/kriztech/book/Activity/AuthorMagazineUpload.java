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

public class AuthorMagazineUpload extends AppCompatActivity implements View.OnClickListener {

    private PrefManager prefManager;
    private PermissionUtils takeDocPermissionUtils, takeCoverPermissionUtils;

    private TextView txtBack, txtToolbarTitle, txtSampleMagazineName, txtFullMagazineName, txtMagazineCoverName;
    private EditText edtMagazinetitle, edtMagazineDesc, edtMagazinePrice;
    private Spinner spinCategory;
    private LinearLayout lyToolbar, lyBack, lyUploadSample, lyEmptySampleMagazine, lySelectedSampleMagazine, lyRemoveSampleMagazine, lyViewSampleMagazine,
            lyUploadFull, lyEmptyFullMagazine, lySelectedFullMagazine, lyRemoveFullMagazine, lyViewFullMagazine, lyMagazineCost,
            lyUploadCover, lyEmptyCover, lySelectedCover, lyRemoveCover, lySubmit;
    private RadioGroup rgMagazineCost;
    private RadioButton rbPaid, rbFree;

    private ArrayAdapter categoryAdapter;
    private List<Result> categoryList;
    private ArrayList<String> allCategory;

    private static final int PICKFILE_RESULT_CODE = 248;
    private String strMagazineCover = "", strCategoryID = "", strMagazinetitle = "", strMagazineDesc = "", strMagazinePrice = "", strIsPaid, strSampleMagazine, strFullMagazine, clickType = "";
    private RequestBody authorID, title, categoryID, description, isPaid, price;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(AuthorMagazineUpload.this);
        setContentView(R.layout.activity_author_magazine_upload);
        PrefManager.forceRTLIfSupported(getWindow(), AuthorMagazineUpload.this);
        prefManager = new PrefManager(AuthorMagazineUpload.this);
        takeDocPermissionUtils = new PermissionUtils(AuthorMagazineUpload.this, mDocPermissionResult);
        takeCoverPermissionUtils = new PermissionUtils(AuthorMagazineUpload.this, mCoverPermissionResult);

        Init();
        GetCategory();
        txtToolbarTitle.setText("" + getResources().getString(R.string.upload_magazine));

        rgMagazineCost.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPaid) {
                    strIsPaid = "1";
                    lyMagazineCost.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rbFree) {
                    strIsPaid = "0";
                    lyMagazineCost.setVisibility(View.GONE);
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
            txtSampleMagazineName = findViewById(R.id.txtSampleMagazineName);
            txtFullMagazineName = findViewById(R.id.txtFullMagazineName);
            txtMagazineCoverName = findViewById(R.id.txtMagazineCoverName);
            spinCategory = findViewById(R.id.spinCategory);

            rgMagazineCost = findViewById(R.id.rgMagazineCost);
            rbPaid = findViewById(R.id.rbPaid);
            rbFree = findViewById(R.id.rbFree);
            lyMagazineCost = findViewById(R.id.lyMagazineCost);

            edtMagazinetitle = findViewById(R.id.edtMagazineTitle);
            edtMagazineDesc = findViewById(R.id.edtMagazineDesc);
            edtMagazinePrice = findViewById(R.id.edtMagazinePrice);

            lyUploadSample = findViewById(R.id.lyUploadSample);
            lyEmptySampleMagazine = findViewById(R.id.lyEmptySampleMagazine);
            lySelectedSampleMagazine = findViewById(R.id.lySelectedSampleMagazine);
            lyRemoveSampleMagazine = findViewById(R.id.lyRemoveSampleMagazine);
            lyViewSampleMagazine = findViewById(R.id.lyViewSampleMagazine);
            lyViewSampleMagazine.setVisibility(View.GONE);

            lyUploadFull = findViewById(R.id.lyUploadFull);
            lyEmptyFullMagazine = findViewById(R.id.lyEmptyFullMagazine);
            lySelectedFullMagazine = findViewById(R.id.lySelectedFullMagazine);
            lyRemoveFullMagazine = findViewById(R.id.lyRemoveFullMagazine);
            lyViewFullMagazine = findViewById(R.id.lyViewFullMagazine);
            lyViewFullMagazine.setVisibility(View.GONE);

            lyUploadCover = findViewById(R.id.lyUploadCover);
            lyEmptyCover = findViewById(R.id.lyEmptyCover);
            lySelectedCover = findViewById(R.id.lySelectedCover);
            lyRemoveCover = findViewById(R.id.lyRemoveCover);

            lyBack.setOnClickListener(this);
            lyEmptySampleMagazine.setOnClickListener(this);
            lyEmptyFullMagazine.setOnClickListener(this);
            lyEmptyCover.setOnClickListener(this);
            lyRemoveSampleMagazine.setOnClickListener(this);
            lyRemoveFullMagazine.setOnClickListener(this);
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
        categoryAdapter = new ArrayAdapter(AuthorMagazineUpload.this, R.layout.spinner_item, allCategory);
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

            case R.id.lyEmptySampleMagazine:
                clickType = "SampleMagazine";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectMagazine();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyFullMagazine:
                clickType = "FullMagazine";
                if (takeDocPermissionUtils.isStoragePermissionGranted()) {
                    Constant.isSelectPic = true;
                    selectMagazine();
                } else {
                    takeDocPermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_and_upload_document_file));
                }
                break;

            case R.id.lyEmptyCover:
                if (Utils.checkLoginUser(AuthorMagazineUpload.this)) {
                    if (takeCoverPermissionUtils.isStorageCameraPermissionGranted()) {
                        Constant.isSelectPic = true;
                        selectImage();
                    } else {
                        takeCoverPermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    }
                }
                break;

            case R.id.lyRemoveSampleMagazine:
                lyEmptySampleMagazine.setVisibility(View.VISIBLE);
                lySelectedSampleMagazine.setVisibility(View.GONE);
                txtSampleMagazineName.setText("");
                strSampleMagazine = "";
                break;

            case R.id.lyRemoveFullMagazine:
                lyEmptyFullMagazine.setVisibility(View.VISIBLE);
                lySelectedFullMagazine.setVisibility(View.GONE);
                txtFullMagazineName.setText("");
                strFullMagazine = "";
                break;

            case R.id.lyRemoveCover:
                lyEmptyCover.setVisibility(View.VISIBLE);
                lySelectedCover.setVisibility(View.GONE);
                txtMagazineCoverName.setText("");
                strMagazineCover = "";
                break;

            case R.id.lySubmit:
                ValidateMagazineDetails();
                break;
        }
    }

    private void selectMagazine() {
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorMagazineUpload.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorMagazineUpload.this, getString(R.string.we_need_storage_and_upload_document_file));
                    } else if (allPermissionClear) {
                        Constant.isSelectPic = true;
                        selectMagazine();
                    }
                }
            });

    private void ValidateMagazineDetails() {
        strMagazinetitle = "" + edtMagazinetitle.getText().toString().trim();
        strMagazinePrice = "" + edtMagazinePrice.getText().toString().trim();
        strMagazineDesc = "" + edtMagazineDesc.getText().toString().trim();

        if (TextUtils.isEmpty(strMagazinetitle) && (rbPaid.isChecked() && TextUtils.isEmpty(strMagazinePrice)) && TextUtils.isEmpty(strMagazineDesc)
                && TextUtils.isEmpty(strSampleMagazine) && TextUtils.isEmpty(strFullMagazine) && TextUtils.isEmpty(strMagazineCover)) {

            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.fill_up_all_fields), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strMagazinetitle)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.enter_book_title), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strCategoryID)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.select_magazine_category), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (rbPaid.isChecked() && TextUtils.isEmpty(strMagazinePrice)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.enter_magazine_cost), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strMagazineDesc)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.enter_magazine_description), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strSampleMagazine)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.select_sample_magazine), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strFullMagazine)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.select_full_magazine), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strMagazineCover)) {
            Toasty.warning(AuthorMagazineUpload.this, "" + getResources().getString(R.string.select_magazine_cover), Toasty.LENGTH_SHORT).show();
            return;
        }

        UploadMagazine();
    }

    /* upload_magazine API */
    private void UploadMagazine() {

        Log.e("uploadfile", "SampleMagazine :==> " + strSampleMagazine);
        Log.e("uploadfile", "FullMagazine :==> " + strFullMagazine);
        Log.e("uploadfile", "MagazineCover :==> " + strMagazineCover);

        File fileSampleMagazine = new File(strSampleMagazine);
        // Parsing any Media type file
        RequestBody rbSampleMagazine = RequestBody.create(MediaType.parse("*/*"), fileSampleMagazine);
        MultipartBody.Part sampleMagazine = MultipartBody.Part.createFormData("sample_url", fileSampleMagazine.getName(), rbSampleMagazine);

        File fileFullMagazine = new File(strFullMagazine);
        RequestBody rbFullMagazine = RequestBody.create(MediaType.parse("*/*"), fileFullMagazine);
        MultipartBody.Part fullMagazine = MultipartBody.Part.createFormData("full_magazine", fileFullMagazine.getName(), rbFullMagazine);

        File fileMagazineCover = new File(strMagazineCover);
        RequestBody rbMagazineCover = RequestBody.create(MediaType.parse("image/*"), fileMagazineCover);
        MultipartBody.Part coverPoster = MultipartBody.Part.createFormData("image", fileMagazineCover.getName(), rbMagazineCover);

        authorID = RequestBody.create(MediaType.parse("text/plain"), "" + prefManager.getAuthorId());
        title = RequestBody.create(MediaType.parse("text/plain"), "" + strMagazinetitle);
        categoryID = RequestBody.create(MediaType.parse("text/plain"), "" + strCategoryID);
        description = RequestBody.create(MediaType.parse("text/plain"), "" + strMagazineDesc);
        isPaid = RequestBody.create(MediaType.parse("text/plain"), "" + strIsPaid);
        price = RequestBody.create(MediaType.parse("text/plain"), "" + strMagazinePrice);

        Utils.ProgressBarShow(AuthorMagazineUpload.this);
        Call<SuccessModel> call = BaseURL.getVideoAPI().upload_magazine(authorID, title, categoryID, description, isPaid, price, sampleMagazine, fullMagazine, coverPoster);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {
                try {
                    Log.e("upload_magazine", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("upload_magazine", "message ==> " + response.body().getMessage());
                        Toasty.success(AuthorMagazineUpload.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                        Constant.isSelectPic = false;
                        finish();
                    } else {
                        Log.e("upload_magazine", "message ==> " + response.body().getMessage());
                        Toasty.info(AuthorMagazineUpload.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("upload_magazine", "Exception ==> " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Utils.ProgressbarHide();
                Log.e("upload_magazine", "onFailure ==> " + t.getMessage());
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

                        if (clickType.equalsIgnoreCase("SampleMagazine")) {
                            strSampleMagazine = "" + Utils.getDocumentPath(AuthorMagazineUpload.this, selectedFileUri);
                            Log.e("onActivityResult", "SampleMagazine :=> " + strSampleMagazine);

                            lyEmptySampleMagazine.setVisibility(View.GONE);
                            lySelectedSampleMagazine.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorMagazineUpload.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorMagazineUpload.this, selectedFileUri).endsWith(".epub")) {
                                txtSampleMagazineName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtSampleMagazineName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorMagazineUpload.this, selectedFileUri));
                            }

                        } else if (clickType.equalsIgnoreCase("FullMagazine")) {
                            strFullMagazine = "" + Utils.getDocumentPath(AuthorMagazineUpload.this, selectedFileUri);
                            Log.e("onActivityResult", "FullMagazine :=> " + strFullMagazine);

                            lyEmptyFullMagazine.setVisibility(View.GONE);
                            lySelectedFullMagazine.setVisibility(View.VISIBLE);

                            if (Utils.getFileName(AuthorMagazineUpload.this, selectedFileUri).endsWith(".pdf") ||
                                    Utils.getFileName(AuthorMagazineUpload.this, selectedFileUri).endsWith(".epub")) {
                                txtFullMagazineName.setText(Utils.getFileName(this, selectedFileUri));
                            } else {
                                txtFullMagazineName.setText(Utils.getFileName(this, selectedFileUri) + "."
                                        + Utils.getFileExtension(AuthorMagazineUpload.this, selectedFileUri));
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
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AuthorMagazineUpload.this, R.style.AlertDialogDanger);
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(AuthorMagazineUpload.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(AuthorMagazineUpload.this, getString(R.string.we_need_storage_and_upload_document_file));
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
                .setAspectRatio(1, 2).getIntent(AuthorMagazineUpload.this);
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
                        txtMagazineCoverName.setText(filename);
                        // get the image uri after the image crope and resize it
                        decodeFile(getRealPathFromURI(cropResult.getUri()), Constant.PROFILE_IMAGE_SIZE, Constant.PROFILE_IMAGE_SIZE);
                    }
                }
            });

    private String getRealPathFromURI(Uri contentURI) {
        @SuppressLint("Recycle") Cursor cursor = AuthorMagazineUpload.this.getContentResolver().query(contentURI, null, null, null, null);
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
                    strMagazineCover = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                // Store to tmp file
                String extr = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(AuthorMagazineUpload.this);
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
                    strMagazineCover = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                String extr = "";
                // Store to tmp file
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(AuthorMagazineUpload.this);
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
        strMagazineCover = strMyImagePath;
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