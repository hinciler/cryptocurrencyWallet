package com.sikke.app.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.pixplicity.easyprefs.library.Prefs;
import com.sikke.app.AppConstants;
import com.sikke.app.Base58;
import com.sikke.app.BaseActivity;
import com.sikke.app.BuildConfig;
import com.sikke.app.database.model.Wallet;
import com.sikke.app.helpers.AppHelper;
import com.sikke.app.helpers.ECDSAHelper;
import com.sikke.app.receivers.MyScheduledReceiver;
import com.sikke.app.R;
import com.sikke.app.database.DataBaseHelper;
import com.sikke.app.database.model.User;
import com.sikke.app.fragments.CreateWalletFragment;
import com.sikke.app.fragments.MainFragment;
import com.sikke.app.fragments.MyWalletsFragment;
import com.sikke.app.fragments.ReadWalletFragment;
import com.sikke.app.fragments.RequestSikkeFragment;
import com.sikke.app.fragments.SendSikkeFragment;
import com.sikke.app.interfaces.NavigationInterface;
import com.sikke.app.thirdParty.AES256Cipher;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sikke.app.AppConstants.DEFAULT_WALLET;
import static com.sikke.app.AppConstants.IS_LOGGEDIN_BOOLEAN;
import static com.sikke.app.AppConstants.TIMESTAMP;
import static com.sikke.app.AppConstants.URL_HELP;
import static com.sikke.app.AppConstants.URL_PARIFIX;
import static com.sikke.app.AppConstants.URL_SIKKE;
import static com.sikke.app.AppConstants.U_PASSWORD;
import static com.sikke.app.helpers.AppHelper.getDownloadFilePath;


public class MainActivity extends BaseActivity implements NavigationInterface{

    MainFragment mainFragment = null;
    SendSikkeFragment sendSikkeFragment =null;
    RequestSikkeFragment requestSikkeFragment =null;
    CreateWalletFragment createWalletFragment = null;
    MyWalletsFragment myWalletsFragment = null;
    private NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout  drawer;
    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;
    private Toolbar toolbar;

    // index to identify current nav menu country_item
    public static int navItemIndex = 0;

    // toolbar titles respected to selected nav menu country_item
    private String[] activityTitles;
    private DataBaseHelper db;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    // tags used to attach the fragments

    private static final String TAG_HOME = "home";
    private static final String TAG = "logout";
    private static final String TAG_CREATE_WALLET = "create_wallet";
    private static final String TAG_IMPORT_WALLET = "movies";
    public static String CURRENT_TAG = TAG_HOME;
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    public User user;
    public Uri selectedFile;

    BroadcastReceiver myReceiver;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        avloadingIndicatorView.hide();
        callReceiver();


