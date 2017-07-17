package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.FragmentBottomPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.FragmentBottomPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;

public class BottomPlayFragment extends Fragment
        implements FragmentBottomPlay.OnFragmentBottomPlayListener {

    private static final String ARG_MUSIC = "ARG_MUSIC";

    private FragmentBottomPlay mFragmentBottomPlay;
    private LocalReceiver mLocalReceiver;

    public static BottomPlayFragment newInstance(@Nullable Music music) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MUSIC, music);

        BottomPlayFragment fragment = new BottomPlayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentBottomPlayBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_bottom_play, container, false);

        mLocalReceiver = new LocalReceiver();

        setBindingVariable(binding);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Context context = getContext();

        registerLocalReceiver(context);
        context.startService(MusicService.newIntent(context, MusicService.ACTION_SEND_MUSIC_DATA));
    }

    private void registerLocalReceiver(Context context) {
        IntentFilter filter = new IntentFilter(Contracts.ACTION_PLAYING_MUSIC_UPDATED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalReceiver);
    }

    private void setBindingVariable(FragmentBottomPlayBinding binding) {
        Music music = getArguments().getParcelable(ARG_MUSIC);
        Context context = getContext();

        if (music != null) {
            mFragmentBottomPlay = new FragmentBottomPlay(context,
                    music.getSmallPicture(),
                    music.getName(),
                    music.getSinger(),
                    this);
        } else {
            mFragmentBottomPlay = new FragmentBottomPlay(context, this);
        }

        binding.setFragmentBottomPlay(mFragmentBottomPlay);
    }

    @Override
    public void onClickPlayButton() {
        // TODO: 2017/7/17 判断有没有正在播放的歌曲，如果没有就随机播放一首，貌似到了绑定服务的时候了
        Context context = getContext();
        mFragmentBottomPlay.changePlayButtonDrawable(context);

        Intent intent = MusicService.newIntent(context, MusicService.ACTION_PLAY_OR_PAUSE);
        context.startService(intent);
    }

    @Override
    public void onClickNextButton() {
        // TODO: 2017/7/17 判断有没有正在播放的歌曲，如果没有就随机播放一首，貌似到了绑定服务的时候了
        Context context = getContext();
        if (!mFragmentBottomPlay.isMusicPlaying()) {
            mFragmentBottomPlay.changePlayButtonDrawable(context);
        }

        Intent intent = MusicService.newIntent(context, MusicService.ACTION_PLAY_NEXT);
        context.startService(intent);
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Contracts.ACTION_PLAYING_MUSIC_UPDATED:
                    Music music = intent.getParcelableExtra(MusicService.EXTRA_MUSIC);

                    mFragmentBottomPlay.setImageUrl(music.getSmallPicture());
                    mFragmentBottomPlay.setMusicName(music.getName());
                    mFragmentBottomPlay.setSinger(music.getSinger());

                    getArguments().putParcelable(ARG_MUSIC, music);
                    break;
                default:
            }
        }
    }
}
