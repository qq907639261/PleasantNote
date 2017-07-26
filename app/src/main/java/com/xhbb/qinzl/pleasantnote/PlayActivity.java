package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.data.PrefrencesUtils;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutImageViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class PlayActivity extends AppCompatActivity
        implements Response.Listener<String>,
        Response.ErrorListener,
        AdapterView.OnItemSelectedListener {

    private static final Object REQUESTS_TAG = "PlayActivity";

    private ActivityPlay mActivityPlay;
    private LocalReceiver mLocalReceiver;
    private PlaySpinnerAdapter mPlaySpinnerAdapter;
    private boolean mCreating;

    public static void start(Context context) {
        context.startActivity(newIntent(context));
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PlayActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPlayBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_play);

        int playSpinnerSelection = PrefrencesUtils.getPlaySpinnerSelection(this);

        mCreating = true;
        mPlaySpinnerAdapter = new PlaySpinnerAdapter();
        mActivityPlay = new ActivityPlay(this, mPlaySpinnerAdapter, playSpinnerSelection);
        mLocalReceiver = new LocalReceiver();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            BottomSheetBehavior.from(binding.bottom).setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        binding.playSpinner.setOnItemSelectedListener(this);
        mPlaySpinnerAdapter.obtainPlaySpinnerIcons();
        binding.setActivityPlay(mActivityPlay);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mCreating) {
            mPlaySpinnerAdapter.obtainPlaySpinnerIcons();
        }
        mCreating = false;
        registerLocalReceiver();
        startService(MusicService.newIntent(this, MusicService.ACTION_SEND_MUSIC));
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contracts.ACTION_MUSIC_PLAYED);
        filter.addAction(Contracts.ACTION_CURRENT_MUSIC_SENT);

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlaySpinnerAdapter.recyclePlaySpinnerIcons();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
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

    private void handleReceive(Intent intent) {
        switch (intent.getAction()) {
            case Contracts.ACTION_CURRENT_MUSIC_SENT:
                Music music = intent.getParcelableExtra(MusicService.EXTRA_MUSIC);
                mActivityPlay.setBigPicture(this, music.getBigPicture());
                NetworkUtils.addLyricsRequest(this, music.getCode(), REQUESTS_TAG, this, this);
                break;
            case Contracts.ACTION_MUSIC_PLAYED:

                break;
            default:
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        PrefrencesUtils.savePlaySpinnerSelection(this, i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceive(intent);
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
                LayoutImageViewBinding binding = DataBindingUtil.inflate(getLayoutInflater(),
                        R.layout.layout_image_view, viewGroup, false);

                view = binding.getRoot();
                viewHolder = new ViewHolder();

                viewHolder.mImageView = binding.imageView;
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
