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
    public static final String ACTION_START_FOREGROUND = Contracts.AUTHORITY + ".ACTION_START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = Contracts.AUTHORITY + ".ACTION_STOP_FOREGROUND";
    public static final String ACTION_SEND_MUSIC_TO_PLAY_WIDGET = Contracts.AUTHORITY + ".ACTION_SEND_MUSIC_TO_PLAY_WIDGET";
    public static final String ACTION_PLAY_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_MUSIC";
    public static final String ACTION_PAUSE_MUSIC = Contracts.AUTHORITY + ".ACTION_PAUSE_MUSIC";
    public static final String ACTION_PLAY_NEXT_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_NEXT_MUSIC";

    public static final int LIMIT_VALUE_OF_HISTORY_MUSIC = 50;

    private static final String ACTION_PLAY_NEW_MUSIC = Contracts.AUTHORITY + ".ACTION_PLAY_NEW_MUSIC";
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
    private boolean mMusicPlaying;

    public static Intent newIntent(Context context) {
        return new Intent(context, MusicService.class);
    }

    public static Intent newIntent(Context context, String action) {
        return newIntent(context).setAction(action);
    }

    public static Intent newPlayNewMusicIntent(Context context, Music music) {
        return newIntent(context, ACTION_PLAY_NEW_MUSIC).putExtra(EXTRA_MUSIC, music);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMusics = new ArrayList<>();
        mMusicBinder = new MusicBinder(this);
        mHistoryMusicPositions = new ArrayList<>();
        mPlaySpinnerValues = getResources().getIntArray(R.array.play_spinner_values);
        mForegroundNotification = NotificationUtils.getForegroundNotification(this);
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    private void setListener(OnMusicServiceListener listener) {
        mListener = listener;
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
        if (intent.getAction() == null) {
            return;
        }

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
            case ACTION_SEND_MUSIC_TO_PLAY_WIDGET:
                sendMusicToPlayWidget();
                break;
            case ACTION_PLAY_MUSIC:
                play();
                break;
            case ACTION_PAUSE_MUSIC:
                pause();
                break;
            case ACTION_PLAY_NEXT_MUSIC:
                playNext();
                break;
            default:
        }
    }

    private void sendMusicToPlayWidget() {
        Music music = getCurrentMusic();
        if (mMusicPlaying) {
            sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_PLAYED, music);
        } else {
            sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_STOPPED, music);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
        return super.onUnbind(intent);
    }

    public void play() {
        mMediaPlayer.start();
        mMusicPlaying = mMediaPlayer.isPlaying();

        if (mMusicPlaying) {
            sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_PLAYED, getCurrentMusic());
        }
    }

    private void sendPrivateMusicBroadcast(String action, Music music) {
        Intent intent = new Intent(action);
        intent.putExtra(Contracts.EXTRA_MUSIC, music);
        sendBroadcast(intent, getString(R.string.permission_private));
    }

    public boolean isMusicPlaying() {
        return mMusicPlaying;
    }

    public void pause() {
        mMediaPlayer.pause();
        mMusicPlaying = false;
        sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_STOPPED, getCurrentMusic());
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

        final MusicService musicService = this;
        final ContentResolver contentResolver = getApplicationContext().getContentResolver();

        mInitMusicTask = new AsyncTask<Void, Void, List<Music>>() {
            @Override
            protected List<Music> doInBackground(Void... voids) {
                String selection = MusicContract._TYPE + "=" + MusicType.HISTORY;
                String sortOrder = MusicContract._ID + " DESC LIMIT " + LIMIT_VALUE_OF_HISTORY_MUSIC;

                Cursor cursor = contentResolver.query(MusicContract.URI, null, selection, null, sortOrder);

                return getMusicsAndCloseCursor(cursor);
            }

            @Override
            protected void onPostExecute(List<Music> musics) {
                super.onPostExecute(musics);
                musicService.setMusics(musics);
                resetAndPlay();
            }
        }.execute();
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

    private void executeQueryMoreMusicTask(final Music currentMusic) {
        if (mQueryMoreMusicTask != null) {
            mQueryMoreMusicTask.cancel(false);
        }

        final ContentResolver contentResolver = getApplicationContext().getContentResolver();
        final MusicService musicService = this;

        mQueryMoreMusicTask = new AsyncTask<Void, Void, List<Music>>() {
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

                Cursor cursor = contentResolver.query(MusicContract.URI, null, selection, null, sortOrder);

                return getMusicsAndCloseCursor(cursor, currentMusic);
            }

            @Override
            protected void onPostExecute(List<Music> musics) {
                super.onPostExecute(musics);
                if (musics.contains(currentMusic)) {
                    int currentMusicPosition = musics.indexOf(currentMusic);

                    musicService.setCurrentMusicPosition(currentMusicPosition);
                    musicService.setMusics(musics);
                }
            }
        }.execute();
    }

    private void resetAndPlay() {
        if (mMusics.isEmpty()) {
            return;
        }

        try {
            Music music = mMusics.get(mCurrentMusicPosition);
            String playUrl = music.getPlayUrl();

            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.prepareAsync();
            saveCurrentMusicAsync();
            mMusicPlaying = true;

            if (mListener != null) {
                mListener.onMediaPlayerPreparing();
            }

            sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_PLAYED, music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentMusicAsync() {
        final Music currentMusic = mMusics.get(mCurrentMusicPosition);
        final ContentResolver contentResolver = getApplicationContext().getContentResolver();

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
        sendPrivateMusicBroadcast(Contracts.ACTION_MUSIC_STOPPED, null);
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
            mListener.onMediaPlayerPrepared();
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

        int spinnerSelection = PrefrencesUtils.getSwitchModeSpinnerSelection(this);
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

    private void setMusics(List<Music> musics) {
        mMusics = musics;
    }

    private void setCurrentMusicPosition(int currentMusicPosition) {
        mCurrentMusicPosition = currentMusicPosition;
    }

    public class MusicBinder extends Binder {

        private MusicService mMusicService;

        MusicBinder(MusicService musicService) {
            mMusicService = musicService;
        }

        public MusicService getService(@Nullable OnMusicServiceListener listener) {
            mMusicService.setListener(listener);
            return mMusicService;
        }
    }

    public interface OnMusicServiceListener {

        void onMediaPlayerPrepared();
        void onMediaPlayerPreparing();
    }
}
