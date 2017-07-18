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

    public static final int MUSIC_ID_BY_INSERT = 0;

    private MediaPlayer mMediaPlayer;
    private Music mMusic;
    private Notification mNotification;
    private Cursor mCursor;
    private QueryMoreMusicIntoCursorTask mQueryMoreMusicIntoCursorTask;

    public static Intent newIntent(Context context, String action) {
        return new Intent(context, MusicService.class)
                .setAction(action);
    }

    public static Intent newIntent(Context context, String action, Music music) {
        return newIntent(context, action).putExtra(EXTRA_MUSIC, music);
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
                new InitMusicTask().execute();
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

    private void playNewMusic(Intent intent) {
        mMusic = intent.getParcelableExtra(EXTRA_MUSIC);
        sendMusicData();
        restartMusic();
        queryMoreMusicIntoCursor();
    }

    private void queryMoreMusicIntoCursor() {
        if (mQueryMoreMusicIntoCursorTask != null) {
            mQueryMoreMusicIntoCursorTask.cancel(true);
        }
        mQueryMoreMusicIntoCursorTask = new QueryMoreMusicIntoCursorTask();
        mQueryMoreMusicIntoCursorTask.execute();
    }

    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            sendLocalBroadcast(Contracts.ACTION_MUSIC_STOPPED);
        } else {
            mMediaPlayer.start();
            sendLocalBroadcast(Contracts.ACTION_MUSIC_PLAYED);
        }
    }

    private void sendLocalBroadcast(String action) {
        if (mMusic != null) {
            Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void restartMusic() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusic.getPlayUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        saveCurrentMusicData();
        mMediaPlayer.release();
        mMediaPlayer = null;

        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void saveCurrentMusicData() {
        if (mMusic == null) {
            return;
        }
        final ContentValues musicValues = mMusic.getMusicValues();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = getContentResolver();

                String where = MusicContract._ID + "=" + MUSIC_ID_BY_INSERT;
                int updatedRows = contentResolver.update(MusicContract.URI, musicValues, where, null);

                if (updatedRows < 1) {
                    musicValues.put(MusicContract._ID, MUSIC_ID_BY_INSERT);
                    contentResolver.insert(MusicContract.URI, musicValues);
                }
            }
        }).start();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext();
    }

    private void playNext() {
        if (mCursor == null) {
            mMediaPlayer.seekTo(0);
            mMediaPlayer.start();
        } else {
            mMusic = new Music(mCursor);
            restartMusic();
            moveToNext(mCursor);
        }
        sendMusicData();
    }

    private void sendMusicData() {
        if (mMusic != null) {
            Intent intent = new Intent(Contracts.ACTION_CURRENT_MUSIC_UPDATED);
            intent.putExtra(EXTRA_MUSIC, mMusic);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    private void moveToNext(Cursor cursor) {
        if (!cursor.moveToNext()) {
            cursor.moveToFirst();
        }
    }

    private class InitMusicTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            String selection = MusicContract._ID + "=" + MUSIC_ID_BY_INSERT;
            return getContentResolver().query(MusicContract.URI, null, selection, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mMusic = new Music(cursor);
                    sendMusicData();
                    restartMusic();
                    queryMoreMusicIntoCursor();
                }
                cursor.close();
            }
        }
    }

    private class QueryMoreMusicIntoCursorTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            String query = mMusic.getQuery();
            String selection;
            String[] selectionArgs;

            if (query != null) {
                selection = MusicContract._QUERY + "=? AND " + MusicContract._ID + ">0";
                selectionArgs = new String[]{query};
            } else {
                selection = MusicContract._RANKING_CODE + "=" + mMusic.getRankingCode()
                        + " AND " + MusicContract._ID + ">0";
                selectionArgs = null;
            }

            Cursor cursor = getContentResolver().query(MusicContract.URI, null, selection, selectionArgs, null);
            if (cursor == null || isCancelled()) {
                return cursor;
            }

            while (cursor.moveToNext()) {
                if (isCancelled()) {
                    return cursor;
                }

                long musicCode = cursor.getLong(cursor.getColumnIndex(MusicContract._CODE));
                if (musicCode == mMusic.getCode() || cursor.isLast()) {
                    moveToNext(cursor);
                    break;
                }
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = cursor;
        }
    }
}
