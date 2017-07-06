package com.xhbb.qinzl.pleasantnote.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;

/**
 * Created by qinzl on 2017/7/6.
 */

public class MainTasks {

    public static void updateMusicData(final Context context, final ContentValues[] musicValueses,
                                       final boolean firstPage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = context.getContentResolver();
                if (firstPage) {
                    String deleteWhere = MusicContract._QUERY + " NOT NULL";
                    contentResolver.delete(MusicContract.URI, deleteWhere, null);
                }
                contentResolver.bulkInsert(MusicContract.URI, musicValueses);
            }
        }).start();
    }

    public static void updateMusicData(final Context context, final ContentValues[] musicValueses,
                                       final int rankingId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String deleteWhere = MusicContract._RANKING_ID + "=?";
                String[] deleteSelectionArgs = {String.valueOf(rankingId)};

                ContentResolver contentResolver = context.getContentResolver();
                contentResolver.delete(MusicContract.URI, deleteWhere, deleteSelectionArgs);
                contentResolver.bulkInsert(MusicContract.URI, musicValueses);
            }
        }).start();
    }
}
