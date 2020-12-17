package com.sikke.app.database.model;

public class User {

    public static final String TABLE_NAME = "user";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_U_EMAIL = "u_email";
    public static final String COLUMN_U_NAME = "u_name";
    public static final String COLUMN_U_SURNAME = "u_surname";
    public static final String COLUMN_U_ALIAS = "u_alias";
    public static final String COLUMN_U_INDENTITY_NUMBER = "u_identity_number";
    public static final String COLUMN_U_PHONE = "u_phone";
    public static final String COLUMN_U_PHONE_COUNTRY = "u_phone_country";
    public static final String COLUMN_U_COUNTRY = "u_country";
    public static final String COLUMN_U_CITY = "u_city";
    public static final String COLUMN_U_DISTRICT = "u_district";
    public static final String COLUMN_U_LAST_LOGIN_DATE = "u_last_login_time";
    public static final String COLUMN_U_STATUS = "u_status";
    public static final String COLUMN_U_TAX_OFFICE = "u_tax_office";
    public static final String COLUMN_U_TAX_TITLE = "u_tax_title";
    public static final String COLUMN_U_TZ = "u_tz";
    public static final String COLUMN_U_PASSWORD = "u_password";
    public static final String COLUMN_PIN_CODE = "u_pin_code";
    public static final String COLUMN_IV_CODE = "u_iv_code";
    public static final String COLUMN_U_SECURITY_QUESTION = "u_security_question";
    public static final String COLUMN_U_SECURITY_ANSWER = "u_securtiy_answer";
    public static final String COLUMN_IS_LOGGEDIN = "u_is_loggedin";
    public static final String COLUMN_U_ACCESS_TOKEN = "u_access_token";
    public static final String COLUMN_U_REFRESH_TOKEN = "u_refresh_token";
    public static final String COLUMN_U_LANGUAGE = "u_language";
    public static final String COLUMN_INSERT_DATE = "w_insert_date";


    private int id;
    private String u_email;
    private String u_name;
    private String u_surname;
    private String u_alias;
    private String u_identity_number;
    private String u_phone;
    private String u_phone_country;
    private String u_country;
    private String u_city;
    private String u_district;
    private String u_last_login_time;
    private String u_status;
    private String u_tax_office;
    private String u_tax_title;
    private String u_tz;
    private String u_password;
    private String u_pin_code;
    private String u_iv_code;
    private String u_security_question;
    private String u_securtiy_answer;
    private int u_is_loggedin;
    private String u_access_token;
    private String u_refresh_token;
    private String u_language;
    private String u_insert_date;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_U_EMAIL + " TEXT,"
                    + COLUMN_U_NAME + " TEXT,"
                    + COLUMN_U_SURNAME + " TEXT,"
                    + COLUMN_U_ALIAS + " TEXT,"
                    + COLUMN_U_INDENTITY_NUMBER + " TEXT,"
                    + COLUMN_U_PHONE + " TEXT,"
                    + COLUMN_U_PHONE_COUNTRY + " TEXT,"
                    + COLUMN_U_COUNTRY + " TEXT,"
                    + COLUMN_U_CITY + " TEXT,"
                    + COLUMN_U_DISTRICT + " TEXT,"
                    + COLUMN_U_LAST_LOGIN_DATE + " TEXT,"
                    + COLUMN_U_STATUS + " TEXT,"
                    + COLUMN_U_TAX_OFFICE + " TEXT,"
                    + COLUMN_U_TAX_TITLE + " TEXT,"
                    + COLUMN_U_TZ + " TEXT,"
                    + COLUMN_U_PASSWORD + " TEXT,"
                    + COLUMN_PIN_CODE + " TEXT,"
                    + COLUMN_IV_CODE + " TEXT,"
                    + COLUMN_U_SECURITY_QUESTION + " TEXT,"
                    + COLUMN_U_SECURITY_ANSWER + " TEXT,"
                    + COLUMN_IS_LOGGEDIN + " INTEGER,"
                    + COLUMN_U_ACCESS_TOKEN + " TEXT,"
                    + COLUMN_U_REFRESH_TOKEN + " TEXT,"
                    + COLUMN_U_LANGUAGE + " TEXT,"
                    + COLUMN_INSERT_DATE + " DATETIME DEFAULT CURRENT_DATE"
                    + ")";


    public User(){

    }

