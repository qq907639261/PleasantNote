package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/6/29.
 */

public class ActivityMain implements LayoutAppBar.OnLayoutAppBarListener {

    private LayoutMain mLayoutMain;
    private OnActivityMainListener mListener;

    public ActivityMain(Context context, PagerAdapter pagerAdapter,
                        OnActivityMainListener listener) {
        mListener = listener;
        mLayoutMain = new LayoutMain(context, pagerAdapter, this);
    }

    public void setViewPagerVisible(boolean viewPagerVisible) {
        mLayoutMain.setViewPagerVisible(viewPagerVisible);
    }

    public void setSearchViewCollapsed(boolean searchViewCollapsed) {
        mLayoutMain.setSearchViewCollapsed(searchViewCollapsed);
    }

    public LayoutMain getLayoutMain() {
        return mLayoutMain;
    }

    public boolean onDrawerItemSelected(MenuItem item, DrawerLayout drawerLayout,
                                        NavigationView navigationView) {
        Context context = drawerLayout.getContext();
        switch (item.getItemId()) {
            case R.id.nav_my_favorited:
                Toast.makeText(context, "我的收藏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_recently_played:
                Toast.makeText(context, "最近播放", Toast.LENGTH_SHORT).show();
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
