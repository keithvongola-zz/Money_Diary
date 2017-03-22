package com.keithvongola.android.moneydiary.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.pojo.Plan;
import com.keithvongola.android.moneydiary.wheelAdapter.PlansWheelAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;


public class PlansPopupWindow extends PopupWindow implements OnWheelChangedListener {
    @BindView(R.id.popup_data_title) TextView title;
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_plans_submit) Button submit;
    @BindView(R.id.list_wheel) WheelView listWheel;

    private ArrayList<Plan> plans;

    private Context mContext;
    private int currentPlan;

    public PlansPopupWindow(FragmentActivity context,
                            View.OnClickListener selectItemsOnClick){
        super(context);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.plans_wheel_picker, null);
        ButterKnife.bind(this, mMenuView);

        Utility.setWheelViewStyle(listWheel);
        listWheel.addChangingListener(this);

        submit.setOnClickListener(selectItemsOnClick);
        cancel.setOnClickListener(selectItemsOnClick);

        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorGrey100)));
        this.setOutsideTouchable(false);
        this.setFocusable(false);
    }


    private void initParentList() {
        PlansWheelAdapter plansWheelAdapter = new PlansWheelAdapter(mContext,plans);
        listWheel.setViewAdapter(plansWheelAdapter);
        listWheel.setCurrentItem(0);
    }

    @Override
    public void onChanged(WheelView wheelView, int i, int i1) {
            currentPlan = wheelView.getCurrentItem();
    }

    public String getCurrentPlanName(){
        if (plans == null)
            return mContext.getString(R.string.pw_empty_account);
        else if (plans.size() == 0)
            return mContext.getString(R.string.pw_empty_account);
        return plans.get(currentPlan).getName();
    }

    public int getCurrentPlanId(){
        return plans.get(currentPlan).getId();
    }

    public void initWheelPosition(int planId) {
        for (int i = 0; i < plans.size(); i++){
            if (planId==plans.get(i).getId()){
                listWheel.setCurrentItem(i);
                break;
            }
        }
    }

    public void reloadPlans() {
        listWheel.setCurrentItem(0);
    }

    public void setData(ArrayList<Plan> plans){
        this.plans = plans;
        initParentList();
    }
}
