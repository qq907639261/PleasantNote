package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
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
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.async.UpdateMusicDataService;
import com.xhbb.qinzl.pleasantnote.common.Enums.VolleyState;
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ItemMusic;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener,
        LayoutRecyclerView.OnLayoutRecyclerViewListener {

    private static final String ARG_RANKING_CODE = "ARG_RANKING_CODE";
    private static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";

    private int mRankingCode;
    private Object mRequestTag;
    private OnMainFragmentListener mListener;

    protected LayoutRecyclerView mLayoutRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected boolean mViewRecreating;
    protected MusicAdapter mMusicAdapter;
    protected int mVolleyState;

    public static MainFragment newInstance(int rankingCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_RANKING_CODE, rankingCode);

        MainFragment fragment = new MainFragment();
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
            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);

            mViewRecreating = true;
            mLayoutManager.scrollToPosition(itemPosition);
        }

        mMusicAdapter.setScrolledToEnd(true);
        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelVolleyRequest();
    }

    protected void cancelVolleyRequest() {
        NetworkUtils.cancelAllRequest(getContext(), mRequestTag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentListener) {
            mListener = (OnMainFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(outState);
    }

    protected void saveInstanceState(Bundle outState) {
        outState.putInt(ARG_ITEM_POSITION, mLayoutManager.findFirstVisibleItemPosition());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        NetworkUtils.addRankingRequest(context, mRankingCode, mRequestTag, this, this);

        String selection = MusicContract._RANKING_CODE + "=" + mRankingCode
                + " AND " + MusicContract._ID + ">0";
        return new CursorLoader(context, MusicContract.URI, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);

        if (cursor.getCount() > 0) {
            mLayoutRecyclerView.setTipsText(null);
        } else if (mViewRecreating || mVolleyState == VolleyState.ERROR) {
            mLayoutRecyclerView.setTipsText(getString(R.string.network_error_text));
        }

        if (mViewRecreating || mVolleyState != 0) {
            mViewRecreating = false;
            mVolleyState = 0;
            mLayoutRecyclerView.setRefreshing(false);
        }
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
    public void onScrollStateChanged(int newState) {

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
        Intent intent = UpdateMusicDataService.newIntent(context, response, mRankingCode);
        context.startService(intent);
    }

    class MusicAdapter extends RecyclerViewAdapter
            implements ItemMusic.OnItemMusicListener {

        private static final int TYPE_DEFAULT_ITEM = 0;
        private static final int TYPE_LAST_ITEM = 1;

        private boolean mScrolledToEnd;

        MusicAdapter(int defaultLayoutRes) {
            super(defaultLayoutRes);
        }

        void setScrolledToEnd(boolean scrolledToEnd) {
            mScrolledToEnd = scrolledToEnd;
        }

        boolean isScrolledToEnd() {
            return mScrolledToEnd;
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

            ItemMusic itemMusic = new ItemMusic(getContext(), picture, musicName, singer, seconds,
                    position, this);
            ViewDataBinding binding = holder.getBinding();

            binding.setVariable(BR.itemMusic, itemMusic);
            if (position == getItemCount() - 1) {
                binding.setVariable(BR.scrolledToEnd, mScrolledToEnd);
            }

            binding.executePendingBindings();
        }

        @Override
        public void onClickItem(int itemPosition) {
            mCursor.moveToPosition(itemPosition);
            Music music = new Music(mCursor);
            Context context = getContext();

            Intent intent = MusicService.newIntent(context, MusicService.ACTION_PLAY_NEW_MUSIC, music);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }

            if (mListener != null) {
                mListener.onClickItem(music);
            }
        }
    }

    interface OnMainFragmentListener {

        void onClickItem(Music music);
    }
}
