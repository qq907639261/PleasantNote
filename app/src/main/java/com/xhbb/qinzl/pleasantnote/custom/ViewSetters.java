package com.xhbb.qinzl.pleasantnote.custom;

import android.content.Context;
import android.content.ContextWrapper;
import android.databinding.BindingAdapter;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/6/28.
 */

public class ViewSetters {

    @BindingAdapter(value = {"android:actionBarSetted", "android:drawerLayout"}, requireAll = false)
    public static void setToolbar(Toolbar toolbar, boolean actionBarSetted, DrawerLayout drawerLayout) {
        if (!actionBarSetted) {
            return;
        }

        Context context = toolbar.getContext();
        if (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        @SuppressWarnings("ConstantConditions")
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.setSupportActionBar(toolbar);

        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar,
                    R.string.open_drawer_accessibility, R.string.close_drawer_accessibility);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    @BindingAdapter({"android:onItemSelected"})
    public static void setOnNavigationViewListener(
            NavigationView navigationView,
            NavigationView.OnNavigationItemSelectedListener listener) {
        navigationView.setNavigationItemSelectedListener(listener);
    }
}
