package com.xhbb.qinzl.pleasantnote;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener {

    private ActivityMainBinding mBinding;
    private ActivityMain mActivityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MusicRankingAdapter pagerAdapter = new MusicRankingAdapter(getSupportFragmentManager());

        mActivityMain = new ActivityMain(this, pagerAdapter, this);
        mActivityMain.setSearchViewCollapsed(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                    .commit();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment != null) {
                mActivityMain.setViewPagerVisible(false);
            }
        }

        mBinding.setActivityMain(mActivityMain);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = mBinding.drawerLayout;
        NavigationView navigationView = mBinding.navigationView;

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                mActivityMain.setViewPagerVisible(true);
            }
        }
    }

    @Override
    public void onDrawerOpened(SearchView searchView) {
        clearFocus(searchView);
    }

    private void clearFocus(SearchView searchView) {
        if (searchView.hasFocus()) {
            searchView.clearFocus();
            searchView.setFocusable(false);
        }
    }

    @Override
    public void onQueryTextSubmit(SearchView searchView, String s) {
        clearFocus(searchView);

        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = MusicQueryFragment.class.getSimpleName();

        MusicQueryFragment queryFragment = (MusicQueryFragment)
                fragmentManager.findFragmentByTag(fragmentTag);

        if (queryFragment == null) {
            mActivityMain.setViewPagerVisible(false);

            MusicQueryFragment newFragment = MusicQueryFragment.newInstance(s);
            fragmentManager.beginTransaction().
                    add(R.id.fragment_container, newFragment, fragmentTag)
                    .addToBackStack(null)
                    .commit();
        } else {
            queryFragment.refreshData(s);
        }
    }

    private class MusicRankingAdapter extends FragmentStatePagerAdapter {

        int[] mRankingCodes = getResources().getIntArray(R.array.music_ranking_code);
        String[] mTabTitles = getResources().getStringArray(R.array.music_ranking);

        MusicRankingAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int rankingCode = mRankingCodes[position];
            return MusicRankingFragment.newInstance(rankingCode);
        }

        @Override
        public int getCount() {
            return mTabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }
}
