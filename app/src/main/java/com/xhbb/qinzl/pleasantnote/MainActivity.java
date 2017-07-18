package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener {

    private ActivityMainBinding mBinding;
    private ActivityMain mActivityMain;
    private boolean mBackPressedBeforeDestroy;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        MusicRankingAdapter pagerAdapter = new MusicRankingAdapter(fragmentManager);

        mActivityMain = new ActivityMain(this, pagerAdapter, this);

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                    .commit();
        } else {
            mActivityMain.setSearchViewCollapsed(true);
            if (fragmentManager.findFragmentById(R.id.fragment_container) != null) {
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
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment queryFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (queryFragment != null) {
            removeQueryFragment(fragmentManager, queryFragment);
            return;
        }

        mBackPressedBeforeDestroy = true;
        super.onBackPressed();
    }

    private void removeQueryFragment(FragmentManager fragmentManager, Fragment queryFragment) {
        fragmentManager.beginTransaction()
                .remove(queryFragment)
                .commit();

        mActivityMain.setViewPagerVisible(true);
        mActivityMain.setSearchViewCollapsed(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(MusicService.newIntent(this, MusicService.ACTION_STOP_FOREGROUND));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mBackPressedBeforeDestroy) {
            startService(MusicService.newIntent(this, MusicService.ACTION_START_FOREGROUND));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackPressedBeforeDestroy) {
            stopService(new Intent(this, MusicService.class));
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
        mActivityMain.setViewPagerVisible(false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MusicQueryFragment.newInstance(s))
                .commit();
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
            return MainFragment.newInstance(rankingCode);
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
