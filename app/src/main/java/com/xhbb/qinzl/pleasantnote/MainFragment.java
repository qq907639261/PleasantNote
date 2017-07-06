package com.xhbb.qinzl.pleasantnote;

import android.content.ContentValues;
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
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.common.Enums.ErrorState;
import com.xhbb.qinzl.pleasantnote.common.RecyclerViewAdapter;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.server.JsonUtils;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>, Response.ErrorListener {

    private MusicAdapter mMusicAdapter;
    private int mRankingId;
    private String mQuery;
    private LayoutRecyclerView mLayoutRecyclerView;
    private int mErrorState;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutRecyclerViewBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.layout_recycler_view, container, false);

        mLayoutRecyclerView = new LayoutRecyclerView();
        mMusicAdapter = new MusicAdapter(getContext(), R.layout.item_music_master);

        initData();
        getLoaderManager().initLoader(0, null, this);

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        return binding.getRoot();
    }

    private void initData() {
        Context context = getContext();
        mRankingId = context.getResources().getInteger(R.integer.ranking_id_default);
        NetworkUtils.addRankingRequest(context, mRankingId, this, this);
    }

    public void refreshData(int rankingId) {
        mRankingId = rankingId;
        mQuery = null;

        NetworkUtils.addRankingRequest(getContext(), mRankingId, this, this);
        getLoaderManager().restartLoader(0, null, this);
    }

    public void refreshData(String query) {
        mQuery = query;
        mRankingId = 0;

        NetworkUtils.addQueryRequest(getContext(), query, 1, this, this);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        boolean networkAvailable = NetworkUtils.isNetworkAvailable(context);
        mErrorState = networkAvailable ? ErrorState.NO_ERROR : ErrorState.NETWORK_ERROR;

        mLayoutRecyclerView.setErrorText(null);
        mLayoutRecyclerView.setAutoRefreshing(true);

        String selection;
        String[] selectionArgs;
        if (mQuery != null) {
            selection = MusicContract._QUERY + "=?";
            selectionArgs = new String[]{mQuery};
        } else {
            selection = MusicContract._RANKING_ID + "=?";
            selectionArgs = new String[]{String.valueOf(mRankingId)};
        }

        return new CursorLoader(context, MusicContract.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);

        if (cursor.getCount() > 0) {
            mLayoutRecyclerView.setAutoRefreshing(false);
            return;
        }

        if (mErrorState != ErrorState.NO_ERROR) {
            mLayoutRecyclerView.setAutoRefreshing(false);

            switch (mErrorState) {
                case ErrorState.NETWORK_ERROR:
                    mLayoutRecyclerView.setErrorText(getString(R.string.network_error_text));
                    break;
                default:
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMusicAdapter.swapCursor(null);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mErrorState = ErrorState.NETWORK_ERROR;
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResponse(String response) {
        mErrorState = ErrorState.NO_ERROR;

        ContentValues[] musicValueses;
        if (mQuery != null) {
            musicValueses = JsonUtils.getMusicValueses(response, mQuery);
        } else {
            musicValueses = JsonUtils.getMusicValueses(response, mRankingId);
        }

        if (musicValueses == null) {
            Toast.makeText(getContext(), R.string.already_to_end_toast, Toast.LENGTH_SHORT).show();
        } else {

        }
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
