package com.xhbb.qinzl.pleasantnote;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;

import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ActivityMain activityMain = new ActivityMain(this, this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                    .commit();
        }

        // TODO: 2017/7/8 ViewPagerStateAdapter 

        mBinding.setActivityMain(activityMain);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = mBinding.drawerLayout;
        NavigationView navigationView = mBinding.navigationView;

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
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

        // TODO: 2017/7/8 需要完善多次连续查询的情况
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MusicQueryFragment.newInstance(s))
                .addToBackStack(null)
                .commit();

        // TODO: 2017/7/8 隐藏Toolbar，可能还要隐藏ViewPager
    }
}
