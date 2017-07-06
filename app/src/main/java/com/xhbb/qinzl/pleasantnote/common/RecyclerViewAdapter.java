package com.xhbb.qinzl.pleasantnote.common;

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

    protected Cursor mCursor;

    private int mDefaultLayoutRes;

    public RecyclerViewAdapter(int defaultLayoutRes) {
        mDefaultLayoutRes = defaultLayoutRes;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mDefaultLayoutRes, parent, false);
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
