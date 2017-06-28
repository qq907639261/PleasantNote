package com.xhbb.qinzl.pleasantnote.custom;

import android.content.Context;
import android.content.ContextWrapper;
import android.databinding.BindingAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by qinzl on 2017/6/28.
 */

public class ViewAttributes {

    @BindingAdapter({"android:actionBarSetted"})
    public static void setActionBar(Toolbar toolbar, boolean actionBarSetted) {
        if (actionBarSetted) {
            Context context = toolbar.getContext();
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            }

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.setSupportActionBar(toolbar);
            }
        }
    }
}
