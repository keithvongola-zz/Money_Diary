package com.keithvongola.android.moneydiary.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.wheelAdapter.DayWheelAdapter;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import static com.keithvongola.android.moneydiary.Utility.formatDateStringAsLong;
import static com.keithvongola.android.moneydiary.Utility.getCurrentDate;


public class DatePopupWindow extends PopupWindow implements OnWheelChangedListener {
    private Context mContext;
    @BindView(R.id.popup_data_title) TextView title;
    @BindView(R.id.popup_cancel) Button cancel;
    @BindView(R.id.popup_date_submit) Button submit;
    @BindView(R.id.year_wheel) WheelView year;
    @BindView(R.id.month_wheel) WheelView month;
    @BindView(R.id.day_wheel) WheelView day;

    private DayWheelAdapter dAdapter;
    private ArrayList<String> date_d;

    private int startYear = 1990;
    private int currentYear;
    private int currentMonth;
    private int currentDay;

    private Calendar calendar;

    public DatePopupWindow(FragmentActivity context, View.OnClickListener itemsOnClick) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mMenuView = inflater.inflate(R.layout.date_wheel_picker, null);
        ButterKnife.bind(this, mMenuView);

        initDate();
        initView();
        updateDaysArray();

        NumericWheelAdapter yAdapter = new NumericWheelAdapter(context, startYear, startYear + 100);
        NumericWheelAdapter mAdapter = new NumericWheelAdapter(context, 1, 12);
        dAdapter = new DayWheelAdapter(context,date_d);

        yAdapter.setTextSize(22);
        mAdapter.setTextSize(22);
        dAdapter.setTextSize(22);

        year.setViewAdapter(yAdapter);
        year.setCurrentItem(currentYear-startYear);

        month.setViewAdapter(mAdapter);
        month.setCurrentItem(currentMonth-1);

        day.setViewAdapter(dAdapter);
        day.setCurrentItem(currentDay-1);

        year.addChangingListener(this);
        month.addChangingListener(this);
        day.addChangingListener(this);

        submit.setOnClickListener(itemsOnClick);
        cancel.setOnClickListener(itemsOnClick);
        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorGrey100)));
        this.setOutsideTouchable(false);
        this.setFocusable(false);
    }

    private void initView() {
        Utility.setWheelViewStyle(year);
        Utility.setWheelViewStyle(month);
        Utility.setWheelViewStyle(day);

        year.setVisibleItems(5);
        month.setVisibleItems(5);
        day.setVisibleItems(5);

        year.setCyclic(true);
        month.setCyclic(true);
        day.setCyclic(true);
    }

    private void initDate() {
        calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH)+1;
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);

        currentYear = nowYear;
        currentMonth = nowMonth;
        currentDay = nowDay;
    }

    private void updateDaysArray(){
        calendar.set(currentYear, currentMonth-1, 1);
        int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int startDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)-1;
        date_d = new ArrayList<>();
        String daysArray[] = mContext.getResources().getStringArray(R.array.days_array);

        for (int i = 1; i <= daysOfMonth; i++) {
            date_d.add( i+ " "+ daysArray[startDayOfMonth%7]);
            startDayOfMonth++;
        }
        dAdapter = new DayWheelAdapter(mContext,date_d);
        dAdapter.setTextSize(22);
        day.setViewAdapter(dAdapter);
    }


    public int getCurrentYear() {
        return currentYear;
    }

    public String getCurrentMonth() {
        return new DecimalFormat("00").format(currentMonth);
    }

    public String getCurrentDay() {
        return new DecimalFormat("00").format(currentDay);
    }

    @Override
    public void onChanged(WheelView wheelView, int i, int i1) {
        if (wheelView == year) {
            currentYear = year.getCurrentItem()+startYear;
            updateDaysArray();
        } else if (wheelView == month) {
            currentMonth = month.getCurrentItem()+1;
            updateDaysArray();
         } else if (wheelView == day) {
            currentDay = day.getCurrentItem()+1;
        }
    }

    public void setDateWheel(long dateInLong) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInLong);
        year.setCurrentItem(calendar.get(Calendar.YEAR)-startYear);
        month.setCurrentItem(calendar.get(Calendar.MONTH)+1);
        day.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH)-1);
    }

    public void reloadDate() {
        setDateWheel(formatDateStringAsLong(getCurrentDate()));
    }
}
