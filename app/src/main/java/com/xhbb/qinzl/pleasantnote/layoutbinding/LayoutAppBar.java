package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/8.
 */

public class LayoutAppBar extends BaseObservable {

    private boolean mTabLayoutVisible;
    private OnLayoutAppBarListener mListener;
    private boolean mSearchViewCollapsed;

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
    public boolean isSearchViewCollapsed() {
        return mSearchViewCollapsed;
    }

    void setSearchViewCollapsed(boolean searchViewCollapsed) {
        mSearchViewCollapsed = searchViewCollapsed;
        notifyPropertyChanged(BR.searchViewCollapsed);

        if (searchViewCollapsed) {
            notifyPropertyChanged(BR.searchViewImeOptions);
        }
    }

    @Bindable
    public boolean isTabLayoutVisible() {
        return mTabLayoutVisible;
    }

    @Bindable
    public int getSearchViewImeOptions() {
        return EditorInfo.IME_ACTION_SEARCH;
    }

    void setTabLayoutVisible(boolean tabLayoutVisible) {
        mTabLayoutVisible = tabLayoutVisible;
        notifyPropertyChanged(BR.tabLayoutVisible);
    }

    interface OnLayoutAppBarListener {

        void onDrawerOpened(SearchView searchView);
        void onQueryTextSubmit(SearchView searchView, String s);
    }
}
