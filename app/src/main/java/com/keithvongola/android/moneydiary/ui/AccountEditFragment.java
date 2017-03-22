package com.keithvongola.android.moneydiary.ui;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateFormat;
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
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import com.keithvongola.android.moneydiary.pojo.Account;
import com.keithvongola.android.moneydiary.popupwindow.AccountTypePopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_ACCOUNT;
import static com.keithvongola.android.moneydiary.Utility.INTENT_EXTRA_COUNTRY;
import static com.keithvongola.android.moneydiary.Utility.accountTypeResId;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sAccountIdSelection;

public class AccountEditFragment extends Fragment implements CalculatorPopupWindow.OnPopupWindowInteractionListener {
    @BindView(R.id.account_edit_container) FrameLayout acEditContainer;
    @BindView(R.id.account_name_container) LinearLayout nameContainer;
    @BindView(R.id.account_type_container) LinearLayout typeContainer;
    @BindView(R.id.account_balance_container) LinearLayout balanceContainer;
    @BindView(R.id.account_currency_container) LinearLayout currencyContainer;
    @BindView(R.id.account_institution_container) LinearLayout institutionContainer;

    @BindView(R.id.et_account_name) EditText nameView;
    @BindView(R.id.et_account_type) EditText typeView;
    @BindView(R.id.et_account_balance) EditText balanceView;
    @BindView(R.id.et_account_currency) EditText currencyView;
    @BindView(R.id.et_account_institution) EditText institutionView;

    private CalculatorPopupWindow calculatorPopupWindow;
    private AccountTypePopupWindow accountTypePopupWindow;
    private boolean isNewAccount;
    private Account account;
    private MonetaryAmount currentAccountBalance;

