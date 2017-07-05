package com.xhbb.qinzl.pleasantnote.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    public static void addRankingRequest(Context context, int rankingId,
                                         Response.Listener<String> listener,
                                         Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/top")
                .buildUpon()
                .appendQueryParameter("topid", String.valueOf(rankingId))
                .build().toString();

        addRequest(context, url, listener, errorListener);
    }

    public static void addQueryRequest(Context context, String query, int pages,
                                       Response.Listener<String> listener,
                                       Response.ErrorListener errorListener) {
        String url = Uri.parse("http://ali-qqmusic.showapi.com/search")
                .buildUpon()
                .appendQueryParameter("keyword", query)
                .appendQueryParameter("page", String.valueOf(pages))
                .build().toString();

        addRequest(context, url, listener, errorListener);
    }

    private static void addRequest(final Context context, String url, Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "APPCODE " + context.getString(R.string.ali_app_code));
                return headers;
            }
        };

        RequestQueue queue = MainSingleton.getInstance(context).getRequestQueue();
        queue.add(request);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }
}
