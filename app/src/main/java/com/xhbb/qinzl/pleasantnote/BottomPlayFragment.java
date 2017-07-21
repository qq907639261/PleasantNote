package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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

    private FragmentBottomPlay mFragmentBottomPlay;
    private LocalReceiver mLocalReceiver;

    public static BottomPlayFragment newInstance() {
        return new BottomPlayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentBottomPlayBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_bottom_play, container, false);

        mFragmentBottomPlay = new FragmentBottomPlay(getContext(), this);
        mLocalReceiver = new LocalReceiver();

        if (savedInstanceState == null) {
            startMusicService(MusicService.ACTION_INIT_MUSIC);
        }

        binding.setFragmentBottomPlay(mFragmentBottomPlay);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerLocalReceiver();
        startMusicService(MusicService.ACTION_SEND_MUSIC);
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contracts.ACTION_MUSIC_PLAYED);
        filter.addAction(Contracts.ACTION_MUSIC_STOPPED);
        filter.addAction(Contracts.ACTION_CURRENT_MUSIC_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLocalReceiver, filter);
    }

    private void startMusicService(String action) {
        Context context = getContext();
        Intent intent = MusicService.newIntent(context, action);
        context.startService(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalReceiver);
    }

    @Override
    public void onClickPlayButton() {
        startMusicService(MusicService.ACTION_PLAY_OR_PAUSE);
    }

    @Override
    public void onClickNextButton() {
        startMusicService(MusicService.ACTION_PLAY_NEXT);
    }

    @Override
    public void onClickBottomPlayFragment() {
        PlayActivity.start(getContext());
    }

    private void handleReceive(Intent intent) {
        switch (intent.getAction()) {
            case Contracts.ACTION_CURRENT_MUSIC_UPDATED:
                Music music = intent.getParcelableExtra(MusicService.EXTRA_MUSIC);
                mFragmentBottomPlay.setImageUrl(music.getSmallPicture());
                mFragmentBottomPlay.setMusicName(music.getName());
                mFragmentBottomPlay.setSinger(music.getSinger());
                mFragmentBottomPlay.setPlayButtonDrawable(getContext(), true);
                break;
            case Contracts.ACTION_MUSIC_PLAYED:
                mFragmentBottomPlay.setPlayButtonDrawable(getContext(), true);
                break;
            case Contracts.ACTION_MUSIC_STOPPED:
                mFragmentBottomPlay.setPlayButtonDrawable(getContext(), false);
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
}
