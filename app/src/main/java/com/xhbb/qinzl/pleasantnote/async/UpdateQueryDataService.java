package com.xhbb.qinzl.pleasantnote.async;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.xhbb.qinzl.pleasantnote.common.Enums.DataUpdatedState;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class UpdateQueryDataService extends IntentService {

    public static final String EXTRA_DATA_UPDATED_STATE = Contracts.AUTHORITY + ".EXTRA_DATA_UPDATED_STATE";

    private static final String TAG = "UpdateQueryDataService";

    private static final String EXTRA_MUSIC_JSON = Contracts.AUTHORITY + ".EXTRA_MUSIC_JSON";
    private static final String EXTRA_FIRST_PAGE = Contracts.AUTHORITY + ".EXTRA_FIRST_PAGE";

    public static Intent newIntent(Context context, String musicJson, boolean firstPage) {
        return new Intent(context, UpdateQueryDataService.class)
                .putExtra(EXTRA_MUSIC_JSON, musicJson)
                .putExtra(EXTRA_FIRST_PAGE, firstPage);
    }

    public UpdateQueryDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int updatedState = 0;
        Context context = getApplicationContext();
        String musicJson = intent.getStringExtra(EXTRA_MUSIC_JSON);
        boolean firstPage = intent.getBooleanExtra(EXTRA_FIRST_PAGE, false);

        ContentValues[] musicValueses = JsonUtils.getMusicValuesesByQuery(musicJson);

        if (musicValueses != null) {
            if (musicValueses.length < NetworkUtils.MAX_COUNT_OF_EACH_PAGE) {
                updatedState = DataUpdatedState.SCROLLED_TO_END_UPDATE;
            }

            MainTasks.updateMusicDataByQuery(context, musicValueses, firstPage);
        } else {
            if (firstPage) {
                updatedState = DataUpdatedState.EMPTY_DATA;
            } else {
                updatedState = DataUpdatedState.SCROLLED_TO_END_NO_UPDATE;
            }
        }

        Intent broadcastIntent = new Intent(Contracts.ACTION_MUSIC_DATA_UPDATED);
        broadcastIntent.putExtra(EXTRA_DATA_UPDATED_STATE, updatedState);

        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}
