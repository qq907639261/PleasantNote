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
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ItemMusic;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicQueryFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener,
        LayoutRecyclerView.OnLayoutRecyclerViewListener {

    private static final String ARG_QUERY = "ARG_QUERY";
    private static final String ARG_CURRENT_PAGE = "ARG_CURRENT_PAGE";
    private static final String ARG_SCROLLED_TO_END = "ARG_SCROLLED_TO_END";
    private static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";

    private MusicAdapter mMusicAdapter;
    private String mQuery;
    private LayoutRecyclerView mLayoutRecyclerView;
    private int mCurrentPage;
    private boolean mScrolledToEnd;
    private LinearLayoutManager mLayoutManager;
    private boolean mHasMusicData;
    private boolean mViewRecreating;

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
            mCurrentPage = savedInstanceState.getInt(ARG_CURRENT_PAGE);
            mScrolledToEnd = savedInstanceState.getBoolean(ARG_SCROLLED_TO_END);

            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);
            mLayoutManager.scrollToPosition(itemPosition);
        }

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);

        // TODO: 2017/7/11 还需要修复旋转BUG，以及实现滚动刷新。

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

    public void refreshData(String query) {
        mQuery = query;
        getArguments().putString(ARG_QUERY, mQuery);

        mLayoutRecyclerView.setRefreshing(true);
        mLayoutRecyclerView.setTipsText(getString(R.string.refreshing_text));

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mCurrentPage = 1;
        mScrolledToEnd = false;
        mHasMusicData = false;

        Context context = getContext();
        NetworkUtils.addQueryRequest(context, mQuery, 1, this, this);

        String selection = MusicContract._QUERY + "=?";
        String[] selectionArgs = new String[]{mQuery};

        return new CursorLoader(context, MusicContract.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);
        mHasMusicData = cursor.getCount() > 0;

        if (mHasMusicData) {
            mLayoutRecyclerView.setTipsText(null);
            mLayoutRecyclerView.setRefreshing(false);
        } else if (mViewRecreating) {
            NetworkUtils.addQueryRequest(getContext(), mQuery, mCurrentPage, this, this);
        }

        mViewRecreating = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    @Override
    public void onSwipeRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLayoutRecyclerView.setRefreshing(false);
        if (!mHasMusicData) {
            mLayoutRecyclerView.setTipsText(getString(R.string.network_error_text));
        }
    }

    @Override
    public void onResponse(String response) {
        ContentValues[] musicValueses = JsonUtils.getMusicValueses(response, mQuery);
        if (!isAdded()) {
            return;
        }

        if (musicValueses != null) {
            if (musicValueses.length < NetworkUtils.COUNT_OF_QUERY_PAGE) {
                mScrolledToEnd = true;
            }
            boolean firstPage = mCurrentPage == 1;
            MainTasks.updateMusicData(getContext(), musicValueses, firstPage);
        } else {
            if (mMusicAdapter.getItemCount() > 0) {
                mScrolledToEnd = true;
                mMusicAdapter.notifyItemChanged(mMusicAdapter.getItemCount() - 1);
            } else {
                mLayoutRecyclerView.setTipsText(getString(R.string.empty_data_text));
                mLayoutRecyclerView.setRefreshing(false);
            }
        }
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
                binding.setVariable(BR.scrolledToEnd, mScrolledToEnd);
            }

            binding.executePendingBindings();
        }
    }
}
