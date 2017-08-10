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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
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

        mBinder = new DownloadMusicBinder(this);
        mDownloadMusicExecutorService = Executors.newFixedThreadPool(THREAD_COUNT_IN_POOL);
        mDownloadStates = new SparseIntArray();
        mHandler = new Handler();
        mRandom = new Random();
    }

    private void setListener(OnDownloadMusicServiceListener listener) {
        mListener = listener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStoped = false;
        startDownload();
        return super.onStartCommand(intent, flags, startId);
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

    public void putDownloadStates(int musicCode, int downloadState) {
        mDownloadStates.put(musicCode, downloadState);
    }

    private void updateDownloadStateAsync(final int musicCode, final int downloadState) {
        final ContentResolver contentResolver = getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateDownloadState(downloadState, musicCode, contentResolver);
            }
        }).start();
    }

    private void updateDownloadState(int downloadState, int musicCode, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put(DownloadContract._STATE, downloadState);

        updateDownloadByMusicCode(contentResolver, values, musicCode);
    }

    private void updateDownloadByMusicCode(ContentResolver contentResolver, ContentValues values,
                                           int musicCode) {
        String where = DownloadContract._MUSIC_CODE + "=" + musicCode;
        updateDownload(contentResolver, values, where);
    }

    private void updateDownload(ContentResolver contentResolver, ContentValues values, String where) {
        contentResolver.update(DownloadContract.URI, values, where, null);
        contentResolver.notifyChange(DownloadContract.URI, null);
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
                    int musicCode = download.getMusicCode();

                    putDownloadStates(musicCode, DownloadState.PREPARING);
                    updateDownloadStateAsync(musicCode, DownloadState.PREPARING);

                    executeDownloadMusicExecutorService(download);
                }
            }
        };
    }

    private void executeDownloadMusicExecutorService(final Download download) {
        final ContentResolver contentResolver = getContentResolver();
        final int musicCode = download.getMusicCode();

        mDownloadMusicExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (mDownloadMusicExecutorService.isShutdown()) {
                    return;
                }

                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                String downloadUrl = download.getUrl();
                File musicFile = new File(dir, musicCode + ".mp3");

                updateDownloadState(DownloadState.DOWNLOADING, musicCode, contentResolver);

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

                        updateDownloadByMusicCode(contentResolver, values, musicCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                        createFileFailed = true;
                        return;
                    } finally {
                        if (createFileFailed) {
                            //noinspection ResultOfMethodCallIgnored
                            musicFile.delete();
                            finishDownload(musicCode, DownloadState.FAILED);
                        }
                    }
                }

                HttpURLConnection connection = null;
                RandomAccessFile randomAccessFile = null;
                InputStream inputStream = null;

                try {
                    URL url = new URL(downloadUrl);

                    connection = (HttpURLConnection) url.openConnection();
                    randomAccessFile = new RandomAccessFile(musicFile, "rw");
                    long downloadedLength = musicFile.length();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("RANGE", "bytes=" + downloadedLength + "-");
                    randomAccessFile.seek(downloadedLength);

                    inputStream = connection.getInputStream();
                    int contentLength = connection.getContentLength();
                    byte[] b = new byte[1024];

                    int len;
                    while ((len = inputStream.read(b)) != -1) {
                        int downloadState = mDownloadStates.get(musicCode);

                        if (downloadState == DownloadState.PAUSE || downloadState == DownloadState.CANCEL) {
                            if (downloadState == DownloadState.CANCEL) {
                                //noinspection ResultOfMethodCallIgnored
                                musicFile.delete();
                                deleteDownloadByMusicCode(musicCode, contentResolver);
                            }

                            finishDownload(musicCode, downloadState);
                            return;
                        }

                        randomAccessFile.write(b, 0, len);

                        downloadedLength += len;
                        final int progress = (int) (((double) downloadedLength / contentLength) * 100);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onProgressUpdate(musicCode, progress);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //noinspection ResultOfMethodCallIgnored
                    musicFile.delete();
                    finishDownload(musicCode, DownloadState.FAILED);
                    return;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                finishDownload(musicCode, DownloadState.DOWNLOADED);
            }

            private void finishDownload(final int musicCode, final int downloadState) {
                updateDownloadState(downloadState, musicCode, contentResolver);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadStates.delete(musicCode);

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

    private void deleteDownloadByMusicCode(int musicCode, ContentResolver contentResolver) {
        String where = DownloadContract._MUSIC_CODE + "=" + musicCode;

        contentResolver.delete(DownloadContract.URI, where, null);
        contentResolver.notifyChange(DownloadContract.URI, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDownloadMusicExecutorService.shutdown();
        updateWaitingAndPreparingToPauseAsync();

        if (mQueryDownloadDataTask != null) {
            mQueryDownloadDataTask.cancel(false);
        }
    }

    private void updateWaitingAndPreparingToPauseAsync() {
        final ContentResolver contentResolver = getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(DownloadContract._STATE, DownloadState.PAUSE);

                String where = DownloadContract._STATE + " IN (" + DownloadState.PREPARING +
                        "," + DownloadState.WAITING + ")";

                updateDownload(contentResolver, values, where);
            }
        }).start();
    }

    public class DownloadMusicBinder extends Binder {

        private DownloadMusicService mDownloadMusicService;

        DownloadMusicBinder(DownloadMusicService downloadMusicService) {
            mDownloadMusicService = downloadMusicService;
        }

        public DownloadMusicService getService(@Nullable OnDownloadMusicServiceListener listener) {
            mDownloadMusicService.setListener(listener);
            return mDownloadMusicService;
        }
    }

    public interface OnDownloadMusicServiceListener {

        void onDownloadFailed();
        void onDownloadStopped();
        void onProgressUpdate(int musicCode, int progress);
        void onDownloadSucceeded();
    }
}
