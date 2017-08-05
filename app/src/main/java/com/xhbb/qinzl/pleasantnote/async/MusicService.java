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
import android.support.annotation.NonNull;
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

    private MediaPlayer mMediaPlayer;
    private Notification mForegroundNotification;
    private List<Music> mMusics;
    private int mCurrentMusicPosition;
    private AsyncTask<Void, Void, List<Music>> mQueryMoreMusicTask;
    private AsyncTask<Void, Void, List<Music>> mInitMusicTask;
    private int[] mPlaySpinnerValues;
    private List<Integer> mHistoryMusicPositions;
    private MusicBinder mMusicBinder;
    private Random mRandom;
    private OnMusicServiceListener mListener;
    private boolean mPlaying;

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

        mMusics = new ArrayList<>();
        mMusicBinder = new MusicBinder();
        mHistoryMusicPositions = new ArrayList<>();
        mPlaySpinnerValues = getResources().getIntArray(R.array.play_spinner_values);
        mForegroundNotification = NotificationUtils.getForegroundNotification(getApplicationContext());
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
                executeInitMusicTask();
                break;
            case ACTION_PLAY_NEW_MUSIC:
                playNewMusic(intent);
                break;
            case ACTION_START_FOREGROUND:
                startForeground(1, mForegroundNotification);
                break;
            case ACTION_STOP_FOREGROUND:
                stopForeground(true);
                break;
            default:
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
        return super.onUnbind(intent);
    }

    public void play() {
        mMediaPlayer.start();
        mPlaying = mMediaPlayer.isPlaying();
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    public void pause() {
        mMediaPlayer.pause();
        mPlaying = false;
    }

    public int getPlayedMillis() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void seekTo(int millis) {
        mMediaPlayer.seekTo(millis);
    }

    public Music getCurrentMusic() {
        return mMusics.isEmpty() ? null : mMusics.get(mCurrentMusicPosition);
    }

    public boolean hasMusic() {
        return mMusics.size() > 0;
    }

    private void executeInitMusicTask() {
        if (mInitMusicTask != null) {
            mInitMusicTask.cancel(false);
        }
        mInitMusicTask = getInitMusicTask().execute();
    }

    private AsyncTask<Void, Void, List<Music>> getInitMusicTask() {
        return new AsyncTask<Void, Void, List<Music>>() {
            private ContentResolver iContentResolver;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                iContentResolver = getContentResolver();
            }

            @Override
            protected List<Music> doInBackground(Void... voids) {
                String selection = MusicContract._TYPE + "=" + MusicType.HISTORY;
                String sortOrder = MusicContract._ID + " DESC LIMIT " + LIMIT_VALUE_OF_HISTORY_MUSIC;

                Cursor cursor = iContentResolver.query(MusicContract.URI, null, selection, null, sortOrder);

                return getMusicsAndCloseCursor(cursor);
            }

            @Override
            protected void onPostExecute(List<Music> musics) {
                super.onPostExecute(musics);
                mMusics = musics;
                resetAndPlay();
            }
        };
    }

    private List<Music> getMusicsAndCloseCursor(Cursor cursor, Music currentMusic) {
        List<Music> musics = new ArrayList<>();

        if (cursor != null) {
            boolean moveSucceeded = cursor.moveToFirst();
            while (moveSucceeded) {
                Music music = new Music(cursor);

                if (music.getCode() == currentMusic.getCode()) {
                    musics.add(currentMusic);
                } else {
                    musics.add(music);
                }

                moveSucceeded = cursor.moveToNext();
            }

            cursor.close();
        }

        return musics;
    }

    private List<Music> getMusicsAndCloseCursor(Cursor cursor) {
        List<Music> musics = new ArrayList<>();

        if (cursor != null) {
            boolean moveSucceeded = cursor.moveToFirst();

            while (moveSucceeded) {
                musics.add(new Music(cursor));
                moveSucceeded = cursor.moveToNext();
            }

            cursor.close();
        }

        return musics;
    }

    private void playNewMusic(Intent intent) {
        Music newMusic = intent.getParcelableExtra(EXTRA_MUSIC);

        mHistoryMusicPositions.clear();
        mCurrentMusicPosition = 0;
        mMusics.clear();
        mMusics.add(newMusic);

        resetAndPlay();
        executeQueryMoreMusicTask(newMusic);
    }

    private void executeQueryMoreMusicTask(Music currentMusic) {
        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(false);
        }
        mQueryMoreMusicTask = getQueryMoreMusicTask(currentMusic).execute();
    }

    @NonNull
    private AsyncTask<Void, Void, List<Music>> getQueryMoreMusicTask(final Music currentMusic) {
        return new AsyncTask<Void, Void, List<Music>>() {
            private ContentResolver iContentResolver;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                iContentResolver = getContentResolver();
            }

            @Override
            protected List<Music> doInBackground(Void... voids) {
                int musicType = currentMusic.getMusicType();

                String selection = MusicContract._TYPE + "=" + musicType;
                String sortOrder = null;

                if (musicType == MusicType.RANKING) {
                    selection += " AND " + MusicContract._RANKING_CODE + "=" + currentMusic.getRankingCode();
                } else if (musicType == MusicType.HISTORY) {
                    sortOrder = MusicContract._ID + " DESC LIMIT " + LIMIT_VALUE_OF_HISTORY_MUSIC;
                }

                Cursor cursor = iContentResolver.query(MusicContract.URI, null, selection, null, sortOrder);

                return getMusicsAndCloseCursor(cursor, currentMusic);
            }

            @Override
            protected void onPostExecute(List<Music> musics) {
                super.onPostExecute(musics);
                if (musics.contains(currentMusic)) {
                    mCurrentMusicPosition = musics.indexOf(currentMusic);
                    mMusics = musics;
                }
            }
        };
    }

    private void resetAndPlay() {
        if (mMusics.isEmpty()) {
            return;
        }

        try {
            String playUrl = mMusics.get(mCurrentMusicPosition).getPlayUrl();

            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.prepareAsync();
            saveCurrentMusicAsync();
            mPlaying = true;

            if (mListener != null) {
                mListener.onPreparing();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentMusicAsync() {
        final Music currentMusic = mMusics.get(mCurrentMusicPosition);
        final ContentResolver contentResolver = getContentResolver();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int musicCode = currentMusic.getCode();
                String where = MusicContract._CODE + "=" + musicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.HISTORY;

                ContentValues musicValues = currentMusic.getMusicValues();
                musicValues.put(MusicContract._TYPE, MusicType.HISTORY);

                contentResolver.delete(MusicContract.URI, where, null);
                contentResolver.insert(MusicContract.URI, musicValues);
                contentResolver.notifyChange(MusicContract.URI, null);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mInitMusicTask != null) {
            mInitMusicTask.cancel(false);
        }

        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(false);
        }

        CleanUpHistoryMusicJob.cancelJob();
        cleanUpHistoryMusicAsync();

        mMediaPlayer.release();
    }

    private void cleanUpHistoryMusicAsync() {
        final Context context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainTasks.cleanUpHistoryMusic(context);
            }
        }).start();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mListener != null) {
            mListener.onPrepared();
        }
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext();
    }

    public void playNext() {
        if (mMusics.isEmpty()) {
            return;
        }

        addHistoryMusicPosition(mHistoryMusicPositions, mCurrentMusicPosition);

        int spinnerSelection = PrefrencesUtils.getPlaySpinnerSelection(getApplicationContext());
        if (mPlaySpinnerValues[spinnerSelection] == getResources().getInteger(R.integer.play_spinner_list_loop)) {
            if (++mCurrentMusicPosition >= mMusics.size()) {
                mCurrentMusicPosition = 0;
            }
        } else if (mPlaySpinnerValues[spinnerSelection] == getResources().getInteger(R.integer.play_spinner_random_play)) {
            if (mRandom == null) {
                mRandom = new Random();
            }
            mCurrentMusicPosition = mRandom.nextInt(mMusics.size());
        }

        resetAndPlay();
    }

    private void addHistoryMusicPosition(List<Integer> historyMusicPositions,
                                         int currentMusicPosition) {
        int historyMusicPositionsSize = historyMusicPositions.size();

        if ((historyMusicPositionsSize == 0) ||
                (historyMusicPositionsSize > 0 &&
                        historyMusicPositions.get(historyMusicPositionsSize - 1)
                                != currentMusicPosition)) {
            historyMusicPositions.add(currentMusicPosition);
        }

        if (historyMusicPositionsSize > 10) {
            historyMusicPositions.remove(0);
        }
    }

    public void playPrevious() {
        if (mHistoryMusicPositions.isEmpty()) {
            resetAndPlay();
            return;
        }

        int last = mHistoryMusicPositions.size() - 1;

        mCurrentMusicPosition = mHistoryMusicPositions.get(last);
        mHistoryMusicPositions.remove(last);

        resetAndPlay();
    }

    public class MusicBinder extends Binder {

        public MusicService getService(@Nullable OnMusicServiceListener listener) {
            mListener = listener;
            return MusicService.this;
        }
    }

    public interface OnMusicServiceListener {

        void onPrepared();
        void onPreparing();
    }
}
