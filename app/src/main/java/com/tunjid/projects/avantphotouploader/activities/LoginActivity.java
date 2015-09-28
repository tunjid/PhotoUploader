package com.tunjid.projects.avantphotouploader.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreActivity;
import com.tunjid.projects.avantphotouploader.dialogfragments.WebViewDialogFragment;
import com.tunjid.projects.avantphotouploader.fragments.LoginSignUpFragment;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.services.AvantApi;

/**
 * <p>A login screen that offers login via email/password. </p>
 * <p>A user is reurned in the form of an RxJava observable for asynchronous tasks. </p>
 */
public class LoginActivity extends CoreActivity {

    public static final String SIGN_UP_TAG = "SIGN_UP_BEGIN_TAG";

    // Service life-cycle management.
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        api = ((AvantApi.LocalBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    // Login activity saves not state for security reasons
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initializeViewComponents();

        // Bind to the API service
        Intent APIintent = new Intent(this, AvantApi.class);
        bindService(APIintent, this, BIND_AUTO_CREATE);
    }

    /**
     * Initializes all view components in this activity.
     */
    private void initializeViewComponents() {

        toolbar = (Toolbar) findViewById(R.id.action_bar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mainContent = (RelativeLayout) findViewById(R.id.container);

        toolbar.setVisibility(View.GONE);
        showProgress(false);

        // Bind to the API service
        Intent APIintent = new Intent(this, AvantApi.class);
        bindService(APIintent, this, BIND_AUTO_CREATE);

        // Go straight to login home fragment
        slideUpTransaction()
                .replace(R.id.container, LoginSignUpFragment.newInstance(), SIGN_UP_TAG)
                .addToBackStack(SIGN_UP_TAG)
                .commit();
    }

    @Override
    public void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mainContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean hasActionBar() {
        return false;
    }
    @Override
    public void afterActivityCreated(String fragmentTag) {
        // No child fragment uses this call back.
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {

            // Subtract 2; zero indexed, and I want what's behind

            currentFragment = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName();
            fm.popBackStack();
        }
        else {
            finish();
        }
    }

    public void showTermsOfService() {
        defaultTransaction()
                .replace(R.id.container, WebViewDialogFragment.newInstance(0, Utils.AVANT_TERMS_OF_SERVICE), Utils.AVANT_TERMS_OF_SERVICE)
                .addToBackStack(Utils.AVANT_TERMS_OF_SERVICE)
                .commit();
    }

    public void showPrivacyPolicy() {
        defaultTransaction()
                .replace(R.id.container, WebViewDialogFragment.newInstance(0, Utils.AVANT_PRIVACY_POLICY), Utils.AVANT_PRIVACY_POLICY)
                .addToBackStack(Utils.AVANT_PRIVACY_POLICY)
                .commit();
    }
}