        db = new DataBaseHelper(this);
        mHandler = new Handler();
        navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);

        user = db.getUser(1);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
        }



        Bundle b = getIntent().getExtras();
        String id = b.getString("btn_name");
        if (id.equals("main")){
            int position = b.getInt("position");
            loadMainFragment(position);
        }else if (id.equals("send")){
            int position = b.getInt("position");
            loadSendSikkeFragment(position);
        }else if (id.equals("request")){
            int position = b.getInt("position");
            loadRequestSikkeFragment(position);
        }else if (id.equals("create_wallet")){
            loadCreateWalletFragment();
        }else if (id.equals("my_wallet")){
            loadMyWallet();
        }
    }

    private void callReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("sikke.ACTION_FINISH");
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                /**** Returning Result *****/
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", 1);
                MainActivity.this.setResult(RESULT_OK, returnIntent);
                SQLiteDatabase sqLiteDatabase = db.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
                sqLiteDatabase.delete("user", null, null);
                sqLiteDatabase.delete("wallet", null, null);
                Prefs.putString(U_PASSWORD, "");
                Prefs.putBoolean(IS_LOGGEDIN_BOOLEAN, false);
                finish();
            }
        };

        registerReceiver(myReceiver, intentFilter);
    }

    private void addWalletToDB(String w_number, String pri_key, String pub_key, String alias_name, String balance) {

        long id = db.insertWalletNumber(w_number, pri_key, pub_key,  alias_name, balance);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK) {
            selectedFile = data.getData(); //The uri with the location of the file
            try {
                String strSelectedFile = selectedFile.toString();
                String file_extension = strSelectedFile.substring(strSelectedFile.length() - 3);
                if(file_extension.equals("skk")){
                    InputStream inputStream =  getContentResolver().openInputStream(selectedFile);;

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int i;

                    i = inputStream.read();
                    while (i != -1) {
                        byteArrayOutputStream.write(i);
                        i = inputStream.read();
                    }
                    inputStream.close();

                    JSONObject JWalletObject = new JSONObject(byteArrayOutputStream.toString());
                    JSONArray walletArray = JWalletObject.optJSONArray("wallets");

                    for(int j=0; j<walletArray.length(); j++){
                        Wallet wallet;
                        JSONObject walletObject = walletArray.getJSONObject(j);
                        String wallet_number = walletObject.getString("wallet_address");
                        String pri_key = walletObject.optString("private_key");
                        String balance = walletObject.optString("balance");

                        wallet= db.getWallet(wallet_number);

                        String pin_code = Prefs.getString(U_PASSWORD, "") ;
                        byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                        String encryptedPvtKey = AES256Cipher.encryptPvt(u_password,pri_key);
                        if(wallet.getWalletNumber() == null){
                            byte[] pvt_byte = Base58.decode(AES256Cipher.decryptPvt(u_password, db.getAllWallets().get(i).getPrivateKey()));
                            BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                            byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                            addWalletToDB(wallet_number,encryptedPvtKey,Base58.encode(public_byte), "", balance);

                            new ImportWalletAsyncTask().execute(pri_key);
                        }
                    }

                }else {
                    Toast.makeText(this, "Yanlış Formatda Dosya Seçtiniz", Toast.LENGTH_SHORT).show();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    class ImportWalletAsyncTask extends AsyncTask<String, Void, Void> {

        User user=  db.getUser(1);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avloadingIndicatorView.show();
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            avloadingIndicatorView.hide();
        }

        @Override
        protected Void doInBackground(String... params) {

            try {
                String private_key =  params[0];

                OkHttpClient client = AppHelper.getOkhttpClient();

                //Save Wallet To Api
                PrivateKey privateKey =  ECDSAHelper.importPrivateKey(private_key);
                String sign = ECDSAHelper.sign(TIMESTAMP, privateKey);
                String access_token = user.getAccessToken();

                String pin_code = Prefs.getString(U_PASSWORD, "") ;
                byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                String encryptedPvtKey = AES256Cipher.encryptPvt(u_password,private_key);


                byte[] pvt_byte = Base58.decode(private_key);

                BigInteger priv_big_integer = new BigInteger(1, pvt_byte);
                byte[] public_byte= AppHelper.hexStringToByteArray(ECDSAHelper.publicKeyFromPrivate(priv_big_integer)) ;
                String public_key = Base58.encode(public_byte);

                RequestBody requestUpdateBody = new MultipartBody.Builder()
                        .addFormDataPart("sign", sign)
                        .addFormDataPart("nonce", TIMESTAMP)
                        .addFormDataPart("w_type",  DEFAULT_WALLET)
                        .addFormDataPart("w_pub_key", public_key)
                        .addFormDataPart("w_zeugma",  encryptedPvtKey)
                        .setType(MultipartBody.FORM)
                        .build();

                Request requestUpdate = AppHelper.getOkhttpRequestBuilder()
                        .addHeader("Authorization", "Bearer "+ access_token)
                        .url(AppConstants.GET_WALLET_ITEM)
                        .put(requestUpdateBody)
                        .build();

                Response responseUpdate = client.newCall(requestUpdate).execute();
                String jsonUpdateData = responseUpdate.body().string();

                JSONObject JWalletObject = new JSONObject(jsonUpdateData);

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_wallet, menu);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    AppHelper.requestPermission(this);
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.import_wallet:
                try {

                    Intent intent = new Intent()
                            .setType("*/*")
                            .setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);

                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;

            case R.id.export_wallet:
                AppHelper.requestPermission(this);

                try {
                    JSONArray jsonArray = new JSONArray();
                    String pin_code = Prefs.getString(U_PASSWORD, "");

                    for(int i= 0; i< db.getAllWallets().size(); i++){
                        JSONObject wallet = new JSONObject();
                        byte [] u_password =   AES256Cipher.key128Bit(pin_code);
                        String pvt_key = AES256Cipher.decryptPvt(u_password, db.getAllWallets().get(i).getPrivateKey() );
                        wallet.put("private_key", pvt_key);
                        wallet.put("wallet_address",  db.getAllWallets().get(i).getWalletNumber());
                        Log.v("wallet_number",   db.getAllWallets().get(i).getWalletNumber());
                        jsonArray.put(wallet);
                    }

                    JSONObject walletObj = new JSONObject();
                    walletObj.put("wallets", jsonArray);

                    String jsonStr = walletObj.toString();

                    File exportWallet = new File(getDownloadFilePath(this) + "/" + "keselerim.skk"   );
                    OutputStream output = new FileOutputStream(exportWallet);
                    OutputStreamWriter osw = new OutputStreamWriter(output);

                    // Write the string to the file
                    osw.write(jsonStr);

                    osw.flush();
                    osw.close();

                    System.out.println("jsonString: "+ jsonStr);

                    Toast.makeText(this, R.string.congratulations_backup+getDownloadFilePath(this) + "keselerim.skk", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                return true;

            case R.id.refresh:
                Toast.makeText(this, "Cüzdanlar Yenilendi", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void lockDrawer() {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockDrawer() {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }



    public void loadMyWallet() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (myWalletsFragment == null) {
            myWalletsFragment = MyWalletsFragment.newInstance();
            ft.replace(R.id.frameLayoutMainFragments, myWalletsFragment, "my_wallet");
        } else {
            ft.show(myWalletsFragment);
        }
        ft.commit();
    }


    public void loadMainFragment(int position) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance(position);
            ft.replace(R.id.frameLayoutMainFragments, mainFragment, "main_fragment");
        } else {
            ft.show(mainFragment);
        }
        ft.commit();
    }

    private void loadSendSikkeFragment(int position) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (sendSikkeFragment == null) {
            sendSikkeFragment = SendSikkeFragment.newInstance(position);
            ft.replace(R.id.frameLayoutMainFragments, sendSikkeFragment, "send_skk");
        } else {
            ft.show(sendSikkeFragment);
        }
        ft.commit();
    }

    private void loadRequestSikkeFragment(int position) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (requestSikkeFragment == null) {
            requestSikkeFragment = RequestSikkeFragment.newInstance(position);
            ft.replace(R.id.frameLayoutMainFragments, requestSikkeFragment, "request_skk");
        } else {
            ft.show(requestSikkeFragment);
        }
        ft.commit();
    }

    private void loadCreateWalletFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (createWalletFragment == null) {
            createWalletFragment = CreateWalletFragment.newInstance();
            ft.replace(R.id.frameLayoutMainFragments, createWalletFragment, "create_wallet_fragment");
        } else {
            ft.show(createWalletFragment);
        }
        ft.commit();
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void loadHomeFragment() {
        // selecting appropriate nav menu country_item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                // update the main content by replacing fragments

                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frameLayoutMainFragments, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        //Closing drawer on country_item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                MyWalletsFragment homeFragment = new MyWalletsFragment();
                return homeFragment;
            case 1:
                // create_wallet
                CreateWalletFragment createWalletFragment = new CreateWalletFragment();
                return createWalletFragment;
            case 2:
                // movies fragment
                ReadWalletFragment readWalletFragment = new ReadWalletFragment();
                return readWalletFragment;
            case 3:
                // notifications fragment
                ReadWalletFragment notificationsFragment = new ReadWalletFragment();
                return notificationsFragment;
            default:
                return new MyWalletsFragment();
        }
    }

    private void setUpNavigationView() {

        Menu mMenu = navigationView.getMenu();
        MenuItem exitMenu = mMenu.findItem(R.id.nav_exit);
        exitMenu.setIcon(new IconicsDrawable(getApplication())
                .icon(FontAwesome.Icon.faw_sign_out_alt));
        MenuItem helpMenu = mMenu.findItem(R.id.nav_help);
        helpMenu.setIcon(new IconicsDrawable(getApplication())
                .icon(FontAwesome.Icon.faw_question_circle));
        MenuItem sikkeMenu = mMenu.findItem(R.id.nav_sikke);
        sikkeMenu.setIcon(new IconicsDrawable(getApplication())
                .icon(FontAwesome.Icon.faw_link));
        MenuItem parifixMenu = mMenu.findItem(R.id.nav_parifix);
        parifixMenu.setIcon(new IconicsDrawable(getApplication())
                .icon(FontAwesome.Icon.faw_link));
        MenuItem versionName = mMenu.findItem(R.id.nav_version);
        versionName.setTitle("Version: " + BuildConfig.VERSION_NAME);


        if(!Prefs.getBoolean(IS_LOGGEDIN_BOOLEAN, false)){
            mMenu.setGroupVisible(R.id.main_menu, false);
            MenuItem myAccountMenu = mMenu.findItem(R.id.nav_my_account);
            myAccountMenu.setVisible(false);
        }

        //Setting Navigation View Item Selected Listener to handle the country_item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on country_item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intentWeb = new Intent(MainActivity.this, WebviewActivity.class);
                //Check to see which country_item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_create_wallet:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_CREATE_WALLET;
                        break;
                    case R.id.nav_import_wallet:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_IMPORT_WALLET;
                        break;
                    case R.id.nav_contacts:
                        Toast.makeText(MainActivity.this, "Bu Özellik İlerleyen Sürümlerde Aktif Olacak", Toast.LENGTH_SHORT).show();
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_my_account:
                        startActivity(new Intent(MainActivity.this, AccountSettingsActivity.class));
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_exit:

                        new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setTitle(MainActivity.this.getResources().getString(R.string.warning))
                                .setMessage(MainActivity.this.getResources().getString(R.string.exit_confirmation))
                                .setPositiveButton(MainActivity.this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AppHelper.setLogout(db, MainActivity.this);
                                    }
                                })
                                .setCancelable(false)
                                .setNegativeButton(MainActivity.this.getResources().getString(R.string.no), null)
                                .show();
                        return true;

                    case R.id.nav_help:
                        intentWeb.putExtra("url",URL_HELP);
                        startActivity(intentWeb);
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_sikke:
                        intentWeb.putExtra("url",URL_SIKKE);
                        startActivity(intentWeb);
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_parifix:
                        intentWeb.putExtra("url",URL_PARIFIX);
                        startActivity(intentWeb);
                        drawer.closeDrawers();
                        return true;

                    default:
                        navItemIndex = 0;
                }

                //Checking if the country_item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        FragmentManager manager = getFragmentManager();
        Fragment firstFrag = manager.findFragmentByTag("my_wallet");
        if(firstFrag != null ){
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setTitle(this.getResources().getString(R.string.warning))
                    .setMessage(this.getResources().getString(R.string.exit_confirmation))
                    .setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton(this.getResources().getString(R.string.no), null)
                    .show();
        }else {
            super.onBackPressed();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        AppHelper.scheduleAlarm(getBaseContext(),pendingIntent,alarmManager );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppHelper.scheduleAlarm(getBaseContext(),pendingIntent,alarmManager );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.v("Account Activity", "Scheduled Alarm Cancelled");
        }
    }

}
