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
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.Enums.DownloadState;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.DbHelper;
import com.xhbb.qinzl.pleasantnote.model.LocalSong;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.util.ArrayList;
import java.util.List;

public class LocalSongActivity extends AppCompatActivity {

    private LocalSongAdapter mLocalSongAdapter;
    private AsyncTask<Void, Void, List<LocalSong>> mInitLocalSongDataTask;

    public static void start(Context context) {
        Intent starter = new Intent(context, LocalSongActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mLocalSongAdapter = new LocalSongAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mLocalSongAdapter);

        executeInitLocalSongDataTask(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                .commit();
    }

    private LocalSongAdapter getLocalSongAdapter() {
        return mLocalSongAdapter;
    }

    private void executeInitLocalSongDataTask(final LocalSongActivity localSongActivity) {
        mInitLocalSongDataTask = new AsyncTask<Void, Void, List<LocalSong>>() {
            private Context mContext;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mContext = localSongActivity.getApplicationContext();
            }

            @Override
            protected List<LocalSong> doInBackground(Void... voids) {
                String sql = "SELECT * FROM " + MusicContract.TABLE + "," +
                        DownloadContract.TABLE + " WHERE " +
                        MusicContract._CODE + "=" + DownloadContract._MUSIC_CODE + " AND " +
                        MusicContract._TYPE + "=" + MusicType.LOCAL;

                DbHelper dbHelper = new DbHelper(mContext);
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, null);

                List<LocalSong> localSongs = new ArrayList<>();

                boolean moveSucceeded = cursor.moveToFirst();
                while (moveSucceeded) {
                    localSongs.add(new LocalSong(cursor));
                    moveSucceeded = cursor.moveToNext();
                }

                cursor.close();
                dbHelper.close();

                return localSongs;
            }

            @Override
            protected void onPostExecute(List<LocalSong> localSongs) {
                super.onPostExecute(localSongs);
                localSongActivity.getLocalSongAdapter().swapLocalSongs(localSongs);

                if (localSongs.size() == 0) {
                    Toast.makeText(localSongActivity, R.string.local_song_data_is_empty_toast, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitLocalSongDataTask.cancel(false);
    }

    private class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.ViewHolder> {

        private static final int TYPE_DEFAULT = 0;
        private static final int TYPE_SONG_UN_DOWNLOADED = 1;

        private List<LocalSong> mLocalSongs;
        private SparseIntArray mSongPositionSparseArray;
        private Context mContext;

        LocalSongAdapter(Context context) {
            mLocalSongs = new ArrayList<>();
            mSongPositionSparseArray = new SparseIntArray();
            mContext = context;
        }

        void swapLocalSongs(List<LocalSong> localSongs) {
            mLocalSongs = localSongs;
            notifyDataSetChanged();
        }

        int getSongPosition(int songCode) {
            return mSongPositionSparseArray.get(songCode);
        }

        @Override
        public int getItemViewType(int position) {
            int downloadState = mLocalSongs.get(position).getDownloadState();

            if (downloadState == DownloadState.DOWNLOADED) {
                return TYPE_DEFAULT;
            } else {
                return TYPE_SONG_UN_DOWNLOADED;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_DEFAULT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_local_song, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_local_song_un_downloaded, parent, false);
            }

            return new ViewHolder(view, viewType, mContext);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LocalSong localSong = mLocalSongs.get(position);

            holder.bindLocalSong(localSong);
            mSongPositionSparseArray.put(localSong.getMusicCode(), position);
        }

        @Override
        public int getItemCount() {
            return mLocalSongs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View mItemView;
            private TextView mMusicNameText;
            private TextView mSingerNameText;
            private ProgressBar mDownloadProgress;
            private CheckBox mSelectSongCheckBox;
            private LocalSong mLocalSong;
            private Context mContext;

            ViewHolder(View itemView, int viewType, Context context) {
                super(itemView);
                mItemView = itemView;
                mMusicNameText = itemView.findViewById(R.id.musicNameText);
                mSingerNameText = itemView.findViewById(R.id.singerNameText);
                mContext = context;

                if (viewType == TYPE_DEFAULT) {
                    mSelectSongCheckBox = itemView.findViewById(R.id.selectSongCheckBox);
                } else {
                    mDownloadProgress = itemView.findViewById(R.id.downloadProgress);
                }
            }

            void bindLocalSong(LocalSong localSong) {
                mLocalSong = localSong;
                mMusicNameText.setText(localSong.getMusicName());
                mSingerNameText.setText(localSong.getSingerName());

                if (mDownloadProgress != null) {
                    mDownloadProgress.setProgress(localSong.getDownloadProgress());
                }

                mItemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (mLocalSong.getDownloadState() != DownloadState.DOWNLOADED) {
                    return;
                }

                Music music = new Music(mLocalSong.getMusicName(), mLocalSong.getMusicCode(),
                        mLocalSong.getPlayUrl(), mLocalSong.getMusicType(),
                        mLocalSong.getTotalSeconds(), mLocalSong.getSingerName(),
                        mLocalSong.getBigPictureUrl(), mLocalSong.getSmallPictureUrl());
                mContext.startService(MusicService.newPlayNewMusicIntent(mContext, music));
            }
        }
    }
}
