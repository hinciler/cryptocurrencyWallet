package com.sikke.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sikke.app.database.model.SavedWallet;
import com.sikke.app.database.model.User;
import com.sikke.app.database.model.Wallet;

import java.util.ArrayList;
import java.util.List;


public class DataBaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 8;

    // Database Name
    private static final String DATABASE_NAME = "wallet_db";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void dropTable(SQLiteDatabase db){
        db.execSQL("DROP TABLE wallet;");
        db.execSQL("DROP TABLE user;");
        db.execSQL("DROP TABLE saved_wallet;");
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create tables
        db.execSQL(Wallet.CREATE_TABLE);
        db.execSQL(SavedWallet.CREATE_TABLE);
        db.execSQL(User.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Wallet.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SavedWallet.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // ------------------------ "Wallet" table methods ----------------//
    public long insertWalletNumber(String wallet_number, String private_number, String public_number, String alias_name, String balance) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Wallet.COLUMN_WALLET_NUMBER, wallet_number);
        values.put(Wallet.COLUMN_W_PRIVATE_KEY, private_number);
        values.put(Wallet.COLUMN_W_PUBLIC_KEY, public_number);
        values.put(Wallet.COLUMN_WALLET_ALIAS_NAME, alias_name);
        values.put(Wallet.COLUMN_WALLET_BALANCE, balance);

        // insert row
        long id = db.insert(Wallet.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }


    public Wallet getWallet(String wallet_number) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        Wallet wallet= new Wallet();

        Cursor cursor = db.query(Wallet.TABLE_NAME,
                new String[]{Wallet.COLUMN_ID, Wallet.COLUMN_WALLET_NUMBER,Wallet.COLUMN_WALLET_ALIAS_NAME,Wallet.COLUMN_WALLET_BALANCE, Wallet.COLUMN_WALLET_TYPE,Wallet.COLUMN_WALLET_STATUS,Wallet.COLUMN_WALLET_LIMIT_HOURLY, Wallet.COLUMN_WALLET_LIMIT_DAILY,Wallet.COLUMN_WALLET_LIMIT_MAX_AMOUNT,Wallet.COLUMN_W_PRIVATE_KEY,Wallet.COLUMN_W_PUBLIC_KEY, Wallet.COLUMN_WALLET_INSERT_DATE, Wallet.COLUMN_WALLET_NOTIFICATION, Wallet.COLUMN_WALLET_CALLBACK},
                Wallet.COLUMN_WALLET_NUMBER + "=?",
                new String[]{wallet_number}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        if( cursor != null && cursor.moveToFirst() ) {
            // prepare wallet object
             wallet = new Wallet(
                    cursor.getInt(cursor.getColumnIndex(Wallet.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_NUMBER)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_ALIAS_NAME)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_BALANCE)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_TYPE)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_STATUS)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_LIMIT_HOURLY)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_LIMIT_DAILY)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_LIMIT_MAX_AMOUNT)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_W_PRIVATE_KEY)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_W_PUBLIC_KEY)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_INSERT_DATE)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_NOTIFICATION)),
                    cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_CALLBACK))
            );
        }

        // close the db connection
        cursor.close();
        db.close();

        return wallet;
    }

    public int updateWallet(Wallet wallet) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Wallet.COLUMN_WALLET_ALIAS_NAME, wallet.getWalletAliasName());
        values.put(Wallet.COLUMN_WALLET_BALANCE, wallet.getWalletBalance());
        values.put(Wallet.COLUMN_WALLET_TYPE, wallet.getWalletType());
        values.put(Wallet.COLUMN_WALLET_STATUS, wallet.getWalletStatus());
        values.put(Wallet.COLUMN_W_PRIVATE_KEY, wallet.getPrivateKey());
        values.put(Wallet.COLUMN_W_PUBLIC_KEY, wallet.getPublicKey());
        values.put(Wallet.COLUMN_WALLET_LIMIT_HOURLY, wallet.getWalletLimitHourly());
        values.put(Wallet.COLUMN_WALLET_LIMIT_DAILY, wallet.getWalletLimitDaily());
        values.put(Wallet.COLUMN_WALLET_NOTIFICATION, wallet.getWalletNotification());
        values.put(Wallet.COLUMN_WALLET_CALLBACK, wallet.getWalletCallback());
        values.put(Wallet.COLUMN_WALLET_LIMIT_MAX_AMOUNT, wallet.getWalletLimitMaxAmount());

        // updating row
        return db.update(Wallet.TABLE_NAME, values, Wallet.COLUMN_WALLET_NUMBER + " = ?",
                new String[]{String.valueOf(wallet.getWalletNumber())});


    }

    public List<Wallet> getAllWallets() {
        List<Wallet> wallets = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Wallet.TABLE_NAME+ " ORDER BY " +
                Wallet.COLUMN_ID + " ASC" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Wallet wallet = new Wallet();
                wallet.setId(cursor.getInt(cursor.getColumnIndex(Wallet.COLUMN_ID)));
                wallet.setWalletNumber(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_NUMBER)));
                wallet.setWalletBalance(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_BALANCE)));
                wallet.setPrivateKey(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_W_PRIVATE_KEY)));
                wallet.setPublicKey(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_W_PUBLIC_KEY)));
                wallet.setInsertDate(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_INSERT_DATE)));
                wallets.add(wallet);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return wallets list
        return wallets;
    }

    public int getWalletsCount() {
        String countQuery = "SELECT  * FROM " + Wallet.TABLE_NAME + " ORDER BY " +
                Wallet.COLUMN_WALLET_INSERT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        db.close();
        // return count
        return count;
    }


    public void deleteWallet(Wallet wallet) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Wallet.TABLE_NAME, Wallet.COLUMN_ID + " = ?",
                new String[]{String.valueOf(wallet.getId())});
        db.close();
    }

    // ------------------------ "SavedWallet" table methods ----------------//

    public long insertWallet(String wallet_number) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Wallet.COLUMN_WALLET_NUMBER, wallet_number);

        // insert row
        long id = db.insert(Wallet.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }



    public SavedWallet getWallet(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SavedWallet.TABLE_NAME,
                new String[]{SavedWallet.COLUMN_ID, SavedWallet.COLUMN_WALLET_NUMBER, SavedWallet.COLUMN_INSERT_DATE},
                SavedWallet.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        SavedWallet savedWallet= new SavedWallet(
                cursor.getInt(cursor.getColumnIndex(Wallet.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_NUMBER)),
                cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_INSERT_DATE))
        );

        // close the db connection
        cursor.close();

        return savedWallet;
    }

    public List<SavedWallet> getAllSavedWallets() {
        List<SavedWallet> savedWallets = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + SavedWallet.TABLE_NAME+ " ORDER BY " +
                SavedWallet.COLUMN_INSERT_DATE + " DESC" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SavedWallet savedWallet = new SavedWallet();
                savedWallet.setId(cursor.getInt(cursor.getColumnIndex(Wallet.COLUMN_ID)));
                savedWallet.setWalletNumber(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_NUMBER)));
                savedWallet.setInsertDate(cursor.getString(cursor.getColumnIndex(Wallet.COLUMN_WALLET_INSERT_DATE)));
                savedWallets.add(savedWallet);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return savedWallets;
    }

    // ------------------------ "User" table methods ----------------//
    public long insertUser(String email, String password, String pinCode, String iv_code, int isLoggedIn, String accessToken, String refreshToken) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        // `id` and `timestamp` will be inserted automatically.
        values.put(User.COLUMN_U_EMAIL, email);
        values.put(User.COLUMN_U_PASSWORD, password);
        values.put(User.COLUMN_PIN_CODE, pinCode);
        values.put(User.COLUMN_IV_CODE, iv_code);
        values.put(User.COLUMN_IS_LOGGEDIN, isLoggedIn);
        values.put(User.COLUMN_U_ACCESS_TOKEN, accessToken);
        values.put(User.COLUMN_U_REFRESH_TOKEN, refreshToken);

        // insert row
        long id = db.insert(User.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }


    public User getUser(int is_loggedin) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        User user = new User();
        Cursor cursor = db.query(User.TABLE_NAME,
                new String[]{User.COLUMN_ID, User.COLUMN_U_EMAIL, User.COLUMN_U_NAME, User.COLUMN_U_SURNAME, User.COLUMN_U_ALIAS, User.COLUMN_U_INDENTITY_NUMBER,
                        User.COLUMN_U_PHONE, User.COLUMN_U_PHONE_COUNTRY, User.COLUMN_U_COUNTRY, User.COLUMN_U_CITY, User.COLUMN_U_DISTRICT, User.COLUMN_U_LAST_LOGIN_DATE, User.COLUMN_U_STATUS,
                        User.COLUMN_U_TAX_OFFICE, User.COLUMN_U_TAX_TITLE, User.COLUMN_U_TZ,
                        User.COLUMN_U_PASSWORD, User.COLUMN_PIN_CODE, User.COLUMN_IV_CODE, User.COLUMN_U_SECURITY_QUESTION, User.COLUMN_U_SECURITY_ANSWER, User.COLUMN_IS_LOGGEDIN,
                        User.COLUMN_U_ACCESS_TOKEN, User.COLUMN_U_REFRESH_TOKEN, User.COLUMN_U_LANGUAGE, User.COLUMN_INSERT_DATE},
                User.COLUMN_IS_LOGGEDIN + "=?",
                new String[]{String.valueOf(is_loggedin)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if( cursor != null && cursor.moveToFirst() ){

             user = new User(
                    cursor.getInt(cursor.getColumnIndex(User.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_NAME)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_SURNAME)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_ALIAS)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_INDENTITY_NUMBER)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_PHONE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_PHONE_COUNTRY)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_COUNTRY)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_CITY)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_DISTRICT)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_LAST_LOGIN_DATE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_STATUS)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_TAX_OFFICE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_TAX_TITLE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_TZ)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_PASSWORD)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_PIN_CODE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_IV_CODE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_SECURITY_QUESTION)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_SECURITY_ANSWER)),
                    cursor.getInt(cursor.getColumnIndex(User.COLUMN_IS_LOGGEDIN)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_ACCESS_TOKEN)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_REFRESH_TOKEN)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_U_LANGUAGE)),
                    cursor.getString(cursor.getColumnIndex(User.COLUMN_INSERT_DATE))
            );

            cursor.close();
        }
        // prepare user object

        // close the db connection
        cursor.close();
        db.close();

        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + User.TABLE_NAME + " ORDER BY " +
                User.COLUMN_INSERT_DATE + " DESC" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(User.COLUMN_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(User.COLUMN_U_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(User.COLUMN_U_PASSWORD)));
                user.setPinCode(cursor.getString(cursor.getColumnIndex(User.COLUMN_PIN_CODE)));
                user.setIvCode(cursor.getString(cursor.getColumnIndex(User.COLUMN_IV_CODE)));
                user.setIsLoggedIn(cursor.getInt(cursor.getColumnIndex(User.COLUMN_IS_LOGGEDIN)));
                user.setAccessToken(cursor.getString(cursor.getColumnIndex(User.COLUMN_U_ACCESS_TOKEN)));
                user.setRefreshToken(cursor.getString(cursor.getColumnIndex(User.COLUMN_U_REFRESH_TOKEN)));
                users.add(user);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return users;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.COLUMN_U_ALIAS, user.getAlias());
        values.put(User.COLUMN_U_PASSWORD, user.getPassword());
        values.put(User.COLUMN_IV_CODE, user.getIvCode());
        values.put(User.COLUMN_PIN_CODE, user.getPinCode());
        values.put(User.COLUMN_U_NAME, user.getName());
        values.put(User.COLUMN_U_SURNAME, user.getSurname());
        values.put(User.COLUMN_U_PHONE, user.getPhone());
        values.put(User.COLUMN_U_PHONE_COUNTRY, user.getPhoneCountry());
        values.put(User.COLUMN_U_INDENTITY_NUMBER, user.getIdentityNumber());
        values.put(User.COLUMN_U_COUNTRY, user.getCountry());
        values.put(User.COLUMN_U_CITY, user.getCity());
        values.put(User.COLUMN_U_DISTRICT, user.getDistrict());
        values.put(User.COLUMN_U_LAST_LOGIN_DATE, user.getLastLogin());
        values.put(User.COLUMN_U_STATUS, user.getStatus());
        values.put(User.COLUMN_U_TAX_OFFICE, user.getTaxOffice());
        values.put(User.COLUMN_U_TAX_TITLE, user.getTaxTitle());
        values.put(User.COLUMN_U_TZ, user.getTz());
        values.put(User.COLUMN_U_SECURITY_QUESTION, user.getSecurityQuestion());
        values.put(User.COLUMN_U_SECURITY_ANSWER, user.getSecurityAnswer());
        values.put(User.COLUMN_U_REFRESH_TOKEN, user.getRefreshToken());
        values.put(User.COLUMN_U_ACCESS_TOKEN, user.getAccessToken());
        values.put(User.COLUMN_IS_LOGGEDIN, user.getIsLoggedIn());

        // updating row
        db.update(User.TABLE_NAME, values, User.COLUMN_IS_LOGGEDIN + " = ?",
                new String[]{String.valueOf(user.getIsLoggedIn())});

    }

    public int getUsersCount() {
        String countQuery = "SELECT  * FROM " + User.TABLE_NAME + " ORDER BY " +
                User.COLUMN_INSERT_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }



}