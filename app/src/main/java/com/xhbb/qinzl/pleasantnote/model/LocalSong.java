package com.xhbb.qinzl.pleasantnote.model;

import android.database.Cursor;
import android.os.Environment;

import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

import java.io.File;

/**
 * Created by qinzl on 2017/8/11.
 */

public class LocalSong {

    private int mMusicCode;
    private String mMusicName;
    private String mSingerName;
    private int mDownloadState;
    private int mDownloadProgress;
    private String mPlayUrl;
    private int mTotalSeconds;
    private String mBigPictureUrl;
    private String mSmallPictureUrl;
    private int mMusicType;

    public LocalSong(Cursor cursor) {
        mMusicCode = cursor.getInt(cursor.getColumnIndex(MusicContract._CODE));
        mMusicName = cursor.getString(cursor.getColumnIndex(MusicContract._NAME));
        mSingerName = cursor.getString(cursor.getColumnIndex(MusicContract._SINGER));
        mDownloadState = cursor.getInt(cursor.getColumnIndex(DownloadContract._STATE));
        mDownloadProgress = cursor.getInt(cursor.getColumnIndex(DownloadContract._PROGRESS));
        mMusicType = cursor.getInt(cursor.getColumnIndex(MusicContract._TYPE));
        mTotalSeconds = cursor.getInt(cursor.getColumnIndex(MusicContract._TOTAL_SECONDS));
        mBigPictureUrl = cursor.getString(cursor.getColumnIndex(MusicContract._BIG_PICTURE_URL));
        mSmallPictureUrl = cursor.getString(cursor.getColumnIndex(MusicContract._SMALL_PICTURE_URL));

        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File musicFile = new File(musicDir, mMusicCode + ".mp3");
        mPlayUrl = musicFile.getPath();
    }

    public int getMusicCode() {
        return mMusicCode;
    }

    public String getMusicName() {
        return mMusicName;
    }

    public String getSingerName() {
        return mSingerName;
    }

    public int getDownloadState() {
        return mDownloadState;
    }

    public int getDownloadProgress() {
        return mDownloadProgress;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public int getTotalSeconds() {
        return mTotalSeconds;
    }

    public String getBigPictureUrl() {
        return mBigPictureUrl;
    }

    public String getSmallPictureUrl() {
        return mSmallPictureUrl;
    }

    public int getMusicType() {
        return mMusicType;
    }
}
