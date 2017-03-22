package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract;
import com.keithvongola.android.moneydiary.pojo.Transaction;
import com.keithvongola.android.moneydiary.ui.ExpenseEditFragment;
import com.keithvongola.android.moneydiary.ui.IncomeEditFragment;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.keithvongola.android.moneydiary.ui.SavingEditFragment;
import com.keithvongola.android.moneydiary.ui.TransferEditFragment;
import com.squareup.picasso.Picasso;

import org.javamoney.moneta.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.money.MonetaryAmount;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_TRANSACTION;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getMainCurrency;
import static com.keithvongola.android.moneydiary.Utility.moneyConversion;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionIdSelection;

public class TransactionsExpandableAdapter extends BackableExpandableListAdapter {
    private SwipeLayout currentExpandedSwipeLayout;

    private Context mContext;
    private static String mainCurrency;
    private static int mainCurrencyUnit;
    private static final String[] groups = {"Dec", "Nov", "Oct", "Sep", "Aug", "Jul", "Jun", "May", "Apr", "Mar", "Feb", "Jan"};
    private List<List<Transaction>> children;

    public static class GroupHolder {
        @BindView(R.id.transactions_group) LinearLayout transactionsGroupll;
        @BindView(R.id.transactions_group_date_container) LinearLayout transactionsGroupDateContainer;
        @BindView(R.id.transactions_group_inexp_container) LinearLayout transactionsGroupInexpContainer;
        @BindView(R.id.transactions_group_balance_container) LinearLayout transactionsGroupBalanceContainer;
        @BindView(R.id.transactions_group_month) TextView transactionsGroupMonth;
        @BindView(R.id.transactions_group_year) TextView transactionsGroupYear;
        @BindView(R.id.transactions_group_income) TextView transactionsGroupIncome;
        @BindView(R.id.transactions_group_expense) TextView transactionsGroupExpense;
        @BindView(R.id.transactions_group_balance) TextView transactionsGroupBalance;

