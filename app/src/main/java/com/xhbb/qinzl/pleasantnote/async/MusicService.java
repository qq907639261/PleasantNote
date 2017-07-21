package com.xhbb.qinzl.pleasantnote.async;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.io.IOException;

public class MusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    public static final String ACTION_INIT_MUSIC = Contracts.AUTHORITY + ".ACTION_INIT_MUSIC";
    public static final String ACTION_SEND_MUSIC = Contracts.AUTHORITY + ".ACTION_SEND_MUSIC";
    public static final String ACTION_PLAY_NEW_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_NEW_MUSIC";
    public static final String ACTION_PLAY_OR_PAUSE = Contracts.AUTHORITY + ".ACTION_PLAY_OR_PAUSE";
    public static final String ACTION_PLAY_NEXT = Contracts.AUTHORITY + ".ACTION_PLAY_NEXT";
    public static final String ACTION_START_FOREGROUND = Contracts.AUTHORITY + ".ACTION_START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = Contracts.AUTHORITY + ".ACTION_STOP_FOREGROUND";

    public static final String EXTRA_MUSIC = Contracts.AUTHORITY + ".EXTRA_MUSIC";
    public static final int LIMIT_VALUE_OF_HISTORY_MUSIC = 50;

    private static final String EXTRA_DATA_POSITION = Contracts.AUTHORITY + ".EXTRA_DATA_POSITION";

    private MediaPlayer mMediaPlayer;
    private Music mMusic;
    private Notification mNotification;
    private Cursor mCursor;
    private QueryMoreMusicTask mQueryMoreMusicTask;
    private int mDataPosition;
    private InitMusicTask mInitMusicTask;

    public static Intent newIntent(Context context, String action) {
        return new Intent(context, MusicService.class)
                .setAction(action);
    }

    public static Intent newIntent(Context context, String action, Music music, int dataPosition) {
        return newIntent(context, action).putExtra(EXTRA_MUSIC, music)
                .putExtra(EXTRA_DATA_POSITION, dataPosition);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotification = NotificationUtils.getForegroundNotification(getApplicationContext());
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_INIT_MUSIC:
                initMusic();
                break;
            case ACTION_SEND_MUSIC:
                sendMusicData();
                break;
            case ACTION_PLAY_NEW_MUSIC:
                playNewMusic(intent);
                break;
            case ACTION_PLAY_OR_PAUSE:
                playOrPause();
                break;
            case ACTION_PLAY_NEXT:
                playNext();
                break;
            case ACTION_START_FOREGROUND:
                startForeground(1, mNotification);
                break;
            case ACTION_STOP_FOREGROUND:
                stopForeground(true);
                break;
            default:
        }
    }

    private void initMusic() {
        if (mInitMusicTask != null) {
            mInitMusicTask.cancel(true);
        }
        mInitMusicTask = new InitMusicTask();
        mInitMusicTask.execute();
    }

    private void playNewMusic(Intent intent) {
        mMusic = intent.getParcelableExtra(EXTRA_MUSIC);
        mDataPosition = intent.getIntExtra(EXTRA_DATA_POSITION, 0);
        restartMusic();
        queryMoreMusic();
    }

    private void queryMoreMusic() {
        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(true);
        }
        mQueryMoreMusicTask = new QueryMoreMusicTask();
        mQueryMoreMusicTask.execute();
    }

    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            sendLocalBroadcast(Contracts.ACTION_MUSIC_STOPPED);
        } else {
            mMediaPlayer.start();
            if (mMediaPlayer.isPlaying()) {
                sendLocalBroadcast(Contracts.ACTION_MUSIC_PLAYED);
            }
        }
    }

    private void sendLocalBroadcast(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void restartMusic() {
        if (mMusic == null) {
            return;
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusic.getPlayUrl());
            mMediaPlayer.prepareAsync();
            sendMusicData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mInitMusicTask != null) {
            mInitMusicTask.cancel(true);
        }
        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(true);
        }

        CleanUpHistoryMusicJob.cancelJob();
        cleanUpHistoryMusic();

        mMediaPlayer.release();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void cleanUpHistoryMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainTasks.cleanUpHistoryMusic(getApplicationContext());
            }
        }).start();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        saveCurrentMusic(mMusic);
    }

    private void saveCurrentMusic(final Music music) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = getContentResolver();
                ContentValues musicValues = music.getMusicValues();
                musicValues.put(MusicContract._TYPE, MusicType.HISTORY);

                int musicCode = music.getCode();
                String where = MusicContract._CODE + "=" + musicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.HISTORY;

                contentResolver.delete(MusicContract.URI, where, null);
                contentResolver.insert(MusicContract.URI, musicValues);
            }
        }).start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (noCursorData()) {
            sendLocalBroadcast(Contracts.ACTION_MUSIC_STOPPED);
        } else {
            playNext();
        }
    }

    private void playNext() {
        if (noCursorData()) {
            return;
        }

        if (!mCursor.moveToPosition(++mDataPosition)) {
            mCursor.moveToFirst();
            mDataPosition = 0;
        }

        mMusic = new Music(mCursor);
        restartMusic();
    }

    private boolean noCursorData() {
        return mCursor == null || mCursor.getCount() == 0;
    }

    private void sendMusicData() {
        if (mMusic != null) {
            Intent intent = new Intent(Contracts.ACTION_CURRENT_MUSIC_UPDATED);
            intent.putExtra(EXTRA_MUSIC, mMusic);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private class InitMusicTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            String selection = MusicContract._TYPE + "=" + MusicType.HISTORY;
            String sortOrder = MusicContract._ID + " DESC LIMIT " + LIMIT_VALUE_OF_HISTORY_MUSIC;

            Cursor cursor = getContentResolver().query(MusicContract.URI, null, selection, null, sortOrder);
            cursor = handleCancelled(isCancelled(), cursor);

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            setCursor(cursor);
            restartMusic();
        }
    }

    private class QueryMoreMusicTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            int musicType = mMusic.getMusicType();

            String selection = MusicContract._TYPE + "=" + musicType;
            String sortOrder = null;

            if (musicType == MusicType.RANKING) {
                selection += " AND " + MusicContract._RANKING_CODE + "=" + mMusic.getRankingCode();
            } else if (musicType == MusicType.HISTORY) {
                sortOrder = MusicContract._ID + " DESC LIMIT " + LIMIT_VALUE_OF_HISTORY_MUSIC;
            }

            Cursor cursor = getContentResolver().query(MusicContract.URI, null, selection, null, sortOrder);
            cursor = handleCancelled(isCancelled(), cursor);

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            setCursor(cursor);
        }
    }

    private Cursor handleCancelled(boolean cancelled, Cursor cursor) {
        if (cancelled && cursor != null) {
            cursor.close();
            cursor = null;
        }
        return cursor;
    }

    private void setCursor(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;

        if (mCursor.moveToPosition(mDataPosition)) {
            mMusic = new Music(mCursor);
        }
    }
}
