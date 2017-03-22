package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.ui.PlansListFragment;

import static com.keithvongola.android.moneydiary.Utility.ARG_URI;

public class PlansPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public PlansPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        PlansListFragment fragment = new PlansListFragment();
        Bundle arg = new Bundle();
        switch (position){
            case 0: // plans in progress
                arg.putParcelable(ARG_URI, PlansEntry.buildPlansUriWithStatus(0));
                fragment.setArguments(arg);
                break;
            case 1: // plans completed
                arg.putParcelable(ARG_URI, PlansEntry.buildPlansUriWithStatus(1));
                fragment.setArguments(arg);
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.plans_pager_title_in_progress);
            case 1:
                return mContext.getString(R.string.plans_pager_title_completed);
        }
        return null;
    }
}

