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


public class CreateAccountFragment extends Fragment {

    @BindView(R.id.input_email)
    EditText input_email;
    @BindView(R.id.input_pass)
    EditText input_password;
    @BindView(R.id.btn_create_account)
    Button btn_create;

    public static CreateAccountFragment newInstance() {

        return new CreateAccountFragment();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_account, container, false);
        ButterKnife.bind(this, rootView);


        return rootView;
    }


    @OnClick(R.id.btn_create_account)
    void createAccountClicked() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        new CreateAccountAsyncTask().execute();
    }


    class CreateAccountAsyncTask extends AsyncTask<String, Void, Void> {

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
                Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
                intent.putExtra("btn_name","login");
                startActivity(intent);
            }
        }

        protected Void doInBackground(String... urls) {

            String emailText = input_email.getText().toString();
            String passwordText = input_password.getText().toString();

            OkHttpClient client = AppHelper.getOkhttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", emailText)
                    .addFormDataPart("password", passwordText)
                    .addFormDataPart("password_confirm", passwordText)
                    .build();

            Request request = AppHelper.getOkhttpRequestBuilder()
                    .url(AppConstants.SIGN_UP )
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();

                String jsonData = response.body().string();
                JSONObject JcreateAccountObject = new JSONObject(jsonData);
                String error = JcreateAccountObject.optString("status");
                JSONArray errorsAsList = JcreateAccountObject.optJSONArray("errorsAsList");
                String message = JcreateAccountObject.optString("message");
//                    JSONObject errors= Jobject.getJSONObject("errors");
//                    JSONArray emailErrors = errors.optJSONArray("email");

                if(error.equals("error")){
                    isSuccess =false;

                    for(int i = 0; i < errorsAsList.length(); i++){
                        Log.v("error",errorsAsList.get(i).toString());
                        errorMessages.add(errorsAsList.get(i).toString());
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

    public void onSignupFailed() {
        Toast.makeText(getActivity(), R.string.create_account_error, Toast.LENGTH_LONG).show();

        btn_create.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = input_email.getText().toString();
        String password = input_password.getText().toString();

        if (email.isEmpty()||!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            input_email.setError(null);
        }

        if (password.isEmpty()) {
            input_password.setError(getText(R.string.enter_valid_password));
            valid = false;
        } else {
            input_password.setError(null);
        }

        return valid;
    }


    @OnClick(R.id.txt_login)
    void loginClicked(){
        Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
        intent.putExtra("btn_name","login");
        startActivity(intent);
    }

    @OnClick(R.id.txt_goBack)
    void goBackClicked(){
        getActivity().onBackPressed();
    }

}
