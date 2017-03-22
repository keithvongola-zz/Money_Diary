package com.keithvongola.android.moneydiary.ui;

import android.content.ContentValues;
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

import com.keithvongola.android.moneydiary.adapter.BudgetIconAdapter;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;
import com.keithvongola.android.moneydiary.pojo.Plan;
import com.keithvongola.android.moneydiary.pojo.SubPlan;
import com.keithvongola.android.moneydiary.popupwindow.CalculatorPopupWindow;
import com.keithvongola.android.moneydiary.popupwindow.DatePopupWindow;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.gp89developers.calculatorinputview.activities.CalculatorActivity.POINT;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_PARENT_ID;
import static com.keithvongola.android.moneydiary.Utility.ARG_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_SUB_PLAN;
import static com.keithvongola.android.moneydiary.Utility.bigDecimalToDbVal;
import static com.keithvongola.android.moneydiary.Utility.formatDateStringAsLong;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.Utility.getCalendarFromFormattedLong;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sPlansIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubPlansIdSelection;

public class PlansEditFragment extends Fragment implements CalculatorPopupWindow.OnPopupWindowInteractionListener {
    @BindView(R.id.plans_edit_container) FrameLayout plansEditContainer;
    @BindView(R.id.plan_name_container) LinearLayout nameContainer;
    @BindView(R.id.plan_target_container) LinearLayout targetAmountContainer;
    @BindView(R.id.plan_start_date_container) LinearLayout startDateContainer;
    @BindView(R.id.plan_terms_container) LinearLayout termsContainer;
    @BindView(R.id.plan_grid_layout) RecyclerView gridRecycleView;
    @BindView(R.id.et_plan_name) EditText nameView;
    @BindView(R.id.et_plan_target) EditText targetAmountView;
    @BindView(R.id.et_plan_start_date) EditText startDateView;
    @BindView(R.id.et_plan_terms) EditText termsView;

    private CalculatorPopupWindow calculatorPopupWindow;
    private DatePopupWindow datePopupWindow;
    private boolean isNewPlan, isMainPlan;
    private Plan plan;
    private SubPlan subPlan;

    private String name, parentId;
    private long dateInLong;
    private int terms, iconResId;
    private MonetaryAmount targetAmount;

    public PlansEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            isMainPlan = getArguments().getBoolean(ARG_IS_MAIN_PLAN);
            plan = getArguments().getParcelable(ARG_PLAN);
            subPlan = getArguments().getParcelable(ARG_SUB_PLAN);
            parentId = getArguments().getString(ARG_PARENT_ID);

            isNewPlan = plan == null && subPlan == null;

