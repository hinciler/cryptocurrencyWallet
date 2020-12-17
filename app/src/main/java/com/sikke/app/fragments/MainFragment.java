package com.sikke.app.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.BaseActivity;
import com.sikke.app.database.model.Wallet;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.AppConstants;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.Base58;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.R;
import com.sikke.app.items.TransactionItem;
import com.sikke.app.items.WalletItem;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.adapters.SettingPagerAdapter;
import com.sikke.app.adapters.TransactionItemAdapter;
import com.sikke.app.adapters.WalletPagerAdapter;
import com.sikke.app.database.DataBaseHelper;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.mikepenz.iconics.Iconics.TAG;
import static com.sikke.app.AppConstants.ALIAS_NAME;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.AppConstants.WALLET_NUMBER;


public class MainFragment extends Fragment implements Serializable, TransactionItemAdapter.OnItemClickListener {

    @BindView(R.id.btn_send)
    Button btn_send;
    @BindView(R.id.btn_request)
    Button btn_request;
    @BindView(R.id.btn_left)
    Button btn_left;
    @BindView(R.id.btn_right)
    Button btn_right;
    @BindView(R.id.transactionList)
    LinearLayout transaction_list;
    @BindView(R.id.wallet_setting_layout)
    LinearLayout wallet_setting_layout;
    @BindView(R.id.wallet_pager_row)
    LinearLayout wallet_pager_row;
    @BindView(R.id.btn_lyt)
    LinearLayout btn_lyt;
    @BindView(R.id.wallet_pager)
    ViewPager walletViewPager;
    @BindView(R.id.setting_pager)
    ViewPager settingWalletViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.recyclerViewTransactionList)
    RecyclerView recyclerView;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;


    private RecyclerView.Adapter txAdapter;
    private RecyclerView.LayoutManager txLayoutManager;

    public int walletListSize=0;
    private TransactionItemAdapter.OnItemClickListener listener;

    private String wallet_number = Prefs.getString(WALLET_NUMBER, "0");
    private String alias_name = Prefs.getString(ALIAS_NAME, "");
    private Map<String, ArrayList<TransactionItem>> walletTxMap = new HashMap<>();
    private ArrayList<WalletItem> walletList = new ArrayList<>();
    private ArrayList<TransactionItem> txList = new ArrayList<>();

    PagerAdapter walletAdapter;
    private DataBaseHelper db;
    private View rootView;
    private int position;

    public static MainFragment newInstance(int position) {
        MainFragment mainFragment = new MainFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        mainFragment.setArguments(args);

        return mainFragment;

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        db = new DataBaseHelper(Objects.requireNonNull(getActivity()).getApplicationContext());

        getActivity().setTitle(R.string.wallet_preferences);

        position = getArguments().getInt("position", 0);

        if(!Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
            btn_left.setVisibility(View.GONE);
            btn_right.setVisibility(View.GONE);
        }

        new ShowBalanceAsyncTask().execute();

        return rootView;
    }

    @Override
    public void onItemClicked(View v, TransactionItem item) {
        ShowTxItemDialogFragment df= new ShowTxItemDialogFragment(item);
        if (getFragmentManager() != null) {
            df.show(getFragmentManager(), "Dialog");
        }
    }

    public void enableDisableGoBack(boolean keyEvent){
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return keyEvent;
                }
            }
            return false;
        });
    }

    @SuppressLint("StaticFieldLeak")
    class  ShowBalanceAsyncTask extends AsyncTask<String , Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }


        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showWallets();
            showTransactionList();
            new ShowTxAsyncTask().execute(position);

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
                        w.PublicKey =wallet.getPublicKey();
                        w.Balance = wallet.getWalletBalance();
                        Log.v("wallet_number", db.getAllWallets().get(i).getWalletNumber());
                        walletList.add(w);
                    }
                }else {
                    WalletItem w = new WalletItem();
                    w.WalletNumber = wallet_number;
                    w.AliasName = alias_name;

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

    @SuppressLint("StaticFieldLeak")
    public class ShowTxAsyncTask extends AsyncTask<Integer , Integer, Void>{

        boolean isSuccess;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableDisableGoBack(true);
            avloadingIndicatorView.show();


        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            walletListSize++;
            if (!isSuccess){
                walletViewPager.setCurrentItem(position);
                walletAdapter.notifyDataSetChanged();
                avloadingIndicatorView.hide();
                enableDisableGoBack(false);
                txList.clear();

                if(walletTxMap.get(walletList.get(position).WalletNumber) != null)
                    txList.addAll(walletTxMap.get(walletList.get(position).WalletNumber));

//                Log.v("wallet_number", walletList.get(position).WalletNumber);
                txAdapter.notifyDataSetChanged();
            }else {
                Toast.makeText(getActivity(),  R.string.server_is_not_responding_please_try_again , Toast.LENGTH_LONG).show();
                enableDisableGoBack(false);
            }

        }

        @Override
        protected Void doInBackground(Integer... i) {
            try {

                OkHttpClient client = AppHelper.getOkhttpClient();
                Request request_tx_list;
                request_tx_list = AppHelper.getOkhttpRequestBuilder()
                        .url(AppConstants.GET_TX_URL+"?"+"wallet="+ walletList.get(i[0]).WalletNumber+"&w_pub_key="+walletList.get(i[0]).PublicKey+"&limit=50"+"&sort=desc")
                        .get()
                        .build();
                Response response_tx = client.newCall(request_tx_list).execute();

                String jsonTxData = response_tx.body().string();
                JSONObject reader = new JSONObject(jsonTxData);
                JSONArray txArray = reader.getJSONArray("tx_items");

                ArrayList<TransactionItem> txList = new ArrayList<>();

                for (int j = 0; j < txArray.length(); j++) {

                    JSONObject txObject = txArray.getJSONObject(j);
                    TransactionItem t = new TransactionItem();

                    long complete_mills = txObject.optLong("complete_time");
                    long action_mills = txObject.optLong("action_time");

                    Date dateCompleteMills = new Date(complete_mills);
                    Date dateActionMills = new Date(action_mills);

                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yy-MM-dd @HH:mm");
                    @SuppressLint("SimpleDateFormat") DateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                        df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    String date = df.format(dateCompleteMills);
                    t.Date = AppHelper.splitDate(date).get(0);
                    t.Hour = AppHelper.splitDate(date).get(1);
                    t.Description = txObject.optString("desc","");
                    t.Amount = txObject.optString("amount", "0");
                    t.ActionTime = dfTime.format(dateActionMills);;
                    t.CompleteTime = dfTime.format(dateCompleteMills);
                    t.Hash = txObject.optString("hash", "0");
                    t.PrevHash = txObject.optString("prev_hash", "0");
                    t.To = txObject.optString("to", "0");
                    t.From = txObject.optString("wallet", "0");
                    t.TxGroup = txObject.optString("group");
                    t.TxNumber = txObject.optString("seq");

                    txList.add(t);
                }

                walletTxMap.put(walletList.get(i[0]).WalletNumber, txList);

            }catch (Exception e){
                isSuccess = false;
                e.printStackTrace();
            }

            return null;
        }
    }

    public void showWallets() {
        // Locate the ViewPager in fragment_main.xml
        walletAdapter = new WalletPagerAdapter(getActivity(), walletList);

        // Binds the Adapter to the walletViewPager
        walletViewPager.setAdapter(walletAdapter);

    }

    public void showTransactionList() {

        walletViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int pagerPosition) {
                position = pagerPosition;
                final Handler handler = new Handler();
                handler.postDelayed(() -> {

                }, 2000);
                new ShowTxAsyncTask().execute(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        // Transaction List
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        txLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(txLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        listener=this;
        txAdapter = new TransactionItemAdapter(listener, this.txList, getActivity());
        recyclerView.setAdapter(txAdapter);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        SettingPagerAdapter adapter = new SettingPagerAdapter( getActivity(), getChildFragmentManager());
        adapter.addFragment(WalletGeneralSettingFragment.newInstance(walletList.get(position).WalletNumber));
        adapter.addFragment(WalletSecuritySettingFragment.newInstance(walletList.get(position).WalletNumber));
        adapter.addFragment(WalletDeveloperSettingFragment.newInstance(walletList.get(position).WalletNumber));

        viewPager.setAdapter(adapter);
    }

    @OnClick(R.id.btn_send)
    void sendButtonClicked() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("btn_name", "send");
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @OnClick(R.id.btn_reload)
    void reloadButtonClicked() {
        transaction_list.setVisibility(View.VISIBLE);
        wallet_setting_layout.setVisibility(View.GONE);
        new ShowTxAsyncTask().execute(position);
        Toast.makeText(getActivity(),  R.string.transactions_updated , Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.btn_settings)
    void settingButtonClicked() {
        transaction_list.setVisibility(View.GONE);
        wallet_setting_layout.setVisibility(View.VISIBLE);
        setupViewPager(settingWalletViewPager);
        tabLayout.setupWithViewPager(settingWalletViewPager);
    }

    @OnClick(R.id.btn_share)
    void shareButtonClicked() {
        String stringToSent = walletList.get(position).WalletNumber;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.wallet_info);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, stringToSent);
        startActivity(Intent.createChooser(sharingIntent, getActivity().getResources().getString(R.string.share_wallet)));
    }

    @OnClick(R.id.btn_request)
    void requestButtonClicked() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.putExtra("btn_name", "request");
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @OnClick(R.id.btn_right)
    void rightButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() + 1);
    }

    @OnClick(R.id.btn_left)
    void leftButtonClicked() {
        walletViewPager.setCurrentItem(walletViewPager.getCurrentItem() - 1);
    }


}
