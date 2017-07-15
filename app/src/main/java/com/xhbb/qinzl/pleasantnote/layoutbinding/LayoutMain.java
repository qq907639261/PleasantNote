package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.view.PagerAdapter;
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

    LayoutMain(Context context, PagerAdapter pagerAdapter,
               LayoutAppBar.OnLayoutAppBarListener onLayoutAppBarListener) {
        mBottomFragmentHeight = context.getResources().getDimension(R.dimen.bottomPlayFragmentHeight);
        mPagerAdapter = pagerAdapter;
        mViewPagerVisibility = View.VISIBLE;

        mLayoutAppBar = new LayoutAppBar(onLayoutAppBarListener);
        mLayoutAppBar.setTabLayoutVisible(true);
    }

    public void onClickBottomFragment() {
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
