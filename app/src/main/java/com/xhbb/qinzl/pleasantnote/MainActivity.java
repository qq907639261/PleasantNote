package com.xhbb.qinzl.pleasantnote;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

public class MainActivity extends AppCompatActivity
        implements ActivityMain.OnActivityMainListener {

    private ActivityMainBinding mBinding;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MainFragment.newInstance())
                    .replace(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                    .commit();
        }

        mBinding.setActivityMain(new ActivityMain(this, this));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switch (intent.getAction()) {
            case Intent.ACTION_SEARCH:
                String query = intent.getStringExtra(SearchManager.QUERY);
                getMainFragment().autoRefreshData(query);
                invalidateOptionsMenu();
                break;
            default:
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_my_favorited:
                Toast.makeText(this, "menu_my_favorited", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_recently_played:
                Toast.makeText(this, "menu_recently_played", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_local_song:
                Toast.makeText(this, "menu_local_song", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDrawerItemSelected(int rankingId) {
        getMainFragment().autoRefreshData(rankingId);
    }

    private MainFragment getMainFragment() {
        return (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    public void onDrawerOpened() {
        if (mSearchView.hasFocus()) {
            mSearchView.clearFocus();
        }
    }
}