        public GroupHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    public static class ChildHolder {
        @BindView(R.id.transaction_list_holder) LinearLayout transactionListLayout;
        @BindView(R.id.transaction_name) TextView transactionNameView;
        @BindView(R.id.transaction_sub_name) TextView transactionSubNameView;
        @BindView(R.id.transaction_amount) TextView transactionAmountView;
        @BindView(R.id.transaction_icon) ImageView iconImageView;
        @BindView(R.id.bottom_wrapper) LinearLayout transactionBtmLayout;
        @BindView(R.id.transaction_edit) ImageView transactionEditImage;
        @BindView(R.id.transaction_delete) ImageView transactionDeleteImage;
        @BindView(R.id.transaction_edit_iv) ImageView transactionEditImageView;
        @BindView(R.id.transaction_delete_iv) ImageView transactionDeleteImageView;
        @BindView(R.id.transaction_swipe) SwipeLayout swipeLayout;
        @BindView(R.id.transaction_day_of_month) TextView transactionDayOfMonth;
        @BindView(R.id.transaction_day_of_week) TextView transactionDayOfWeek;
        @BindView(R.id.transaction_date_container) LinearLayout transactionDateContainer;
        public ChildHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    public TransactionsExpandableAdapter(Context context, List<List<Transaction>> children) {
        this.mContext = context;
        this.children = children;
        mainCurrency = Utility.getMainCurrency(mContext);
        mainCurrencyUnit = Utility.getMainCurrencyUnit(mContext);
        notifyDataSetChanged();
    }

    public void setData(List<List<Transaction>> children) {
        this.children = children;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
            return groups.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(children != null) return children.get(groupPosition).size();
        return 0;
    }

    public boolean isGroupsEmpty() {
        for(int i=0; i<getGroupCount(); i++){
            if(getChildrenCount(i)>0) return false;
        }
        return true;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return children.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_transactions_group,parent,false);
            groupHolder = new GroupHolder(convertView);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        groupHolder.transactionsGroupMonth.setText(groups[groupPosition]);
        groupHolder.transactionsGroupBalance.setText(mContext.getString(R.string.hint_empty));
        groupHolder.transactionsGroupIncome.setText(mContext.getString(R.string.group_income_title) + mContext.getString(R.string.hint_empty));
        groupHolder.transactionsGroupExpense.setText(mContext.getString(R.string.group_expense_title) + mContext.getString(R.string.hint_empty));

        if (children != null && children.get(groupPosition).size() > 0) {
            setGroupVisibility(groupHolder, true);
            List<Transaction> childList = children.get(groupPosition);
            groupHolder.transactionsGroupYear.setText(String.valueOf(childList.get(0).getYear()));
            String currency = getMainCurrency(mContext);
            MonetaryAmount income = Money.of(new BigDecimal(0), currency);
            MonetaryAmount expense = Money.of(new BigDecimal(0), currency);

            for (Transaction child : childList) {
                MonetaryAmount childAmount = child.getAmount();
                if (!childAmount.getCurrency().toString().equals(currency)) {
                    childAmount = moneyConversion(mContext, childAmount, currency);
                }

                if (child.getAmount().signum() > 0) income = income.add(childAmount);
                else expense = expense.add(childAmount);
            }

            MonetaryAmount balance = income.add(expense);
            if (!(balance.signum() == 0)) {
                setAmount(groupHolder.transactionsGroupBalance, balance);
            }

            if (!(income.signum()==0)) {
                setAmountWithColor(mContext, groupHolder.transactionsGroupIncome, income);
            }

            if (!(expense.signum()==0)) {
                setAmountWithColor(mContext, groupHolder.transactionsGroupExpense, expense);
            }

        } else {
            setGroupVisibility(groupHolder, false);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_transaction, parent, false);
            childHolder = new ChildHolder(convertView);
            childHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            childHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, childHolder.transactionBtmLayout);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        int visibilityEdit = getEditBtn() ? View.VISIBLE : View.GONE; // determine visibility of edit button
        int visibilityDelete = getDeleteBtn() ? View.VISIBLE : View.GONE; // determine visibility of delete button
        childHolder.transactionEditImageView.setVisibility(visibilityEdit);
        childHolder.transactionDeleteImageView.setVisibility(visibilityDelete);

        childHolder.swipeLayout.setSwipeEnabled(!(getEditBtn() | getDeleteBtn()));

        final Transaction transaction = children.get(groupPosition).get(getChildrenCount(groupPosition) - childPosition - 1);
        String transactionName = transaction.getType() == 3 ? transaction.getPlanName() : transaction.getSubBudgetName();
        childHolder.transactionNameView.setText(transactionName);

        if (transaction.getIconResId() != 0) {
            switch (transaction.getIconResId()) {
                case 1: // this is a transfer transaction
                    int transfer = transaction.getAmount().signum() < 0 ? R.drawable.budget_86 : R.drawable.budget_85;
                    Picasso.with(mContext).load(transfer).into(childHolder.iconImageView);
                    break;
                case 2: // this is a balance change transaction
                    Picasso.with(mContext).load(R.drawable.budget_96).into(childHolder.iconImageView);
                    break;
                default :
                Picasso.with(mContext).load(transaction.getIconResId()).into(childHolder.iconImageView);
            }
        }
        switch (transaction.getType()) {
            case 0:
                    childHolder.transactionSubNameView.setText(transaction.getMainAccountName());
                break;
            case 1:
                childHolder.transactionSubNameView.setText(transaction.getMainAccountName());
                break;
            case 2:
                childHolder.transactionSubNameView.setText(transaction.getMainAccountName()
                        + " > " + transaction.getSubAccountName());
                break;
            case 3:
                childHolder.transactionSubNameView.setText(mContext.getString(R.string.tab_saving));
                Picasso.with(mContext).load(R.drawable.budget_95).into(childHolder.iconImageView);
                break;
            default:
                childHolder.transactionSubNameView.setText(transaction.getMainBudgetName());
        }

