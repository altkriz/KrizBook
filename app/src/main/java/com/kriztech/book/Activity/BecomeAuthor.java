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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.kriztech.book.Model.AuthorRegistrationModel.AuthorRegistrationModel;
import com.kriztech.book.Model.AuthorBankDetailModel.AuthorBankDetailModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.ScalingUtilities;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
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

public class BecomeAuthor extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = BecomeAuthor.class.getSimpleName();
    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;

    private RoundedImageView ivAuthor;
    private TextView txtToolbarTitle, txtBack, txtRegister;
    private EditText edtAuthorname, edtAuthorBiodata, edtAuthoraddress, edtAuthoremail, edtAuthorBankName, edtAuthorAccountNum, edtAuthorIFSC, edtAuthorBankHolderName;
    private TextInputLayout tilAuthorPassword;
    private TextInputEditText edtAuthorpassword;
    private LinearLayout lyBack, lyToolbar, lyAuthorImgEdit, lyRegister;

    private String strAuthorName = "", strAuthorBio = "", strAddress = "", strEmail = "", strPassword = "", strBankname = "", strAccountnum = "", strIfsc = "", strBankholder = "",
            fileProfile = "";
    private RequestBody userId, authorEmail, authorAddress, authorName, authorBio, authorPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(BecomeAuthor.this);
        setContentView(R.layout.activity_become_author);
        PrefManager.forceRTLIfSupported(getWindow(), BecomeAuthor.this);
        prefManager = new PrefManager(BecomeAuthor.this);

        takePermissionUtils = new PermissionUtils(BecomeAuthor.this, mPermissionResult);

        Init();
        txtToolbarTitle.setText("" + getResources().getString(R.string.author_register));

        edtAuthorpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    tilAuthorPassword.setPasswordVisibilityToggleEnabled(true);
                } else {
                    tilAuthorPassword.setPasswordVisibilityToggleEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void Init() {
        try {
            lyToolbar = findViewById(R.id.lyToolbar);
            lyBack = findViewById(R.id.lyBack);
            txtBack = findViewById(R.id.txtBack);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtRegister = findViewById(R.id.txtRegister);

            ivAuthor = findViewById(R.id.ivAuthor);

            edtAuthorname = findViewById(R.id.edtAuthorname);
            edtAuthorBiodata = findViewById(R.id.edtAuthorBiodata);
            edtAuthoraddress = findViewById(R.id.edtAuthoraddress);
            edtAuthoremail = findViewById(R.id.edtAuthoremail);
            tilAuthorPassword = findViewById(R.id.tilAuthorPassword);
            edtAuthorpassword = findViewById(R.id.edtAuthorpassword);
            edtAuthorBankName = findViewById(R.id.edtAuthorBankName);
            edtAuthorAccountNum = findViewById(R.id.edtAuthorAccountNum);
            edtAuthorIFSC = findViewById(R.id.edtAuthorIFSC);
            edtAuthorBankHolderName = findViewById(R.id.edtAuthorBankHolderName);

            lyAuthorImgEdit = findViewById(R.id.lyAuthorImgEdit);
            lyRegister = findViewById(R.id.lyRegister);

            lyBack.setOnClickListener(this);
            lyAuthorImgEdit.setOnClickListener(this);
            lyRegister.setOnClickListener(this);
        } catch (Exception e) {
            Log.e("Init", "Exception => " + e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyBack:
                finish();
                break;

            case R.id.lyAuthorImgEdit:
                if (Utils.checkLoginUser(BecomeAuthor.this)) {
                    if (takePermissionUtils.isStorageCameraPermissionGranted()) {
                        Constant.isSelectPic = true;
                        selectImage();
                    } else {
                        takePermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    }
                }
                break;

            case R.id.lyRegister:
                ValidateAuthorDetails();
                break;
        }
    }

    private void ValidateAuthorDetails() {

        strAuthorName = "" + edtAuthorname.getText().toString().trim();
        strAuthorBio = "" + edtAuthorBiodata.getText().toString().trim();
        strAddress = "" + edtAuthoraddress.getText().toString().trim();
        strEmail = "" + edtAuthoremail.getText().toString().trim();
        strPassword = "" + edtAuthorpassword.getText().toString().trim();
        strBankname = "" + edtAuthorBankName.getText().toString().trim();
        strAccountnum = "" + edtAuthorAccountNum.getText().toString().trim();
        strIfsc = "" + edtAuthorIFSC.getText().toString().trim();
        strBankholder = "" + edtAuthorBankHolderName.getText().toString().trim();

        Log.e("=> AuthorName", "" + strAuthorName);
        Log.e("=> AuthorBio", "" + strAuthorBio);
        Log.e("=> Address", "" + strAddress);
        Log.e("=> Email", "" + strEmail);
        Log.e("=> Password", "" + strPassword);
        Log.e("=> BankName", "" + strBankname);
        Log.e("=> Accountnum", "" + strAccountnum);
        Log.e("=> IFSC", "" + strIfsc);
        Log.e("=> BankHolderName", "" + strBankholder);

        if (TextUtils.isEmpty(strAuthorName)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_name), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strAuthorBio)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_bio), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strAddress)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_address), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strEmail)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_email), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strPassword)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_password), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBankname)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_bank_name), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strAccountnum)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_account_number), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strIfsc)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_ifsc_code), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strBankholder)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_author_bank_holder_name), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (!Utils.isEmailValid(strEmail)) {
            Toasty.warning(BecomeAuthor.this, "" + getResources().getString(R.string.enter_valid_email), Toasty.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(fileProfile)) {
            AddAuthorWithImg();
        } else {
            AddAuthor();
        }

    }

    /* add_author API */
    private void AddAuthor() {
        Utils.ProgressBarShow(BecomeAuthor.this);

        Call<AuthorRegistrationModel> call = BaseURL.getVideoAPI().add_author("" + prefManager.getLoginId(), "" + strEmail, "" + strAddress,
                "" + strPassword, "" + strAuthorName, "" + strAuthorBio);
        call.enqueue(new Callback<AuthorRegistrationModel>() {
            @Override
            public void onResponse(Call<AuthorRegistrationModel> call, Response<AuthorRegistrationModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("add_author", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(BecomeAuthor.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();

                        Log.e("add_author", "authorID ==> " + response.body().getResult().get(0).getId());
                        Log.e("add_author", "authorName ==> " + response.body().getResult().get(0).getName());

                        /* Store Author Data */
                        Utils.storeAuthorCred(BecomeAuthor.this,
                                "" + response.body().getResult().get(0).getId(),
                                "" + response.body().getResult().get(0).getEmail(),
                                "" + response.body().getResult().get(0).getName());

                        /* Add Bank Details */
                        AddBankDetails();

                        Log.e("add_author", "authorID ==> " + response.body().getResult().get(0).getId());
                        Log.e("add_author", "authorName ==> " + response.body().getResult().get(0).getName());
                    } else {
                        Log.e("add_author", "Message ==> " + response.body().getMessage());
                        Utils.AlertDialog(BecomeAuthor.this, "" + response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("add_author", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<AuthorRegistrationModel> call, Throwable t) {
                Log.e("add_author", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    /* add_author API with Author Image */
    private void AddAuthorWithImg() {
        File file = new File(fileProfile);
        Log.e("<file>-->", file + "");

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part bodyAuthorImage = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        userId = RequestBody.create(MediaType.parse("text/plain"), "" + prefManager.getLoginId());
        authorEmail = RequestBody.create(MediaType.parse("text/plain"), "" + strEmail);
        authorName = RequestBody.create(MediaType.parse("text/plain"), "" + strAuthorName);
        authorBio = RequestBody.create(MediaType.parse("text/plain"), "" + strAuthorBio);
        authorAddress = RequestBody.create(MediaType.parse("text/plain"), "" + strAddress);
        authorPassword = RequestBody.create(MediaType.parse("text/plain"), "" + strPassword);

        Utils.ProgressBarShow(BecomeAuthor.this);
        Call<AuthorRegistrationModel> call = BaseURL.getVideoAPI().add_author_with_img(userId, authorEmail, authorAddress, authorPassword, authorName, authorBio, bodyAuthorImage);
        call.enqueue(new Callback<AuthorRegistrationModel>() {
            @Override
            public void onResponse(Call<AuthorRegistrationModel> call, Response<AuthorRegistrationModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("add_author_with_img", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(BecomeAuthor.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();

                        Log.e("add_author_with_img", "authorID ==> " + response.body().getResult().get(0).getId());
                        Log.e("add_author_with_img", "authorName ==> " + response.body().getResult().get(0).getName());

                        /* Store Author Data */
                        Utils.storeAuthorCred(BecomeAuthor.this,
                                "" + response.body().getResult().get(0).getId(),
                                "" + response.body().getResult().get(0).getEmail(),
                                "" + response.body().getResult().get(0).getName());

                        /* Add Bank Details */
                        AddBankDetails();

                    } else {
                        Log.e("add_author_with_img", "Message ==> " + response.body().getMessage());
                        Utils.AlertDialog(BecomeAuthor.this, "" + response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("add_author_with_img", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<AuthorRegistrationModel> call, Throwable t) {
                Log.e("add_author_with_img", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
    }

    /* add_bank_detail API */
    private void AddBankDetails() {
        Utils.ProgressBarShow(BecomeAuthor.this);

        Call<AuthorBankDetailModel> call = BaseURL.getVideoAPI().add_bank_detail("" + prefManager.getAuthorId(), "" + strAccountnum,
                "" + strBankholder, "" + strBankname, "" + strIfsc);
        call.enqueue(new Callback<AuthorBankDetailModel>() {
            @Override
            public void onResponse(Call<AuthorBankDetailModel> call, Response<AuthorBankDetailModel> response) {
                Utils.ProgressbarHide();
                try {
                    Log.e("add_bank_detail", "Status ==> " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(BecomeAuthor.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();
                        Log.e("add_bank_detail", "authorID ==> " + response.body().getResult().get(0).getAuthorId());

                        /* Upload Book or Magazine Selection Dialog */
                        Utils.authorDocUploadDialog(BecomeAuthor.this, true);
                    } else {
                        Log.e("add_bank_detail", "Message ==> " + response.body().getMessage());
                        Utils.AlertDialog(BecomeAuthor.this, "" + response.body().getMessage(), false, false);
                    }
                } catch (Exception e) {
                    Log.e("add_bank_detail", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(Call<AuthorBankDetailModel> call, Throwable t) {
                Log.e("add_bank_detail", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
            }
        });
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(BecomeAuthor.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(BecomeAuthor.this, getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    } else if (allPermissionClear) {
                        Constant.isSelectPic = true;
                        selectImage();
                    }

                }
            });

    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel_)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(BecomeAuthor.this, R.style.AlertDialogDanger);
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

    ActivityResultLauncher<Intent> resultCallbackForCrop = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("CallbackForCrop", "result => " + result.getData());
                        Intent data = result.getData();
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);

                        Picasso.get().load(cropResult.getUri()).into(ivAuthor);
                        Log.e("fileProfile", "==> " + fileProfile);
                        // get the image uri after the image crope and resize it
                        decodeFile(getRealPathFromURI(cropResult.getUri()), Constant.PROFILE_IMAGE_SIZE, Constant.PROFILE_IMAGE_SIZE);
                    }
                }
            });

    private String getRealPathFromURI(Uri contentURI) {
        @SuppressLint("Recycle") Cursor cursor = BecomeAuthor.this.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void beginCrop(Uri source) {
        Intent intent = CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1).getIntent(BecomeAuthor.this);
        resultCallbackForCrop.launch(intent);
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
                    fileProfile = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                // Store to tmp file
                String extr = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(BecomeAuthor.this);
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

        } else {
            Log.e("jpeg path ==>", "" + strMyImagePath);
            try {
                // Part 1: Decode image
                Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);

                if (!(unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight)) {
                    // Part 2: Scale image
                    scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, desiredWidth, desiredHeight, ScalingUtilities.ScalingLogic.FIT);
                } else if (unscaledBitmap.getWidth() <= desiredWidth && unscaledBitmap.getHeight() <= desiredHeight) {
                    fileProfile = path;
                    //AddProfileImg(path);
                    return path;
                } else {
                    unscaledBitmap.recycle();
                    return path;
                }

                String extr = "";
                // Store to tmp file
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    extr = Functions.getAppFolder(BecomeAuthor.this);
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

        }
        if (strMyImagePath == null) {
            return path;
        }
        Log.e("strMyImagePath", "(Final) => " + strMyImagePath);
        fileProfile = strMyImagePath;
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