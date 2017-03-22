package com.keithvongola.android.moneydiary.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

public class SubPlan implements Parcelable{
    private String name, currency;
    private int id, parentId, currencyUnit, iconResId;
    private MonetaryAmount amountTarget;

    @Override
    public int describeContents() {
        return 0;
    }

    public SubPlan(int id, int parentId, String name, String currency, int currencyUnit, String amountTarget, int iconResId){
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.currency = currency;
        this.currencyUnit = currencyUnit;
        amountTarget = amountTarget == null? "0": amountTarget;
        this.amountTarget = Money.of(new BigDecimal(amountTarget).movePointLeft(currencyUnit), currency);
        this.iconResId = iconResId;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(currency);
        dest.writeString(amountTarget.getNumber().toString());

        dest.writeInt(id);
        dest.writeInt(parentId);
        dest.writeInt(iconResId);
        dest.writeInt(currencyUnit);
    }

    protected SubPlan(Parcel in) {
        name = in.readString();
        currency = in.readString();
        amountTarget = Money.of(new BigDecimal(in.readString()), currency);

        id = in.readInt();
        parentId = in.readInt();
        iconResId = in.readInt();
        currencyUnit = in.readInt();
    }

    public static final Creator<SubPlan> CREATOR = new Creator<SubPlan>() {
        @Override
        public SubPlan createFromParcel(Parcel in) {
            return new SubPlan(in);
        }

        @Override
        public SubPlan[] newArray(int size) {
            return new SubPlan[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public int getCurrencyUnit() {
        return currencyUnit;
    }

    public MonetaryAmount getAmountTarget() {
        return amountTarget;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }
}
