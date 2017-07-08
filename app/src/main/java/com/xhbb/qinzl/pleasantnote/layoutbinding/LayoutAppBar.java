package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/8.
 */

public class LayoutAppBar extends BaseObservable {

    private OnLayoutAppBarListener mListener;
    private int mTabLayoutVisibility;

    LayoutAppBar(OnLayoutAppBarListener listener) {
        mListener = listener;
    }

    public void onDrawerOpened(SearchView searchView) {
        mListener.onDrawerOpened(searchView);
    }

    public void onQueryTextSubmit(SearchView searchView, String s) {
        mListener.onQueryTextSubmit(searchView, s);
    }

    @Bindable
    public int getTabLayoutVisibility() {
        return mTabLayoutVisibility;
    }

    void setTabLayoutVisibility(boolean tabLayoutVisible) {
        mTabLayoutVisibility = tabLayoutVisible ? View.VISIBLE : View.GONE;
        notifyPropertyChanged(BR.tabLayoutVisibility);
    }

    interface OnLayoutAppBarListener {

        void onDrawerOpened(SearchView searchView);
        void onQueryTextSubmit(SearchView searchView, String s);
    }
}
