package com.keithvongola.android.moneydiary.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract;
import com.keithvongola.android.moneydiary.pojo.Account;
import com.keithvongola.android.moneydiary.pojo.Budget;
import com.keithvongola.android.moneydiary.pojo.Transaction;
import com.keithvongola.android.moneydiary.popupwindow.AccountsPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.BudgetsPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.DatePopupWindow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_TRANSACTION;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.formatDateStringAsLong;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.Utility.getCalendarFromFormattedLong;
import static com.keithvongola.android.moneydiary.Utility.hideKeyboard;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.buildSubBudgetsUriWithType;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionIdSelection;

public class IncomeEditFragment extends Fragment implements CalculatorPopupWindow.OnPopupWindowInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.item_income) LinearLayout incomeEditView;
    @BindView(R.id.et_amount) EditText amountView;
    @BindView(R.id.et_account) EditText accountView;
    @BindView(R.id.et_category) EditText categoryView;
    @BindView(R.id.developing_operation_inputText) TextView devOperationInputView;
    @BindView(R.id.et_date) EditText dateView;
    @BindView(R.id.et_notes) EditText notesView;
    @BindView(R.id.transaction_amount_container) LinearLayout amountContainer;
    @BindView(R.id.til_et_category) TextInputLayout categoryContainer;
    @BindView(R.id.til_et_account) TextInputLayout accountContainer;
    @BindView(R.id.til_et_date) TextInputLayout dateContainer;
    @BindView(R.id.til_et_notes) TextInputLayout notesContainer;

    @BindView(R.id.btn_discard) Button btnDiscard;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.btn_save_and_new) Button btnSaveAndNew;
    @BindView(R.id.btn_delete) Button btnDelete;

    private static final int ACCOUNTS_LOADER = 10;
    private static final int BUDGETS_LOADER = 11;
    private boolean isIncomeNew;
    private Transaction transactionIncome;
    private DatePopupWindow datePopupWindow;
    private CalculatorPopupWindow calculatorPopupWindow;
    private AccountsPopupWindow accountsPopupWindow;
    private BudgetsPopupWindow budgetsPopupWindow;

    public IncomeEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isIncomeNew = true;
        if (getArguments() != null) {
            transactionIncome = getArguments().getParcelable(ARG_TRANSACTION);
            isIncomeNew = false;
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_income);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_income, container, false);
        ButterKnife.bind(this,rootView);

        accountsPopupWindow = new AccountsPopupWindow(getActivity(), selectItemsOnClick, false);
        budgetsPopupWindow = new BudgetsPopupWindow(getActivity(),selectItemsOnClick);
        datePopupWindow = new DatePopupWindow(getActivity(), selectItemsOnClick);
        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(),selectItemsOnClick);
        calculatorPopupWindow.setOnPopupWindowClickListener(this);

        //Clear all focus once PopupWindow is dismissed
        PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                clearFocus();
            }
        };
        calculatorPopupWindow.setOnDismissListener(onDismissListener);
        accountsPopupWindow.setOnDismissListener(onDismissListener);
        budgetsPopupWindow.setOnDismissListener(onDismissListener);
        datePopupWindow.setOnDismissListener(onDismissListener);

        //Display or Hide popup-window when focus change
        amountView.setOnFocusChangeListener(onFocusChangeListener);
        accountView.setOnFocusChangeListener(onFocusChangeListener);
        categoryView.setOnFocusChangeListener(onFocusChangeListener);
        dateView.setOnFocusChangeListener(onFocusChangeListener);
        notesView.setOnFocusChangeListener(onFocusChangeListener);

        amountView.setInputType(InputType.TYPE_NULL);
        accountView.setInputType(InputType.TYPE_NULL);
        categoryView.setInputType(InputType.TYPE_NULL);
        dateView.setInputType(InputType.TYPE_NULL);

        if (!isIncomeNew) {
            btnSaveAndNew.setVisibility(View.GONE);
            amountView.setText(getAmountStr(transactionIncome.getAmount()));
            dateView.setText(getCalendarFromFormattedLong(transactionIncome.getDate().getTime()));
            datePopupWindow.setDateWheel(transactionIncome.getDate().getTime());
            notesView.setText(transactionIncome.getNotes());
        } else {
            btnDelete.setVisibility(View.GONE);
            initValues();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(ACCOUNTS_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(BUDGETS_LOADER, null, this);
    }

    @Override
    public void setEditAmountText(String str) {
            amountView.setText(str);
    }

    @Override
    public String getEditAmountText() {
        return amountView.getText().toString().replace(",","");
    }

    public void setDevOperationInputView(String str) {
        devOperationInputView.setText(str);
    }

    public String getDevOperationInputView() {
        return devOperationInputView.getText().toString();
    }

    private View.OnClickListener selectItemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.popup_calculator_submit:
                    clearFocus();
                    break;
                case R.id.popup_date_submit:
                    dateView.setText(datePopupWindow.getCurrentDay()
                            +"-"+ datePopupWindow.getCurrentMonth()
                            +"-"+ datePopupWindow.getCurrentYear());
                    clearFocus();
                    break;
                case R.id.popup_account_submit:
                    accountView.setText(accountsPopupWindow.getDisplayNameStr());
                    clearFocus();
                    break;
                case R.id.popup_category_submit:
                    categoryView.setText(budgetsPopupWindow.getDisplayNameStr());
                    clearFocus();
                    break;
                case R.id.popup_cancel: {
                    clearFocus();
                    break;
                }
            }
        }};


    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Drawable backgroundFocus = ContextCompat.getDrawable(getActivity(), R.color.colorAmber100);
            Drawable backgroundWithoutFocus = ContextCompat.getDrawable(getActivity(), R.drawable.list_item_border);
            switch (v.getId()) {
                case (R.id.et_amount):
                    if (hasFocus) {
                        amountContainer.setBackground(backgroundFocus);
                        calculatorPopupWindow.showAtLocation(incomeEditView, Gravity.BOTTOM, 0, 0);
                        devOperationInputView.setVisibility(View.VISIBLE);
                    } else {
                        devOperationInputView.setVisibility(View.GONE);
                        amountContainer.invalidate();
                        calculatorPopupWindow.dismiss();
                        amountContainer.setBackground(backgroundWithoutFocus);
                    }
                    break;
                case (R.id.et_account):
                    if (hasFocus) {
                        if (accountsPopupWindow.getCurrentParentID() == -1) {
                            Toast.makeText(getActivity(), R.string.pw_empty_account, Toast.LENGTH_SHORT).show();
                            clearFocus();
                        } else {
                            accountContainer.setBackground(backgroundFocus);
                            accountsPopupWindow.showAtLocation(incomeEditView, Gravity.BOTTOM, 0, 0);
                        }
                    }else {
                        accountContainer.setBackground(backgroundWithoutFocus);
                        accountsPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_category):
                    if (hasFocus) {
                        if (budgetsPopupWindow.getCurrentParentID() == -1) {
                            Toast.makeText(getActivity(), R.string.toast_empty_budget_name, Toast.LENGTH_SHORT).show();
                            clearFocus();
                        } else {
                            categoryContainer.setBackground(backgroundFocus);
                            budgetsPopupWindow.showAtLocation(incomeEditView, Gravity.BOTTOM, 0, 0);
                        }
                    } else {
                        categoryContainer.setBackground(backgroundWithoutFocus);
                        budgetsPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_date):
                    if (hasFocus) {
                        dateContainer.setBackground(backgroundFocus);
                        datePopupWindow.showAtLocation(incomeEditView, Gravity.BOTTOM, 0, 0);
                    } else {
                        dateContainer.setBackground(backgroundWithoutFocus);
                        datePopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_notes):
                    if (hasFocus) {
                        notesContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                    } else {
                        notesContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        hideKeyboard(getActivity(),v);
                    }
                    break;
            }
        }
    };

    public void clearFocus() {
        incomeEditView.clearFocus();
    }

    @OnClick(R.id.btn_discard)
    public void discard(){
        if (isIncomeNew)
            getActivity().finish();
        else
            getFragmentManager().popBackStack();
    }

    @OnClick(R.id.btn_save)
    public void actionSaveTransaction() {
        if(saveTransaction()){
            if(isIncomeNew)
                getActivity().finish();
            else
                getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @OnClick(R.id.btn_save_and_new)
    public void actionSaveAndNew(){
        if(saveTransaction()){
            accountsPopupWindow.reloadAccounts();
            budgetsPopupWindow.reloadWheel();
            datePopupWindow.reloadDate();
            initValues();
        }
    }

    @OnClick(R.id.btn_delete)
    public void deleteTransaction() {
        getActivity().getContentResolver().delete(TransactionEntry.CONTENT_URI,
                sTransactionIdSelection,
                new String[]{String.valueOf(transactionIncome.getId())});
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public boolean popupWindowIsShowing(){
        if(calculatorPopupWindow.isShowing()){
            calculatorPopupWindow.dismiss();
            return true;
        }
        if(accountsPopupWindow.isShowing()){
            accountsPopupWindow.dismiss();
            return true;
        }
        if(budgetsPopupWindow.isShowing()){
            budgetsPopupWindow.dismiss();
            return true;
        }
        if(datePopupWindow.isShowing()){
            datePopupWindow.dismiss();
            return true;
        }
        return false;
    }

    private ContentValues getIncomeContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_TYPE, "1");
        BigDecimal incomeAmount = new BigDecimal(getEditAmountText());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                bigDecimalToDbVal(incomeAmount,accountsPopupWindow.getCurrencyUnit()));
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_CURRENCY,
                accountsPopupWindow.getCurrentAccountCurrency());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT,
                accountsPopupWindow.getCurrentChildID());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT,
                budgetsPopupWindow.getCurrentParentID());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_SUB_CAT,
                budgetsPopupWindow.getCurrentChildID());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_DATE,
                formatDateStringAsLong(String.valueOf(dateView.getText())));
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_NOTES,
                String.valueOf(notesView.getText()));
        return contentValues;
    }

    private void initValues(){
        amountView.setText("0");
        dateView.setText(getCalendarFromFormattedLong(Calendar.getInstance().getTime().getTime()));
        notesView.setText("");
    }

    private boolean saveTransaction() {
        try {
            BigDecimal expenseAmount = new BigDecimal(getEditAmountText());
            if (!(expenseAmount.signum()>0)) {
                Toast.makeText(getActivity(), R.string.toast_transaction_amount_zero, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e){
            Toast.makeText(getActivity(), R.string.toast_transaction_amount_invalid , Toast.LENGTH_SHORT).show();
            return false;
        }

        if (accountsPopupWindow.getCurrentChildID() == -1) {
            Toast.makeText(getActivity(), R.string.toast_transaction_empty_account, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (budgetsPopupWindow.getCurrentChildID() == -1) {
            Toast.makeText(getActivity(), R.string.toast_transaction_empty_category, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isIncomeNew) {
            //Insert transaction records to database
            getActivity().getContentResolver().insert(TransactionEntry.CONTENT_URI,
                    getIncomeContentValues());
        } else {
            //Update transaction records in database
            getActivity().getContentResolver().update(TransactionEntry.CONTENT_URI,
                    getIncomeContentValues(),
                    sTransactionIdSelection,
                    new String[]{String.valueOf(transactionIncome.getId())});
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ACCOUNTS_LOADER:
                return new CursorLoader(getActivity(),
                        MoneyContract.AccountsEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            case BUDGETS_LOADER:
                return new CursorLoader(getActivity(),
                        buildSubBudgetsUriWithType(0),
                        null,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ACCOUNTS_LOADER:
                ArrayList<Account> accountList = new ArrayList<>();
                if (data != null & data.moveToFirst()) {
                    do {
                        Account account = new Account(data);
                        accountList.add(account);
                    } while (data.moveToNext());
                }
                accountsPopupWindow.setData(accountList);

                if (!isIncomeNew) {
                    accountsPopupWindow.initWheelPosition(transactionIncome.getMainAccount());
                    accountView.setText(accountsPopupWindow.getDisplayNameStr());
                } else {
                    accountView.setText(accountsPopupWindow.getDisplayNameStr());
                }
                break;
            case BUDGETS_LOADER:
                ArrayList<Budget> budgetsParentList = new ArrayList<>();
                ArrayList<Budget> budgetsList = new ArrayList<>();
                int lastParentId = 0;
                if (data != null & data.moveToFirst()) {
                    do {
                        Budget budget = new Budget(data.getInt(0),
                                data.getInt(2),
                                data.getString(1),
                                data.getString(3),
                                data.getString(4));
                        budgetsList.add(budget);
                        if (data.getInt(0) != lastParentId) {
                            budgetsParentList.add(budget);
                            lastParentId = data.getInt(0);
                        }
                    } while (data.moveToNext());
                }
                budgetsPopupWindow.setData(budgetsParentList, budgetsList);

                if (!isIncomeNew)
                    budgetsPopupWindow.initWheelPosition(transactionIncome.getMainBudgetID(), transactionIncome.getSubBudgetID());
                categoryView.setText(budgetsPopupWindow.getDisplayNameStr());
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
