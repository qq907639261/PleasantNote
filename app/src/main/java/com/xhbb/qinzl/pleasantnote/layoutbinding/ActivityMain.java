package com.xhbb.qinzl.pleasantnote.layoutbinding;

import android.content.Context;
import android.content.res.Resources;
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

    public boolean onDrawerItemSelected(MenuItem item, DrawerLayout drawerLayout,
                                        NavigationView navigationView) {
        Resources resources = drawerLayout.getResources();
        int rankingId;
        switch (item.getItemId()) {
            case R.id.nav_western_countries:
                rankingId = resources.getInteger(R.integer.ranking_id_western_countries);
                break;
            case R.id.nav_mainland:
                rankingId = resources.getInteger(R.integer.ranking_id_mainland);
                break;
            case R.id.nav_hong_kong_and_taiwan:
                rankingId = resources.getInteger(R.integer.ranking_id_hong_kong_and_taiwan);
                break;
            case R.id.nav_korea:
                rankingId = resources.getInteger(R.integer.ranking_id_korea);
                break;
            case R.id.nav_japan:
                rankingId = resources.getInteger(R.integer.ranking_id_japan);
                break;
            case R.id.nav_ballad:
                rankingId = resources.getInteger(R.integer.ranking_id_ballad);
                break;
            case R.id.nav_rocking:
                rankingId = resources.getInteger(R.integer.ranking_id_rocking);
                break;
            case R.id.nav_sales:
                rankingId = resources.getInteger(R.integer.ranking_id_sales);
                break;
            default:
                rankingId = resources.getInteger(R.integer.ranking_id_default);
        }
        mListener.onDrawerItemSelected(rankingId);

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onDrawerOpened() {
        mListener.onDrawerOpened();
    }

    public interface OnActivityMainListener {

        void onDrawerItemSelected(int rankingId);
        void onDrawerOpened();
    }
}
