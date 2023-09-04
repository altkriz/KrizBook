package com.kriztech.book.Activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kriztech.book.R;
import com.kriztech.book.Utility.LocaleUtils;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdView;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import es.dmoral.toasty.Toasty;

public class PDFShow extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private PDFView pdfView;

    private static final String TAG = PDFShow.class.getSimpleName();
    private PrefManager prefManager;

    private TextView txtToolbarTitle;
    private LinearLayout lyToolbar, lyBack, lyFbAdView;
    private RelativeLayout rlAdView;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    int pageNumber;
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;
    private String uri, type, toolbarTitle, secretKey = "", filePassword = "";
    private InputStream is;
    private boolean isFullscreen = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.HideNavigation(PDFShow.this);
        Utils.setTheme(PDFShow.this);
        setContentView(R.layout.activity_pdfview);
        PrefManager.forceRTLIfSupported(getWindow(), PDFShow.this);

        init();

        Intent intent = getIntent();
        if (intent != null) {
            uri = intent.getStringExtra("link");
            toolbarTitle = intent.getStringExtra("toolbarTitle");
            secretKey = intent.getStringExtra("secretKey");
            filePassword = intent.getStringExtra("filePassword");
            type = intent.getStringExtra("type");
            Log.e("uri", "" + uri);
            Log.e("secretKey", "" + secretKey);
            Log.e("filePassword", "" + filePassword);
            Log.e("type", "" + type);
        }

        AdInit();

        pdfView.setBackgroundColor(Color.LTGRAY);
        txtToolbarTitle.setText("" + toolbarTitle);

        if (type.equals("link")) {
            new PdfAsyncTask().execute(uri);
        } else {
            File file = new File(uri);
            displayFromFile(file);
        }

        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen();
            }
        });

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(PDFShow.this);

            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);
            lyToolbar = findViewById(R.id.lyToolbar);
            lyToolbar.setVisibility(View.VISIBLE);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            lyBack = findViewById(R.id.lyBack);

            pdfView = findViewById(R.id.pdfView_activity);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(PDFShow.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(PDFShow.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    public void displayFromUri(InputStream inputStream) {
        pdfView.fromStream(inputStream)
                .defaultPage(0)
                .enableSwipe(true) // allows to block changing pages using swipe
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .swipeHorizontal(false)
                .scrollHandle(new DefaultScrollHandle(this))
                .password(null)
                .enableAntialiasing(true)
                .spacing(5) // in dp
                .onPageError(this)
                .enableDoubletap(true)
                .load();
    }

    @SuppressLint("NewApi")
    public void displayFromFile(File file) {
        Log.e("=>file", "" + file.getPath());
        Log.e("=>secretKey", "" + secretKey);

        try {
            pdfView.fromBytes(LocaleUtils.decodeFile(secretKey.getBytes(), Files.readAllBytes(file.toPath())))
                    .defaultPage(0)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .password(null)
                    .onLoad(this)
                    .swipeHorizontal(false)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(5) // in dp
                    .onPageError(this)
                    .enableDoubletap(true)
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", is, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        Utils.ProgressbarHide();
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Utils.ProgressbarHide();
        Log.e("onPageError", "Throwable => " + t.getMessage());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toasty.error(this, "error_launchPicker", Toasty.LENGTH_SHORT).show();
        }
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            isFullscreen = true;
            lyToolbar.setVisibility(View.GONE);
        } else {
            isFullscreen = false;
            lyToolbar.setVisibility(View.VISIBLE);
        }
    }

    class PdfAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Utils.ProgressBarShow(PDFShow.this);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL myURL = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                Log.e("uri", "" + uri.toString());

            } catch (Exception e) {
                Log.e("PdfAsyncTask", "Exception => " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("onPostExecute", "=> " + s);
            Utils.ProgressbarHide();
            displayFromUri(is);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
            fbAdView = null;
        }
    }

}