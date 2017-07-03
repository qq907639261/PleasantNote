package com.xhbb.qinzl.pleasantnote.layoutbinding;

/**
 * Created by qinzl on 2017/7/3.
 */

public class ActivityFragment {

    private OnActivityFragmentListener mListener;

    ActivityFragment(OnActivityFragmentListener listener) {
        mListener = listener;
    }

    public void onDrawerOpened() {
        mListener.onDrawerOpened();
    }

    interface OnActivityFragmentListener {

        void onDrawerOpened();
    }
}
