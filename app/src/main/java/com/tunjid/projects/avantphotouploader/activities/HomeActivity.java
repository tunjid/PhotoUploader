package com.tunjid.projects.avantphotouploader.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreActivity;
import com.tunjid.projects.avantphotouploader.fragments.NavigationDrawerFragment;
import com.tunjid.projects.avantphotouploader.fragments.UploadFileFragment;
import com.tunjid.projects.avantphotouploader.fragments.UploadedFilesFragment;
import com.tunjid.projects.avantphotouploader.fragments.ViewFileFragment;
import com.tunjid.projects.avantphotouploader.helpers.FloatingActionButton;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.services.AvantApi;

import java.util.ArrayList;

public class HomeActivity extends CoreActivity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int UPLOAD_FILE = 0;
    private static final int UPLOADED_FILES = 1;


    public static final String UPLOAD_FILE_TAG = "UPLOAD_FILE_TAG";
    public static final String UPLOADED_FILES_TAG = "UPLOADED_FILES_TAG";
    public static final String VIEW_FILE_TAG = "VIEW_FILE_TAG";

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        api = ((AvantApi.LocalBinder) service).getService();
    }

    //  This activity's has retainTaskState to true,
    // so any new intent, e.g from a notification, will fire this method
    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        setIntent(newIntent);

        String flag = newIntent.getStringExtra(Utils.GENERIC_FLAG);

        if (flag != null && flag.equals(UPLOADED_FILES_TAG)) {
            onNavigationDrawerItemSelected(UPLOADED_FILES);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViewComponents();

        if (isSavedInstance) {
            currentFragment = savedInstanceState.getString(Utils.CURRENT_FRAGMENT);
            assert currentFragment != null;

            switch (currentFragment) {
                case UPLOAD_FILE_TAG:
                    UploadFileFragment uploadFileFragment = (UploadFileFragment)
                            getSupportFragmentManager().getFragment(savedInstanceState, UPLOAD_FILE_TAG);

                    slideUpTransaction()
                            .replace(R.id.container, uploadFileFragment, UPLOAD_FILE_TAG)
                            .commit();
                    break;
                case UPLOADED_FILES_TAG:
                    UploadedFilesFragment uploadedFilesFragment = (UploadedFilesFragment)
                            getSupportFragmentManager().getFragment(savedInstanceState, UPLOADED_FILES_TAG);

                    slideUpTransaction()
                            .replace(R.id.container, uploadedFilesFragment, UPLOADED_FILES_TAG)
                            .commit();
                    break;
                case VIEW_FILE_TAG:
                    ViewFileFragment viewFileFragment = (ViewFileFragment)
                            getSupportFragmentManager().getFragment(savedInstanceState, VIEW_FILE_TAG);

                    slideUpTransaction()
                            .replace(R.id.container, viewFileFragment, VIEW_FILE_TAG)
                            .commit();
                    break;
            }
        }

        // Bind to the API service
        Intent APIintent = new Intent(this, AvantApi.class);
        bindService(APIintent, this, BIND_AUTO_CREATE);

        // Get phone screen details
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        SharedPreferences sharedPreferences
                = getSharedPreferences(Utils.PREFS, MODE_PRIVATE);

        sharedPreferences.edit()
                .putInt(Utils.SCREEN_HEIGHT, metrics.heightPixels)
                .putInt(Utils.SCREEN_WIDTH, metrics.widthPixels)
                .putFloat(Utils.SCREEN_DPI, metrics.density)
                .putFloat(Utils.SCREEN_Y_DPI, metrics.ydpi)
                .apply();

        // Determine whether the current user is valid
        ParseUser parseUser = ParseUser.getCurrentUser();

        if (parseUser == null || ParseAnonymousUtils.isLinked(parseUser)) {
            // Send the user to LoginActivity.class
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Initializes all view components in this activity.
     */
    private void initializeViewComponents() {

        fab = (FloatingActionButton) findViewById(R.id.fab);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mainContent = (RelativeLayout) findViewById(R.id.container);

        setSupportActionBar(toolbar);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);


        // Set up the navigation drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.navigation_layout));

        showProgress(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = defaultTransaction();

        ArrayList<String> fragmentTags = new ArrayList<>();

        if (fragmentManager.getFragments() != null) {
            for (Fragment f : fragmentManager.getFragments()) {
                if (f != null && f.getTag() != null) {
                    fragmentTags.add(f.getTag());
                }
            }
        }

        switch (position) {
            case UPLOAD_FILE:
                if (!currentFragment.equals(UPLOAD_FILE_TAG)) {

                    fragmentTransaction
                            .addToBackStack(UPLOAD_FILE_TAG)
                            .replace(R.id.container,
                                    fragmentTags.contains(UPLOAD_FILE_TAG)
                                            ? (UploadFileFragment) fragmentManager.findFragmentByTag(UPLOAD_FILE_TAG)
                                            : UploadFileFragment.newInstance(UPLOAD_FILE_TAG),
                                    UPLOAD_FILE_TAG);

                    currentFragment = UPLOAD_FILE_TAG;
                    toolbarTitle = getString(R.string.home_nav_close);
                }
                break;
            case UPLOADED_FILES:
                if (!currentFragment.equals(UPLOADED_FILES_TAG)) {

                    fragmentTransaction
                            .addToBackStack(UPLOADED_FILES_TAG)
                            .replace(R.id.container,
                                    fragmentTags.contains(UPLOADED_FILES_TAG)
                                            ? (UploadedFilesFragment) fragmentManager.findFragmentByTag(UPLOADED_FILES_TAG)
                                            : UploadedFilesFragment.newInstance(UPLOADED_FILES_TAG),
                                    UPLOADED_FILES_TAG);

                    currentFragment = UPLOADED_FILES_TAG;
                    toolbarTitle = getString(R.string.home_nav_close);
                }
                break;

        }

        fragmentTransaction.commit();
    }

    public void onSaveInstanceState(Bundle outState) {

        outState.putString(Utils.CURRENT_FRAGMENT, currentFragment);
        FragmentManager fm = getSupportFragmentManager();

        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {

            // Zero indexed
            String backStackEntry = fm.getBackStackEntryAt(i).getName();

            switch (backStackEntry) {
                case UPLOAD_FILE_TAG:
                    UploadFileFragment uploadFileFragment
                            = (UploadFileFragment) fm.findFragmentByTag(UPLOAD_FILE_TAG);
                    getSupportFragmentManager().putFragment(outState, UPLOAD_FILE_TAG, uploadFileFragment);
                    break;
                case UPLOADED_FILES_TAG:
                    UploadedFilesFragment uploadedFilesFragment
                            = (UploadedFilesFragment) fm.findFragmentByTag(UPLOADED_FILES_TAG);
                    getSupportFragmentManager().putFragment(outState, UPLOADED_FILES_TAG, uploadedFilesFragment);
                    break;
                case VIEW_FILE_TAG:
                    ViewFileFragment viewFileFragment
                            = (ViewFileFragment) fm.findFragmentByTag(VIEW_FILE_TAG);
                    getSupportFragmentManager().putFragment(outState, VIEW_FILE_TAG, viewFileFragment);
                    break;
            }

            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void afterActivityCreated(String fragmentTag) {
        super.afterActivityCreated(fragmentTag);

        switch (fragmentTag) {
            case UPLOAD_FILE_TAG:
                fab.hideThenShow(R.drawable.ic_file_upload_white_24dp);
                setToolbarTitle(getResources().getStringArray(R.array.home_navigation)[0]);
                break;
            case UPLOADED_FILES_TAG:
                fab.hideThenShow(R.drawable.ic_refresh_white_24dp);
                setToolbarTitle(getResources().getStringArray(R.array.home_navigation)[1]);
                break;
            case VIEW_FILE_TAG:
                fab.hideTranslate();
                setToolbarTitle(getString(R.string.view_file));
                break;
        }
    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {

            // Subtract 2; zero indexed, and I want what's behind
            String backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName();

            switch (backStackEntry) {
                case UPLOAD_FILE_TAG:
                    fab.hideThenShow(R.drawable.ic_file_upload_white_24dp);
                    toolbarTitle = getString(R.string.home_nav_close);
                    break;
            }
            currentFragment = backStackEntry;
            restoreActionBar();
            fm.popBackStack();

            if (getCurrentFragmentPosition() >= UPLOAD_FILE && getCurrentFragmentPosition() <= UPLOADED_FILES) {

                NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                        getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

                if (navigationDrawerFragment != null) {
                    navigationDrawerFragment.selectAdapterPosition(getCurrentFragmentPosition());
                }
            }
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.confirm_close)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mainContent.setVisibility(show ? View.GONE : View.VISIBLE);
        toolbar.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    public void showViewFileFragment(String formType) {

        defaultTransaction()
                .addToBackStack(VIEW_FILE_TAG)
                .replace(R.id.container, ViewFileFragment.newInstance(VIEW_FILE_TAG, formType), VIEW_FILE_TAG)
                .commit();
    }

    private int getCurrentFragmentPosition() {
        switch (currentFragment) {
            case UPLOAD_FILE_TAG:
                return UPLOAD_FILE;
            case UPLOADED_FILES_TAG:
                return UPLOADED_FILES;
            case VIEW_FILE_TAG:
                return 2;
            default:
                throw new IllegalStateException("Current fragment String is not valid");
        }
    }
}
