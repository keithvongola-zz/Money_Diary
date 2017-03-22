package com.keithvongola.android.moneydiary.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

public class Budget implements Parcelable{
    private String parentName, childName, currency;
    private int parentID, childID, isExpense, currencyUnit, iconResId;
    private MonetaryAmount amount, amountUsed;

    @Override
    public int describeContents() {
        return 0;
    }

    public Budget(int parentID, String parentName, String currency, int iconResId, int currencyUnit, String amount, String amountUsed, int isExpense){
        this.parentID = parentID;
        this.parentName = parentName;
        this.currency = currency;
        this.currencyUnit = currencyUnit;
        amount = amount == null? "0": amount;
        amountUsed = amountUsed == null? "0": amountUsed;
        this.amount = Money.of(new BigDecimal(amount).movePointLeft(currencyUnit), currency);
        this.amountUsed = Money.of(new BigDecimal(amountUsed).movePointLeft(currencyUnit), currency);
        this.isExpense = isExpense;
        this.iconResId = iconResId;
    }

    public Budget(int parentID, int childID, String parentName, String childName, String currency, int iconResId, int currencyUnit, String amount, String amountUsed, int isExpense){
        this.parentID = parentID;
        this.parentName = parentName;
        this.childID = childID;
        this.childName = childName;
        this.iconResId = iconResId;
        this.currency = currency;
        this.currencyUnit = currencyUnit;
        amount = amount == null? "0": amount;
        amountUsed = amountUsed == null? "0": amountUsed;
        this.amount = Money.of(new BigDecimal(amount).movePointLeft(currencyUnit), currency);
        this.amountUsed = Money.of(new BigDecimal(amountUsed).movePointLeft(currencyUnit), currency);
        this.isExpense = isExpense;
    }

    public Budget(int parentID, int childID, String parentName, String childName, String currency){
        this.parentID = parentID;
        this.parentName = parentName;
        this.childID = childID;
        this.childName = childName;
        this.currency = currency;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parentName);
        dest.writeString(childName);
        dest.writeString(currency);
        dest.writeString(amount.getNumber().toString());
        dest.writeString(amountUsed.getNumber().toString());

        dest.writeInt(parentID);
        dest.writeInt(childID);
        dest.writeInt(iconResId);
        dest.writeInt(currencyUnit);
        dest.writeInt(isExpense);
    }

    protected Budget(Parcel in) {
        parentName = in.readString();
        childName = in.readString();
        currency = in.readString();
        amount = Money.of(new BigDecimal(in.readString()), currency);
        amountUsed = Money.of(new BigDecimal(in.readString()), currency);
        parentID = in.readInt();
        childID = in.readInt();
        iconResId = in.readInt();
        currencyUnit = in.readInt();
        isExpense = in.readInt();
    }

    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        @Override
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        @Override
        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };

    public int getParentID() {
        return parentID;
    }

    public int getChildID() {
        return childID;
    }

    public String getParentName() {
        return parentName;
    }

    public String getChildName() {
        return childName;
    }

    public MonetaryAmount getAmount(){
        return amount;
    }

    public int getIsExpense() {
        return isExpense;
    }

    public String getCurrency() {
        return currency;
    }

    public int getCurrencyUnit() {
        return currencyUnit;
    }

    public MonetaryAmount getAmountUsed() {
        return amountUsed;
    }

    public int getIconResId() {
        return iconResId;
    }
}
