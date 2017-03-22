package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.pojo.Account;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

public class AccountsChildWheelAdapter extends AbstractWheelTextAdapter {
    private ArrayList<Account> accounts;

    public AccountsChildWheelAdapter(Context mContext, ArrayList<Account> accounts) {
        super(mContext, R.layout.wheel_item_account);
        this.accounts = accounts;
    }


    /**
     * Returns {@code View} of wheel list item at position {@param i} with {@code Account} {@code name}
     *
     * @param i
     * @param cachedView
     * @param parent
     * @return
     */
    @Override
    public View getItem(int i, View cachedView, ViewGroup parent) {
        View view = super.getItem(i, cachedView, parent);
        TextView accountTextView = (TextView) view.findViewById(R.id.child_item);
        accountTextView.setText(accounts.get(i).getName());
        return view;
    }

    /**
     * @return  number of list items in {@code WheelView}
     */
    @Override
    public int getItemsCount() {
        return accounts.size();
    }

    @Override
    protected CharSequence getItemText(int i) {
        return null;
    }
}
