package com.xhbb.qinzl.pleasantnote.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.xhbb.qinzl.pleasantnote.BuildConfig;

/**
 * Created by qinzl on 2017/7/4.
 */

public interface Contracts {

    String AUTHORITY = BuildConfig.APPLICATION_ID;
    String CONTENT_AUTHORITY = "content://" + AUTHORITY + "/";

    String ACTION_UPDATE_QUERY_DATA_FINISHED = AUTHORITY + ".ACTION_UPDATE_QUERY_DATA_FINISHED";
    String ACTION_MUSIC_PLAYED = AUTHORITY + ".ACTION_MUSIC_PLAYED";
    String ACTION_MUSIC_STOPPED = AUTHORITY + ".ACTION_MUSIC_STOPPED";

    String EXTRA_MUSIC = AUTHORITY + ".EXTRA_MUSIC";

    interface MusicContract extends BaseColumns {

        String TABLE = "music";
        Uri URI = Uri.parse(CONTENT_AUTHORITY + TABLE);

        String _NAME = "name";
        String _CODE = "code";
        String _TYPE = "type";
        String _PLAY_URL = "play_url";
        String _TOTAL_SECONDS = "total_seconds";
        String _SINGER = "singer";
        String _SMALL_PICTURE_URL = "small_picture_url";
        String _BIG_PICTURE_URL = "big_picture_url";
        String _RANKING_CODE = "ranking_code";
    }

    interface DownloadContract extends BaseColumns {

        String TABLE = "download";
        Uri URI = Uri.parse(CONTENT_AUTHORITY + TABLE);

        String _MUSIC_CODE = "music_code";
        String _STATE = "state";
        String _URL = "url";
        String _PROGRESS = "progress";
    }
}
