package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.content.ContextWrapper;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.xhbb.qinzl.pleasantnote.R;
import com.xhbb.qinzl.pleasantnote.common.GlideApp;

/**
 * Created by qinzl on 2017/6/28.
 */

public class ViewSetters {

    @BindingAdapter(value = {"android:actionBarSetted", "android:drawerLayout",
            "android:onDrawerOpened"}, requireAll = false)
    public static void setToolbar(Toolbar toolbar, boolean actionBarSetted,
                                  DrawerLayout drawerLayout,
                                  final OnDrawerOpenedListener onDrawerOpenedListener) {
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
                    R.string.open_drawer_accessibility, R.string.close_drawer_accessibility) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    if (onDrawerOpenedListener != null) {
                        onDrawerOpenedListener.onDrawerOpened();
                    }
                }
            };

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

    @BindingAdapter(value = {"android:url", "android:placeholder"}, requireAll = false)
    public static void loadImage(ImageView imageView, String url, Drawable placeholder) {
        Context context = imageView.getContext();
        if (placeholder == null) {
            placeholder = ActivityCompat.getDrawable(context, R.drawable.empty_image);
        }

        GlideApp.with(context)
                .load(url)
                .placeholder(placeholder)
                .into(imageView);
    }

    public interface OnDrawerOpenedListener {

        void onDrawerOpened();
    }
}
