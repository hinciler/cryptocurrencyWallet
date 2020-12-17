package com.sikke.app.items;

import android.os.Parcel;
import android.os.Parcelable;

public class WalletItem implements Parcelable {

    public String Unique;
    public String PageUrl = null;
    public String Url = null;
    public String WalletNumber;
    public String Balance;
    public String WalletId;
    public String AliasName;
    public String Currency;
    public String PublicKey;
    public String PvtKey;


    public WalletItem() {

    }

    protected WalletItem(Parcel in) {
        Unique = in.readString();
        PageUrl = in.readString();
        Url = in.readString();
        WalletNumber = in.readString();
        Balance = in.readString();
        WalletId= in.readString();
        AliasName= in.readString();
        Currency = in.readString();
        PublicKey = in.readString();
        PvtKey = in.readString();

    }

    public static final Creator<WalletItem> CREATOR = new Creator<WalletItem>() {
        @Override
        public WalletItem createFromParcel(Parcel in) {
            return new WalletItem(in);
        }

        @Override
        public WalletItem[] newArray(int size) {
            return new WalletItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Unique);
        dest.writeString(PageUrl);
        dest.writeString(Url);
        dest.writeString(WalletNumber);
        dest.writeString(Balance);
        dest.writeString(WalletId);
        dest.writeString(AliasName);
        dest.writeString(Currency);
        dest.writeString(PublicKey);
        dest.writeString(PvtKey);
    }
}
