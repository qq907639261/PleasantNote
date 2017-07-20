package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xhbb.qinzl.pleasantnote.BR;
import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/3.
 */

public class LayoutMain extends BaseObservable {

    private PagerAdapter mPagerAdapter;
    private float mBottomFragmentHeight;
    private int mViewPagerVisibility;
    private LayoutAppBar mLayoutAppBar;

    LayoutMain(AppCompatActivity activity, PagerAdapter pagerAdapter,
               LayoutAppBar.OnLayoutAppBarListener onLayoutAppBarListener) {
        mBottomFragmentHeight = activity.getResources().getDimension(R.dimen.bottomPlayFragmentHeight);
        mPagerAdapter = pagerAdapter;
        mViewPagerVisibility = View.VISIBLE;

        mLayoutAppBar = new LayoutAppBar(activity, onLayoutAppBarListener);
        mLayoutAppBar.setTabLayoutVisible(true);
    }

    @Bindable
    public int getViewPagerVisibility() {
        return mViewPagerVisibility;
    }

    void setViewPagerVisible(boolean viewPagerVisible) {
        mViewPagerVisibility = viewPagerVisible ? View.VISIBLE : View.GONE;
        notifyPropertyChanged(BR.viewPagerVisibility);

        mLayoutAppBar.setTabLayoutVisible(viewPagerVisible);
    }

    void setSearchViewCollapsed(boolean searchViewCollapsed) {
        mLayoutAppBar.setSearchViewCollapsed(searchViewCollapsed);
    }

    public PagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    public float getBottomFragmentHeight() {
        return mBottomFragmentHeight;
    }

    public LayoutAppBar getLayoutAppBar() {
        return mLayoutAppBar;
    }
}
