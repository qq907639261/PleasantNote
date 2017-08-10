package com.xhbb.qinzl.pleasantnote.common;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by qinzl on 2017/7/5.
 */

public abstract class RecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerViewAdapter.BindingHolder> {

    private Cursor mCursor;
    private int mLayoutRes;
    private Context mContext;

    public RecyclerViewAdapter(Context context, int defaultLayoutRes) {
        mContext = context;
        mLayoutRes = defaultLayoutRes;
    }

    protected Cursor getCursor() {
        return mCursor;
    }

    protected Context getContext() {
        return mContext;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void swapCursor(Cursor cursor, int startPosition, int itemCount) {
        mCursor = cursor;
        notifyItemRangeChanged(startPosition, itemCount);
    }

    protected void setLayoutRes(int layoutRes) {
        mLayoutRes = layoutRes;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutRes, parent, false);
        return new BindingHolder(view);
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    protected class BindingHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mBinding;

        BindingHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return mBinding;
        }
    }
}
