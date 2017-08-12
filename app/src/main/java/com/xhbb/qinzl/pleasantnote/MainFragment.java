package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ItemMusic;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.model.Music;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public abstract class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener,
        LayoutRecyclerView.OnLayoutRecyclerViewListener {

    protected static final String ARG_ITEM_POSITION = "ARG_ITEM_POSITION";

    protected LayoutRecyclerView mLayoutRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected boolean mViewRecreating;
    protected MusicAdapter mMusicAdapter;
    protected int mVolleyState;
    protected Object mRequestsTag;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.cancelRequests(getContext(), mRequestsTag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_ITEM_POSITION, mLayoutManager.findFirstVisibleItemPosition());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    @SuppressWarnings("WeakerAccess")
    protected class MusicAdapter extends RecyclerViewAdapter
            implements ItemMusic.OnItemMusicListener {

        private static final int TYPE_DEFAULT_ITEM = 0;
        private static final int TYPE_LAST_ITEM = 1;

        private boolean mScrolledToEnd;

        MusicAdapter(Context context, int defaultLayoutRes) {
            super(context, defaultLayoutRes);
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
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);
            Music music = new Music(cursor);

            String picture = music.getSmallPictureUrl();
            String musicName = music.getName();
            String singer = music.getSingerName();
            int seconds = music.getTotalSeconds();

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
            Cursor cursor = getCursor();
            cursor.moveToPosition(itemPosition);
            Music music = new Music(cursor);

            Context context = getContext();
            Intent intent = MusicService.newPlayNewMusicIntent(context, music);
            context.startService(intent);
        }
    }
}
