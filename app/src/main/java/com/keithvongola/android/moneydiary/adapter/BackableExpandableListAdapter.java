package com.keithvongola.android.moneydiary.adapter;

import android.widget.BaseExpandableListAdapter;

import com.daimajia.swipe.SwipeLayout;

public abstract class BackableExpandableListAdapter extends BaseExpandableListAdapter {
    private boolean showEdit = false;
    private boolean showDelete = false;
    private SwipeLayout currentExpandedSwipeLayout;

    /**
     * Set to true when edit button in actionbar is selected else false
     * Notify any registered observers that the data set has changed.
     */
    public void setEditBtn(boolean isShow) {
        showEdit = isShow;
        notifyDataSetChanged();
    }

    /**
     * Set to true when delete button in actionbar is selected else false.
     *  Notify any registered observers that the data set has changed.
     */
    public void setDeleteBtn(boolean isShow) {
        showDelete = isShow;
        notifyDataSetChanged();
    }

    /**
     * @return {@code True} if edit button in {@code SwipeLayout} is visible else {@code false}
     */
    public boolean getEditBtn() {
        return showEdit;
    }

    /**
     * @return {@code True} if delete button in {@code SwipeLayout} is visible else {@code false}.
     */
    public boolean getDeleteBtn() {
        return showDelete;
    }


    /**
     * Set {@code showDelete} and {@code showEdit} to default value, {@code false}.
     * Notify any registered observers that the data set has changed.
     */
    public void resetEditAndDelete() {
        showDelete = false;
        showEdit = false;
        notifyDataSetChanged();
    }

    /**
     * Reset {@code showDelete} and {@code showEdit} to false.
     */
    public boolean getSwipeStatus() {
        if (showEdit | showDelete) {
            resetEditAndDelete();
            return false;
        }

        if (currentExpandedSwipeLayout != null) {
            currentExpandedSwipeLayout.close();
            return false;
        }
        return true;
    }

    public void setCurrentExpandedSwipeLayout(SwipeLayout currentExpandedSwipeLayout) {
        this.currentExpandedSwipeLayout = currentExpandedSwipeLayout;
    }

    public SwipeLayout getCurrentExpandedSwipeLayout() {
        return currentExpandedSwipeLayout;
    }
}
