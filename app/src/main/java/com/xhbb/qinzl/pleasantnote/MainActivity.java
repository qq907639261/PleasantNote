package com.xhbb.qinzl.pleasantnote;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xhbb.qinzl.pleasantnote.async.CleanUpHistoryMusicJob;
import com.xhbb.qinzl.pleasantnote.async.MusicService;
import com.xhbb.qinzl.pleasantnote.common.MainSingleton;
import com.xhbb.qinzl.pleasantnote.data.Contracts;
import com.xhbb.qinzl.pleasantnote.databinding.ActivityMainBinding;
import com.xhbb.qinzl.pleasantnote.layoutbinding.ActivityMain;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        ActivityMain.OnActivityMainListener,
        Application.ActivityLifecycleCallbacks,
        View.OnClickListener,
        AMapLocationListener,
        SelectPhotoDialogFragment.OnSelectPhotoDialogFragmentListener {

    private static final String FRAGMENT_TAG_SELECT_PHOTO = "SELECT_PHOTO";
    private static final int REQUEST_IMAGE_CAPTURE = 0;

    private ActivityMainBinding mBinding;
    private ActivityMain mActivityMain;
    private Activity mStartedActivity;
    private boolean mPreviousActivityPausing;
    private TextView mCurrentLocationText;
    private ImageView mMyIconImage;
    private AMapLocationClient mAMapLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MainSingleton.getInstance(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

        FragmentManager fragmentManager = getSupportFragmentManager();
        MusicRankingAdapter pagerAdapter = new MusicRankingAdapter(fragmentManager, getResources());
        View navHeaderView = navigationView.getHeaderView(0);

        mCurrentLocationText = navHeaderView.findViewById(R.id.currentLocationText);
        mMyIconImage = navHeaderView.findViewById(R.id.myIconImage);
        mActivityMain = new ActivityMain(this, pagerAdapter, this);
        mAMapLocationClient = new AMapLocationClient(this);

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
        mMyIconImage.setOnClickListener(this);
        setMyIconImageIfExists();

        mAMapLocationClient.setLocationListener(this);
        mAMapLocationClient.setLocationOption(new AMapLocationClientOption()
                .setOnceLocation(true));
    }

    private void setMyIconImageIfExists() {
        File myIconImage = getMyIconPicture();
        if (myIconImage.exists()) {
            Uri imageURI = Uri.parse(myIconImage.getPath());
            mMyIconImage.setImageURI(imageURI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mAMapLocationClient.startLocation();
        } else {
            Toast.makeText(this, R.string.location_permission_denied_toast, Toast.LENGTH_SHORT).show();
        }
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
    protected void onStart() {
        super.onStart();
        mAMapLocationClient.startLocation();
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

        mAMapLocationClient.unRegisterLocationListener(this);
        mAMapLocationClient.onDestroy();
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

    @Override
    public void onClick(View view) {
        onMyIconImageClick();
    }

    private void onMyIconImageClick() {
        DialogFragment fragment = SelectPhotoDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), FRAGMENT_TAG_SELECT_PHOTO);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation == null) {
            return;
        }

        if (aMapLocation.getErrorCode() != 0) {
            if (aMapLocation.getErrorCode() == 12) {
                checkAndRequestLocationPermission();
            }
            return;
        }

        // 在模拟器上定位不成功，但在真机上测试有效
        String currentLocation = aMapLocation.getCountry() + " " +
                aMapLocation.getProvince() + " " +
                aMapLocation.getCity();
        mCurrentLocationText.setText(currentLocation);
    }

    private void checkAndRequestLocationPermission() {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ContextCompat.checkSelfPermission(this, locationPermission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{locationPermission}, 0);
        }
    }

    @Override
    public void onTakePictureButtonClick() {
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, R.string.your_camera_unavailable_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            File myIconPicture = getMyIconPicture();
            String authority = Contracts.AUTHORITY + ".fileprovider";

            Uri imageUri = FileProvider.getUriForFile(this, authority, myIconPicture);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                Bitmap imageBitmap = BitmapFactory.decodeFile(getMyIconPicture().getAbsolutePath());
                mMyIconImage.setImageBitmap(imageBitmap);
                break;
            default:
        }
    }

    @NonNull
    private File getMyIconPicture() {
        File parent = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String child = getString(R.string.default_my_icon_file_name);

        return new File(parent, child);
    }

    @Override
    public void onSelectImageButtonClick() {
        // TODO: 2017/8/16
    }

    private class MusicRankingAdapter extends FragmentStatePagerAdapter {

        private int[] mRankingCodes;
        private String[] mTabTitles;

        private MusicRankingAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            mRankingCodes = resources.getIntArray(R.array.music_ranking_code);
            mTabTitles = resources.getStringArray(R.array.music_ranking);
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
