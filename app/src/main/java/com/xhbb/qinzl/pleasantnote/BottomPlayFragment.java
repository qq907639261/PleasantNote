package com.xhbb.qinzl.pleasantnote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.databinding.FragmentBottomPlayBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.FragmentBottomPlay;
import com.xhbb.qinzl.pleasantnote.model.Music;

public class BottomPlayFragment extends Fragment
        implements FragmentBottomPlay.OnFragmentBottomPlayListener, ServiceConnection,
        MusicService.OnMusicServiceListener {

    private FragmentBottomPlay mFragmentBottomPlay;
    private MusicService mMusicService;

    public static BottomPlayFragment newInstance() {
        return new BottomPlayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentBottomPlayBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_bottom_play, container, false);

        mFragmentBottomPlay = new FragmentBottomPlay(this);

        binding.setFragmentBottomPlay(mFragmentBottomPlay);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        bindMusicService();
    }

    private void bindMusicService() {
        Context context = getContext();
        Intent musicService = new Intent(context, MusicService.class);
        context.bindService(musicService, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMusicService != null) {
            getContext().unbindService(this);
        }
    }

    @Override
    public void onClickPlayButton() {
        mMusicService.play();
        mFragmentBottomPlay.setPlaySwitcherDisplayedChild(mMusicService.isPlaying());
    }

    @Override
    public void onClickPauseButton() {
        mMusicService.pause();
        mFragmentBottomPlay.setPlaySwitcherDisplayedChild(false);
    }

    @Override
    public void onClickNextButton() {
        mMusicService.playNext();
    }

    @Override
    public void onClickBottomPlayFragment() {
        PlayActivity.start(getContext());
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMusicService = ((MusicService.MusicBinder) iBinder).getService(this);

        Music music = mMusicService.getCurrentMusic();
        if (music != null) {
            displayCurrentMusicData(music);
            mFragmentBottomPlay.setPlaySwitcherDisplayedChild(mMusicService.isPlaying());
        }
    }

    private void displayCurrentMusicData(Music music) {
        mFragmentBottomPlay.setImageUrl(music.getSmallPictureUrl());
        mFragmentBottomPlay.setMusicName(music.getName());
        mFragmentBottomPlay.setSinger(music.getSingerName());
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPreparing() {
        displayCurrentMusicData(mMusicService.getCurrentMusic());
        mFragmentBottomPlay.setPlaySwitcherDisplayedChild(true);
    }
}
