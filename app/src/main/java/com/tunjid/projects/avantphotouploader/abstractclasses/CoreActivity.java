package com.tunjid.projects.avantphotouploader.abstractclasses;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.helpers.FloatingActionButton;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.services.AvantApi;

/**
 * Abstract Activty that holds the basic components that all activities use.
 */
public abstract class CoreActivity extends AppCompatActivity
        implements
        ServiceConnection {

    private boolean wasStopped = false;
    protected boolean isSavedInstance = false;

    protected CharSequence toolbarTitle;
    protected String currentFragment = "";

    protected Toolbar toolbar;
    protected FloatingActionButton fab;
    protected ProgressBar loadMoreBar;
    protected ProgressBar progressBar;
    protected RelativeLayout mainContent;

    protected AvantApi api;

    private AlertDialog errorAlertDialog;


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        // Overriden in child subclasses
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        // Overidden in child subclasses
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isSavedInstance = true;
            wasStopped = savedInstanceState.getBoolean(Utils.WAS_STOPPED);
            if (savedInstanceState.containsKey(Utils.TOOLBAR_TITLE)) {
                toolbarTitle = savedInstanceState.getString(Utils.TOOLBAR_TITLE);
            }
        }
        else {
            isSavedInstance = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        wasStopped = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Utils.WAS_STOPPED, wasStopped);
        if (toolbarTitle != null) {
            outState.putString(Utils.TOOLBAR_TITLE, toolbarTitle.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (api != null) {
            api = null;
        }

        try {
            unbindService(this);
        }
        catch (Exception e) { // Service might not be bound
            e.printStackTrace();
        }
    }

    /**
     * Gets the ToolBar used as the toolbar in this activity
     */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the FAB used in this activity
     */
    public FloatingActionButton getFAB() {
        return fab;
    }

    /**
     * Gets the bound service that is the Api instance
     */
    public AvantApi getApi() {
        return api;
    }

    @Nullable
    public CoreFragment getFragment(String tag) {
        Fragment coreFragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (coreFragment instanceof CoreFragment) {
            return (CoreFragment) coreFragment;
        }
        return null;
    }

    @Nullable
    public CoreFragment getCurrentFragment() {
        Fragment coreFragment = getSupportFragmentManager().findFragmentByTag(currentFragment);

        if (coreFragment instanceof CoreFragment) {
            return (CoreFragment) coreFragment;
        }
        return null;
    }

    public boolean hasActionBar() {
        return toolbar != null;
    }

    public void setToolbarTitle(CharSequence toolbarTitle) {
        this.toolbarTitle = toolbarTitle;
        restoreActionBar();
    }

    /**
     * Toggles visibility of the {@link ProgressBar} showing that a refresh action is going on
     */
    public void toggleLoadMoreBar(boolean state) {
        loadMoreBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows the progress UI and hides the login form and vice versa.
     * for activities whos content view is activity_base.
     * <p/>
     * Activities that don't must overide this method
     */
    public void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mainContent.setVisibility(show ? View.GONE : View.VISIBLE);
        toolbar.setVisibility(show ? View.GONE : View.VISIBLE);
        if (fab != null) {
            if (show) {
                fab.hide();
            }
            else {
                fab.show();
            }
        }
    }

    public void openWebPage(String link) {

        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            link = "http://" + link;
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(i);
    }

    public boolean wasStopped() {
        return wasStopped;
    }


    /**
     * Call back within an activity to perform certain actions after
     * a fragment view has been notified that it's parent activity exists
     *
     * @param currentFragment A string representing the current fragment shown
     */
    public void afterActivityCreated(String currentFragment) {
        this.currentFragment = currentFragment;
    }

    /**
     * Refreshes the toolbar title
     */
    protected void restoreActionBar() {
        if (toolbar != null) {
            toolbar.setTitle(toolbarTitle);
        }
    }

    /**
     * Method used for creating a default animated {@link FragmentTransaction}
     * that slides up. Used for photos and restoring from instance state.
     *
     * @return an uncommited {@link FragmentTransaction} to build upon
     */
    @SuppressLint("CommitTransaction")
    public FragmentTransaction slideUpTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out,
                        R.anim.abc_fade_in, R.anim.abc_slide_out_bottom);
    }

    /**
     * Method used for creating a default animated {@link FragmentTransaction}
     *
     * @return an uncommited {@link FragmentTransaction} to build upon
     */
    @SuppressLint("CommitTransaction")
    public FragmentTransaction defaultTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.abc_fade_out,
                        R.anim.abc_fade_in, R.anim.slide_out_left);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Shows an error dialog when there is a network issue.
     */
    public void showErrorDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (errorAlertDialog == null || !errorAlertDialog.isShowing()) {

                    try {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(CoreActivity.this);
                        builder.setNegativeButton(R.string.go_back, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        });

                        builder.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setTitle(R.string.network_error);

                        // 3. Get the AlertDialog from create()

                        errorAlertDialog = builder.create();

                        errorAlertDialog.show();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


}
