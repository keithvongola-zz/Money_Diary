package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.pojo.Plan;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;


public class PlansWheelAdapter extends AbstractWheelTextAdapter {
    private ArrayList<Plan> plans;

    public PlansWheelAdapter(Context context, ArrayList<Plan> plans) {
        super(context, R.layout.wheel_item_account);
        this.plans = plans;
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        TextView accountTextView = (TextView) view.findViewById(R.id.child_item);
        accountTextView.setText(plans.get(index).getName());

        return view;
    }

    @Override
    public int getItemsCount() {
        return plans.size();
    }

    @Override
    protected CharSequence getItemText(int i) {
        return null;
    }
}
