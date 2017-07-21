package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.R;
import com.xhbb.qinzl.pleasantnote.common.GlideApp;

/**
 * Created by qinzl on 2017/6/28.
 */

public class ViewSetters {

    @BindingAdapter(value = {"android:context", "android:displayHomeAsUpEnabled",
            "android:drawerLayout", "android:onDrawerOpened"}, requireAll = false)
    public static void setToolbar(Toolbar toolbar, Context context,
                                  boolean displayHomeAsUpEnabled, DrawerLayout drawerLayout,
                                  OnDrawerOpenedListener onDrawerOpenedListener) {
        if (context == null || !(context instanceof AppCompatActivity)) {
            return;
        }
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);

        setDrawerLayout(drawerLayout, toolbar, activity, onDrawerOpenedListener);
    }

    private static void setDrawerLayout(DrawerLayout drawerLayout, Toolbar toolbar,
                                        AppCompatActivity activity,
                                        final OnDrawerOpenedListener onDrawerOpenedListener) {
        if (drawerLayout == null) {
            return;
        }
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

    @BindingAdapter(value = {"android:url", "android:errorRes"}, requireAll = false)
    public static void setImageView(ImageView imageView, String url, Integer errorRes) {
        Context context = imageView.getContext();
        if (errorRes == null) {
            errorRes = R.drawable.empty_image;
        }
        GlideApp.with(context)
                .load(url)
                .error(errorRes)
                .into(imageView);
    }

    @BindingAdapter({"android:onItemSelected"})
    public static void setNavigationView(
            NavigationView navigationView,
            NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener) {
        if (onNavigationItemSelectedListener != null) {
            navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
        }
    }

    @BindingAdapter({"android:onRefresh"})
    public static void setSwipeRefreshLayout(
            SwipeRefreshLayout swipeRefreshLayout,
            SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        if (onRefreshListener != null) {
            swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        }
    }

    @BindingAdapter({"android:collapsed"})
    public static void setSearchView(SearchView searchView, boolean collapsed) {
        if (collapsed) {
            searchView.onActionViewCollapsed();
        }
    }

    @BindingAdapter({"android:onQueryTextSubmit"})
    public static void setSearchView(SearchView searchView,
                                     final OnQueryTextSubmitListener onQueryTextSubmitListener) {
        if (onQueryTextSubmitListener == null) {
            return;
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                onQueryTextSubmitListener.onQueryTextSubmit(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @BindingAdapter({"android:onScrollStateChanged"})
    public static void setRecyclerView(RecyclerView recyclerView,
                                       final OnScrollStateChangedListener onScrollStateChangedListener) {
        if (onScrollStateChangedListener == null) {
            return;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                onScrollStateChangedListener.onScrollStateChanged(newState);
            }
        });
    }

    public interface OnDrawerOpenedListener {

        void onDrawerOpened();
    }

    public interface OnQueryTextSubmitListener {

        void onQueryTextSubmit(String s);
    }

    public interface OnScrollStateChangedListener {

        void onScrollStateChanged(int newState);
    }
}
