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

        setBindingVariable(binding);
        registerLocalReceiver();

        return binding.getRoot();
    }

    private void registerLocalReceiver() {
        mLocalReceiver = new LocalReceiver();
        IntentFilter filter = new IntentFilter(Contracts.ACTION_NEXT_PLAYED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLocalReceiver, filter);
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
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalReceiver);
    }

    @Override
    public void onClickPlayButton() {
        Context context = getContext();
        mFragmentBottomPlay.changePlayButtonDrawable(context);

        Intent intent = MusicService.newIntent(context, Contracts.ACTION_PLAY_PAUSE);
        context.startService(intent);
    }

    @Override
    public void onClickNextButton() {
        Context context = getContext();
        if (!mFragmentBottomPlay.isMusicPlaying()) {
            mFragmentBottomPlay.changePlayButtonDrawable(context);
        }

        Intent intent = MusicService.newIntent(context, Contracts.ACTION_NEXT);
        context.startService(intent);
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Contracts.ACTION_NEXT_PLAYED:
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
