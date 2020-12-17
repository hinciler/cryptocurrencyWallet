package com.sikke.app.helpers;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.AppConstants;
import com.sikke.app.BaseActivity;
import com.sikke.app.activities.AccountActivity;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.receivers.MyScheduledReceiver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;


import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import butterknife.internal.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.Context.ALARM_SERVICE;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.U_PASSWORD;

public class AppHelper {

    static final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    public static List<String> splitDate(String date){
        List<String > dateList = new ArrayList<>();
        StringTokenizer tk = new StringTokenizer(date);

        String newDate = tk.nextToken();  // <---  yyyy-mm-dd
        String time = tk.nextToken();  // <---  hh:mm:ss

        if (time != null && time.length() > 0 && time.charAt(time.length() - 1) == '0') {
            time = time.substring(0, time.length() - 3);
        }

        dateList.add(newDate);
        dateList.add(time);

        return dateList;

    }

    public static String createSignKey(){
        try {
            String message = AppConstants.CLIENT_ID+AppConstants.NONCE;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(AppConstants.CLIENT_SECRET.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hashBytes = sha256_HMAC.doFinal(message.getBytes());
            String hashStr = toHexString(hashBytes);

            return hashStr;

        }
        catch (NoSuchAlgorithmException e) {} catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static String toHexString(byte[] bytes) {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v/16];
            hexChars[j*2 + 1] = hexArray[v%16];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static String getDownloadFilePath(Activity activity) {
        String downloadPath = "";
        String fFolder = "Sikke/";

        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (isSDPresent) {

            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + fFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }


            downloadPath = Environment.getExternalStorageDirectory() +
                    File.separator + fFolder;

        } else {

            File folder = new File(activity.getApplication().getFilesDir() +
                    File.separator + fFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            downloadPath = activity.getApplication().getFilesDir() +
                    File.separator + fFolder;

        }
        return downloadPath;
    }

    public static OkHttpClient getOkhttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(AppConstants.REQUEST_CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AppConstants.REQUEST_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public static Request.Builder getOkhttpRequestBuilder() {
        return new Request.Builder()
                .addHeader("X-Api-Sign", createSignKey())
                .addHeader("X-Api-Nonce", AppConstants.NONCE)
                .addHeader("X-Api-Client-Id", AppConstants.CLIENT_ID)
                .addHeader("X-Lang", AppConstants.LANGUAGE);
    }

    public static void requestPermission(Activity activity){
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                    return;
                }

                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }
    }

    public static String loadJSONFromAsset(Context context) {
        String json ;
        try {
            InputStream is = context.getAssets().open("country.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String formatNumber(double number){
        DecimalFormat formatter = new DecimalFormat("###,###,###.##");

        String formattedString = formatter.format(number);

        return formattedString;
    }

    public static void setLanguage(Context context, String lang) {

        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public static void setLogout(DataBaseHelper db, Activity activity){
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        sqLiteDatabase.delete("user", null, null);
        sqLiteDatabase.delete("wallet", null, null);
        Prefs.putString(U_PASSWORD, "");
        Prefs.putBoolean(IS_LOGGEDIN_BOOLEAN, false);
        Intent intent = new Intent(activity, AccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public static void scheduleAlarm(Context context, PendingIntent pendingIntent, AlarmManager alarmManager){
        Intent myIntent = new Intent(context, MyScheduledReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("val", 8);
        myIntent.putExtras(bundle);
        pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 180);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.v("Main Activity", "Alarm Scheduled");
    }

}
