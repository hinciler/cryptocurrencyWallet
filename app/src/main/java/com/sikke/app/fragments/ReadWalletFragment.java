package com.sikke.app.fragments;


import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.Base58;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.R;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.Wallet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
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
import static com.sikke.app.AppConstants.PVT_KEY;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.AppConstants.WALLET_NUMBER;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadWalletFragment extends Fragment {

    @BindView(R.id.txt_goBack)
    TextView txt_goBack;
    @BindView(R.id.input_qr_code)
    EditText input_qr_code;
    @BindView(R.id.rd_key)
    SurfaceView cameraPreview;

    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    public String pvt_key;
    public String wallet_number;
    private DataBaseHelper db;


    public static ReadWalletFragment newInstance() {
        // Required empty public constructor
        return  new ReadWalletFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_read_wallet, container, false);
        ButterKnife.bind(this, rootView);

        db = new DataBaseHelper(getActivity().getApplicationContext());
        readPvtKey();

        return rootView;
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
                        readPvtKey();
                }
            }
            break;
        }
    }


    public void readPvtKey(){

        barcodeDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(getActivity(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    FragmentCompat.requestPermissions(ReadWalletFragment.this,new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);

                    return;
                }

                try {
                    cameraSource.start(cameraPreview.getHolder());
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
                    input_qr_code.post(() -> {
                        //Create vibrate
                        Vibrator vibrator = (Vibrator)getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                        input_qr_code.setText(qrcodes.valueAt(0).displayValue);
                        pvt_key= qrcodes.valueAt(0).displayValue;
                        cameraSource.stop();
                    });
                }
            }

        });

    }

    class ImportWalletAsyncTask extends AsyncTask<String, Void, Void>{

        private boolean isSuccess;
        ArrayList errorMessages = new ArrayList();
        ArrayList successMessages = new ArrayList();

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
                getActivity().onBackPressed();

            }else {

                if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){

                    String pin_code = Prefs.getString(U_PASSWORD, "") ;

                    try {
                        Wallet wallet;
                        wallet= db.getWallet(wallet_number);
                        if(wallet.getPrivateKey()!= null){
                            byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                            pvt_key = input_qr_code.getText().toString();
                            String encryptedPvtKey = AES256Cipher.encryptPvt(u_password, pvt_key);
                            if(wallet.getWalletNumber()==null){
                                byte[] pvt_byte = Base58.decode(AES256Cipher.decryptPvt(u_password, pvt_key));
                                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                                byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                                addWalletToDB(wallet_number,encryptedPvtKey,Base58.encode(public_byte), "","0");
                            }else {
                                wallet.setPrivateKey(encryptedPvtKey);
                                db.updateWallet(wallet);
                            }
                            Toast.makeText(getActivity(), "Tebikler! Cüzdan Aktarıldı.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra("btn_name", "main");
                            startActivity(intent);
                        }else {
                            Toast.makeText(getActivity(), "Cüzdan Aktarılamadı! Bu Cüzdan Hesabınızda Kayıtlıdır.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else {
                    Prefs.putString(PVT_KEY, pvt_key);
                    Prefs.putString(WALLET_NUMBER, wallet_number);
                    Toast.makeText(getActivity(), "Tebikler! Cüzdan Aktarıldı.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("btn_name", "main");
                    startActivity(intent);
                }
            }

        }

        @Override
        protected Void doInBackground(String... strings) {

            try {
                ECDSAHelper ecdsaHelper = new ECDSAHelper();
                pvt_key = input_qr_code.getText().toString();


                byte[] pvt_byte = Base58.decode(pvt_key);
                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                byte[] public_byte= AppHelper.hexStringToByteArray(ecdsaHelper.publicKeyFromPrivate(priv_big_integer)) ;
                String public_key = Base58.encode(public_byte);

                OkHttpClient client = AppHelper.getOkhttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("w_asset", "SKK")
                        .addFormDataPart("w_pub_key", public_key)
                        .setType(MultipartBody.FORM)
                        .build();

                Request request = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.CREATE_WALLET )
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                String jsonData = response.body().string();

                Log.v("jsondata", jsonData);
                JSONObject JWalletObject = new JSONObject(jsonData);
                String error = JWalletObject.optString("status");
                JSONArray errorsAsList = JWalletObject.optJSONArray("errorsAsList");
                String message = JWalletObject.optString("message");
                JSONObject JWalletNumberObject =  JWalletObject.getJSONObject("wallet");
                String walletNumber = JWalletNumberObject.getString("address");

                wallet_number= walletNumber;

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
                }

                isSuccess = true;

                if(pvt_key.length() < 44 || pvt_key.length() > 48){
                    errorMessages.add("Hatalı Private Key");
                    isSuccess = false;
                }

            }catch (Exception e){
                isSuccess = false;
                e.printStackTrace();
            }

            return null;
        }
    }

    private void addWalletToDB(String w_number, String pri_key, String pub_key, String alias_name, String balance) {

        long id = db.insertWalletNumber(w_number, pri_key, pub_key,  alias_name, balance);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @OnClick(R.id.txt_goBack)
    void goBackClicked(){
        getActivity().onBackPressed();
    }

    @OnClick(R.id.btn_import_wallet)
    void importWalletClicked(){
        new ImportWalletAsyncTask().execute();
    }



}
