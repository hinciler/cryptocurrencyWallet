package com.sikke.app;

import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.thirdParty.AES256Cipher;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.ACCESS_TOKEN;
import static com.sikke.app.AppConstants.REFRESH_TOKEN;

public class AccessToken {


    public static String access_token;
    public static String getAccessToken(User user, DataBaseHelper db){

        try {

            String accessToken =  user.getAccessToken();
            String refreshToken = user.getRefreshToken();
            String uEmail = user.getEmail();
            String uPasswordEncrypt = user.getPassword();

            byte[] key = AppHelper.hexStringToByteArray(user.getPinCode());
            byte[] iv = AppHelper.hexStringToByteArray(user.getIvCode());
            String uPasswordDecrypt = AES256Cipher.decrypt(key,iv, uPasswordEncrypt);

//            Log.v("acces_toke_decrypt", accessTokenDecrypt);

            OkHttpClient client = AppHelper.getOkhttpClient();

            Request request_current_user = AppHelper.getOkhttpRequestBuilder()
                    .addHeader("Authorization", "Bearer "+ accessToken)
                    .url(AppConstants.GET_CURRENT_USER)
                    .get()
                    .build();

            Response response_current_user = client.newCall(request_current_user).execute();
//            Log.v("current_user", response_current_user.body().string());

            if (response_current_user.code() == AppConstants.UNAUTHORIZED){
                Log.v("response error code", String.valueOf(response_current_user.code()));

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("grant_type", "refresh_token")
                        .addFormDataPart("refresh_token", refreshToken)
                        .build();

                Request request_refresh_token = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_REFRESH_TOKEN)
                        .post(requestBody)
                        .build();

                Response response_refresh_token = client.newCall(request_refresh_token).execute();
                String jsonRefreshTokenData = response_refresh_token.body().string();
                Log.v("errorRefresh",jsonRefreshTokenData);

                if(response_refresh_token.code() == AppConstants.UNAUTHORIZED){
                    RequestBody requestLoginBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("grant_type", "password")
                            .addFormDataPart("username", uEmail)
                            .addFormDataPart("password", uPasswordDecrypt)
                            .build();

                    Request request_login = AppHelper.getOkhttpRequestBuilder()
                            .url(AppConstants.GET_ACCESS_TOKEN )
                            .post(requestLoginBody)
                            .build();

                    Response response_access_token = client.newCall(request_login).execute();

                    String jsonGetAccessData = response_access_token.body().string();
                    JSONObject JloginObject = new JSONObject(jsonGetAccessData);
                    String error = JloginObject.optString("status");

                    access_token =JloginObject.getString("access_token");
                    String refresh_token =JloginObject.getString("refresh_token");

                    user.setAccessToken(access_token);
                    user.setRefreshToken(refresh_token);

                    db.updateUser(user);
                    Prefs.putString(ACCESS_TOKEN, access_token);
                    Prefs.putString(REFRESH_TOKEN, refresh_token);
                    return access_token;

                }else {
                    JSONObject JRefreshTokenObject = new JSONObject(jsonRefreshTokenData);
                    String errorRefresh = JRefreshTokenObject.optString("status");
                    Log.v("errorRefresh", errorRefresh);
                    access_token =JRefreshTokenObject.getString("access_token");
                    user.setAccessToken(access_token);
                    db.updateUser(user);
                    Prefs.putString(ACCESS_TOKEN, access_token);
                    return access_token;

                }

            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
     return access_token;
    }
}
