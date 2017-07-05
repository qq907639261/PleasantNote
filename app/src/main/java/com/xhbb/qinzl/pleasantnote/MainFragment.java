package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;

public class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private MusicAdapter mMusicAdapter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutRecyclerViewBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.layout_recycler_view, container, false);

        mMusicAdapter = new MusicAdapter(getContext(), R.layout.item_music_list);

        LayoutRecyclerView layoutRecyclerView = new LayoutRecyclerView();

        getLoaderManager().initLoader(0, null, this);

        binding.setLayoutRecyclerView(layoutRecyclerView);
        return binding.getRoot();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), Contracts.MusicContract.URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    private class MusicAdapter extends RecyclerViewAdapter {

        MusicAdapter(Context context, int defaultLayoutRes) {
            super(context, defaultLayoutRes);
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {

        }
    }
}
