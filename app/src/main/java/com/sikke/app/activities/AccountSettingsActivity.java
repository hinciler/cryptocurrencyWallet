package com.sikke.app.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.AppConstants;
import com.sikke.app.Base58;
import com.sikke.app.MyApplication;
import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.database.model.Wallet;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.thirdParty.AES256Cipher;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.DEFAULT_WALLET;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.helpers.AppHelper.toHexString;


public class AccountSettingsActivity extends AppCompatPreferenceActivity {

    private static DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setupActionBar();
        db = new DataBaseHelper(this);

        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_account_settings));

        new GetUserInfoAsyncTask().execute();
    }

    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else {
            if(preference.getKey().equals("password")){
                preference.setSummary(stringValue.replaceAll(".","*"));
            }else {
                preference.setSummary(stringValue);
            }
        }
        return true;
    };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }



    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || LanguagePreferenceFragment.class.getName().equals(fragmentName)
                || SecurityAccountPreferenceFragment.class.getName().equals(fragmentName)
                || ChangePasswordPreferenceFragment.class.getName().equals(fragmentName);
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class GetUserInfoAsyncTask extends  AsyncTask <String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            try {

                User user=  db.getUser(1);
                OkHttpClient client = AppHelper.getOkhttpClient();

                String accessToken = user.getAccessToken();
                Request request_current_user = AppHelper.getOkhttpRequestBuilder()
                        .addHeader("Authorization", "Bearer "+ accessToken)
                        .url(AppConstants.GET_CURRENT_USER)
                        .get()
                        .build();

                Response response_current_user = client.newCall(request_current_user).execute();

                String jsonData = response_current_user.body().string();

                JSONObject jsonObjectUser = new JSONObject(jsonData);

                String user_info = jsonObjectUser.optString("user");

                JSONObject jsonObject = new JSONObject(user_info);

                String alias_name = jsonObject.optString("alias_name");
                String name = jsonObject.optString("name");
                String surname = jsonObject.optString("surname");
                String phone = jsonObject.optString("phone");
                String phone_country = jsonObject.optString("phone_country");
                String email = jsonObject.optString("email");
                String identity_number = jsonObject.optString("identity_number");
                String country = jsonObject.optString("country");
                String city = jsonObject.optString("city");
                String district = jsonObject.optString("district");
                String insert_date = jsonObject.optString("insert_date");
                String last_login_time = jsonObject.optString("last_login_time");
                String status = jsonObject.optString("status");
                String tax_office = jsonObject.optString("tax_office");
                String tax_title = jsonObject.optString("tax_title");
                String security_question = jsonObject.optString("security_question");
                String security_answer = jsonObject.optString("security_answer");
                String tz = jsonObject.optString("tz");

                user.setAlias(alias_name);
                user.setName(name);
                user.setSurname(surname);
                user.setPhone(phone);
                user.setPhoneCountry(phone_country);
                user.setIdentityNumber(identity_number);
                user.setCountry(country);
                user.setCity(city);
                user.setDistrict(district);
                user.setLastLogin(last_login_time);
                user.setStatus(status);
                user.setTaxOffice(tax_office);
                user.setTaxTitle(tax_title);
                user.setTz(tz);
                user.setSecurityQuestion(security_question);
                user.setSecurityAnswer(security_answer);
                db.updateUser(user);


            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements AdapterView.OnItemSelectedListener {
        @BindView(R.id.input_name)
        EditText input_name;
        @BindView(R.id.input_surname)
        EditText input_surname;
        @BindView(R.id.input_nickname)
        EditText input_nickname;
        @BindView(R.id.input_phone)
        EditText input_phone;
        @BindView(R.id.spinner_countries)
        Spinner spinner_countries;
        @BindView(R.id.spinner_country_code)
        Spinner spinner_country_code;
        @BindView(R.id.avloadingIndicatorView)
        AVLoadingIndicatorView avloadingIndicatorView;

        User user=  db.getUser(1);
        public String country_item;
        public String phone_country_item;
        Map<String,String> countryListMap = new HashMap<>();
        Map<String,String> countryShortListMap = new HashMap<>();

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_myaccount, container, false);
            ButterKnife.bind(this, rootView);

            getJsonFromAssets();
            avloadingIndicatorView.hide();
            // Spinner click listener
            spinner_countries.setOnItemSelectedListener(this);
            spinner_country_code.setOnItemSelectedListener(this);
            setAccounInfoText();

            return rootView;
        }

        private int getIndex(Spinner spinner, String myString){
            for (int i=0;i<spinner.getCount();i++){
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                    return i;
                }
            }
            return 0;
        }

        private void setSpinnerAdapter(String[] spinnerArray, Spinner spinner) {

            ArrayAdapter<String> adapter =new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // On selecting a spinner country_item

            switch (parent.getId()) {
                case R.id.spinner_countries:
                    country_item = parent.getSelectedItem().toString();

                    Log.d("form","regionid:" + country_item);
                    break;
                case R.id.spinner_country_code:
                    phone_country_item =  parent.getSelectedItem().toString();
                    Log.d("form","state id:" + phone_country_item);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

        private void setAccounInfoText() {
            input_name.setText(user.getName());
            input_surname.setText(user.getSurname());
            input_nickname.setText(user.getAlias());
            input_phone.setText(user.getPhone());
            spinner_countries.setSelection(getIndex(spinner_countries, countryListMap.get(user.getCountry().toUpperCase())));
            spinner_country_code.setSelection(getIndex(spinner_country_code, "+" + user.getPhoneCountry()));
        }

        public void getJsonFromAssets() {
            try {
                JSONArray countryArray = new JSONArray(AppHelper.loadJSONFromAsset(getActivity().getApplicationContext()));
                String[] spinner_country_list = new String[countryArray.length()];
                String[] spinner_phone_country = new String[countryArray.length()];
                for(int i=0; i<countryArray.length(); i++){
                    JSONObject countryObject = countryArray.getJSONObject(i);
                    String country_name = countryObject.getString("UlkeAdi");
                    String country_code = countryObject.getString("IkiliKod");
                    spinner_phone_country[i] = "+" + countryObject.getString("TelKodu");
                    countryListMap.put(country_code,country_name);
                    countryShortListMap.put(country_name,country_code);
                    spinner_country_list[i] = countryObject.getString("UlkeAdi");
//                    Log.v("wallet_number",countryListMap.get(country_name));
                }
                setSpinnerAdapter(spinner_country_list, spinner_countries);
                setSpinnerAdapter(spinner_phone_country, spinner_country_code);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("StaticFieldLeak")
        class UpdateUserSettingAsyncTask extends AsyncTask<String, Void, Void> {

            String name = input_name.getText().toString();
            String surname = input_surname.getText().toString();
            String nickname = input_nickname.getText().toString();
            String phone = input_phone.getText().toString();
            String country =countryShortListMap.get(country_item);
            String phone_country =phone_country_item.substring(1);


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                avloadingIndicatorView.show();
            }

            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Toast.makeText(getActivity(),  R.string.user_saved , Toast.LENGTH_LONG).show();
                avloadingIndicatorView.hide();
            }


            @Override
            protected Void doInBackground(String... strings) {
                try {
                    String accessToken =  user.getAccessToken();
                    OkHttpClient client = AppHelper.getOkhttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .addFormDataPart("name", name)
                            .addFormDataPart("surname", surname)
                            .addFormDataPart("alias_name", nickname)
                            .addFormDataPart("phone", phone)
                            .addFormDataPart("country", country.toLowerCase())
                            .addFormDataPart("phone_country", phone_country)
                            .setType(MultipartBody.FORM)
                            .build();

                    Request request = AppHelper.getOkhttpRequestBuilder()
                            .addHeader("Authorization", "Bearer "+ accessToken)
                            .url(AppConstants.GET_CURRENT_USER)
                            .put(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    JSONObject JWalletObject = new JSONObject(jsonData);
                    Log.v("jsondata", jsonData);

                    user.setAlias(nickname);
                    user.setName(name);
                    user.setSurname(surname);
                    user.setPhone(phone);
                    user.setPhoneCountry(phone_country);
                    user.setCountry(country);
                    db.updateUser(user);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }

        @OnClick(R.id.btn_save)
        void saveButtonClicked() {
            new UpdateUserSettingAsyncTask().execute();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SecurityAccountPreferenceFragment extends PreferenceFragment {

        // fragment_account_security
        @BindView(R.id.input_security_question)
        EditText input_security_question;
        @BindView(R.id.input_security_answer)
        EditText input_security_answer;
        @BindView(R.id.input_identity_number)
        EditText input_identity_number;
        @BindView(R.id.avloadingIndicatorView)
        AVLoadingIndicatorView avloadingIndicatorView;

        User user=  db.getUser(1);


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_account_security, container, false);
            ButterKnife.bind(this, rootView);
            avloadingIndicatorView.hide();

            setAccounInfoText();

            return rootView;
        }

        private void setAccounInfoText() {
            input_security_question.setText(user.getSecurityQuestion());
            input_security_answer.setText(user.getSecurityAnswer());
            input_identity_number.setText(user.getIdentityNumber());

        }


        @SuppressLint("StaticFieldLeak")
        class UpdateUserSecurityAsyncTask extends AsyncTask<String, Void, Void> {

            String security_question = input_security_question.getText().toString();
            String security_answer = input_security_answer.getText().toString();
            String identity_number = input_identity_number.getText().toString();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                avloadingIndicatorView.show();
            }

            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Toast.makeText(getActivity(),  R.string.user_saved , Toast.LENGTH_LONG).show();
                avloadingIndicatorView.hide();
            }


            @Override
            protected Void doInBackground(String... strings) {
                try {
                    String accessToken =  user.getAccessToken();
                    OkHttpClient client = AppHelper.getOkhttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .addFormDataPart("security_question", security_question)
                            .addFormDataPart("security_answer", security_answer)
                            .addFormDataPart("identity_number", identity_number)
                            .setType(MultipartBody.FORM)
                            .build();

                    Request request = AppHelper.getOkhttpRequestBuilder()
                            .addHeader("Authorization", "Bearer "+ accessToken)
                            .url(AppConstants.GET_CURRENT_USER)
                            .put(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    JSONObject JWalletObject = new JSONObject(jsonData);

                    user.setSecurityQuestion(security_question);
                    user.setSecurityAnswer(security_answer);
                    user.setIdentityNumber(identity_number);
                    db.updateUser(user);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }


        @OnClick(R.id.btn_save)
        void saveButtonClicked() {
            new UpdateUserSecurityAsyncTask().execute();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ChangePasswordPreferenceFragment extends PreferenceFragment {

        @BindView(R.id.input_old_password)
        EditText input_old_password;
        @BindView(R.id.input_password)
        EditText input_password;
        @BindView(R.id.input_confirm_password)
        EditText input_confirm_password;
        @BindView(R.id.avloadingIndicatorView)
        AVLoadingIndicatorView avloadingIndicatorView;
        String user_password = Prefs.getString(U_PASSWORD, "");
        User user=  db.getUser(1);
        String accessToken =  user.getAccessToken();
        // fragment_account_security
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);
            ButterKnife.bind(this, rootView);
            avloadingIndicatorView.hide();


            return rootView;

        }

        @SuppressLint("StaticFieldLeak")
        class UpdatePasswordAsyncTask extends AsyncTask<String, Void, Void> {

            String old_password = input_old_password.getText().toString();
            String new_password = input_password.getText().toString();
            String new_password_confirm = input_confirm_password.getText().toString();
            ArrayList<String> errorMessages = new ArrayList<>();
            private boolean isSuccess;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                avloadingIndicatorView.show();
            }

            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (!isSuccess) {
                    if(!errorMessages.isEmpty()){
                        for (int i = 0; i< errorMessages.size(); i++){
                            Toast.makeText(getActivity(), errorMessages.get(i), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getActivity(),  R.string.server_is_not_responding_please_try_again , Toast.LENGTH_LONG).show();
                    }

                }else {
                    Toast.makeText(getActivity(), "Şifreniz Değişti...", Toast.LENGTH_LONG).show();
                }
                input_old_password.setText("");
                input_password.setText("");
                input_confirm_password.setText("");
                avloadingIndicatorView.hide();
            }


            @Override
            protected Void doInBackground(String... strings) {
                try {

                    OkHttpClient client = AppHelper.getOkhttpClient();

                    if(new_password.equals(new_password_confirm)){
                        if(user_password.equals(old_password)){
                            String access_token =  user.getAccessToken();
                            RequestBody requestBody = new MultipartBody.Builder()
                                    .addFormDataPart("old_password", old_password)
                                    .addFormDataPart("new_password", new_password)
                                    .addFormDataPart("new_password_confirm", new_password_confirm)
                                    .setType(MultipartBody.FORM)
                                    .build();

                            Request request = AppHelper.getOkhttpRequestBuilder()
                                    .addHeader("Authorization", "Bearer "+ accessToken)
                                    .url(AppConstants.PUT_USER_PASSWORD)
                                    .post(requestBody)
                                    .build();

                            for(int i = 0; i<db.getAllWallets().size(); i++){
                                String wallet_number = db.getAllWallets().get(i).getWalletNumber();
                                Wallet wallet= db.getWallet(wallet_number);
                                byte [] u_password =   AES256Cipher.key128Bit(user_password);
                                byte [] u_new_password =   AES256Cipher.key128Bit(new_password);
                                String pvt_key = AES256Cipher.decryptPvt(u_password, db.getAllWallets().get(i).getPrivateKey());
                                byte[] pvt_byte = Base58.decode(pvt_key);
                                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                                byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                                String public_key = Base58.encode(public_byte);

                                String encryptedPvtKey = AES256Cipher.encryptPvt(u_new_password, pvt_key);

                                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(pvt_key);
                                String sign = ECDSAHelper.sign(TIMESTAMP, privateKey);

                                RequestBody requestUpdateBody = new MultipartBody.Builder()
                                        .addFormDataPart("sign", sign)
                                        .addFormDataPart("nonce", TIMESTAMP)
                                        .addFormDataPart("w_type",  DEFAULT_WALLET)
                                        .addFormDataPart("w_pub_key", public_key)
                                        .addFormDataPart("w_zeugma",  encryptedPvtKey)
                                        .setType(MultipartBody.FORM)
                                        .build();

                                Request requestUpdate = AppHelper.getOkhttpRequestBuilder()
                                        .addHeader("Authorization", "Bearer "+ access_token)
                                        .url(AppConstants.GET_WALLET_ITEM)
                                        .put(requestUpdateBody)
                                        .build();

                                Response response = client.newCall(requestUpdate).execute();
                                String jsonData = response.body().string();

                                byte[] pin_byte = AES256Cipher.getRandomAesCryptKey() ;
                                byte[] iv_byte = AES256Cipher.getRandomAesCryptIv();
                                String pin_hex  = toHexString(pin_byte);
                                String iv_hex = toHexString(iv_byte);
                                String password_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, new_password);

                                wallet.setPrivateKey(encryptedPvtKey);
                                db.updateWallet(wallet);

                                user.setIvCode(iv_hex);
                                user.setPinCode(pin_hex);
                                user.setPassword(password_encrypt);
                                db.updateUser(user);
                                Prefs.putString(U_PASSWORD, new_password);
                            }

                            Response response = client.newCall(request).execute();
                            String jsonData = response.body().string();
                            JSONObject JWalletObject = new JSONObject(jsonData);


                        }else {
                            isSuccess =false;
                            errorMessages.add("Eski Şifre Hatalı");
                        }
                    }else {
                        isSuccess =false;
                        errorMessages.add("Şifre Eşleşmedi");
                    }

                   isSuccess = true;

                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }

        @OnClick(R.id.btn_save)
        void saveButtonClicked() {
            new UpdatePasswordAsyncTask().execute();
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LanguagePreferenceFragment extends PreferenceFragment  {

        private SharedPreferences.OnSharedPreferenceChangeListener listener;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_language);
            setHasOptionsMenu(true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //Setup a shared preference listener for hpwAddress and restart transport
            listener = (prefs1, key) -> {
                if (key.equals("language")) {

                    String languageCode = prefs1.getString(key, Locale.getDefault().getDisplayLanguage());
                    AppHelper.setLanguage(MyApplication.getInstance().getApplicationContext(), languageCode);

                    new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                            .setTitle(this.getResources().getString(R.string.warning))
                            .setMessage(this.getResources().getString(R.string.change_language))
                            .setPositiveButton(this.getResources().getString(android.R.string.ok), (dialog, which) -> {
                                getActivity().finish();
                                System.exit(0);
                            })
                            .setCancelable(false)
                            .show();

                }
            };

            prefs.registerOnSharedPreferenceChangeListener(listener);

            bindPreferenceSummaryToValue(findPreference("language"));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.v("id_value", String.valueOf(id));
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), AccountSettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
