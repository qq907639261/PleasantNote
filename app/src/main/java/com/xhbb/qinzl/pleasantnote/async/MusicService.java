package com.xhbb.qinzl.pleasantnote.async;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xhbb.qinzl.pleasantnote.data.Contracts;

import java.io.IOException;

public class MusicService extends Service
        implements MediaPlayer.OnPreparedListener {

    private static final String EXTRA_MUSIC_URL = Contracts.AUTHORITY + ".EXTRA_MUSIC_URL";

    private MediaPlayer mMediaPlayer;

    public static Intent newIntent(Context context, String action, String musicUrl) {
        return newIntent(context, action)
                .putExtra(EXTRA_MUSIC_URL, musicUrl);
    }

    public static Intent newIntent(Context context, String action) {
        return new Intent(context, MusicService.class)
                .setAction(action);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();

        Notification notification = NotificationUtils.getForegroundNotification(getApplicationContext());
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case Contracts.ACTION_RESET:
                String musicUrl = intent.getStringExtra(EXTRA_MUSIC_URL);
                reset(musicUrl);
                break;
            case Contracts.ACTION_PLAY_PAUSE:
                playOrPause();
                break;
            default:
        }
    }

    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    private void reset(String musicUrl) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(musicUrl);
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
