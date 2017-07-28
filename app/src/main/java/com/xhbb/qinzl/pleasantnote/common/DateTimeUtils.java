package com.xhbb.qinzl.pleasantnote.common;

import android.content.Context;

import com.xhbb.qinzl.pleasantnote.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by qinzl on 2017/7/27.
 */

public class DateTimeUtils {

    public static String getFormattedTime(Context context, int seconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);

        long hours = minutes / 60;
        long minutesOfHour = minutes % 60;
        int secondsOfMinute = seconds % 60;

        String formattedTime = context.getString(R.string.format_time, minutesOfHour, secondsOfMinute);
        if (hours > 0) {
            formattedTime = hours + ":" + formattedTime;
        }

        return formattedTime;
    }
}
