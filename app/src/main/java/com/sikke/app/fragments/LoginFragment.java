package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.BuildConfig;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.interfaces.NavigationInterface;
import com.sikke.app.R;
import com.sikke.app.activities.AccountWrapperActivity;
import com.sikke.app.database.DataBaseHelper;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.helpers.AppHelper.toHexString;


public class LoginFragment extends Fragment {

    @BindView(R.id.txt_forgotPassword)
    TextView txt_forgotPassword;
    @BindView(R.id.txt_createAccount)
    TextView txt_createAccount;
    @BindView(R.id.txt_goBack)
    TextView txt_goBack;
    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.input_email)
    EditText input_email;
    @BindView(R.id.input_pass)
    EditText input_password;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    public String access_token;
    public String refresh_token;
    private DataBaseHelper db;

    public static LoginFragment newInstance(){
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, rootView);
        db = new DataBaseHelper(getActivity().getApplicationContext());

        avloadingIndicatorView.hide();

        if (BuildConfig.DEBUG){
            input_email.setText("sikkedev@gmail.com");
            input_password.setText("sikke002");
        }

        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    class LoginAccountAsyncTask extends AsyncTask<String, Void, Void> {

        private boolean isSuccess;
        ArrayList<String> errorMessages = new ArrayList<>();
        ArrayList<String> successMessages = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
            btn_login.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!isSuccess) {
                if(!errorMessages.isEmpty()){
                    for (int i = 0; i< errorMessages.size(); i++){
                        Toast.makeText(getActivity(), errorMessages.get(i).toString(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(),  R.string.server_is_not_responding_please_try_again , Toast.LENGTH_LONG).show();
                }

            }else {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("btn_name","my_wallet");
                startActivity(intent);
                input_email.setText("");
                input_password.setText("");
            }
            avloadingIndicatorView.hide();
            btn_login.setEnabled(true);

        }

        protected Void doInBackground(String... urls) {

            String emailText = input_email.getText().toString().trim();
            String passwordText = input_password.getText().toString();

            OkHttpClient client = AppHelper.getOkhttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("grant_type", "password")
                    .addFormDataPart("username", emailText)
                    .addFormDataPart("password", passwordText)
                    .build();

            Request request = AppHelper.getOkhttpRequestBuilder()
                    .url(AppConstants.GET_ACCESS_TOKEN )
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();

                String jsonData = response.body().string();
                JSONObject JloginObject = new JSONObject(jsonData);
                String error = JloginObject.optString("status");
                JSONArray errorsAsList = JloginObject.optJSONArray("errorsAsList");
                String message = JloginObject.optString("message");

                if(error.equals("error")){
                    isSuccess =false;

                    if(errorsAsList != null){
                        for(int i = 0; i < errorsAsList.length(); i++){
                            Log.v("error",errorsAsList.get(i).toString());
                            errorMessages.add(errorsAsList.get(i).toString());
                        }
                    }else {
                        Log.v("error",message);
                        errorMessages.add(message);
                    }

                    return null;
                }else {

                    successMessages.add(message);
                    access_token = JloginObject.getString("access_token");
                    refresh_token = JloginObject.getString("refresh_token");

                    byte[] pin_byte = AES256Cipher.getRandomAesCryptKey() ;
                    byte[] iv_byte = AES256Cipher.getRandomAesCryptIv();
                    assert pin_byte != null;
                    String pin_hex  = toHexString(pin_byte);
                    String iv_hex = toHexString(iv_byte);
                    String password_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, passwordText);

                    Prefs.putString(U_PASSWORD, passwordText);
                    Prefs.putBoolean(IS_LOGGEDIN_BOOLEAN, true);

                    addUserToDB(emailText, password_encrypt, pin_hex, iv_hex,1, access_token, refresh_token);

                }

                isSuccess = true;

                sendOneSignalNotification();

            } catch (Exception e) {
                Log.v("server error", String.valueOf(errorMessages));

                isSuccess = false;
                e.printStackTrace();
            }

            return null;

        }

    }

    private void sendOneSignalNotification() {

            OneSignal.idsAvailable((userId, registrationId) -> {
                try {
                    OneSignal.postNotification(new JSONObject("{'contents': {'en':'You Logined Mobile Wallet', 'tr':'Mobil Cüzdana Giriş Yaptınız.'}, " +
                                    "'include_player_ids': ['" + userId + "'], " +
                                    "'headings': {'en': 'Warning Login!' , 'tr': 'Sikke Mobil Cüzdan'}, " +
                                    "'data': {'openURL': 'https://imgur.com'}" +
                                    "}"),
                            new OneSignal.PostNotificationResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    Log.i("OneSignalExample", "postNotification Success: " + response);
                                }

                                @Override
                                public void onFailure(JSONObject response) {
                                    Log.e("OneSignalExample", "postNotification Failure: " + response);
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            });

    }

    private void addUserToDB(String u_email, String password, String pin_code,String iv_code, int is_loggedin,  String access_token, String refresh_token) {
        long id = db.insertUser(u_email,password,pin_code,iv_code, is_loggedin,access_token,refresh_token );
    }

    public void onLoginFailed() {
        Toast.makeText(getActivity(), R.string.login_error, Toast.LENGTH_LONG).show();

        btn_login.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = input_email.getText().toString().trim();
        String password = input_password.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            input_email.setError(null);
        }

        if (password.isEmpty() ) {
            input_password.setError(getText(R.string.enter_valid_password));
            valid = false;
        } else {
            input_password.setError(null);
        }

        return valid;
    }

    @OnClick(R.id.btn_login)
    void loginButtonClicked(){
        if (!validate()) {
            onLoginFailed();
            return;
        }
        new LoginAccountAsyncTask().execute();
    }

    @OnClick(R.id.txt_forgotPassword)
    void forgotPasswordClicked(){
        Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
        intent.putExtra("btn_name","forgot_password");
        startActivity(intent);
    }

    @OnClick(R.id.txt_createAccount)
    void createAccountClicked(){
        Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
        intent.putExtra("btn_name","create");
        startActivity(intent);
    }

    @OnClick(R.id.txt_goBack)
    void goBackClicked(){
        getActivity().onBackPressed();
    }


}
