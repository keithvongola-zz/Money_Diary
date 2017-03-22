package com.keithvongola.android.moneydiary.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeLoadMoreTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;
import com.keithvongola.android.moneydiary.R;

public class LoadMoreFooterView extends TextView implements SwipeTrigger, SwipeLoadMoreTrigger {
    String onMoveReleaseStr;
    String onMoveSwipeStr;
    String onMoveCompleteStr;
    String onCompleteStr;

    public LoadMoreFooterView(Context context) {
        super(context);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLoadMore() {
        setText(R.string.load_more_refresh);
    }

    @Override
    public void onPrepare() {
        setText("");
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (yScrolled <= -getHeight()) {
                setText(onMoveReleaseStr);
            } else {
                setText(onMoveSwipeStr);
            }
        } else {
            setText(onMoveCompleteStr);
        }
    }

    @Override
    public void onRelease() {
        setText("");
    }

    @Override
    public void onComplete() {
        setText(R.string.load_more_complete);
    }

    @Override
    public void onReset() {
        setText("");
    }


    public void setOnMoveReleaseStr(String onMoveReleaseStr) {
        this.onMoveReleaseStr = onMoveReleaseStr;
    }

    public void setOnMoveSwipeStr(String onMoveSwipeStr) {
        this.onMoveSwipeStr = onMoveSwipeStr;
    }

    public void setOnMoveCompleteStr(String onMoveCompleteStr) {
        this.onMoveCompleteStr = onMoveCompleteStr;
    }

    public void setOnCompleteStr(String onCompleteStr) {
        this.onCompleteStr = onCompleteStr;
    }
}