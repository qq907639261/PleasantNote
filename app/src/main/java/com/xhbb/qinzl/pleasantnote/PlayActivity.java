package com.xhbb.qinzl.pleasantnote;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.DownloadMusicService;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.DateTimeUtils;
import com.xhbb.qinzl.pleasantnote.common.Enums.DownloadState;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.common.GlideApp;
import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Download;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements Response.Listener<String>,
        Response.ErrorListener, AdapterView.OnItemSelectedListener, View.OnClickListener,
        ServiceConnection, MusicService.OnMusicServiceListener,
        Chronometer.OnChronometerTickListener, SeekBar.OnSeekBarChangeListener {

    private static final Object REQUESTS_TAG = "PlayActivity";
    private static final String ARG_LYRICS = "ARG_LYRICS";

    private ActivityPlay mActivityPlay;
    private PlaySpinnerAdapter mPlaySpinnerAdapter;
    private ActivityPlayBinding mBinding;
    private MusicService mMusicService;
    private AsyncTask<Void, Void, Boolean> mInitFavoritedSwitcherChildTask;
    private AsyncTask<Void, Void, Boolean> mInitDownloadButtonEnabledTask;
    private AsyncTask<Void, Void, String> mDisplayLyricsTask;
    private AsyncTask<Void, Void, Drawable> mDisplayBackgroundTask;
    private Music mFavoritedChangedMusic;

    public static void start(Context context) {
        context.startActivity(newIntent(context));
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PlayActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_play);

        int playSpinnerSelection = PrefrencesUtils.getPlaySpinnerSelection(this);

        mPlaySpinnerAdapter = new PlaySpinnerAdapter(getResources());
        mActivityPlay = new ActivityPlay(this, mPlaySpinnerAdapter, playSpinnerSelection);

        if (savedInstanceState != null) {
            String lyrics = savedInstanceState.getString(ARG_LYRICS);
            mActivityPlay.setLyrics(lyrics);
        }

        if (isPortraitOrientation()) {
            BottomSheetBehavior.from(mBinding.bottomLayout)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        mBinding.playNextButton.setOnClickListener(this);
        mBinding.playPreviousButton.setOnClickListener(this);
        mBinding.playButton.setOnClickListener(this);
        mBinding.pauseButton.setOnClickListener(this);
        mBinding.playSpinner.setOnItemSelectedListener(this);
        mBinding.alreadyFavoritedFab.setOnClickListener(this);
        mBinding.noFavoritedFab.setOnClickListener(this);
        mBinding.playChrononmeter.setOnChronometerTickListener(this);
        mBinding.playSeekBar.setOnSeekBarChangeListener(this);
        mBinding.downloadButton.setOnClickListener(this);
        mBinding.downloadButton.setEnabled(false);
        mBinding.downloadButton.setImageResource(R.drawable.ic_file_downloaded);

        mBinding.setActivityPlay(mActivityPlay);
    }

    private ActivityPlay getActivityPlay() {
        return mActivityPlay;
    }

    private ActivityPlayBinding getBinding() {
        return mBinding;
    }

    private MusicService getMusicService() {
        return mMusicService;
    }

    private boolean isPortraitOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindMusicService();
        mPlaySpinnerAdapter.obtainPlaySpinnerIcons(getResources());
        mPlaySpinnerAdapter.notifyDataSetChanged();
    }

    private void bindMusicService() {
        Intent service = new Intent(this, MusicService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateFavoritedDataAsync();
        mPlaySpinnerAdapter.recyclePlaySpinnerIcons();
        mBinding.playChrononmeter.stop();
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.cancelRequests(this, REQUESTS_TAG);

        if (mDisplayLyricsTask != null) {
            mDisplayLyricsTask.cancel(false);
        }

        if (mInitFavoritedSwitcherChildTask != null) {
            mInitFavoritedSwitcherChildTask.cancel(false);
        }

        if (mDisplayBackgroundTask != null) {
            mDisplayBackgroundTask.cancel(false);
        }

        if (mInitDownloadButtonEnabledTask != null) {
            mInitDownloadButtonEnabledTask.cancel(false);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        executeDisplayLyricsTask(response);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_LYRICS, mActivityPlay.getLyrics());
    }

    private void executeDisplayLyricsTask(String response) {
        if (mDisplayLyricsTask != null) {
            mDisplayLyricsTask.cancel(false);
        }
        mDisplayLyricsTask = getDisplayLyricsTask(response, this).execute();
    }

    @NonNull
    private AsyncTask<Void, Void, String> getDisplayLyricsTask(final String response,
                                                               final PlayActivity playActivity) {
        return new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return JsonUtils.getLyrics(response);
            }

            @Override
            protected void onPostExecute(String lyrics) {
                super.onPostExecute(lyrics);
                playActivity.getActivityPlay().setLyrics(lyrics);
            }
        };
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        PrefrencesUtils.savePlaySpinnerSelection(this, i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton:
                playMusic();
                break;
            case R.id.pauseButton:
                pauseMusic();
                break;
            case R.id.playNextButton:
                mMusicService.playNext();
                break;
            case R.id.playPreviousButton:
                mMusicService.playPrevious();
                break;
            case R.id.noFavoritedFab:
                changeFavoritedSwitcher(true);
                break;
            case R.id.alreadyFavoritedFab:
                changeFavoritedSwitcher(false);
                break;
            case R.id.downloadButton:
                downloadMusicAsync();
                break;
            default:
        }
    }

    private void pauseMusic() {
        mMusicService.pause();
        mBinding.playChrononmeter.stop();
        mBinding.playSwitcher.setDisplayedChild(0);
    }

    private void playMusic() {
        if (mMusicService.hasMusic()) {
            mBinding.playSwitcher.setDisplayedChild(1);
            setPlayChrononmeterBase();
            mBinding.playChrononmeter.start();
        }
        mMusicService.play();
    }

    private void downloadMusicAsync() {
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, writeExternalStorage) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{writeExternalStorage}, 0);
            return;
        }

        mBinding.downloadButton.setEnabled(false);
        mBinding.downloadButton.setImageResource(R.drawable.ic_file_downloaded);
        Toast.makeText(this, R.string.start_download_music_toast, Toast.LENGTH_SHORT).show();

        final Context context = getApplicationContext();
        final ContentResolver contentResolver = context.getContentResolver();
        final Music currentMusic = mMusicService.getCurrentMusic();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int musicCode = currentMusic.getCode();
                ContentValues musicValues = currentMusic.getMusicValues();
                musicValues.put(MusicContract._TYPE, MusicType.LOCAL);

                String musicWhere = MusicContract._CODE + "=" + musicCode +
                        " AND " + MusicContract._TYPE + "=" + MusicType.LOCAL;

                contentResolver.delete(MusicContract.URI, musicWhere, null);
                contentResolver.insert(MusicContract.URI, musicValues);
                contentResolver.notifyChange(MusicContract.URI, null);

                String selection = DownloadContract._MUSIC_CODE + "=" + musicCode;
                Cursor cursor = contentResolver.query(DownloadContract.URI, null, selection, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    ContentValues downloadValues = new Download(cursor).getDownloadValues();

                    downloadValues.put(DownloadContract._STATE, DownloadState.WAITING);
                    String downloadWhere = DownloadContract._MUSIC_CODE + "=" + musicCode;

                    contentResolver.update(DownloadContract.URI, downloadValues, downloadWhere, null);
                } else {
                    ContentValues downloadValues = new ContentValues();
                    downloadValues.put(DownloadContract._STATE, DownloadState.WAITING);
                    downloadValues.put(DownloadContract._MUSIC_CODE, musicCode);

                    contentResolver.insert(DownloadContract.URI, downloadValues);
                }

                if (cursor != null) {
                    cursor.close();
                }

                contentResolver.notifyChange(DownloadContract.URI, null);
                startService(DownloadMusicService.newIntent(context));
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadMusicAsync();
        } else {
            Toast.makeText(this, R.string.denied_permission_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeFavoritedSwitcher(boolean favorited) {
        mFavoritedChangedMusic = mMusicService.getCurrentMusic();
        if (mFavoritedChangedMusic != null) {
            mBinding.favoritedSwitcher.setDisplayedChild(favorited ? 1 : 0);
        }
    }

    private void setPlayChrononmeterBase() {
        int playedMillis = mMusicService.getPlayedMillis();
        mBinding.playChrononmeter.setBase(SystemClock.elapsedRealtime() - playedMillis);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMusicService = ((MusicService.MusicBinder) iBinder).getService(this);

        boolean hasMusic = mMusicService.hasMusic();
        mBinding.playSeekBar.setEnabled(hasMusic);

        if (hasMusic) {
            updateCurrentMusic();
            setPlayChrononmeterBase();

            if (mMusicService.isPlaying()) {
                mBinding.playChrononmeter.start();
                mBinding.playSwitcher.setDisplayedChild(1);
            }
        }
    }

    private void executeInitFavoritedSwitcherChildTask() {
        if (mInitFavoritedSwitcherChildTask != null) {
            mInitFavoritedSwitcherChildTask.cancel(false);
        }
        mInitFavoritedSwitcherChildTask = getInitFavoritedSwitcherChildTask(this).execute();
    }

    @NonNull
    private AsyncTask<Void, Void, Boolean> getInitFavoritedSwitcherChildTask(
            final PlayActivity playActivity) {

        return new AsyncTask<Void, Void, Boolean>() {
            private ContentResolver mContentResolver;
            private int mMusicCode;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContentResolver = playActivity.getApplicationContext().getContentResolver();
                mMusicCode = playActivity.getMusicService().getCurrentMusic().getCode();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String selection = MusicContract._CODE + "=" + mMusicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                Cursor cursor = mContentResolver.query(MusicContract.URI, null, selection, null, null);
                boolean hasData = false;

                if (cursor != null) {
                    hasData = cursor.getCount() > 0;
                    cursor.close();
                }

                return hasData;
            }

            @Override
            protected void onPostExecute(Boolean favorited) {
                super.onPostExecute(favorited);
                playActivity.getBinding().favoritedSwitcher.setDisplayedChild(favorited ? 1 : 0);
            }
        };
    }

    private void updateCurrentMusic() {
        Music currentMusic = mMusicService.getCurrentMusic();

        NetworkUtils.addLyricsRequest(this, currentMusic.getCode(), REQUESTS_TAG, this, this);
        executeDisplayBackgroundTask(currentMusic.getBigPictureUrl());
        executeInitFavoritedSwitcherChildTask();
        executeInitDownloadButtonEnabledTask();

        String formattedTime = DateTimeUtils.getFormattedTime(this, currentMusic.getTotalSeconds());

        mBinding.playDuration.setText(formattedTime);
        mBinding.playSeekBar.setMax(currentMusic.getTotalSeconds());
    }

    private void executeInitDownloadButtonEnabledTask() {
        if (mInitDownloadButtonEnabledTask != null) {
            mInitDownloadButtonEnabledTask.cancel(false);
        }
        mInitDownloadButtonEnabledTask = getInitDownloadButtonEnabledTask(this).execute();
    }

    private AsyncTask<Void, Void, Boolean> getInitDownloadButtonEnabledTask(
            final PlayActivity playActivity) {

        return new AsyncTask<Void, Void, Boolean>() {
            private ContentResolver mContentResolver;
            private int mMusicCode;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContentResolver = playActivity.getApplicationContext().getContentResolver();
                mMusicCode = playActivity.getMusicService().getCurrentMusic().getCode();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String selection = DownloadContract._MUSIC_CODE + "=" + mMusicCode;

                Cursor cursor = mContentResolver.query(DownloadContract.URI, null, selection, null, null);
                boolean downloadButtonEnabled = cursor == null || cursor.getCount() == 0;

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int downloadState = cursor.getInt(cursor.getColumnIndex(DownloadContract._STATE));
                        downloadButtonEnabled = downloadState == DownloadState.PAUSE
                                || downloadState == DownloadState.FAILED;
                    }

                    cursor.close();
                }

                return downloadButtonEnabled;
            }

            @Override
            protected void onPostExecute(Boolean enabled) {
                super.onPostExecute(enabled);
                ActivityPlayBinding binding = playActivity.getBinding();

                binding.downloadButton.setEnabled(enabled);
                binding.downloadButton.setImageResource(enabled ?
                        R.drawable.ic_file_download : R.drawable.ic_file_downloaded);
            }
        };
    }

    private void executeDisplayBackgroundTask(String pictureUrl) {
        if (mDisplayBackgroundTask != null) {
            mDisplayBackgroundTask.cancel(false);
        }
        mDisplayBackgroundTask = getDisplayBackgroundTask(pictureUrl, this).execute();
    }

    @NonNull
    private AsyncTask<Void, Void, Drawable> getDisplayBackgroundTask(final String pictureUrl,
                                                                     final PlayActivity playActivity) {
        return new AsyncTask<Void, Void, Drawable>() {
            private Context mContext;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContext = playActivity.getApplicationContext();
            }

            @Override
            protected Drawable doInBackground(Void... voids) {
                Drawable bigPicture = null;

                try {
                    bigPicture = GlideApp.with(mContext)
                            .asDrawable()
                            .load(pictureUrl)
                            .error(R.drawable.empty_image)
                            .centerCrop(mContext)
                            .submit()
                            .get();

                    bigPicture.setAlpha(50);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                return bigPicture;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                playActivity.getActivityPlay().setBigPicture(drawable);
            }
        };
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onPrepared() {
        setPlayChrononmeterBase();
        mBinding.playChrononmeter.start();
    }

    @Override
    public void onPreparing() {
        mBinding.playChrononmeter.stop();
        setPlayChrononmeterBase();
        updateFavoritedDataAsync();
        updateCurrentMusic();
        mBinding.playSwitcher.setDisplayedChild(1);
    }

    private void updateFavoritedDataAsync() {
        if (mFavoritedChangedMusic == null) {
            return;
        }

        boolean favorited = mBinding.favoritedSwitcher.getDisplayedChild() == 1;

        final ContentResolver contentResolver = getApplicationContext().getContentResolver();
        final int musicCode = mFavoritedChangedMusic.getCode();
        final ContentValues musicValues = favorited ?
                mFavoritedChangedMusic.getMusicValues() : null;

        mFavoritedChangedMusic = null;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String where = MusicContract._CODE + "=" + musicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                contentResolver.delete(MusicContract.URI, where, null);
                if (musicValues != null) {
                    musicValues.put(MusicContract._TYPE, MusicType.FAVORITED);
                    contentResolver.insert(MusicContract.URI, musicValues);
                }
                contentResolver.notifyChange(MusicContract.URI, null);
            }
        }).start();
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        int progress = (int) TimeUnit.MILLISECONDS.toSeconds(mMusicService.getPlayedMillis());
        mBinding.playSeekBar.setProgress(progress);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            String formattedTime = DateTimeUtils.getFormattedTime(this, progress);
            mBinding.playChrononmeter.setText(formattedTime);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mBinding.playChrononmeter.stop();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progressOfMillis = (int) TimeUnit.SECONDS.toMillis(seekBar.getProgress());

        mMusicService.seekTo(progressOfMillis);
        mBinding.playChrononmeter.setBase(SystemClock.elapsedRealtime() - progressOfMillis);

        if (mMusicService.isPlaying()) {
            mBinding.playChrononmeter.start();
        }
    }

    private class PlaySpinnerAdapter extends BaseAdapter {

        private TypedArray mSwitchModeSpinnerIcons;
        private CharSequence[] mSwitchModeSpinnerAccessibilities;

        private PlaySpinnerAdapter(Resources resources) {
            mSwitchModeSpinnerAccessibilities =
                    resources.getTextArray(R.array.play_spinner_accessibility);
        }

        private void obtainPlaySpinnerIcons(Resources resources) {
            mSwitchModeSpinnerIcons = resources.obtainTypedArray(R.array.play_spinner_drawable);
        }

        private void recyclePlaySpinnerIcons() {
            mSwitchModeSpinnerIcons.recycle();
        }

        @Override
        public int getCount() {
            return mSwitchModeSpinnerAccessibilities.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.spinner_switch_mode_image, viewGroup, false);

                ImageView switchModeImage = view.findViewById(R.id.switchModeImage);
                viewHolder = new ViewHolder(switchModeImage);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.setSwitchModeImageDrawable(mSwitchModeSpinnerIcons.getDrawable(i));
            viewHolder.setSwitchModeImageContentDescription(mSwitchModeSpinnerAccessibilities[i]);

            return view;
        }

        private class ViewHolder {

            private ImageView mSwitchModeImage;

            ViewHolder(ImageView switchModeImage) {
                mSwitchModeImage = switchModeImage;
            }

            void setSwitchModeImageDrawable(Drawable drawable) {
                mSwitchModeImage.setImageDrawable(drawable);
            }

            void setSwitchModeImageContentDescription(CharSequence contentDescription) {
                mSwitchModeImage.setContentDescription(contentDescription);
            }
        }
    }
}
