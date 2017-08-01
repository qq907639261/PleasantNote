package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.xhbb.qinzl.pleasantnote.async.UpdateRankingDataService;
import com.xhbb.qinzl.pleasantnote.common.Enums;
import com.xhbb.qinzl.pleasantnote.common.Enums.VolleyState;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.LayoutRecyclerViewBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.LayoutRecyclerView;
import com.xhbb.qinzl.pleasantnote.server.NetworkUtils;

public class MusicRankingFragment extends MainFragment {

    private static final String ARG_RANKING_CODE = "ARG_RANKING_CODE";

    private int mRankingCode;

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
        mRequestsTag = getClass().getSimpleName() + mRankingCode;
        mMusicAdapter.setScrolledToEnd(true);

        if (savedInstanceState != null) {
            int itemPosition = savedInstanceState.getInt(ARG_ITEM_POSITION);
            mViewRecreating = true;
            mLayoutManager.scrollToPosition(itemPosition);
        }

        binding.setLayoutRecyclerView(mLayoutRecyclerView);
        getLoaderManager().initLoader(0, null, this);

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        String selection = Contracts.MusicContract._RANKING_CODE + "=" + mRankingCode + " AND "
                + Contracts.MusicContract._TYPE + "=" + Enums.MusicType.RANKING;

        NetworkUtils.addRankingRequest(context, mRankingCode, mRequestsTag, this, this);

        return new CursorLoader(context, Contracts.MusicContract.URI, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicAdapter.swapCursor(cursor);

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
            mVolleyState = 0;
            mLayoutRecyclerView.setRefreshing(false);
        }
    }

    @Override
    public void onSwipeRefresh() {
        getLoaderManager().restartLoader(0, null, this);
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
        Context context = getContext();
        Intent intent = UpdateRankingDataService.newIntent(context, response, mRankingCode);

        mVolleyState = VolleyState.RESPONSE;
        context.startService(intent);
    }
}
