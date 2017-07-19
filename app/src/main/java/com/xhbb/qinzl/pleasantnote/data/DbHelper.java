package com.xhbb.qinzl.pleasantnote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/4.
 */

class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pleasant_note.db";
    private static final int DB_VERSION = 6;

    private static final String CREATE_TABLE_MUSIC =
            "CREATE TABLE " + MusicContract.TABLE + "(" +
                    MusicContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MusicContract._NAME + " TEXT," +
                    MusicContract._SECONDS + " INTEGER," +
                    MusicContract._CODE + " LONG," +
                    MusicContract._SINGER + " TEXT," +
                    MusicContract._BIG_PICTURE + " TEXT," +
                    MusicContract._SMALL_PICTURE + " TEXT," +
                    MusicContract._PLAY_URL + " TEXT," +
                    MusicContract._DOWNLOAD_URL + " TEXT," +
                    MusicContract._RANKING_CODE + " INTEGER," +
                    MusicContract._TYPE + " INTEGER" +
                    ")";

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_MUSIC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.execSQL("DROP TABLE " + MusicContract.TABLE);
            onCreate(sqLiteDatabase);

            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }
}
