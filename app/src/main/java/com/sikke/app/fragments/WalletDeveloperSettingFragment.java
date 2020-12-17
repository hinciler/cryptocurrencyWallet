package com.sikke.app.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.Wallet;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class WalletDeveloperSettingFragment extends Fragment {

    @BindView(R.id.txt_callback_url)
    TextView txt_callback_url;

    private DataBaseHelper db;
    public Wallet wallet;
    public String wallet_number;
    public static WalletDeveloperSettingFragment newInstance(String wallet_number){

        WalletDeveloperSettingFragment walletDeveloperSettingFragment= new WalletDeveloperSettingFragment();

        Bundle args = new Bundle();
        args.putString("wallet_number", wallet_number);
        walletDeveloperSettingFragment.setArguments(args);

        return walletDeveloperSettingFragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_developer_setting, container, false);
        ButterKnife.bind(this, rootView);
        db = new DataBaseHelper(getActivity().getApplicationContext());
        wallet_number = getArguments().getString("wallet_number", "");
        wallet = db.getWallet(wallet_number);

        txt_callback_url.setText(wallet.getWalletCallback());

        return rootView;
    }

}
