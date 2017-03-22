package com.keithvongola.android.moneydiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keithvongola.android.moneydiary.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurrencyRecyclerViewAdapter extends RecyclerView.Adapter<CurrencyRecyclerViewAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter{
    private Context mContext;
    private Cursor cursor;

    public CurrencyRecyclerViewAdapter(Cursor cursor, Context mContext) {
        this.cursor = cursor;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_currency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        final String currencyAlphaCode = cursor.getString(1);
        final String currencyUnit = cursor.getString(3);
        int stringId = mContext.getResources().getIdentifier(currencyAlphaCode,
                "string", mContext.getPackageName());

        if (stringId != 0)
            holder.currencyName.setText(mContext.getString(stringId));

        holder.currencyAlphaCode.setText(currencyAlphaCode);
        holder.currencyListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("country",currencyAlphaCode);
                intent.putExtra("unit",currencyUnit);
                ((AppCompatActivity) v.getContext()).setResult(AppCompatActivity.RESULT_OK,intent);
                ((AppCompatActivity) v.getContext()).finish();
            }
        });
        int drawableId = mContext.getResources().getIdentifier(cursor.getString(4).toLowerCase(),
                "drawable", mContext.getPackageName());
        if (drawableId != 0)
            Picasso.with(mContext).load(drawableId).into(holder.currencyFlag);
    }

    @Override
    public int getItemCount() {
        if (cursor != null) return cursor.getCount();
        return 0;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(1).substring(0,1);
    }

    public void setData(Cursor cursor) {
        this.cursor = cursor;
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.currency_list_item) LinearLayout currencyListLayout;
        @BindView(R.id.currency_item_flag) ImageView currencyFlag;
        @BindView(R.id.currency_item_name) TextView currencyName;
        @BindView(R.id.currency_item_alpha_code) TextView currencyAlphaCode;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
