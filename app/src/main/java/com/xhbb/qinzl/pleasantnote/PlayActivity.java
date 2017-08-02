package com.xhbb.qinzl.pleasantnote;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
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
import com.xhbb.qinzl.pleasantnote.common.GlideApp;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
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
    private AsyncTask<Void, Void, Boolean> mInitFavoritedTask;
    private AsyncTask<String, Void, String> mDisplayLyricsTask;
    private AsyncTask<String, Void, Drawable> mDisplayBackgroundTask;
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

        if (mDisplayBackgroundTask != null) {
            mDisplayBackgroundTask.cancel(false);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        displayLyrics(response);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_LYRICS, mActivityPlay.getLyrics());
    }

    private void displayLyrics(String response) {
        if (mDisplayLyricsTask != null) {
            mDisplayLyricsTask.cancel(false);
        }

        mDisplayLyricsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... responses) {
                return JsonUtils.getLyrics(responses[0]);
            }

            @Override
            protected void onPostExecute(String lyrics) {
                super.onPostExecute(lyrics);
                mActivityPlay.setLyrics(lyrics);
            }
        }.execute(response);
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

        mInitFavoritedTask = new AsyncTask<Void, Void, Boolean>() {
            private ContentResolver iContentResolver;
            private int iMusicCode;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                iContentResolver = getContentResolver();
                iMusicCode = mMusicService.getCurrentMusic().getCode();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String selection = MusicContract._CODE + "=" + iMusicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                Cursor cursor = iContentResolver.query(MusicContract.URI, null, selection, null, null);

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
        }.execute();
    }

    private void updateCurrentMusic() {
        Music currentMusic = mMusicService.getCurrentMusic();

        NetworkUtils.addLyricsRequest(this, currentMusic.getCode(), REQUESTS_TAG, this, this);
        displayBackground(currentMusic.getBigPictureUrl());
        initFavoritedSwitcher();

        String formattedTime = DateTimeUtils.getFormattedTime(this, currentMusic.getSeconds());

        mBinding.playDuration.setText(formattedTime);
        mBinding.playSeekBar.setMax(currentMusic.getSeconds());
    }

    private void displayBackground(String pictureUrl) {
        if (mDisplayBackgroundTask != null) {
            mDisplayBackgroundTask.cancel(false);
        }

        mDisplayBackgroundTask = new AsyncTask<String, Void, Drawable>() {
            private Activity iActivity;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                iActivity = PlayActivity.this;
            }

            @Override
            protected Drawable doInBackground(String... pictureUrls) {
                Drawable bigPicture = null;

                try {
                    bigPicture = GlideApp.with(iActivity)
                            .asDrawable()
                            .load(pictureUrls[0])
                            .error(R.drawable.empty_image)
                            .centerCrop(iActivity)
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
                mActivityPlay.setBigPicture(drawable);
            }
        }.execute(pictureUrl);
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
        if (mFavoritedChangedMusic == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            private ContentResolver iContentResolver;
            private int iMusicCode;
            private ContentValues iMusicValues;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                boolean favorited = mBinding.favoritedSwitcher.getDisplayedChild() == 1;

                iContentResolver = getContentResolver();
                iMusicCode = mFavoritedChangedMusic.getCode();
                iMusicValues = favorited ? mFavoritedChangedMusic.getMusicValues() : null;

                mFavoritedChangedMusic = null;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                String where = MusicContract._CODE + "=" + iMusicCode + " AND "
                        + MusicContract._TYPE + "=" + MusicType.FAVORITED;

                iContentResolver.delete(MusicContract.URI, where, null);
                if (iMusicValues != null) {
                    iContentResolver.insert(MusicContract.URI, iMusicValues);
                }

                return null;
            }
        }.execute();
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

        private TypedArray iPlaySpinnerIcons;
        private String[] iPlaySpinnerAccessibilities;

        private PlaySpinnerAdapter() {
            iPlaySpinnerAccessibilities =
                    getResources().getStringArray(R.array.play_spinner_accessibility);
        }

        private void obtainPlaySpinnerIcons() {
            iPlaySpinnerIcons = getResources().obtainTypedArray(R.array.play_spinner_drawable);
        }

        private void recyclePlaySpinnerIcons() {
            iPlaySpinnerIcons.recycle();
        }

        @Override
        public int getCount() {
            return iPlaySpinnerAccessibilities.length;
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

                viewHolder.iImageView = view.findViewById(R.id.imageView);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.iImageView.setImageDrawable(iPlaySpinnerIcons.getDrawable(i));
            viewHolder.iImageView.setContentDescription(iPlaySpinnerAccessibilities[i]);

            return view;
        }

        private class ViewHolder {

            private ImageView iImageView;
        }
    }
}
