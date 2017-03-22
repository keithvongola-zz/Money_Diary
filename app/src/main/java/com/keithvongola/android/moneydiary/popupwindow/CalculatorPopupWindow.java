package com.keithvongola.android.moneydiary.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gp89developers.calculatorinputview.utils.Operators;
import com.keithvongola.android.moneydiary.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CalculatorPopupWindow extends PopupWindow {
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_calculator_submit) Button submit;
    @BindView(R.id.popup_title) TextView title;
    @BindView(R.id.clear_button) Button clear;
    @BindView(R.id.delete_button) Button delete;
    @BindView(R.id.divider_button) Button divider;
    @BindView(R.id.multiplication_button) Button multiplication;
    @BindView(R.id.sum_button) Button sum;
    @BindView(R.id.subtraction_button) Button subtraction;
    @BindView(R.id.equal_button) Button equal;
    @BindView(R.id.submit) Button okay;
    @BindView(R.id.one_button) Button one;
    @BindView(R.id.two_button) Button two;
    @BindView(R.id.three_button) Button three;
    @BindView(R.id.four_button) Button four;
    @BindView(R.id.five_button) Button five;
    @BindView(R.id.six_button) Button six;
    @BindView(R.id.seven_button) Button seven;
    @BindView(R.id.eight_button) Button eight;
    @BindView(R.id.nine_button) Button nine;
    @BindView(R.id.zero_button) Button zero;
    @BindView(R.id.two_zero_button) Button twoZero;
    @BindView(R.id.point_button) Button point;
    private OnPopupWindowInteractionListener mCallback;

    private static final String ZERO = "0";
    private static final String ZERO_ZERO = "00";
    private static final String POINT = ".";

    private boolean isFirstClicked;
    //operations values
    private boolean lastPoint;
    private boolean clickArithmeticOperator;
    private boolean clickEqualOperator;
    private boolean clearInput;
    private BigDecimal firstValue;
    private BigDecimal secondsValue;
    private String operatorExecute = Operators.NONE;

    public CalculatorPopupWindow(FragmentActivity context, View.OnClickListener selectItemsOnClick) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calculator, null);
        ButterKnife.bind(this, view);

        one.setOnClickListener(mOnNumberBtnClickListener);
        two.setOnClickListener(mOnNumberBtnClickListener);
        three.setOnClickListener(mOnNumberBtnClickListener);
        four.setOnClickListener(mOnNumberBtnClickListener);
        five.setOnClickListener(mOnNumberBtnClickListener);
        six.setOnClickListener(mOnNumberBtnClickListener);
        seven.setOnClickListener(mOnNumberBtnClickListener);
        eight.setOnClickListener(mOnNumberBtnClickListener);
        nine.setOnClickListener(mOnNumberBtnClickListener);
        zero.setOnClickListener(mOnNumberBtnClickListener);
        twoZero.setOnClickListener(mOnNumberBtnClickListener);
        point.setOnClickListener(mOnNumberBtnClickListener);

        divider.setOnClickListener(mOnOperatorBtnClickListener);
        multiplication.setOnClickListener(mOnOperatorBtnClickListener);
        sum.setOnClickListener(mOnOperatorBtnClickListener);
        subtraction.setOnClickListener(mOnOperatorBtnClickListener);

        clear.setOnClickListener(mOnOperatorBtnClickListener);
        delete.setOnClickListener(mOnOperatorBtnClickListener);
        equal.setOnClickListener(mOnOperatorBtnClickListener);
        okay.setOnClickListener(mOnOperatorBtnClickListener);

        cancel.setOnClickListener(selectItemsOnClick);
        submit.setOnClickListener(selectItemsOnClick);
        this.setContentView(view);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(context.getColor(R.color.colorGrey100));
        this.setBackgroundDrawable(dw);
        this.setOutsideTouchable(false);
        this.setFocusable(false);
        isFirstClicked = true;
    }

    public interface OnPopupWindowInteractionListener {
        void setEditAmountText(String str);
        String getEditAmountText();
        void setDevOperationInputView(String str);
        String getDevOperationInputView();
    }

    public void setOnPopupWindowClickListener(OnPopupWindowInteractionListener mCallback){
        this.mCallback = mCallback;
    }

    private final View.OnClickListener mOnOperatorBtnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (view instanceof Button) {
                String value = ((Button) view).getText().toString();

                switch (value) {
                    case Operators.SUM:
                    case Operators.SUBTRACTION:
                    case Operators.MULTIPLICATION:
                    case Operators.DIVIDER: {
                        equal.setVisibility(View.VISIBLE);
                        okay.setVisibility(View.GONE);
                        clickEqualOperator = false;
                        operatorExecute = value;
                        if (!clickArithmeticOperator) {
                            clickArithmeticOperator = true;
                            prepareOperation(false);
                        } else {
                            replaceOperator(value);
                        }
                        break;
                    }
                    case Operators.CLEAR: {
                        clear();
                        break;
                    }
                    case Operators.DELETE: {
                        removeLastNumber();
                        break;
                    }
                    case Operators.EQUAL:
                    case "OK": {
                        if (clearInput) {
                            returnResultOperation();
                        } else if(!mCallback.getEditAmountText().equals(POINT)){
                            prepareOperation(true);

                            equal.setVisibility(View.GONE);
                            okay.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.VISIBLE);
                            clickEqualOperator = true;
                            clickArithmeticOperator = false;
                            firstValue = null;
                            secondsValue = null;
                        }
                        break;
                    }
                }
            }
        }
    };



    private final View.OnClickListener mOnNumberBtnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (view instanceof Button) {
                String value = ((Button) view).getText().toString();
                concatNumeric(value);

                equal.setVisibility(View.VISIBLE);
                okay.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
                clickEqualOperator = false;
                clickArithmeticOperator = false;
            }
        }
    };

    private void concatNumeric(String value) {
        if (value == null || mCallback.getEditAmountText() == null) {
            return;
        }
        String oldValue;

        if(isFirstClicked){
            isFirstClicked = false;
            oldValue = "";
        } else {
            oldValue = mCallback.getEditAmountText();
        }

        lastPoint = oldValue.contains(POINT);

        String newValue;
        if(clearInput || (oldValue.equals(ZERO) && !value.equals(POINT))){
            newValue = value;
        } else if (value.equals(POINT) && lastPoint) {
            newValue = oldValue;
        } else {
            newValue = (oldValue + value);
        }

        newValue = oldValue.equals(ZERO) && value.equals(ZERO_ZERO) ? oldValue : newValue;

        mCallback.setEditAmountText(newValue);

        clearInput = false;
    }

    private void prepareOperation(boolean isEqualExecute) {
        if(!mCallback.getEditAmountText().equals(POINT)) {
            clearInput = true;

            if (isEqualExecute) {
                mCallback.setDevOperationInputView("");
            } else {
                concatDevelopingOperation(operatorExecute, mCallback.getEditAmountText(), false);
            }

                if (firstValue == null) {
                    firstValue = new BigDecimal(mCallback.getEditAmountText().replaceAll(",", ""));
                } else if (secondsValue == null) {
                    secondsValue = new BigDecimal(mCallback.getEditAmountText().replaceAll(",", ""));
                }

                if (!clickEqualOperator) {
                    executeOperation(operatorExecute);
                }
        }
    }

    private void replaceOperator(String operator) {
        String operationValue = mCallback.getDevOperationInputView();

        if (TextUtils.isEmpty(operationValue)) {
            return;
        }

        String oldOperator = operationValue.substring(operationValue.length() - 1, operationValue.length());

        if (oldOperator.equals(operator)) {
            return;
        }

        String operationNewValue = operationValue.substring(0, operationValue.length() - 2);
        concatDevelopingOperation(operator, operationNewValue, true);
    }

    private void concatDevelopingOperation(String operator, String value, boolean clear) {
        boolean noValidCharacter = operator.equals(Operators.CLEAR) || operator.equals(Operators.DELETE) || operator.equals(Operators.EQUAL);

        if (!noValidCharacter) {
            String oldValue = clear ? "" : mCallback.getDevOperationInputView();
            mCallback.setDevOperationInputView(String.format("%s %s %s", oldValue, value, operator));
        }
    }

    private void executeOperation(String operator) {
        if (firstValue == null || secondsValue == null) {
            return;
        }

        BigDecimal resultOperation = new BigDecimal("0.00");

        switch (operator) {
            case Operators.SUM: {
                resultOperation = firstValue.add(secondsValue);
                break;
            }
            case Operators.SUBTRACTION: {
                resultOperation = firstValue.subtract(secondsValue);
                break;
            }
            case Operators.MULTIPLICATION: {
                resultOperation = firstValue.multiply(secondsValue);
                break;
            }
            case Operators.DIVIDER: {
                if (secondsValue.signum() > 0) {
                    int scale = firstValue.scale() > 3 ? firstValue.scale() : 3;
                    resultOperation = firstValue.divide(secondsValue,scale, RoundingMode.HALF_UP);
                }
                break;
            }
        }

        mCallback.setEditAmountText(formatValue(resultOperation));

        firstValue = resultOperation;
        secondsValue = null;
    }

    private void returnResultOperation() {
        String result = mCallback.getEditAmountText();
        mCallback.setEditAmountText(result);
        if(okay.getVisibility()==View.VISIBLE) {
            this.dismiss();
        }
    }

    private void clear() {
        firstValue = null;
        secondsValue = null;
        lastPoint = false;
        operatorExecute = Operators.NONE;

        mCallback.setDevOperationInputView("");
        mCallback.setEditAmountText(ZERO);
    }

    private void removeLastNumber() {
        String value = mCallback.getEditAmountText();

        if (TextUtils.isEmpty(value) || value.length() == 1) {
            mCallback.setEditAmountText(ZERO);
            return;
        }

        mCallback.setEditAmountText(value.substring(0, value.length() - 1));
    }

    private String formatValue(BigDecimal value) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(',');

        decimalFormatSymbols.setDecimalSeparator('.');
        NumberFormat decimalFormat = new DecimalFormat();
        ((DecimalFormat) decimalFormat).setDecimalFormatSymbols(decimalFormatSymbols);
        decimalFormat.setMinimumFractionDigits(value.scale());

        String valueStr = decimalFormat.format(value);

        int pointIndex = valueStr.indexOf(POINT);

        String integerValue = pointIndex == -1 ? valueStr : valueStr.substring(0, valueStr.indexOf(POINT));
        String decimalValue = pointIndex == -1 ? ZERO : valueStr.substring(valueStr.indexOf(POINT) + 1, valueStr.length());

        if (decimalValue.equals(ZERO_ZERO) || decimalValue.equals(ZERO)) {
            return integerValue;
        }

        return valueStr;
    }

}
