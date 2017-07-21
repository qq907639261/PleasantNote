package com.xhbb.qinzl.pleasantnote.server;

import android.content.Context;
import android.net.Uri;

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

    public static final int MAX_COUNT_OF_EACH_PAGE = 60;

    public static void addRankingRequest(Context context, int rankingCode, Object requestTag,
                                         Response.Listener<String> listener,
                                         Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/top")
                .buildUpon()
                .appendQueryParameter("topid", String.valueOf(rankingCode))
                .build().toString();

        addRequest(context, url, requestTag, listener, errorListener);
    }

    public static void addQueryRequest(Context context, String query, int currentPage,
                                       Object requestTag,
                                       Response.Listener<String> listener,
                                       Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/search")
                .buildUpon()
                .appendQueryParameter("keyword", query)
                .appendQueryParameter("page", String.valueOf(currentPage))
                .build().toString();

        addRequest(context, url, requestTag, listener, errorListener);
    }

    public static void addLyricsRequest(Context context, int musicCode, Object requestTag,
                                        Response.Listener<String> listener,
                                        Response.ErrorListener errorListener) {
        @SuppressWarnings("SpellCheckingInspection")
        String url = Uri.parse("http://ali-qqmusic.showapi.com/song-word")
                .buildUpon()
                .appendQueryParameter("musicid", String.valueOf(musicCode))
                .build().toString();

        addRequest(context, url, requestTag, listener, errorListener);
    }

    public static void cancelRequests(Context context, Object requestsTag) {
        MainSingleton.getInstance(context)
                .getRequestQueue()
                .cancelAll(requestsTag);
    }

    private static void addRequest(Context context, String url, Object requestTag,
                                   Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
        final String appCode = context.getString(R.string.ali_app_code);
        StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "APPCODE " + appCode);
                return headers;
            }
        };
        request.setTag(requestTag);

        RequestQueue queue = MainSingleton.getInstance(context).getRequestQueue();
        queue.add(request);
    }
}
