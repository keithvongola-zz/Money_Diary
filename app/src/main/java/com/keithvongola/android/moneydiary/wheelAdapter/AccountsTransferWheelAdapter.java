package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.pojo.Account;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;


public class AccountsTransferWheelAdapter extends AbstractWheelTextAdapter {
    private ArrayList<Account> accounts;

    public AccountsTransferWheelAdapter(Context context, ArrayList<Account> accounts) {
        super(context, R.layout.wheel_item_account_with_group);
        this.accounts = accounts;
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        TextView accountNameTV = (TextView) view.findViewById(R.id.account_name);
        TextView accountGroupNameTV = (TextView) view.findViewById(R.id.account_group_name);

        accountNameTV.setText(accounts.get(index).getName());
        accountGroupNameTV.setText(context.getString(accounts.get(index).getTypeResId()));

        return view;
    }

    @Override
    public int getItemsCount() {
        return accounts.size();
    }

    @Override
    protected CharSequence getItemText(int i) {
        return null;
    }
}
