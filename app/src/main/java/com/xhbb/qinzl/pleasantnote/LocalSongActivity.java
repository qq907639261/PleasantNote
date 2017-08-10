package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.model.Music;

public class LocalSongActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private LocalSongAdapter mLocalSongAdapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, LocalSongActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mLocalSongAdapter = new LocalSongAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mLocalSongAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: 2017/8/10 不能只查询music表，看来不得不用到多表查询了
        String selection = MusicContract._TYPE + "=" + MusicType.LOCAL;
        return new CursorLoader(this, MusicContract.URI, null, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mLocalSongAdapter.swapCursor(cursor);
        if (cursor.getCount() == 0) {
            Toast.makeText(LocalSongActivity.this, R.string.local_song_data_is_empty_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLocalSongAdapter.swapCursor(null);
    }

    private class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.ViewHolder> {

        private Cursor mCursor;
        private SparseIntArray mSongPositionSparseArray;

        LocalSongAdapter() {
            mSongPositionSparseArray = new SparseIntArray();
        }

        void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }

        void putSongPositionSparseArray(int songCode, int songPosition) {
            mSongPositionSparseArray.put(songCode, songPosition);
        }

        int getSongPosition(int songCode) {
            return mSongPositionSparseArray.get(songCode);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_song, parent, false);
            return new ViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindAdapter(mCursor, position);
        }

        @Override
        public int getItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mMusicNameText;
            private TextView mSingerNameText;
            private ProgressBar mDownloadProgress;
            private LocalSongAdapter mLocalSongAdapter;

            ViewHolder(View itemView, LocalSongAdapter localSongAdapter) {
                super(itemView);
                mLocalSongAdapter = localSongAdapter;

                mMusicNameText = itemView.findViewById(R.id.musicNameText);
                mSingerNameText = itemView.findViewById(R.id.singerNameText);
                mDownloadProgress = itemView.findViewById(R.id.downloadProgress);
            }

            void bindAdapter(Cursor cursor, int position) {
                cursor.moveToPosition(position);
                Music music = new Music(cursor);

                mMusicNameText.setText(music.getName());
                mSingerNameText.setText(music.getSingerName());

                mLocalSongAdapter.putSongPositionSparseArray(music.getCode(), position);
            }
        }
    }
}
