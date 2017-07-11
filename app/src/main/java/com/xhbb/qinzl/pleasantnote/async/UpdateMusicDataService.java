package com.xhbb.qinzl.pleasantnote.async;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;

public class UpdateMusicDataService extends IntentService {

    private static final String TAG = "UpdateMusicDataService";
    private static final String EXTRA_MUSIC_JSON = Contracts.AUTHORITY + ".EXTRA_MUSIC_JSON";
    private static final String EXTRA_RANKING_CODE = Contracts.AUTHORITY + ".EXTRA_RANKING_CODE";

    public static Intent newIntent(Context context, String musicJson, int rankingCode) {
        return new Intent(context, UpdateMusicDataService.class)
                .putExtra(EXTRA_MUSIC_JSON, musicJson)
                .putExtra(EXTRA_RANKING_CODE, rankingCode);
    }

    public UpdateMusicDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        String musicJson = intent.getStringExtra(EXTRA_MUSIC_JSON);
        int rankingCode = intent.getIntExtra(EXTRA_RANKING_CODE, 0);

        ContentValues[] musicValueses = JsonUtils.getMusicValueses(musicJson, rankingCode);
        MainTasks.updateMusicData(context, musicValueses, rankingCode);
    }
}