            if(!isNewPlan && isMainPlan) {
                name = plan.getName();
                terms = plan.getTerms();
                dateInLong = plan.getDateStart().getTime();
                iconResId = plan.getIconResId();
            } else if (!isNewPlan && !isMainPlan) {
                name = subPlan.getName();
                targetAmount = subPlan.getAmountTarget();
                iconResId = subPlan.getIconResId();
            }
        }
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
            name = nameView.getText().toString();
            if (name.length()<=0) {
                Toast.makeText(getActivity(), R.string.toast_empty_budget_name, Toast.LENGTH_LONG).show();
                return true;
            }

            if (isMainPlan){
                String termsStr = termsView.getText().toString().replace(",","");
                int pointIndex = termsStr.indexOf(POINT);
                try {
                    terms = pointIndex == -1 ? Integer.parseInt(termsStr) : Integer.parseInt(termsStr.substring(0, termsStr.indexOf(POINT)));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.toast_plan_terms_invalid, Toast.LENGTH_LONG).show();
                    return true;
                }

                if (terms <= 0) {
                    Toast.makeText(getActivity(), R.string.toast_plan_terms_zero_or_negative, Toast.LENGTH_LONG).show();
                    return true;
                }
            } else {
                try {
                    targetAmount = Money.of(new BigDecimal(targetAmountView.getText().toString().replace(",", "")), getMainCurrency(getActivity()));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.toast_plan_amount_invalid, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (targetAmount.signum() < 0) {
                    Toast.makeText(getActivity(), R.string.toast_plan_amount_invalid, Toast.LENGTH_LONG).show();
                    return true;
                }
            }
            if (((BudgetIconAdapter) gridRecycleView.getAdapter()).getLastCheckedResId() == -1) {
                Toast.makeText(getActivity(), R.string.toast_empty_icon, Toast.LENGTH_LONG).show();
                return true;
            }

            String currencyStr = getMainCurrency(getActivity());
            ContentValues cv = new ContentValues();
            if (isMainPlan) {
                cv.put(PlansEntry.COLUMN_PLANS_NAME, name);
                cv.put(PlansEntry.COLUMN_PLANS_CURRENCY, currencyStr);
                cv.put(PlansEntry.COLUMN_PLANS_STATUS, 1);
                cv.put(PlansEntry.COLUMN_PLANS_TERMS, terms);
                cv.put(PlansEntry.COLUMN_PLANS_DATE_START, formatDateStringAsLong(startDateView.getText().toString()));
                cv.put(PlansEntry.COLUMN_PLANS_ICON, ((BudgetIconAdapter) gridRecycleView.getAdapter()).getLastCheckedResId());

                if (isNewPlan)
                    getContext().getContentResolver().insert(PlansEntry.CONTENT_URI, cv);
                else
                    getContext().getContentResolver().update(PlansEntry.CONTENT_URI, cv, sPlansIdSelection, new String[]{String.valueOf(plan.getId())});
            } else {
                targetAmount = Money.of(new BigDecimal(targetAmountView.getText().toString().replace(",","")), currencyStr);
                cv.put(SubPlansEntry.COLUMN_SUB_PLANS_NAME, name);
                cv.put(SubPlansEntry.COLUMN_SUB_PLANS_AMOUNT,
                        bigDecimalToDbVal(targetAmount));
                cv.put(SubPlansEntry.COLUMN_SUB_PLANS_ICON, ((BudgetIconAdapter) gridRecycleView.getAdapter()).getLastCheckedResId());

                if (isNewPlan) {
                    cv.put(SubPlansEntry.COLUMN_SUB_PLANS_PARENT, parentId);
                    cv.put(SubPlansEntry.COLUMN_PLANS_CURRENCY,currencyStr);
                    getContext().getContentResolver().insert(SubPlansEntry.CONTENT_URI, cv);
                } else {
                    getContext().getContentResolver().update(SubPlansEntry.CONTENT_URI, cv, sSubPlansIdSelection, new String[]{String.valueOf(subPlan.getId())});
                }
            }
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_plans_edit, container, false);
        ButterKnife.bind(this, rootView);

        BudgetIconAdapter iconAdapter = new BudgetIconAdapter(getActivity());
        RecyclerView.LayoutManager lLayout = new GridLayoutManager(getActivity(), 5);
        gridRecycleView.setLayoutManager(lLayout);
        gridRecycleView.setAdapter(iconAdapter);

        datePopupWindow = new DatePopupWindow(getActivity(),onClickListener);
        datePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                plansEditContainer.clearFocus();
            }
        });

        //Determine whether this fragment is for creating new plan or editing existing plan
        if (isMainPlan) {
            targetAmountContainer.setVisibility(View.GONE);
        } else {
            startDateContainer.setVisibility(View.GONE);
            termsContainer.setVisibility(View.GONE);
        }


        if (!isNewPlan) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_edit_plan);
            nameView.setText(name);
            iconAdapter.setLastCheckedPos(iconResId);

            if (isMainPlan) {
                nameView.setText(name);
                termsView.setText(String.valueOf(terms));
                startDateView.setText(getCalendarFromFormattedLong(dateInLong));
                datePopupWindow.setDateWheel(dateInLong);
            } else {
                targetAmountView.setText(getAmountStr(targetAmount));
            }
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_new_plan);
            nameView.setHint(R.string.account_enter_name);
            if (!isMainPlan) {
                targetAmount = Money.of(new BigDecimal("0"), getMainCurrency(getActivity()));
                targetAmountView.setText("0");
            } else {
                startDateView.setText(getCalendarFromFormattedLong(Calendar.getInstance().getTime().getTime()));
                termsView.setText("1");
            }
        }

        calculatorPopupWindow = new CalculatorPopupWindow(getActivity(), onClickListener);
        calculatorPopupWindow.setOnPopupWindowClickListener(this);
        calculatorPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                plansEditContainer.clearFocus();
            }
        });

        nameView.setOnFocusChangeListener(onFocusChangeListener);
        targetAmountView.setOnFocusChangeListener(onFocusChangeListener);
        termsView.setOnFocusChangeListener(onFocusChangeListener);
        startDateView.setOnFocusChangeListener(onFocusChangeListener);

        termsView.setInputType(InputType.TYPE_NULL);
        targetAmountView.setInputType(InputType.TYPE_NULL);
        startDateView.setInputType(InputType.TYPE_NULL);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.hideKeyboard(getActivity());
    }


    @Override
    public void setEditAmountText(String str) {
        if(targetAmountContainer.hasFocus())
            targetAmountView.setText(str);
         else
            termsView.setText(str);
    }

    @Override
    public String getEditAmountText() {
        if(targetAmountContainer.hasFocus())
            return targetAmountView.getText().toString();
        else
            return termsView.getText().toString();
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
                    plansEditContainer.clearFocus();
                    break;
                case R.id.popup_date_submit:
                    startDateView.setText(datePopupWindow.getCurrentDay()
                        +"-"+ datePopupWindow.getCurrentMonth()
                        +"-"+ datePopupWindow.getCurrentYear());
                    plansEditContainer.clearFocus();
                    break;
            }
        }
    };

    //Show popup window when view has focus,
    //else hide popup window
    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case (R.id.et_plan_name):
                    if (hasFocus) {
                        nameContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                    } else {
                        nameContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        Utility.hideKeyboard(getActivity(),v);
                    }
                    break;
                case (R.id.et_plan_start_date):
                    if (hasFocus) {
                        startDateContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        datePopupWindow.showAtLocation(plansEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        startDateContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        datePopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_plan_target):
                    if (hasFocus) {
                        targetAmountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        calculatorPopupWindow.showAtLocation(plansEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        targetAmountContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        targetAmountView.invalidate();
                        calculatorPopupWindow.dismiss();
                    }
                    break;
                case (R.id.et_plan_terms):
                    if (hasFocus) {
                        termsContainer.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorAmber100));
                        calculatorPopupWindow.showAtLocation(plansEditContainer, Gravity.BOTTOM, 0, 0);
                    } else {
                        termsContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.list_item_border));
                        termsView.invalidate();
                        calculatorPopupWindow.dismiss();
                    }
                    break;
            }
        }
    };

    public boolean popupWindowIsShowing(){
        if (datePopupWindow.isShowing()){
            datePopupWindow.dismiss();
            return true;
        } else if(calculatorPopupWindow.isShowing()){
            calculatorPopupWindow.dismiss();
            return true;
        }
        return false;
    }
}
