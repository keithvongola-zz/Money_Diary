package com.keithvongola.android.moneydiary.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithvongola.android.moneydiary.adapter.BudgetsPagerAdapter;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Backable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BudgetsFragment extends Fragment implements Backable{
    @BindView(R.id.budgets_viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    private int lastPosition = 0;

    public BudgetsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budgets, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_budgets));

        BudgetsPagerAdapter mBudgetsPagerAdapter = new BudgetsPagerAdapter(getChildFragmentManager(), getActivity());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mBudgetsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return rootView;
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(
                "android:switcher:" + R.id.budgets_viewpager + ":" + lastPosition);
        return ((BudgetsListFragment) fragment).onBackPressed();
    }

}
