package com.keithvongola.android.moneydiary.ui;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.adapter.ExRateRecycleViewAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.ExchangeRateEntry;

public class ExRateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.exchange_rate_recycle_list) FastScrollRecyclerView recyclerView;
    private static final int EXCHANGE_RATE_LOADER = 0;
    private ExRateRecycleViewAdapter exRateRecycleViewAdapter;

    public ExRateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(EXCHANGE_RATE_LOADER,null,this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_exchange_rate, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh){
           //Refresh exchange rate from Yahoo Finance API
           Utility.startExRateIntent(getActivity());
           return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exchange_rate, container, false);
        ButterKnife.bind(this,rootView);

        //Set title as "{MAIN_CURRENCY}/..."
        ((AppCompatActivity) getActivity())
                .getSupportActionBar().setTitle(Utility.getMainCurrency(getActivity())+"/...");

        exRateRecycleViewAdapter = new ExRateRecycleViewAdapter(null,getContext());
        recyclerView.setAdapter(exRateRecycleViewAdapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.hideKeyboard(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                ExchangeRateEntry.CONTENT_URI,
                null,
                null,
                new String[]{Utility.getMainCurrency(getActivity())},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        exRateRecycleViewAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        exRateRecycleViewAdapter.setData(null);
    }

}
