package com.keithvongola.android.moneydiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.databases.MoneyContract;
import com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;
import com.keithvongola.android.moneydiary.service.ExRateIntentService;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import kankan.wheel.widget.WheelView;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.CurrencyEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry.buildExchangeRateUri;


public class Utility {
    public static final String FRAG_TAG_EDIT = "EDIT";
    public static final String FRAG_TAG_NAV= "NAV";
    public static final String FRAG_TAG_TF = "transaction_fragment";

    public static final String ARG_ACCOUNT = "ACCOUNT";
    public static final String ARG_BUDGET = "BUDGET";
    public static final String ARG_BUDGET_PARENT_ID = "ID";
    public final static String ARG_EX_RATE_TITLE = "EX_RATE_TITLE";
    public final static String ARG_EX_RATE_AUTO = "EX_RATE_AUTO";
    public final static String ARG_EX_RATE_MANUAL = "EX_RATE_MANUAL";
    public static final String ARG_IS_EXPENSE = "IS_EXPENSE";
    public static final String ARG_IS_MAIN_BUDGET = "IS_MAIN_BUDGET";
    public final static String ARG_IS_MANUAL = "IS_MANUAL";
    public static final String ARG_IS_MAIN_PLAN = "ISMAINPLAN";
    public static final String ARG_PAGE = "EXTRA_PAGE";
    public static final String ARG_PARENT_ID = "PARENTID";
    public static final String ARG_PLAN = "PLAN";
    public static final String ARG_SUB_PLAN = "SUBPLAN";
    public static final String ARG_TRANSACTION = "TRANSACTION";
    public static final String ARG_URI = "URI";

    public static final String INTENT_EXTRA_COUNTRY = "country";

    public static MonetaryAmountFormat formatWithCurrency = MonetaryFormats.getAmountFormat(
            AmountFormatQueryBuilder.of(Locale.getDefault())
                    .set(CurrencyStyle.SYMBOL)
                    .build());

    private static DecimalFormat decimalFormatter = new DecimalFormat("#,###.###");
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_ISO8601 = "dd-MM-yyyy XXX";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
    private static final SimpleDateFormat dateFormatIso = new SimpleDateFormat(DATE_FORMAT_ISO8601, Locale.ENGLISH);

    private static int[] SHADOWS_COLORS = new int[] { 0xFFFFFFFF, 0x00FFFFFF,
            0x00FFFFFF };

    public static void setWheelViewStyle(WheelView wheelView){
        wheelView.setShadowColor(SHADOWS_COLORS[0],SHADOWS_COLORS[1],SHADOWS_COLORS[2]);
        wheelView.setWheelBackground(R.color.colorWhite);
        wheelView.setWheelForeground(R.color.wheelCurtainColor);
    }

