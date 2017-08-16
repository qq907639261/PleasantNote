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
        executeQueryDownloadDataTask();
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
        startService(newIntent(this));
    }

//    public void changeDownloadStates(int musicCode, int downloadState) {
//        if (mDownloadStates.indexOfKey(musicCode) >= 0) {
//            mDownloadStates.put(musicCode, downloadState);
//        }
//    }

//    public void updateDownloadStateAndStartServiceAsync(final Download download,
//                                                        final int downloadState,
//                                                        final ContentResolver contentResolver) {
//        final DownloadMusicService downloadMusicService = this;
//        final Handler handler = mHandler;
//        final ExecutorService downloadMusicExecutorService = mDownloadMusicExecutorService;
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                downloadMusicService.updateDownloadState(download, downloadState, contentResolver);
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!downloadMusicExecutorService.isShutdown()) {
//                            downloadMusicService.startDownload();
//                        }
//                    }
//                });
//            }
//        }).start();
//    }

    private void updateDownloadState(Download download, int downloadState,
                                     ContentResolver contentResolver) {
        download.setState(downloadState);
        ContentValues downloadValues = download.getDownloadValues();

        int musicCode = downloadValues.getAsInteger(DownloadContract._MUSIC_CODE);
        String where = DownloadContract._MUSIC_CODE + "=" + musicCode;

        contentResolver.update(DownloadContract.URI, downloadValues, where, null);
        contentResolver.notifyChange(DownloadContract.URI, null);
    }

    private void executeQueryDownloadDataTask() {
        if (mQueryDownloadDataTask != null) {
            mQueryDownloadDataTask.cancel(false);
        }

        final DownloadMusicService downloadMusicService = this;

        mQueryDownloadDataTask = new AsyncTask<Void, Void, List<Download>>() {
            private ContentResolver mContentResolver;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContentResolver = downloadMusicService.getApplicationContext().getContentResolver();
            }

            @Override
            protected List<Download> doInBackground(Void... voids) {
                int limit = DownloadMusicService.THREAD_COUNT_IN_POOL -
                        downloadMusicService.getDownloadStates().size();

                String selection = DownloadContract._STATE + "=" + DownloadState.WAITING;
                String sortOrder = DownloadContract._ID + " LIMIT " + limit;
                Cursor cursor = mContentResolver.query(DownloadContract.URI, null, selection, null, sortOrder);

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

                if (downloads.size() == 0) {
                    downloadMusicService.stopSelf();
                    return;
                }

                for (Download download : downloads) {
                    int musicCode = download.getMusicCode();

                    mDownloadStates.put(musicCode, DownloadState.DOWNLOADING);
                    executeDownloadMusicExecutorService(download);
                }
            }
        }.execute();
    }

    private SparseIntArray getDownloadStates() {
        return mDownloadStates;
    }

    private void executeDownloadMusicExecutorService(final Download download) {
        final ContentResolver contentResolver = getContentResolver();
        final int musicCode = download.getMusicCode();
        final ExecutorService downloadMusicExecutorService = mDownloadMusicExecutorService;
        final SparseIntArray downloadStates = mDownloadStates;
        final Handler handler = mHandler;
        final DownloadMusicService downloadMusicService = this;

        mDownloadMusicExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (downloadMusicExecutorService.isShutdown()) {
                    return;
                }

                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    finishDownload(musicCode, DownloadState.FAILED);
                    return;
                }

                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                String downloadUrl = download.getUrl();
                File musicFile = new File(dir, musicCode + ".mp3");

                downloadMusicService.updateDownloadState(download, DownloadState.DOWNLOADING, contentResolver);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        OnDownloadMusicServiceListener listener = downloadMusicService.getListener();
                        if (listener != null) {
                            listener.onDownloading(musicCode);
                        }
                    }
                });

                if (!musicFile.exists()) {
                    boolean createFileFailed = false;

                    try {
                        if (!musicFile.createNewFile()) {
                            createFileFailed = true;
                            return;
                        }

                        int random = mRandom.nextInt(40) + 1;
                        downloadUrl = "http://10.0.2.2/music/" + random + ".mp3";

                        download.setUrl(downloadUrl);
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

                    if (dir.getFreeSpace() < (contentLength - downloadedLength + 1024)) {
                        finishDownload(musicCode, DownloadState.FAILED);
                        return;
                    }

                    byte[] b = new byte[1024];
                    int len;
                    while ((len = inputStream.read(b)) != -1) {
                        int downloadState = downloadStates.get(musicCode);

                        if (downloadState == DownloadState.PAUSE || downloadState == DownloadState.CANCEL) {
                            if (downloadState == DownloadState.CANCEL) {
                                //noinspection ResultOfMethodCallIgnored
                                musicFile.delete();
                                deleteDownloadByMusicCode(musicCode, contentResolver);
                            }

                            download.setProgress((int) (((double) downloadedLength / contentLength) * 100));
                            finishDownload(musicCode, downloadState);
                            return;
                        }

                        randomAccessFile.write(b, 0, len);

                        downloadedLength += len;
                        final int progress = (int) (((double) downloadedLength / contentLength) * 100);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                OnDownloadMusicServiceListener listener = downloadMusicService.getListener();
                                if (listener != null) {
                                    listener.onProgressUpdate(musicCode, progress);
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
                downloadMusicService.updateDownloadState(download, downloadState, contentResolver);
                downloadStates.delete(musicCode);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!downloadMusicExecutorService.isShutdown()) {
                            downloadMusicService.startDownload();

                            OnDownloadMusicServiceListener listener = downloadMusicService.getListener();
                            if (listener != null) {
                                switch (downloadState) {
                                    case DownloadState.DOWNLOADED:
                                        listener.onDownloaded(musicCode);
                                        break;
                                    case DownloadState.PAUSE:
                                        listener.onPause(musicCode);
                                        break;
                                    default:
                                }
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
        updateWaitingToPauseAsync();

        if (mQueryDownloadDataTask != null) {
            mQueryDownloadDataTask.cancel(false);
        }
    }

    private void updateWaitingToPauseAsync() {
        final ContentResolver contentResolver = getApplicationContext().getContentResolver();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String selection = DownloadContract._STATE + "=" + DownloadState.WAITING;

                Cursor cursor = contentResolver.query(DownloadContract.URI, null, selection, null, null);
                if (cursor != null) {
                    boolean moveSucceeded = cursor.moveToFirst();

                    while (moveSucceeded) {
                        Download download = new Download(cursor);
                        ContentValues downloadValues = download.getDownloadValues();

                        String where = DownloadContract._MUSIC_CODE + "=" + download.getMusicCode();
                        downloadValues.put(DownloadContract._STATE, DownloadState.PAUSE);

                        contentResolver.update(DownloadContract.URI, downloadValues, where, null);

                        moveSucceeded = cursor.moveToNext();
                    }
                    contentResolver.notifyChange(DownloadContract.URI, null);

                    cursor.close();
                }
            }
        }).start();
    }

    private OnDownloadMusicServiceListener getListener() {
        return mListener;
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

        void onProgressUpdate(int musicCode, int progress);
        void onDownloading(int musicCode);
        void onDownloaded(int musicCode);
        void onPause(int musicCode);
    }
}
