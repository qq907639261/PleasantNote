package com.xhbb.qinzl.pleasantnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhbb.qinzl.pleasantnote.async.MainTasks;
import com.xhbb.qinzl.pleasantnote.common.Enums.RefreshState;
import com.xhbb.qinzl.pleasantnote.common.Enums.VolleyState;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicQueryFragment extends MainFragment {

    private static final String ARG_QUERY = "ARG_QUERY";
    private static final String ARG_CURRENT_PAGE = "ARG_CURRENT_PAGE";
    private static final String ARG_SCROLLED_TO_END = "ARG_SCROLLED_TO_END";
    private static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";

    private String mQuery;
    private int mCurrentPage;

    public static MusicQueryFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);

        MusicQueryFragment fragment = new MusicQueryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutRecyclerViewBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.layout_recycler_view, container, false);

        Context context = getContext();

        mLayoutManager = new LinearLayoutManager(context);
        mMusicAdapter = new MusicAdapter(R.layout.item_music);
        mLayoutRecyclerView = new LayoutRecyclerView(context, mMusicAdapter, mLayoutManager, this);
        mQuery = getArguments().getString(ARG_QUERY);

        if (savedInstanceState != null) {
            mViewRecreating = true;
            mCurrentPage = savedInstanceState.getInt(ARG_CURRENT_PAGE);
            mScrolledToEnd = savedInstanceState.getBoolean(ARG_SCROLLED_TO_END);

            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);
            mLayoutManager.scrollToPosition(itemPosition);
        }

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtils.cancelAllRequest(getContext(), null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_PAGE, mCurrentPage);
        outState.putInt(ARG_ITEM_POSITION, mLayoutManager.findFirstVisibleItemPosition());
        outState.putBoolean(ARG_SCROLLED_TO_END, mScrolledToEnd);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mCurrentPage = 1;
        mScrolledToEnd = false;
        mHasMusicData = false;
        mRefreshState = RefreshState.SWIPE;

        Context context = getContext();
        NetworkUtils.addQueryRequest(context, mQuery, 1, this, this);

        String selection = MusicContract._QUERY + "=?";
        String[] selectionArgs = new String[]{mQuery};

        return new CursorLoader(context, MusicContract.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onSwipeRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onScrollStateChanged(int newState) {
        if (mScrolledToEnd || mRefreshState != RefreshState.DEFAULT) {
            return;
        }

        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

        if (lastVisibleItemPosition >
                mMusicAdapter.getItemCount() - NetworkUtils.MAX_COUNT_OF_EACH_PAGE) {
            mRefreshState = RefreshState.SCROLL;
            NetworkUtils.addQueryRequest(getContext(), mQuery, ++mCurrentPage, this, this);
        }
    }

    @Override
    public void onResponse(String response) {
        mVolleyState = VolleyState.RESPONSE;
        ContentValues[] musicValueses = JsonUtils.getMusicValueses(response, mQuery);

        if (!isAdded()) {
            return;
        }

        if (musicValueses != null) {
            mScrolledToEnd = musicValueses.length < NetworkUtils.MAX_COUNT_OF_EACH_PAGE;
            boolean firstPage = mCurrentPage == 1;
            MainTasks.updateMusicData(getContext(), musicValueses, firstPage);
        } else {
            mScrolledToEnd = mHasMusicData;
            getLoaderManager().initLoader(0, null, this);
        }
    }
}
