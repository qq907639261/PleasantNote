package com.xhbb.qinzl.pleasantnote.common;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.evernote.android.job.JobManager;
import com.xhbb.qinzl.pleasantnote.async.MyJobCreator;

/**
 * Created by qinzl on 2017/7/4.
 */

public class MainSingleton {

    @SuppressLint("StaticFieldLeak")
    private static MainSingleton sInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;

    public static MainSingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MainSingleton(context);
        }
        return sInstance;
    }

    private MainSingleton(Context context) {
        mContext = context.getApplicationContext();
        JobManager.create(mContext).addJobCreator(new MyJobCreator());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }
}
