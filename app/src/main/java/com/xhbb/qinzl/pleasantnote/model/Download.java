package com.xhbb.qinzl.pleasantnote.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;

/**
 * Created by qinzl on 2017/8/3.
 */

public class Download {

    private int mMusicCode;
    private int mState;
    private String mUrl;
    private int mProgress;

    public Download(Cursor cursor) {
        mMusicCode = cursor.getInt(cursor.getColumnIndex(DownloadContract._MUSIC_CODE));
        mState = cursor.getInt(cursor.getColumnIndex(DownloadContract._STATE));
        mUrl = cursor.getString(cursor.getColumnIndex(DownloadContract._URL));
        mProgress = cursor.getInt(cursor.getColumnIndex(DownloadContract._PROGRESS));
    }

//    public Download(int musicCode, int state, String url, int progress) {
//        mMusicCode = musicCode;
//        mState = state;
//        mUrl = url;
//        mProgress = progress;
//    }

    public ContentValues getDownloadValues() {
        ContentValues downloadValues = new ContentValues();

        downloadValues.put(DownloadContract._MUSIC_CODE, mMusicCode);
        downloadValues.put(DownloadContract._STATE, mState);
        downloadValues.put(DownloadContract._URL, mUrl);
        downloadValues.put(DownloadContract._PROGRESS, mProgress);

        return downloadValues;
    }

    public int getMusicCode() {
        return mMusicCode;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setState(int state) {
        mState = state;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }
}
