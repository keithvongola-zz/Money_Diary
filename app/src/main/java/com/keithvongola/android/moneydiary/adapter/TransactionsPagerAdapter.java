package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.ui.ExpenseEditFragment;
import com.keithvongola.android.moneydiary.ui.IncomeEditFragment;
import com.keithvongola.android.moneydiary.ui.SavingEditFragment;
import com.keithvongola.android.moneydiary.ui.TransferEditFragment;


public class TransactionsPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public TransactionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        Class fragmentClass;
        switch (position){
            case 0 :
                fragmentClass = ExpenseEditFragment.class;
                break;
            case 1:
                fragmentClass = IncomeEditFragment.class;
                break;
            case 2 :
                fragmentClass = TransferEditFragment.class;
                break;
            case 3 :
                fragmentClass = SavingEditFragment.class;
                break;
            default:
                return null;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.expense);
            case 1:
                return mContext.getString(R.string.tab_income);
            case 2:
                return mContext.getString(R.string.tab_transfer);
            case 3:
                return mContext.getString(R.string.tab_saving);
        }
        return null;
    }
}