    public AccountEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (id == R.id.action_done) {
            String nameStr = nameView.getText().toString();
            String currencyStr = currencyView.getText().toString();
            MonetaryAmount balance;

            try {
                balance = Money.of(new BigDecimal(balanceView.getText().toString().replace(",", "")), currencyStr);
            } catch (Exception e){
                Toast.makeText(getActivity(), R.string.toast_account_balance_invalid, Toast.LENGTH_LONG).show();
                return true;
            }

            if (nameStr.length() == 0) {
                Toast.makeText(getActivity(), R.string.toast_empty_account_name, Toast.LENGTH_LONG).show();
                return true;
            }

            ContentValues cvAccount = new ContentValues(); // ContentValues for account's data
            cvAccount.put(AccountsEntry.COLUMN_ACCOUNT_NAME, nameStr);
            cvAccount.put(AccountsEntry.COLUMN_ACCOUNT_CURRENCY, currencyStr);
            cvAccount.put(AccountsEntry.COLUMN_ACCOUNT_INSTITUTION, institutionView.getText().toString());
            cvAccount.put(AccountsEntry.COLUMN_ACCOUNT_IS_ACTIVE, 1);

            ContentValues cvTransaction = new ContentValues(); // ContentValues for transaction's data
            cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_TYPE, 0);
            cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_CURRENCY, currencyStr);
            cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_CAT, 1);
            cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_SUB_CAT, 2);
            String date = (DateFormat.format("dd-MM-yyyy", new java.util.Date()).toString());
            cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_DATE, Utility.formatDateStringAsLong(date));

            if(isNewAccount){
               cvAccount.put(AccountsEntry.COLUMN_ACCOUNT_TYPE, accountTypePopupWindow.getAccountType());

               Uri uri = getContext().getContentResolver().insert(
                        AccountsEntry.CONTENT_URI,
                        cvAccount);

                cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT, AccountsEntry.getAccountIdFromUri(uri));
                cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                        bigDecimalToDbVal(balance));
            } else {
                if (cvAccount.size() > 0) {
                    getContext().getContentResolver().update(
                            AccountsEntry.CONTENT_URI,
                            cvAccount,
                            sAccountIdSelection,
                            new String[]{String.valueOf(account.getId())});
                    cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_MAIN_ACCOUNT, account.getId());
                    cvTransaction.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT,
                            bigDecimalToDbVal(balance.subtract(account.getCurrent())));
                }
            }

            if (!currentAccountBalance.equals(balance) | (isNewAccount && !(balance.signum()==0))) {
                getContext().getContentResolver().insert(
                        TransactionEntry.CONTENT_URI,
                        cvTransaction);
            }
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accounts_edit, container, false);
        ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_account);

            isNewAccount = false;
            account = bundle.getParcelable(ARG_ACCOUNT);

            nameView.setText(account.getName());
            currentAccountBalance = account.getCurrent();
            balanceView.setText(getAmountStr(currentAccountBalance));
            currencyView.setText(account.getCurrency());

            if (account.getInstitution() == null || account.getInstitution().length() == 0)
                institutionView.setHint(R.string.hint_empty);
            else
                institutionView.setText(account.getInstitution());
            typeContainer.setVisibility(View.GONE);
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_new_account);
            isNewAccount = true;

            nameView.setHint(R.string.account_enter_name);
            typeView.setText(accountTypeResId(0));
            currentAccountBalance = Money.of(new BigDecimal("0"), getMainCurrency(getActivity()));
            balanceView.setText("0");
            currencyView.setText(getMainCurrency(getActivity()));
            institutionView.setHint(R.string.hint_optional);

            accountTypePopupWindow = new AccountTypePopupWindow(getActivity(),onClickListener);
            accountTypePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    acEditContainer.clearFocus();
                }
            });
            typeView.setOnFocusChangeListener(onFocusChangeListener);
            typeView.setInputType(InputType.TYPE_NULL);
        }

        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(), onClickListener);
        calculatorPopupWindow.setOnPopupWindowClickListener(this);
        calculatorPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                acEditContainer.clearFocus();
            }
        });

        nameView.setOnFocusChangeListener(onFocusChangeListener);
        balanceView.setOnFocusChangeListener(onFocusChangeListener);
        currencyView.setOnFocusChangeListener(onFocusChangeListener);
        institutionView.setOnFocusChangeListener(onFocusChangeListener);

        currencyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CurrencyActivity.class);
                startActivityForResult(intent,0);
                        }
        });

        currencyView.setInputType(InputType.TYPE_NULL);
        balanceView.setInputType(InputType.TYPE_NULL);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.hideKeyboard(getActivity());
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
                case R.id.popup_account_type_submit:
                    typeView.setText(accountTypeResId(
                            accountTypePopupWindow.getAccountType()));
                    acEditContainer.clearFocus();
                    break;
                case R.id.popup_calculator_submit:
                case R.id.popup_cancel:
                    acEditContainer.clearFocus();
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
                case (R.id.et_account_name):
                    if (hasFocus) {
                        nameContainer.setBackground(backgroundFocus);
                    } else {
                        nameContainer.setBackground(backgroundWithoutFocus);
                        Utility.hideKeyboard(getActivity(),v);
                    }
                    break;
                case (R.id.et_account_type):
                    if (hasFocus) {
                        typeContainer.setBackground(backgroundFocus);
                        accountTypePopupWindow.showAtLocation(acEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        typeContainer.setBackground(backgroundWithoutFocus);
                        accountTypePopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_account_balance):
                    if (hasFocus) {
                        balanceContainer.setBackground(backgroundFocus);
                        calculatorPopupWindow.showAtLocation(acEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        balanceContainer.setBackground(backgroundWithoutFocus);
                        balanceView.invalidate();
                        calculatorPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_account_currency):
                    if (hasFocus) {
                        currencyContainer.setBackground(backgroundFocus);
                        Intent intent = new Intent(getActivity(),CurrencyActivity.class);
                        startActivityForResult(intent,0);
                    } else {
                        currencyContainer.setBackground(backgroundWithoutFocus);
                        Utility.hideKeyboard(getActivity(),v);
                    }
                    break;
                case (R.id.et_account_institution):
                    if (hasFocus) {
                        institutionContainer.setBackground(backgroundFocus);
                    } else {
                        institutionContainer.setBackground(backgroundWithoutFocus);
                        Utility.hideKeyboard(getActivity(),v);
                    }
                    break;
            }
        }
    };

    //Get currency of account from CurrencyActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            currencyView.setText(data.getStringExtra(INTENT_EXTRA_COUNTRY));
        currencyContainer.clearFocus();
    }

    public boolean popupWindowIsShowing(){
        if (isNewAccount && accountTypePopupWindow.isShowing()){
            accountTypePopupWindow.dismiss();
            return true;
        } else if(calculatorPopupWindow.isShowing()){
            calculatorPopupWindow.dismiss();
            return true;
        }
        return false;
    }
}
