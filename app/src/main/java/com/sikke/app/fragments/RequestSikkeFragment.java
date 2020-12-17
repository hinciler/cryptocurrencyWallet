package com.sikke.app.fragments;


import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.AppConstants;
import com.sikke.app.database.model.Wallet;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.BuildConfig;
import com.sikke.app.R;
import com.sikke.app.items.WalletItem;
import com.sikke.app.adapters.WalletPagerAdapter;
import com.sikke.app.database.DataBaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.WALLET_NUMBER;
import static com.sikke.app.helpers.AppHelper.getDownloadFilePath;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestSikkeFragment extends Fragment {

    ArrayList<WalletItem> walletList = new ArrayList<WalletItem>();

    ViewPager walletViewPager;
    PagerAdapter walletAdapter;

    @BindView(R.id.btn_left)
    Button btn_left;
    @BindView(R.id.btn_right)
    Button btn_right;
    @BindView(R.id.request_amount)
    EditText request_amount;
    @BindView(R.id.request_description)
    EditText request_description;
    @BindView(R.id.barcode_image)
    ImageView barcode_image;

    JSONObject qr_object = new JSONObject();
    private DataBaseHelper db;
    public String wallet_number = Prefs.getString(WALLET_NUMBER, "0");
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    public static RequestSikkeFragment newInstance(int position) {

        RequestSikkeFragment requestSikkeFragment = new RequestSikkeFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        requestSikkeFragment.setArguments(args);

        return requestSikkeFragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request_sikke, container, false);

        db = new DataBaseHelper(getActivity().getApplicationContext());
        ButterKnife.bind(this, rootView);

        getActivity().setTitle(R.string.request_sikke);

        if(!Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
            btn_left.setVisibility(View.GONE);
            btn_right.setVisibility(View.GONE);
        }

        new ShowBalanceAsyncTask().execute();



        return rootView;
    }

    class  ShowBalanceAsyncTask extends AsyncTask<String , Void, Void> {

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showWallets();

            if(Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
                int position = getArguments().getInt("position", 0);
                walletViewPager.setCurrentItem(position);
                wallet_number = db.getAllWallets().get(position).getWalletNumber();
            }

            try {
                qr_object.put("to", wallet_number);

                Log.v("wallet_number",wallet_number);
                createQR(qr_object,barcode_image);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

            }catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }
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

    @OnTextChanged(value = {R.id.request_amount , R.id.request_description}, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterAmountInput(Editable editable) {

        try {
            qr_object.put("to", wallet_number);
            qr_object.put("amount", request_amount.getText().toString());
            qr_object.put("description", request_description.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        createQR(qr_object,barcode_image);

    }

    public void createQR(JSONObject qr_object, ImageView img ){

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
            BitMatrix result = writer.encode(String.valueOf(qr_object), BarcodeFormat.QR_CODE, 350,350, hints);

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


    @OnClick(R.id.btn_right)
    void rightButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() + 1);
    }

    @OnClick(R.id.btn_left)
    void leftButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() - 1);
    }

    @OnClick(R.id.btn_request)
    void requestClicked(){

        View content = getActivity().findViewById(R.id.barcode_image);
        content.setDrawingCacheEnabled(true);

        Bitmap bitmap = content.getDrawingCache();
        File photoUri = new File(getDownloadFilePath(getActivity()) + "/" + wallet_number + ".jpg"   );

        try {

            int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_ASK_PERMISSIONS);
                        }
                        return;
                    }

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                return;
            }else {
                String amount= request_amount.getText().toString();
                Double dbAmount = Double.parseDouble(amount);
                String url =  "https://wallet.sikke.com.tr" +  "?modal=send&to=" + wallet_number + "&amount=" + dbAmount + "&currency=" + "SKK" + "&description=" + request_description.getText().toString();
                String stringToSent = url;
                photoUri.createNewFile();
                FileOutputStream ostream = new FileOutputStream(photoUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", photoUri));
                    share.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.skk_request);
                share.putExtra(android.content.Intent.EXTRA_TEXT, stringToSent);
                startActivity(Intent.createChooser(share,"SKK istemek için Paylaş"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.btn_cancel)
    void goBackClicked(){
        getActivity().onBackPressed();
    }

}
