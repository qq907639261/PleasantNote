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
import com.xhbb.qinzl.pleasantnote.model.Music;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener,
        MainFragment.OnMainFragmentListener {

    private ActivityMainBinding mBinding;
    private ActivityMain mActivityMain;

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
                    .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance(null))
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
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commit();

            mActivityMain.setViewPagerVisible(true);
            mActivityMain.setSearchViewCollapsed(true);

            return;
        }

        stopService(new Intent(this, MusicService.class));
        super.onBackPressed();
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
    public void onClickItem(Music music) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_fragment_container, BottomPlayFragment.newInstance(music))
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
