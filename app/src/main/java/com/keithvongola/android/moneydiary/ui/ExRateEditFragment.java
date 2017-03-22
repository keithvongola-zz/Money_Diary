package com.keithvongola.android.moneydiary.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_AUTO;
import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_MANUAL;
import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_TITLE;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MANUAL;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sExchangeRateNameSelection;

public class ExRateEditFragment extends Fragment implements
        CalculatorPopupWindow.OnPopupWindowInteractionListener {
    @BindView(R.id.ll_ex_rate_edit) FrameLayout exRateLL;
    @BindView(R.id.exchange_rate_et_container) LinearLayout exRateContainer;
    @BindView(R.id.manual_exchange_rate_switch) Switch exRateSwitch;
    @BindView(R.id.exchange_rate_et) EditText exRateEt;

    private CalculatorPopupWindow calculatorPopupWindow;
    boolean isManual;
    private String exRateName, exRateFromApi, exRateManual;
    private MenuItem menuItemSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isManual = false;
        if (getArguments() != null) {
            if (getArguments().getInt(ARG_IS_MANUAL) == 1) {
                isManual = true;
                exRateManual = getArguments().getString(ARG_EX_RATE_MANUAL);
            }
            exRateName = getArguments().getString(ARG_EX_RATE_TITLE);
            exRateFromApi = getArguments().getString(ARG_EX_RATE_AUTO);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_done, menu);
        menuItemSubmit = menu.findItem(R.id.action_done);
        if (!isManual)
            menuItemSubmit.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_done && isManual != exRateSwitch.isChecked()) {
            String exRateManualStr = exRateEt.getText().toString();
            //Check if the manual exchange rate is empty
            if (exRateManualStr.length() > 0) {
                double exRateManualVal = Double.parseDouble(exRateManualStr);
                //Check if the manual exchange rate is positive
                //insert manual exchange rate to db if true
                if (exRateManualVal > 0) {
                    ContentValues cv = new ContentValues();

                    NumberFormat formatter = new DecimalFormat("#0.0000");
                    cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_MANUAL_ASK, formatter.format(exRateManualVal));
                    cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL, 1);

                    getContext().getContentResolver().update(
                            ExchangeRateEntry.CONTENT_URI,
                            cv,
                            ExchangeRateEntry.COLUMN_EXCHANGE_RATE_NAME + "=?",
                            new String[]{exRateName});

                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getActivity(), R.string.toast_ex_rate_zero_or_negative, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.toast_ex_rate_empty, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exchange_rate_edit, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(exRateName);

        //Set default value for switch and hint text
        exRateSwitch.setChecked(isManual);
        exRateEt.setHint(exRateFromApi);

        //Set default visibility for exchange rate container
        if (isManual) {
            exRateEt.setText(exRateManual);
            exRateContainer.setVisibility(View.VISIBLE);
        } else {
            exRateContainer.setVisibility(View.GONE);
        }

        //Set visibility of exchange rate container on switch checked change
        exRateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    exRateContainer.setVisibility(View.VISIBLE);
                    menuItemSubmit.setVisible(true);
                } else {
                    exRateContainer.setVisibility(View.GONE);
                    menuItemSubmit.setVisible(false);
                    if (isManual) {
                        isManual = false;
                        ContentValues cv = new ContentValues();
                        cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL, 0);

                        getContext().getContentResolver().update(
                                ExchangeRateEntry.CONTENT_URI,
                                cv,
                                sExchangeRateNameSelection,
                                new String[]{exRateName});
                    }
                }
            }
        });

        //Display calculator popup window when edit text is on focus
        exRateEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.exchange_rate_et) {
                    if (hasFocus) {
                        exRateContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.color.colorAmber100));
                        calculatorPopupWindow.showAtLocation(exRateLL, Gravity.BOTTOM, 0, 0);
                    } else {
                        exRateContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.list_item_border));
                        exRateEt.invalidate();
                        calculatorPopupWindow.dismiss();
                    }
                }
            }
        });

        //Set onClickListener for action submit and cancel for calculator popup window
        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.popup_calculator_submit:
                            case R.id.popup_cancel:
                                exRateLL.clearFocus();
                                break;
                        }
                    }
                }
        );
        calculatorPopupWindow.setOnPopupWindowClickListener(this);

        //Clear all focus when calculator popup window dismiss
        calculatorPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                exRateLL.clearFocus();
            }
        });
        exRateEt.setInputType(InputType.TYPE_NULL);

        return rootView;
    }

    @Override
    public void setEditAmountText(String str) {
        exRateEt.setText(str);
    }

    @Override
    public String getEditAmountText() {
        return exRateEt.getText().toString();
    }

    public void setDevOperationInputView(String str) {
    }

    public String getDevOperationInputView() {
        return null;
    }


    public boolean popupWindowIsShowing() {
        if (calculatorPopupWindow.isShowing()) {
            calculatorPopupWindow.dismiss();
            return true;
        }
        return false;
    }

}
