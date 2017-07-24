package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.xhbb.qinzl.pleasantnote.BR;

/**
 * Created by qinzl on 2017/7/13.
 */

public class FragmentBottomPlay extends BaseObservable {

    private String mImageUrl;
    private String mMusicName;
    private String mSinger;
    private int mPlaySwitcherDisplayedChild;
    private OnFragmentBottomPlayListener mListener;

    public FragmentBottomPlay(OnFragmentBottomPlayListener listener) {
        mListener = listener;
    }

    @Bindable
    public int getPlaySwitcherDisplayedChild() {
        return mPlaySwitcherDisplayedChild;
    }

    public void setPlaySwitcherDisplayedChild(boolean played) {
        mPlaySwitcherDisplayedChild = played ? 1 : 0;
        notifyPropertyChanged(BR.playSwitcherDisplayedChild);
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

    public void onClickBottomPlayFragment() {
        mListener.onClickBottomPlayFragment();
    }

    public void onClickPlayButton() {
        mListener.onClickPlayButton();
    }

    public void onClickPauseButton() {
        mListener.onClickPauseButton();
    }

    public void onClickNextButton() {
        mListener.onClickNextButton();
    }

    public interface OnFragmentBottomPlayListener {

        void onClickPlayButton();
        void onClickPauseButton();
        void onClickNextButton();
        void onClickBottomPlayFragment();
    }
}
