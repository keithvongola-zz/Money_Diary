package com.keithvongola.android.moneydiary.wheelAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.pojo.Budget;

import java.util.ArrayList;

import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;


public class BudgetsChildWheelAdapter extends AbstractWheelTextAdapter {
    private ArrayList<Budget> budgets;

    public BudgetsChildWheelAdapter(Context context, ArrayList<Budget> budgets) {
        super(context, R.layout.wheel_item_account);
        this.budgets = budgets;
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        TextView budgetTextView = (TextView) view.findViewById(R.id.child_item);

        budgetTextView.setText(budgets.get(index).getChildName());

        return view;
    }

    @Override
    public int getItemsCount() {
        return budgets.size();
    }

    @Override
    protected CharSequence getItemText(int i) {
        return null;
    }
}
