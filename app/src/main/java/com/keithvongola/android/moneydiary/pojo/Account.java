package com.keithvongola.android.moneydiary.pojo;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.keithvongola.android.moneydiary.Utility;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

public class Account implements Parcelable {
    private String name, institution, currency;
    private int id, type, currencyUnit;
    private MonetaryAmount current, saving;

    @Override
    public int describeContents() {
        return 0;
    }

    public Account(Cursor data){
        this.id = data.getInt(0);
        this.name = data.getString(1);
        this.type = data.getInt(2);
        this.institution = data.getString(3);
        this.currency = data.getString(4);
        this.currencyUnit = data.getInt(6);
        if(data.getString(7) == null) this.current = Money.of(new BigDecimal("0"), currency);
        else this.current = Money.of(new BigDecimal(data.getString(7)).movePointLeft(currencyUnit), currency);
        if(data.getString(8) == null) this.saving = Money.of(new BigDecimal("0"), currency);
        else this.saving = Money.of(new BigDecimal(data.getString(8)).movePointLeft(currencyUnit), currency);

    }

    public Account(int type){
        this.type = type;
    }

    public Account(Parcel in) {
        name = in.readString();
        institution = in.readString();
        currency = in.readString();
        id = in.readInt();
        type = in.readInt();
        currencyUnit = in.readInt();
        current = Money.of(new BigDecimal(in.readString()), currency);
        saving = Money.of(new BigDecimal(in.readString()), currency);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(institution);
        dest.writeString(currency);
        dest.writeString(current == null ? "0" : current.getNumber().toString());
        dest.writeString(saving == null ? "0" : saving.getNumber().toString());

        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeInt(currencyUnit);
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public void setName(String name){
        this.name = name;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setType(int type){
        this.type = type;
    }

    /**
     * Returns the {@code id} of this {@code Account}
     *
     * @return the {@code id} of this {@code Account}, never {@code null }
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the {@code name} of this {@code Account}.
     *
     * @return the {@code name} of this {@code Account}, never {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Return the {@code type} of  this {@code Account}, from 0 to 6.
     * 0: Cash, 1: Banking, 2: Credit Cards, 3: Stocks/Funds, 4. Debts, 5. Claims, 6: Others
     *
     * @return the type of  this {@code Account}, never {@code null}
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the {@code institution} of this {@code account}.
     * {@code institution} are optional field inputted by the user .
     *
     * @return the {@code institution} of this {@code account},can be {@code null}
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Returns the string resources Id of {@code Account} {@code type}
     *
     * @return the string resources Id of {@code Account} {@code type}, never {@code null}
     */
    public int getTypeResId() {
        return Utility.accountTypeResId(type);
    }

    /**
     * Returns the {@code currency} of this {@code Account}.
     * {@code currency} is in ISO 4217 standard, of  3 units alpha code.
     *
     * @return the currency of this {@code Account}, never {@code null}
     */
    public String getCurrency(){
        return currency;
    }

    /**
     * Returns the {@code currencyUnit} of {@code currency}.
     * Currency unit is used to scale {@code current} and {@code saving} when
     * querying/ inserting momentary amount  from/into the database.
     *
     * @return the current amount of this {@code Account}, never {@code null}
     */
    public int getCurrencyUnit() {
        return currencyUnit;
    }

    /**
     * Returns the current amount of this {@code Account}
     *
     * @return the current amount of this {@code Account}, never {@code null}
     */
    public MonetaryAmount getCurrent() {
        return current;
    }

    /**
     * Returns the saving amount of this {@code Account}
     *
     * @return the saving amount of this {@code Account}, never {@code null}
     */
    public MonetaryAmount getSaving() {
        return saving.negate();
    }

    /**
     * Returns the total balance of this {@code Account}, which is
     * the sum of  {@code current} and {@code saving}.
     *
     * @return the total balance of this {@code Account}, never {@code null}
     */
    public MonetaryAmount getTotal(){
        return saving.negate().add(current);
    }
}
