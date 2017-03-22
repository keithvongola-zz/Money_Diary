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

import com.keithvongola.android.moneydiary.wheelAdapter.AccountsTypeWheelAdapter;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;


public class AccountTypePopupWindow extends PopupWindow implements OnWheelChangedListener {
    @BindView(R.id.popup_data_title) TextView title;
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_account_type_submit) Button submit;
    @BindView(R.id.list_wheel) WheelView listWheel;

    private Context mContext;
    private int currentAccountType;

    public AccountTypePopupWindow(FragmentActivity context,
                                  View.OnClickListener selectItemsOnClick){
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.accounts_type_wheel_picker, null);
        ButterKnife.bind(this, mMenuView);

        initParentList();
        Utility.setWheelViewStyle(listWheel);
        listWheel.addChangingListener(this);

        submit.setOnClickListener(selectItemsOnClick);
        cancel.setOnClickListener(selectItemsOnClick);

        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorGrey100));
        this.setBackgroundDrawable(dw);
        this.setOutsideTouchable(false);
        this.setFocusable(false);
    }


    private void initParentList() {
        AccountsTypeWheelAdapter accountsTypeWheelAdapter = new AccountsTypeWheelAdapter(mContext);

        listWheel.setViewAdapter(accountsTypeWheelAdapter);
        listWheel.setCurrentItem(0);
    }

    @Override
    public void onChanged(WheelView wheelView, int i, int i1) {
            currentAccountType = wheelView.getCurrentItem();
    }

    public Integer getAccountType(){
        return currentAccountType;
    }
}
