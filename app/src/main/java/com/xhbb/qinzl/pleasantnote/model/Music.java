package com.xhbb.qinzl.pleasantnote.model;

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
    private long mCode;
    private String mSinger;
    private long mSingerCode;
    private String mBigPicture;
    private String mSmallPicture;
    private String mPlayUrl;
    private String mDownloadUrl;

    public Music(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex(MusicContract._NAME));
        mSeconds = cursor.getInt(cursor.getColumnIndex(MusicContract._SECONDS));
        mCode = cursor.getLong(cursor.getColumnIndex(MusicContract._CODE));
        mSinger = cursor.getString(cursor.getColumnIndex(MusicContract._SINGER));
        mSingerCode = cursor.getLong(cursor.getColumnIndex(MusicContract._SINGER_CODE));
        mBigPicture = cursor.getString(cursor.getColumnIndex(MusicContract._BIG_PICTURE));
        mSmallPicture = cursor.getString(cursor.getColumnIndex(MusicContract._SMALL_PICTURE));
        mPlayUrl = cursor.getString(cursor.getColumnIndex(MusicContract._PLAY_URL));
        mDownloadUrl = cursor.getString(cursor.getColumnIndex(MusicContract._DOWNLOAD_URL));
    }

    private Music(Parcel in) {
        mName = in.readString();
        mSeconds = in.readInt();
        mCode = in.readLong();
        mSinger = in.readString();
        mSingerCode = in.readLong();
        mBigPicture = in.readString();
        mSmallPicture = in.readString();
        mPlayUrl = in.readString();
        mDownloadUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeInt(mSeconds);
        parcel.writeLong(mCode);
        parcel.writeString(mSinger);
        parcel.writeLong(mSingerCode);
        parcel.writeString(mBigPicture);
        parcel.writeString(mSmallPicture);
        parcel.writeString(mPlayUrl);
        parcel.writeString(mDownloadUrl);
    }

    public String getName() {
        return mName;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public long getCode() {
        return mCode;
    }

    public String getSinger() {
        return mSinger;
    }

    public long getSingerCode() {
        return mSingerCode;
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
