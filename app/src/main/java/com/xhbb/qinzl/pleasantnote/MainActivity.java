package com.xhbb.qinzl.pleasantnote;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.Toast;

import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.viewmodel.MainModel;

public class MainActivity extends AppCompatActivity
        implements MainModel.OnMainModelListener {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        MainModel mainModel = new MainModel(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bottom_fragment_container, BottomPlayFragment.newInstance())
                    .commit();
        }

        mBinding.setMainModel(mainModel);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switch (intent.getAction()) {
            case Intent.ACTION_SEARCH:
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchMusic(query);
                invalidateOptionsMenu();
                break;
            default:
        }
    }

    private void searchMusic(String query) {
        Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
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
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationItemSelected(int rankingId) {
        searchMusic(String.valueOf(rankingId));
    }
}
