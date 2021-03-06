package com.xhbb.qinzl.pleasantnote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/4.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pleasant_note.db";
    private static final int DB_VERSION = 13;

    private static final String CREATE_TABLE_MUSIC =
            "CREATE TABLE " + MusicContract.TABLE + "(" +
                    MusicContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MusicContract._NAME + " TEXT," +
                    MusicContract._TOTAL_SECONDS + " INTEGER," +
                    MusicContract._CODE + " LONG," +
                    MusicContract._SINGER + " TEXT," +
                    MusicContract._BIG_PICTURE_URL + " TEXT," +
                    MusicContract._SMALL_PICTURE_URL + " TEXT," +
                    MusicContract._PLAY_URL + " TEXT," +
                    MusicContract._RANKING_CODE + " INTEGER," +
                    MusicContract._TYPE + " INTEGER" +
                    ")";

    private static final String CREATE_TABLE_DOWNLOAD =
            "CREATE TABLE " + DownloadContract.TABLE + "(" +
                    DownloadContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DownloadContract._MUSIC_CODE + " INTEGER," +
                    DownloadContract._URL + " TEXT," +
                    DownloadContract._STATE + " INTEGER," +
                    DownloadContract._PROGRESS + " INTEGER" +
                    ")";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_MUSIC);
        sqLiteDatabase.execSQL(CREATE_TABLE_DOWNLOAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MusicContract.TABLE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DownloadContract.TABLE);
            onCreate(sqLiteDatabase);

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}
