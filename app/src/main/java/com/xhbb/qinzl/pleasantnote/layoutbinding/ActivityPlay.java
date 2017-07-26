package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.widget.BaseAdapter;

import com.xhbb.qinzl.pleasantnote.BR;
import com.xhbb.qinzl.pleasantnote.R;
import com.xhbb.qinzl.pleasantnote.common.GlideApp;

import java.util.concurrent.ExecutionException;

/**
 * Created by qinzl on 2017/7/20.
 */

public class ActivityPlay extends BaseObservable {

    private String mLyrics;
    private Drawable mBigPicture;
    private Context mContext;
    private int mLyricsColor;
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
    public int getLyricsColor() {
        return mLyricsColor;
    }

    public void setLyricsColor(Context context, boolean searchLyricsFailed) {
        if (searchLyricsFailed) {
            mLyricsColor = ActivityCompat.getColor(context, R.color.tipsText);
        } else {
            mLyricsColor = ActivityCompat.getColor(context, R.color.lyrics);
        }
        notifyPropertyChanged(BR.lyricsColor);
    }

    @Bindable
    public Drawable getBigPicture() {
        return mBigPicture;
    }

    public void setBigPicture(final Context context, final String bigPicture) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mBigPicture = GlideApp.with(context)
                            .asDrawable()
                            .load(bigPicture)
                            .error(R.drawable.empty_image)
                            .centerCrop(context)
                            .submit()
                            .get();
                    mBigPicture.setAlpha(50);
                    notifyPropertyChanged(BR.bigPicture);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Context getContext() {
        return mContext;
    }
}
