package com.xhbb.qinzl.pleasantnote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.DateTimeUtils;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class PlayActivity extends AppCompatActivity implements Response.Listener<String>,
        Response.ErrorListener, AdapterView.OnItemSelectedListener, View.OnClickListener,
        ServiceConnection, MusicService.OnMusicServiceListener {

    private static final Object REQUESTS_TAG = "PlayActivity";

    private ActivityPlay mActivityPlay;
    private PlaySpinnerAdapter mPlaySpinnerAdapter;
    private ActivityPlayBinding mBinding;
    private MusicService mMusicService;

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
        mPlaySpinnerAdapter.recyclePlaySpinnerIcons();
        if (mMusicService != null) {
            unbindService(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.cancelRequests(this, REQUESTS_TAG);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mActivityPlay.setLyrics(getString(R.string.search_lyrics_failed_text));
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
                    mBinding.playOrPauseSwitcher.setDisplayedChild(1);
                    startPlayChrononmeter();
                }
                break;
            case R.id.pauseButton:
                mMusicService.pause();
                mBinding.playChrononmeter.stop();
                mBinding.playOrPauseSwitcher.setDisplayedChild(0);
                break;
            case R.id.playNextButton:
                mMusicService.playNext();
                break;
            case R.id.playPreviousButton:
                mMusicService.playPrevious();
                break;
            default:
        }
    }

    private void startPlayChrononmeter() {
        setPlayChrononmeterBase();
        mBinding.playChrononmeter.start();
    }

    private void setPlayChrononmeterBase() {
        int currentPlayMillis = mMusicService.getCurrentPosition();
        mBinding.playChrononmeter.setBase(SystemClock.elapsedRealtime() - currentPlayMillis);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMusicService = ((MusicService.MusicBinder) iBinder).getService();
        mMusicService.setOnMusicServiceListener(this);

        Music music = mMusicService.getMusic();
        if (music != null) {
            displayCurrentMusicData(music);

            if (mMusicService.isPlaying()) {
                mBinding.playOrPauseSwitcher.setDisplayedChild(1);
                startPlayChrononmeter();
            } else {
                setPlayChrononmeterBase();
            }
        }
    }

    private void displayCurrentMusicData(Music music) {
        NetworkUtils.addLyricsRequest(this, music.getCode(), REQUESTS_TAG, this, this);

        String playDuration = DateTimeUtils.getPlayDuration(this, music.getSeconds());

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
        displayCurrentMusicData(mMusicService.getMusic());
        mBinding.playOrPauseSwitcher.setDisplayedChild(1);
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
            return mPlaySpinnerIcons == null ? 0 : mPlaySpinnerIcons.length();
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
