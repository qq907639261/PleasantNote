package com.xhbb.qinzl.pleasantnote.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/6.
 */

class MainTasks {

    static void updateMusicDataByQuery(Context context, ContentValues[] musicValueses,
                                       boolean firstPage) {
        ContentResolver contentResolver = context.getContentResolver();

        if (firstPage) {
            String where = MusicContract._TYPE + "=" + MusicType.QUERY;

            contentResolver.delete(MusicContract.URI, where, null);
        }
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }

    static void updateMusicData(Context context, ContentValues[] musicValueses, int rankingCode) {
        ContentResolver contentResolver = context.getContentResolver();

        String where = MusicContract._RANKING_CODE + "=" + rankingCode + " AND "
                + MusicContract._TYPE + "=" + MusicType.RANKING;

        contentResolver.delete(MusicContract.URI, where, null);
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }

    static void deleteHistoryMusicIfOutRange(Context context) {
        String[] projection = {"COUNT(*)"};
        String selection = MusicContract._TYPE + "=" + MusicType.HISTORY;
        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(MusicContract.URI, projection, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int currentCount = cursor.getInt(0);
                int differenceCount = currentCount - MusicService.MAX_COUNT_OF_HISTORY_MUSIC;

                if (differenceCount > 0) {
                    String where = MusicContract._ID + " IN (SELECT " + MusicContract._ID
                            + " FROM " + MusicContract.TABLE
                            + " WHERE " + MusicContract._TYPE + "=" + MusicType.HISTORY
                            + " LIMIT " + differenceCount + ")";

                    contentResolver.delete(MusicContract.URI, where, null);
                }
            }
            cursor.close();
        }
    }
}
