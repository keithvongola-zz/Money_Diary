package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;


public class DayWheelAdapter extends AbstractWheelTextAdapter {
    private ArrayList<String> dates;

    public DayWheelAdapter(Context context, ArrayList<String> dates) {
        super(context);
        this.dates = dates;
    }

    @Override
    public int getItemsCount() {
        return dates.size();
    }

    @Override
    protected CharSequence getItemText(int i) {
        return dates.get(i);
    }

}
