package com.xhbb.qinzl.pleasantnote;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.DateTimeUtils;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements Response.Listener<String>,
        Response.ErrorListener, AdapterView.OnItemSelectedListener, View.OnClickListener,
        ServiceConnection, MusicService.OnMusicServiceListener,
        Chronometer.OnChronometerTickListener, SeekBar.OnSeekBarChangeListener {

    private static final Object REQUESTS_TAG = "PlayActivity";

    private ActivityPlay mActivityPlay;
    private PlaySpinnerAdapter mPlaySpinnerAdapter;
    private ActivityPlayBinding mBinding;
    private MusicService mMusicService;
    private AsyncTask<Music, Void, Boolean> mInitFavoritedTask;
    private AsyncTask<Void, Void, String> mDisplayLyricsTask;
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

        mPlaySpinnerAdapter = new PlaySpinnerAdapter();
        mActivityPlay = new ActivityPlay(this, mPlaySpinnerAdapter, playSpinnerSelection);

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

        mBinding.setActivityPlay(mActivityPlay);
    }

    private boolean isPortraitOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindMusicService();
        mPlaySpinnerAdapter.obtainPlaySpinnerIcons();
        mPlaySpinnerAdapter.notifyDataSetChanged();
    }

    private void bindMusicService() {
        Intent service = new Intent(this, MusicService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateFavoritedData();
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

        if (mInitFavoritedTask != null) {
            mInitFavoritedTask.cancel(false);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        displayLyrics(response);
    }

    private void displayLyrics(final String response) {
        if (mDisplayLyricsTask != null) {
            mDisplayLyricsTask.cancel(false);
        }

        mDisplayLyricsTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return JsonUtils.getLyrics(response);
            }

            @Override
            protected void onPostExecute(String lyrics) {
                super.onPostExecute(lyrics);
                mActivityPlay.setLyrics(lyrics);
            }
        }.execute();
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
                downloadMusic();
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

    private void downloadMusic() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        Music currentMusic = mMusicService.getCurrentMusic();
        String fileName = String.valueOf(currentMusic.getCode());

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File file = new File(dir, fileName);

        if (file.exists()) {
            Toast.makeText(mMusicService, "音乐已下载", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse("http://10.0.2.2/XL_9.1.34.812.exe");
//        Uri uri = Uri.parse(currentMusic.getDownloadUrl());
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadMusic();
        } else {
            Toast.makeText(mMusicService, R.string.download_permission_denied_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeFavoritedSwitcher(boolean favorited) {
        mFavoritedChangedMusic = mMusicService.getCurrentMusic();
        if (mFavoritedChangedMusic != null) {
            mFavoritedChangedMusic.setMusicType(MusicType.FAVORITED);
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

        if (mMusicService.hasMusic()) {
            updateCurrentMusic();
            setPlayChrononmeterBase();

            if (mMusicService.isPlaying()) {
                mBinding.playChrononmeter.start();
                mBinding.playSwitcher.setDisplayedChild(1);
            }
        }
    }

    private void initFavoritedSwitcher() {
        if (mInitFavoritedTask != null) {
            mInitFavoritedTask.cancel(false);
        }

        mInitFavoritedTask = new AsyncTask<Music, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Music... musics) {
                Music music = musics[0];
                String selection = MusicContract._CODE + "=" + music.getCode() + " AND "
                        + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                Cursor cursor = getContentResolver().query(MusicContract.URI, null, selection, null, null);

                boolean favorited = false;
                if (cursor != null) {
                    favorited = cursor.getCount() > 0;
                    cursor.close();
                }

                return favorited;
            }

            @Override
            protected void onPostExecute(Boolean favorited) {
                super.onPostExecute(favorited);
                mBinding.favoritedSwitcher.setDisplayedChild(favorited ? 1 : 0);
            }
        }.execute(mMusicService.getCurrentMusic());
    }

    private void updateCurrentMusic() {
        Music currentMusic = mMusicService.getCurrentMusic();

        NetworkUtils.addLyricsRequest(this, currentMusic.getCode(), REQUESTS_TAG, this, this);
        mActivityPlay.setBigPicture(this, currentMusic.getBigPicture());
        initFavoritedSwitcher();

        String formattedTime = DateTimeUtils.getFormattedTime(this, currentMusic.getSeconds());

        mBinding.playDuration.setText(formattedTime);
        mBinding.playSeekBar.setMax(currentMusic.getSeconds());
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
        updateFavoritedData();
        updateCurrentMusic();
        mBinding.playSwitcher.setDisplayedChild(1);
    }

    private void updateFavoritedData() {
        if (mFavoritedChangedMusic != null) {
            final boolean favorited = mBinding.favoritedSwitcher.getDisplayedChild() == 1;
            final int musicCode = mFavoritedChangedMusic.getCode();
            final ContentValues musicValues = favorited ?
                    mFavoritedChangedMusic.getMusicValues() : null;

            mFavoritedChangedMusic = null;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentResolver contentResolver = getContentResolver();
                    String where = MusicContract._CODE + "=" + musicCode + " AND "
                            + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                    contentResolver.delete(MusicContract.URI, where, null);
                    if (favorited) {
                        contentResolver.insert(MusicContract.URI, musicValues);
                    }
                }
            }).start();
        }
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

        private TypedArray mPlaySpinnerIcons;
        private String[] mPlaySpinnerAccessibilities;

        private PlaySpinnerAdapter() {
            mPlaySpinnerAccessibilities =
                    getResources().getStringArray(R.array.play_spinner_accessibilities);
        }

        private void obtainPlaySpinnerIcons() {
            mPlaySpinnerIcons = getResources().obtainTypedArray(R.array.play_spinner_drawables);
        }

        private void recyclePlaySpinnerIcons() {
            mPlaySpinnerIcons.recycle();
        }

        @Override
        public int getCount() {
            return mPlaySpinnerAccessibilities.length;
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
                view = getLayoutInflater().inflate(R.layout.layout_image_view, viewGroup, false);
                viewHolder = new ViewHolder();

                viewHolder.mImageView = view.findViewById(R.id.imageView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.mImageView.setImageDrawable(mPlaySpinnerIcons.getDrawable(i));
            viewHolder.mImageView.setContentDescription(mPlaySpinnerAccessibilities[i]);

            return view;
        }

        private class ViewHolder {

            private ImageView mImageView;
        }
    }
}
