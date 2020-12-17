package com.sikke.app.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.AppConstants;
import com.sikke.app.Base58;
import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.items.WalletItem;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.database.model.Wallet;
import com.sikke.app.thirdParty.AES256Cipher;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.helpers.AppHelper.formatNumber;

public class MyWalletAdapter  extends RecyclerView.Adapter<MyWalletAdapter.ViewHolder>{
    private ArrayList<WalletItem> items;
    private Context mCtx;
    private DataBaseHelper db;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data country_item is just a string in this case

        @BindView(R.id.txt_my_wallet_number)
        public TextView txt_my_wallet_number;
        @BindView(R.id.my_wallet_currency)
        public TextView my_wallet_currency;
        @BindView(R.id.my_wallet_balance)
        public TextView my_wallet_balance;
        @BindView(R.id.textViewOptions)
        public TextView buttonViewOption;
        @BindView(R.id.skk_balance_layout)
        public RelativeLayout skk_balance_layout;


        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            ButterKnife.bind(this, v);
            db = new DataBaseHelper(mCtx);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyWalletAdapter(ArrayList<WalletItem> myDataset, Context mCtx) {
        this.items = myDataset;
        this.mCtx = mCtx;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.my_wallet_item, parent, false);

        // set the view's size, margins, paddings and layout parameters
        MyWalletAdapter.ViewHolder vh = new MyWalletAdapter.ViewHolder(v);
        return vh;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final WalletItem wItem = items.get(position);


        if (wItem.AliasName.equals("")){
            holder.txt_my_wallet_number.setText(wItem.WalletNumber == null ? "" : wItem.WalletNumber);
        }else {
            holder.txt_my_wallet_number.setText(wItem.AliasName == null ? "" : wItem.AliasName);
        }


//        holder.img_copy_pvt_key.setText(country_item. == null ? "" : country_item.Hour);
        holder.my_wallet_currency.setText(wItem.WalletId == null ? "SKK" : wItem.Currency);

        double balance = Double.parseDouble(wItem.Balance);
        String strBalance = formatNumber(balance);
        holder.my_wallet_balance.setText(wItem.Balance == null ? "0,000000 " :  strBalance );

        holder.txt_my_wallet_number.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Private Key", wItem.WalletNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mCtx,  R.string.copied , Toast.LENGTH_LONG).show();
            }
        });

        holder.skk_balance_layout.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("position_adapter", String.valueOf(position));
                Intent intent = new Intent(mCtx, MainActivity.class);
                intent.putExtra("btn_name", "main");
                intent.putExtra("position", position);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mCtx.startActivity(intent);

            }
        });

        holder.buttonViewOption.setOnClickListener(view -> {

            PopupMenu popup = new PopupMenu(mCtx, holder.buttonViewOption);
            //inflating menu from xml resource
            popup.inflate(R.menu.wallet_options);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                Intent intent = new Intent(mCtx, MainActivity.class);
                switch (item.getItemId()) {
                    case R.id.menuCopyWallet:
                        ClipboardManager clipboard = (ClipboardManager) mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Private Key", items.get(position).WalletNumber);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mCtx,  R.string.copied , Toast.LENGTH_LONG).show();
                        break;
                    case R.id.menuSend:
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("btn_name", "send");
                        intent.putExtra("position", position);
                        mCtx.startActivity(intent);
                        popup.dismiss();
                        break;
                    case R.id.menuShowTx:
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("btn_name", "main");
                        intent.putExtra("position", position);
                        mCtx.startActivity(intent);
                        popup.dismiss();
                        break;
                    case R.id.menuDelete:
                        items.remove(wItem);
                        new SweetAlertDialog(mCtx, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(mCtx.getResources().getString(R.string.ask_confirmation))
                                .setContentText(mCtx.getResources().getString(R.string.confirmation_detail))
                                .setConfirmText(mCtx.getResources().getString(android.R.string.yes))
                                .setConfirmClickListener(sDialog -> {
                                    sDialog
                                            .setTitleText(mCtx.getResources().getString(R.string.congratulations))
                                            .setContentText(mCtx.getResources().getString(R.string.deleted_wallet))
                                            .setConfirmText(mCtx.getResources().getString(android.R.string.ok))
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    MyWalletAdapter.this.notifyDataSetChanged();
                                    new DeleteWalletAsyncTask().execute(wItem.PvtKey);

                                })
                                .show();
                        popup.dismiss();
                        break;
                }
                return false;
            });

            popup.show();


        });
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteWalletAsyncTask extends AsyncTask<String, Void, Void> {

        String sign_text = TIMESTAMP;
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            SQLiteDatabase sqLiteDatabase = db.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
            sqLiteDatabase.delete("wallet", null, null);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                String pin_code = Prefs.getString(U_PASSWORD, "");
                byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                String decryptedPrivateKey = AES256Cipher.decryptPvt(u_password, strings[0]);
                byte[] pvt_byte = Base58.decode(decryptedPrivateKey);
                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                String public_key = Base58.encode(public_byte);
                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(decryptedPrivateKey);
                String sign = ECDSAHelper.sign(sign_text, privateKey);

                OkHttpClient client = AppHelper.getOkhttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("sign", sign)
                        .addFormDataPart("nonce", TIMESTAMP)
                        .addFormDataPart("w_pub_key", public_key)
                        .addFormDataPart("is_deleted", "1")
                        .setType(MultipartBody.FORM)
                        .build();

                Request request = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_WALLET_ITEM)
                        .delete(requestBody)
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

    @Override
    public int getItemCount() {
        return items.size();
    }
}
