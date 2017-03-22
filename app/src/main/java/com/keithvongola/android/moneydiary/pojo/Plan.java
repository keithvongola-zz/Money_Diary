package com.keithvongola.android.moneydiary.pojo;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.money.MonetaryAmount;

import static com.keithvongola.android.moneydiary.Utility.getCurrentDay;
import static com.keithvongola.android.moneydiary.Utility.getCurrentMonth;
import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;

public class Plan implements Parcelable{
    private String name, currency;
    private int id, currencyUnit, iconResId, terms;
    private MonetaryAmount amountActual, amountTarget, amountCurrentMonth;
    private Date dateStart;

    @Override
    public int describeContents() {
        return 0;
    }

    public Plan(int id, String name, long dateInLong, int terms, String currency, int currencyUnit, String amountActual, String amountTarget, String amountCurrentMonth, int iconResId){
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.currencyUnit = currencyUnit;
        amountActual = amountActual == null? "0": amountActual;
        amountTarget = amountTarget == null? "0": amountTarget;
        amountCurrentMonth = amountCurrentMonth == null? "0": amountCurrentMonth;
        this.amountActual = Money.of(new BigDecimal(amountActual).movePointLeft(currencyUnit).negate(), currency);
        this.amountTarget = Money.of(new BigDecimal(amountTarget).movePointLeft(currencyUnit), currency);
        this.amountCurrentMonth = Money.of(new BigDecimal(amountCurrentMonth).movePointLeft(currencyUnit).negate(), currency);
        this.iconResId = iconResId;
        this.dateStart = new Date(dateInLong);
        this.terms = terms;
    }

    public Plan(int id, String name, long dateInLong, int terms, String currency, int iconResId){
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.iconResId = iconResId;
        this.dateStart = new Date(dateInLong);
        this.terms = terms;
    }

    public Plan(Cursor data){
        this.id = data.getInt(0);
        this.name = data.getString(2);
        this.currency = data.getString(5);
        this.currencyUnit = data.getInt(10);
        String amountActual = data.getString(8) == null ? "0" : data.getString(8);
        this.amountActual = Money.of(new BigDecimal(amountActual).movePointLeft(currencyUnit).negate(), currency);
        String amountTarget = data.getString(7) == null ? "0" : data.getString(7);
        this.amountTarget = Money.of(new BigDecimal(amountTarget).movePointLeft(currencyUnit), currency);
        String amountCurrentMonth = data.getString(9) == null ? "0" : data.getString(9);
        this.amountCurrentMonth = Money.of(new BigDecimal(amountCurrentMonth).movePointLeft(currencyUnit).negate(), currency);
        this.iconResId = data.getInt(6);
        this.dateStart = new Date(data.getLong(3));
        this.terms = data.getInt(4);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(currency);
        dest.writeString(amountActual.toString());
        dest.writeString(amountTarget.toString());
        dest.writeString(amountCurrentMonth.toString());

        dest.writeInt(id);
        dest.writeInt(iconResId);
        dest.writeInt(currencyUnit);
        dest.writeInt(terms);

        dest.writeSerializable(dateStart);
    }

    protected Plan(Parcel in) {
        name = in.readString();
        currency = in.readString();

        id = in.readInt();
        terms = in.readInt();
        iconResId = in.readInt();
        currencyUnit = in.readInt();

        dateStart = (Date) in.readSerializable();
        amountActual = Money.of(new BigDecimal(in.readString()), currency);
        amountTarget = Money.of(new BigDecimal(in.readString()), currency);
        amountCurrentMonth = Money.of(new BigDecimal(in.readString()), currency);
    }

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    public String getName() {
        return name;
    }

    public MonetaryAmount getAmountActual(){
        return amountActual;
    }

    public MonetaryAmount getAmountDiff(){
        return amountTarget.subtract(amountActual);
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

    public int getTerms() {
        return terms;
    }

    public MonetaryAmount getAmountCurrentMonth() {
        return amountCurrentMonth;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public MonetaryAmount getMonthlyContribution(){
        if (amountTarget.subtract(amountActual).signum() > 0) {
            BigDecimal termsDiff = new BigDecimal(terms - getCurrentTerms() + 1);
            if (termsDiff.signum()>0){
                return amountTarget.divide(terms);

            } else if (termsDiff.signum()<0){
                return amountTarget.subtract(amountActual);
            }
        }
        return Money.of(new BigDecimal("0"), currency);
    }

    public int getCurrentTerms(){
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(dateStart);
        int diffMonth = getCurrentMonth() - calendarStart.get(Calendar.MONTH);
        int diffYear = getCurrentYear() - calendarStart.get(Calendar.YEAR);
        int currentTerms = diffMonth + diffYear * 12;
        if ((getCurrentDay() - calendarStart.get(Calendar.DAY_OF_MONTH))<0)
            currentTerms--;

        if ( currentTerms < 1) {
            return 1;
        } else if (currentTerms > terms) {
            return terms;
        }
        return currentTerms;
    }

    public boolean isCompleted(){
        if ((getAmountDiff().signum() > 0 | amountTarget.signum()==0)){
            return false;
        } else if (getAmountDiff().signum() <=0 | amountTarget.signum()!=0){
            return true;
        }
        return  false;
    }
}