    public static String getMainCurrency(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_main_currency_key),
                context.getString(R.string.pref_main_currency_default));
    }

    public static long formatDateStringAsLong(String dateStr){
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormatIso.parse(dateStr+" -04:00"));
            return calendar.getTimeInMillis();
        }
        catch (ParseException e) {
            e.printStackTrace();
            return Long.parseLong(null);
        }
    }

    public static String getCalendarFromFormattedLong(long l){
        Date date = new Date(l);
        return dateFormat.format(date);
    }

    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH)+1;
    }

    public static int getCurrentMonth(long l) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l);
        return calendar.get(Calendar.MONTH)+1;
    }

    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null){
            return;
        }
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void hideKeyboard(Context ctx, View v) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static int accountTypeResId(int i){
        switch (i){
            case 0:
                return R.string.account_cash;
            case 1:
                return R.string.account_banking;
            case 2:
                return R.string.account_credit_cards;
            case 3:
                return R.string.account_stocks_funds;
            case 4:
                return R.string.account_debts;
            case 5:
                return R.string.account_claims;
            case 6:
                return R.string.account_others;
            default:
                throw new UnsupportedOperationException("Unknown account type");
        }
    }

    //Set height for expandable list view dynamically
    public static void setListViewHeight(ExpandableListView listView) {
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            if (groupItem.getVisibility()==View.VISIBLE) {
                groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += groupItem.getMeasuredHeight();
                if (listView.isGroupExpanded(i)) {
                    for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                        View listItem = listAdapter.getChildView(i, j, false, null,
                                listView);
                        listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                        totalHeight += listItem.getMeasuredHeight();
                    }
                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setAmount( TextView tv, MonetaryAmount amount){
        tv.setText(formatWithCurrency.format(amount));
    }

    public static void setAmountWithColor(Context c, TextView tv, MonetaryAmount amount){
        if (amount.signum() > 0)
            tv.setTextColor(ContextCompat.getColor(c, R.color.colorPrimary));
        else if (amount.signum() < 0)
            tv.setTextColor(ContextCompat.getColor(c, R.color.colorDangerButton));
        else
            tv.setTextColor(ContextCompat.getColor(c, android.R.color.tertiary_text_dark));

        tv.setText(formatWithCurrency.format(amount));
    }


    public static ArrayList getExchangeCurrencyName(Context context){
        Cursor cursor = context.getContentResolver().query(CurrencyEntry.CONTENT_URI,
                new String[]{CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE},
                null,
                null,
                null);

        ArrayList<String> exchangeCurrencyName = new ArrayList<>();
        String mainCurrency = getMainCurrency(context);
        if (cursor !=null && cursor.moveToFirst()) {
            for (int i=0; i<cursor.getCount(); i++) {
                if (!cursor.getString(0).equals(mainCurrency))
                    exchangeCurrencyName.add(mainCurrency+cursor.getString(0));
                cursor.moveToNext();
            }
        cursor.close();
        }

        return exchangeCurrencyName;
    }

    public static void startExRateIntent(Context context){
        Intent mServiceIntent = new Intent(context, ExRateIntentService.class);
        mServiceIntent.putExtra("tag", "init");
        context.startService(mServiceIntent);
    }

    public static Integer getMainCurrencyUnit(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = sharedPreferences.getString(
                context.getResources().getString(R.string.pref_decimal_place_key),
                context.getResources().getString(R.string.pref_decimal_place_default));

        return Integer.valueOf(defaultValue);
    }

    public static String bigDecimalToDbVal(Context context, BigDecimal bigDecimal, String currencyAlphaCode){
        Cursor c = context.getContentResolver().query(
                MoneyContract.CurrencyEntry.CONTENT_URI,
                new String[]{MoneyContract.CurrencyEntry.TABLE_NAME + "." + MoneyContract.CurrencyEntry.COLUMN_CURRENCY_UNIT},
                MoneyContract.CurrencyEntry.TABLE_NAME + "." + MoneyContract.CurrencyEntry.COLUMN_CURRENCY_ALPHA_CODE + " =? ",
                new String[]{currencyAlphaCode},
                null,
                null);

        int currencyUnit = 0;
        if (c!=null && c.moveToFirst()) {
            currencyUnit = c.getInt(0);
            c.close();
        }

        return bigDecimal.setScale(currencyUnit,BigDecimal.ROUND_HALF_UP)
                .movePointRight(currencyUnit).toBigIntegerExact().toString();
    }

    public static String bigDecimalToDbVal(MonetaryAmount amount){
        int currencyUnit = amount.getCurrency().getDefaultFractionDigits();
        return amount.scaleByPowerOfTen(currencyUnit).divideToIntegralValue(1).getNumber().toString();
    }

    public static String bigDecimalToDbVal(BigDecimal bigDecimal, Integer currencyUnit){
       return bigDecimal.setScale(currencyUnit,BigDecimal.ROUND_HALF_UP)
                .movePointRight(currencyUnit).toBigIntegerExact().toString();
    }

    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        normalDrawable.mutate();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context,color));

        item.setIcon(wrapDrawable);
    }

    public static int getProgress(MonetaryAmount current, MonetaryAmount base){
        Double currentVal = Double.valueOf(current.getNumber().toString())*100;
        Double baseVal = Double.valueOf(base.getNumber().toString());
        return (int) (currentVal/baseVal);
    }

    public static MonetaryAmount moneyConversion(Context context, MonetaryAmount amount, String targetCurrency){
        if (amount.signum() == 0) return Money.of(0, targetCurrency);

        Cursor c = context.getContentResolver().query(buildExchangeRateUri(targetCurrency + amount.getCurrency().toString()),
                new String[]{ExchangeRateEntry.TABLE_NAME  + "." + ExchangeRateEntry.COLUMN_EXCHANGE_IS_MANUAL,
                        ExchangeRateEntry.TABLE_NAME  + "." + ExchangeRateEntry.COLUMN_EXCHANGE_ASK,
                        ExchangeRateEntry.TABLE_NAME  + "." + ExchangeRateEntry.COLUMN_EXCHANGE_MANUAL_ASK},
                ExchangeRateEntry.TABLE_NAME  + "." + ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID + " =? ",
                null,
                null);

        double targetAmount = Double.valueOf(amount.getNumber().toString());
        if (c.moveToFirst()) {
            if(c.getDouble(1) > 0 | c.getDouble(2) > 0) {
                double rate = c.getInt(0) == 0 ? c.getDouble(1) : c.getDouble(2);
                targetAmount /= rate;
            } else {
                return Money.of(0, targetCurrency);
            }
            c.close();
        }

        return Money.of(new BigDecimal(targetAmount),targetCurrency);
    }

    public static String getAmountStr(MonetaryAmount amount){
        return decimalFormatter.format(Double.parseDouble(amount.getNumber().toString()));
    }

    public static final int[] pieColors = {
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(254, 247, 120),
            Color.rgb(106, 167, 134), Color.rgb(53, 194, 209), Color.rgb(64, 89, 128),
            Color.rgb(149, 165, 124), Color.rgb(217, 184, 162), Color.rgb(191, 134, 134),
            Color.rgb(179, 48, 80), Color.rgb(193, 37, 82), Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0), Color.rgb(106, 150, 31), Color.rgb(179, 100, 53),
            Color.rgb(192, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
            Color.rgb(140, 234, 255), Color.rgb(255, 140, 157)
    };
}
