package com.xhbb.qinzl.pleasantnote.common;

/**
 * Created by qinzl on 2017/7/12.
 */

public interface Enums {

    interface RefreshState {

        int DEFAULT = 0;
        int SWIPE = 1;
        int SCROLL = 2;
    }

    interface VolleyState {

        int DEFAULT = 0;
        int RESPONSE = 1;
        int ERROR = 2;
    }
}
