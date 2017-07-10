package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/6/29.
 */

public class ActivityMain implements LayoutAppBar.OnLayoutAppBarListener {

    private LayoutMain mLayoutMain;
    private OnActivityMainListener mListener;
    private boolean mFragmentContainerEmptied;

    public ActivityMain(Context context, PagerAdapter pagerAdapter,
                        OnActivityMainListener listener) {
        mListener = listener;
        mFragmentContainerEmptied = true;

        mLayoutMain = new LayoutMain(context, pagerAdapter, this);
    }

    public void setFragmentContainerEmptied(boolean fragmentContainerEmptied) {
        if (mFragmentContainerEmptied != fragmentContainerEmptied) {
            mFragmentContainerEmptied = fragmentContainerEmptied;
            mLayoutMain.setViewPagerVisible(mFragmentContainerEmptied);
        }
    }

    public LayoutMain getLayoutMain() {
        return mLayoutMain;
    }

    public boolean onDrawerItemSelected(MenuItem item, DrawerLayout drawerLayout,
                                        NavigationView navigationView) {
        switch (item.getItemId()) {
            case R.id.nav_local_song:

                break;
            case R.id.nav_my_favorited:

                break;
            case R.id.nav_recently_played:

                break;
            default:
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onDrawerOpened(SearchView searchView) {
        mListener.onDrawerOpened(searchView);
    }

    @Override
    public void onQueryTextSubmit(SearchView searchView, String s) {
        mListener.onQueryTextSubmit(searchView, s);
    }

    public interface OnActivityMainListener {

        void onDrawerOpened(SearchView searchView);
        void onQueryTextSubmit(SearchView searchView, String s);
    }
}
