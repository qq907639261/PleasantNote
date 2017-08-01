package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/20.
 */

public class ActivityPlay extends BaseObservable {

    private String mLyrics;
    private Drawable mBigPicture;
    private Context mContext;
    private BaseAdapter mPlaySpinnerAdater;
    private int mSpinnerSelection;

    public ActivityPlay(Context context, BaseAdapter playSpinnerAdater, int spinnerSelection) {
        mContext = context;
        mPlaySpinnerAdater = playSpinnerAdater;
        mSpinnerSelection = spinnerSelection;
    }

    public int getSpinnerSelection() {
        return mSpinnerSelection;
    }

    @Bindable
    public String getLyrics() {
        return mLyrics;
    }

    public void setLyrics(String lyrics) {
        mLyrics = lyrics;
        notifyPropertyChanged(BR.lyrics);
    }

    public BaseAdapter getPlaySpinnerAdater() {
        return mPlaySpinnerAdater;
    }

    @Bindable
    public Drawable getBigPicture() {
        return mBigPicture;
    }

    public void setBigPicture(Drawable bigPicture) {
        mBigPicture = bigPicture;
        notifyPropertyChanged(BR.bigPicture);
    }

    public Context getContext() {
        return mContext;
    }
}
