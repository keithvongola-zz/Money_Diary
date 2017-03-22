package com.keithvongola.android.moneydiary.pojo;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.money.MonetaryAmount;

public class Transaction implements Parcelable {
    private String currency, place, notes, photoDir,mainBudgetName, subBudgetName, mainAccountName, subAccountName, planName;
    private int id, type, mainAccount, subAccount, mainBudgetID, subBudgetID, iconResId, currencyUnit;
    private MonetaryAmount amount;
    private Date date;

    @Override
    public int describeContents() {
        return 0;
    }

    public Transaction(int id, int type, String currency, int mainAccount, int subAccount, int mainBudgetID,
                       int subBudgetID, long dateInLong, String amount, int iconResId, String place, String notes, String photoDir,
                       int currencyUnit, String mainBudgetName,String subBudgetName, String mainAccountName, String subAccountName, String planName) {
        this.currency = currency;
        this.place = place;
        this.notes = notes;
        this.photoDir = photoDir;
        this.mainBudgetName = mainBudgetName;
        this.subBudgetName = subBudgetName;
        this.mainAccountName = mainAccountName;
        this.subAccountName = subAccountName;
        this.planName = planName;
        this.id = id;
        this.type = type;
        this.mainAccount = mainAccount;
        this.subAccount = subAccount;
        this.mainBudgetID = mainBudgetID;
        this.subBudgetID = subBudgetID;
        this.currencyUnit = currencyUnit;
        this.iconResId = iconResId;
        this.amount = Money.of(new BigDecimal(amount).movePointLeft(currencyUnit), currency);

        this.date = new Date(dateInLong);
    }

    public Transaction(Cursor cursor){
        this.id = cursor.getInt(0);
        this.type = cursor.getInt(1);
        this.currency = cursor.getString(2);
        this.mainAccount = cursor.getInt(3);
        this.subAccount = cursor.getInt(4);
        this.mainBudgetID = cursor.getInt(5);
        this.subBudgetID = cursor.getInt(6);
        this.date = new Date(cursor.getLong(7));
        this.place = cursor.getString(9);
        this.notes = cursor.getString(10);
        this.photoDir = cursor.getString(11);
        this.iconResId = cursor.getInt(12);
        this.currencyUnit = cursor.getInt(13);
        this.amount = Money.of(new BigDecimal(cursor.getString(8)).movePointLeft(currencyUnit), currency);
        this.mainBudgetName = cursor.getString(14);
        this.subBudgetName = cursor.getString(15);
        this.mainAccountName = cursor.getString(16);
        this.subAccountName = cursor.getString(17);
        this.planName = cursor.getString(18);
    }

    public Transaction(Parcel in) {
        currency = in.readString();
        place = in.readString();
        notes = in.readString();
        photoDir = in.readString();
        mainBudgetName = in.readString();
        subBudgetName = in.readString();
        mainAccountName = in.readString();
        subAccountName = in.readString();
        planName = in.readString();
        amount = Money.of(new BigDecimal(in.readString()), currency);

        id = in.readInt();
        type = in.readInt();
        mainAccount = in.readInt();
        subAccount = in.readInt();
        mainBudgetID = in.readInt();
        subBudgetID = in.readInt();
        currencyUnit = in.readInt();
        iconResId = in.readInt();

        date = (Date) in.readSerializable();
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currency);
        dest.writeString(place);
        dest.writeString(notes);
        dest.writeString(photoDir);
        dest.writeString(mainBudgetName);
        dest.writeString(subBudgetName);
        dest.writeString(mainAccountName);
        dest.writeString(subAccountName);
        dest.writeString(planName);

        dest.writeString(amount == null ? "0" : amount.getNumber().toString());

        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(mainAccount);
        dest.writeInt(subAccount);
        dest.writeInt(mainBudgetID);
        dest.writeInt(subBudgetID);
        dest.writeInt(currencyUnit);
        dest.writeInt(iconResId);

        dest.writeSerializable(date);
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoDir() {
        return photoDir;
    }

    public void setPhotoDir(String photoDir) {
        this.photoDir = photoDir;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(int mainAccount) {
        this.mainAccount = mainAccount;
    }

    public int getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(int subAccount) {
        this.subAccount = subAccount;
    }

    public int getMainBudgetID() {
        return mainBudgetID;
    }

    public void setMainBudgetID(int mainBudgetID) {
        this.mainBudgetID = mainBudgetID;
    }

    public int getSubBudgetID() {
        return subBudgetID;
    }

    public void setSubBudgetID(int subBudgetID) {
        this.subBudgetID = subBudgetID;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public void setAmount(MonetaryAmount amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMonth(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }


    public int getDayOfWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfMonth(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getYear(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
    public String getSubBudgetName() {
        return subBudgetName;
    }

    public void setSubBudgetName(String subBudgetName) {
        this.subBudgetName = subBudgetName;
    }

    public String getMainBudgetName() {
        return mainBudgetName;
    }

    public String getMainAccountName() {
        return mainAccountName;
    }

    public String getSubAccountName() {
        return subAccountName;
    }

    public int getCurrencyUnit() {
        return currencyUnit;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getPlanName() {
        return planName;
    }
}
