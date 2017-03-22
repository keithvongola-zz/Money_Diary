package com.keithvongola.android.moneydiary.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithvongola.android.moneydiary.adapter.PlansPagerAdapter;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Backable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlansFragment extends Fragment implements Backable{
    @BindView(R.id.plans_viewpager) ViewPager mViewPager;
    @BindView(R.id.plans_tabs) TabLayout tabLayout;
    private int lastPosition = 0;

    public PlansFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plans, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_budgets));

        PlansPagerAdapter mPlansPagerAdapter = new PlansPagerAdapter(getChildFragmentManager(), getActivity());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mPlansPagerAdapter);
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
                "android:switcher:" + R.id.plans_viewpager + ":" + lastPosition);
        return ((PlansListFragment) fragment).onBackPressed();
    }

}
