package com.xhbb.qinzl.pleasantnote.common;

/**
 * Created by qinzl on 2017/7/5.
 */

public interface Enums {

    interface VolleyState {

        int NOTHING = 0;
        int ERROR = 1;
        int RESPONSE = 2;
    }

    interface RefreshState {

        int NOTHING = 0;
        int AUTO = 1;
        int SWIPE = 2;
        int SCROLL = 3;
    }
}
