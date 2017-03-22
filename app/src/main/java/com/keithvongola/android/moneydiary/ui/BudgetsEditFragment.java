package com.keithvongola.android.moneydiary.ui;

import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.adapter.BudgetIconAdapter;
import com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import com.keithvongola.android.moneydiary.pojo.Budget;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_BUDGET;
import static com.keithvongola.android.moneydiary.Utility.ARG_BUDGET_PARENT_ID;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_EXPENSE;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_BUDGET;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.hideKeyboard;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sMainBudgetsIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetsIdSelection;

public class BudgetsEditFragment extends Fragment implements CalculatorPopupWindow.OnPopupWindowInteractionListener {
    @BindView(R.id.ll_budget_edit) FrameLayout budgetEditContainer;
    @BindView(R.id.budget_name_container) LinearLayout nameContainer;
    @BindView(R.id.budget_balance_container) LinearLayout balanceContainer;
    @BindView(R.id.budget_grid_layout) RecyclerView iconGridLayout;
    @BindView(R.id.et_budget_name) EditText nameView;
    @BindView(R.id.et_budget_balance) EditText balanceView;

    private CalculatorPopupWindow calculatorPopupWindow;
    private boolean isNewBudget, isMainBudget, isExpense;
    private String parentID;
    private Budget budget;

    public BudgetsEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            budget = getArguments().getParcelable(ARG_BUDGET);
            if(budget == null)
                isNewBudget = true;
            isExpense = getArguments().getBoolean(ARG_IS_EXPENSE);
            isMainBudget = getArguments().getBoolean(ARG_IS_MAIN_BUDGET);
            parentID = getArguments().getString(ARG_BUDGET_PARENT_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_done){
            String nameStr = nameView.getText().toString();
            String currencyStr = getMainCurrency(getActivity());
            BigDecimal balance = new BigDecimal(0);

            if (nameStr.length() <= 0)
                Toast.makeText(getActivity(), R.string.toast_empty_budget_name, Toast.LENGTH_LONG).show();
            else if(!isMainBudget && balance.signum() < 0)
                Toast.makeText(getActivity(), R.string.toast_budget_amount_zero_or_negative, Toast.LENGTH_LONG).show();
            else if(((BudgetIconAdapter) iconGridLayout.getAdapter()).getLastCheckedResId() == -1)
                Toast.makeText(getActivity(), R.string.toast_empty_icon, Toast.LENGTH_LONG).show();
            else {
                ContentValues cv = new ContentValues();

                if(isMainBudget) {
                    cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_NAME, nameStr);
                    cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_CURRENCY, currencyStr);
                    int valIsExpense = isExpense ? 1 : 0 ;
                    cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_EXPENSE, valIsExpense);
                    cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_ICON,
                            ((BudgetIconAdapter)iconGridLayout.getAdapter()).getLastCheckedResId());
                    cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE,1);
                    if(isNewBudget) {
                        getContext().getContentResolver().insert(
                                MainBudgetsEntry.CONTENT_URI,
                                cv);
                    } else {
                        getContext().getContentResolver().update(
                                MainBudgetsEntry.CONTENT_URI,
                                cv,
                                sMainBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getParentID())});
                    }
                } else {
                    try {
                        balance = new BigDecimal(balanceView.getText().toString().replace(",",""));
                    } catch (Exception e){
                        Toast.makeText(getActivity(), R.string.toast_budget_amount_invalid, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_PARENT_ID,parentID);
                    cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_NAME, nameStr);
                    cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY, currencyStr);
                    cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_ICON,
                            ((BudgetIconAdapter)iconGridLayout.getAdapter()).getLastCheckedResId());
                    cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_IS_ACTIVE,1);
                    if(isNewBudget) {
                        cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_AMOUNT,
                                bigDecimalToDbVal(getActivity(), balance, currencyStr));
                        getContext().getContentResolver().insert(
                                SubBudgetsEntry.CONTENT_URI,
                                cv);
                    } else {
                        cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_AMOUNT,
                                bigDecimalToDbVal(balance, budget.getCurrencyUnit()));
                        getContext().getContentResolver().update(
                                SubBudgetsEntry.CONTENT_URI,
                                cv,
                                sSubBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getChildID())});
                    }
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideKeyboard(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budget_edit, container, false);
        ButterKnife.bind(this, rootView);

        if (isNewBudget) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_new_budget);

            nameView.setHint(R.string.account_enter_name);
            if (!isMainBudget) {
                balanceContainer.setVisibility(View.VISIBLE);
                balanceView.setText(R.string.zero);
            }
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_budget);
                nameView.setText(budget.getParentName());

            if (!isMainBudget) {
                nameView.setText(budget.getChildName());
                balanceContainer.setVisibility(View.VISIBLE);
                balanceView.setText(getAmountStr(budget.getAmount()));
            }
        }

        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(), onClickListener);
        calculatorPopupWindow.setOnPopupWindowClickListener(this);
        calculatorPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                budgetEditContainer.clearFocus();
            }
        });

        nameView.setOnFocusChangeListener(onFocusChangeListener);
        balanceView.setOnFocusChangeListener(onFocusChangeListener);

        balanceView.setInputType(InputType.TYPE_NULL);

        BudgetIconAdapter budgetIconAdapter = new BudgetIconAdapter(getActivity());
        if (!isNewBudget)
            budgetIconAdapter.setLastCheckedPos(budget.getIconResId());
        RecyclerView.LayoutManager lLayout = new GridLayoutManager(getActivity(), 5);
        iconGridLayout.setLayoutManager(lLayout);
        iconGridLayout.setAdapter(budgetIconAdapter);

        return rootView;
    }

    @Override
    public void setEditAmountText(String str) {
        balanceView.setText(str);
    }

    @Override
    public String getEditAmountText() {
        return balanceView.getText().toString();
    }

    public void setDevOperationInputView(String str) {
    }

    public String getDevOperationInputView() {
        return null;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.popup_calculator_submit:
                case R.id.popup_cancel:
                    budgetEditContainer.clearFocus();
                    break;
            }
        }
    };

    //Show popup window when view has focus,
    //else hide popup window
    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Drawable backgroundFocus = ContextCompat.getDrawable(getActivity(), R.color.colorAmber100);
            Drawable backgroundWithoutFocus = ContextCompat.getDrawable(getActivity(), R.drawable.list_item_border);
            switch (v.getId()) {
                case (R.id.et_budget_name):
                    if (hasFocus) {
                        nameContainer.setBackground(backgroundFocus);
                    } else {
                        nameContainer.setBackground(backgroundWithoutFocus);
                        hideKeyboard(getActivity(),v);
                    }
                    break;
                case (R.id.et_budget_balance):
                    if (hasFocus) {
                        balanceContainer.setBackground(backgroundFocus);
                        calculatorPopupWindow.showAtLocation(budgetEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        balanceContainer.setBackground(backgroundWithoutFocus);
                        balanceView.invalidate();
                        calculatorPopupWindow.dismiss();
                    }
                    break;
            }
        }
    };

    public boolean popupWindowIsShowing(){
        if (calculatorPopupWindow.isShowing()) {
            calculatorPopupWindow.dismiss();
            return true;
        }
        return false;
    }
}
