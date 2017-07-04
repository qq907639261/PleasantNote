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

    interface MusicContract extends BaseColumns {

        String TABLE_NAME = "music";
        Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY + TABLE_NAME);

        String _NAME = "name";
        String _SECONDS = "seconds";
        String _CODE = "code";
        String _SINGER_NAME = "singer_name";
        String _SINGER_CODE = "singer_code";
        String _BIG_PICTURE = "big_picture";
        String _SMALL_PICTURE = "small_picture";
        String _PLAY_URL = "play_url";
        String _DOWNLOAD_URL = "download_url";
    }
}
