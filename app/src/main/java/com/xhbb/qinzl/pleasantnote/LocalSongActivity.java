package com.xhbb.qinzl.pleasantnote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.async.DownloadMusicService;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.Enums.DownloadState;
import com.xhbb.qinzl.pleasantnote.common.Enums.MusicType;
import com.xhbb.qinzl.pleasantnote.data.Contracts.DownloadContract;
import com.xhbb.qinzl.pleasantnote.data.Contracts.MusicContract;
import com.xhbb.qinzl.pleasantnote.data.DbHelper;
import com.xhbb.qinzl.pleasantnote.model.Download;
import com.xhbb.qinzl.pleasantnote.model.LocalSong;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.util.ArrayList;
import java.util.List;

public class LocalSongActivity extends AppCompatActivity
        implements ServiceConnection,
        DownloadMusicService.OnDownloadMusicServiceListener {

    private LocalSongAdapter mLocalSongAdapter;
    private AsyncTask<Void, Void, List<LocalSong>> mInitLocalSongDataTask;
    private DownloadMusicService mDownloadMusicService;

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

        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                .commit();
    }

    private LocalSongAdapter getLocalSongAdapter() {
        return mLocalSongAdapter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        executeInitLocalSongDataTask(this);
        bindService(DownloadMusicService.newIntent(this), this, Context.BIND_AUTO_CREATE);
    }

    private void executeInitLocalSongDataTask(final LocalSongActivity localSongActivity) {
        if (mInitLocalSongDataTask != null) {
            mInitLocalSongDataTask.cancel(false);
        }

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
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInitLocalSongDataTask.cancel(false);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mDownloadMusicService = ((DownloadMusicService.DownloadMusicBinder) iBinder).getService(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onProgressUpdate(int musicCode, int progress) {
        Integer songPosition = mLocalSongAdapter.getSongPosition(musicCode);

        if (songPosition != null) {
            mLocalSongAdapter.setDownloadProgress(songPosition, progress);
            mLocalSongAdapter.notifyItemChanged(songPosition);
        }
    }

    @Override
    public void onDownloading(int musicCode) {
        Integer songPosition = mLocalSongAdapter.getSongPosition(musicCode);

        if (songPosition != null) {
            mLocalSongAdapter.setDownloadState(songPosition, DownloadState.DOWNLOADING);
            mLocalSongAdapter.notifyItemChanged(songPosition);
        }
    }

    private DownloadMusicService getDownloadMusicService() {
        return mDownloadMusicService;
    }

    private class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.ViewHolder> {

        private static final int TYPE_DEFAULT = 0;
        private static final int TYPE_SONG_UN_DOWNLOADED = 1;

        private List<LocalSong> mLocalSongs;
        private SparseArray<Integer> mSongPositionSparseArray;
        private LocalSongActivity mLocalSongActivity;

        LocalSongAdapter(LocalSongActivity localSongActivity) {
            mLocalSongs = new ArrayList<>();
            mSongPositionSparseArray = new SparseArray<>();
            mLocalSongActivity = localSongActivity;
        }

        void swapLocalSongs(List<LocalSong> localSongs) {
            mLocalSongs = localSongs;
            notifyDataSetChanged();
        }

        Integer getSongPosition(int songCode) {
            return mSongPositionSparseArray.get(songCode);
        }

        void setDownloadProgress(int songPosition, int progress) {
            mLocalSongs.get(songPosition).setDownloadProgress(progress);
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

            return new ViewHolder(view, viewType, mLocalSongActivity);
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

        void setDownloadState(Integer songPosition, int downloadState) {
            mLocalSongs.get(songPosition).setDownloadState(downloadState);
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View mItemView;
            private TextView mMusicNameText;
            private TextView mSingerNameText;
            private ProgressBar mDownloadProgress;
            private CheckBox mSelectSongCheckBox;
            private LocalSong mLocalSong;
            private LocalSongActivity mLocalSongActivity;

            ViewHolder(View itemView, int viewType, LocalSongActivity localSongActivity) {
                super(itemView);
                mItemView = itemView;
                mMusicNameText = itemView.findViewById(R.id.musicNameText);
                mSingerNameText = itemView.findViewById(R.id.singerNameText);
                mLocalSongActivity = localSongActivity;

                if (viewType == LocalSongAdapter.TYPE_DEFAULT) {
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
                    int downloadProgress = localSong.getDownloadProgress();
                    mDownloadProgress.setProgress(downloadProgress);

                    if (downloadProgress == 100) {
                        mLocalSong.setDownloadState(DownloadState.DOWNLOADED);
                        mDownloadProgress.setVisibility(View.GONE);
                    }
                }

                mItemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int downloadState = mLocalSong.getDownloadState();

                if (downloadState != DownloadState.DOWNLOADED) {
                    DownloadMusicService downloadMusicService = mLocalSongActivity.getDownloadMusicService();

                    Download download = new Download(mLocalSong.getMusicCode(),
                            mLocalSong.getDownloadState(), mLocalSong.getDownloadUrl(),
                            mLocalSong.getDownloadProgress());

                    if (downloadState == DownloadState.DOWNLOADING
                            || downloadState == DownloadState.WAITING) {
                        mLocalSong.setDownloadState(DownloadState.PAUSE);

                        if (downloadState == DownloadState.DOWNLOADING) {
                            downloadMusicService.changeDownloadStates(mLocalSong.getMusicCode(), DownloadState.PAUSE);
                        } else {
                            downloadMusicService.updateDownloadStateAndStartServiceAsync(download,
                                    DownloadState.PAUSE,
                                    mLocalSongActivity.getContentResolver());
                        }
                    } else {
                        mLocalSong.setDownloadState(DownloadState.WAITING);

                        downloadMusicService.updateDownloadStateAndStartServiceAsync(download,
                                DownloadState.WAITING,
                                mLocalSongActivity.getContentResolver());
                    }

                    return;
                }

                Music music = new Music(mLocalSong.getMusicName(), mLocalSong.getMusicCode(),
                        mLocalSong.getPlayUrl(), mLocalSong.getMusicType(),
                        mLocalSong.getTotalSeconds(), mLocalSong.getSingerName(),
                        mLocalSong.getBigPictureUrl(), mLocalSong.getSmallPictureUrl());

                Intent service = MusicService.newPlayNewMusicIntent(mLocalSongActivity, music);
                mLocalSongActivity.startService(service);
            }
        }
    }
}
