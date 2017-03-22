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
import com.keithvongola.android.moneydiary.pojo.Account;
import com.keithvongola.android.moneydiary.wheelAdapter.AccountsChildWheelAdapter;
import com.keithvongola.android.moneydiary.wheelAdapter.AccountsParentWheelAdapter;
import com.keithvongola.android.moneydiary.wheelAdapter.AccountsTransferWheelAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;

import static com.keithvongola.android.moneydiary.Utility.setWheelViewStyle;

public class AccountsPopupWindow extends PopupWindow implements OnWheelChangedListener {
    @BindView(R.id.popup_data_title) TextView title;
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_account_submit) Button submit;
    @BindView(R.id.list_wheel_parent) WheelView listWheelParent;
    @BindView(R.id.list_wheel_child) WheelView listWheelChild;

    private ArrayList<Account> accountList;
    private ArrayList<Account> accountParentList;
    private ArrayList<Account> accountChildList;

    private Context mContext;
    private Account currentParent;
    private Account currentChild;
    private boolean isTransfer;

    public AccountsPopupWindow(FragmentActivity context,
                               View.OnClickListener selectItemsOnClick,
                               boolean isTransfer){
        super(context);
        this.mContext = context;
        this.isTransfer = isTransfer;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.accounts_wheel_picker, null);
        ButterKnife.bind(this, mMenuView);

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
        accountParentList = new ArrayList<>();
        int accountType = -1;
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getType() != accountType)
                accountParentList.add(accountList.get(i));
            accountType = accountList.get(i).getType();
        }

        AccountsParentWheelAdapter parentAdapter = new AccountsParentWheelAdapter(mContext, accountParentList);
        parentAdapter.setTextSize(22);
        listWheelParent.setViewAdapter(parentAdapter);
        listWheelParent.setCurrentItem(0);
        if (accountParentList.size() != 0)
            currentParent = accountParentList.get(listWheelParent.getCurrentItem());
    }

    private void initChildList() {
        accountChildList = new ArrayList<>();
        for (int i = 0; i < accountList.size(); i++){
            if (accountList.get(i).getType() == getCurrentParentType())
                accountChildList.add(accountList.get(i));
        }

        AccountsChildWheelAdapter childAdapter = new AccountsChildWheelAdapter(mContext, accountChildList);
        childAdapter.setTextSize(22);
        listWheelChild.setViewAdapter(childAdapter);
        listWheelChild.setCurrentItem(0);
        if (accountChildList.size() != 0)
            currentChild = accountChildList.get(listWheelChild.getCurrentItem());
    }

    private void initAccountsWithGroupList() {
        AccountsTransferWheelAdapter wheelAdapter = new AccountsTransferWheelAdapter(mContext, accountList);
        wheelAdapter.setTextSize(22);
        listWheelParent.setViewAdapter(wheelAdapter);
        listWheelChild.setViewAdapter(wheelAdapter);
        listWheelParent.setCurrentItem(0);
        listWheelChild.setCurrentItem(1);

        if (accountList.size() != 0) {
            currentParent = accountList.get(listWheelParent.getCurrentItem());
            currentChild = accountList.get(listWheelChild.getCurrentItem());
        }
    }

    @Override
    public void onChanged(WheelView wheelView, int i, int i1) {
        if (wheelView == listWheelParent){
            if (isTransfer) {
                currentParent = accountList.get(listWheelParent.getCurrentItem());
            } else {
                currentParent = accountParentList.get(listWheelParent.getCurrentItem());
                initChildList();
            }
        } else if (wheelView == listWheelChild) {
            if (isTransfer)
                currentChild = accountList.get(listWheelChild.getCurrentItem());
            else
                currentChild = accountChildList.get(listWheelChild.getCurrentItem());
        }
    }


    public Integer getCurrentParentType(){
        return currentParent.getType();
    }

    public Integer getCurrentParentID(){
        if (currentParent == null)
            return -1;
        return currentParent.getId();
    }

    public Integer getCurrentChildID(){
        if (currentChild == null)
            return -1;
        return currentChild.getId();
    }

    public String getCurrentChildName(){
        return currentChild.getName();
    }

    public String getCurrentParentName(){
        return currentParent.getName();
    }

    public String getDisplayNameStr(){
        if (accountList == null)
            return mContext.getString(R.string.pw_empty_account);
        else if (accountList.size() == 0)
            return mContext.getString(R.string.pw_empty_account);
        else if(isTransfer)
            return getCurrentParentName() + "(" + mContext.getString(currentParent.getTypeResId()) + ") > "
                    + getCurrentChildName() + "(" +  mContext.getString(currentChild.getTypeResId()) + ")";

        return mContext.getString(currentParent.getTypeResId()) + " : " + getCurrentChildName() + " ("+getCurrentAccountCurrency() + ")";
    }

    public String getCurrentAccountCurrency(){
        return currentChild.getCurrency();
    }

    public int getCurrencyUnit(){
        if(isTransfer)
            return currentParent.getCurrencyUnit();
        return currentChild.getCurrencyUnit();
    }

    public boolean initWheelPosition(int childId){
        for (int i=0; i < accountParentList.size(); i++) {
            listWheelParent.setCurrentItem(i);
            for (int j=0; j < accountChildList.size(); j++) {
                if (accountChildList.get(j).getId() == childId) {
                    listWheelChild.setCurrentItem(j);
                    return true;
                }
            }
        }
        return false;
    }

    public void initWheelPosition(int parentId, int childId) {
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).getId() == parentId)
                listWheelParent.setCurrentItem(i);
            if (accountList.get(i).getId() == childId)
                listWheelChild.setCurrentItem(i);
        }
    }

    public void reloadAccounts(){
        listWheelParent.setCurrentItem(0);
    }

    public void reloadTransferAccounts(){
        listWheelParent.setCurrentItem(0);
        listWheelChild.setCurrentItem(1);
    }

    public void setData(ArrayList<Account> data) {
        this.accountList = data;
        if (isTransfer) {
            //Setup both view with accounts list
            initAccountsWithGroupList();
        } else {
            //Setup parent wheel with accounts groups and child wheel with accounts in group
            initParentList();
            initChildList();
        }

        setWheelViewStyle(listWheelParent);
        setWheelViewStyle(listWheelChild);

        listWheelChild.addChangingListener(this);
        listWheelParent.addChangingListener(this);
    }
}
