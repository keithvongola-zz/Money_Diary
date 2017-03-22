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
import com.keithvongola.android.moneydiary.pojo.Budget;
import com.keithvongola.android.moneydiary.wheelAdapter.BudgetsChildWheelAdapter;
import com.keithvongola.android.moneydiary.wheelAdapter.BudgetsParentWheelAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;


public class BudgetsPopupWindow extends PopupWindow implements OnWheelChangedListener {
    @BindView(R.id.popup_data_title) TextView title;
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_category_submit) Button submit;
    @BindView(R.id.list_wheel_parent) WheelView listWheelParent;
    @BindView(R.id.list_wheel_child) WheelView listWheelChild;

    private Context mContext;

    private ArrayList<Budget> budgetsList;
    private ArrayList<Budget> budgetsParentList;
    private ArrayList<Budget> budgetsChildList;

    private Budget currentParent;
    private Budget currentChild;

    public BudgetsPopupWindow(FragmentActivity context,
                              View.OnClickListener selectItemsOnClick){
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.category_wheel_picker, null);
        ButterKnife.bind(this, mMenuView);

        Utility.setWheelViewStyle(listWheelParent);
        Utility.setWheelViewStyle(listWheelChild);

        listWheelChild.addChangingListener(this);
        listWheelParent.addChangingListener(this);

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
        BudgetsParentWheelAdapter parentAdapter = new BudgetsParentWheelAdapter(mContext, budgetsParentList);
        parentAdapter.setTextSize(22);
        listWheelParent.setViewAdapter(parentAdapter);
        listWheelParent.setCurrentItem(0);
        if (budgetsParentList.size() != 0)
            currentParent = budgetsParentList.get(listWheelParent.getCurrentItem());
    }

    private void initChildList() {
        budgetsChildList = new ArrayList<>();
        for (int i = 0; i < budgetsList.size(); i++) {
            if(budgetsList.get(i).getParentID() == currentParent.getParentID())
                budgetsChildList.add(budgetsList.get(i));
        }

        BudgetsChildWheelAdapter childAdapter = new BudgetsChildWheelAdapter(mContext, budgetsChildList);
        childAdapter.setTextSize(22);
        listWheelChild.setViewAdapter(childAdapter);
        if (budgetsChildList.size() > 0) {
            listWheelChild.setCurrentItem(0);
            currentChild = budgetsChildList.get(listWheelChild.getCurrentItem());
        } else {
            currentChild = null;
        }
    }


    @Override
    public void onChanged(WheelView wheelView, int i, int i1) {
        if (wheelView == listWheelParent) {
            currentParent = budgetsParentList.get(listWheelParent.getCurrentItem());
            initChildList();
        } else if (wheelView == listWheelChild) {
            currentChild = budgetsChildList.get(listWheelChild.getCurrentItem());
        }
    }

    public int getCurrentParentID() {
        if (currentParent != null)
            return currentParent.getParentID();
        return -1;
    }

    public int getCurrentChildID(){
        if (currentChild != null)
            return currentChild.getChildID();
        return -1;
    }

    public String getDisplayNameStr(){
        if (currentParent == null)
            return mContext.getString(R.string.pw_empty_budget);
        else if (currentChild == null)
            return currentParent.getParentName() + " > ";
        return currentChild.getParentName() + " > " + currentChild.getChildName();
    }

    public boolean initWheelPosition(int parentId, int childId){
        for (int i=0; i < budgetsParentList.size(); i++) {
            if (budgetsParentList.get(i).getParentID() == parentId)
                listWheelParent.setCurrentItem(i);
            for (int j = 0; j < budgetsChildList.size(); j++) {
                if (budgetsChildList.get(j).getChildID() == childId){
                    listWheelChild.setCurrentItem(j);
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadWheel() {
        listWheelParent.setCurrentItem(0);
    }

    public void setData(ArrayList<Budget> budgetsParentList, ArrayList<Budget> budgetsList) {
        this.budgetsParentList = budgetsParentList;
        this.budgetsList = budgetsList;
        initParentList();
        initChildList();
    }
}
