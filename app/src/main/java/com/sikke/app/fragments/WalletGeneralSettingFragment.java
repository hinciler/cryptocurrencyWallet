package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.Base58;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.Wallet;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.PrivateKey;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.DEFAULT_WALLET;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.ONLY_IN_WALLET;
import static com.sikke.app.AppConstants.ONLY_OUT_WALLET;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.U_PASSWORD;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class WalletGeneralSettingFragment extends Fragment {

    @BindView(R.id.txt_wallet_name)
    EditText txt_wallet_name;
    @BindView(R.id.txt_wallet_smart_contract_token)
    TextView txt_wallet_smart_contract_token;
    @BindView(R.id.txt_wallet_create_date)
    TextView txt_wallet_create_date;
    @BindView(R.id.radio_type)
    RadioGroup radio_type;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;


    private DataBaseHelper db;
    public String wallet_number;
    public  String decryptedPrivateKey;
    String public_key;
    public String wallet_type = "0";
    public String alias_name = "";
    public String notification = "";
    public String insert_date= " ";
    public String contract_token="";
    public String wallet_status;
    public String limit_daily;
    public String limit_hourly;
    public String limit_max_amount;

    public String type ;
    Wallet wallet;


    public static WalletGeneralSettingFragment newInstance(String wallet_number) {
        WalletGeneralSettingFragment walletGeneralSettingFragment = new WalletGeneralSettingFragment();

        Bundle args = new Bundle();
        args.putString("wallet_number", wallet_number);
        walletGeneralSettingFragment.setArguments(args);

        return walletGeneralSettingFragment;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_general_setting, container, false);
        ButterKnife.bind(this, rootView);



        if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){

            db = new DataBaseHelper(getActivity().getApplicationContext());
            wallet_number = getArguments().getString("wallet_number", "");
            wallet =  db.getWallet(wallet_number);
            String pin_code = Prefs.getString(U_PASSWORD, "");
            try {
                byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                decryptedPrivateKey = AES256Cipher.decryptPvt(u_password, wallet.getPrivateKey());
                byte[] pvt_byte = Base58.decode(decryptedPrivateKey);
                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                public_key = Base58.encode(public_byte);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        new GetWalletSettingAsyncTask().execute();

        return rootView;
    }

    class GetWalletSettingAsyncTask extends AsyncTask<String, Void, Void>{



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
                wallet.setWalletAliasName(alias_name);
                wallet.setWalletType(wallet_type);
                wallet.setInsertDate(insert_date);
                wallet.setWalletStatus(wallet_status);
                wallet.setLimitHourly(limit_hourly);
                wallet.setLimitDaily(limit_daily);
                wallet.setWalletNotification(notification);
                wallet.setLimitMaxAmount(limit_max_amount);
                db.updateWallet(wallet);
            }

            txt_wallet_name.setText(alias_name);

            if(wallet_type.equals(DEFAULT_WALLET)){
                radio_type.check(R.id.radio_wallet_standard);
            }else if(wallet_type.equals(ONLY_IN_WALLET)){
                radio_type.check(R.id.radio_wallet_moneybox);
            }else if(wallet_type.equals(ONLY_OUT_WALLET)){
                radio_type.check(R.id.radio_wallet_free);
            }

            txt_wallet_smart_contract_token.setText(contract_token);
            txt_wallet_create_date.setText(insert_date);
//            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

            avloadingIndicatorView.hide();

        }


        @Override
        protected Void doInBackground(String... strings) {
            try {

                OkHttpClient client = AppHelper.getOkhttpClient();
                Request request_wallet_item = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_WALLET_ITEM+"/"+wallet_number+"?w_pub_key="+public_key)
                        .get()
                        .build();

                Response response_wallet_item = client.newCall(request_wallet_item).execute();
                String jsonWalletData = response_wallet_item.body().string();

//                Log.v("jsonWalletData", jsonWalletData);
                JSONObject JWalletObject = new JSONObject(jsonWalletData);
                if(!JWalletObject.isNull("wallet")){
                    String  walletData = JWalletObject.getString("wallet");
                    JSONObject JWObject = new JSONObject(walletData);

                    alias_name = JWObject.optString("alias_name", "");
                    wallet_type = JWObject.optString("type", "0");
                    wallet_status = JWObject.optString("status", "1");
                    notification = JWObject.optString("notification", "1");
                    limit_daily = JWObject.optString("limit_daily","0");
                    limit_hourly = JWObject.optString("limit_hourly","0");
                    limit_max_amount = JWObject.optString("limit_max_amount","0");
                    insert_date = JWObject.optString("insert_date", "01-01-2018");
                    contract_token = JWObject.optString("contract_token", "");
                }
//                message = JWalletObject.getString("message");

//                JSONObject JStatusObject = new JSONObject(statusObject);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    class UpdateWalletSettingAsyncTask extends AsyncTask<String, Void, Void>{

        String sign_text = TIMESTAMP;
        String message;
        String alias_name = txt_wallet_name.getText().toString();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

            if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
                wallet.setWalletAliasName(alias_name);
                wallet.setWalletType(type);
                db.updateWallet(wallet);
            }
            avloadingIndicatorView.hide();

            Toast.makeText(getActivity(),  R.string.wallet_saved , Toast.LENGTH_LONG).show();
        }


        @Override
        protected Void doInBackground(String... strings) {
            try {
                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(decryptedPrivateKey);
                String sign = ECDSAHelper.sign(sign_text, privateKey);

                int selectedId = radio_type.getCheckedRadioButtonId();
                switch(selectedId){
                        case R.id.radio_wallet_standard:
                            type = "0";
                            break;
                        case R.id.radio_wallet_moneybox:
                            type = "1";
                            break;
                        case R.id.radio_wallet_free:
                            type= "2";
                            break;
                }

                Log.v("type",  type);
                OkHttpClient client = AppHelper.getOkhttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("sign", sign)
                        .addFormDataPart("nonce", TIMESTAMP)
                        .addFormDataPart("w_alias_name", alias_name)
                        .addFormDataPart("w_type",  type)
                        .addFormDataPart("w_pub_key", public_key)
                        .setType(MultipartBody.FORM)
                        .build();

                Request request = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_WALLET_ITEM)
                        .put(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                JSONObject JWalletObject = new JSONObject(jsonData);
                message = JWalletObject.optString("message");

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }


    @OnClick(R.id.btn_save)
    void saveButtonClicked() {
        new UpdateWalletSettingAsyncTask().execute();
    }

}
