package com.xhbb.qinzl.pleasantnote.data;

import android.content.Context;
import android.preference.PreferenceManager;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/26.
 */

public class PrefrencesUtils {

    public static void savePlaySpinnerSelection(Context context, int playSpinnerValue) {
        String key = context.getString(R.string.key_play_spinner_value);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(key, playSpinnerValue)
                .apply();
    }

    public static int getPlaySpinnerSelection(Context context) {
        String key = context.getString(R.string.key_play_spinner_value);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, 0);
    }
}
