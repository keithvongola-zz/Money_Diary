package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;

import com.keithvongola.android.moneydiary.R;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

import static com.keithvongola.android.moneydiary.Utility.accountTypeResId;


public class AccountsTypeWheelAdapter extends AbstractWheelTextAdapter {
    private static final Integer[] accountTypeList = new Integer[]{0,1,2,3,4,5,6};

    public AccountsTypeWheelAdapter(Context context) {
        super(context, R.layout.wheel_item_account);
    }

    @Override
    public int getItemsCount() {
        return accountTypeList.length;
    }

    @Override
    protected CharSequence getItemText(int i) {
        return context.getString(accountTypeResId(accountTypeList[i]));
    }
}
