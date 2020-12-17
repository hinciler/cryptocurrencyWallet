package com.sikke.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.activities.AccountActivity;
import com.sikke.app.activities.MainActivity;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.helpers.AppHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.AppConstants.setValuesFromSharedPreferences;

public class BaseActivity extends AppCompatActivity {

    private DataBaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setValuesFromSharedPreferences(getBaseContext());
        String languageCode = AppConstants.LANGUAGE; // shared preferences dan çekiyor contstansın içinden
        if (languageCode != null && !languageCode.isEmpty()) {
            AppHelper.setLanguage(getBaseContext(), languageCode);
        }

        db = new DataBaseHelper(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public static final long DISCONNECT_TIMEOUT = 150000; // 30 sec = 30 * 1000 ms

    @SuppressLint("HandlerLeak")
    private Handler disconnectHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Süre Aşımı!");
            alertDialog
                    .setMessage("Uygulama Süreniz Dolmuştur Lütfen Tekrar Giriş Yapınız.");
            alertDialog.setNegativeButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            AppHelper.setLogout(db, BaseActivity.this);

                            dialog.cancel();
                        }
                    });

            AlertDialog alert = alertDialog.create();

            alert.show();

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                alert.cancel();
                AppHelper.setLogout(db, BaseActivity.this);
            }, 2000);
        }
    };

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
