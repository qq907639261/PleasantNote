package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;

/**
 * Created by qinzl on 2017/7/3.
 */

public class LayoutMain extends BaseObservable {

    private float mBottomFragmentHeight;
    private LayoutAppBar mLayoutAppBar;

    LayoutMain(float bottomFragmentHeight,
               LayoutAppBar.OnLayoutAppBarListener onLayoutAppBarListener) {
        mBottomFragmentHeight = bottomFragmentHeight;

        mLayoutAppBar = new LayoutAppBar(onLayoutAppBarListener);
    }

    public float getBottomFragmentHeight() {
        return mBottomFragmentHeight;
    }

    public LayoutAppBar getLayoutAppBar() {
        return mLayoutAppBar;
    }
}
