package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/6.
 */

public class ItemMusic {

    private Context mContext;
    private String mImageUrl;
    private String mMusic;
    private String mSinger;
    private int mSeconds;

    public ItemMusic(Context context, String imageUrl, String music, String singer, int seconds) {
        mContext = context;
        mImageUrl = imageUrl;
        mMusic = music;
        mSinger = singer;
        mSeconds = seconds;
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
        if (mSeconds == 0) {
            return null;
        }

        int minutes = mSeconds / 60;
        int seconds = mSeconds % 60;

        String formattedSeconds = String.valueOf(seconds);
        if (seconds < 10) {
            formattedSeconds = "0" + seconds;
        }

        return mContext.getString(R.string.format_music_duration, minutes, formattedSeconds);
    }
}
