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
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xhbb.qinzl.pleasantnote.R;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    public static final String ACTION_INIT_MUSIC = Contracts.AUTHORITY + ".ACTION_INIT_MUSIC";
    public static final String ACTION_PLAY_NEW_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_NEW_MUSIC";
    public static final String ACTION_START_FOREGROUND = Contracts.AUTHORITY + ".ACTION_START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = Contracts.AUTHORITY + ".ACTION_STOP_FOREGROUND";

    public static final int LIMIT_VALUE_OF_HISTORY_MUSIC = 50;

    private static final String EXTRA_MUSIC = Contracts.AUTHORITY + ".EXTRA_MUSIC";
    private static final String EXTRA_DATA_POSITION = Contracts.AUTHORITY + ".EXTRA_DATA_POSITION";

    private MediaPlayer mMediaPlayer;
    private Music mMusic;
    private Notification mNotification;
    private Cursor mCursor;
    private QueryMoreMusicTask mQueryMoreMusicTask;
    private int mDataPosition;
    private InitMusicTask mInitMusicTask;
    private int[] mPlaySpinnerValues;
    private List<Integer> mPreviousMusicPositions;
    private MusicBinder mMusicBinder;
    private Random mRandom;
    private OnMusicServiceListener mListener;

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

        mMusicBinder = new MusicBinder();
        mPreviousMusicPositions = new ArrayList<>();
        mPlaySpinnerValues = getResources().getIntArray(R.array.play_spinner_values);
        mNotification = NotificationUtils.getForegroundNotification(getApplicationContext());
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
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
            case ACTION_PLAY_NEW_MUSIC:
                playNewMusic(intent);
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

    public void play() {
        mMediaPlayer.start();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public int getPlayedMillis() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void seekTo(int millis) {
        mMediaPlayer.seekTo(millis);
    }

    public Music getMusic() {
        return mMusic;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
        return super.onUnbind(intent);
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
        resetAndPlay();
        queryMoreMusic();
    }

    private void queryMoreMusic() {
        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(true);
        }
        mQueryMoreMusicTask = new QueryMoreMusicTask();
        mQueryMoreMusicTask.execute();
    }

    private void resetAndPlay() {
        if (mMusic == null) {
            return;
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mMusic.getPlayUrl());
            mMediaPlayer.prepareAsync();

            if (mListener != null) {
                mListener.onMediaPlayerPreparing();
            }
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

        if (mListener != null) {
            mListener.onMediaPlayerStarted();
        }
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
            mediaPlayer.start();
        } else {
            playNext();
        }
    }

    public void playNext() {
        if (noCursorData()) {
            resetAndPlay();
            return;
        }

        addPreviousMusicPosition();

        int spinnerSelection = PrefrencesUtils.getPlaySpinnerSelection(getApplicationContext());
        if (mPlaySpinnerValues[spinnerSelection] == getResources().getInteger(R.integer.play_spinner_list_loop)) {
            if (++mDataPosition >= mCursor.getCount()) {
                mDataPosition = 0;
            }
        } else if (mPlaySpinnerValues[spinnerSelection] == getResources().getInteger(R.integer.play_spinner_random_play)) {
            if (mRandom == null) {
                mRandom = new Random();
            }
            mDataPosition = mRandom.nextInt(mCursor.getCount());
        }

        moveCursorAndChangeMusic();
    }

    private void moveCursorAndChangeMusic() {
        mCursor.moveToPosition(mDataPosition);
        mMusic = new Music(mCursor);
        resetAndPlay();
    }

    private void addPreviousMusicPosition() {
        int previousPositionsSize = mPreviousMusicPositions.size();
        if (previousPositionsSize > 0 &&
                mPreviousMusicPositions.get(previousPositionsSize - 1) != mDataPosition) {
            mPreviousMusicPositions.add(mDataPosition);
        } else if (mPreviousMusicPositions.isEmpty()) {
            mPreviousMusicPositions.add(mDataPosition);
        }

        if (previousPositionsSize > 10) {
            mPreviousMusicPositions.remove(0);
        }
    }

    public void playPrevious() {
        if (mPreviousMusicPositions.isEmpty()) {
            resetAndPlay();
            return;
        }

        int i = mPreviousMusicPositions.size() - 1;
        mDataPosition = mPreviousMusicPositions.get(i);
        moveCursorAndChangeMusic();
        mPreviousMusicPositions.remove(i);
    }

    private boolean noCursorData() {
        return mCursor == null || mCursor.getCount() == 0;
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

    public class MusicBinder extends Binder {

        public MusicService getService(@Nullable OnMusicServiceListener listener) {
            mListener = listener;
            return MusicService.this;
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
            resetAndPlay();
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

    public interface OnMusicServiceListener {

        void onMediaPlayerStarted();
        void onMediaPlayerPreparing();
    }
}
