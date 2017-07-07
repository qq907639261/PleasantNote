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

    public ItemMusic(Context context, String imageUrl, String music, String singer,
                     int totalSeconds) {
        mImageUrl = imageUrl;
        mMusic = music;
        mSinger = singer;

        if (totalSeconds != 0) {
            setDuration(context, totalSeconds);
        }
    }

    private void setDuration(Context context, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        mDuration = context.getString(R.string.format_music_duration, minutes, seconds);
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
}
