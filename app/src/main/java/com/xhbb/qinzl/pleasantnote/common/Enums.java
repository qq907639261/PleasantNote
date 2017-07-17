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

    interface MusicDataUpdatedState {

        int EMPTY_DATA = 1;
        int SCROLLED_TO_END_UPDATE = 2;
        int SCROLLED_TO_END_NO_UPDATE = 3;
    }
}
