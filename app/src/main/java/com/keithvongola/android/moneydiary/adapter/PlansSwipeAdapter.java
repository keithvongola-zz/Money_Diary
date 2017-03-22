package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.net.Uri;
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
import com.keithvongola.android.moneydiary.databases.MoneyContract.PlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;
import com.keithvongola.android.moneydiary.databases.MoneyContract.TransactionEntry;
import com.keithvongola.android.moneydiary.pojo.Plan;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.keithvongola.android.moneydiary.ui.PlansEditFragment;
import com.keithvongola.android.moneydiary.ui.SubPlansFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_URI;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.getProgress;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.Utility.setAmountWithColor;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sPlansIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubPlansParentIdSelection;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sTransactionSubCategory;

public class PlansSwipeAdapter extends BackableSwipeAdapter<PlansSwipeAdapter.ViewHolder> {
    private List<Plan> plans;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.plan_list_holder) LinearLayout planListLayout;
        @BindView(R.id.action_edit) ImageView actionEditIV;
        @BindView(R.id.action_delete) ImageView actionDeleteIV;
        @BindView(R.id.plan_edit_iv) ImageView editPlanIV;
        @BindView(R.id.plan_delete_iv) ImageView deletePlanIV;
        @BindView(R.id.plan_icon) ImageView icon;
        @BindView(R.id.plan_name) TextView name;
        @BindView(R.id.plan_amount_left_title) TextView amountLeftTitle;
        @BindView(R.id.plan_amount_left) TextView amountLeft;
        @BindView(R.id.plan_balance) TextView balanceAmount;
        @BindView(R.id.plan_target) TextView targetAmount;
        @BindView(R.id.plan_monthly_contribution) TextView monthlyContribution;
        @BindView(R.id.plan_terms) TextView terms;
        @BindView(R.id.bottom_wrapper) LinearLayout planBtmLayout;
        @BindView(R.id.plan_swipe) SwipeLayout swipeLayout;
        @BindView(R.id.plan_progress_bar) ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public PlansSwipeAdapter(Context context, List<Plan> plans) {
        this.mContext = context;
        this.plans = plans;
    }

    public void setData(List<Plan> plans) {
        this.plans = plans;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_plans, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Plan plan = plans.get(position);
        int progress = 0;

        viewHolder.name.setText(plan.getName());

        if (plan.getIconResId() != 0)
            Picasso.with(mContext).load(plan.getIconResId()).into(viewHolder.icon);

        setAmountWithColor(mContext, viewHolder.amountLeft, plan.getAmountCurrentMonth());
        setAmount(viewHolder.balanceAmount, plan.getAmountActual());
        setAmount(viewHolder.targetAmount, plan.getAmountTarget());

        if (plan.getMonthlyContribution().signum() == 0)
            viewHolder.monthlyContribution.setText(R.string.hint_empty);
        else
            setAmount(viewHolder.monthlyContribution, plan.getMonthlyContribution());

        if (plan.getAmountActual().compareTo(plan.getAmountTarget()) == 1)
            viewHolder.terms.setText(R.string.hint_empty);
        else
            viewHolder.terms.setText(plan.getCurrentTerms() + "/" + plan.getTerms());

        if (plan.getAmountTarget().signum() > 0)
            progress = getProgress(plan.getAmountActual(), plan.getAmountTarget());

        if (progress>50)
            viewHolder.progressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_bar_green));
        else
            viewHolder.progressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_bar_red));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            viewHolder.progressBar.setProgress(progress,true);
        else
            viewHolder.progressBar.setProgress(progress);

        int visibilityEdit, visibilityDelete;
        if (!getEditBtn() && !getDeleteBtn()) {
            visibilityEdit = View.GONE;
            visibilityDelete = View.GONE;
        } else {
             visibilityEdit = getEditBtn() ? View.VISIBLE : View.INVISIBLE;
             visibilityDelete = getDeleteBtn() ? View.VISIBLE : View.INVISIBLE;
        }
        viewHolder.editPlanIV.setVisibility(visibilityEdit);
        viewHolder.deletePlanIV.setVisibility(visibilityDelete);

        viewHolder.swipeLayout.setSwipeEnabled(!(getEditBtn() | getDeleteBtn()));

        View.OnClickListener mEditBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable(ARG_PLAN, plan);
                args.putBoolean(ARG_IS_MAIN_PLAN, true);

                PlansEditFragment fragment = new PlansEditFragment();
                fragment.setArguments(args);

                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        };

        viewHolder.planListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri childUri = SubPlansEntry.buildSubPlansUriWithParentID(plan.getId());
                Bundle args = new Bundle();
                args.putParcelable(ARG_URI, childUri);

                SubPlansFragment fragment = new SubPlansFragment();
                fragment.setArguments(args);

                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        });

        viewHolder.actionEditIV.setOnClickListener(mEditBtnClickListener);
        viewHolder.editPlanIV.setOnClickListener(mEditBtnClickListener);

        View.OnClickListener mDeleteBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getContentResolver().delete(PlansEntry.CONTENT_URI,
                        sPlansIdSelection,
                        new String[]{String.valueOf(plan.getId())});

                mContext.getContentResolver().delete(SubPlansEntry.CONTENT_URI,
                        sSubPlansParentIdSelection,
                        new String[]{String.valueOf(plan.getId())});

                mContext.getContentResolver().delete(TransactionEntry.CONTENT_URI,
                        TransactionEntry.COLUMN_TRANSACTION_TYPE + " = 3 AND " + sTransactionSubCategory,
                        new String[]{String.valueOf(plan.getId())});

                List<Integer> openItems = getOpenItems();
                for (int i = 0; i < openItems.size(); i++){
                    closeItem(openItems.get(i));
                }
            }
        };

        viewHolder.deletePlanIV.setOnClickListener(mDeleteBtnClickListener);
        viewHolder.actionDeleteIV.setOnClickListener(mDeleteBtnClickListener);

        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {

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
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    @Override
    public int getItemCount() {
        if (plans != null)
            return plans.size();
        else
            return -1;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.budget_swipe;
    }
}
