package com.xhbb.qinzl.pleasantnote.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/6.
 */

public class MainTasks {

    public static void updateMusicData(Context context, ContentValues[] musicValueses,
                                       boolean firstPage) {
        ContentResolver contentResolver = context.getContentResolver();
        if (firstPage) {
            String deleteWhere = MusicContract._QUERY + " NOT NULL";
            contentResolver.delete(MusicContract.URI, deleteWhere, null);
        }
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }

    public static void updateMusicData(Context context, ContentValues[] musicValueses,
                                       int rankingId) {
        String deleteWhere = MusicContract._RANKING_ID + "=?";
        String[] deleteSelectionArgs = {String.valueOf(rankingId)};

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(MusicContract.URI, deleteWhere, deleteSelectionArgs);
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }
}
