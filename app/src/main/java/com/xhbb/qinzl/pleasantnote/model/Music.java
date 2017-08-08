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
    private String mSingerName;
    private String mBigPictureUrl;
    private String mSmallPictureUrl;
    private String mPlayUrl;
    private int mRankingCode;
    private int mMusicType;

    public Music(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex(MusicContract._NAME));
        mCode = cursor.getInt(cursor.getColumnIndex(MusicContract._CODE));
        mPlayUrl = cursor.getString(cursor.getColumnIndex(MusicContract._PLAY_URL));
        mMusicType = cursor.getInt(cursor.getColumnIndex(MusicContract._TYPE));
        mSeconds = cursor.getInt(cursor.getColumnIndex(MusicContract._TOTAL_SECONDS));
        mSingerName = cursor.getString(cursor.getColumnIndex(MusicContract._SINGER));
        mBigPictureUrl = cursor.getString(cursor.getColumnIndex(MusicContract._BIG_PICTURE_URL));
        mSmallPictureUrl = cursor.getString(cursor.getColumnIndex(MusicContract._SMALL_PICTURE_URL));
        mRankingCode = cursor.getInt(cursor.getColumnIndex(MusicContract._RANKING_CODE));
    }

    private Music(Parcel in) {
        mName = in.readString();
        mSeconds = in.readInt();
        mCode = in.readInt();
        mSingerName = in.readString();
        mBigPictureUrl = in.readString();
        mSmallPictureUrl = in.readString();
        mPlayUrl = in.readString();
        mRankingCode = in.readInt();
        mMusicType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeInt(mSeconds);
        parcel.writeInt(mCode);
        parcel.writeString(mSingerName);
        parcel.writeString(mBigPictureUrl);
        parcel.writeString(mSmallPictureUrl);
        parcel.writeString(mPlayUrl);
        parcel.writeInt(mRankingCode);
        parcel.writeInt(mMusicType);
    }

    public ContentValues getMusicValues() {
        ContentValues musicValues = new ContentValues();

        musicValues.put(MusicContract._NAME, mName);
        musicValues.put(MusicContract._CODE, mCode);
        musicValues.put(MusicContract._PLAY_URL, mPlayUrl);
        musicValues.put(MusicContract._TYPE, mMusicType);
        musicValues.put(MusicContract._TOTAL_SECONDS, mSeconds);
        musicValues.put(MusicContract._SMALL_PICTURE_URL, mSmallPictureUrl);
        musicValues.put(MusicContract._BIG_PICTURE_URL, mBigPictureUrl);
        musicValues.put(MusicContract._SINGER, mSingerName);
        musicValues.put(MusicContract._RANKING_CODE, mRankingCode);

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

    public String getSingerName() {
        return mSingerName;
    }

    public String getBigPictureUrl() {
        return mBigPictureUrl;
    }

    public String getSmallPictureUrl() {
        return mSmallPictureUrl;
    }

    public String getPlayUrl() {
        return mPlayUrl;
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
