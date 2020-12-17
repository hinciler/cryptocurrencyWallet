package com.sikke.app.activities;


import android.content.Intent;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.sikke.app.BaseActivity;
import com.sikke.app.R;
import com.sikke.app.fragments.CreateAccountFragment;
import com.sikke.app.fragments.ForgotPasswordFragment;
import com.sikke.app.fragments.LoginFragment;
import com.sikke.app.fragments.ReadWalletFragment;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountWrapperActivity extends BaseActivity {

    LoginFragment loginFragment=null;
    CreateAccountFragment createAccountFragment =null;
    ReadWalletFragment readWalletFragment=null;
    ForgotPasswordFragment forgotPasswordFragment=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        setContentView(R.layout.activity_account_wrapper);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle b = getIntent().getExtras();
        String id = b.getString("btn_name");
        if (id.equals("login")){
            loadLoginFragment();
        }else if(id.equals("create")){
            loadCreateFragment();
        }else if (id.equals("qr_login")){
            loadReadWalletFragment();
        }else if (id.equals("forgot_password")){
            loadForgotPasswordFragment();
        }
    }




    private void loadLoginFragment() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance();
            ft.replace(R.id.frameLayoutFragments, loginFragment, "first");
        } else {
            ft.show(loginFragment);
        }
        ft.commit();
    }

    private void loadCreateFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (createAccountFragment == null) {
            createAccountFragment = CreateAccountFragment.newInstance();
            ft.replace(R.id.frameLayoutFragments, createAccountFragment, "second");
        } else {
            ft.show(loginFragment);
        }
        ft.commit();
    }

    private void loadReadWalletFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (readWalletFragment == null) {
            readWalletFragment = ReadWalletFragment.newInstance();
            ft.replace(R.id.frameLayoutFragments, readWalletFragment, "third");
        } else {
            ft.show(loginFragment);
        }
        ft.commit();
    }

    private void loadForgotPasswordFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (forgotPasswordFragment == null) {
            forgotPasswordFragment = ForgotPasswordFragment.newInstance();
            ft.replace(R.id.frameLayoutFragments, forgotPasswordFragment, "fourth");
        } else {
            ft.show(loginFragment);
        }
        ft.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
