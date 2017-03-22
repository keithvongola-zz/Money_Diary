package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.keithvongola.android.moneydiary.Utility;
import com.keithvongola.android.moneydiary.ui.ExRateEditFragment;
import com.keithvongola.android.moneydiary.ui.MainActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_AUTO;
import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_MANUAL;
import static com.keithvongola.android.moneydiary.Utility.ARG_EX_RATE_TITLE;
import static com.keithvongola.android.moneydiary.Utility.FRAG_TAG_EDIT;

public class ExRateRecycleViewAdapter extends RecyclerView.Adapter<ExRateRecycleViewAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter{
    private Context mContext;
    private Cursor cursor;

    public ExRateRecycleViewAdapter(Cursor cursor, Context context) {
        this.mContext = context;
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public void setData(Cursor cursor) {
        this.cursor = cursor;
        this.notifyDataSetChanged();
    }

    @Override
    public ExRateRecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_ex_rate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExRateRecycleViewAdapter.ViewHolder holder, int position) {
        final int i = position;
        cursor.moveToPosition(i);

        final String exRateValueFromAPI = cursor.getString(3);
        int stringId = mContext.getResources().getIdentifier(cursor.getString(6), //Get currency full name string by alphabet code
                "string", mContext.getPackageName());

        if (stringId != 0) holder.exRateName.setText(mContext.getString(stringId));

        if (cursor.getInt(4) == 1)
            holder.exRateValue.setText(cursor.getString(5));
        else
            holder.exRateValue.setText(exRateValueFromAPI);

        holder.exRateAlphaCode.setText(cursor.getString(6));

        holder.exRateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(i);
                Bundle args = new Bundle();
                args.putInt(Utility.ARG_IS_MANUAL,cursor.getInt(4));
                args.putString(ARG_EX_RATE_TITLE , cursor.getString(2));
                args.putString(ARG_EX_RATE_AUTO, cursor.getString(3));
                if (cursor.getString(5) != null)
                    args.putString(ARG_EX_RATE_MANUAL, cursor.getString(5));

                ExRateEditFragment fragment = new ExRateEditFragment();

                fragment.setArguments(args);
                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_bottom, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.slide_out_bottom)
                        .replace(R.id.content_main_container, fragment, FRAG_TAG_EDIT)
                        .addToBackStack(null).commit();
            }
        });

        int rid = mContext.getResources().getIdentifier(cursor.getString(7).toLowerCase(),
                "drawable", mContext.getPackageName());
        if (rid != 0) Picasso.with(mContext).load(rid).into(holder.exRateFlag);
    }

    @Override
    public int getItemCount() {
        if(cursor != null) return cursor.getCount();
        return 0;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(6).substring(0,1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.exchange_rate_list_item) LinearLayout exRateLayout;
        @BindView(R.id.exchange_rate_item_flag) ImageView exRateFlag;
        @BindView(R.id.exchange_rate_item_name) TextView exRateName;
        @BindView(R.id.exchange_rate_item_alpha_code) TextView exRateAlphaCode;
        @BindView(R.id.exchange_rate_item_value) TextView exRateValue;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
