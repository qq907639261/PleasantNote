package com.xhbb.qinzl.pleasantnote;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;

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
    private CheckFavoritedTask mCheckFavoritedTask;
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

        if (mMusicService != null) {
            unbindService(this);
        }

        if (mCheckFavoritedTask != null) {
            mCheckFavoritedTask.cancel(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.cancelRequests(this, REQUESTS_TAG);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mActivityPlay.setLyrics(getString(R.string.no_lyrics_text));
        mActivityPlay.setLyricsColor(this, true);
    }

    @Override
    public void onResponse(String response) {
        setLyrics(response);
    }

    private void setLyrics(final String response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String lyrics = JsonUtils.getLyrics(response);
                mActivityPlay.setLyrics(lyrics);
                mActivityPlay.setLyricsColor(PlayActivity.this, false);
            }
        }).start();
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
                mMusicService.play();
                if (mMusicService.isPlaying()) {
                    mBinding.playSwitcher.setDisplayedChild(1);
                    startPlayChrononmeter();
                }
                break;
            case R.id.pauseButton:
                mMusicService.pause();
                mBinding.playChrononmeter.stop();
                mBinding.playSwitcher.setDisplayedChild(0);
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
            default:
        }
    }

    private void changeFavoritedSwitcher(boolean favorited) {
        mFavoritedChangedMusic = mMusicService.getMusic();
        if (mFavoritedChangedMusic != null) {
            mFavoritedChangedMusic.setMusicType(MusicType.FAVORITED);
            mBinding.favoritedSwitcher.setDisplayedChild(favorited ? 1 : 0);
        }
    }

    private void startPlayChrononmeter() {
        setPlayChrononmeterBase();
        mBinding.playChrononmeter.start();
    }

    private void setPlayChrononmeterBase() {
        int currentPlayMillis = mMusicService.getPlayedMillis();
        mBinding.playChrononmeter.setBase(SystemClock.elapsedRealtime() - currentPlayMillis);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMusicService = ((MusicService.MusicBinder) iBinder).getService(this);

        Music music = mMusicService.getMusic();
        if (music != null) {
            displayCurrentMusicData(music);

            if (mMusicService.isPlaying()) {
                mBinding.playSwitcher.setDisplayedChild(1);
                startPlayChrononmeter();
            } else {
                setPlayChrononmeterBase();
            }
        }
    }

    private void refreshFavoritedSwitcher(Music music) {
        if (mCheckFavoritedTask != null) {
            mCheckFavoritedTask.cancel(false);
        }
        mCheckFavoritedTask = new CheckFavoritedTask();
        mCheckFavoritedTask.execute(music);
    }

    private void displayCurrentMusicData(Music music) {
        refreshFavoritedSwitcher(music);
        NetworkUtils.addLyricsRequest(this, music.getCode(), REQUESTS_TAG, this, this);

        String playDuration = DateTimeUtils.getFormattedTime(this, music.getSeconds());
        int progress = (int) TimeUnit.MILLISECONDS.toSeconds(mMusicService.getPlayedMillis());

        mBinding.playSeekBar.setMax(music.getSeconds());
        mBinding.playSeekBar.setProgress(progress);
        mBinding.playDuration.setText(playDuration);
        mActivityPlay.setBigPicture(this, music.getBigPicture());
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }

    @Override
    public void onMediaPlayerStarted() {
        startPlayChrononmeter();
    }

    @Override
    public void onMediaPlayerPreparing() {
        updateFavoritedData();
        displayCurrentMusicData(mMusicService.getMusic());
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
                    String where = MusicContract._CODE + "=" + musicCode + " AND "
                            + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                    getContentResolver().delete(MusicContract.URI, where, null);
                    if (favorited) {
                        getContentResolver().insert(MusicContract.URI, musicValues);
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

    private class CheckFavoritedTask extends AsyncTask<Music, Void, Boolean> {

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
