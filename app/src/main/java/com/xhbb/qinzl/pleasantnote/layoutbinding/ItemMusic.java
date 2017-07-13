package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;

import com.xhbb.qinzl.pleasantnote.R;

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
            setDuration(context, seconds);
        }
    }

    private void setDuration(Context context, int seconds) {
        int minutes = seconds / 60;
        int secondOfMinute = seconds % 60;
        mDuration = context.getString(R.string.format_music_duration, minutes, secondOfMinute);
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
