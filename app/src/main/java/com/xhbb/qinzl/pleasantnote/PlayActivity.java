package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.Enums.LoopType;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityPlayBinding;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutOneImageBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class PlayActivity extends AppCompatActivity
        implements Response.Listener<String>,
        Response.ErrorListener {

    private static final Object REQUESTS_TAG = PlayActivity.class.getSimpleName();

    private ActivityPlay mActivityPlay;
    private LocalReceiver mLocalReceiver;

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

        PlaySpinnerAdapter playSpinnerAdapter = new PlaySpinnerAdapter();

        mActivityPlay = new ActivityPlay(this, playSpinnerAdapter);
        mLocalReceiver = new LocalReceiver();

        binding.setActivityPlay(mActivityPlay);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceive(intent);
        }
    }

    private class PlaySpinnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 3;
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
        public View getView(int loopType, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                LayoutOneImageBinding binding = DataBindingUtil.inflate(getLayoutInflater(),
                        R.layout.layout_one_image, viewGroup, false);

                view = binding.getRoot();
                viewHolder = new ViewHolder();

                viewHolder.mImageView = binding.imageView;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            setImageViewByLoopType(loopType, viewHolder);

            return view;
        }

        private void setImageViewByLoopType(int loopType, ViewHolder viewHolder) {
            ImageView imageView = viewHolder.mImageView;
            switch (loopType) {
                case LoopType.LIST:
                    imageView.setImageResource(R.drawable.ic_list_loop);
                    imageView.setContentDescription(getString(R.string.list_loop_accessibility));
                    break;
                case LoopType.RANDOM:
                    imageView.setImageResource(R.drawable.ic_random_play);
                    imageView.setContentDescription(getString(R.string.random_play_accessibility));
                    break;
                case LoopType.SINGLE:
                    imageView.setImageResource(R.drawable.ic_single_cycle);
                    imageView.setContentDescription(getString(R.string.single_cycle_accessibility));
                    break;
                default:
            }
        }

        private class ViewHolder {

            private ImageView mImageView;
        }
    }
}
