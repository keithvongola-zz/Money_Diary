package com.keithvongola.android.moneydiary.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.Backable;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.adapter.AccountsExpandableListAdapter;
import com.keithvongola.android.moneydiary.pojo.Account;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.moneyConversion;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.Utility.setListViewHeight;
import static com.keithvongola.android.moneydiary.Utility.tintMenuIcon;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;

public class AccountsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, Backable {
    @BindView(R.id.AccountsExpandableListView) ExpandableListView accountsExpandableListView;
    @BindView(R.id.empty_view) TextView emptyAccountsView;
    @BindView(R.id.toolbar_info_balance) TextView balanceView;
    @BindView(R.id.toolbar_info_current_asset) TextView currentAssetView;
    @BindView(R.id.toolbar_info_saving) TextView savingView;
    @BindView(R.id.toolbar_info_current_liability) TextView currentLiabilityView;
    @BindView(R.id.toolbar_info_long_term_liability) TextView longTermLiabilityView;

    private static final int ACCOUNT_LOADER = 0;
    private AccountsExpandableListAdapter accountsExpandableAdapter;

    public AccountsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(ACCOUNT_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_accounts, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem menuItemEdit = menu.findItem(R.id.action_edit_account);
        MenuItem menuItemDelete = menu.findItem(R.id.action_delete_account);

        if (menuItemEdit != null)
            tintMenuIcon(getActivity(), menuItemEdit, R.color.colorGrey400);

        if (menuItemDelete != null)
            tintMenuIcon(getActivity(), menuItemDelete, R.color.colorGrey400);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();

        if (accountsExpandableAdapter.getEditBtn() | accountsExpandableAdapter.getDeleteBtn()) {
            menu.clear();
            menuInflater.inflate(R.menu.menu_done, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_account:
                addAccount();
                return true;

            case R.id.action_edit_account:
                accountsExpandableAdapter.setEditBtn(true);
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.action_delete_account:
                accountsExpandableAdapter.setDeleteBtn(true);
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.action_done:
                accountsExpandableAdapter.resetEditAndDelete();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_accounts));

        accountsExpandableAdapter = new AccountsExpandableListAdapter(getActivity(), null, null);
        accountsExpandableListView.setFocusable(false);
        accountsExpandableListView.setAdapter(accountsExpandableAdapter);
        accountsExpandableListView.setGroupIndicator(null);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                AccountsEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Account list for getting account's type
        List<Account> parentList = new ArrayList<>();
        String  mainCurrency = getMainCurrency(getActivity());
        MonetaryAmount currentAssets = Money.of(new BigDecimal("0"), mainCurrency);
        MonetaryAmount savingAmount = Money.of(new BigDecimal("0"), mainCurrency);
        MonetaryAmount currentLiability = Money.of(new BigDecimal("0"), mainCurrency);
        for (int i = 0; i < 7; i++) {
            Account accountType = new Account(i);
            parentList.add(accountType);
        }

        //Initial list for different account type
        List<List<Account>> childList = new ArrayList<>();
        List<Account> child;
        Account account;
        for (int i = 0; i < 7; i++) {
            child = new ArrayList<>();
            childList.add(child);
            accountsExpandableListView.expandGroup(i);
        }

        //Add accounts to list by type
        if (data.moveToFirst()) {
            for (int i = 0; i < data.getCount(); i++) {
                account = new Account(data);
                childList.get(account.getType()).add(account);
                data.moveToNext();
                MonetaryAmount amount = account.getCurrent();
                if (!amount.getCurrency().toString().equals(mainCurrency))
                    amount = moneyConversion(getActivity(), amount, mainCurrency);

                if (account.getType() == 2 | account.getType() == 4)
                   currentLiability = currentLiability.add(amount);
                else
                   currentAssets = currentAssets.add(amount);

                if (account.getSaving().signum() > 0) {
                    MonetaryAmount saving = account.getSaving();
                    if (!saving.getCurrency().toString().equals(mainCurrency))
                        saving = moneyConversion(getActivity(), saving, mainCurrency);
                        savingAmount = savingAmount.add(saving);
                }
            }
            emptyAccountsView.setVisibility(View.GONE);
        } else {
            emptyAccountsView.setVisibility(View.VISIBLE);
        }

        accountsExpandableAdapter.setData(parentList,childList);
        accountsExpandableListView.setDividerHeight(0);
        accountsExpandableListView.setHeaderDividersEnabled(false);
        setListViewHeight(accountsExpandableListView);

        //Set total balanceView text
        setAmountWithColor(getActivity(), balanceView, currentAssets.add(savingAmount));
        setAmountWithColor(getActivity(), currentAssetView, currentAssets);
        setAmountWithColor(getActivity(), savingView, savingAmount);
        setAmountWithColor(getActivity(), currentLiabilityView, currentLiability);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        accountsExpandableAdapter.setData(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(ACCOUNT_LOADER, null, this);
    }


    @Override
    public boolean onBackPressed() {
        boolean back = accountsExpandableAdapter.getSwipeStatus();
        getActivity().invalidateOptionsMenu();
        return back;
    }

    @OnClick(R.id.empty_view)
    public void addAccount(){
        AccountEditFragment fragment = new AccountEditFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out)
                .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                .addToBackStack(null).commit();
    }
}
