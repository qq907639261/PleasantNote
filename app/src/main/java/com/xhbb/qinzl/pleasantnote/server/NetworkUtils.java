package com.xhbb.qinzl.pleasantnote.server;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.xhbb.qinzl.pleasantnote.R;
import com.xhbb.qinzl.pleasantnote.common.MainSingleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qinzl on 2017/7/4.
 */

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static void addRankingRequest(Context context, int rankingCode, @Nullable Object tag,
                                         Response.Listener<String> listener,
                                         Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/top")
                .buildUpon()
                .appendQueryParameter("topid", String.valueOf(rankingCode))
                .build().toString();

        addRequest(context, url, tag, listener, errorListener);
    }

    public static void addQueryRequest(Context context, String query, int pages,
                                       Response.Listener<String> listener,
                                       Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/search")
                .buildUpon()
                .appendQueryParameter("keyword", query)
                .appendQueryParameter("page", String.valueOf(pages))
                .build().toString();

        addRequest(context, url, null, listener, errorListener);
    }

    public static void cancelAllRequest(Context context, @Nullable Object tag) {
        MainSingleton.getInstance(context).getRequestQueue().cancelAll(tag == null ? TAG : tag);
    }

    private static void addRequest(final Context context, String url, @Nullable Object tag,
                                   Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "APPCODE " + context.getString(R.string.ali_app_code));
                return headers;
            }
        };
        request.setTag(tag == null ? TAG : tag);

        RequestQueue queue = MainSingleton.getInstance(context).getRequestQueue();
        queue.add(request);
    }
}
