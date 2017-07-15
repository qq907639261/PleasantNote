package com.xhbb.qinzl.pleasantnote.async;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class UpdateMusicDataService extends IntentService {

    private static final String TAG = "UpdateMusicDataService";

    private static final String EXTRA_MUSIC_JSON = Contracts.AUTHORITY + ".EXTRA_MUSIC_JSON";
    private static final String EXTRA_RANKING_CODE = Contracts.AUTHORITY + ".EXTRA_RANKING_CODE";
    private static final String EXTRA_QUERY = Contracts.AUTHORITY + ".EXTRA_QUERY";
    private static final String EXTRA_FIRST_PAGE = Contracts.AUTHORITY + ".EXTRA_FIRST_PAGE";

    private static final String ACTION_RANKING = Contracts.AUTHORITY + ".ACTION_RANKING";
    private static final String ACTION_QUERY = Contracts.AUTHORITY + ".ACTION_QUERY";

    public static Intent newIntent(Context context, String musicJson, int rankingCode) {
        return new Intent(context, UpdateMusicDataService.class)
                .putExtra(EXTRA_MUSIC_JSON, musicJson)
                .putExtra(EXTRA_RANKING_CODE, rankingCode)
                .setAction(ACTION_RANKING);
    }

    public static Intent newIntent(Context context, String musicJson, String query,
                                   boolean firstPage) {
        return new Intent(context, UpdateMusicDataService.class)
                .putExtra(EXTRA_MUSIC_JSON, musicJson)
                .putExtra(EXTRA_QUERY, query)
                .putExtra(EXTRA_FIRST_PAGE, firstPage)
                .setAction(ACTION_QUERY);
    }

    public UpdateMusicDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        String musicJson = intent.getStringExtra(EXTRA_MUSIC_JSON);

        switch (intent.getAction()) {
            case ACTION_RANKING:
                updateRankingData(intent, context, musicJson);
                break;
            case ACTION_QUERY:
                updateQueryData(intent, context, musicJson);
                break;
            default:
        }
    }

    private void updateQueryData(Intent intent, Context context, String musicJson) {
        String query = intent.getStringExtra(EXTRA_QUERY);
        boolean firstPage = intent.getBooleanExtra(EXTRA_FIRST_PAGE, false);

        ContentValues[] musicValueses = JsonUtils.getMusicValueses(musicJson, query);
        Intent broadcastIntent = null;

        if (musicValueses != null) {
            if (musicValueses.length < NetworkUtils.MAX_COUNT_OF_EACH_PAGE) {
                broadcastIntent = new Intent(Contracts.ACTION_SCROLLED_TO_END);
            }

            MainTasks.updateMusicData(context, musicValueses, firstPage);
        } else {
            if (firstPage) {
                broadcastIntent = new Intent(Contracts.ACTION_EMPTY_DATA);
            } else {
                broadcastIntent = new Intent(Contracts.ACTION_SCROLLED_TO_END);
            }
        }

        if (broadcastIntent != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
        }
    }

    private void updateRankingData(Intent intent, Context context, String musicJson) {
        int rankingCode = intent.getIntExtra(EXTRA_RANKING_CODE, 0);
        ContentValues[] musicValueses = JsonUtils.getMusicValueses(musicJson, rankingCode);
        MainTasks.updateMusicData(context, musicValueses, rankingCode);
    }
}
