package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/6/29.
 */

public class ActivityMain implements ActivityFragment.OnActivityFragmentListener {

    private OnActivityMainListener mListener;
    private ActivityFragment mActivityFragment;

    public ActivityMain(Context context, OnActivityMainListener listener) {
        mListener = listener;

        float bottomFragmentHeight = context.getResources().getDimension(R.dimen.bottomPlayFragmentHeight);
        mActivityFragment = new ActivityFragment(bottomFragmentHeight, this);
    }

    public ActivityFragment getActivityFragment() {
        return mActivityFragment;
    }

    public boolean onItemSelected(MenuItem item, DrawerLayout drawerLayout,
                                  NavigationView navigationView) {
        int rankingId;
        switch (item.getItemId()) {
            case R.id.nav_western_countries:
                rankingId = 3;
                break;
            case R.id.nav_mainland:
                rankingId = 5;
                break;
            case R.id.nav_hong_kong_and_taiwan:
                rankingId = 6;
                break;
            case R.id.nav_korea:
                rankingId = 16;
                break;
            case R.id.nav_japan:
                rankingId = 17;
                break;
            case R.id.nav_ballad:
                rankingId = 18;
                break;
            case R.id.nav_rocking:
                rankingId = 19;
                break;
            case R.id.nav_sales:
                rankingId = 23;
                break;
            default:
                rankingId = 26;
        }
        mListener.onNavigationItemSelected(rankingId);

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onDrawerOpened() {
        mListener.onDrawerOpened();
    }

    public interface OnActivityMainListener {

        void onNavigationItemSelected(int rankingId);
        void onDrawerOpened();
    }
}
