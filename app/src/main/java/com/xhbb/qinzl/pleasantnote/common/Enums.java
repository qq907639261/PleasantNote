package com.xhbb.qinzl.pleasantnote.common;

/**
 * Created by qinzl on 2017/7/12.
 */

public interface Enums {

    interface RefreshState {

        int SWIPE = 1;
        int SCROLL = 2;
    }

    interface VolleyState {

        int RESPONSE = 1;
        int ERROR = 2;
    }

    interface DataUpdatedState {

        int EMPTY_DATA_NO_UPDATE = 1;
        int SCROLLED_TO_END_UPDATE = 2;
        int SCROLLED_TO_END_NO_UPDATE = 3;
    }

    interface MusicType {

        int RANKING = 1;
        int QUERY = 2;
        int HISTORY = 3;
        int FAVORITED = 4;
        int LOCAL = 5;
    }

    interface DownloadState {

        int PAUSE = 1;
        int CANCEL = 2;
        int FAILED = 3;
        int WAITING = 4;
        int PREPARING = 5;
        int DOWNLOADING = 6;
        int DOWNLOADED = 7;
    }
}
