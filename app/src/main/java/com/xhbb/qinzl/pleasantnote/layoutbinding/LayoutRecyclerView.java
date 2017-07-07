package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v7.widget.RecyclerView;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/3.
 */

public class LayoutRecyclerView extends BaseObservable {

    private boolean mAutoRefreshing;
    private boolean mSwipeRefreshing;
    private String mErrorText;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private OnLayoutRecyclerViewListener mListener;

    public LayoutRecyclerView(RecyclerView.Adapter recyclerViewAdapter,
                              RecyclerView.LayoutManager layoutManager,
                              OnLayoutRecyclerViewListener listener) {
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
    public boolean isSwipeRefreshing() {
        return mSwipeRefreshing;
    }

    public void setSwipeRefreshing(boolean swipeRefreshing) {
        mSwipeRefreshing = swipeRefreshing;
        notifyPropertyChanged(BR.swipeRefreshing);
    }

    @Bindable
    public boolean isAutoRefreshing() {
        return mAutoRefreshing;
    }

    public void setAutoRefreshing(boolean autoRefreshing) {
        mAutoRefreshing = autoRefreshing;
        notifyPropertyChanged(BR.autoRefreshing);
    }

    @Bindable
    public String getErrorText() {
        return mErrorText;
    }

    public void setErrorText(String errorText) {
        mErrorText = errorText;
        notifyPropertyChanged(BR.errorText);
    }

    public void onSwipeRefresh() {
        mListener.onSwipeRefresh();
    }

    public interface OnLayoutRecyclerViewListener {

        void onSwipeRefresh();
    }
}
