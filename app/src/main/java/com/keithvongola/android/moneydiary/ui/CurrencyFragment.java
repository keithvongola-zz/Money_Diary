package com.keithvongola.android.moneydiary.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.keithvongola.android.moneydiary.adapter.CurrencyRecyclerViewAdapter;
import com.keithvongola.android.moneydiary.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.databases.MoneyContract.CurrencyEntry;

public class CurrencyFragment extends Fragment
        implements LoaderCallbacks<Cursor> {
    @BindView(R.id.currency_recycle_list) FastScrollRecyclerView recyclerView;

    private static final int CURRENCY_LOADER = 0;
    private Uri currencyURI = CurrencyEntry.CONTENT_URI;
    private CurrencyRecyclerViewAdapter currencyRecyclerViewAdapter;

    public CurrencyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CURRENCY_LOADER,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_currency_list, container, false);
        ButterKnife.bind(this,rootView);

        currencyRecyclerViewAdapter = new CurrencyRecyclerViewAdapter(null,getContext());
        recyclerView.setAdapter(currencyRecyclerViewAdapter);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                currencyURI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        currencyRecyclerViewAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        currencyRecyclerViewAdapter.setData(null);
    }
}
