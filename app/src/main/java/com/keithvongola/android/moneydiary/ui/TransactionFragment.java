package com.keithvongola.android.moneydiary.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.adapter.TransactionsPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_PAGE;

public class TransactionFragment extends Fragment{
    @BindView(R.id.transaction_viewpager) ViewPager mViewPager;
    @BindView(R.id.transaction_tabs) TabLayout tabLayout;

    private int lastPosition = 0;
    public TransactionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            lastPosition = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_transaction, container, false);
        ButterKnife.bind(this, rootView);

        TransactionsPagerAdapter transactionsPagerAdapter = new TransactionsPagerAdapter(getActivity(), getChildFragmentManager());
        mViewPager.setAdapter(transactionsPagerAdapter);
        mViewPager.setCurrentItem(lastPosition);
        mViewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        return rootView;
    }

    public int getLastPosition() {
        return lastPosition;
    }
}