    public User(int id, String u_email, String u_name, String u_surname, String u_alias, String u_identity_number, String u_phone, String u_phone_country,
                String u_country, String u_city,String u_district,String u_last_login_time,String u_status,String u_tax_office, String u_tax_title,
                String u_tz, String u_password, String u_pin_code, String u_iv_code, String u_security_question, String u_securtiy_answer,
                int u_is_loggedin, String u_access_token, String u_refresh_token,String u_language, String u_insert_date){

        this.id = id;
        this.u_email = u_email;
        this.u_name = u_name;
        this.u_surname = u_surname;
        this.u_alias = u_alias;
        this.u_identity_number = u_identity_number;
        this.u_phone = u_phone;
        this.u_phone_country = u_phone_country;
        this.u_country = u_country;
        this.u_city = u_city;
        this.u_district = u_district;
        this.u_last_login_time = u_last_login_time;
        this.u_status = u_status;
        this.u_tax_office = u_tax_office;
        this.u_tax_title = u_tax_title;
        this.u_tz = u_tz;
        this.u_password = u_password;
        this.u_pin_code = u_pin_code;
        this.u_iv_code = u_iv_code;
        this.u_security_question = u_security_question;
        this.u_securtiy_answer = u_securtiy_answer;
        this.u_is_loggedin = u_is_loggedin;
        this.u_access_token = u_access_token;
        this.u_refresh_token = u_refresh_token;
        this.u_language = u_language;
        this.u_insert_date = u_insert_date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return u_email;
    }

    public void setEmail(String u_email) {
        this.u_email = u_email;
    }

    public String getName() {
        return u_name;
    }

    public void setName(String u_name) {
        this.u_name = u_name;
    }

    public String getSurname() {
        return u_surname;
    }

    public void setSurname(String u_surname) {
        this.u_surname = u_surname;
    }

    public String getAlias() {
        return u_alias;
    }

    public void setAlias(String u_alias) {
        this.u_alias = u_alias;
    }

    public String getIdentityNumber() {
        return u_identity_number;
    }

    public void setIdentityNumber(String u_identity_number) {
        this.u_identity_number = u_identity_number;
    }

    public String getPhone() {
        return u_phone;
    }

    public void setPhone(String u_phone) {
        this.u_phone = u_phone;
    }

    public String getPhoneCountry() {
        return u_phone_country;
    }

    public void setPhoneCountry(String u_phone_country) {
        this.u_phone_country = u_phone_country;
    }

    public String getCountry() {
        return u_country;
    }

    public void setCountry(String u_country) {
        this.u_country = u_country;
    }

    public String getCity() {
        return u_city;
    }

    public void setCity(String u_city) {
        this.u_city = u_city;
    }

    public String getDistrict() {
        return u_district;
    }

    public void setDistrict(String u_district) {
        this.u_district = u_district;
    }

    public String getLastLogin() {
        return u_last_login_time;
    }

    public void setLastLogin(String u_last_login_time) {
        this.u_last_login_time = u_last_login_time;
    }

    public String getStatus() {
        return u_status;
    }

    public void setStatus(String u_status) {
        this.u_status = u_status;
    }

    public String getTaxOffice() {
        return u_tax_office;
    }

    public void setTaxOffice(String u_tax_office) {
        this.u_tax_office = u_tax_office;
    }

    public String getTaxTitle() {
        return u_tax_title;
    }

    public void setTaxTitle(String u_tax_title) {
        this.u_tax_title = u_tax_title;
    }

    public String getTz() {
        return u_tz;
    }

    public void setTz(String u_tz) {
        this.u_tz = u_tz;
    }

    public String getPassword() {
        return u_password;
    }

    public void setPassword(String u_password) {
        this.u_password = u_password;
    }

    public String getPinCode() {
        return u_pin_code;
    }

    public void setPinCode(String u_pin_code) {
        this.u_pin_code = u_pin_code;
    }

    public String getIvCode() {
        return u_iv_code;
    }

    public void setIvCode(String u_iv_code) {
        this.u_iv_code = u_iv_code;
    }

    public String getSecurityQuestion() {
        return u_security_question;
    }

    public void setSecurityQuestion(String u_security_question) {
        this.u_security_question = u_security_question;
    }
    public String getSecurityAnswer() {
        return u_securtiy_answer;
    }

    public void setSecurityAnswer(String u_securtiy_answer) {
        this.u_securtiy_answer = u_securtiy_answer;
    }

    public int getIsLoggedIn() {
        return u_is_loggedin;
    }

    public void setIsLoggedIn(int u_is_loggedin) {
        this.u_is_loggedin = u_is_loggedin;
    }

    public String getAccessToken() {
        return u_access_token;
    }

    public void setAccessToken(String u_access_token) {
        this.u_access_token = u_access_token;
    }

    public String getRefreshToken() {
        return u_refresh_token;
    }

    public void setRefreshToken(String u_refresh_token) {
        this.u_refresh_token = u_refresh_token;
    }

    public String getLanguage() {
        return u_language;
    }

    public void setLanguage(String u_language) {
        this.u_language = u_language;
    }

}
