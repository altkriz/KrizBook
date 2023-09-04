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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.kriztech.book.Model.ProfileModel.ProfileModel;
import com.kriztech.book.Model.ProfileModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.Functions;
import com.kriztech.book.Utility.PermissionUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.ScalingUtilities;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
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

public class Profile extends AppCompatActivity {

    private PrefManager prefManager;
    private PermissionUtils takePermissionUtils;

    private ShimmerFrameLayout shimmer;
    private RoundedImageView rivUser;
    private LinearLayout lyBack, lyImgEdit;
    private TextView txtUpdate;
    private EditText etEmail, etPhoneNumber, etUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;

    private List<Result> profileList;

    private String strUsername, strPassword, strEmail, strMobileNumber, fileProfile = "", entryFrom = "";
    private RequestBody userId, email, fullname, mobileNumber, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(Profile.this);
        Utils.HideNavigation(Profile.this);
        setContentView(R.layout.activity_profile);
        PrefManager.forceRTLIfSupported(getWindow(), Profile.this);

        takePermissionUtils = new PermissionUtils(Profile.this, mPermissionResult);
        Init();

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    tilPassword.setPasswordVisibilityToggleEnabled(true);
                } else {
                    tilPassword.setPasswordVisibilityToggleEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lyImgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkLoginUser(Profile.this)) {
                    if (takePermissionUtils.isStorageCameraPermissionGranted()) {
                        Constant.isSelectPic = true;
                        selectImage();
                    } else {
                        takePermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    }
                }
            }
        });

        txtUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAndUpdate();
            }
        });

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entryFrom.equalsIgnoreCase("OTP")) {
                    startActivity(new Intent(Profile.this, MainActivity.class));
                }
                finish();
            }
        });

    }

    private void Init() {
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                entryFrom = getIntent().getStringExtra("from");
                Log.e("from =>", "" + entryFrom);
            }
            prefManager = new PrefManager(Profile.this);
            shimmer = findViewById(R.id.shimmer);

            lyBack = findViewById(R.id.lyBack);

            rivUser = findViewById(R.id.rivUser);
            lyImgEdit = findViewById(R.id.lyImgEdit);
            txtUpdate = findViewById(R.id.txtUpdate);
            etUsername = findViewById(R.id.etUsername);
            etEmail = findViewById(R.id.etEmail);
            etPhoneNumber = findViewById(R.id.etPhoneNumber);
            etPassword = findViewById(R.id.etPassword);
            tilPassword = findViewById(R.id.tilPassword);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "isSelectPic => " + Constant.isSelectPic);
        if (!Constant.isSelectPic) {
            GetProfile();
        }
    }

    /* profile API */
    private void GetProfile() {
        Utils.shimmerShow(shimmer);

        Call<ProfileModel> call = BaseURL.getVideoAPI().profile("" + prefManager.getLoginId());
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("profile status", "" + response.body().getStatus());

                        if (response.body().getResult().size() > 0) {
                            profileList = new ArrayList<Result>();
                            profileList = response.body().getResult();

                            etUsername.setText("" + profileList.get(0).getFullname());
                            etEmail.setText("" + profileList.get(0).getEmail());
                            etPhoneNumber.setText("" + profileList.get(0).getMobile());
                            etPassword.setText("" + profileList.get(0).getPassword());

                            if (!TextUtils.isEmpty(profileList.get(0).getImage())) {
                                Picasso.get().load(profileList.get(0).getImage()).into(rivUser);
                            }

                            Utils.storeUserCred(Profile.this,
                                    "" + response.body().getResult().get(0).getId(),
                                    "" + response.body().getResult().get(0).getType(),
                                    "" + response.body().getResult().get(0).getEmail(),
                                    "" + response.body().getResult().get(0).getFullname(),
                                    "" + response.body().getResult().get(0).getMobile());

                            if (response.body().getAuthorProfile() != null) {
                                Utils.storeAuthorCred(Profile.this,
                                        "" + response.body().getAuthorProfile().getId(),
                                        "" + response.body().getAuthorProfile().getEmail(),
                                        "" + response.body().getAuthorProfile().getName());
                            } else {
                                Utils.storeAuthorCred(Profile.this, "0", "", "");
                            }
                        }

                    } else {
                        Log.e("profile status", "" + response.body().getStatus());
                    }
                } catch (Exception e) {
                    Log.e("profile Exception =>", "" + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                Log.e("profile onFailure =>", "" + t.getMessage());
                Utils.shimmerHide(shimmer);
            }
        });
    }

    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear = true;
                    List<String> blockPermissionCheck = new ArrayList<>();
                    for (String key : result.keySet()) {
                        if (!(result.get(key))) {
                            allPermissionClear = false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(Profile.this, key));
                        }
                    }
                    Log.e("blockPermissionCheck", "" + blockPermissionCheck);
                    Log.e("allPermissionClear", "" + allPermissionClear);
                    if (blockPermissionCheck.contains("blocked")) {
                        Functions.showPermissionSetting(Profile.this, getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    } else if (allPermissionClear) {
                        Constant.isSelectPic = true;
                        selectImage();
                    }

                }
            });

    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel_)};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Profile.this, R.style.AlertDialogDanger);
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

    // below three method is related with taking the picture from camera
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

    // create a temp image file
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on scren orientation
        // changes
        if (!TextUtils.isEmpty(imageFilePath)) {
            outState.putString("imageFilePath", imageFilePath);
            outState.putBoolean("isSelected", Constant.isSelectPic);
            Log.e("onSave", "imageFilePath => " + imageFilePath);
            Log.e("onSave", "isSelectPic => " + Constant.isSelectPic);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        imageFilePath = savedInstanceState.getString("imageFilePath");
        Constant.isSelectPic = savedInstanceState.getBoolean("isSelected");
        Log.e("onRestore", "isSelectPic => " + Constant.isSelectPic);
    }

    ActivityResultLauncher<Intent> resultCallbackForCrop = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("CallbackForCrop", "result => " + result.getData());
                        Intent data = result.getData();
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);

                        Picasso.get().load(cropResult.getUri()).into(rivUser);
                        Log.e("fileProfile", "" + fileProfile);
                        // get the image uri after the image crope and resize it
                        decodeFile(getRealPathFromURI(cropResult.getUri()), Constant.PROFILE_IMAGE_SIZE, Constant.PROFILE_IMAGE_SIZE);
                    }
                }
            });

    private void beginCrop(Uri source) {
        Intent intent = CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1).getIntent(Profile.this);
        resultCallbackForCrop.launch(intent);
    }

    private String decodeFile(String path, int desiredWidth, int desiredHeight) {
        Log.e("path", "==> " + path);
        Log.e("desiredWidth", "==> " + desiredWidth);
        Log.e("desiredHeight", "==> " + desiredHeight);

        String strMyImagePath = null;

        if (path.contains(".png")) {
            Bitmap scaledBitmap = null;
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
                    extr = Functions.getAppFolder(Profile.this);
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

            if (strMyImagePath == null) {
                return path;
            }
            Log.e("strMyImagePath", "(Final) => " + strMyImagePath);
            fileProfile = strMyImagePath;
            //AddProfileImg(strMyImagePath);

        } else {
            Bitmap scaledBitmap = null;
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
                    extr = Functions.getAppFolder(Profile.this);
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

            if (strMyImagePath == null) {
                return path;
            }
            Log.e("strMyImagePath", "(Final) => " + strMyImagePath);
            fileProfile = strMyImagePath;
            //AddProfileImg(strMyImagePath);
        }
        return strMyImagePath;
    }

    private String getRealPathFromURI(Uri contentURI) {
        @SuppressLint("Recycle") Cursor cursor = Profile.this.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void ValidateAndUpdate() {
        strUsername = etUsername.getText().toString().trim();
        strEmail = etEmail.getText().toString().trim();
        strMobileNumber = etPhoneNumber.getText().toString().trim();
        strPassword = etPassword.getText().toString().trim();

        Log.e("=> fileProfile", "" + fileProfile);
        Log.e("=> strUsername", "" + strUsername);
        Log.e("=> strEmail", "" + strEmail);
        Log.e("=> strMobileNumber", "" + strMobileNumber);
        Log.e("=> strPassword", "" + strPassword);

        if (TextUtils.isEmpty(strUsername)) {
            Toasty.warning(Profile.this, "" + getResources().getString(R.string.enter_your_name_profile), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strMobileNumber)) {
            Toasty.warning(Profile.this, "" + getResources().getString(R.string.enter_phone_number_profile), Toasty.LENGTH_SHORT).show();
            return;
        }
        if (!Utils.isEmailValid(strEmail)) {
            Toasty.warning(Profile.this, "" + getResources().getString(R.string.enter_valid_email), Toasty.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(fileProfile)) {
            ProfileUpdate();
        } else {
            AddProfileImg(fileProfile);
        }
    }

    /* update_profile API with Image */
    private void AddProfileImg(String filePath) {

        File file = new File(filePath);
        Log.e("<file>-->", file + "");

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        userId = RequestBody.create(MediaType.parse("text/plain"), "" + prefManager.getLoginId());
        email = RequestBody.create(MediaType.parse("text/plain"), "" + strEmail);
        fullname = RequestBody.create(MediaType.parse("text/plain"), "" + strUsername);
        mobileNumber = RequestBody.create(MediaType.parse("text/plain"), "" + strMobileNumber);
        password = RequestBody.create(MediaType.parse("text/plain"), "" + strPassword);

        Utils.ProgressBarShow(Profile.this);
        Call<SuccessModel> call = BaseURL.getVideoAPI().add_profile_img(userId, fullname, email, password, mobileNumber, body);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(@NonNull Call<SuccessModel> call, @NonNull Response<SuccessModel> response) {
                try {
                    Log.e("Add_Profile_fg", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(Profile.this, "" + response.body().getMessage(), Toasty.LENGTH_SHORT).show();

                        if (entryFrom.equalsIgnoreCase("OTP")) {
                            startActivity(new Intent(Profile.this, MainActivity.class));
                            finish();
                        } else {
                            GetProfile();
                        }

                    } else {
                        Utils.AlertDialog(Profile.this, response.body().getMessage(), false, false);
                    }

                } catch (Exception e) {
                    Log.e("Add_Profile_fg", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(@NonNull Call<SuccessModel> call, @NonNull Throwable t) {
                Log.e("Add_Profile_fg", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(Profile.this, t.getMessage(), false, false);
            }
        });
    }

    /* update_profile API */
    private void ProfileUpdate() {
        Utils.ProgressBarShow(Profile.this);

        Call<SuccessModel> call = BaseURL.getVideoAPI().updateprofile("" + prefManager.getLoginId(),
                "" + strUsername, "" + strEmail, "" + strPassword, "" + strMobileNumber);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(@NonNull Call<SuccessModel> call, @NonNull Response<SuccessModel> response) {
                try {
                    Log.e("Profile", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Toasty.success(Profile.this, "" + response.body().getMessage(), Toasty.LENGTH_LONG).show();

                        if (entryFrom.equalsIgnoreCase("OTP")) {
                            startActivity(new Intent(Profile.this, MainActivity.class));
                            finish();
                        } else {
                            GetProfile();
                        }

                    } else {
                        Utils.AlertDialog(Profile.this, response.body().getMessage(), false, false);
                    }

                } catch (Exception e) {
                    Log.e("Profile", "Exception => " + e);
                }
                Utils.ProgressbarHide();
            }

            @Override
            public void onFailure(@NonNull Call<SuccessModel> call, @NonNull Throwable t) {
                Log.e("Profile", "onFailure => " + t.getMessage());
                Utils.ProgressbarHide();
                Utils.AlertDialog(Profile.this, t.getMessage(), false, false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (entryFrom.equalsIgnoreCase("OTP")) {
            startActivity(new Intent(Profile.this, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.ProgressbarHide();
        Utils.shimmerHide(shimmer);
    }

}