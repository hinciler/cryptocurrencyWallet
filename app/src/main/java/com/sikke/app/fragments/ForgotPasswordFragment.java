package com.sikke.app.fragments;


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
import android.widget.Toast;

import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.R;
import com.sikke.app.activities.AccountWrapperActivity;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment {

    @BindView(R.id.btn_reset_password)
    Button btn_reset_password;
    @BindView(R.id.input_email)
    EditText input_email;

    public static ForgotPasswordFragment newInstance()  {

        return new ForgotPasswordFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        ButterKnife.bind(this, rootView);


        return rootView;
    }

    @OnClick(R.id.btn_reset_password)
    void resetPasswordButtonClicked(){
        if (!validate()) {
            onResetPasswordFailed();
            return;
        }

        new ResetPasswordAsyncTask().execute();

    }


    class ResetPasswordAsyncTask extends AsyncTask<String, Void, Void> {

        private boolean isSuccess;
        ArrayList errorMessages = new ArrayList();
        ArrayList successMessages = new ArrayList();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                    Toast.makeText(getActivity(),  R.string.server_is_not_responding_please_try_again , Toast.LENGTH_SHORT).show();
                }
            }else {
//                for (int i = 0; i< successMessages.size(); i++){
//                    Toast.makeText(getActivity(), successMessages.get(i).toString(), Toast.LENGTH_SHORT).show();
//                }
            }
        }

        protected Void doInBackground(String... urls) {

            String emailText = input_email.getText().toString();

            OkHttpClient client = AppHelper.getOkhttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", emailText)
                    .build();

            Request request = AppHelper.getOkhttpRequestBuilder()
                    .url(AppConstants.FORGOT_PASSWORD )
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
                        Log.v("message",jsonData);
                        errorMessages.add(message);
                    }

                    return null;
                }else {
                    successMessages.add(message);
                    Log.v("succes",jsonData);
                }

                isSuccess = true;

            } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
            }

            return null;

        }

    }

    public void onResetPasswordFailed() {
        Toast.makeText(getActivity(), R.string.reset_password_error, Toast.LENGTH_LONG).show();

        btn_reset_password.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = input_email.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            input_email.setError(null);
        }

        return valid;
    }


    @OnClick(R.id.txt_login)
    void loginClicked(){
        Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
        intent.putExtra("btn_name","login");
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
