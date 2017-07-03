package com.xhbb.qinzl.pleasantnote.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/3.
 */

public class RecyclerViewModel extends BaseObservable {

    private boolean mAutoRefreshing;
    private String mErrorText;

    public RecyclerViewModel() {
        mAutoRefreshing = true;
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
}
