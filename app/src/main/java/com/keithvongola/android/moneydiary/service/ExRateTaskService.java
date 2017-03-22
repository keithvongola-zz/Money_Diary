package com.keithvongola.android.moneydiary.service;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.pojo.Rate;
import com.keithvongola.android.moneydiary.pojo.ResponseGetQuery;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sExchangeRateIsManualSelection;

public class ExRateTaskService extends GcmTaskService {
    private String LOG_TAG = ExRateTaskService.class.getSimpleName();
    private Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVICE_STATUS_OK,
            SERVICE_STATUS_SERVER_DOWN,
            SERVICE_STATUS_INVALID_EXCHANGE_RATE,
            SERVICE_STATUS_SERVER_INVALID})

    public @interface ServiceStatus {}
    public static final int SERVICE_STATUS_OK = 0;
    public static final int SERVICE_STATUS_INVALID_EXCHANGE_RATE = 1;
    public static final int SERVICE_STATUS_SERVER_DOWN = 2;
    public static final int SERVICE_STATUS_SERVER_INVALID = 3;

    public ExRateTaskService(){}

    public ExRateTaskService(Context context){
        mContext = context;
    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        if (mContext == null){
            mContext = this;
        }

        setServiceStatus(mContext,SERVICE_STATUS_OK);

        final int[] result = {GcmNetworkManager.RESULT_FAILURE};
        if (taskParams.getTag().equals("init") || taskParams.getTag().equals("periodic")) {
            OkHttpClient okClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://query.yahooapis.com/")
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            YahooAPIService yahooAPIService = retrofit.create(YahooAPIService.class);

            String exchangeNameString = "";

            final ArrayList exchangeNameList = Utility.getExchangeCurrencyName(mContext);
            for (int i = 0; i < exchangeNameList.size(); i++) {
                exchangeNameString = exchangeNameString + "\""+ exchangeNameList.get(i) + "\",";
            }
            String q = "select * from yahoo.finance.xchange where pair in ("
                    + exchangeNameString.substring(0, exchangeNameString.length()-1) + ")";
            String format = "json";
            String diagnostics = "true";
            String env = "store://datatables.org/alltableswithkeys";

            Call<ResponseGetQuery> exchangeRateCall = yahooAPIService.getResponseGetQuery(q, diagnostics, format, env);
            exchangeRateCall.enqueue(new Callback<ResponseGetQuery>(){
                @Override
                public void onResponse(Call<ResponseGetQuery> call, retrofit2.Response<ResponseGetQuery> response) {
                    if(response.body() != null) {
                        result[0] = GcmNetworkManager.RESULT_SUCCESS;
                        Log.d(LOG_TAG, "Starting sync Exchange Rate");

                        List<Rate> exRateList = response.body().getResult().getResults().getRate();
                        Vector<ContentValues> cVVector = new Vector<ContentValues>(exRateList.size());
                        for (int i=0; i<exRateList.size(); i++ ){
                            ContentValues values = new ContentValues();
                            Rate rate = exRateList.get(i);

                            values.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE_ID, rate.getId());
                            values.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE_NAME, rate.getName());
                            values.put(ExchangeRateEntry.COLUMN_EXCHANGE_ASK, rate.getAsk());

                            if(!rate.getBid().equals("N/A")){
                                values.put(ExchangeRateEntry.COLUMN_EXCHANGE_BID,rate.getBid());
                            } else {
                                values.put(ExchangeRateEntry.COLUMN_EXCHANGE_BID,rate.getBid());
                            }

                            cVVector.add(values);
                        }
                        if ( cVVector.size() > 0 ) {
                            mContext.getContentResolver().delete(ExchangeRateEntry.CONTENT_URI,
                                    sExchangeRateIsManualSelection,
                                    new String[]{"0"});
                            ContentValues[] cvArray = new ContentValues[cVVector.size()];
                            cVVector.toArray(cvArray);
                            mContext.getContentResolver().bulkInsert(ExchangeRateEntry.CONTENT_URI,
                                    cvArray);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseGetQuery> call, Throwable t) {
                    Log.d("Retrofit", " Get exchange rate fail");
                }
            });
        }
        return result[0];

    }

    static public void setServiceStatus(Context c, @ServiceStatus int serviceStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_service_status_key), serviceStatus);
        spe.apply();
    }
}
