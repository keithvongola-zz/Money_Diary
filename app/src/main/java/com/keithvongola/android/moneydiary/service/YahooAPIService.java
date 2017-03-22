package com.keithvongola.android.moneydiary.service;

import com.keithvongola.android.moneydiary.pojo.ResponseGetQuery;
import com.keithvongola.android.moneydiary.pojo.Results;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YahooAPIService {
    @GET("v1/public/yql")
    Call<Results> getExchangeRate(@Query("q") String query, @Query("diagnostics") String diagnostics,
                                  @Query("format") String format, @Query("env") String env);

    @GET("v1/public/yql")
    Call<ResponseGetQuery> getResponseGetQuery(@Query("q") String query, @Query("diagnostics") String diagnostics,
                                           @Query("format") String format, @Query("env") String env);


}
