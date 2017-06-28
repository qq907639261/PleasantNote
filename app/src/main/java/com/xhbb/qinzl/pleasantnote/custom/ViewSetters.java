package com.xhbb.qinzl.pleasantnote.custom;

import android.content.Context;
import android.content.ContextWrapper;
import android.databinding.BindingAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by qinzl on 2017/6/28.
 */

public class ViewSetters {

    @BindingAdapter({"android:actionBarSetted"})
    public static void setToolbar(Toolbar toolbar, boolean actionBarSetted) {
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
