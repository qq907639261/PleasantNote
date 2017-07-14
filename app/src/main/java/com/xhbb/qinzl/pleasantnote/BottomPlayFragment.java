package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

        return binding.getRoot();
    }

    private void setBindingVariable(FragmentBottomPlayBinding binding) {
        Music music = getArguments().getParcelable(ARG_MUSIC);
        Context context = getContext();

        if (music != null) {
            String imageUrl = music.getSmallPicture();
            String musicName = music.getName();
            String singer = music.getSinger();
            mFragmentBottomPlay = new FragmentBottomPlay(
                    context, imageUrl, musicName, singer, this);
        } else {
            mFragmentBottomPlay = new FragmentBottomPlay(context, this);
        }

        binding.setFragmentBottomPlay(mFragmentBottomPlay);
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

    }
}
