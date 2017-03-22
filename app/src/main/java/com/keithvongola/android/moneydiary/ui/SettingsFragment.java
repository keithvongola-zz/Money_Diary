package com.keithvongola.android.moneydiary.ui;


import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;

import com.keithvongola.android.moneydiary.Backable;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, Backable {
    Preference main_currency_pref, decimal_place_pref;
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_setting);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.pref_main_currency_key));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_exchange_rate_key));
        onSharedPreferenceChanged(sharedPreferences,getString(R.string.pref_decimal_place_key));

        main_currency_pref = findPreference(getString(R.string.pref_main_currency_key));
        decimal_place_pref = findPreference(getString(R.string.pref_decimal_place_key));

        ((PreferenceGroup) findPreference(getString(R.string.pref_group_currency))).removePreference(decimal_place_pref);

        findPreference(getString(R.string.pref_main_currency_key)).setSummary(main_currency_pref.getSharedPreferences()
                .getString(getResources().getString(R.string.pref_main_currency_key),
                        getResources().getString(R.string.pref_main_currency_default)));

        main_currency_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(),CurrencyActivity.class);
                startActivityForResult(intent,2);
                return true;
            }
        });

        findPreference(getString(R.string.pref_exchange_rate_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main_container,new ExRateFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor ed = sharedPref.edit();
                String currency = data.getStringExtra("country");
                ed.putString(getResources().getString(R.string.pref_main_currency_key), currency).apply();

                //Update Main Budgets currency
                ContentValues cv = new ContentValues();
                cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_CURRENCY, currency);
                getActivity().getContentResolver().update(MainBudgetsEntry.CONTENT_URI,
                        cv, null, null);
                //Update Sub Budgets currency
                cv = new ContentValues();
                cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_CURRENCY, currency);
                getActivity().getContentResolver().update(SubBudgetsEntry.CONTENT_URI,
                        cv, null, null);
                //Update Plans currency
                cv = new ContentValues();
                cv.put(PlansEntry.COLUMN_PLANS_CURRENCY, currency);
                getActivity().getContentResolver().update(PlansEntry.CONTENT_URI,
                        cv, null, null);cv = new ContentValues();
                cv.put(SubPlansEntry.COLUMN_PLANS_CURRENCY, currency);
                getActivity().getContentResolver().update(SubPlansEntry.CONTENT_URI,
                        cv, null, null);


                ed.putString(getResources().getString(R.string.pref_decimal_place_key),data.getStringExtra("unit")).apply();
                onSharedPreferenceChanged(sharedPref, getString(R.string.pref_main_currency_key));
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        if (activity != null && key.equals(getResources().getString(R.string.pref_main_currency_key))){
            main_currency_pref = findPreference(getString(R.string.pref_main_currency_key));

            main_currency_pref
                    .setSummary(sharedPreferences.getString(
                    getResources().getString(R.string.pref_main_currency_key),
                    getResources().getString(R.string.pref_main_currency_default)));

            Utility.startExRateIntent(activity);
            BackupManager bm = new BackupManager(getActivity());
            bm.dataChanged();
            }

    }

    @Override
    public void onResume(){
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_setting);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
