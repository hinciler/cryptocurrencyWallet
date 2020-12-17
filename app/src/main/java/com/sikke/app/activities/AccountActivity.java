package com.sikke.app.activities;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sikke.app.BaseActivity;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.thirdParty.MyBounceInterpolator;
import com.sikke.app.R;
import com.sikke.app.fragments.MainFragment;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountActivity extends BaseActivity {
    
    LinearLayout layout_btn;
    ImageView img_logo;
    Animation anim_slideup_btn;
    MainFragment mainFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//
//        String languageToLoad  = "en"; // your language
//        Locale locale = new Locale(languageToLoad);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);





        anim_slideup_btn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up_btn);

        layout_btn = findViewById(R.id.layout_btn);
        img_logo = findViewById(R.id.img_logo);

        animateLogo();
        layout_btn.startAnimation(anim_slideup_btn);

    }

    void animateLogo() {
        // Load the animation
        final Animation bounce_logo = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        double animationDuration =  2500;
        bounce_logo.setDuration((long)animationDuration);

        // Use custom animation interpolator to achieve the bounce effect
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.20, 70);

        bounce_logo.setInterpolator(interpolator);

        AnimationSet s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(bounce_logo);

        // Animate the button
        img_logo.startAnimation(s);

        // Run button animation again after it finished
        bounce_logo.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationEnd(Animation arg0) {

            }
        });
    }


    @OnClick(R.id.btn_open_login)
    void loginClicked(){
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),AccountWrapperActivity.class);
        intent.putExtra("btn_name","login");
        startActivity(intent);
    }

    @OnClick(R.id.btn_create)
    void createAccountClicked(){
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),AccountWrapperActivity.class);
        intent.putExtra("btn_name","create");
        startActivity(intent);
    }

    @OnClick(R.id.btn_rd_qr)
    void loginWithQRClicked(){
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),AccountWrapperActivity.class);
        intent.putExtra("btn_name","qr_login");
        startActivity(intent);
    }

    @OnClick(R.id.btn_create_wallet)
    void createWalletClicked(){
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("btn_name","create_wallet");
        startActivity(intent);
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
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int result = data.getIntExtra("result", 0);
                if (result == 1) {

                    Toast.makeText(this, "cikis yapildi", Toast.LENGTH_SHORT).show();
                }
            }

            if (resultCode == RESULT_CANCELED) {
                // code here for cancelled result
            }
        }
    }



}
