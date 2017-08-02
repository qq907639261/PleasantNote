package com.xhbb.qinzl.pleasantnote.async;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.xhbb.qinzl.pleasantnote.common.Enums.DownloadState;
import com.xhbb.qinzl.pleasantnote.data.Contracts;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadMusicService<T> extends Service {

    private static final String EXTRA_MUSIC = Contracts.AUTHORITY + ".EXTRA_MUSIC";
    private static final String EXTRA_DOWNLODA_STATE = Contracts.AUTHORITY + ".EXTRA_DOWNLODA_STATE";
    private static final int THREAD_COUNT = 3;

    private HashMap<T, Integer> mDownloadStates;
    private OnDownloadMusicServiceListener mListener;
    private DownloadMusicBinder mBinder;
    private ExecutorService mExecutorService;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new DownloadMusicBinder();
        mExecutorService = Executors.newFixedThreadPool(THREAD_COUNT);
        mDownloadStates = new HashMap<>();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        @SuppressWarnings("unchecked")
        T music = intent.getParcelableExtra(EXTRA_MUSIC);
        int downloadState = intent.getIntExtra(EXTRA_DOWNLODA_STATE, 0);

        downloadMusic(music, downloadState);

        return super.onStartCommand(intent, flags, startId);
    }

    public void downloadMusic(final T music, final int downloadState) {
        mDownloadStates.put(music, downloadState);

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                int downloadState = mDownloadStates.get(music);

                if (downloadState == DownloadState.STOP) {
                    mDownloadStates.remove(music);
                    return;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }

    public class DownloadMusicBinder extends Binder {

        public DownloadMusicService getService(@Nullable OnDownloadMusicServiceListener listener) {
            mListener = listener;
            return DownloadMusicService.this;
        }
    }

    public interface OnDownloadMusicServiceListener {

    }
}
