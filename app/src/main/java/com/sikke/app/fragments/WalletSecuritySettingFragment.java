package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import static com.sikke.app.AppConstants.NOTIFICATION_CLOSED;
import static com.sikke.app.AppConstants.NOTIFICATION_OPEN;
import static com.sikke.app.AppConstants.ONLY_IN_WALLET;
import static com.sikke.app.AppConstants.ONLY_OUT_WALLET;
import static com.sikke.app.AppConstants.STATUS_CLOSED;
import static com.sikke.app.AppConstants.STATUS_OPEN;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.U_PASSWORD;

/**
 * A simple {@link Fragment} subclass.
 */
public class WalletSecuritySettingFragment extends Fragment {


    @BindView(R.id.radio_situation)
    RadioGroup radio_situation;
    @BindView(R.id.radio_notifications)
    RadioGroup radio_notifications;
    @BindView(R.id.wallet_hourly_limit)
    TextView wallet_hourly_limit;
    @BindView(R.id.wallet_daily_limit)
    TextView wallet_daily_limit;
    @BindView(R.id.wallet_one_time_limit)
    TextView wallet_one_time_limit;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    private DataBaseHelper db;
    public Wallet wallet;
    public String wallet_number;
    public String wallet_status = "1";
    public String wallet_notifications;
    public String decryptedPrivateKey;
    public String public_key;

    public static WalletSecuritySettingFragment newInstance(String wallet_number){
        WalletSecuritySettingFragment walletSecuritySettingFragment = new WalletSecuritySettingFragment();

        Bundle args = new Bundle();
        args.putString("wallet_number", wallet_number);
        walletSecuritySettingFragment.setArguments(args);

        return walletSecuritySettingFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_security_setting, container, false);
        ButterKnife.bind(this, rootView);


        if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
            db = new DataBaseHelper(getActivity().getApplicationContext());
            wallet_number = getArguments().getString("wallet_number", "");
            wallet = db.getWallet(wallet_number);

            wallet_hourly_limit.setText(wallet.getWalletLimitHourly());
            wallet_daily_limit.setText(wallet.getWalletLimitDaily());
            wallet_one_time_limit.setText(wallet.getWalletLimitMaxAmount());
            wallet_status = wallet.getWalletStatus();
            wallet_notifications = wallet.getWalletNotification();
        }

        String pin_code = Prefs.getString(U_PASSWORD, "");
        try {
            byte [] u_password =   AES256Cipher.key128Bit(pin_code);
            decryptedPrivateKey = AES256Cipher.decryptPvt(u_password, wallet.getPrivateKey() );
            byte[] pvt_byte = Base58.decode(decryptedPrivateKey);
            BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
            byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
            public_key = Base58.encode(public_byte);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new GetWalletSettingAsyncTask().execute();


            // Inflate the layout for this fragment
        return rootView;
    }

     @SuppressLint("StaticFieldLeak")
     class GetWalletSettingAsyncTask extends AsyncTask<String, Void, Void>{

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                if (wallet_status.equals(STATUS_OPEN)) {
                    radio_situation.check(R.id.radio_active);
                } else if (wallet_status.equals(STATUS_CLOSED)) {
                    radio_situation.check(R.id.radio_passive);
                }

                if(wallet_notifications.equals(NOTIFICATION_OPEN)){
                    radio_notifications.check(R.id.radio_open);
                }else if(wallet_notifications.equals(NOTIFICATION_CLOSED)){
                    radio_notifications.check(R.id.radio_close);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            avloadingIndicatorView.hide();
        }


        @Override
        protected Void doInBackground(String... strings) {

            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateWalletSettingAsyncTask extends AsyncTask<String, Void, Void> {

        String sign_text = TIMESTAMP;
        String message;
        String hourly_limit = wallet_hourly_limit.getText().toString();
        String daily_limit = wallet_daily_limit.getText().toString();
        String max_limit = wallet_one_time_limit.getText().toString();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

            if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
                wallet.setWalletStatus(wallet_status);
                wallet.setLimitHourly(hourly_limit);
                wallet.setLimitDaily(daily_limit);
                wallet.setLimitMaxAmount(max_limit);
                db.updateWallet(wallet);
                avloadingIndicatorView.hide();
            }
            avloadingIndicatorView.hide();

            Toast.makeText(getActivity(),  R.string.wallet_saved , Toast.LENGTH_LONG).show();
        }


        @Override
        protected Void doInBackground(String... strings) {
            try {
                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(decryptedPrivateKey);
                String sign = ECDSAHelper.sign(sign_text, privateKey);
                int selectedStatusId = radio_situation.getCheckedRadioButtonId();
                int selectedNotificationId = radio_notifications.getCheckedRadioButtonId();

                switch(selectedStatusId){
                    case R.id.radio_active:
                        wallet_status = "1";
                        break;
                    case R.id.radio_passive:
                        wallet_status = "0";
                        break;
                }

                switch(selectedNotificationId){
                    case R.id.radio_open:
                        wallet_notifications = "1";
                        break;
                    case R.id.radio_close:
                        wallet_notifications = "0";
                        break;
                }

                OkHttpClient client = AppHelper.getOkhttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("sign", sign)
                        .addFormDataPart("nonce", TIMESTAMP)
                        .addFormDataPart("w_status", wallet_status)
                        .addFormDataPart("w_notification", wallet_notifications)
                        .addFormDataPart("w_limit_hourly",  hourly_limit)
                        .addFormDataPart("w_limit_daily",  daily_limit)
                        .addFormDataPart("w_limit_max_amount",  max_limit)
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
