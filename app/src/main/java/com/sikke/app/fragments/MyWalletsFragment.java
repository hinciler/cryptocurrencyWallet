package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.Base58;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AccessToken;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.R;
import com.sikke.app.items.WalletItem;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.adapters.MyWalletAdapter;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.database.model.Wallet;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.sikke.app.AppConstants.U_PASSWORD;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyWalletsFragment extends Fragment {

    private RecyclerView.Adapter walletAdapter;
    private RecyclerView.LayoutManager walletLayoutManager;
    ArrayList<WalletItem> walletList = new ArrayList<>();
    private DataBaseHelper db;
    AccessToken accessToken;
    @BindView(R.id.txt_my_wallet_description)
    TextView txt_my_wallet_description;
    @BindView(R.id.btn_crt_wallet_number)
    Button btn_crt_wallet_number;
    @BindView(R.id.recyclerViewMyWalletList)
    RecyclerView recyclerViewWallet;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;
    public String getaccessToken;

    public static MyWalletsFragment newInstance() {
        // Required empty public constructor
        return new MyWalletsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_wallets, container, false);
        ButterKnife.bind(this, rootView);
        db = new DataBaseHelper(getActivity().getApplicationContext());
        accessToken = new AccessToken();

        getActivity().setTitle(R.string.nav_home);

        new ShowWalletsAsyncTask().execute();

        return rootView;
    }


    public void showWallets() {

        // Transaction List
        recyclerViewWallet.setHasFixedSize(true);

        // use a linear layout manager
        walletLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewWallet.setLayoutManager(walletLayoutManager);

        recyclerViewWallet.setItemAnimator(new DefaultItemAnimator());

        walletAdapter = new MyWalletAdapter(walletList, getActivity());
        recyclerViewWallet.setAdapter(walletAdapter);

    }

    private void addWalletToDB(String w_number, String pri_key, String pub_key, String alias_name, String balance) {

        long id = db.insertWalletNumber(w_number, pri_key, pub_key,  alias_name, balance);
    }

    @SuppressLint("StaticFieldLeak")
    public class ShowWalletsAsyncTask extends AsyncTask<String , Void, Void>{

        private boolean isSuccess;
        ArrayList errorMessages = new ArrayList();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(db.getWalletsCount()==0){
                txt_my_wallet_description.setText(R.string.no_wallet_warning);
                btn_crt_wallet_number.setVisibility(View.VISIBLE);
            }

            showWallets();
            if(!isSuccess){
                if(!errorMessages.isEmpty()){
                    for (int i = 0; i< errorMessages.size(); i++){
                        Toast.makeText(getActivity(), errorMessages.get(i).toString(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getActivity(),  R.string.server_error , Toast.LENGTH_LONG).show();
                }
            }
            avloadingIndicatorView.hide();

        }

        @Override
        protected Void doInBackground(String... strings) {
//            AccessToken
            User user=  db.getUser(1);
            getaccessToken=  accessToken.getAccessToken(user, db);

            try {
                String accessToken =  user.getAccessToken();

                OkHttpClient client = AppHelper.getOkhttpClient();
                Request request_wallets = AppHelper.getOkhttpRequestBuilder()
                        .addHeader("Authorization", "Bearer "+ accessToken)
                        .url(AppConstants.GET_WALLETS)
                        .get()
                        .build();

                Response response_wallets = client.newCall(request_wallets).execute();

                String jsonData = response_wallets.body().string();
//                Log.v("jsonData",jsonData);
                JSONObject JWalletObject = new JSONObject(jsonData);
                String error = JWalletObject.optString("status");
                JSONArray errorsAsList = JWalletObject.optJSONArray("errorsAsList");
                String message = JWalletObject.optString("message");
                JSONArray walletArray = JWalletObject.optJSONArray("wallets");

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
                }else {

                    isSuccess=true;
                }

                    for(int i=0; i<walletArray.length(); i++){
                        Wallet wallet;
                        WalletItem w = new WalletItem();

                        String pin_code = Prefs.getString(U_PASSWORD, "") ;
                        JSONObject walletObject = walletArray.getJSONObject(i);
                        String wallet_number = walletObject.getString("address");
                        String pri_key = walletObject.optString("w_pri_key");
                        String pri_key_encrypted = walletObject.optString("w_zeugma");
                        String alias_name = walletObject.optString("alias_name");
                        String is_deleted = walletObject.optString("is_deleted");
                        String balance = walletObject.optString("balance");
                        wallet= db.getWallet(wallet_number);
                        byte [] u_password_byte =   AES256Cipher.key128Bit(pin_code);

                        if(!is_deleted.equals("1")){
                            if(!pri_key.equals("")){
                                byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                                String encryptedPvtKey = AES256Cipher.encryptPvt(u_password, pri_key);
                                w.PvtKey =encryptedPvtKey ;
                                if(wallet.getWalletNumber() == null){
                                    byte[] pvt_byte = Base58.decode(AES256Cipher.decryptPvt(u_password_byte, encryptedPvtKey));
                                    BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                                    byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                                    addWalletToDB(wallet_number,encryptedPvtKey,Base58.encode(public_byte), alias_name, balance);
                                }
                            }else if (!pri_key_encrypted.equals("")){
                                w.PvtKey =pri_key_encrypted ;
                                if(wallet.getWalletNumber() == null){
                                    byte[] pvt_byte = Base58.decode(AES256Cipher.decryptPvt(u_password_byte, pri_key_encrypted));
                                    BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                                    byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                                    addWalletToDB(wallet_number,pri_key_encrypted, Base58.encode(public_byte), alias_name, balance);
                                }

                            }else {
                                wallet= db.getWallet(wallet_number);
                                w.PvtKey =pri_key_encrypted ;
                                if(wallet.getWalletNumber() == null){
                                    addWalletToDB(wallet_number,pri_key_encrypted,"", alias_name, balance);
                                }
                            }

                            w.WalletNumber = wallet_number;
                            w.AliasName = alias_name;
                            w.Balance = balance;
                            walletList.add(w);

                            wallet.setWalletBalance(balance);
                            db.updateWallet(wallet);
                        }


                    }

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    @OnClick(R.id.btn_crt_wallet_number)
    void createWalletButtonClicked() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.putExtra("btn_name", "create_wallet");
        startActivity(intent);
    }


}
