package com.xhbb.qinzl.pleasantnote.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/6.
 */

class MainTasks {

    static void updateMusicData(Context context, ContentValues[] musicValueses,
                                boolean firstPage) {
        ContentResolver contentResolver = context.getContentResolver();
        if (firstPage) {
            String deleteWhere = MusicContract._QUERY + " NOT NULL";
            contentResolver.delete(MusicContract.URI, deleteWhere, null);
        }
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }

    static void updateMusicData(Context context, ContentValues[] musicValueses, int rankingCode) {
        ContentResolver contentResolver = context.getContentResolver();
        String deleteWhere = MusicContract._RANKING_CODE + "=?";
        String[] deleteSelectionArgs = {String.valueOf(rankingCode)};

        contentResolver.delete(MusicContract.URI, deleteWhere, deleteSelectionArgs);
        contentResolver.bulkInsert(MusicContract.URI, musicValueses);
    }
}
