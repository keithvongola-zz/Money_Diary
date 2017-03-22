package com.keithvongola.android.moneydiary.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.ui.TransactionActivity;

import static com.keithvongola.android.moneydiary.Utility.ARG_PAGE;


public class TransactionsWidgetProvider extends AppWidgetProvider{
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            Intent expenseIntent = new Intent(context, TransactionActivity.class);
            expenseIntent.putExtra(ARG_PAGE, 0);
            PendingIntent expensePendingIntent = PendingIntent.getActivity(context, 0, expenseIntent, 0);

            Intent incomeIntent = new Intent(context, TransactionActivity.class);
            incomeIntent.putExtra(ARG_PAGE, 1);
            PendingIntent incomePendingIntent = PendingIntent.getActivity(context, 0, incomeIntent, 0);

            Intent transferIntent = new Intent(context, TransactionActivity.class);
            incomeIntent.putExtra(ARG_PAGE, 2);
            PendingIntent transferPendingIntent = PendingIntent.getActivity(context, 0, transferIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.money_diary_widget);
            views.setOnClickPendingIntent(R.id.expense_layout, expensePendingIntent);
            views.setOnClickPendingIntent(R.id.income_layout, incomePendingIntent);
            views.setOnClickPendingIntent(R.id.transfer_layout, transferPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
