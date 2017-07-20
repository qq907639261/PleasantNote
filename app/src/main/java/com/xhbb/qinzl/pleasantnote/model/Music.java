package com.xhbb.qinzl.pleasantnote.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/6.
 */

public class Music implements Parcelable {

    private String mName;
    private int mSeconds;
    private int mCode;
    private String mSinger;
    private String mBigPicture;
    private String mSmallPicture;
    private String mPlayUrl;
    private String mDownloadUrl;
    private int mRankingCode;
    private int mMusicType;

    public Music(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex(MusicContract._NAME));
        mSeconds = cursor.getInt(cursor.getColumnIndex(MusicContract._SECONDS));
        mCode = cursor.getInt(cursor.getColumnIndex(MusicContract._CODE));
        mSinger = cursor.getString(cursor.getColumnIndex(MusicContract._SINGER));
        mBigPicture = cursor.getString(cursor.getColumnIndex(MusicContract._BIG_PICTURE));
        mSmallPicture = cursor.getString(cursor.getColumnIndex(MusicContract._SMALL_PICTURE));
        mPlayUrl = cursor.getString(cursor.getColumnIndex(MusicContract._PLAY_URL));
        mDownloadUrl = cursor.getString(cursor.getColumnIndex(MusicContract._DOWNLOAD_URL));
        mRankingCode = cursor.getInt(cursor.getColumnIndex(MusicContract._RANKING_CODE));
        mMusicType = cursor.getInt(cursor.getColumnIndex(MusicContract._TYPE));
    }

    private Music(Parcel in) {
        mName = in.readString();
        mSeconds = in.readInt();
        mCode = in.readInt();
        mSinger = in.readString();
        mBigPicture = in.readString();
        mSmallPicture = in.readString();
        mPlayUrl = in.readString();
        mDownloadUrl = in.readString();
        mRankingCode = in.readInt();
        mMusicType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeInt(mSeconds);
        parcel.writeInt(mCode);
        parcel.writeString(mSinger);
        parcel.writeString(mBigPicture);
        parcel.writeString(mSmallPicture);
        parcel.writeString(mPlayUrl);
        parcel.writeString(mDownloadUrl);
        parcel.writeInt(mRankingCode);
        parcel.writeInt(mMusicType);
    }

    public ContentValues getMusicValues() {
        ContentValues musicValues = new ContentValues();

        musicValues.put(MusicContract._NAME, mName);
        musicValues.put(MusicContract._CODE, mCode);
        musicValues.put(MusicContract._SECONDS, mSeconds);
        musicValues.put(MusicContract._PLAY_URL, mPlayUrl);
        musicValues.put(MusicContract._DOWNLOAD_URL, mDownloadUrl);
        musicValues.put(MusicContract._SMALL_PICTURE, mSmallPicture);
        musicValues.put(MusicContract._BIG_PICTURE, mBigPicture);
        musicValues.put(MusicContract._SINGER, mSinger);
        musicValues.put(MusicContract._RANKING_CODE, mRankingCode);
        musicValues.put(MusicContract._TYPE, mMusicType);

        return musicValues;
    }

    public String getName() {
        return mName;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public int getCode() {
        return mCode;
    }

    public String getSinger() {
        return mSinger;
    }

    public String getBigPicture() {
        return mBigPicture;
    }

    public String getSmallPicture() {
        return mSmallPicture;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public int getRankingCode() {
        return mRankingCode;
    }

    public int getMusicType() {
        return mMusicType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}
