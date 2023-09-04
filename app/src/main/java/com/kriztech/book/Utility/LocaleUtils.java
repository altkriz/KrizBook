package com.kriztech.book.Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.StringDef;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.SecureRandom;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LocaleUtils {

    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String Arabic = "ar";
    public static final String Hindi = "hi";
    public static final String Swahili = "sw";
    public static final String Tamil = "ta";
    public static final String Telugu = "te";
    public static final String Zulu = "zu";

    public static void initialize(Context context, @LocaleDef String defaultLanguage) {
        setLocale(context, defaultLanguage);
    }

    public static boolean setLocale(Context context, @LocaleDef String language) {
        return updateResources(context, language);
    }

    private static boolean updateResources(Context context, String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        context.createConfigurationContext(configuration);
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return true;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ENGLISH, Arabic, FRENCH, Hindi, Swahili, Tamil, Telugu, Zulu})
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = {ENGLISH, Arabic, FRENCH, Hindi, Swahili, Tamil, Telugu, Zulu};
    }


    private static SharedPreferences getDefaultSharedPreference(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance().getApplicationContext()) != null)
            return PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance().getApplicationContext());
        else
            return null;
    }

    public static void setSelectedLanguageId(String id) {
        final SharedPreferences prefs = getDefaultSharedPreference(MyApp.getInstance().getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language_id", id);
        editor.apply();
    }

    public static String getSelectedLanguageId() {
        return getDefaultSharedPreference(MyApp.getInstance().getApplicationContext())
                .getString("app_language_id", "en");
    }

    public static byte[] generateKey(String password) throws Exception {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        @SuppressLint("DeletedProvider") SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(fileData);
        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }

    /**
     * Encrypts the text.
     *
     * @param clearText The text you want to encrypt
     * @return Encrypted data if successful, or null if unsucessful
     */
    protected String encrypt(byte[] key, String clearText) {
        byte[] encryptedText = null;
        try {
            SecretKey ks = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, ks);
            encryptedText = c.doFinal(clearText.getBytes("UTF-8"));
            return Base64.encodeToString(encryptedText, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decrypts the text
     *
     * @param encryptedText The text you want to encrypt
     * @return Decrypted data if successful, or null if unsucessful
     */
    protected String decrypt(byte[] key, String encryptedText) {
        byte[] clearText = null;
        try {
            SecretKey ks = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, ks);
            clearText = c.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));
            return new String(clearText, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String decryptFile(String key, File inputFile) {
        String strFile = "";
        FileInputStream inputStream = null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            //create string from byte array
            strFile = new String(inputBytes);
        } catch (Exception ex) {
            Log.e("decryptFile", "Exception ==>> " + ex);
        } finally {
            // close the streams using close method
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ioe) {
                Log.e("decryptFile", "IOException ==>> " + ioe);
            }
        }
        return strFile;
    }

    public static File byteToFile(byte[] key, byte[] fileBytes) {
        File fileFromBytes = null;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] decrypted = cipher.doFinal(fileBytes);
            // convert byte[] to File
            ByteArrayInputStream bis = new ByteArrayInputStream(decrypted);
            ObjectInputStream ois = new ObjectInputStream(bis);
            fileFromBytes = (File) ois.readObject();
            bis.close();
            ois.close();
            FileUtils.writeByteArrayToFile(fileFromBytes, fileBytes);
        } catch (Exception e) {
            Log.e("byteToFile", "Exception ==>> " + e);
        }
        return fileFromBytes;
    }

}