package com.xhbb.qinzl.pleasantnote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BottomPlayFragment extends Fragment {

    public static BottomPlayFragment newInstance() {
        return new BottomPlayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_play, container, false);
    }
}
