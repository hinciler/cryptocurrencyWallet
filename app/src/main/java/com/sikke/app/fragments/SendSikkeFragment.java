package com.sikke.app.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AccessToken;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.Base58;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.R;
import com.sikke.app.items.WalletItem;
import com.sikke.app.activities.AccountWrapperActivity;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.adapters.WalletPagerAdapter;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.database.model.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.PVT_KEY;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.AppConstants.WALLET_NUMBER;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendSikkeFragment extends Fragment {

    @BindView(R.id.btn_left)
    Button btn_left;
    @BindView(R.id.btn_right)
    Button btn_right;
    @BindView(R.id.total)
    EditText total;
    @BindView(R.id.amount)
    EditText amount;
    @BindView(R.id.fee)
    EditText fee;
    @BindView(R.id.saved_wallet_numbers)
    EditText saved_wallet_numbers;
    @BindView(R.id.send_sikke_description)
    EditText send_sikke_description;
    @BindView(R.id.ishiddenSwitch)
    Switch ishiddenSwitch;
    @BindView(R.id.btn_send)
    Button btn_send;
    @BindView(R.id.img_read_from_barcode)
    ImageView img_read_from_barcode;
    AccessToken accessToken;

    String  guid;
    String status= "0";
    String seq= "lahmacun";
    ProgressDialog progressDialog;

    String[] savedWalletNumbers = { "SKK" };
    ArrayList<WalletItem> walletList = new ArrayList<>();

    ViewPager walletViewPager;
    PagerAdapter walletAdapter;

    final int RequestCameraPermissionID = 1001;

    private DataBaseHelper db;

    public String pvt_key = Prefs.getString(PVT_KEY, "");
    public String wallet_number = Prefs.getString(WALLET_NUMBER, "0");
    SweetAlertDialog seq_dialog ;

    public static SendSikkeFragment newInstance(int position) {

        SendSikkeFragment sendSikkeFragment = new SendSikkeFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        sendSikkeFragment.setArguments(args);

        return sendSikkeFragment;

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send_sikke, container, false);
        ButterKnife.bind(this, rootView);
        db = new DataBaseHelper(getActivity().getApplicationContext());
        accessToken = new AccessToken();

        getActivity().setTitle(R.string.send_sikke);
        seq_dialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE);

        new ShowBalanceAsyncTask().execute();

        ArrayAdapter<String> savedWalletAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, savedWalletNumbers);
        AutoCompleteTextView acSavedWalletNumbers =  rootView.findViewById(R.id.saved_wallet_numbers);
        //Set the number of characters the user must type before the drop down list is shown

        acSavedWalletNumbers.setThreshold(3);
        acSavedWalletNumbers.setAdapter(savedWalletAdapter);

        return rootView;
    }

    class CheckStatusAsyncTask extends  AsyncTask<String, Void, Void>{

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            btn_send.setEnabled(true);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {

                OkHttpClient client = AppHelper.getOkhttpClient();
                Request request_status = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_TX_URL + "/" +guid)
                        .get()
                        .build();

                Response response_status = client.newCall(request_status).execute();
                String jsonStatusData = response_status.body().string();
                JSONObject JStatusGuidObject = new JSONObject(jsonStatusData);
                String statusObject = JStatusGuidObject.getString("tx");
                JSONObject JStatusObject = new JSONObject(statusObject);
                status = JStatusObject.getString("status");
                if(status.equals("1")){
                    seq = JStatusObject.getString("seq");
                    Log.v("seq", seq);
                }
                Log.v("statusObject", statusObject);

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public String formatAmount(String amount_text){
        if(amount_text.contains(".")){
            String last_string= amount_text.substring(amount_text.length() - 1);

            while (last_string.equals("0")  )
            {
                amount_text = amount_text.substring(0,amount_text.length() - 1);
                last_string= amount_text.substring(amount_text.length() - 1);

            }
            if(last_string.equals(".")){
                amount_text = amount_text.substring(0,amount_text.length() - 1);
            }
        }
        return amount_text;
    }

    @SuppressLint("StaticFieldLeak")
    class SendSkkAsyncTask extends AsyncTask<String, Void, Void> {

            private boolean isSuccess;
            ArrayList<String> errorMessages = new ArrayList<String>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
                progressDialog.setCancelable(false);
                progressDialog.setTitle(R.string.please_wait);
                progressDialog.setMessage(getActivity().getResources().getString(R.string.sending_process));
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }

            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(!isSuccess){
                    if(!errorMessages.isEmpty()){
                        for (int i = 0; i< errorMessages.size(); i++){
                            Toast.makeText(getActivity(), errorMessages.get(i).toString(), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getActivity(),  R.string.server_is_not_responding_please_try_again , Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                    btn_send.setEnabled(true);
                }else {
                    final Handler handler = new Handler();
                    final int delay = 2000; //milliseconds

                    if(status.equals("0")){

                        handler.postDelayed(new Runnable(){
                            public void run(){
                                //do something
                                new CheckStatusAsyncTask().execute();
                                if(status.equals("1")){
                                    Log.v("seq", "seq "+seq);
                                    handler.removeCallbacks(this::run);
                                    Toast.makeText(getActivity(), R.string.congratulations_sent, Toast.LENGTH_SHORT).show();
                                    saved_wallet_numbers.setText("");
                                    send_sikke_description.setText("");
                                    amount.setText("0");
                                    progressDialog.dismiss();

                                    seq_dialog.setTitleText(getActivity().getResources().getString(R.string.congratulations));
                                    seq_dialog.setContentText(getActivity().getResources().getString(R.string.your_payment)+seq);
                                    seq_dialog.setConfirmClickListener(sDialog -> {
                                                seq_dialog.dismiss();
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.putExtra("btn_name","my_wallet");
                                                startActivity(intent);
                                            });
                                    seq_dialog   .show();

                                    btn_send.setEnabled(true);

                                }else {
                                    handler.postDelayed(this, delay);
                                }
                            }
                        }, delay);

                    }else {
                        Log.v("finished", status);
                        btn_send.setEnabled(true);
                        progressDialog.dismiss();
                    }

                }
            }
            @Override
            protected Void doInBackground(String... strings) {
                User user=  db.getUser(1);
                AccessToken.getAccessToken(user, db);

                try {

                    String accessToken =  user.getAccessToken();
                    String amount_text =  amount.getText().toString().replace(",",".");
                    amount_text = formatAmount(amount_text);

                    String to_wallet = saved_wallet_numbers.getText().toString();
                    String description = send_sikke_description.getText().toString();
                    String timeStamp = String.valueOf(System.currentTimeMillis()/1000);
                    String sign_text = wallet_number+"__" + to_wallet+"__" + amount_text+"__"+ "SKK" +"__"+ timeStamp;
                    //$from_w_number__$to_w_number__$inputTxAmount__$asset__$tx_nonce

                    String isHidden;
                    if(ishiddenSwitch.isChecked()){
                        isHidden= "1";
                    }else {
                        isHidden="0";
                    }

                    byte[] pvt_byte = Base58.decode(pvt_key);

                    BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                    byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                    String public_key = Base58.encode(public_byte);
                    PrivateKey privateKey =  ECDSAHelper.importPrivateKey(pvt_key);
                    String sign = ECDSAHelper.sign(sign_text, privateKey);

                    OkHttpClient client = AppHelper.getOkhttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .addFormDataPart("tx_w_number", wallet_number)
                            .addFormDataPart("tx_to_w_number", to_wallet)
                            .addFormDataPart("tx_sign", sign)
                            .addFormDataPart("tx_asset", "SKK")
                            .addFormDataPart("tx_desc", description)
                            .addFormDataPart("tx_amount", amount_text)
                            .addFormDataPart("tx_nonce",timeStamp )
                            .addFormDataPart("w_pub_key", public_key)
                            .addFormDataPart("is_hidden", isHidden)
                            .setType(MultipartBody.FORM)
                            .build();

                    Request request = AppHelper.getOkhttpRequestBuilder()
                            .addHeader("Authorization", "Bearer "+ accessToken)
                            .url(AppConstants.GET_TX_URL)
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    final String jsonData = response.body().string();
                    JSONObject JTxObject = new JSONObject(jsonData);
                    Log.v("jsondata_tx", jsonData);

                    String error = JTxObject.optString("status");
                    JSONArray errorsAsList = JTxObject.optJSONArray("errorsAsList");
                    String message = JTxObject.optString("message");

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


                        String txObject = JTxObject.getString("tx");
                        JSONObject JGuidObject = new JSONObject(txObject);
                        guid = JGuidObject.getString("_id");
                        isSuccess =true;
                    }



                }catch (Exception e){
                    isSuccess=false;
                    e.printStackTrace();
                }

                return null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    showDialog(getActivity());
                }
            }
            break;
        }
    }

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.barcode_screen_dialog);


        BarcodeDetector barcodeDetector;
        final CameraSource cameraSource;
        final SurfaceView  read_pvt_key = dialog.findViewById(R.id.read_pvt_key);

        barcodeDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(getActivity(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        read_pvt_key.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    FragmentCompat.requestPermissions(SendSikkeFragment.this,new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);

                    return;
                }

                try {
                    cameraSource.start(read_pvt_key.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {


            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();

                if(qrcodes.size() != 0)
                {

                    try {
                        JSONObject JRequestInfoObject = new JSONObject(qrcodes.valueAt(0).displayValue);
                        final String walletNumber = JRequestInfoObject.getString("to");
                        final String description = JRequestInfoObject.getString("description");
                        final String amountJson= JRequestInfoObject.getString("amount");
                        saved_wallet_numbers.post(() -> {
                            //Create vibrate
                            Vibrator vibrator = (Vibrator)getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            saved_wallet_numbers.setText(walletNumber);
                            amount.setText(amountJson);
                            send_sikke_description.setText(description);

                            dialog.dismiss();
                        });
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Lutfen Duzgun Formatda Taratin", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            }

        });


        Button dialogButton = dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void showWallets() {

        // Locate the ViewPager in fragment_main.xml
        walletViewPager = getActivity().findViewById(R.id.wallet_pager);
        walletAdapter = new WalletPagerAdapter(getActivity().getApplicationContext(), walletList);


        walletViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                wallet_number= walletList.get(position).WalletNumber;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        // Binds the Adapter to the ViewPager
        walletViewPager.setAdapter(walletAdapter);

    }

    class  ShowBalanceAsyncTask extends AsyncTask<String , Void, Void>{

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showWallets();
            checkPvtKeyExist();

        }

        @Override
        protected Void doInBackground(String... strings) {

            try {
                OkHttpClient client = AppHelper.getOkhttpClient();

                if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
                    for(int i = 0; i<db.getAllWallets().size(); i++){
                        Wallet wallet;
                        WalletItem w = new WalletItem();
                        w.WalletNumber = db.getAllWallets().get(i).getWalletNumber();
                        wallet= db.getWallet(w.WalletNumber);
                        w.AliasName = wallet.getWalletAliasName() == null ? "" : wallet.getWalletAliasName();
                        w.Balance = wallet.getWalletBalance();

                        walletList.add(w);
                    }
                }else {
                    WalletItem w = new WalletItem();
                    w.WalletNumber = wallet_number;

                    Request request_balance = AppHelper.getOkhttpRequestBuilder()
                            .url(AppConstants.GET_WALLET_BALANCE + wallet_number)
                            .get()
                            .build();

                    Response response_balance = client.newCall(request_balance).execute();

                    assert response_balance.body() != null;
                    String jsonBalanceData = Objects.requireNonNull(response_balance.body()).string();
                    JSONObject JBalanceObject = new JSONObject(jsonBalanceData);

                    w.Balance = JBalanceObject.getString("balance");
                    walletList.add(w);
                }


//                walletAdapter.notifyDataSetChanged();

            }catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }
    }

    private void checkPvtKeyExist() {
        int position = getArguments().getInt("position", 0);
        if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
            wallet_number= walletList.get(position).WalletNumber;
            Log.v("wallet_number", wallet_number);
            walletViewPager.setCurrentItem(position);
            Wallet wallet =  db.getWallet(wallet_number);
            if(!wallet.getPrivateKey().equals("")){

                String pin_code = Prefs.getString(U_PASSWORD, "");
                try {
                    byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                    pvt_key = AES256Cipher.decryptPvt(u_password, wallet.getPrivateKey() );
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                Log.v("info ", "pvt_key: "+ wallet.getPrivateKey());
            }else {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }

                builder.setTitle(getActivity().getResources().getString(R.string.private_key_error))
                        .setMessage(getActivity().getResources().getString(R.string.no_private_key_error))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            Intent intent = new Intent(getActivity().getApplicationContext(), AccountWrapperActivity.class);
                            intent.putExtra("btn_name","qr_login");
                            startActivity(intent);
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> getActivity().onBackPressed())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }else {
            pvt_key = Prefs.getString(PVT_KEY, "0");
            wallet_number = Prefs.getString(WALLET_NUMBER, "0");
            btn_left.setVisibility(View.GONE);
            btn_right.setVisibility(View.GONE);
        }
    }


    @SuppressLint("DefaultLocale")
    @OnTextChanged(value = R.id.amount , callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterAmountInput(Editable editable) {

        if(amount.getTag()==null){
            double amountDb = 0;

            if (!amount.getText().toString().isEmpty() ){
                amountDb =  Double.parseDouble(amount.getText().toString().replace(",", "."));
            }else {
                amountDb = 0;
            }

            double totalDb ;
            double feeDb ;

            feeDb = amountDb * 0.1 / 100;
            totalDb = feeDb + amountDb ;

            total.setTag( "total changed" );
            fee.setText(String.format("%.6f", feeDb));
            total.setText(String.format("%.6f", totalDb));
        }

        amount.setTag(null);

    }



    @SuppressLint("DefaultLocale")
    @OnTextChanged(value = R.id.total, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTotalInput(Editable editable) {

        if(total.getTag()==null){
            double totalDb;

            if (!total.getText().toString().isEmpty() ){
                totalDb =  Double.parseDouble(total.getText().toString().replace(",", "."));
            }else {
                totalDb = 0;
            }

            double amountDb ;
            double feeDb;

            amountDb = totalDb*1000/1001 ;
            feeDb = amountDb * 0.1 / 100;

            amount.setTag( "amount changed" );
            fee.setText(String.format("%.6f", feeDb));
            amount.setText(String.format("%.6f", amountDb));
        }
        total.setTag(null);


    }

    @OnClick(R.id.btn_right)
    void rightButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() + 1);
    }

    @OnClick(R.id.btn_left)
    void leftButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() - 1);
    }
    @OnClick(R.id.btn_send)
    void sendButtonClicked() {
        btn_send.setEnabled(false);
        new SendSkkAsyncTask().execute();
    }

    @OnClick(R.id.btn_cancel)
    void cancelButtonClicked() {
        getActivity().onBackPressed();

    }

    @OnClick(R.id.img_read_from_barcode)
    void readBarcodeButtonClicked() {
        showDialog(getActivity());

    }

}

