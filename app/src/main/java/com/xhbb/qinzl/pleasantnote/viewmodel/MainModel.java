package com.xhbb.qinzl.pleasantnote.viewmodel;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.xhbb.qinzl.pleasantnote.R;

/**
 * Created by qinzl on 2017/6/29.
 */

public class MainModel {

    private OnMainModelListener mListener;

    public MainModel(OnMainModelListener listener) {
        mListener = listener;
    }

    public boolean onNavigationItemSelected(MenuItem item, DrawerLayout drawerLayout,
                                            NavigationView navigationView) {
        int rankingId;
        switch (item.getItemId()) {
            case R.id.menu_western_countries:
                rankingId = 3;
                break;
            case R.id.menu_mainland:
                rankingId = 5;
                break;
            case R.id.menu_hong_kong_and_taiwan:
                rankingId = 6;
                break;
            case R.id.menu_korea:
                rankingId = 16;
                break;
            case R.id.menu_japan:
                rankingId = 17;
                break;
            case R.id.menu_ballad:
                rankingId = 18;
                break;
            case R.id.menu_rocking:
                rankingId = 19;
                break;
            case R.id.menu_sales:
                rankingId = 23;
                break;
            default:
                rankingId = 26;
        }
        mListener.onNavigationItemSelected(rankingId);

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    public interface OnMainModelListener {

        void onNavigationItemSelected(int rankingId);
    }
}
