package com.xhbb.qinzl.pleasantnote.async;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.xhbb.qinzl.pleasantnote.data.Contracts;

public class UpdateMusicService extends IntentService {

    private static final String EXTRA_MUSIC_JSON = Contracts.AUTHORITY + "_EXTRA_MUSIC_JSON";

    public static Intent newIntent(Context context, String json) {
        return new Intent(context, UpdateMusicService.class)
                .putExtra(EXTRA_MUSIC_JSON, json);
    }

    public UpdateMusicService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: 2017/7/11 服务+广播
    }
}
