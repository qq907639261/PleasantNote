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

    interface MusicContract extends BaseColumns {

        String TABLE = "music";
        Uri URI = Uri.parse(CONTENT_AUTHORITY + TABLE);

        String _NAME = "name";
        String _SECONDS = "seconds";
        String _CODE = "code";
        String _SINGER_NAME = "singer_name";
        String _BIG_PICTURE_URL = "big_picture_url";
        String _SMALL_PICTURE_URL = "small_picture_url";
        String _PLAY_URL = "play_url";
        String _RANKING_CODE = "ranking_code";
        String _TYPE = "type";
    }
}
