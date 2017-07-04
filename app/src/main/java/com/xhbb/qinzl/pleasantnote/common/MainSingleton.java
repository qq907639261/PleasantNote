package com.xhbb.qinzl.pleasantnote.common;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by qinzl on 2017/7/4.
 */

public class MainSingleton {

    @SuppressLint("StaticFieldLeak")
    private static MainSingleton sMainSingleton;
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private RequestQueue mRequestQueue;

    public static MainSingleton getInstance(Context context) {
        if (sMainSingleton == null) {
            sMainSingleton = new MainSingleton(context);
        }
        return sMainSingleton;
    }

    private MainSingleton(Context context) {
        sContext = context.getApplicationContext();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(sContext);
        }
        return mRequestQueue;
    }
}
