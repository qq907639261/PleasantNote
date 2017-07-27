package com.xhbb.qinzl.pleasantnote.common;

import android.content.Context;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/27.
 */

public class DateTimeUtils {

    public static String getPlayDuration(Context context, int seconds) {
        int minutes = seconds / 60;
        int secondsOfMinute = seconds % 60;
        return context.getString(R.string.format_music_duration, minutes, secondsOfMinute);
    }
}
