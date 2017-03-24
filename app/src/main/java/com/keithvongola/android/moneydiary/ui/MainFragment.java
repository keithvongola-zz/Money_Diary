package com.keithvongola.android.moneydiary.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.itangqi.waveloadingview.WaveLoadingView;

import static com.keithvongola.android.moneydiary.Utility.ARG_PAGE;
import static com.keithvongola.android.moneydiary.Utility.getCurrentMonth;
import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.moneyConversion;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry.buildTransactionUriByTypeAndDate;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnChartValueSelectedListener {
    @BindView(R.id.fab_menu) FloatingActionsMenu fabMenu;
    @BindView(R.id.overlay_frame_layout) FrameLayout overlayFrameLayout;
    @BindView(R.id.current_month_text) TextView currentMonthView;
    @BindView(R.id.income_amount) TextView incomeAmountView;
    @BindView(R.id.expense_amount) TextView expenseAmountView;
    @BindView(R.id.budget_left_amount) TextView budgetLeftAmountView;
    @BindView(R.id.waveProgressView) WaveLoadingView waveProgressView;
    @BindView(R.id.overview_container) LinearLayout overviewContainerView;
    @BindView(R.id.line_chart) LineChart mChart;
    @BindView(R.id.chart_container) LinearLayout chartContainer;
    @BindView(R.id.income_legend) TextView incomeLegendView;
    @BindView(R.id.expense_legend) TextView expenseLegendView;
    @BindView(R.id.saving_legend) TextView savingLegendView;

    private static final int TRANSACTIONS_LOADER = 0;
    private static final int BUDGETS_LOADER = 2;
    private ArrayList<ILineDataSet> dataSets;
    private static int currentMonth;
    private static String mainCurrency;

    public MainFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(TRANSACTIONS_LOADER, null, this);
        getLoaderManager().initLoader(BUDGETS_LOADER, null, this);

        mainCurrency = getMainCurrency(getActivity());
        currentMonth = getCurrentMonth();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this,rootView);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        overlayFrameLayout.getBackground().setAlpha(0);
        fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                overlayFrameLayout.getBackground().setAlpha(240);
                overlayFrameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        fabMenu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                overlayFrameLayout.getBackground().setAlpha(0);
                overlayFrameLayout.setOnTouchListener(null);
            }
        });
        currentMonthView.setText(String.format("%02d", getCurrentMonth()) + "/" + getCurrentYear());
        overviewContainerView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorWhite));
        initChart();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TRANSACTIONS_LOADER, null,this);
        getLoaderManager().restartLoader(BUDGETS_LOADER, null,this);
    }

    @OnClick(R.id.fab_income)
    public void startIncomeActivity(){
        fabMenu.collapse();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(getActivity(),TransactionActivity.class);
                intent.putExtra(ARG_PAGE, 1);
                startActivity(intent);
            }
        }, 80);
    }

    @OnClick(R.id.fab_expense)
    public void startExpenseActivity(){
        fabMenu.collapse();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(getActivity(),TransactionActivity.class);
                intent.putExtra(ARG_PAGE, 0);
                startActivity(intent);
            }
        }, 80);
    }

    @OnClick(R.id.fab_transfer)
    public void startTransferActivity(){
        fabMenu.collapse();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(getActivity(),TransactionActivity.class);
                intent.putExtra(ARG_PAGE, 2);
                startActivity(intent);
            }
        }, 80);
    }

    @OnClick(R.id.fab_saving)
    public void startSavingActivity(){
        fabMenu.collapse();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(getActivity(),TransactionActivity.class);
                intent.putExtra(ARG_PAGE, 3);
                startActivity(intent);
            }
        }, 80);
    }

    public boolean collapse(){
        if (fabMenu.isExpanded()){
            fabMenu.collapse();
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TRANSACTIONS_LOADER:
                return new CursorLoader(getActivity(),
                        buildTransactionUriByTypeAndDate(),
                        null,
                        null,
                        null,
                        null);
            case BUDGETS_LOADER:
                return new CursorLoader(getActivity(),
                        MainBudgetsEntry.buildMainBudgetsUriWithType(true),
                        null,
                        null,
                        null,
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TRANSACTIONS_LOADER:
                setLineChartData(data);
                MonetaryAmount currentMonthIncome = Money.of(new BigDecimal(String.valueOf(dataSets.get(0).getEntriesForXValue(currentMonth-1).get(0).getY())),
                        getMainCurrency(getActivity()));
                MonetaryAmount currentMonthExpense = Money.of(new BigDecimal(String.valueOf(dataSets.get(1).getEntriesForXValue(currentMonth-1).get(0).getY())),
                        getMainCurrency(getActivity()));
                setAmount(expenseAmountView, currentMonthExpense);
                setAmount(incomeAmountView, currentMonthIncome);
                break;
            case BUDGETS_LOADER:
                if (data.moveToFirst()) {
                    MonetaryAmount totalBudget = Money.of(0,mainCurrency);
                    MonetaryAmount budgetUsed = Money.of(0,mainCurrency);
                    for (int i = 0; i < data.getCount(); i++){
                        totalBudget = totalBudget.add(Money.of(new BigDecimal(data.getInt(3)),data.getString(2))
                                .scaleByPowerOfTen(-data.getInt(7)));
                        budgetUsed = budgetUsed.add(Money.of(new BigDecimal(data.getInt(4)),data.getString(2))
                                .scaleByPowerOfTen(-data.getInt(7)));

                        data.moveToNext();
                    }
                    MonetaryAmount budgetLeft = totalBudget.add(budgetUsed);
                    setAmount(budgetLeftAmountView, budgetLeft);
                    int budgetLeftPercent;
                    if (totalBudget.signum() > 0) {
                        budgetLeftPercent = budgetLeft.divide(totalBudget.getNumber())
                                .scaleByPowerOfTen(2)
                                .getNumber()
                                .intValue();
                    } else {
                        budgetLeftPercent = 0;
                    }

                    if (budgetLeftPercent > 0) waveProgressView.setCenterTitle(budgetLeftPercent + "%");
                    else waveProgressView.setCenterTitle("0%");

                    if (budgetLeftPercent < 10)  waveProgressView.setProgressValue(20);
                    else if(budgetLeftPercent < 80) waveProgressView.setProgressValue(budgetLeftPercent);
                    else waveProgressView.setProgressValue(80);

                    if (budgetLeftPercent < 50) {
                        waveProgressView.setWaveColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
                        waveProgressView.setCenterTitleStrokeColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
                    }
                }
                break;
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void initChart(){
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBorders(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getLegend().setEnabled(false);

        mChart.getAxisLeft().setEnabled(true);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getAxisLeft().setDrawLabels(false);

        mChart.getAxisRight().setEnabled(true);
        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisRight().setDrawGridLines(true);
        final String[] months = getActivity().getResources().getStringArray(R.array.months_array);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return months[(int) value];
            }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
    }

    public void setLineChartData(Cursor c) {
        ArrayList<Entry> incomeValues = new ArrayList<>();
        ArrayList<Entry> expenseValues = new ArrayList<>();
        ArrayList<Entry> savingValues = new ArrayList<>();

        MonetaryAmount incomeTotal = Money.of(0, mainCurrency);
        MonetaryAmount expenseTotal = Money.of(0, mainCurrency);
        MonetaryAmount savingTotal = Money.of(0, mainCurrency);
        int m = 1;

        if (c != null && c.moveToFirst()) {
            int i = 0;
            while (i < c.getCount() && m < 12) {
                if (Integer.parseInt(c.getString(0)) == m) {
                    MonetaryAmount amount = Money.of(new BigDecimal(c.getString(4)).scaleByPowerOfTen(-c.getInt(3)), c.getString(2));
                    if (!c.getString(2).equals(mainCurrency))
                        amount = moneyConversion(getActivity(), amount, mainCurrency);
                    switch (c.getInt(1)) {
                        case 0:
                            expenseTotal = expenseTotal.add(amount);
                            break;
                        case 1:
                            incomeTotal = incomeTotal.add(amount);
                            break;
                        case 3:
                            savingTotal = savingTotal.add(amount);
                            break;
                    }
                    c.moveToNext();
                    i++;
                } else {
                    incomeValues.add(new Entry(m - 1, Float.parseFloat(incomeTotal.getNumber().toString())));
                    expenseValues.add(new Entry(m - 1, Float.parseFloat(expenseTotal.negate().getNumber().toString())));
                    savingValues.add(new Entry(m - 1, Float.parseFloat(savingTotal.negate().getNumber().toString())));
                    incomeTotal = Money.of(0, mainCurrency);
                    expenseTotal = Money.of(0, mainCurrency);
                    savingTotal = Money.of(0, mainCurrency);
                    m++;
                }
            }
        }

        while (m < 13) {
            incomeValues.add(new Entry(m - 1, Float.parseFloat(incomeTotal.getNumber().toString())));
            expenseValues.add(new Entry(m - 1, Float.parseFloat(expenseTotal.negate().getNumber().toString())));
            savingValues.add(new Entry(m - 1, Float.parseFloat(savingTotal.negate().getNumber().toString())));
            incomeTotal = Money.of(0, mainCurrency);
            expenseTotal = Money.of(0, mainCurrency);
            savingTotal = Money.of(0, mainCurrency);
            m++;
        }

        final String[] title = getActivity().getResources().getStringArray(R.array.legend_title_array);
        LineDataSet lineDataSet1 = new LineDataSet(expenseValues, title[0]);
        LineDataSet lineDataSet2 = new LineDataSet(incomeValues, title[1]);
        LineDataSet lineDataSet3 = new LineDataSet(savingValues, title[2]);

        dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);
        dataSets.add(lineDataSet3);

        final int[] lineColor = getActivity().getResources().getIntArray(R.array.line_color_array);

        for (int z = 0; z < 3; z++) {
            ((LineDataSet) dataSets.get(z)).setColors(lineColor[z]);
            ((LineDataSet) dataSets.get(z)).setColors(lineColor[z]);
            ((LineDataSet) dataSets.get(z)).setCircleColor(lineColor[z]);
            ((LineDataSet) dataSets.get(z)).setCircleRadius(3f);
            ((LineDataSet) dataSets.get(z)).setLineWidth(2f);
            ((LineDataSet) dataSets.get(z)).setDrawCircleHole(false);
            dataSets.get(z).setValueTextSize(9f);
        }

        ((LineDataSet) dataSets.get(2)).enableDashedLine(10, 10, 0);

        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setVisibleXRangeMaximum(6);
        mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        float selectedX = e.getX();
        MonetaryAmount expense = Money.of(new BigDecimal(String.valueOf(dataSets.get(0).getEntriesForXValue(selectedX).get(0).getY())), mainCurrency);
        MonetaryAmount income = Money.of(new BigDecimal(String.valueOf(dataSets.get(1).getEntriesForXValue(selectedX).get(0).getY())), mainCurrency);
        MonetaryAmount saving = Money.of(new BigDecimal(String.valueOf(dataSets.get(2).getEntriesForXValue(selectedX).get(0).getY())), mainCurrency);
        setAmount(incomeLegendView, income);
        setAmount(expenseLegendView, expense);
        setAmount(savingLegendView, saving);
    }

    @Override
    public void onNothingSelected() {
        incomeLegendView.setText(" - ");
        expenseLegendView.setText(" - ");
        savingLegendView.setText(" - ");
    }
}

