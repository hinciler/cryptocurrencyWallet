package com.sikke.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class AppConstants {

    //185.195.255.169
    public static String BASE_URL =  "http://api.sikke.network/v1/";
    public static String SIGN_UP = BASE_URL+"auth/signup";
    public static String FORGOT_PASSWORD = BASE_URL+"auth/forgot_password";
    public static String GET_ACCESS_TOKEN = BASE_URL+"oauth/token";
    public static String GET_REFRESH_TOKEN = BASE_URL+"oauth/refresh_token";
    public static String GET_WALLETS = BASE_URL+"wallet/user_wallets";
    public static String GET_WALLET_BALANCE = BASE_URL+"wallet/balance/";
    public static String GET_CURRENT_USER = BASE_URL+"user/current";
    public static String PUT_USER_PASSWORD = BASE_URL+"user/current/change_password";
    public static String GET_WALLET_ITEM = BASE_URL+"wallet";
    public static String GET_TX_URL = BASE_URL+"tx";
    public static String CREATE_WALLET = BASE_URL+"wallet/generate_wallet_number_from_public_key";
    public static String URL_PARIFIX = "http://www.parifix.com";
    public static String URL_SIKKE ="https://sikke.com.tr";
    public static String URL_HELP = "https://sikke.com.tr/android-help.html";
    public static String CLIENT_ID = "a202fb3bcc83556c176c5d3e4cdfe05cd68a8bd79b64825037c5e5c6c472d1f0";
    public static String CLIENT_SECRET = "689519448c2b51d85cf9138097f2789f6b91a67e2ddefcbdb2cc07f947fff06a";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String PVT_KEY = "pvt_key";
    public static final String PUB_KEY = "pub_key";
    public static final String WALLET_NUMBER = "wallet_number";
    public static final String ALIAS_NAME = "alias_name";
    public static final String U_EMAIL = "u_email";
    public static final String U_PASSWORD = "u_password";
    public static final String IS_LOGGEDIN = "is_loggedin";
    public static final String IS_LOGGEDIN_BOOLEAN = "is_loggedin";
    public static final String PINCODE_STEP = "pincode_step";
    public static final String PIN_CODE = "pin_code";
    public static final String PIN_TRY = "pin_try";
    public static final String IS_FIRST_CREATED = "is_first_created";
    public static final String DEFAULT_WALLET = "0";
    public static final String ONLY_IN_WALLET = "1";
    public static final String ONLY_OUT_WALLET = "2";
    public static final String STATUS_OPEN = "1";
    public static final String STATUS_CLOSED = "0";
    public static final String NOTIFICATION_OPEN = "1";
    public static final String NOTIFICATION_CLOSED = "0";
    public static final String TIMESTAMP = String.valueOf(System.currentTimeMillis()/1000);
    public static String LANGUAGE = "";

    //Http Request Codes
    public static final int UNAUTHORIZED = 403;

    public static void setValuesFromSharedPreferences(Context context) {
        if (BuildConfig.DEBUG) {
            REQUEST_CONNECTION_TIMEOUT_SECONDS = 15;
            REQUEST_READ_TIMEOUT_SECONDS = 10;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        LANGUAGE = prefs.getString("language", LANGUAGE);

    }



    public static Long tsLong = System.currentTimeMillis()/1000;
    public static String NONCE= tsLong.toString();

    // http request settings
    public static int REQUEST_CONNECTION_TIMEOUT_SECONDS = 10;
    public static int REQUEST_READ_TIMEOUT_SECONDS = 20;


}
