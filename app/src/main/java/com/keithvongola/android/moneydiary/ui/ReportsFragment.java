package com.keithvongola.android.moneydiary.ui;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.keithvongola.android.moneydiary.Backable;
import com.keithvongola.android.moneydiary.R;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.formatDateStringAsLong;
import static com.keithvongola.android.moneydiary.Utility.getAmountStr;
import static com.keithvongola.android.moneydiary.Utility.getCalendarFromFormattedLong;
import static com.keithvongola.android.moneydiary.Utility.getCurrentDay;
import static com.keithvongola.android.moneydiary.Utility.getCurrentMonth;
import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.moneyConversion;
import static com.keithvongola.android.moneydiary.Utility.pieColors;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry.buildMainsBudgetsUriWithDate;

public class ReportsFragment extends Fragment implements Backable,
        LoaderManager.LoaderCallbacks<Cursor>,
        DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        OnChartValueSelectedListener {
    @BindView(R.id.reports_previous_btn) ImageButton previousBtn;
    @BindView(R.id.reports_next_btn) ImageButton nextBtn;
    @BindView(R.id.reports_date_start) TextView dateStartView;
    @BindView(R.id.reports_date_end) TextView dateEndView;
    @BindView(R.id.reports_selected_text) TextView selectedChartView;
    @BindView(R.id.reports_spinner) Spinner periodSpinner;
    @BindView(R.id.reports_pie_chart) PieChart pieChart;

    private static final int TRANSACTION_LOADER = 0;
    private Uri transactionsUri;
    private DatePickerDialog datePickerDialog;
    private static int period = 1; // 0 : Weekly, 1 : Monthly, 2 : Yearly (Default to be 1 : Monthly)
    private int lastDateView;
    private Calendar cal;
    private PieDataSet dataSet;
    private static String mainCurrency;

    public ReportsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsUri = buildMainsBudgetsUriWithDate(getDate(null, true), getDate(null, false));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_reports);
        mainCurrency = getMainCurrency(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        ButterKnife.bind(this, rootView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.records_period_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);
        periodSpinner.setOnItemSelectedListener(this);
        periodSpinner.setSelection(1);

        dateStartView.setText(getDate(null, true));
        dateEndView.setText(getDate(null, false));
        datePickerDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, this, getCurrentYear(), getCurrentMonth(), getCurrentDay());

        dateStartView.setOnClickListener(this);
        dateEndView.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.DKGRAY);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
        pieChart.setCenterText(getString(R.string.app_name));
        pieChart.setCenterTextSize(16f);
        pieChart.setOnChartValueSelectedListener(this);
        pieChart.setHoleColor(ContextCompat.getColor(getActivity(), R.color.colorTeal100));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRANSACTION_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                transactionsUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        DatabaseUtils.dumpCursor(c);
        if (c != null && c.moveToFirst()){
            List<PieEntry> entries = new ArrayList<>();
            String mainCurrency = getMainCurrency(getActivity());
            String lastBudgetName = c.getString(1);
            MonetaryAmount total = Money.of(0, mainCurrency);
            int i = 0;
            while (i < c.getCount()) {
                if (c.getString(1).equals(lastBudgetName)) { // Same budget with different currency
                    MonetaryAmount amount = Money.of(new BigDecimal(c.getString(4)).scaleByPowerOfTen(-c.getInt(3)).negate(), c.getString(2));
                    if (!amount.getCurrency().toString().equals(mainCurrency))
                        amount = moneyConversion(getActivity(), amount, mainCurrency);
                    total = total.add(amount);
                    c.moveToNext();
                    i++;
                } else {
                    entries.add(new PieEntry(Float.parseFloat(total.getNumber().toString()), lastBudgetName));
                    lastBudgetName = c.getString(1);
                    total = Money.of(0, mainCurrency);
                }
            }

            entries.add(new PieEntry(Float.parseFloat(total.getNumber().toString()), lastBudgetName));

            dataSet = new PieDataSet(entries, "");
            dataSet.setColors(pieColors);
            dataSet.setValueTextSize(12f);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());

            pieChart.setData(data);
            pieChart.invalidate(); // refresh
        } else {
            pieChart.setData(null);
            pieChart.invalidate();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public String getDate(String date, boolean isStart) {
        cal = Calendar.getInstance();
        if (date != null)
            cal.setTimeInMillis(formatDateStringAsLong(date));

        cal.set(Calendar.HOUR_OF_DAY, 0); // Reset the hour, min, s and ms in day
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        switch (period) {
            case 0 : // Weekly
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                if (!isStart)
                    cal.add(Calendar.DAY_OF_YEAR, 6);
                break;
            case 1 : // Monthly
                if (isStart)
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                else
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case 2 : // Yearly
                if (isStart) {
                    cal.set(Calendar.MONTH, 0);
                    cal.set(Calendar.DAY_OF_YEAR, 1);
                } else {
                    cal.set(Calendar.MONTH, 12);
                    cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
                }
                break;
            case 3 : // Custom Date
                break;
        }
        return getCalendarFromFormattedLong(cal.getTimeInMillis());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
        if (period == 3) { // Custom Date
            if (lastDateView == R.id.reports_date_start)
                dateStartView.setText(selectedDate);
            else
                dateEndView.setText(selectedDate);
        }
        updateDateAndUri(selectedDate);
    }

    @Override
    public boolean onBackPressed() {
        if (datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
            return false;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        period = position;
        updateDateAndUri(dateStartView.getText().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void updateDateAndUri(String date) {
        if (period != 3) {
            dateStartView.setText(getDate(date, true));
            dateEndView.setText(getDate(date, false));
        }

        String sqlDateStart = dateStartView.getText().toString();
        sqlDateStart = sqlDateStart.substring(6) + sqlDateStart.substring(2, 6) + sqlDateStart.substring(0,2);

        String sqlDateEnd = dateEndView.getText().toString();
        sqlDateEnd = sqlDateEnd.substring(6) + sqlDateEnd.substring(2, 6) + sqlDateEnd.substring(0,2);

        transactionsUri = buildMainsBudgetsUriWithDate(sqlDateStart, sqlDateEnd);
        getLoaderManager().restartLoader(TRANSACTION_LOADER, null, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reports_previous_btn:
                long dateStart = formatDateStringAsLong(dateStartView.getText().toString());
                cal = Calendar.getInstance();
                cal.setTimeInMillis(dateStart);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                updateDateAndUri(getCalendarFromFormattedLong(cal.getTimeInMillis()));
                break;
            case R.id.reports_next_btn:
                long dateEnd = formatDateStringAsLong(dateEndView.getText().toString());
                cal = Calendar.getInstance();
                cal.setTimeInMillis(dateEnd);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                updateDateAndUri(getCalendarFromFormattedLong(cal.getTimeInMillis()));
                break;
            case R.id.reports_date_start:
            case R.id.reports_date_end:
                lastDateView = v.getId();
                cal = Calendar.getInstance();
                cal.setTimeInMillis(formatDateStringAsLong(((TextView) v).getText().toString()));
                datePickerDialog.updateDate(cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        MonetaryAmount expense = Money.of(new BigDecimal(String.valueOf(e.getY())), mainCurrency);
        selectedChartView.setText(dataSet.getEntryForIndex((int) h.getX()).getLabel() + ": "
                + expense.getCurrency().toString() + " " + getAmountStr(expense));
    }

    @Override
    public void onNothingSelected() {
        selectedChartView.setText(getString(R.string.reports_select_chart));
    }
}
