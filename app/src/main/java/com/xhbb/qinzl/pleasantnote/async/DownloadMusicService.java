package com.xhbb.qinzl.pleasantnote.async;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;

import com.xhbb.qinzl.pleasantnote.common.Enums.DownloadState;
import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.model.Download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadMusicService extends Service {

    private static final int THREAD_COUNT_IN_POOL = 3;

    private SparseIntArray mDownloadStates;
    private OnDownloadMusicServiceListener mListener;
    private DownloadMusicBinder mBinder;
    private ExecutorService mDownloadMusicExecutorService;
    private Handler mHandler;
    private boolean mServiceStoped;
    private Random mRandom;
    private AsyncTask<Void, Void, List<Download>> mQueryDownloadDataTask;

    public static Intent newIntent(Context context) {
        return new Intent(context, DownloadMusicService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new DownloadMusicBinder();
        mDownloadMusicExecutorService = Executors.newFixedThreadPool(THREAD_COUNT_IN_POOL);
        mDownloadStates = new SparseIntArray();
        mHandler = new Handler();
        mRandom = new Random();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mListener = null;
        return super.onUnbind(intent);
    }

    public void updateDownloadStateAsync(final int musicId, final int downloadState) {
        final ContentResolver contentResolver = getContentResolver();

        new Thread(new Runnable() {
            @Override
            public void run() {
                updateDownloadState(downloadState, musicId, contentResolver);
            }
        }).start();
    }

    private void updateDownloadState(int downloadState, int musicId, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract._STATE, downloadState);

        updateDownloadByMusicId(contentResolver, values, musicId);
    }

    private void updateDownloadByMusicId(ContentResolver contentResolver, ContentValues values,
                                         int musicId) {
        String where = DownloadContract._MUSIC_ID + "=" + musicId;
        updateDownload(contentResolver, values, where);
    }

    private void updateDownload(ContentResolver contentResolver, ContentValues values, String where) {
        contentResolver.update(DownloadContract.URI, values, where, null);
        contentResolver.notifyChange(DownloadContract.URI, null);
    }

    public void startDownload() {
        if (mDownloadStates.size() > 5) {
            return;
        }

        if (mServiceStoped) {
            startService(newIntent(getApplicationContext()));
            mServiceStoped = false;
        }

        executeQueryDownloadDataTask();
    }

    private void executeQueryDownloadDataTask() {
        if (mQueryDownloadDataTask != null) {
            mQueryDownloadDataTask.cancel(false);
        }
        mQueryDownloadDataTask = getQueryDownloadDataTask().execute();
    }

    @NonNull
    private AsyncTask<Void, Void, List<Download>> getQueryDownloadDataTask() {
        return new AsyncTask<Void, Void, List<Download>>() {
            private ContentResolver iContentResolver;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                iContentResolver = getContentResolver();
            }

            @Override
            protected List<Download> doInBackground(Void... voids) {
                String selection = DownloadContract._STATE + "=" + DownloadState.WAITING;
                String sortOrder = DownloadContract._ID + " LIMIT 10";

                Cursor cursor = iContentResolver.query(DownloadContract.URI, null, selection, null, sortOrder);
                List<Download> downloads = new ArrayList<>();

                if (cursor != null) {
                    boolean moveSucceeded = cursor.moveToFirst();

                    while (moveSucceeded) {
                        downloads.add(new Download(cursor));
                        moveSucceeded = cursor.moveToNext();
                    }

                    cursor.close();
                }

                return downloads;
            }

            @Override
            protected void onPostExecute(List<Download> downloads) {
                super.onPostExecute(downloads);
                for (Download download : downloads) {
                    int musicId = download.getMusicId();

                    mDownloadStates.put(musicId, DownloadState.DOWNLOADING);
                    updateDownloadStateAsync(musicId, DownloadState.DOWNLOADING);

                    executeDownloadMusicExecutorService(download);
                }
            }
        };
    }

    private void executeDownloadMusicExecutorService(final Download download) {
        final ContentResolver contentResolver = getContentResolver();

        mDownloadMusicExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
//                if (dir.getFreeSpace())
//                ContextCompat.getExternalFilesDirs()

                int musicId = download.getMusicId();
                String downloadUrl = download.getUrl();

                File musicFile = new File(dir, String.valueOf(musicId));

                if (!musicFile.exists()) {
                    boolean createFileFailed = false;

                    try {
                        if (!musicFile.createNewFile()) {
                            createFileFailed = true;
                            return;
                        }

                        int random = mRandom.nextInt(40) + 1;
                        downloadUrl = "http://10.0.2.2/music/" + random + ".mp3";

                        ContentValues values = new ContentValues();
                        values.put(DownloadContract._URL, downloadUrl);

                        updateDownloadByMusicId(contentResolver, values, musicId);
                    } catch (IOException e) {
                        e.printStackTrace();
                        createFileFailed = true;
                        return;
                    } finally {
                        if (createFileFailed) {
                            //noinspection ResultOfMethodCallIgnored
                            musicFile.delete();
                            finishDownload(musicId, DownloadState.FAILED);
                        }
                    }
                }

                // TODO: 2017/8/4 下载，并随时判断是否需要暂停或取消
//                downloadUrl

                int downloadState = mDownloadStates.get(musicId);

                if (downloadState == DownloadState.PAUSE || downloadState == DownloadState.CANCEL) {
                    if (downloadState == DownloadState.CANCEL) {
                        //noinspection ResultOfMethodCallIgnored
                        musicFile.delete();
                        deleteDownloadByMusicId(musicId, contentResolver);
                    }

                    finishDownload(musicId, downloadState);
                    return;
                }

                finishDownload(musicId, DownloadState.DOWNLOADED);
            }

            private void finishDownload(final int musicId, final int downloadState) {
                updateDownloadState(downloadState, musicId, contentResolver);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadStates.delete(musicId);

                        if (mDownloadStates.size() == 0) {
                            stopSelf();
                            mServiceStoped = true;
                        }

                        if (mListener != null) {
                            switch (downloadState) {
                                case DownloadState.FAILED:
                                    mListener.onDownloadFailed();
                                    break;
                                case DownloadState.CANCEL:
                                case DownloadState.PAUSE:
                                    mListener.onDownloadStopped();
                                    break;
                                case DownloadState.DOWNLOADED:
                                    mListener.onDownloadSucceeded();
                                    break;
                                default:
                            }
                        }
                    }
                });
            }
        });
    }

    private void deleteDownloadByMusicId(int musicId, ContentResolver contentResolver) {
        String where = DownloadContract._MUSIC_ID + "=" + musicId;

        contentResolver.delete(DownloadContract.URI, where, null);
        contentResolver.notifyChange(DownloadContract.URI, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        updateDownloadingAndWaitingToPauseAsync();

        if (mQueryDownloadDataTask != null) {
            mQueryDownloadDataTask.cancel(false);
        }

        mDownloadMusicExecutorService.shutdown();
    }

    private void updateDownloadingAndWaitingToPauseAsync() {
        final ContentResolver contentResolver = getContentResolver();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(DownloadContract._STATE, DownloadState.PAUSE);

                String where = DownloadContract._STATE + " IN (" + DownloadState.DOWNLOADING +
                        "," + DownloadState.WAITING + ")";

                updateDownload(contentResolver, values, where);
            }
        }).start();
    }

    public class DownloadMusicBinder extends Binder {

        public DownloadMusicService getService(@Nullable OnDownloadMusicServiceListener listener) {
            mListener = listener;
            return DownloadMusicService.this;
        }
    }

    public interface OnDownloadMusicServiceListener {

        void onDownloadFailed();
        void onDownloadStopped();
        void onDownloadSucceeded();
    }
}
