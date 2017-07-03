package com.xhbb.qinzl.pleasantnote;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;

public class MainFragment extends Fragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutRecyclerViewBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.layout_recycler_view, container, false);

        LayoutRecyclerView layoutRecyclerView = new LayoutRecyclerView();

        binding.setLayoutRecyclerView(layoutRecyclerView);
        return binding.getRoot();
    }
}
