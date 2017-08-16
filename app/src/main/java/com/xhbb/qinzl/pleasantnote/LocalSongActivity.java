package com.xhbb.qinzl.pleasantnote;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.xhbb.qinzl.pleasantnote.model.LocalSong;
import com.xhbb.qinzl.pleasantnote.model.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalSongActivity extends AppCompatActivity
        implements ServiceConnection,
        DownloadMusicService.OnDownloadMusicServiceListener,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        AlertDialogFragment.OnAlertDialogFragmentListener {

    private static final String DIALOG_FRAGMENT_TAG = "dialog";

    private LocalSongAdapter mLocalSongAdapter;
    private AsyncTask<Void, Void, List<LocalSong>> mInitLocalSongDataTask;
    private Button mCancelButton;
    private CheckBox mAllSongCheckBox;
    private Button mBatchManageButton;
    private View mBottomToolbar;
    private View mBottomFragmentContainer;

    public static void start(Context context) {
        Intent starter = new Intent(context, LocalSongActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_song);

        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mBatchManageButton = (Button) findViewById(R.id.batchManageButton);
        mAllSongCheckBox = (CheckBox) findViewById(R.id.selectAllCheckBox);
        mBottomToolbar = findViewById(R.id.bottomToolbar);
        mBottomFragmentContainer = findViewById(R.id.bottom_fragment_container);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);

        mLocalSongAdapter = new LocalSongAdapter(this, mAllSongCheckBox);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mLocalSongAdapter);

        mBatchManageButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mAllSongCheckBox.setOnCheckedChangeListener(this);
        deleteButton.setOnClickListener(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        executeInitLocalSongDataTask();
        bindService(DownloadMusicService.newIntent(this), this, Context.BIND_AUTO_CREATE);
    }

    private void executeInitLocalSongDataTask() {
        if (mInitLocalSongDataTask != null) {
            mInitLocalSongDataTask.cancel(false);
        }

        final Context context = getApplicationContext();
        final LocalSongAdapter localSongAdapter = mLocalSongAdapter;

        mInitLocalSongDataTask = new AsyncTask<Void, Void, List<LocalSong>>() {
            @Override
            protected List<LocalSong> doInBackground(Void... voids) {
                String sql = "SELECT * FROM " + MusicContract.TABLE + "," +
                        DownloadContract.TABLE + " WHERE " +
                        MusicContract._CODE + "=" + DownloadContract._MUSIC_CODE + " AND " +
                        MusicContract._TYPE + "=" + MusicType.LOCAL;

                DbHelper dbHelper = new DbHelper(context);
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
                localSongAdapter.swapLocalSongs(localSongs);

                if (localSongs.size() == 0) {
                    Toast.makeText(context, R.string.local_song_data_is_empty_toast, Toast.LENGTH_SHORT).show();
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
        ((DownloadMusicService.DownloadMusicBinder) iBinder).getService(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onProgressUpdate(int musicCode, int progress) {
        int songPosition = mLocalSongAdapter.getPositionInSparseArray(musicCode);
        mLocalSongAdapter.setDownloadProgress(songPosition, progress);
        mLocalSongAdapter.notifyItemChanged(songPosition);
    }

    @Override
    public void onDownloading(int musicCode) {
        changeDownloadState(musicCode, DownloadState.DOWNLOADING);
    }

    @Override
    public void onDownloaded(int musicCode) {
        changeDownloadState(musicCode, DownloadState.DOWNLOADED);
        mLocalSongAdapter.deletePositionInSparseArray(musicCode);
    }

    @Override
    public void onPause(int musicCode) {
        changeDownloadState(musicCode, DownloadState.PAUSE);
    }

    private void changeDownloadState(int musicCode, int downloadState) {
        int songPosition = mLocalSongAdapter.getPositionInSparseArray(musicCode);
        mLocalSongAdapter.setDownloadState(songPosition, downloadState);
        mLocalSongAdapter.notifyItemChanged(songPosition);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.batchManageButton:
                onBatchManageButtonClick();
                break;
            case R.id.cancelButton:
                onCancelButtonClick();
                break;
            case R.id.deleteButton:
                onDeleteButtonClick();
                break;
            default:
        }
    }

    private void onDeleteButtonClick() {
        DialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                getString(R.string.delete_song_dialog_message),
                getString(R.string.delete_dialog_title));

        alertDialogFragment.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    private void deleteFileAsync(final List<String> filePaths) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String filePath : filePaths) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(filePath).delete();
                }
            }
        }).start();
    }

    private void deleteDataAsync(final Uri uri, final String where) {
        final ContentResolver contentResolver = getApplicationContext().getContentResolver();

        new Thread(new Runnable() {
            @Override
            public void run() {
                contentResolver.delete(uri, where, null);
            }
        }).start();
    }

    private void onCancelButtonClick() {
        replaceBatchManageView(false);
    }

    private void onBatchManageButtonClick() {
        replaceBatchManageView(true);
    }

    private void replaceBatchManageView(boolean batchManageViewVisible) {
        mBatchManageButton.setVisibility(batchManageViewVisible ? View.GONE : View.VISIBLE);
        mBottomFragmentContainer.setVisibility(batchManageViewVisible ? View.GONE : View.VISIBLE);
        mCancelButton.setVisibility(batchManageViewVisible ? View.VISIBLE : View.GONE);
        mAllSongCheckBox.setVisibility(batchManageViewVisible ? View.VISIBLE : View.GONE);
        mBottomToolbar.setVisibility(batchManageViewVisible ? View.VISIBLE : View.GONE);
        mLocalSongAdapter.swapSongCheckBoxVisible(batchManageViewVisible);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getTag() == null) {
            List<LocalSong> localSongs = mLocalSongAdapter.getLocalSongs();
            for (LocalSong localSong : localSongs) {
                localSong.setSongChecked(b);
            }

            mLocalSongAdapter.notifyDataSetChanged();
        } else {
            compoundButton.setTag(null);
        }
    }

    @Override
    public void onDialogPositiveButtonClick() {
        deleteSelectedSongs();
    }

    private void deleteSelectedSongs() {
        List<LocalSong> localSongs = mLocalSongAdapter.getLocalSongs();
        List<LocalSong> newLocalSongs = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        StringBuilder deleteWhere = new StringBuilder(" IN (");

        for (int i = 0; i < localSongs.size(); i++) {
            LocalSong localSong = localSongs.get(i);

            if (!localSong.isSongChecked()) {
                newLocalSongs.add(localSong);
                continue;
            }

            if (filePaths.size() > 0) {
                deleteWhere.append(",");
            }

            filePaths.add(localSong.getPlayUrl());
            deleteWhere.append(localSong.getMusicCode());
        }

        deleteWhere.append(")");

        if (filePaths.size() == 0) {
            Toast.makeText(this, R.string.no_select_song_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        mLocalSongAdapter.swapLocalSongs(newLocalSongs);
        replaceBatchManageView(false);

        String downloadDeleteWhere = DownloadContract._MUSIC_CODE + deleteWhere;
        String musicDeleteWhere = MusicContract._CODE + deleteWhere + " AND " +
                MusicContract._TYPE + "=" + MusicType.LOCAL;

        deleteDataAsync(DownloadContract.URI, downloadDeleteWhere);
        deleteDataAsync(MusicContract.URI, musicDeleteWhere);
        deleteFileAsync(filePaths);
    }

    private class LocalSongAdapter extends RecyclerView.Adapter<LocalSongAdapter.ViewHolder> {

        private static final int TYPE_DEFAULT = 0;
        private static final int TYPE_SONG_UN_DOWNLOADED = 1;

        private List<LocalSong> mLocalSongs;
        private Context mContext;
        private boolean mSongCheckBoxVisible;
        private CheckBox mAllSongCheckBox;
        private SparseIntArray mPositionSparseArray;

        LocalSongAdapter(Context context, CheckBox allSongCheckBox) {
            mPositionSparseArray = new SparseIntArray();
            mLocalSongs = new ArrayList<>();
            mContext = context;
            mAllSongCheckBox = allSongCheckBox;
        }

        void swapLocalSongs(List<LocalSong> localSongs) {
            mLocalSongs = localSongs;
            notifyDataSetChanged();
        }

        void swapSongCheckBoxVisible(boolean songCheckBoxVisible) {
            mSongCheckBoxVisible = songCheckBoxVisible;
            notifyDataSetChanged();
        }

        int getPositionInSparseArray(int musicCode) {
            return mPositionSparseArray.get(musicCode);
        }

        void deletePositionInSparseArray(int musicCode) {
            mPositionSparseArray.delete(musicCode);
        }

        List<LocalSong> getLocalSongs() {
            return mLocalSongs;
        }

        void setDownloadProgress(int songPosition, int progress) {
            mLocalSongs.get(songPosition).setDownloadProgress(progress);
        }

        @Override
        public int getItemViewType(int position) {
            LocalSong localSong = mLocalSongs.get(position);
            int downloadState = localSong.getDownloadState();

            if (downloadState == DownloadState.DOWNLOADED) {
                return TYPE_DEFAULT;
            } else {
                mPositionSparseArray.put(localSong.getMusicCode(), position);
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

            return new ViewHolder(view, mContext, mAllSongCheckBox);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LocalSong localSong = mLocalSongs.get(position);
            holder.bindLocalSong(localSong, mSongCheckBoxVisible);
        }

        @Override
        public int getItemCount() {
            return mLocalSongs.size();
        }

        void setDownloadState(Integer songPosition, int downloadState) {
            mLocalSongs.get(songPosition).setDownloadState(downloadState);
        }

        class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener,
                CompoundButton.OnCheckedChangeListener {

            private View mItemView;
            private TextView mMusicNameText;
            private TextView mSingerNameText;
            private ProgressBar mDownloadProgress;
            private CheckBox mSongCheckBox;
            private LocalSong mLocalSong;
            private Context mContext;
            private CheckBox mAllSongCheckBox;
            private View mSongCheckBoxContainer;

            ViewHolder(View itemView, Context context, CheckBox allSongCheckBox) {
                super(itemView);

                mItemView = itemView;
                mContext = context;
                mAllSongCheckBox = allSongCheckBox;

                mMusicNameText = itemView.findViewById(R.id.musicNameText);
                mSingerNameText = itemView.findViewById(R.id.singerNameText);
                mSongCheckBoxContainer = itemView.findViewById(R.id.songCheckBoxContainer);
                mSongCheckBox = itemView.findViewById(R.id.songCheckBox);
                mDownloadProgress = itemView.findViewById(R.id.downloadProgress);
            }

            void bindLocalSong(LocalSong localSong, boolean songCheckBoxVisible) {
                mLocalSong = localSong;
                mMusicNameText.setText(mLocalSong.getMusicName());
                mSingerNameText.setText(mLocalSong.getSingerName());
                mSongCheckBoxContainer.setVisibility(songCheckBoxVisible ? View.VISIBLE : View.GONE);
                mSongCheckBox.setChecked(mLocalSong.isSongChecked());

                if (mDownloadProgress != null) {
                    int downloadProgress = mLocalSong.getDownloadProgress();
                    mDownloadProgress.setProgress(downloadProgress);

                    if (mLocalSong.getDownloadState() == DownloadState.DOWNLOADED) {
                        mDownloadProgress.setVisibility(View.GONE);
                    }
                }

                mItemView.setOnClickListener(this);
                mSongCheckBox.setOnCheckedChangeListener(this);
                mSongCheckBoxContainer.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.itemView:
                        onItemViewClick();
                        break;
                    case R.id.songCheckBoxContainer:
                        onCheckBoxContainerClick();
                        break;
                    default:
                }
            }

            private void onCheckBoxContainerClick() {
                mSongCheckBox.setChecked(!mSongCheckBox.isChecked());
            }

            private void onItemViewClick() {
                // 看来应该尽量避免用数据库作为服务与活动（或类似关系）的沟通媒介！！！

//                int downloadState = mLocalSong.getDownloadState();
//
//                if (downloadState != DownloadState.DOWNLOADED) {
//                    DownloadMusicService downloadMusicService = mLocalSongActivity.getDownloadMusicService();
//
//                    Download download = new Download(mLocalSong.getMusicCode(),
//                            mLocalSong.getDownloadState(), mLocalSong.getDownloadUrl(),
//                            mLocalSong.getDownloadProgress());
//
//                    if (downloadState == DownloadState.DOWNLOADING
//                            || downloadState == DownloadState.WAITING) {
//                        if (downloadState == DownloadState.DOWNLOADING) {
//                            downloadMusicService.changeDownloadStates(mLocalSong.getMusicCode(), DownloadState.PAUSE);
//                        } else {
//                            downloadMusicService.updateDownloadStateAndStartServiceAsync(download,
//                                    DownloadState.PAUSE,
//                                    mLocalSongActivity.getContentResolver());
//                        }
//                    } else {
//                        mLocalSong.setDownloadState(DownloadState.WAITING);
//
//                        downloadMusicService.updateDownloadStateAndStartServiceAsync(download,
//                                DownloadState.WAITING,
//                                mLocalSongActivity.getContentResolver());
//                    }
//
//                    return;
//                }

                if (mLocalSong.getDownloadState() != DownloadState.DOWNLOADED) {
                    Toast.makeText(mContext, R.string.song_un_downloaded_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                Music music = new Music(mLocalSong.getMusicName(), mLocalSong.getMusicCode(),
                        mLocalSong.getPlayUrl(), mLocalSong.getMusicType(),
                        mLocalSong.getTotalSeconds(), mLocalSong.getSingerName(),
                        mLocalSong.getBigPictureUrl(), mLocalSong.getSmallPictureUrl());

                Intent service = MusicService.newPlayNewMusicIntent(mContext, music);
                mContext.startService(service);
            }

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mLocalSong.setSongChecked(b);
                if (!b && mAllSongCheckBox.isChecked()) {
                    mAllSongCheckBox.setTag(1);
                    mAllSongCheckBox.setChecked(false);
                }
            }
        }
    }
}
