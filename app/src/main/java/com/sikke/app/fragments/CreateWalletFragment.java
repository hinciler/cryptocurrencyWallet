package com.sikke.app.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.Base58;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.R;
import com.sikke.app.items.WalletItem;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.interfaces.NavigationInterface;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.ALIAS_NAME;
import static com.sikke.app.AppConstants.DEFAULT_WALLET;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.PUB_KEY;
import static com.sikke.app.AppConstants.PVT_KEY;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.AppConstants.WALLET_NUMBER;
import static com.sikke.app.helpers.AppHelper.getDownloadFilePath;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateWalletFragment extends Fragment {

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;
    @BindView(R.id.txt_wallet_number)
    TextView txt_wallet_number;
    @BindView(R.id.txt_private_number)
    TextView txt_private_number;
    @BindView(R.id.barcode_wallet_number)
    ImageView barcode_wallet_number;
    @BindView(R.id.barcode_private_number)
    ImageView barcode_private_number;
    @BindView(R.id.barcode_public_number)
    ImageView barcode_public_number;
    @BindView(R.id.btn_confirm_wallet)
    Button btn_confirm_wallet;
    @BindView(R.id.btn_save_wallet)
    Button btn_save_wallet;
    @BindView(R.id.warning_pvt_key_rel_layout)
    RelativeLayout warning_pvt_key_rel_layout;
    @BindView(R.id.wallet_info)
    LinearLayout wallet_info;
    @BindView(R.id.img_copy_wallet)
    ImageView img_copy_wallet;
    @BindView(R.id.img_share_wallet)
    ImageView img_share_wallet;
    @BindView(R.id.img_copy_pvt)
    ImageView img_copy_pvt;
    @BindView(R.id.img_share_pvt)
    ImageView img_share_pvt;
    @BindView(R.id.cv_show_txt)
    CardView cv_show_txt;
    @BindView(R.id.cv_show_qr)
    CardView cv_show_qr;

    PdfWriter writer;

    private NavigationInterface navigationInterface;

    String private_key;
    String public_key;
    String wallet_number;
    int zeroBalanceWalletCount;

    private DataBaseHelper db;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;


    public static CreateWalletFragment newInstance()  {
        // Required empty public constructor
        return new CreateWalletFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_wallet, container, false);
        ButterKnife.bind(this, rootView);
        db = new DataBaseHelper(getActivity().getApplicationContext());

        checkBalanceCount();

        btn_confirm_wallet.setVisibility(View.GONE);
        btn_save_wallet.setVisibility(View.VISIBLE);
        warning_pvt_key_rel_layout.setVisibility(View.VISIBLE);
        wallet_info.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            navigationInterface = (NavigationInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NavigationInterface");
        }
    }

    private void createKeys(){
            new CreateKeysAsyncTask().execute();
    }

    public void checkBalanceCount(){
        for(int i = 0; i<db.getAllWallets().size(); i++){
            WalletItem w = new WalletItem();

            w.Balance = db.getAllWallets().get(i).getWalletBalance();

            if(w.Balance.equals("0")){
                zeroBalanceWalletCount++;
            }
        }
    }

    class CreateKeysAsyncTask extends AsyncTask<String, Void, Void> {

        private boolean isSuccess;
        ArrayList errorMessages = new ArrayList();
        ArrayList successMessages = new ArrayList();
        User user=  db.getUser(1);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
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
                getActivity().onBackPressed();

            }else {
                avloadingIndicatorView.hide();
                txt_private_number.setText(private_key);
                txt_wallet_number.setText(wallet_number);
                createQR(wallet_number, barcode_wallet_number);
                createQR(private_key, barcode_private_number);


                if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){

                    try {
                        String u_password = Prefs.getString(U_PASSWORD, "") ;
                        byte [] u_password_byte =   AES256Cipher.key128Bit(u_password);
                        String encryptedPvtKey = AES256Cipher.encryptPvt(u_password_byte,private_key);
                        addWalletToDB(wallet_number, encryptedPvtKey, public_key, "", "0");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Prefs.putString(PVT_KEY, private_key);
                    Prefs.putString(PUB_KEY, public_key);
                    Prefs.putString(WALLET_NUMBER, wallet_number);
                    Prefs.putString(ALIAS_NAME, "");
                }

                Toast.makeText(getActivity(), R.string.congratulations_wallet, Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected Void doInBackground(String... strings) {

            try {
                ECDSAHelper ecdsaHelper = new ECDSAHelper();
                private_key =  ecdsaHelper.generatePrivateKey();
                public_key =   ecdsaHelper.generatePublicKey();

                //Create Wallet From Api
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

                //Save Wallet To Api
                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(private_key);
                String sign = ECDSAHelper.sign(TIMESTAMP, privateKey);
                String access_token = user.getAccessToken();

                String pin_code = Prefs.getString(U_PASSWORD, "") ;
                byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                String encryptedPvtKey = AES256Cipher.encryptPvt(u_password,private_key);

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

                Response responseUpdate = client.newCall(requestUpdate).execute();
                String jsonUpdateData = responseUpdate.body().string();

                try {

                    Response response = client.newCall(request).execute();

                    String jsonData = response.body().string();

                    JSONObject JWalletObject = new JSONObject(jsonData);
                    String error = JWalletObject.optString("status");
                    JSONArray errorsAsList = JWalletObject.optJSONArray("errorsAsList");
                    String message = JWalletObject.optString("message");
                    String wallet = JWalletObject.getString("wallet");
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

                } catch (Exception e) {
                    Log.v("server error", String.valueOf(errorMessages));

                    isSuccess = false;
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }


    public void createQR(String qr_object, ImageView img ){

        try {

            int MARGIN_AUTOMATIC = -1;

            int MARGIN_NONE = 0;
            int marginSize = MARGIN_NONE;

            Map<EncodeHintType, Object> hints = null;
            if (marginSize != MARGIN_AUTOMATIC) {
                hints = new EnumMap<>(EncodeHintType.class);
                // We want to generate with a custom margin size
                hints.put(EncodeHintType.MARGIN, marginSize);
            }

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = writer.encode(qr_object, BarcodeFormat.QR_CODE, 350,350, hints);

            final int width = result.getWidth();
            final int height = result.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            img.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void addWalletToDB(String w_number, String pri_key, String pub_key, String alias_name,  String balance) {

        long id = db.insertWalletNumber(w_number, pri_key, pub_key,  alias_name, balance);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    AppHelper.requestPermission(getActivity());
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity().getApplicationContext(), "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void createPdfFromImg(){
        Document document = new Document();

        try {
            AppHelper.requestPermission(getActivity());
            File pdfFile = new File(getDownloadFilePath(getActivity()) + "/" + wallet_number + ".pdf"   );
            OutputStream output = new FileOutputStream(pdfFile);
            writer= PdfWriter.getInstance(document, output);
            document.open();

            // get base image stream
            InputStream ims = getActivity().getAssets().open("kese_print.png");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image baseImage = Image.getInstance(stream.toByteArray());
            baseImage.scaleToFit(510,390);
            document.add(baseImage);

            // get wallet image stream
            Bitmap walletBitmap=((BitmapDrawable)barcode_wallet_number.getDrawable()).getBitmap();
            ByteArrayOutputStream streamWalletBarcode = new ByteArrayOutputStream();
            walletBitmap.compress(Bitmap.CompressFormat.JPEG, 100, streamWalletBarcode);

            Image img_wallet_number = Image.getInstance(streamWalletBarcode.toByteArray());
            img_wallet_number.setAbsolutePosition(139f, 681f);
            img_wallet_number.scaleToFit(105,105);
            document.add(img_wallet_number);

            // get private image stream
            Bitmap privateBitmap=((BitmapDrawable)barcode_private_number.getDrawable()).getBitmap();
            ByteArrayOutputStream streamPrivateBarcode = new ByteArrayOutputStream();
            privateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, streamPrivateBarcode);

            Image img_pvt_number = Image.getInstance(streamPrivateBarcode.toByteArray());
            img_pvt_number.setAbsolutePosition(279f, 681f);
            img_pvt_number.scaleToFit(105,105);
            document.add(img_pvt_number);

            createQR(public_key, barcode_public_number);

            // get public image stream
            Bitmap publicBitmap=((BitmapDrawable)barcode_public_number.getDrawable()).getBitmap();
            ByteArrayOutputStream streamPublicBarcode = new ByteArrayOutputStream();
            publicBitmap.compress(Bitmap.CompressFormat.JPEG, 100, streamPublicBarcode);

            Image img_public_number = Image.getInstance(streamPublicBarcode.toByteArray());
            img_public_number.setAbsolutePosition(416f, 681f);
            img_public_number.scaleToFit(105,105);
            document.add(img_public_number);

            //write text on pdf
            createPdfText(wallet_number, 182, 655, 10);
            createPdfText(private_key, 182, 634, 10);
            createPdfText(public_key, 182, 615, 7);


            Toast.makeText(getActivity().getApplicationContext(), R.string.congratulations_backup+getDownloadFilePath(getActivity())+wallet_number , Toast.LENGTH_SHORT).show();

            document.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createPdfText(String text, int x, int y, int size) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, size);
            cb.showText(text);
            cb.endText();
            cb.restoreState();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_confirm_wallet)
    void confirmWalletClicked(){
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.putExtra("btn_name","main");
        startActivity(intent);
    }

    @OnClick(R.id.img_copy_wallet)
    void copyWalletClicked(){
        //handle menu1 click
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Private Key", wallet_number);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(),  R.string.copied , Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.img_copy_pvt)
    void copyPvtKeyClicked(){
        //handle menu1 click
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Private Key", private_key);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(),  R.string.copied , Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.img_share_pvt)
    void sharePvtKeyClicked(){

        String stringToSent = private_key;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.wallet_info);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringToSent);
        startActivity(Intent.createChooser(sharingIntent, getActivity().getResources().getString(R.string.share_private)));
    }

    @OnClick(R.id.img_share_wallet)
    void shareWalletClicked(){
        String stringToSent = wallet_number;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.wallet_info);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringToSent);
        startActivity(Intent.createChooser(sharingIntent, getActivity().getResources().getString(R.string.share_wallet)));
    }

    @OnClick(R.id.cv_show_qr)
    void showQrClicked(){

        if (txt_wallet_number.getVisibility()==View.GONE)
        {
            txt_private_number.setVisibility(View.VISIBLE);
            txt_wallet_number.setVisibility(View.VISIBLE);
            img_copy_wallet.setVisibility(View.VISIBLE);
            img_share_wallet.setVisibility(View.VISIBLE);
            img_copy_pvt.setVisibility(View.VISIBLE);
            img_share_pvt.setVisibility(View.VISIBLE);
            cv_show_txt.setVisibility(View.GONE);
            cv_show_qr.setVisibility(View.VISIBLE);
            barcode_private_number.setVisibility(View.GONE);
            barcode_wallet_number.setVisibility(View.GONE);
        }else {
            txt_private_number.setVisibility(View.GONE);
            txt_wallet_number.setVisibility(View.GONE);
            barcode_private_number.setVisibility(View.VISIBLE);
            barcode_wallet_number.setVisibility(View.VISIBLE);
            cv_show_qr.setVisibility(View.GONE);
            cv_show_txt.setVisibility(View.VISIBLE);
            img_copy_wallet.setVisibility(View.GONE);
            img_share_wallet.setVisibility(View.GONE);
            img_copy_pvt.setVisibility(View.GONE);
            img_share_pvt.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.cv_show_txt)
    void showTxtClicked(){

        if (txt_wallet_number.getVisibility()==View.GONE)
        {
            txt_private_number.setVisibility(View.VISIBLE);
            txt_wallet_number.setVisibility(View.VISIBLE);
            img_copy_wallet.setVisibility(View.VISIBLE);
            img_share_wallet.setVisibility(View.VISIBLE);
            img_copy_pvt.setVisibility(View.VISIBLE);
            img_share_pvt.setVisibility(View.VISIBLE);
            cv_show_txt.setVisibility(View.GONE);
            cv_show_qr.setVisibility(View.VISIBLE);
            barcode_private_number.setVisibility(View.GONE);
            barcode_wallet_number.setVisibility(View.GONE);
        }else {
            txt_private_number.setVisibility(View.GONE);
            txt_wallet_number.setVisibility(View.GONE);
            barcode_private_number.setVisibility(View.VISIBLE);
            barcode_wallet_number.setVisibility(View.VISIBLE);
            cv_show_qr.setVisibility(View.GONE);
            cv_show_txt.setVisibility(View.VISIBLE);
            img_copy_wallet.setVisibility(View.GONE);
            img_share_wallet.setVisibility(View.GONE);
            img_copy_pvt.setVisibility(View.GONE);
            img_share_pvt.setVisibility(View.GONE);
        }
    }


    @OnClick(R.id.cv_save_wallet)
    void saveWalletClicked(){
        createPdfFromImg();
    }

    @OnClick(R.id.cv_go_wallet)
    void goWalletClicked(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("position", db.getWalletsCount()-1);
        intent.putExtra("btn_name", "main");
        startActivity(intent);
    }

    @OnClick(R.id.btn_crt_pvt_number)
    void createWalletClicked() {

        if (zeroBalanceWalletCount < 3){
            warning_pvt_key_rel_layout.setVisibility(View.GONE);
            wallet_info.setVisibility(View.VISIBLE);

            try {
                createKeys();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(getActivity(), "Hata! Bakiyesi Sıfır Olan En Fazla 3 Adet Kese Açabilirsiniz", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        navigationInterface.unlockDrawer();
    }

}
