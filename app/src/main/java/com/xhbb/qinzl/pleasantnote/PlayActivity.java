package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class PlayActivity extends AppCompatActivity
        implements Response.Listener<String>,
        Response.ErrorListener {

    private ActivityPlay mActivityPlay;
    private LocalReceiver mLocalReceiver;

    public static void start(Context context) {
        context.startActivity(newIntent(context));
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PlayActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPlayBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_play);

        mActivityPlay = new ActivityPlay(this);
        mLocalReceiver = new LocalReceiver();

        binding.setActivityPlay(mActivityPlay);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerLocalReceiver();
        startService(MusicService.newIntent(this, MusicService.ACTION_SEND_MUSIC));
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contracts.ACTION_MUSIC_STOPPED);
        filter.addAction(Contracts.ACTION_MUSIC_PLAYED);
        filter.addAction(Contracts.ACTION_CURRENT_MUSIC_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.cancelAllRequest(this, null);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        String lyrics = JsonUtils.getLyrics(response);
        mActivityPlay.setLyrics(lyrics);
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case Contracts.ACTION_CURRENT_MUSIC_UPDATED:
                Music music = intent.getParcelableExtra(MusicService.EXTRA_MUSIC);
                mActivityPlay.setBigPicture(this, music.getBigPicture());
                NetworkUtils.addLyricsRequest(this, music.getCode(), this, this);
                break;
            case Contracts.ACTION_MUSIC_PLAYED:

                break;
            case Contracts.ACTION_MUSIC_STOPPED:

                break;
            default:
        }
    }
}
