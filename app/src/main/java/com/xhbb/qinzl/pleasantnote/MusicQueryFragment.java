package com.xhbb.qinzl.pleasantnote;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import com.xhbb.qinzl.pleasantnote.async.UpdateQueryDataService;
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

    private String mQuery;
    private int mCurrentPage;
    private int mRefreshState;
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

        mLayoutManager = new LinearLayoutManager(context);
        mMusicAdapter = new MusicAdapter(context, R.layout.item_music);
        mLayoutRecyclerView = new LayoutRecyclerView(context, mMusicAdapter, mLayoutManager, this);
        mQuery = getArguments().getString(ARG_QUERY);
        mRequestsTag = getClass().getSimpleName();
        mLocalReceiver = new LocalReceiver(this, mMusicAdapter, mLayoutRecyclerView);

        registerLocalBroadcast(context);

        if (savedInstanceState != null) {
            boolean scrolledToEnd = savedInstanceState.getBoolean(ARG_SCROLLED_TO_END);
            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);

            mViewRecreating = true;
            mLayoutManager.scrollToPosition(itemPosition);
            mCurrentPage = savedInstanceState.getInt(ARG_CURRENT_PAGE);
            mMusicAdapter.setScrolledToEnd(scrolledToEnd);
        }

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);

        return binding.getRoot();
    }

    private void registerLocalBroadcast(Context context) {
        IntentFilter filter = new IntentFilter(Contracts.ACTION_UPDATE_QUERY_DATA_FINISHED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mLocalReceiver, filter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_PAGE, mCurrentPage);
        outState.putBoolean(ARG_SCROLLED_TO_END, mMusicAdapter.isScrolledToEnd());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mLocalReceiver);

        if (!activity.isChangingConfigurations()) {
            deleteQueryMusicAsync();
        }
    }

    private void deleteQueryMusicAsync() {
        final ContentResolver contentResolver = getContext().getApplicationContext().getContentResolver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String where = MusicContract._TYPE + "=" + MusicType.QUERY;
                contentResolver.delete(MusicContract.URI, where, null);
                contentResolver.notifyChange(MusicContract.URI, null);
            }
        }).start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();

        mCurrentPage = 1;
        mMusicAdapter.setScrolledToEnd(false);
        mRefreshState = RefreshState.SWIPE;
        NetworkUtils.addQueryRequest(context, mQuery, mCurrentPage,
                mRequestsTag, this, this);

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
        } else if (mViewRecreating) {
            getLoaderManager().restartLoader(0, null, this);
            mViewRecreating = false;
        } else if (mVolleyState == VolleyState.ERROR) {
            mLayoutRecyclerView.setTipsText(getString(R.string.network_error_text));
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
            NetworkUtils.addQueryRequest(getContext(), mQuery, ++mCurrentPage, mRequestsTag, this, this);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mVolleyState = VolleyState.ERROR;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResponse(String response) {
        Context context = getContext();
        boolean firstPage = mCurrentPage == 1;
        Intent intent = UpdateQueryDataService.newIntent(context, response, firstPage);

        mVolleyState = VolleyState.RESPONSE;
        context.startService(intent);
    }

    private class LocalReceiver extends BroadcastReceiver {

        private MusicQueryFragment mMusicQueryFragment;
        private MusicAdapter mMusicAdapter;
        private LayoutRecyclerView mLayoutRecyclerView;

        private LocalReceiver(MusicQueryFragment musicQueryFragment, MusicAdapter musicAdapter,
                              LayoutRecyclerView layoutRecyclerView) {
            mMusicQueryFragment = musicQueryFragment;
            mMusicAdapter = musicAdapter;
            mLayoutRecyclerView = layoutRecyclerView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Contracts.ACTION_UPDATE_QUERY_DATA_FINISHED:
                    updateQueryData(intent);
                    break;
                default:
            }
        }

        private void updateQueryData(Intent intent) {
            int updatedState = intent.getIntExtra(
                    UpdateQueryDataService.EXTRA_DATA_UPDATED_STATE, 0);

            if (updatedState == DataUpdatedState.SCROLLED_TO_END_UPDATE) {
                mMusicAdapter.setScrolledToEnd(true);
            } else if (updatedState == DataUpdatedState.SCROLLED_TO_END_NO_UPDATE) {
                mMusicAdapter.setScrolledToEnd(true);
                mMusicQueryFragment.getLoaderManager().initLoader(0, null, mMusicQueryFragment);
            } else if (updatedState == DataUpdatedState.EMPTY_DATA_NO_UPDATE) {
                String emptyDataTips = mMusicQueryFragment.getString(R.string.empty_data_text);
                mLayoutRecyclerView.setTipsText(emptyDataTips);
                mMusicQueryFragment.getLoaderManager().initLoader(0, null, mMusicQueryFragment);
            }
        }
    }
}
