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

    String ACTION_RESET = AUTHORITY + ".ACTION_RESET";
    String ACTION_PLAY_PAUSE = AUTHORITY + ".ACTION_PLAY_PAUSE";

    interface MusicContract extends BaseColumns {

        String TABLE = "music";
        Uri URI = Uri.parse(CONTENT_AUTHORITY + TABLE);

        String _NAME = "name";
        String _SECONDS = "seconds";
        String _CODE = "code";
        String _SINGER = "singer";
        String _SINGER_CODE = "singer_code";
        String _BIG_PICTURE = "big_picture";
        String _SMALL_PICTURE = "small_picture";
        String _PLAY_URL = "play_url";
        String _DOWNLOAD_URL = "download_url";
        String _RANKING_CODE = "ranking_code";
        String _QUERY = "query";
    }
}
