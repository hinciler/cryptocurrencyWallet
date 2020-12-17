package com.sikke.app.items;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionItem implements Parcelable {

    public String Date;
    public String Hour;
    public String From;
    public String To;
    public String Amount;
    public String Description;
    public String TxGroup;
    public String TxNumber;
    public String ActionTime;
    public String CompleteTime;
    public String Hash;
    public String PrevHash;



    public TransactionItem(){

    }

    protected TransactionItem(Parcel in) {
        Date = in.readString();
        Hour = in.readString();
        From = in.readString();
        To = in.readString();
        Amount = in.readString();
        Description = in.readString();
        TxGroup = in.readString();
        TxNumber = in.readString();
        ActionTime = in.readString();
        CompleteTime = in.readString();
        Hash = in.readString();
        PrevHash = in.readString();
    }

    public static final Creator<TransactionItem> CREATOR = new Creator<TransactionItem>() {
        @Override
        public TransactionItem createFromParcel(Parcel in) {
            return new TransactionItem(in);
        }

        @Override
        public TransactionItem[] newArray(int size) {
            return new TransactionItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(Date);
        dest.writeString(Hour);
        dest.writeString(From);
        dest.writeString(To);
        dest.writeString(Amount);
        dest.writeString(Description);
        dest.writeString(TxGroup);
        dest.writeString(TxNumber);
        dest.writeString(ActionTime);
        dest.writeString(CompleteTime);
        dest.writeString(Hash);
        dest.writeString(PrevHash);

    }
}
