package com.keithvongola.android.moneydiary.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import com.keithvongola.android.moneydiary.pojo.Account;
import com.keithvongola.android.moneydiary.ui.AccountEditFragment;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.keithvongola.android.moneydiary.ui.RecordsFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.accountTypeResId;
import static com.keithvongola.android.moneydiary.Utility.getCurrentYear;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.databases.MoneyContract.AccountsEntry;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sAccountIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionAccountSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionSubAccountSelection;

public class AccountsExpandableListAdapter extends BackableExpandableListAdapter {
    private Context mContext;
    private List<Account> groups;
    private List<List<Account>> children;
    private static final List<Integer> assetsAccounts =  Arrays.asList(0, 1, 3, 5, 6); // accounts which type are assets

    public static class GroupHolder {
        @BindView(R.id.accounts_group) LinearLayout accountsGroupLayout;
        @BindView(R.id.accounts_group_title) TextView accountsGroupTitle;

        public GroupHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    public static class ChildHolder {
        @BindView(R.id.account_list_holder) LinearLayout accountListLayout;
        @BindView(R.id.account_name) TextView accountNameView;
        @BindView(R.id.account_balance) TextView accountBalanceView;
        @BindView(R.id.bottom_wrapper) LinearLayout accountBtmLayout;
        @BindView(R.id.account_edit) ImageView accountEditImage;
        @BindView(R.id.account_edit_iv) ImageView accountEditImageView;
        @BindView(R.id.account_delete_iv) ImageView accountDeleteImageView;
        @BindView(R.id.account_delete) ImageView accountDeleteImage;
        @BindView(R.id.account_swipe) SwipeLayout swipeLayout;
        @BindView(R.id.account_detail_grid) GridLayout detailGridView;
        @BindView(R.id.account_current_title) TextView currentAmountTitle;
        @BindView(R.id.account_current_amount) TextView currentAmount;
        @BindView(R.id.account_long_title) TextView longTermsTitle;
        @BindView(R.id.account_long_amount) TextView longTermsAmount;

        public ChildHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    public AccountsExpandableListAdapter(Context context, List<Account> groups, List<List<Account>> children) {
        this.mContext = context;
        this.groups = groups;
        this.children = children;
    }

    public void setData(List<Account> groups, List<List<Account>> children) {
        this.groups = groups;
        this.children = children;
        notifyDataSetChanged();
    }

    public void setData(List<List<Account>> children) {
        this.children = children;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        if (groups != null)
            return groups.size();
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (children != null)
            return children.get(groupPosition).size();
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_accounts_group,parent,false);
            groupHolder = new GroupHolder(convertView);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        Account account = groups.get(groupPosition);

        if (children != null && children.get(groupPosition).size() == 0) {
            // list children is empty, hide accountsGroupLayout
            groupHolder.accountsGroupLayout.setVisibility(View.GONE);
            groupHolder.accountsGroupTitle.setVisibility(View.GONE);
        } else {
            groupHolder.accountsGroupLayout.setVisibility(View.VISIBLE);
            groupHolder.accountsGroupTitle.setVisibility(View.VISIBLE);
            groupHolder.accountsGroupTitle.setText(accountTypeResId(account.getType()));
        }

        groupHolder.accountsGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_accounts,parent,false);
            childHolder = new ChildHolder(convertView);
            childHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            childHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, childHolder.accountBtmLayout);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        int visibilityEdit = getEditBtn() ? View.VISIBLE : View.GONE; // determine visibility of edit button
        int visibilityDelete = getDeleteBtn() ? View.VISIBLE : View.GONE; // determine visibility of delete button
        childHolder.accountEditImageView.setVisibility(visibilityEdit);
        childHolder.accountDeleteImageView.setVisibility(visibilityDelete);
        childHolder.swipeLayout.setSwipeEnabled(!(getEditBtn() | getDeleteBtn()));

        final Account account = children.get(groupPosition).get(childPosition);
        childHolder.accountNameView.setText(account.getName());
        setAmountWithColor(mContext, childHolder.accountBalanceView, account.getTotal());

        childHolder.accountListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            { // replace current fragment with RecordsFragment
                int currentYear = getCurrentYear();

                Bundle args = new Bundle();
                args.putParcelable(ARG_URI,
                        TransactionEntry.buildTransactionUriWithAccountIDAndDate(account.getId(), currentYear + "-01-01", currentYear + "-12-31"));

                RecordsFragment fragment = new RecordsFragment();
                fragment.setArguments(args);
                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, null)
                        .addToBackStack(null).commit();
            }
        });

        final View.OnClickListener mEditBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) { // replace current fragment with AccountEditFragment
                Bundle args = new Bundle();
                args.putParcelable(Utility.ARG_ACCOUNT, account);
                AccountEditFragment fragment = new AccountEditFragment();

                fragment.setArguments(args);
                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        };

        final View.OnClickListener mDeleteBtnClickListener =new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Delete account at position childPosition
                Cursor c = mContext.getContentResolver().query(
                        TransactionEntry.buildTransactionUriWithAccountID(account.getId()),
                        null,
                        null,
                        null,
                        null);

                if (c != null && c.moveToFirst()) { // Transactions are found under this account
                    c.close();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.dialog_confirm_delete_account)
                            .setTitle(R.string.dialog_delete_account_title);

                    builder.setPositiveButton(R.string.dialog_delete_account_transactions, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        { // Delete account and transactions under this account
                            deleteAccount(account);
                            mContext.getContentResolver().delete(TransactionEntry.CONTENT_URI,
                                    sTransactionAccountSelection,
                                    new String[]{String.valueOf(account.getId())});
                            mContext.getContentResolver().delete(TransactionEntry.CONTENT_URI,
                                    sTransactionSubAccountSelection,
                                    new String[]{String.valueOf(account.getId())});
                        }
                    });

                    builder.setNegativeButton(R.string.dialog_keep_transactions, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        { // Update account status to inactive
                            ContentValues cv = new ContentValues();
                            cv.put(AccountsEntry.COLUMN_ACCOUNT_IS_ACTIVE, 0);
                            mContext.getContentResolver().update(AccountsEntry.CONTENT_URI,
                                    cv,
                                    sAccountIdSelection,
                                    new String[]{String.valueOf(account.getId())});
                        }
                    });

                    builder.setNeutralButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        { // close dialog
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(10f);
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setGravity(View.TEXT_ALIGNMENT_CENTER);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(10f);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(10f);
                } else { // No transactions found under this account, delete account
                    deleteAccount(account);
                }
            }
        };

        childHolder.accountEditImageView.setOnClickListener(mEditBtnClickListener);
        childHolder.accountEditImage.setOnClickListener(mEditBtnClickListener);
        childHolder.accountDeleteImageView.setOnClickListener(mDeleteBtnClickListener);
        childHolder.accountDeleteImage.setOnClickListener(mDeleteBtnClickListener);
        childHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
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

        if (assetsAccounts.contains(account.getType()) && account.getSaving().signum() > 0) {
            // account type is assets and saving amount > 0
            childHolder.detailGridView.setVisibility(View.VISIBLE);
            childHolder.currentAmountTitle.setText(mContext.getString(R.string.account_current_title));
            childHolder.longTermsTitle.setText(mContext.getString(R.string.account_long_term_saving));
            setAmount(childHolder.currentAmount, account.getCurrent());
            setAmount(childHolder.longTermsAmount, account.getSaving());
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void deleteAccount(Account account) {
        mContext.getContentResolver().delete(
                AccountsEntry.CONTENT_URI,
                sAccountIdSelection,
                new String[]{String.valueOf(account.getId())});
    }
}

