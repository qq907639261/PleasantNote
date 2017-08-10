package com.xhbb.qinzl.pleasantnote;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.async.CleanUpHistoryMusicJob;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.MainSingleton;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener,
        Application.ActivityLifecycleCallbacks {

    private ActivityMainBinding mBinding;
    private ActivityMain mActivityMain;
    private Activity mStartedActivity;
    private boolean mPreviousActivityPausing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MainSingleton.getInstance(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        MusicRankingAdapter pagerAdapter = new MusicRankingAdapter(fragmentManager);

        mActivityMain = new ActivityMain(this, pagerAdapter, this);

        if (savedInstanceState == null) {
            addBottomPlayFragment();
            startService(MusicService.newIntent(this, MusicService.ACTION_INIT_MUSIC));
        } else {
            mActivityMain.setSearchViewCollapsed(true);
            if (fragmentManager.findFragmentById(R.id.fragment_container) != null) {
                mActivityMain.setViewPagerVisible(false);
            }
        }

        mBinding.setActivityMain(mActivityMain);
        CleanUpHistoryMusicJob.scheduleJob();
        getApplication().registerActivityLifecycleCallbacks(this);
    }

    private void addBottomPlayFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_local_song:
                LocalSongActivity.start(this);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopService(new Intent(this, MusicService.class));
        }
        getApplication().unregisterActivityLifecycleCallbacks(this);
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

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (!mPreviousActivityPausing) {
            startService(MusicService.newIntent(this, MusicService.ACTION_STOP_FOREGROUND));
        }
        mStartedActivity = activity;
        mPreviousActivityPausing = false;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
        mPreviousActivityPausing = true;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mPreviousActivityPausing = false;
        if (activity == mStartedActivity) {
            BottomPlayFragment fragment = (BottomPlayFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.bottom_fragment_container);

            if (fragment.isPlaying()) {
                startService(MusicService.newIntent(this, MusicService.ACTION_START_FOREGROUND));
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private class MusicRankingAdapter extends FragmentStatePagerAdapter {

        private int[] mRankingCodes;
        private String[] mTabTitles;

        private MusicRankingAdapter(FragmentManager fm) {
            super(fm);
            mRankingCodes = getResources().getIntArray(R.array.music_ranking_code);
            mTabTitles = getResources().getStringArray(R.array.music_ranking);
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
