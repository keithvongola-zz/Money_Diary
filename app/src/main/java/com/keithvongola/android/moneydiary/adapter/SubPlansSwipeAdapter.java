package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.databases.MoneyContract.SubPlansEntry;
import com.keithvongola.android.moneydiary.pojo.SubPlan;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.keithvongola.android.moneydiary.ui.PlansEditFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_IS_MAIN_PLAN;
import static com.keithvongola.android.moneydiary.Utility.ARG_SUB_PLAN;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;
import static com.keithvongola.android.moneydiary.Utility.setAmount;
import static com.keithvongola.android.moneydiary.databases.dbUtility.sSubPlansWithID;

public class SubPlansSwipeAdapter extends BackableSwipeAdapter<SubPlansSwipeAdapter.ViewHolder> {
    private List<SubPlan> subPlans;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.plans_child_holder) LinearLayout planListLayout;
        @BindView(R.id.action_edit) ImageView actionEditIV;
        @BindView(R.id.action_delete) ImageView actionDeleteIV;
        @BindView(R.id.plans_child_edit_iv) ImageView editPlanIV;
        @BindView(R.id.plans_child_delete_iv) ImageView deletePlanIV;
        @BindView(R.id.plans_child_icon) ImageView icon;
        @BindView(R.id.plans_child_name) TextView name;
        @BindView(R.id.plans_child_amount) TextView amount;
        @BindView(R.id.bottom_wrapper) LinearLayout plansChildBtmLayout;
        @BindView(R.id.plans_child_swipe) SwipeLayout swipeLayout;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public SubPlansSwipeAdapter(Context context, List<SubPlan> subPlans) {
        this.mContext = context;
        this.subPlans = subPlans;
    }

    public void setData(List<SubPlan> subPlan) {
        this.subPlans = subPlan;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sub_plans, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final SubPlan subPlan = this.subPlans.get(position);
        viewHolder.name.setText(subPlan.getName());

        if (subPlan.getIconResId() != 0)
            Picasso.with(mContext).load(subPlan.getIconResId()).into(viewHolder.icon);

        setAmount(viewHolder.amount, subPlan.getAmountTarget());

        int visibilityEdit = getEditBtn() ? View.VISIBLE : View.GONE;
        int visibilityDelete = getDeleteBtn() ? View.VISIBLE : View.GONE;
        viewHolder.editPlanIV.setVisibility(visibilityEdit);
        viewHolder.deletePlanIV.setVisibility(visibilityDelete);

        viewHolder.swipeLayout.setSwipeEnabled(!(getEditBtn() | getDeleteBtn()));

        View.OnClickListener mEditBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable(ARG_SUB_PLAN, subPlan);
                args.putBoolean(ARG_IS_MAIN_PLAN, false);
                PlansEditFragment fragment = new PlansEditFragment();
                fragment.setArguments(args);

                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        };

        viewHolder.actionEditIV.setOnClickListener(mEditBtnClickListener);
        viewHolder.editPlanIV.setOnClickListener(mEditBtnClickListener);
        viewHolder.planListLayout.setOnClickListener(mEditBtnClickListener);

        View.OnClickListener mDeleteBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getContentResolver().delete(
                        SubPlansEntry.CONTENT_URI,
                        sSubPlansWithID,
                        new String[]{String.valueOf(subPlan.getId())});
            }
        };

        viewHolder.actionDeleteIV.setOnClickListener(mDeleteBtnClickListener);
        viewHolder.deletePlanIV.setOnClickListener(mDeleteBtnClickListener);

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
            public void onClose(SwipeLayout layout){}

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
        });
    }

    @Override
    public int getItemCount() {
        if (subPlans != null) return subPlans.size();
        return -1;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.budget_swipe;
    }
}
