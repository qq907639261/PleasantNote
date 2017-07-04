package com.xhbb.qinzl.pleasantnote.layoutbinding;

/**
 * Created by qinzl on 2017/7/3.
 */

public class ActivityFragment {

    private OnActivityFragmentListener mListener;
    private float mBottomFragmentHeight;

    ActivityFragment(float bottomFragmentHeight, OnActivityFragmentListener listener) {
        mBottomFragmentHeight = bottomFragmentHeight;
        mListener = listener;
    }

    public float getBottomFragmentHeight() {
        return mBottomFragmentHeight;
    }

    public void onDrawerOpened() {
        mListener.onDrawerOpened();
    }

    interface OnActivityFragmentListener {

        void onDrawerOpened();
    }
}
