package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;

import com.xhbb.qinzl.pleasantnote.common.DateTimeUtils;

/**
 * Created by qinzl on 2017/7/6.
 */

public class ItemMusic {

    private String mImageUrl;
    private String mMusic;
    private String mSinger;
    private String mDuration;
    private int mItemPosition;
    private OnItemMusicListener mListener;

    public ItemMusic(Context context, String imageUrl, String music, String singer, int seconds,
                     int itemPosition, OnItemMusicListener listener) {
        mImageUrl = imageUrl;
        mMusic = music;
        mSinger = singer;
        mItemPosition = itemPosition;
        mListener = listener;

        if (seconds != 0) {
            mDuration = DateTimeUtils.getPlayDuration(context, seconds);
        }
    }

    public void onClickItem() {
        mListener.onClickItem(mItemPosition);
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getMusic() {
        return mMusic;
    }

    public String getSinger() {
        return mSinger;
    }

    public String getDuration() {
        return mDuration;
    }

    public interface OnItemMusicListener {

        void onClickItem(int itemPosition);
    }
}
