package com.sikke.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.database.DataBaseHelper;

import static com.sikke.app.AppConstants.IS_LOGGEDIN;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;


public class LaunchActivity extends AppCompatActivity {

    private DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DataBaseHelper(this);

        // determine where to go
        if (!Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("btn_name", "my_wallet");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }

    }

}
