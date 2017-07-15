package com.xhbb.qinzl.pleasantnote.async;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.xhbb.qinzl.pleasantnote.common.MainSingleton;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.io.IOException;

public class MusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    public static final String EXTRA_MUSIC = Contracts.AUTHORITY + ".EXTRA_MUSIC";

    private MediaPlayer mMediaPlayer;
    private Notification mNotification;
    private Cursor mCursor;
    private InitCursorTask mInitCursorTask;

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
        mMediaPlayer = MainSingleton.getInstance(context).getMediaPlayer();

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
            case Contracts.ACTION_RESET:
                Music music = intent.getParcelableExtra(EXTRA_MUSIC);
                reset(music.getPlayUrl());
                initCursor(music);
                break;
            case Contracts.ACTION_PLAY_PAUSE:
                playOrPause();
                break;
            case Contracts.ACTION_NEXT:
                playNext();
                break;
            default:
        }
    }

    private void initCursor(Music music) {
        if (mInitCursorTask != null) {
            mInitCursorTask.cancel(true);
        }

        mInitCursorTask = new InitCursorTask();
        mInitCursorTask.execute(music);
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

    private void reset(String playUrl) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.prepareAsync();

            startForeground(1, mNotification);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
        mInitCursorTask.cancel(true);
        if (mCursor != null) {
            mCursor.close();
        }
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

        Music music = new Music(mCursor);
        reset(music.getPlayUrl());

        Intent intent = new Intent(Contracts.ACTION_NEXT_PLAYED);
        intent.putExtra(EXTRA_MUSIC, music);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }
    }

    private class InitCursorTask extends AsyncTask<Music, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Music... musics) {
            Music music = musics[0];
            String query = music.getQuery();
            String selection;
            String[] selectionArgs;

            if (query != null) {
                selection = MusicContract._QUERY + "=?";
                selectionArgs = new String[]{query};
            } else {
                selection = MusicContract._RANKING_CODE + "=?";
                selectionArgs = new String[]{String.valueOf(music.getRankingCode())};
            }

            Cursor cursor = getContentResolver().query(MusicContract.URI, null, selection, selectionArgs, null);
            if (cursor == null || isCancelled()) {
                return null;
            }

            while (cursor.moveToNext()) {
                if (isCancelled()) {
                    return null;
                }

                long musicId = cursor.getLong(cursor.getColumnIndex(MusicContract._ID));
                if (musicId == music.getId() || cursor.isLast()) {
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
