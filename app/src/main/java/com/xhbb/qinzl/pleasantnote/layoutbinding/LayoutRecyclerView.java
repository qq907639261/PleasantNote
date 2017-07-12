package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v7.widget.RecyclerView;

import com.xhbb.qinzl.pleasantnote.BR;
import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/3.
 */

public class LayoutRecyclerView extends BaseObservable {

    private boolean mRefreshing;
    private String mTipsText;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private OnLayoutRecyclerViewListener mListener;

    public LayoutRecyclerView(Context context, RecyclerView.Adapter recyclerViewAdapter,
                              RecyclerView.LayoutManager layoutManager,
                              OnLayoutRecyclerViewListener listener) {
        mRefreshing = true;
        mTipsText = context.getString(R.string.refreshing_text);
        mRecyclerViewAdapter = recyclerViewAdapter;
        mLayoutManager = layoutManager;
        mListener = listener;
    }

    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return mRecyclerViewAdapter;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Bindable
    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
        notifyPropertyChanged(BR.refreshing);
    }

    @Bindable
    public String getTipsText() {
        return mTipsText;
    }

    public void setTipsText(String tipsText) {
        mTipsText = tipsText;
        notifyPropertyChanged(BR.tipsText);
    }

    public void onSwipeRefresh() {
        mListener.onSwipeRefresh();
    }

    public void onScrollStateChanged(int newState) {
        mListener.onScrollStateChanged(newState);
    }

    public interface OnLayoutRecyclerViewListener {

        void onSwipeRefresh();
        void onScrollStateChanged(int newState);
    }
}
