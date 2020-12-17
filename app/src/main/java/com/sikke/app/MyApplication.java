package com.sikke.app;

import android.app.Application;
import android.content.ContextWrapper;
import android.support.multidex.MultiDexApplication;

import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.typeface.GenericFont;
import com.onesignal.OneSignal;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.thirdParty.CustomFont;

import static com.sikke.app.AppConstants.setValuesFromSharedPreferences;

/**
 * Created by huseyin on 03/04/18.
 */

public class MyApplication extends MultiDexApplication {

    private static MyApplication singleton;

    public static MyApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        initOneSignal();
        initPref();
        initIcon();
        setValuesFromSharedPreferences(getApplicationContext());

//        AppHelper.setLanguage(getApplicationContext(), "en");

        /*String languageCode = AppConstants.LANGUAGE; // shared preferences dan çekiyor contstansın içinden
        if (languageCode != null && !languageCode.isEmpty()) {
            AppHelper.setLanguage(getApplicationContext(), languageCode);
        }*/
    }

    private void initIcon(){
        //register custom fonts like this (or also provide a font definition file)
        Iconics.registerFont(new CustomFont());
        //Generic font creation process
        GenericFont gf2 = new GenericFont("GenericFont", "SampleGenericFont", "gmf", "fonts/materialdrawerfont.ttf");
        gf2.registerIcon("person", '\ue800');
        gf2.registerIcon("up", '\ue801');
        gf2.registerIcon("down", '\ue802');
        Iconics.registerFont(gf2);

    }

    private void initPref(){
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    private void initOneSignal() {
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }


}
