package com.xhbb.qinzl.pleasantnote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.UpdateMusicDataService;
import com.xhbb.qinzl.pleasantnote.common.Enums.DataUpdatedState;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.common.Enums.RefreshState;
import com.xhbb.qinzl.pleasantnote.common.Enums.VolleyState;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicQueryFragment extends MainFragment {

    private static final String ARG_QUERY = "ARG_QUERY";
    private static final String ARG_CURRENT_PAGE = "ARG_CURRENT_PAGE";
    private static final String ARG_SCROLLED_TO_END = "ARG_SCROLLED_TO_END";
    private static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";
    private static final String ARG_TIPS_TEXT = "ARG_TIPS_TEXT";

    private String mQuery;
    private int mCurrentPage;
    private int mRefreshState;
    private String mTipsText;
    private LocalReceiver mLocalReceiver;

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

        mLocalReceiver = new LocalReceiver();
        mLayoutManager = new LinearLayoutManager(context);
        mMusicAdapter = new MusicAdapter(R.layout.item_music);
        mLayoutRecyclerView = new LayoutRecyclerView(context, mMusicAdapter, mLayoutManager, this);
        mQuery = getArguments().getString(ARG_QUERY);

        if (savedInstanceState != null) {
            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);
            boolean scrolledToEnd = savedInstanceState.getBoolean(ARG_SCROLLED_TO_END);

            mViewRecreating = true;
            mTipsText = savedInstanceState.getString(ARG_TIPS_TEXT);
            mCurrentPage = savedInstanceState.getInt(ARG_CURRENT_PAGE);
            mLayoutManager.scrollToPosition(itemPosition);
            mMusicAdapter.setScrolledToEnd(scrolledToEnd);
        }

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);
        registerLocalBroadcast(context);

        return binding.getRoot();
    }

    private void registerLocalBroadcast(Context context) {
        IntentFilter filter = new IntentFilter(Contracts.ACTION_MUSIC_DATA_UPDATED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    protected void cancelVolleyRequest() {
        NetworkUtils.cancelAllRequest(getContext(), null);
    }

    @Override
    protected void saveInstanceState(Bundle outState) {
        outState.putInt(ARG_CURRENT_PAGE, mCurrentPage);
        outState.putInt(ARG_ITEM_POSITION, mLayoutManager.findFirstVisibleItemPosition());
        outState.putBoolean(ARG_SCROLLED_TO_END, mMusicAdapter.isScrolledToEnd());
        outState.putString(ARG_TIPS_TEXT, mLayoutRecyclerView.getTipsText());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mCurrentPage = 1;
        mMusicAdapter.setScrolledToEnd(false);
        mRefreshState = RefreshState.SWIPE;

        Context context = getContext();
        NetworkUtils.addQueryRequest(context, mQuery, mCurrentPage, this, this);

        String selection = MusicContract._TYPE + "=" + MusicType.QUERY;
        return new CursorLoader(context, MusicContract.URI, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mRefreshState == RefreshState.SCROLL) {
            int startPosition = mMusicAdapter.getItemCount();
            int itemCount = NetworkUtils.MAX_COUNT_OF_EACH_PAGE;
            mMusicAdapter.swapCursor(cursor, startPosition, itemCount);
        } else {
            mMusicAdapter.swapCursor(cursor);
        }

        if (cursor.getCount() > 0) {
            mLayoutRecyclerView.setTipsText(null);
        } else if (mVolleyState == VolleyState.ERROR) {
            mLayoutRecyclerView.setTipsText(getString(R.string.network_error_text));
        } else if (mViewRecreating) {
            mLayoutRecyclerView.setTipsText(mTipsText);
        }

        if (mViewRecreating || mVolleyState != 0) {
            mViewRecreating = false;
            mLayoutRecyclerView.setRefreshing(false);
            mRefreshState = 0;
            mVolleyState = 0;
        }
    }

    @Override
    public void onSwipeRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onScrollStateChanged(int newState) {
        if (mMusicAdapter.isScrolledToEnd() || mRefreshState != 0) {
            return;
        }

        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
        int refreshPosition = mMusicAdapter.getItemCount() - NetworkUtils.MAX_COUNT_OF_EACH_PAGE;

        if (lastVisibleItemPosition > refreshPosition) {
            mRefreshState = RefreshState.SCROLL;
            NetworkUtils.addQueryRequest(getContext(), mQuery, ++mCurrentPage, this, this);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mVolleyState = VolleyState.ERROR;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResponse(String response) {
        mVolleyState = VolleyState.RESPONSE;

        Context context = getContext();
        boolean firstPage = mCurrentPage == 1;
        Intent intent = UpdateMusicDataService.newIntent(context, response, firstPage);

        context.startService(intent);
    }

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Contracts.ACTION_MUSIC_DATA_UPDATED:
                    int updatedState = intent.getIntExtra(
                            UpdateMusicDataService.EXTRA_DATA_UPDATED_STATE, 0);

                    if (updatedState == DataUpdatedState.SCROLLED_TO_END_UPDATE) {
                        mMusicAdapter.setScrolledToEnd(true);
                    } else if (updatedState == DataUpdatedState.SCROLLED_TO_END_NO_UPDATE) {
                        mMusicAdapter.setScrolledToEnd(true);
                        getLoaderManager().initLoader(0, null, MusicQueryFragment.this);
                    } else if (updatedState == DataUpdatedState.EMPTY_DATA) {
                        mLayoutRecyclerView.setTipsText(getString(R.string.empty_data_text));
                        getLoaderManager().initLoader(0, null, MusicQueryFragment.this);
                    }

                    break;
                default:
            }
        }
    }
}
