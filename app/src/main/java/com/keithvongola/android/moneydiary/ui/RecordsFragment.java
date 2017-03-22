package com.keithvongola.android.moneydiary.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.adapter.TransactionsExpandableAdapter;
import com.keithvongola.android.moneydiary.Backable;
import com.keithvongola.android.moneydiary.pojo.Transaction;
import com.keithvongola.android.moneydiary.views.LoadMoreFooterView;
import com.keithvongola.android.moneydiary.views.RefreshHeaderView;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.moneyConversion;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.Utility.setListViewHeight;
import static com.keithvongola.android.moneydiary.Utility.tintMenuIcon;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry.buildTransactionUriWithAccountIDAndDate;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry.buildTransactionUriWithDate;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry.getTransactionAccountFromUri;

public class RecordsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Backable,
        OnRefreshListener, OnLoadMoreListener,ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupExpandListener{
    @BindView(R.id.exp) ExpandableListView transactionsExpListView;
    @BindView(R.id.swipeToLoadLayout) SwipeToLoadLayout swipeToLoadLayout;
    @BindView(R.id.swipe_refresh_header) RefreshHeaderView swipeRefreshHeader;
    @BindView(R.id.swipe_load_more_footer) LoadMoreFooterView swipeLoadMoreFooter;
    @BindView(R.id.swipe_target) NestedScrollView nestedScrollView;
    @BindView(R.id.empty_view) TextView emptyTextView;
    @BindView(R.id.toolbar_transactions_balance) TextView balanceView;
    @BindView(R.id.toolbar_transactions_expense) TextView expenseView;
    @BindView(R.id.toolbar_transactions_income) TextView incomeView;

    private static final int TRANSACTIONS_LOADER = 0;
    private boolean haveAccountId;
    private int mAccountId;
    private int currentYear;
    private Uri transactionsUri;

    private TransactionsExpandableAdapter transactionsExpAdapter;

    public RecordsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        currentYear = getCurrentYear();
        if(getArguments()!=null){
            transactionsUri = getArguments().getParcelable(ARG_URI);
            haveAccountId = true;
            mAccountId = Integer.parseInt(getTransactionAccountFromUri(transactionsUri));
        } else {
            transactionsUri = buildTransactionUriWithDate(currentYear+"-01-01",currentYear+"-12-31");
            haveAccountId = false;
        }

        getLoaderManager().initLoader(TRANSACTIONS_LOADER,null,this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_records, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem menuItemEdit = menu.findItem(R.id.action_edit_transaction);
        MenuItem menuItemDelete = menu.findItem(R.id.action_delete_transaction);

        if (menuItemEdit != null)
            tintMenuIcon(getActivity(), menuItemEdit, R.color.colorGrey400);

        if (menuItemDelete != null)
            tintMenuIcon(getActivity(), menuItemDelete, R.color.colorGrey400);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();

        if (transactionsExpAdapter.getEditBtn() | transactionsExpAdapter.getDeleteBtn()) {
            menu.clear();
            menuInflater.inflate(R.menu.menu_done, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_account:
                AccountEditFragment fragment = new AccountEditFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
                return true;

            case R.id.action_edit_transaction:
                if (!transactionsExpAdapter.isGroupsEmpty()) {
                    transactionsExpAdapter.setEditBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_delete_transaction:
                if (!transactionsExpAdapter.isGroupsEmpty()) {
                    transactionsExpAdapter.setDeleteBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_done:
                transactionsExpAdapter.resetEditAndDelete();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_records, container, false);
        ButterKnife.bind(this,rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_records);

        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);

        emptyTextView.setVisibility(View.GONE);

        //init ExpandableListView
        transactionsExpAdapter = new TransactionsExpandableAdapter(getActivity(),null);
        transactionsExpListView.setAdapter(transactionsExpAdapter);
        transactionsExpListView.setGroupIndicator(null);
        transactionsExpListView.setDividerHeight(0);
        transactionsExpListView.setHeaderDividersEnabled(false);
        transactionsExpListView.setOnGroupCollapseListener(this);
        transactionsExpListView.setOnGroupExpandListener(this);

        nestedScrollView.getViewTreeObserver().addOnScrollChangedListener((new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (nestedScrollView.canScrollVertically(-1))
                    swipeToLoadLayout.setRefreshEnabled(false);
                else //Enable Refresh when top is reached
                    swipeToLoadLayout.setRefreshEnabled(true);

                if (nestedScrollView.canScrollVertically(1) && emptyTextView.getWindowVisibility()==View.GONE)
                    swipeToLoadLayout.setLoadMoreEnabled(false);
                else //Enable Load More when bottom is reached
                    swipeToLoadLayout.setLoadMoreEnabled(true);
            }
        }));
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                transactionsUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshHeader.setOnMoveSwipeStr(getString(R.string.refresh_swipe) + (currentYear+1) + getString(R.string.swipe_transaction_records));
        swipeRefreshHeader.setOnMoveReleaseStr(getString(R.string.refresh_release)  + (currentYear+1) + getString(R.string.swipe_transaction_records));
        swipeLoadMoreFooter.setOnMoveSwipeStr(getString(R.string.load_more_swipe) + (currentYear-1) +  getString(R.string.swipe_transaction_records));
        swipeLoadMoreFooter.setOnMoveReleaseStr(getString(R.string.load_more_release)  + (currentYear-1) + getString(R.string.swipe_transaction_records));
        List<List<Transaction>> childList = new ArrayList<>();
        List<Transaction> child;

        //Initial list for transactions in different months
        for (int i = 0; i < 12; i++) {
            child = new ArrayList<>();
            childList.add(child);
        }

        if (data.moveToFirst()) {
            transactionsExpListView.setVisibility(View.VISIBLE);
            //Hide emptyTextView
            emptyTextView.setVisibility(View.GONE);
            String currency = getMainCurrency(getActivity());
            MonetaryAmount totalIncome = Money.of(new BigDecimal(0), currency);
            MonetaryAmount totalExpense = Money.of(new BigDecimal(0), currency);

            for (int i=0; i < data.getCount(); i++) {
                Transaction transaction = new Transaction(data);
                childList.get(11-transaction.getMonth()).add(transaction);
                MonetaryAmount amount = transaction.getAmount();
                if (!amount.getCurrency().toString().equals(currency))
                    amount = moneyConversion(getActivity(), amount, currency);
                if (transaction.getAmount().signum() > 0)
                    totalIncome = totalIncome.add(amount);
                else
                    totalExpense = totalExpense.add(amount);

                data.moveToNext();
            }

            MonetaryAmount balance = totalIncome.add(totalExpense);

            setAmount(balanceView, balance);
            setAmount(incomeView, totalIncome);
            setAmount(expenseView, totalExpense);
        } else {
            //Show emptyTextView if expandableListView is empty
            transactionsExpListView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);

            balanceView.setText(getString(R.string.hint_empty));
            expenseView.setText(getString(R.string.hint_empty));
            incomeView.setText(getString(R.string.hint_empty));
        }
        transactionsExpAdapter.setData(childList);

        //Expand the first group of ExpandableListView
        int firstVisibleGroup = transactionsExpAdapter.getFirstVisibleGroup();
        if(firstVisibleGroup != -1) transactionsExpListView.expandGroup(firstVisibleGroup);


        if(swipeToLoadLayout.isRefreshing()) swipeToLoadLayout.setRefreshing(false);
        else if (swipeToLoadLayout.isLoadingMore()) swipeToLoadLayout.setLoadingMore(false);

        nestedScrollView.smoothScrollTo(0,0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        transactionsExpAdapter.setData(null);
    }

    @Override
    public void onRefresh() {
        currentYear++;
        if(haveAccountId){
            transactionsUri = buildTransactionUriWithAccountIDAndDate(mAccountId,
                    currentYear+"-01-01",currentYear+"-12-31");
        } else {
            transactionsUri = buildTransactionUriWithDate(currentYear + "-01-01", currentYear + "-12-31");
        }
        getLoaderManager().restartLoader(TRANSACTIONS_LOADER, null, this);
    }

    @Override
    public void onLoadMore() {
        currentYear--;
        if(haveAccountId){
            transactionsUri = buildTransactionUriWithAccountIDAndDate(mAccountId,
                    currentYear+"-01-01",currentYear+"-12-31");
        } else {
            transactionsUri = buildTransactionUriWithDate(currentYear+"-01-01",currentYear+"-12-31");
        }
        getLoaderManager().restartLoader(TRANSACTIONS_LOADER,null,this);
    }


    @Override
    public boolean onBackPressed() {
        boolean back = transactionsExpAdapter.getSwipeStatus();
        getActivity().invalidateOptionsMenu();
        return back;
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        setListViewHeight(transactionsExpListView);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        setListViewHeight(transactionsExpListView);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TRANSACTIONS_LOADER,null,this);
    }
}
