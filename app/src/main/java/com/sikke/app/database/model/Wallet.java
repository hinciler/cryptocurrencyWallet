package com.sikke.app.database.model;

public class Wallet {

    public static final String TABLE_NAME = "wallet";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALLET_NUMBER = "w_number";
    public static final String COLUMN_WALLET_ALIAS_NAME = "w_alias_name";
    public static final String COLUMN_WALLET_BALANCE = "w_balance";
    public static final String COLUMN_WALLET_TYPE = "w_type";
    public static final String COLUMN_WALLET_STATUS = "w_status";
    public static final String COLUMN_WALLET_LIMIT_HOURLY= "w_limit_hourly";
    public static final String COLUMN_WALLET_LIMIT_DAILY= "w_limit_daily";
    public static final String COLUMN_WALLET_LIMIT_MAX_AMOUNT= "w_limit_max_amount";
    public static final String COLUMN_WALLET_INSERT_DATE= "w_insert_date";
    public static final String COLUMN_W_PRIVATE_KEY = "w_private_key";
    public static final String COLUMN_W_PUBLIC_KEY = "w_public_key";
    public static final String COLUMN_WALLET_NOTIFICATION = "w_notification";
    public static final String COLUMN_WALLET_CALLBACK = "w_callback_url";

    private int id;
    private String w_number;
    private String w_private_key;
    private String w_public_key;
    private String w_alias_name;
    private String w_balance;
    private String w_type;
    private String w_status;
    private String w_limit_hourly;
    private String w_limit_daily;
    private String w_limit_max_amount;
    private String w_insert_date;
    private String w_notification;
    private String w_callback_url;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_WALLET_NUMBER + " TEXT,"
                    + COLUMN_WALLET_ALIAS_NAME + " TEXT,"
                    + COLUMN_WALLET_BALANCE + " TEXT,"
                    + COLUMN_WALLET_TYPE + " TEXT,"
                    + COLUMN_WALLET_STATUS + " TEXT,"
                    + COLUMN_WALLET_LIMIT_HOURLY + " TEXT,"
                    + COLUMN_WALLET_LIMIT_DAILY + " TEXT,"
                    + COLUMN_WALLET_LIMIT_MAX_AMOUNT + " TEXT,"
                    + COLUMN_WALLET_INSERT_DATE + " TEXT,"
                    + COLUMN_W_PRIVATE_KEY + " TEXT,"
                    + COLUMN_W_PUBLIC_KEY + " TEXT,"
                    + COLUMN_WALLET_NOTIFICATION + " TEXT,"
                    + COLUMN_WALLET_CALLBACK + " TEXT"
                    + ")";

    public Wallet(){

    }

    public Wallet(int id, String w_number,  String w_alias_name, String w_balance, String w_type, String w_status, String w_limit_hourly, String w_limit_daily, String w_limit_max_amount, String w_private_key,String w_public_key, String w_insert_date, String w_notification, String w_callback_url) {
        this.id = id;
        this.w_number = w_number;
        this.w_private_key = w_private_key;
        this.w_public_key = w_public_key;
        this.w_alias_name = w_alias_name;
        this.w_balance = w_balance;
        this.w_type = w_type;
        this.w_status = w_status;
        this.w_limit_hourly = w_limit_hourly;
        this.w_limit_daily = w_limit_daily;
        this.w_limit_max_amount = w_limit_max_amount;
        this.w_insert_date = w_insert_date;
        this.w_notification = w_notification;
        this.w_callback_url = w_callback_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWalletNumber() {
        return w_number;
    }

    public void setWalletNumber(String w_number) {
        this.w_number = w_number;
    }

    public String getPrivateKey() {
        return w_private_key;
    }

    public void setPrivateKey(String w_private_key) {
        this.w_private_key = w_private_key;
    }

    public String getPublicKey() {
        return w_public_key;
    }

    public void setPublicKey(String w_public_key) {
        this.w_public_key = w_public_key;
    }

    public String getWalletAliasName() {
        return w_alias_name;
    }

    public void setWalletAliasName(String w_alias_name) {
        this.w_alias_name = w_alias_name;
    }

    public String getWalletBalance() {
        return w_balance;
    }

    public void setWalletBalance(String w_balance) {
        this.w_balance = w_balance;
    }

    public String getWalletType() {
        return w_type;
    }

    public void setWalletType(String w_type) {
        this.w_type = w_type;
    }

    public String getWalletStatus() {
        return w_status;
    }

    public void setWalletStatus(String w_status) {
        this.w_status = w_status;
    }

    public void setWalletNotification(String w_notification) {
        this.w_notification = w_notification;
    }

    public String getWalletNotification() {
        return w_notification;
    }
    public void setWalletCallback(String w_callback_url) {
        this.w_callback_url = w_callback_url;
    }

    public String getWalletCallback() {
        return w_callback_url;
    }



    public String getWalletLimitHourly() {
        return w_limit_hourly;
    }

    public void setLimitHourly(String w_limit_hourly) { this.w_limit_hourly = w_limit_hourly; }

    public String getWalletLimitDaily() {
        return w_limit_daily;
    }

    public void setLimitDaily(String w_limit_daily) {
        this.w_limit_daily = w_limit_daily;
    }

    public String getWalletLimitMaxAmount() {
        return w_limit_max_amount;
    }

    public void setLimitMaxAmount(String w_limit_max_amount){
        this.w_limit_max_amount = w_limit_max_amount;
    }

    public String getInsertDate() {
        return w_insert_date;
    }

    public void setInsertDate(String w_insert_date) {
        this.w_insert_date = w_insert_date;
    }






}
