package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.keithvongola.android.moneydiary.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BudgetIconAdapter extends RecyclerView.Adapter<BudgetIconAdapter.ViewHolder>{
    private Context mContext;
    private List<Integer> iconList;
    private Integer lastCheckedPos;

    public BudgetIconAdapter(Context mContext) {
        this.mContext = mContext;
        lastCheckedPos = -1;
        iconList = new ArrayList<>();
        for (int i = 1; i < 92; i++) {
            int stringId = mContext.getResources().getIdentifier("budget_" + i,
                    "drawable", mContext.getPackageName());
            iconList.add(stringId);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_budgets_icon, parent, false);
        return new BudgetIconAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (iconList.get(position) != 0)
            Picasso.with(mContext).load(iconList.get(position)).into(holder.iconImageView);

        if (position == lastCheckedPos) {
            // icon is checked
            holder.checkImageView.setVisibility(View.VISIBLE);
            holder.iconContainer.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorAmber100));
        } else {
            holder.checkImageView.setVisibility(View.GONE);
            holder.iconContainer.setBackgroundColor(0);
        }

        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(lastCheckedPos);
                lastCheckedPos = position;
                notifyItemChanged(lastCheckedPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.grid_item_icon_container) RelativeLayout iconContainer;
        @BindView(R.id.grid_item_icon) ImageView iconImageView;
        @BindView(R.id.grid_item_check) ImageView checkImageView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public int getLastCheckedResId() {
        if (lastCheckedPos == -1) return lastCheckedPos;
        return iconList.get(lastCheckedPos);
    }

    public void setLastCheckedPos(Integer iconResId) {
        for (int i = 0; i < iconList.size(); i++) {
            if (iconList.get(i).equals(iconResId)) {
                lastCheckedPos = i;
                notifyItemChanged(i);
                break;
            }
        }
    }
}
