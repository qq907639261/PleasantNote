package com.xhbb.qinzl.pleasantnote.data;

import android.content.Context;
import android.preference.PreferenceManager;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/7/26.
 */

public class PrefrencesUtils {

    public static void saveSwitchModeSpinnerSelection(Context context, int switchModeSpinnerValue) {
        String key = context.getString(R.string.key_switch_mode_spinner_value);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(key, switchModeSpinnerValue)
                .apply();
    }

    public static int getSwitchModeSpinnerSelection(Context context) {
        String key = context.getString(R.string.key_switch_mode_spinner_value);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, 0);
    }
}
