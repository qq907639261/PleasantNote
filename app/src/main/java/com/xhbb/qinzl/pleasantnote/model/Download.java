package com.xhbb.qinzl.pleasantnote.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;

/**
 * Created by qinzl on 2017/8/3.
 */

public class Download {

    private int mMusicId;
    private int mState;
    private String mUrl;
    private int mProgress;

    public Download(Cursor cursor) {

    }

    public ContentValues getDownloadValues() {
        ContentValues downloadValues = new ContentValues();

        downloadValues.put(DownloadContract._MUSIC_ID, mMusicId);
        downloadValues.put(DownloadContract._STATE, mState);
        downloadValues.put(DownloadContract._URL, mUrl);
        downloadValues.put(DownloadContract._PROGRESS, mProgress);

        return downloadValues;
    }

    public int getMusicId() {
        return mMusicId;
    }

    public int getState() {
        return mState;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getProgress() {
        return mProgress;
    }
}
