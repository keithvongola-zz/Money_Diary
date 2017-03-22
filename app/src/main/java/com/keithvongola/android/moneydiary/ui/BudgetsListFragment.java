package com.keithvongola.android.moneydiary.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.adapter.BudgetsSwipeAdapter;
import com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import com.keithvongola.android.moneydiary.pojo.Budget;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.keithvongola.android.moneydiary.Utility.ARG_BUDGET_PARENT_ID;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_EXPENSE;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_BUDGET;
import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.Utility.tintMenuIcon;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.getSubBudgetsIdParentIDFromUri;


public class BudgetsListFragment extends Fragment implements LoaderCallbacks<Cursor> {
    @BindView(R.id.budgets_listview) RecyclerView budgetsListView;
    @BindView(R.id.toolbar_budget_info_title_balance) TextView tvTitleBudgetTotal;
    @BindView(R.id.toolbar_budget_info_balance) TextView tvTotalBudget;
    @BindView(R.id.toolbar_budget_info_title_right) TextView tvTitleBudgeRight;
    @BindView(R.id.toolbar_budget_info_right) TextView tvBudgetRight;
    @BindView(R.id.toolbar_budget_info_title_left) TextView tvTitleBudgetLeft;
    @BindView(R.id.toolbar_budget_info_left) TextView tvBudgetLeft;
    @BindView(R.id.budgets_empty_text) TextView emptyView;

    private static final int BUDGETS_LOADER = 0;
    private Uri budgetsUri;
    private BudgetsSwipeAdapter budgetsSwipeAdapter;
    private boolean isMainBudgets;
    private boolean isExpense;

    public BudgetsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            budgetsUri = getArguments().getParcelable(ARG_URI);
            isExpense = getArguments().getBoolean(ARG_IS_EXPENSE);
        }

        if (budgetsUri == null) {
            isMainBudgets = true;
            budgetsUri = MainBudgetsEntry.buildMainBudgetsUriWithType(isExpense);
        } else {
            isMainBudgets = false;
        }

        getLoaderManager().initLoader(BUDGETS_LOADER, null, this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_budgets, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        MenuItem menuItemEdit = menu.findItem(R.id.action_edit_budget);
        MenuItem menuItemDelete = menu.findItem(R.id.action_delete_budget);

        if (menuItemEdit != null)
            tintMenuIcon(getActivity(), menuItemEdit, R.color.colorGrey400);

        if (menuItemDelete != null)
            tintMenuIcon(getActivity(), menuItemDelete, R.color.colorGrey400);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        if (budgetsSwipeAdapter.getEditBtn() | budgetsSwipeAdapter.getDeleteBtn()) {
            menu.clear();
            menuInflater.inflate(R.menu.menu_done, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_budget:
                addBudgets();
                return true;

            case R.id.action_edit_budget:
                if (budgetsSwipeAdapter.getItemCount() > 0) {
                    budgetsSwipeAdapter.setEditBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_delete_budget:
                if (budgetsSwipeAdapter.getItemCount() > -1) {
                    budgetsSwipeAdapter.setDeleteBtn(true);
                    getActivity().invalidateOptionsMenu();
                }
                return true;

            case R.id.action_done:
                budgetsSwipeAdapter.resetEditAndDelete();
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budgets_list, container, false);
        ButterKnife.bind(this, rootView);

        if (!isMainBudgets)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_sub_budgets));

        String titleBudgetTotal = isExpense ? getString(R.string.title_budget_expense) : getString(R.string.title_budget_income);
        String titleBudgetLeft = isExpense ? getString(R.string.title_spent) : getString(R.string.title_received);
        String titleBudgetRight = getString(R.string.title_remain);

        tvTitleBudgetTotal.setText(titleBudgetTotal);
        tvTitleBudgetLeft.setText(titleBudgetLeft);
        tvTitleBudgeRight.setText(titleBudgetRight);
        tvTitleBudgetLeft.setPaintFlags(tvTitleBudgetLeft.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvTitleBudgeRight.setPaintFlags(tvTitleBudgeRight.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        budgetsSwipeAdapter = new BudgetsSwipeAdapter(getActivity(), null);
        budgetsSwipeAdapter.setMode(Attributes.Mode.Single);

        budgetsListView.setAdapter(budgetsSwipeAdapter);
        budgetsListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                budgetsUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String mainCurrency = getMainCurrency(getActivity());
            emptyView.setVisibility(View.GONE);
            List<Budget> budgets = new ArrayList<>();
            if (data.getColumnCount() == 8) {
                for (int i = 0; i < data.getCount(); i++) {
                    Budget budget = new Budget(data.getInt(0),
                            data.getString(1),
                            data.getString(2),
                            data.getInt(6),
                            data.getInt(7),
                            data.getString(3),
                            data.getString(4),
                            data.getInt(5));
                    budgets.add(budget);
                    data.moveToNext();
                }
            } else {
                for (int i = 0; i < data.getCount(); i++) {
                    Budget budget = new Budget(data.getInt(0),
                            data.getInt(1),
                            data.getString(2),
                            data.getString(3),
                            data.getString(4),
                            data.getInt(6),
                            data.getInt(5),
                            data.getString(7),
                            data.getString(8),
                            data.getInt(9));
                    budgets.add(budget);
                    data.moveToNext();
                }
            }
            budgetsSwipeAdapter.setData(budgets);

            MonetaryAmount totalBudget = Money.of(new BigDecimal(0), mainCurrency);
            MonetaryAmount budgetSpent = Money.of(new BigDecimal(0), mainCurrency);

            for (Budget budget : budgets) {
                totalBudget = totalBudget.add(budget.getAmount());
                budgetSpent = budgetSpent.add(budget.getAmountUsed());
            }

            setAmountWithColor(getActivity(), tvTotalBudget, totalBudget);
            setAmountWithColor(getActivity(), tvBudgetLeft, budgetSpent);

            MonetaryAmount remain = isExpense ? totalBudget.add(budgetSpent) : budgetSpent.subtract(totalBudget);

            if (remain.signum() < 0)
                remain = budgetSpent.subtract(remain);
            setAmountWithColor(getActivity(), tvBudgetRight, remain);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            budgetsSwipeAdapter.setData(null);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        budgetsSwipeAdapter.setData(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(BUDGETS_LOADER, null, this);
    }

    public boolean onBackPressed() {
        boolean back = budgetsSwipeAdapter.getSwipeStatus();
        getActivity().invalidateOptionsMenu();
        return back;
    }

    @OnClick(R.id.budgets_empty_text)
    public void addBudgets() {
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_MAIN_BUDGET, isMainBudgets);
        if (isMainBudgets)
            args.putBoolean(ARG_IS_EXPENSE, isExpense);
        else
            args.putString(ARG_BUDGET_PARENT_ID, getSubBudgetsIdParentIDFromUri(budgetsUri));

        BudgetsEditFragment fragment = new BudgetsEditFragment();
        fragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out)
                .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                .addToBackStack(null).commit();
    }
}
