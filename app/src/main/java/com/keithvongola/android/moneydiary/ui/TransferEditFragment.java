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
import com.keithvongola.android.moneydiary.pojo.Transaction;
import com.keithvongola.android.moneydiary.popupwindow.AccountsPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.DatePopupWindow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_TRANSACTION;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionIdSelection;

public class TransferEditFragment extends Fragment
        implements CalculatorPopupWindow.OnPopupWindowInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.item_transfer) LinearLayout transferEditView;
    @BindView(R.id.et_amount) EditText amountView;
    @BindView(R.id.et_account) EditText accountView;
    @BindView(R.id.developing_operation_inputText) TextView devOperationInputView;
    @BindView(R.id.et_date) EditText dateView;
    @BindView(R.id.et_notes) EditText notesView;
    @BindView(R.id.transaction_amount_container) LinearLayout amountContainer;
    @BindView(R.id.til_et_account) TextInputLayout accountContainer;
    @BindView(R.id.til_et_date) TextInputLayout dateContainer;
    @BindView(R.id.til_et_notes) TextInputLayout notesContainer;

    @BindView(R.id.btn_discard) Button btnDiscard;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.btn_save_and_new) Button btnSaveAndNew;
    @BindView(R.id.btn_delete) Button btnDelete;

    private static final int ACCOUNTS_LOADER = 20;
    private boolean isTransferNew;
    private Transaction transactionTransfer;
    private int mainTransactionId, subTransactionId;

    private DatePopupWindow datePopupWindow;
    private CalculatorPopupWindow calculatorPopupWindow;
    private AccountsPopupWindow accountsPopupWindow;

    public TransferEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(ACCOUNTS_LOADER, null, this);
        isTransferNew = true;
        if (getArguments() != null) {
            transactionTransfer = getArguments().getParcelable(ARG_TRANSACTION);
            isTransferNew = false;
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_transfer);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_transfer, container, false);
        ButterKnife.bind(this,rootView);

        datePopupWindow = new DatePopupWindow(getActivity(), selectItemsOnClick);
        accountsPopupWindow = new AccountsPopupWindow(getActivity(), selectItemsOnClick, true);
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
        datePopupWindow.setOnDismissListener(onDismissListener);

        //Display or Hide popup-window when focus change
        amountView.setOnFocusChangeListener(onFocusChangeListener);
        accountView.setOnFocusChangeListener(onFocusChangeListener);
        dateView.setOnFocusChangeListener(onFocusChangeListener);
        notesView.setOnFocusChangeListener(onFocusChangeListener);

        amountView.setInputType(InputType.TYPE_NULL);
        accountView.setInputType(InputType.TYPE_NULL);
        dateView.setInputType(InputType.TYPE_NULL);

        if (!isTransferNew) {
            btnSaveAndNew.setVisibility(View.GONE);
            amountView.setText(getAmountStr(transactionTransfer.getAmount()).replace("-",""));
            dateView.setText(Utility.getCalendarFromFormattedLong(transactionTransfer.getDate().getTime()));
            datePopupWindow.setDateWheel(transactionTransfer.getDate().getTime());
            notesView.setText(transactionTransfer.getNotes());
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
                case R.id.popup_cancel: {
                    clearFocus();
                    break;
                }
            }
        }};


    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case (R.id.et_amount):
                    if (hasFocus) {
                        amountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        calculatorPopupWindow.showAtLocation(transferEditView, Gravity.BOTTOM, 0, 0);
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
                        if (accountsPopupWindow.getCurrentParentID() == -1) {
                            Toast.makeText(getActivity(), R.string.pw_empty_account, Toast.LENGTH_SHORT).show();
                            clearFocus();
                        } else {
                            accountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                            accountsPopupWindow.showAtLocation(transferEditView, Gravity.BOTTOM, 0, 0);
                        }
                    }else {
                        accountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        accountsPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_date):
                    if (hasFocus) {
                        dateContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        datePopupWindow.showAtLocation(transferEditView, Gravity.BOTTOM, 0, 0);
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
        transferEditView.clearFocus();
    }

    @OnClick(R.id.btn_discard)
    public void discard(){
        if (isTransferNew)
            getActivity().finish();
        else
            getFragmentManager().popBackStack();
    }

    @OnClick(R.id.btn_save)
    public void actionSaveTransaction() {
        if(saveTransaction())
            getActivity().finish();
        else
            getActivity().getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.btn_save_and_new)
    public void actionSaveAndNew(){
        if(saveTransaction()){
            accountsPopupWindow.reloadTransferAccounts();
            datePopupWindow.reloadDate();
            initValues();
        }
    }

    @OnClick(R.id.btn_delete)
    public void deleteTransaction() {
        getAccountID();
        getActivity().getContentResolver().delete(TransactionEntry.CONTENT_URI,
                sTransactionIdSelection,
                new String[]{String.valueOf(mainTransactionId)});

        getActivity().getContentResolver().delete(TransactionEntry.CONTENT_URI,
                sTransactionIdSelection,
                new String[]{String.valueOf(subTransactionId)});

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
        }
        return false;
    }

    private List<ContentValues> getTransferContentValues(){
        ArrayList<ContentValues> contentValuesList= new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_TYPE, "2");
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_CURRENCY,
                    accountsPopupWindow.getCurrentAccountCurrency());
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT, "1");
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_SUB_CAT, "1");
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_DATE,
                    Utility.formatDateStringAsLong(String.valueOf(dateView.getText())));
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_NOTES,
                    String.valueOf(notesView.getText()));

            BigDecimal transferAmount;
            //ContentValves with reverse account IDs
            if (i == 0) {
                transferAmount = new BigDecimal("-"+getEditAmountText());
                contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT,
                        accountsPopupWindow.getCurrentParentID());
                contentValues.put(TransactionEntry.COLUMN_TRANSACTION_SUB_ACCOUNT,
                        accountsPopupWindow.getCurrentChildID());
            } else {
                transferAmount = new BigDecimal(getEditAmountText());
                contentValues.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                        (getEditAmountText()).replace(",", ""));
                contentValues.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT,
                        accountsPopupWindow.getCurrentChildID());
                contentValues.put(TransactionEntry.COLUMN_TRANSACTION_SUB_ACCOUNT,
                        accountsPopupWindow.getCurrentParentID());
            }
            contentValues.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                    bigDecimalToDbVal(transferAmount,accountsPopupWindow.getCurrencyUnit()));

            contentValuesList.add(contentValues);
        }

        return contentValuesList;
    }

    private void getAccountID(){
        if (transactionTransfer.getAmount().signum() < 0) {
            mainTransactionId = transactionTransfer.getId();
            subTransactionId = mainTransactionId + 1;
        } else {
            subTransactionId = transactionTransfer.getId();
            mainTransactionId = subTransactionId -1;
        }
    }

    public void initValues() {
        amountView.setText("0");
        dateView.setText(Utility.getCalendarFromFormattedLong(Calendar.getInstance().getTime().getTime()));
    }

    public boolean saveTransaction() {
        try {
            BigDecimal transferAmount = new BigDecimal(getEditAmountText());
            if (!(transferAmount.signum() > 0)) {
                Toast.makeText(getActivity(), R.string.toast_transaction_amount_zero, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.toast_transaction_amount_invalid , Toast.LENGTH_SHORT).show();
            return false;
        }

        if (accountsPopupWindow.getCurrentChildID() == -1 | accountsPopupWindow.getCurrentParentID() == -1) {
            Toast.makeText(getActivity(), R.string.toast_transaction_empty_account, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isTransferNew) {
            //Insert transaction records to database
            getActivity().getContentResolver().insert(TransactionEntry.CONTENT_URI,
                    getTransferContentValues().get(0));
            getActivity().getContentResolver().insert(TransactionEntry.CONTENT_URI,
                    getTransferContentValues().get(1));
        } else {
            //Update transaction records in database
            getAccountID();
            getActivity().getContentResolver().update(TransactionEntry.CONTENT_URI,
                    getTransferContentValues().get(0),
                    sTransactionIdSelection,
                    new String[]{String.valueOf(mainTransactionId)});

            getActivity().getContentResolver().update(TransactionEntry.CONTENT_URI,
                    getTransferContentValues().get(1),
                    sTransactionIdSelection,
                    new String[]{String.valueOf(subTransactionId)});
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MoneyContract.AccountsEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Account> accountList = new ArrayList<>();
        if (data != null & data.moveToFirst()) {
            do {
                Account account= new Account(data);
                accountList.add(account);
            } while (data.moveToNext());
        }
        accountsPopupWindow.setData(accountList);

        if (!isTransferNew) {
            accountsPopupWindow.initWheelPosition(transactionTransfer.getMainAccount(),
                    transactionTransfer.getSubAccount());
            accountView.setText(accountsPopupWindow.getDisplayNameStr());
        } else {
        accountView.setText(accountsPopupWindow.getDisplayNameStr());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
