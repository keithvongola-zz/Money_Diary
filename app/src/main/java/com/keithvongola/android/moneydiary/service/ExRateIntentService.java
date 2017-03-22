package com.keithvongola.android.moneydiary.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;


public class ExRateIntentService extends IntentService {

    public ExRateIntentService() {
        super(ExRateIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(ExRateIntentService.class.getSimpleName(), "Exchange Rate Intent Service");
        ExRateTaskService exchangeRateTaskService = new ExRateTaskService(this);

        exchangeRateTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), null));
    }

}
