package com.kriztech.book.Utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DownloadEpub implements OnHighlightListener, ReadLocatorListener, FolioReader.OnClosedListener {

    private PrefManager prefManager;
    private static final String TAG = DownloadEpub.class.getSimpleName();
    private Activity activity;

    private FolioReader folioReader;
    private Config config;
    private ArrayList<HighLight> highLightslist;

    private List<Result> bookDetailsList = new ArrayList<>();
    private List<com.kriztech.book.Model.MagazineModel.Result> magazineDetailsList = new ArrayList<>();
    private File docFile = null;
    private String mainKeyAlias = "", docType = "";
    private int fileLength;
    private boolean isDownloaded;

    public DownloadEpub(Activity activity) {
        this.activity = activity;
        prefManager = new PrefManager(activity);
    }

    public void pathEpub(String path, String id, String type, boolean isDownlaoded) {
        this.docType = type;
        this.isDownloaded = isDownlaoded;

        // declare the dialog as a member field of your activity
        getHighlightAndSave();
        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this);

        config = AppUtil.getSavedConfig(activity);
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

        Log.e("path =>", "" + path);
        Log.e("id =>", "" + id);
        Log.e("docType =>", "" + docType);
        Log.e("isDownloaded =>", "" + isDownloaded);
        if (!isDownloaded) {
            checkAndDownload(path, "fromUserData");
        } else {
            FolioReader folioReader = FolioReader.get();
            folioReader.openBook(path);
        }
    }

    @SuppressLint("NewApi")
    public void pathEpub(String path, String id, String type, String secretKey) {
        this.docType = type;
        this.mainKeyAlias = secretKey;
        this.isDownloaded = true;

        // declare the dialog as a member field of your activity
        getHighlightAndSave();
        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this);

        config = AppUtil.getSavedConfig(activity);
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

        Log.e("path =>", "" + path);
        Log.e("id =>", "" + id);
        Log.e("docType =>", "" + docType);
        Log.e("mainKeyAlias =>", "" + mainKeyAlias);

        docFile = new File(path);
        FolioReader folioReader = FolioReader.get();
        try {
            FileUtils.writeByteArrayToFile(docFile, LocaleUtils.decodeFile(mainKeyAlias.getBytes(), Files.readAllBytes(docFile.toPath())));
            folioReader.openBook(docFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pathEpub(List<Result> bookDetailList, List<com.kriztech.book.Model.MagazineModel.Result> magazineDetailList, String type) {
        this.bookDetailsList = bookDetailList;
        this.magazineDetailsList = magazineDetailList;
        this.docType = type;
        this.isDownloaded = false;

        // declare the dialog as a member field of your activity
        getHighlightAndSave();
        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this);

        config = AppUtil.getSavedConfig(activity);
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

        Log.e("docType ==>", "" + docType);
        if (docType.equalsIgnoreCase("book")) {
            Log.e(docType + " URL =>", "" + bookDetailsList.get(0).getUrl());
            Log.e(docType + " ID =>", "" + bookDetailsList.get(0).getId());

            if (!TextUtils.isEmpty(bookDetailsList.get(0).getUrl())) {
                checkAndDownload(bookDetailsList.get(0).getUrl(), "full");
            } else {
                Toasty.info(activity, "" + activity.getResources().getString(R.string.this_file_is_not_available), Toasty.LENGTH_SHORT).show();
            }
        } else if (docType.equalsIgnoreCase("magazine")) {
            Log.e(docType + " URL =>", "" + magazineDetailsList.get(0).getUrl());
            Log.e(docType + " ID =>", "" + magazineDetailsList.get(0).getId());

            if (!TextUtils.isEmpty(magazineDetailsList.get(0).getUrl())) {
                checkAndDownload(magazineDetailsList.get(0).getUrl(), "full");
            } else {
                Toasty.info(activity, "" + activity.getResources().getString(R.string.this_file_is_not_available), Toasty.LENGTH_SHORT).show();
            }
        } else if (docType.equalsIgnoreCase("samplebook")) {
            Log.e(docType + " URL =>", "" + bookDetailList.get(0).getSampleUrl());
            Log.e(docType + " ID =>", "" + bookDetailList.get(0).getId());

            if (!TextUtils.isEmpty(bookDetailList.get(0).getSampleUrl())) {
                checkAndDownload(bookDetailList.get(0).getSampleUrl(), "sample");
            } else {
                Toasty.info(activity, "" + activity.getResources().getString(R.string.this_file_is_not_available), Toasty.LENGTH_SHORT).show();
            }
        } else if (docType.equalsIgnoreCase("samplemagazine")) {
            Log.e(docType + " URL =>", "" + magazineDetailsList.get(0).getSampleUrl());
            Log.e(docType + " ID =>", "" + magazineDetailsList.get(0).getId());

            if (!TextUtils.isEmpty(magazineDetailsList.get(0).getSampleUrl())) {
                checkAndDownload(magazineDetailsList.get(0).getSampleUrl(), "sample");
            } else {
                Toasty.info(activity, "" + activity.getResources().getString(R.string.this_file_is_not_available), Toasty.LENGTH_SHORT).show();
            }
        }
    }

    private void getHighlightAndSave() {
        new Thread(() -> {
            highLightslist = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                highLightslist = objectMapper.readValue(loadAssetTextAsString("highlights/highlights_data.json"), new TypeReference<List<HighlightData>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (highLightslist == null) {
                folioReader.saveReceivedHighLights(highLightslist, new OnSaveHighlight() {
                    @Override
                    public void onFinished() {
                        Log.i("LOG_TAG", "-> saveReadLocator -> " + highLightslist);
                    }
                });
            }
        }).start();
    }

    private String loadAssetTextAsString(String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = activity.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("HomeActivity", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("HomeActivity", "Error closing asset " + name);
                }
            }
        }
        return null;
    }

    @Override
    public void onFolioReaderClosed() {
        FolioReader.clear();
    }

    @Override
    public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {
    }

    @Override
    public void saveReadLocator(ReadLocator readLocator) {
        Log.i("readLocator", "-> saveReadLocator -> " + readLocator.toJson());
    }

    /*========= Download START =========*/

    private void checkAndDownload(String documentURL, String viewFrom) {
        try {
            if (documentURL != null) {
                Log.e("=> documentURL", "" + documentURL);
                Log.e("=> docType", "" + docType);
                Log.e("=> viewFrom", "" + viewFrom);

                String saveDocName = "";
                if (viewFrom.equalsIgnoreCase("fromUserData")) {
                    if (documentURL.contains(".epub") || documentURL.contains(".EPUB")) {
                        saveDocName = "" + System.currentTimeMillis() + prefManager.getLoginId() + ".epub";
                    }
                } else {
                    if (documentURL.contains(".epub") || documentURL.contains(".EPUB")) {
                        if (docType.equalsIgnoreCase("book") || docType.equalsIgnoreCase("samplebook")) {
                            saveDocName = docType + "_" + bookDetailsList.get(0).getTitle().replaceAll("[, $ # * & @ > < . : ' + ! ;]", "").toLowerCase() + prefManager.getLoginId() + ".epub";
                        } else {
                            saveDocName = docType + "_" + magazineDetailsList.get(0).getTitle().replaceAll("[, $ # * & @ > < . : ' + ! ;]", "").toLowerCase() + prefManager.getLoginId() + ".epub";
                        }
                    }
                }

                String downloadDirectory;
                if (docType.equalsIgnoreCase("book") || docType.equalsIgnoreCase("samplebook")) {
                    downloadDirectory = Functions.getAppFolder(activity) + activity.getResources().getString(R.string.books) + "/";
                } else {
                    downloadDirectory = Functions.getAppFolder(activity) + activity.getResources().getString(R.string.magazines) + "/";
                }
                Log.e(TAG, "saveDocName => " + saveDocName);
                Log.e(TAG, "downloadDirectory => " + downloadDirectory);

                File file = new File(downloadDirectory);
                if (!file.exists()) {
                    Log.e(TAG, "Document directory created again");
                    file.mkdirs();
                }

                File checkFile;
                checkFile = new File(file, saveDocName);
                Log.e(TAG, "checkFile exists ? => " + checkFile.exists());

                if (!checkFile.exists()) {

                    docFile = checkFile;
                    Log.e(TAG, "docFile => " + docFile);
                    new DownloadAndSave().execute(documentURL);

                } else {
                    Log.e(TAG, "checkFile length => " + checkFile.length());
                    if (checkFile.length() > 0) {
                        FolioReader folioReader = FolioReader.get();
                        folioReader.openBook(checkFile.getPath());
                    } else {
                        docFile = checkFile;
                        Log.e(TAG, "checkFile => " + checkFile);
                        new DownloadAndSave().execute(documentURL);
                    }
                }

            }
        } catch (Exception e) {
            Log.e("checkAndDownload", "Exception => " + e);
            e.printStackTrace();
        }
    }

    public class DownloadAndSave extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            Functions.showDeterminentLoader(activity, false, false);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            HttpURLConnection connection = null;
            FileOutputStream fos = null;
            try {
                Log.e(TAG, "mainKeyAlias ===>>> " + mainKeyAlias);
                Log.e(TAG, "pdfFile ===>>> " + docFile);
                Log.e(TAG, "sUrl ===>>> " + sUrl[0]);

                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setChunkedStreamingMode(0);
                connection.setDoInput(true);
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }
                Log.i(TAG, "Response " + connection.getResponseCode());

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                fileLength = connection.getContentLength();
                Log.e(TAG, "fileLength ===>>> " + fileLength);

                // download the file
                input = connection.getInputStream();
                fos = new FileOutputStream(docFile);

                byte data[] = new byte[1024];
                long downloaded = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    downloaded += count;
                    // publishing the progress....
                    if (fileLength > 0) {
                        // only if total length is known
                        // An Android service handler is a handler running on a specific background thread.
                        publishProgress((int) (downloaded * 100 / fileLength));
                    }
                    fos.write(data, 0, count);
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception ===>>>> " + e);
                return e.toString();
            } finally {
                try {
                    Log.e(TAG, "finally docFile ===>>>> " + docFile);
                    Log.e(TAG, "docFile size ===>>>> " + docFile.length());
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (IOException ignored) {
                    Log.e(TAG, "IOException ===>>>> " + ignored);
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            Log.e("<<==== progress", "" + progress[0] + " ====>>");
            Functions.showLoadingProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Work Done! PostExecute");
            Log.e(TAG, "onPostExecute ===>>>> " + result);
            Functions.cancelDeterminentLoader();
            if (result == null) {
                scanFile(docFile.getPath());
            }
        }

    }

    public void scanFile(String downloadDoc) {
        MediaScannerConnection.scanFile(activity, new String[]{downloadDoc}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @SuppressLint("NewApi")
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("onScanCompleted", "path => " + path);
                        Log.e("onScanCompleted", "downloadDoc => " + downloadDoc);
                        docFile = new File(path);
                        if (docFile != null) {
                            FolioReader folioReader = FolioReader.get();
                            try {
                                Log.e(TAG, "isDownloaded ==>> " + isDownloaded);
                                if (isDownloaded) {
                                    FileUtils.writeByteArrayToFile(docFile, LocaleUtils.decodeFile(mainKeyAlias.getBytes(), Files.readAllBytes(docFile.toPath())));
                                }
                                folioReader.openBook(docFile.getPath());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toasty.info(activity, "" + activity.getResources().getString(R.string.this_file_is_not_available), Toasty.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*========= Download END =========*/

}