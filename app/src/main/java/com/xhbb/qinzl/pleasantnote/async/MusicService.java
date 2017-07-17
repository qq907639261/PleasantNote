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

    public static final String EXTRA_MUSIC = Contracts.AUTHORITY + ".EXTRA_MUSIC";

    public static final String ACTION_PLAY_NEW_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_NEW_MUSIC";
    public static final String ACTION_PLAY_OR_PAUSE = Contracts.AUTHORITY + ".ACTION_PLAY_OR_PAUSE";
    public static final String ACTION_PLAY_NEXT = Contracts.AUTHORITY + ".ACTION_PLAY_NEXT";
    public static final String ACTION_SEND_MUSIC_DATA = Contracts.AUTHORITY + ".ACTION_PLAYING_MUSIC_UPDATED";

    public static final int MUSIC_ID_BY_INSERT = 0;

    private MediaPlayer mMediaPlayer;
    private Notification mNotification;
    private Music mMusic;
    private Cursor mCursor;
    private ResetCursorTask mResetCursorTask;
    private InitMusicTask mInitMusicTask;

    public static Intent newIntent(Context context, String action, Music music) {
        return newIntent(context, action)
                .putExtra(EXTRA_MUSIC, music);
    }

    public static Intent newIntent(Context context, String action) {
        return new Intent(context, MusicService.class)
                .setAction(action);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();

        mNotification = NotificationUtils.getForegroundNotification(context);
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_PLAY_NEW_MUSIC:
                mMusic = intent.getParcelableExtra(EXTRA_MUSIC);
                playNewMusic();
                resetCursor();
                break;
            case ACTION_PLAY_OR_PAUSE:
                playOrPause();
                break;
            case ACTION_PLAY_NEXT:
                playNext();
                break;
            case ACTION_SEND_MUSIC_DATA:
                sendMusicData();
                break;
            default:
        }
    }

    private void resetCursor() {
        if (mResetCursorTask != null) {
            mResetCursorTask.cancel(true);
        }
        mResetCursorTask = new ResetCursorTask();
        mResetCursorTask.execute();
    }

    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            stopForeground(true);
        } else {
            mMediaPlayer.start();
            startForeground(1, mNotification);
        }
    }

    private void playNewMusic() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusic.getPlayUrl());
            mMediaPlayer.prepareAsync();
            startForeground(1, mNotification);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMusic != null) {
            saveMusicData();
        }

        mMediaPlayer.release();
        mMediaPlayer = null;

        if (mResetCursorTask != null) {
            mResetCursorTask.cancel(true);
        }

        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void saveMusicData() {
        final ContentValues musicValues = mMusic.getMusicValues();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String where = MusicContract._ID + "=" + MUSIC_ID_BY_INSERT;

                ContentResolver contentResolver = getContentResolver();
                int updatedRows = contentResolver.update(MusicContract.URI, musicValues, where, null);

                if (updatedRows <= 0) {
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
            return;
        }

        mMusic = new Music(mCursor);
        playNewMusic();
        sendMusicData();

        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }
    }

    private void sendMusicData() {
        if (mMusic == null) {
            initMusic();
            return;
        }

        Intent intent = new Intent(Contracts.ACTION_PLAYING_MUSIC_UPDATED);
        intent.putExtra(EXTRA_MUSIC, mMusic);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void initMusic() {
        if (mInitMusicTask != null) {
            mInitMusicTask.cancel(false);
        }
        mInitMusicTask = new InitMusicTask();
        mInitMusicTask.execute();
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
            if (cursor != null && cursor.moveToFirst()) {
                mMusic = new Music(cursor);
                sendMusicData();
                playNewMusic();
                resetCursor();
                cursor.close();
            }
        }
    }

    private class ResetCursorTask extends AsyncTask<Void, Void, Cursor> {

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
                return null;
            }

            while (cursor.moveToNext()) {
                if (isCancelled()) {
                    return null;
                }

                long musicCode = cursor.getLong(cursor.getColumnIndex(MusicContract._CODE));
                if (musicCode == mMusic.getCode() || cursor.isLast()) {
                    if (!cursor.moveToNext()) {
                        cursor.moveToFirst();
                    }
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
