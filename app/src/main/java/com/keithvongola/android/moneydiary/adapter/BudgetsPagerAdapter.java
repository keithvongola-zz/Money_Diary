package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.ui.BudgetsListFragment;

import static com.keithvongola.android.moneydiary.Utility.ARG_IS_EXPENSE;

public class BudgetsPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public BudgetsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        BudgetsListFragment budgetsFragment = new BudgetsListFragment();
        Bundle arg = new Bundle();
        switch (position){
            case 0: // budgets expense
                arg.putBoolean(ARG_IS_EXPENSE, true);
                budgetsFragment.setArguments(arg);
                break;
            case 1: // budgets income
                arg.putBoolean(ARG_IS_EXPENSE, false);
                budgetsFragment.setArguments(arg);
                break;
        }
        return budgetsFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.monthly_expense);
            case 1:
                return mContext.getString(R.string.monthly_income);
        }
        return null;
    }

}