        childHolder.transactionDayOfMonth.setText(transaction.getDayOfMonth()+"");

        int stringId = mContext.getResources().getIdentifier("day_of_week_"+transaction.getDayOfWeek(),
                "string", mContext.getPackageName());

        if (stringId != 0)
            childHolder.transactionDayOfWeek.setText(mContext.getString(stringId));

        if (childPosition > 0) {
            final Transaction lastTransaction = children.get(groupPosition).get(getChildrenCount(groupPosition) - childPosition);

            if(lastTransaction.getDayOfMonth() == transaction.getDayOfMonth())
                childHolder.transactionDateContainer.setVisibility(View.INVISIBLE);
            else
                childHolder.transactionDateContainer.setVisibility(View.VISIBLE);
        }

        View.OnClickListener onEditClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class fragmentClass = null;

                Bundle args = new Bundle();
                args.putParcelable(ARG_TRANSACTION, transaction);
                switch (transaction.getType()){
                    case 0:
                        if (transaction.getSubBudgetID() == 2) // this is a balance change
                            Toast.makeText(mContext, "Balance change cannot be edited.", Toast.LENGTH_LONG).show();
                        else fragmentClass = ExpenseEditFragment.class;
                        break;
                    case 1:
                        fragmentClass = IncomeEditFragment.class;
                        break;
                    case 2:
                        fragmentClass = TransferEditFragment.class;
                        break;
                    case 3:
                        fragmentClass = SavingEditFragment.class;
                        break;
                }

                try {
                    Fragment fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(args);

                    ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                            .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                            .addToBackStack(null).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        childHolder.transactionListLayout.setOnClickListener(onEditClickListener);
        childHolder.transactionEditImage.setOnClickListener(onEditClickListener);

        View.OnClickListener mDeleteBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction.getType() == 2) {
                    List<String> transactionIds = new ArrayList<>();
                    transactionIds.add(String.valueOf(transaction.getId()));
                    if (transaction.getAmount().signum() < 0)
                        transactionIds.add(String.valueOf(transaction.getId()+1));
                    else
                        transactionIds.add(String.valueOf(transaction.getId()-1));

                    for (String id : transactionIds)
                        mContext.getContentResolver().delete(
                                MoneyContract.TransactionEntry.CONTENT_URI,
                                sTransactionIdSelection,
                                new String[]{id});
                } else {
                    mContext.getContentResolver().delete(
                            MoneyContract.TransactionEntry.CONTENT_URI,
                            sTransactionIdSelection,
                            new String[]{String.valueOf(transaction.getId())});
                }
            }
        };

        childHolder.transactionDeleteImage.setOnClickListener(mDeleteBtnClickListener);
        childHolder.transactionDeleteImageView.setOnClickListener(mDeleteBtnClickListener);

        setAmountWithColor(mContext, childHolder.transactionAmountView, transaction.getAmount());
        childHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                SwipeLayout currentSwipeLayout = getCurrentExpandedSwipeLayout();
                if (currentSwipeLayout != null && currentSwipeLayout != layout)
                    currentSwipeLayout.close(true);
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
            public void onClose(SwipeLayout layout){
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private void setGroupVisibility(GroupHolder groupHolder, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;

        groupHolder.transactionsGroupll.setVisibility(visibility);
        groupHolder.transactionsGroupDateContainer.setVisibility(visibility);
        groupHolder.transactionsGroupInexpContainer.setVisibility(visibility);
        groupHolder.transactionsGroupBalanceContainer.setVisibility(visibility);
    }

    public int getFirstVisibleGroup() {
        for (int i=0; i<getGroupCount();i++){
            if (getChildrenCount(i)>0)
                return i;
        }
        return -1;
    }
}
