package com.xhbb.qinzl.pleasantnote.async;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xhbb.qinzl.pleasantnote.data.Contracts;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {

    private static final String EXTRA_MUSIC_URL = Contracts.AUTHORITY + ".EXTRA_MUSIC_URL";

    private MediaPlayer mMediaPlayer;

    public static Intent newIntent(Context context, String musicUrl) {
        return newIntent(context).putExtra(EXTRA_MUSIC_URL, musicUrl)
                .setAction(Contracts.ACTION_PLAY_MUSIC);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MusicService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case Contracts.ACTION_PLAY_MUSIC:
                playMusic(intent);
                break;
            default:
        }

        return START_NOT_STICKY;
    }

    private void playMusic(Intent intent) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(intent.getStringExtra(EXTRA_MUSIC_URL));
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
