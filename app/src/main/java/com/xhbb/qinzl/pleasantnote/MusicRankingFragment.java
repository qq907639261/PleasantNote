package com.xhbb.qinzl.pleasantnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.MainTasks;
import com.xhbb.qinzl.pleasantnote.common.Enums;
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ItemMusic;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicRankingFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener,
        LayoutRecyclerView.OnLayoutRecyclerViewListener {

    private static final String ARG_RANKING_ID = "ARG_RANKING_ID";

    private MusicAdapter mMusicAdapter;
    private int mRankingId;
    private LayoutRecyclerView mLayoutRecyclerView;
    private int mVolleyState;
    private int mRefreshState;

    public static MusicRankingFragment newInstance(int rankingId) {
        Bundle args = new Bundle();
        args.putInt(ARG_RANKING_ID, rankingId);

        MusicRankingFragment fragment = new MusicRankingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutRecyclerViewBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.layout_recycler_view, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        mMusicAdapter = new MusicAdapter(R.layout.item_music);
        mLayoutRecyclerView = new LayoutRecyclerView(mMusicAdapter, layoutManager, this);
        mRankingId = getArguments().getInt(ARG_RANKING_ID);

        getLoaderManager().initLoader(0, null, this);

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        return binding.getRoot();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mRefreshState != Enums.RefreshState.SWIPE) {
            mRefreshState = Enums.RefreshState.AUTO;
            mLayoutRecyclerView.setErrorText(null);
            mLayoutRecyclerView.setAutoRefreshing(true);
        }

        Context context = getContext();
        NetworkUtils.addRankingRequest(context, mRankingId, this, this);

        String selection = MusicContract._RANKING_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(mRankingId)};

        return new CursorLoader(context, MusicContract.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);

        if (cursor.getCount() > 0 && mRefreshState == Enums.RefreshState.AUTO) {
            refreshFinished();
            return;
        }

        if (mVolleyState == Enums.VolleyState.RESPONSE || mVolleyState == Enums.VolleyState.ERROR) {
            if (mVolleyState == Enums.VolleyState.ERROR) {
                if (mRefreshState == Enums.RefreshState.AUTO) {
                    mLayoutRecyclerView.setErrorText(getString(R.string.network_error_text));
                } else {
                    // TODO: 2017/7/8
                }
            } else if (mRefreshState == Enums.RefreshState.SWIPE) {
                mLayoutRecyclerView.setErrorText(null);
            }

            mLayoutRecyclerView.setSwipeRefreshing(false);
            refreshFinished();
        }
    }

    private void refreshFinished() {
        mLayoutRecyclerView.setAutoRefreshing(false);
        mVolleyState = Enums.VolleyState.NOTHING;
        mRefreshState = Enums.RefreshState.NOTHING;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mVolleyState = Enums.VolleyState.ERROR;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResponse(String response) {
        mVolleyState = Enums.VolleyState.RESPONSE;

        ContentValues[] musicValueses = JsonUtils.getMusicValueses(response, mRankingId);
        MainTasks.updateMusicData(getContext(), musicValueses, mRankingId);
    }

    @Override
    public void onSwipeRefresh() {
        mRefreshState = Enums.RefreshState.SWIPE;
        getLoaderManager().restartLoader(0, null, this);
    }

    private class MusicAdapter extends RecyclerViewAdapter {

        private static final int TYPE_DEFAULT_ITEM = 0;
        private static final int TYPE_LAST_ITEM = 1;

        MusicAdapter(int defaultLayoutRes) {
            super(defaultLayoutRes);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_LAST_ITEM;
            }
            return TYPE_DEFAULT_ITEM;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_LAST_ITEM) {
                setLayoutRes(R.layout.item_music_last);
            } else {
                setLayoutRes(R.layout.item_music);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            Music music = new Music(mCursor);

            String picture = music.getSmallPicture();
            String musicName = music.getName();
            String singer = music.getSinger();
            int seconds = music.getSeconds();

            ItemMusic itemMusic = new ItemMusic(getContext(), picture, musicName, singer, seconds);
            ViewDataBinding binding = holder.getBinding();

            binding.setVariable(BR.itemMusic, itemMusic);
            if (position == getItemCount() - 1) {
                binding.setVariable(BR.scrolledToEnd, true);
            }

            binding.executePendingBindings();
        }
    }
}
