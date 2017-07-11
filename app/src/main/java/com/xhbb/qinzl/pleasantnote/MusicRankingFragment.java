package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
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
import com.xhbb.qinzl.pleasantnote.async.UpdateMusicDataService;
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ItemMusic;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicRankingFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener,
        LayoutRecyclerView.OnLayoutRecyclerViewListener {

    private static final String ARG_RANKING_CODE = "ARG_RANKING_CODE";
    private static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";

    private MusicAdapter mMusicAdapter;
    private int mRankingCode;
    private Object mRequestTag;
    private LayoutRecyclerView mLayoutRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private boolean mViewRecreating;
    private boolean mHasMusicData;

    public static MusicRankingFragment newInstance(int rankingCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_RANKING_CODE, rankingCode);

        MusicRankingFragment fragment = new MusicRankingFragment();
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
        mRankingCode = getArguments().getInt(ARG_RANKING_CODE);
        mRequestTag = mRankingCode;

        if (savedInstanceState != null) {
            mViewRecreating = true;
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
        NetworkUtils.cancelAllRequest(getContext(), mRequestTag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_ITEM_POSITION, mLayoutManager.findFirstVisibleItemPosition());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        NetworkUtils.addRankingRequest(context, mRankingCode, mRequestTag, this, this);

        String selection = MusicContract._RANKING_CODE + "=?";
        String[] selectionArgs = new String[]{String.valueOf(mRankingCode)};
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
            NetworkUtils.addRankingRequest(getContext(), mRankingCode, mRequestTag, this, this);
        }

        mViewRecreating = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    @Override
    public void onSwipeRefresh() {
        NetworkUtils.addRankingRequest(getContext(), mRankingCode, mRequestTag, this, this);
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
        Context context = getContext();
        Intent intent = UpdateMusicDataService.newIntent(context, response, mRankingCode);
        context.startService(intent);
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
