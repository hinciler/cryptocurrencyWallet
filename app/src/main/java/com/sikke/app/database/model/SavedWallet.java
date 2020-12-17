package com.sikke.app.database.model;

public class SavedWallet {

    public static final String TABLE_NAME = "saved_wallet";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALLET_NUMBER = "w_number";
    public static final String COLUMN_INSERT_DATE = "w_insert_date";

    private int id;
    private String w_number;
    private String w_insert_date;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_WALLET_NUMBER + " TEXT,"
                    + COLUMN_INSERT_DATE + " DATETIME DEFAULT CURRENT_DATE"
                    + ")";

    public SavedWallet(){

    }

    public SavedWallet(int id, String w_number, String w_insert_date) {
        this.id = id;
        this.w_number = w_number;
        this.w_insert_date = w_insert_date;
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

    public String getInsertDate() {
        return w_insert_date;
    }

    public void setInsertDate(String w_insert_date) {
        this.w_insert_date = w_insert_date;
    }


}
