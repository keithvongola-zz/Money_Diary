package com.keithvongola.android.moneydiary.ui;

import android.content.ContentValues;
import android.database.Cursor;
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
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract;
import com.keithvongola.android.moneydiary.pojo.Account;
import com.keithvongola.android.moneydiary.pojo.Plan;
import com.keithvongola.android.moneydiary.pojo.Transaction;
import com.keithvongola.android.moneydiary.popupwindow.AccountsPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.DatePopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.PlansPopupWindow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_TRANSACTION;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionIdSelection;

public class SavingEditFragment extends Fragment implements CalculatorPopupWindow.OnPopupWindowInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.item_saving) LinearLayout savingEditView;
    @BindView(R.id.et_amount) EditText amountView;
    @BindView(R.id.et_account) EditText accountView;
    @BindView(R.id.developing_operation_inputText) TextView devOperationInputView;
    @BindView(R.id.et_date) EditText dateView;
    @BindView(R.id.et_plans) EditText plansView;
    @BindView(R.id.et_notes) EditText notesView;
    @BindView(R.id.transaction_amount_container) LinearLayout amountContainer;
    @BindView(R.id.til_et_account) TextInputLayout accountContainer;
    @BindView(R.id.til_et_plans) TextInputLayout plansContainer;
    @BindView(R.id.til_et_date) TextInputLayout dateContainer;
    @BindView(R.id.til_et_notes) TextInputLayout notesContainer;

    @BindView(R.id.btn_discard) Button btnDiscard;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.btn_save_and_new) Button btnSaveAndNew;
    @BindView(R.id.btn_delete) Button btnDelete;

    private static final int ACCOUNTS_LOADER = 30;
    private static final int PLANS_LOADER = 31;

    private boolean isSavingNew;
    private Transaction transactionSaving;
    private DatePopupWindow datePopupWindow;
    private CalculatorPopupWindow calculatorPopupWindow;
    private AccountsPopupWindow accountsPopupWindow;
    private PlansPopupWindow plansPopupWindow;

    public SavingEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSavingNew = true;
        if (getArguments() != null) {
            transactionSaving = getArguments().getParcelable(ARG_TRANSACTION);
            isSavingNew = false;
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_saving);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_saving, container, false);
        ButterKnife.bind(this,rootView);

        datePopupWindow = new DatePopupWindow(getActivity(), selectItemsOnClick);
        accountsPopupWindow = new AccountsPopupWindow(getActivity(), selectItemsOnClick, false);
        plansPopupWindow = new PlansPopupWindow(getActivity(),selectItemsOnClick);
        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(),selectItemsOnClick);
        calculatorPopupWindow.setOnPopupWindowClickListener(this);

        // Clear focus once PopupWindow is dismissed
        PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                clearFocus();
            }
        };
        calculatorPopupWindow.setOnDismissListener(onDismissListener);
        accountsPopupWindow.setOnDismissListener(onDismissListener);
        datePopupWindow.setOnDismissListener(onDismissListener);
        plansPopupWindow.setOnDismissListener(onDismissListener);

        // Display or Hide popup-window when focus change
        amountView.setOnFocusChangeListener(onFocusChangeListener);
        accountView.setOnFocusChangeListener(onFocusChangeListener);
        dateView.setOnFocusChangeListener(onFocusChangeListener);
        notesView.setOnFocusChangeListener(onFocusChangeListener);
        plansView.setOnFocusChangeListener(onFocusChangeListener);

        amountView.setInputType(InputType.TYPE_NULL);
        accountView.setInputType(InputType.TYPE_NULL);
        dateView.setInputType(InputType.TYPE_NULL);
        plansView.setInputType(InputType.TYPE_NULL);

        if (!isSavingNew){
            btnSaveAndNew.setVisibility(View.GONE);
            amountView.setText(getAmountStr(transactionSaving.getAmount()).replace("-",""));
            dateView.setText(Utility.getCalendarFromFormattedLong(transactionSaving.getDate().getTime()));
            datePopupWindow.setDateWheel(transactionSaving.getDate().getTime());
            notesView.setText(transactionSaving.getNotes());
        } else {
            btnDelete.setVisibility(View.GONE);
            amountView.setText("0");
            dateView.setText(Utility.getCalendarFromFormattedLong(Calendar.getInstance().getTime().getTime()));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(ACCOUNTS_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(PLANS_LOADER, null, this);
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
                case R.id.popup_plans_submit:
                    plansView.setText(plansPopupWindow.getCurrentPlanName());
                    clearFocus();
                    break;
                case R.id.popup_cancel:
                    clearFocus();
                    break;
            }
        }};


    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case (R.id.et_amount):
                    if (hasFocus) {
                        amountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        calculatorPopupWindow.showAtLocation(savingEditView, Gravity.BOTTOM, 0, 0);
                        devOperationInputView.setVisibility(View.VISIBLE);
                    } else {
                        devOperationInputView.setVisibility(View.GONE);
                        amountContainer.invalidate();
                        calculatorPopupWindow.dismiss();
                        amountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                    }
                    break;
                case (R.id.et_account):
                    if (hasFocus) {
                        accountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        accountsPopupWindow.showAtLocation(savingEditView, Gravity.BOTTOM, 0, 0);
                    }else {
                        accountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        accountsPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_plans):
                    if (hasFocus) {
                        plansContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        plansPopupWindow.showAtLocation(savingEditView, Gravity.BOTTOM, 0, 0);
                    } else {
                        plansContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        plansPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_date):
                    if (hasFocus) {
                        dateContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        datePopupWindow.showAtLocation(savingEditView, Gravity.BOTTOM, 0, 0);
                    } else {
                        dateContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        datePopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_notes):
                    if (hasFocus) {
                        notesContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                    } else {
                        notesContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        Utility.hideKeyboard(getActivity(),v);
                    }
                    break;
            }
        }
    };

    public void clearFocus() {
        savingEditView.clearFocus();
    }

    @OnClick(R.id.btn_discard)
    public void discard(){
        if (isSavingNew)
            getActivity().finish();
        else
            getFragmentManager().popBackStack();
    }

    @OnClick(R.id.btn_save)
    public void actionSaveTransaction() {
        if(saveTransaction()) {
            if (isSavingNew)
                getActivity().finish();
            else
                getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @OnClick(R.id.btn_save_and_new)
    public void actionSaveAndNew(){
        if(saveTransaction()){
            accountsPopupWindow.reloadAccounts();
            datePopupWindow.reloadDate();
            plansPopupWindow.reloadPlans();
            amountView.setText("0");
            dateView.setText(Utility.getCalendarFromFormattedLong(Calendar.getInstance().getTime().getTime()));        }
    }

    @OnClick(R.id.btn_delete)
    public void deleteTransaction() {
        getActivity().getContentResolver().delete(TransactionEntry.CONTENT_URI,
                sTransactionIdSelection,
                new String[]{String.valueOf(transactionSaving.getId())});

        getActivity().getSupportFragmentManager().popBackStack();
    }

    public boolean popupWindowIsShowing(){
        if (calculatorPopupWindow.isShowing()) {
            calculatorPopupWindow.dismiss();
            return true;
        } else if (accountsPopupWindow.isShowing()) {
            accountsPopupWindow.dismiss();
            return true;
        } else if (datePopupWindow.isShowing()) {
            datePopupWindow.dismiss();
            return true;
        } else if (plansPopupWindow.isShowing()) {
            plansPopupWindow.dismiss();
            return true;
        }
        return false;
    }

    private ContentValues getSavingContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_TYPE, "3");
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT,
                accountsPopupWindow.getCurrentChildID());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_CURRENCY,
                accountsPopupWindow.getCurrentAccountCurrency());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT, 2);
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_SUB_CAT,
                plansPopupWindow.getCurrentPlanId());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_DATE,
                Utility.formatDateStringAsLong(String.valueOf(dateView.getText())));
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_NOTES,
                String.valueOf(notesView.getText()));

        BigDecimal savingAmount = new BigDecimal(getEditAmountText());
        contentValues.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                bigDecimalToDbVal(savingAmount.negate(),accountsPopupWindow.getCurrencyUnit()));

          return contentValues;
    }

    private boolean saveTransaction() {
        try {
            BigDecimal transferAmount = new BigDecimal(getEditAmountText());
            if (!(transferAmount.signum() > 0)) {
                Toast.makeText(getActivity(), R.string.toast_transaction_amount_zero, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.toast_transaction_amount_invalid, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (accountsPopupWindow.getCurrentChildID() == -1) {
            Toast.makeText(getActivity(), R.string.toast_transaction_empty_account, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isSavingNew) {
            //Insert transaction records to database
            getActivity().getContentResolver().insert(TransactionEntry.CONTENT_URI,
                    getSavingContentValues());
        } else {
            //Update transaction records in database
            getActivity().getContentResolver().update(TransactionEntry.CONTENT_URI,
                    getSavingContentValues(),
                    sTransactionIdSelection,
                    new String[]{String.valueOf(transactionSaving.getId())});
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case (ACCOUNTS_LOADER):
                return new CursorLoader(getActivity(),
                        MoneyContract.AccountsEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            case (PLANS_LOADER):
                return new CursorLoader(getActivity(),
                        MoneyContract.PlansEntry.CONTENT_URI,
                        null,
                        MoneyContract.PlansEntry.TABLE_NAME + "." + MoneyContract.PlansEntry.COLUMN_PLANS_STATUS + " = 1",
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

                if (!isSavingNew) {
                    accountsPopupWindow.initWheelPosition(transactionSaving.getMainAccount(),
                            transactionSaving.getSubAccount());
                    accountView.setText(accountsPopupWindow.getDisplayNameStr());
                } else {
                    accountView.setText(accountsPopupWindow.getDisplayNameStr());
                }
                break;
            case PLANS_LOADER:
                ArrayList<Plan> plans = new ArrayList<>();
                if (data != null && data.moveToFirst()) {
                    do{
                        Plan plan= new Plan(data.getInt(0),
                                data.getString(2),
                                data.getLong(3),
                                data.getInt(4),
                                data.getString(5),
                                data.getInt(6));
                        plans.add(plan);
                    } while (data.moveToNext());
                }
                plansPopupWindow.setData(plans);

                if(!isSavingNew){
                    plansPopupWindow.initWheelPosition(transactionSaving.getMainBudgetID());
                    plansView.setText(plansPopupWindow.getCurrentPlanName());
                } else {
                    plansView.setText(plansPopupWindow.getCurrentPlanName());
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
