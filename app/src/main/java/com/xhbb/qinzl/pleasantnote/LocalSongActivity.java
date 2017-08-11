package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.DbHelper;
import com.xhbb.qinzl.pleasantnote.model.Music;

public class LocalSongActivity extends AppCompatActivity {

    private LocalSongAdapter mLocalSongAdapter;
    private AsyncTask<Void, Void, Cursor> mInitLocalSongDataTask;

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

        executeInitLocalSongDataTask(this);
    }

    private void executeInitLocalSongDataTask(final LocalSongActivity localSongActivity) {
        mInitLocalSongDataTask = new AsyncTask<Void, Void, Cursor>() {
            private Context mContext;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContext = localSongActivity.getApplicationContext();
            }

            @Override
            protected Cursor doInBackground(Void... voids) {
                DbHelper dbHelper = new DbHelper(mContext);
                String sql = "SELECT * FROM " + MusicContract.TABLE +
                        "," + DownloadContract.TABLE + " WHERE " +
                        MusicContract._CODE + "=" + DownloadContract._MUSIC_CODE + " AND " +
                        MusicContract._TYPE + "=" + MusicType.LOCAL;

                return dbHelper.getReadableDatabase().rawQuery(sql, null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                super.onPostExecute(cursor);
                if (cursor.getCount() == 0) {
                    mLocalSongAdapter.swapCursor(null);
                    Toast.makeText(localSongActivity, R.string.local_song_data_is_empty_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                mLocalSongAdapter.swapCursor(cursor);
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitLocalSongDataTask.cancel(false);
        mLocalSongAdapter.swapCursor(null);
    }

    private class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.ViewHolder> {

        private Cursor mCursor;
        private SparseIntArray mSongPositionSparseArray;

        LocalSongAdapter() {
            mSongPositionSparseArray = new SparseIntArray();
        }

        void swapCursor(Cursor cursor) {
            if (mCursor != null) {
                mCursor.close();
            }

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
