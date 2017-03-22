package com.keithvongola.android.moneydiary.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import com.keithvongola.android.moneydiary.pojo.Budget;
import com.keithvongola.android.moneydiary.ui.BudgetsEditFragment;
import com.keithvongola.android.moneydiary.ui.BudgetsListFragment;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_BUDGET;
import static com.keithvongola.android.moneydiary.Utility.ARG_BUDGET_PARENT_ID;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_EXPENSE;
import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_BUDGET;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getProgress;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.MainBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.SubBudgetsEntry.buildSubBudgetsUriWithParentID;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sMainBudgetsIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetParentIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubBudgetsIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionMainCategory;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionSubCategory;

public class BudgetsSwipeAdapter extends BackableSwipeAdapter<BudgetsSwipeAdapter.ViewHolder> {
    private static final String ARG_URI = "URI";

    private List<Budget> budgets;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.budget_list_holder) LinearLayout budgetListLayout;
        @BindView(R.id.action_edit) ImageView actionEditLayout;
        @BindView(R.id.action_delete) ImageView actionDeleteLayout;
        @BindView(R.id.budgets_edit_iv) ImageView editBudgetImageView;
        @BindView(R.id.budgets_delete_iv) ImageView deleteBudgetImageView;
        @BindView(R.id.budget_icon) ImageView iconImageView;
        @BindView(R.id.budget_name) TextView budgetNameView;
        @BindView(R.id.budget_amount_left_title) TextView budgetAmountLeftTitleView;
        @BindView(R.id.budget_amount_left) TextView budgetAmountLeftView;
        @BindView(R.id.budget_amount) TextView budgetAmountView;
        @BindView(R.id.bottom_wrapper) LinearLayout budgetBtmLayout;
        @BindView(R.id.budget_swipe) SwipeLayout swipeLayout;
        @BindView(R.id.budget_left_pb) ProgressBar progressBarView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public BudgetsSwipeAdapter(Context mContext, List<Budget> budgets) {
        this.mContext = mContext;
        this.budgets = budgets;
    }

    public void setData(List<Budget> budgets) {
        this.budgets = budgets;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_budgets, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Budget budget = budgets.get(position);
        int progress = 0;
        if (budget.getChildID() == 0) {
            // this is a main budget item
            viewHolder.budgetNameView.setText(budget.getParentName());
        } else {
            // this is a sub budget item
            viewHolder.budgetNameView.setText(budget.getChildName());
        }

        if (budget.getIconResId() != 0)
            Picasso.with(mContext).load(budget.getIconResId()).into(viewHolder.iconImageView);

        String amountRemainTitle;
        MonetaryAmount amountLeft;
        if (budget.getIsExpense() == 1) {
            // budget expense
            amountRemainTitle = mContext.getString(R.string.budget_title_remain);
            amountLeft = budget.getAmount().add(budget.getAmountUsed());
            if (!(budget.getAmount().signum() == 0))
                progress = getProgress(amountLeft, budget.getAmount());
        } else {
            // budget income
            amountRemainTitle = mContext.getString(R.string.budget_title_received);
            amountLeft = budget.getAmountUsed();
            if( budget.getAmount().signum() == 0)
                progress = 0;
            else if (budget.getAmountUsed().compareTo(budget.getAmount()) == -1)
                progress = getProgress(amountLeft, budget.getAmount());
            else
                progress = 100;
        }

        viewHolder.budgetAmountLeftTitleView.setText(amountRemainTitle);
        setAmountWithColor(mContext,viewHolder.budgetAmountLeftView, amountLeft);
        setAmount(viewHolder.budgetAmountView, budget.getAmount());

        if (progress > 50)
            viewHolder.progressBarView.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_bar_green));
        else
            viewHolder.progressBarView.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_bar_red));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            viewHolder.progressBarView.setProgress(progress,true);
        else
            viewHolder.progressBarView.setProgress(progress);

        int visibilityEdit = getEditBtn() ? View.VISIBLE : View.GONE; // determine visibility of edit button
        int visibilityDelete = getDeleteBtn() ? View.VISIBLE : View.GONE; // determine visibility of delete button
        viewHolder.editBudgetImageView.setVisibility(visibilityEdit);
        viewHolder.deleteBudgetImageView.setVisibility(visibilityDelete);

        viewHolder.swipeLayout.setSwipeEnabled(!(getEditBtn() | getDeleteBtn()));

        View.OnClickListener mEditBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable(ARG_BUDGET, budget);
                if (budget.getChildID() == 0) { // this is main budget item
                    args.putBoolean(ARG_IS_MAIN_BUDGET, true);
                    boolean isExpense = budget.getIsExpense() == 1;
                    args.putBoolean(ARG_IS_EXPENSE, isExpense);
                } else {
                    // this is a sub-budget item
                    args.putBoolean(ARG_IS_MAIN_BUDGET, false);
                    args.putString(ARG_BUDGET_PARENT_ID, String.valueOf(budget.getParentID()));
                }

                BudgetsEditFragment fragment = new BudgetsEditFragment();
                fragment.setArguments(args);

                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        };

        viewHolder.actionEditLayout.setOnClickListener(mEditBtnClickListener);
        viewHolder.editBudgetImageView.setOnClickListener(mEditBtnClickListener);

        if (budget.getChildID() == 0) {
            viewHolder.budgetListLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Bundle args = new Bundle();
                        boolean isExpense = budget.getIsExpense() == 1;
                        args.putBoolean(ARG_IS_EXPENSE, isExpense);
                        args.putBoolean(ARG_IS_MAIN_BUDGET, false);
                        args.putParcelable(ARG_URI, buildSubBudgetsUriWithParentID(budget.getParentID()));

                        BudgetsListFragment fragment = new BudgetsListFragment();
                        fragment.setArguments(args);

                        ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out)
                                .replace(R.id.content_main_container, fragment, "TAG")
                                .addToBackStack(null).commit();
                }
            });
        } else {
            viewHolder.budgetListLayout.setOnClickListener(mEditBtnClickListener);
        }

        View.OnClickListener mDeleteBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Delete budgets at position
                if (budget.getChildID() != 0){
                    Cursor c = mContext.getContentResolver().query(
                            TransactionEntry.CONTENT_URI,
                            null,
                            sTransactionSubCategory,
                            new String[]{String.valueOf(budget.getChildID())},
                            null);

                    if (c != null && c.getCount() > 0) {
                        // sub-budgets is found in transaction table, set sub-budgets inactive
                        c.close();
                        ContentValues cv = new ContentValues();
                        cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_IS_ACTIVE,0);
                        mContext.getContentResolver().update(SubBudgetsEntry.CONTENT_URI,
                                cv,
                                sSubBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getChildID())});
                    } else {
                        // delete sub-budget
                        mContext.getContentResolver().delete(
                                SubBudgetsEntry.CONTENT_URI,
                                sSubBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getChildID())});
                    }
                } else {
                    Cursor c = mContext.getContentResolver().query(
                            TransactionEntry.CONTENT_URI,
                            null,
                            sTransactionMainCategory,
                            new String[]{String.valueOf(budget.getParentID())},
                            null);

                    if (c != null && c.getCount() > 0) {
                        // main budgets is found in transaction table, set main budgets inactive
                        c.close();
                        ContentValues cv = new ContentValues();
                        cv.put(MainBudgetsEntry.COLUMN_MAIN_BUDGET_IS_ACTIVE,0);
                        mContext.getContentResolver().update(MainBudgetsEntry.CONTENT_URI,
                                cv,
                                sMainBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getParentID())});

                        cv = new ContentValues();
                        cv.put(SubBudgetsEntry.COLUMN_SUB_BUDGET_IS_ACTIVE,0);
                        mContext.getContentResolver().update(SubBudgetsEntry.CONTENT_URI,
                                cv,
                                sSubBudgetParentIdSelection,
                                new String[]{String.valueOf(budget.getParentID())});
                    } else {
                        //Delete main budget and all sub-budget under it
                        mContext.getContentResolver().delete(
                                MainBudgetsEntry.CONTENT_URI,
                                sMainBudgetsIdSelection,
                                new String[]{String.valueOf(budget.getParentID())});

                        mContext.getContentResolver().delete(
                                SubBudgetsEntry.CONTENT_URI,
                                sSubBudgetParentIdSelection,
                                new String[]{String.valueOf(budget.getParentID())});
                    }
                }

                List<Integer> openItems = getOpenItems();
                for (int i = 0; i < openItems.size(); i++){
                    closeItem(openItems.get(i));
                }
            }
        };
        viewHolder.actionDeleteLayout.setOnClickListener(mDeleteBtnClickListener);
        viewHolder.deleteBudgetImageView.setOnClickListener(mDeleteBtnClickListener);

        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                SwipeLayout currentSwipeLayout = getCurrentExpandedSwipeLayout();
                if (currentSwipeLayout != null && currentSwipeLayout != layout) {
                    currentSwipeLayout.close(true);
                }
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                setCurrentExpandedSwipeLayout(layout);
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                setCurrentExpandedSwipeLayout(null);
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    @Override
    public int getItemCount() {
        if (budgets != null)
            return budgets.size();
        else
            return -1;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.budget_swipe;
    }
}
