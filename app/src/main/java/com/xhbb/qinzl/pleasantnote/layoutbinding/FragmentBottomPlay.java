package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;

import com.xhbb.qinzl.pleasantnote.BR;
import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/13.
 */

public class FragmentBottomPlay extends BaseObservable {

    private String mImageUrl;
    private String mMusicName;
    private String mSinger;
    private boolean mMusicPlaying;
    private Drawable mPlayButtonDrawable;
    private OnFragmentBottomPlayListener mListener;

    public FragmentBottomPlay(Context context, OnFragmentBottomPlayListener listener) {
        mPlayButtonDrawable = ActivityCompat.getDrawable(context, R.drawable.ic_play);
        mListener = listener;
    }

    public FragmentBottomPlay(Context context, String imageUrl, String musicName, String singer,
                              OnFragmentBottomPlayListener listener) {
        mMusicPlaying = true;
        mPlayButtonDrawable = ActivityCompat.getDrawable(context, R.drawable.ic_pause);
        mImageUrl = imageUrl;
        mMusicName = musicName;
        mSinger = singer;
        mListener = listener;
    }

    @Bindable
    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
        notifyPropertyChanged(BR.imageUrl);
    }

    @Bindable
    public String getMusicName() {
        return mMusicName;
    }

    public void setMusicName(String musicName) {
        mMusicName = musicName;
        notifyPropertyChanged(BR.musicName);
    }

    @Bindable
    public String getSinger() {
        return mSinger;
    }

    public void setSinger(String singer) {
        mSinger = singer;
        notifyPropertyChanged(BR.singer);
    }

    public boolean isMusicPlaying() {
        return mMusicPlaying;
    }

    @Bindable
    public Drawable getPlayButtonDrawable() {
        return mPlayButtonDrawable;
    }

    public void changePlayButtonDrawable(Context context) {
        mMusicPlaying = !mMusicPlaying;
        if (mMusicPlaying) {
            mPlayButtonDrawable = ActivityCompat.getDrawable(context, R.drawable.ic_pause);
        } else {
            mPlayButtonDrawable = ActivityCompat.getDrawable(context, R.drawable.ic_play);
        }
        notifyPropertyChanged(BR.playButtonDrawable);
    }

    public void onClickPlayButton() {
        mListener.onClickPlayButton();
    }

    public void onClickNextButton() {
        mListener.onClickNextButton();
    }

    public interface OnFragmentBottomPlayListener {

        void onClickPlayButton();
        void onClickNextButton();
    }
}
