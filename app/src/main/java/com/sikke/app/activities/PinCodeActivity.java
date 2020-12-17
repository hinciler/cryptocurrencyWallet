package com.sikke.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.thirdParty.AES256Cipher;
import com.sikke.app.BaseActivity;
import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sikke.app.AppConstants.ACCESS_TOKEN;
import static com.sikke.app.AppConstants.IS_FIRST_CREATED;
import static com.sikke.app.AppConstants.IS_LOGGEDIN;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.PINCODE_STEP;
import static com.sikke.app.AppConstants.PIN_CODE;
import static com.sikke.app.AppConstants.PIN_TRY;
import static com.sikke.app.AppConstants.REFRESH_TOKEN;
import static com.sikke.app.AppConstants.U_EMAIL;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.helpers.AppHelper.toHexString;

public class PinCodeActivity extends BaseActivity {
    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    public static final String TAG = "PinLockView";

    @BindView(R.id.warning_pincode_layout)
    RelativeLayout warning_pincode_layout;
    @BindView(R.id.profile_name)
    TextView profile_name;
    @BindView(R.id.warning_list)
    TextView warning_list;
    @BindView(R.id.warning_list_1)
    TextView warning_list_1;
    @BindView(R.id.warning_list_2)
    TextView warning_list_2;
    int step = Prefs.getInt(PINCODE_STEP,0) ;
    String pin_code = Prefs.getString(PIN_CODE, "");
    private DataBaseHelper db;
    int pin_try = Prefs.getInt(PIN_TRY,0);

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
//            Log.d(TAG, "Pin complete: " + pin);
            Log.d(TAG, "Step " + step);
            Log.d(TAG, "Pin complete: " + Prefs.getBoolean(IS_LOGGEDIN,false));
            try {
                if(!Prefs.getBoolean(IS_LOGGEDIN, false)){

                    switch(step) {
                        case 0:
                            Prefs.putString(PIN_CODE, pin);
                            Prefs.putInt(PINCODE_STEP,  1);
                            openPincodeActivity();
                            break;
                        case 1:
                            if(pin_code.equals(pin)){
                                profile_name.setText(R.string.confirm_pincode);
                                Prefs.putBoolean(IS_LOGGEDIN, true);
                                Prefs.putBoolean(IS_FIRST_CREATED, true);
                                Prefs.putInt(PINCODE_STEP,  2);

                                byte[] pin_byte = AES256Cipher.twiceHash256(pin) ;
                                byte[] iv_byte = AES256Cipher.getRandomAesCryptIv();

                                String pin_hex  = toHexString(pin_byte);
                                String iv_hex = toHexString(iv_byte);
                                String email_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, Prefs.getString(U_EMAIL,""));
                                String password_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, Prefs.getString(U_PASSWORD,""));
                                String access_token_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, Prefs.getString(ACCESS_TOKEN,""));
                                String refresh_token_encrypt = AES256Cipher.encrypt(pin_byte, iv_byte, Prefs.getString(REFRESH_TOKEN,""));

                                addUserToDB(email_encrypt, password_encrypt, pin_hex, iv_hex,1,access_token_encrypt,refresh_token_encrypt);
                                //db.insertUser(u_email,password,pin_code,iv_code, is_loggedin,access_token,refresh_token );
                                openPincodeActivity();
                            }else{
                                profile_name.setText("Pin Eşleşmedi");
                                Prefs.putInt(PINCODE_STEP,  0);
                                openPincodeActivity();
                            }
                            break;
                    }

                }else {
                    byte[] pin_bytes = AES256Cipher.twiceHash256(pin) ;
                    String pin_check_hex  = toHexString(pin_bytes);
                    User user=  db.getUser(1);

                    String user_pin_code =  user.getPinCode();
//                    Log.d(TAG, "user_pin_code: " + user_pin_code);
//                    Log.d(TAG, "pin_check_hex: " + pin_check_hex);
                    if(user_pin_code.equals(pin_check_hex)){
                        Prefs.putString(PIN_CODE, pin);
                        openMainActivity();
                        Prefs.putInt(PIN_TRY, 0);
                    }else {
                        if(pin_try < 5){
                            pin_try++;
                            Prefs.putInt(PIN_TRY, pin_try);
                            profile_name.setText("Yanlış Pin "+pin_try);
                        }else {
                            profile_name.setText("Hesap Bloke");
                            Prefs.putString(PIN_CODE, "");
                            user.setIsLoggedIn(0);
                            db.updateUser(user);
                            Prefs.putBoolean(IS_LOGGEDIN_BOOLEAN, false);
                            Prefs.putInt(PINCODE_STEP,  0);
                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEmpty() {
//            Log.d(TAG, "Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
//            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
        }
    };

    private void openPincodeActivity() {
        Intent intent = new Intent(this, PinCodeActivity.class);
        startActivity(intent);
    }

    private void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("btn_name","my_wallet");
        startActivity(intent);
    }
    private void addUserToDB(String u_email, String password, String pin_code,String iv_code, int is_loggedin,  String access_token, String refresh_token) {
        // inserting user in db and getting
        long id = db.insertUser(u_email,password,pin_code,iv_code, is_loggedin,access_token,refresh_token );

        // get the newly inserted note from db
//        Wallet n = db.getUser(id);

//        if (n != null) {
//        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pin_code);
        ButterKnife.bind(this);
        db = new DataBaseHelper(this);



//        for (int i= 0; i< db.getAllUsers().size(); i++){
//            Log.v("access_token",db.getAllUsers().get(i).getAccessToken() );
//            byte[] pincode =   AppHelper.hexStringToByteArray(db.getAllUsers().get(i).getPinCode());
//            byte[] iv = AppHelper.hexStringToByteArray(db.getAllUsers().get(i).getIvCode());
//           try {
//               Log.v("access_token",AES256Cipher.decrypt(pincode, iv, db.getAllUsers().get(i).getEmail())  );
//           } catch (Exception e) {
//               e.printStackTrace();
//           }
//       }

//        Log.v("pin", Prefs.getString(PIN_CODE, "merhaba"));

        checkStepSizeToSetText();

        mPinLockView =  findViewById(R.id.pin_lock_view);
        mIndicatorDots = findViewById(R.id.indicator_dots);

        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
        //mPinLockView.enableLayoutShuffling();

        mPinLockView.setPinLength(4);
        mPinLockView.setTextColor(ContextCompat.getColor(this, R.color.white));

        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
    }

    private void checkStepSizeToSetText() {

        switch(step) {
            case 0:
                profile_name.setVisibility(View.GONE);
                warning_pincode_layout.setVisibility(View.VISIBLE);
                warning_list.setText(R.string.pincode_warning);
                warning_list_1.setText(R.string.pincode_warning_list_1);
                warning_list_2.setText(R.string.pincode_warning_list_2);
                break;
            case 1:
                profile_name.setText(R.string.confirm_pincode);
                profile_name.setVisibility(View.VISIBLE);
                warning_pincode_layout.setVisibility(View.GONE);
                break;
            case 2:
                profile_name.setVisibility(View.VISIBLE);
                warning_pincode_layout.setVisibility(View.GONE);
                profile_name.setText(R.string.input_pincode);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
