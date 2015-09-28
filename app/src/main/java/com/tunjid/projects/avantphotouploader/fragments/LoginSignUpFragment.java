package com.tunjid.projects.avantphotouploader.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreFragment;
import com.tunjid.projects.avantphotouploader.activities.HomeActivity;
import com.tunjid.projects.avantphotouploader.activities.LoginActivity;

/**
 * A simple login fragment
 */
public class LoginSignUpFragment extends CoreFragment
        implements
        LogInCallback,
        SignUpCallback,
        View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private CheckBox agreedCheckBox;
    private RelativeLayout progressBar;


    public static LoginSignUpFragment newInstance() {

        LoginSignUpFragment loginSignUpFragment = new LoginSignUpFragment();
        Bundle args = new Bundle();
        loginSignUpFragment.setArguments(args);
        return loginSignUpFragment;
    }

    public LoginSignUpFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_sign_up, container, false);
        initializeViewComponents(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initializeViewComponents(View rootView) {

        final Button loginButton = (Button) rootView.findViewById(R.id.login);
        final Button signUpButton = (Button) rootView.findViewById(R.id.sign_up);
        final TextView agreementTextView = (TextView) rootView.findViewById(R.id.agreement);

        userNameEditText = (EditText) rootView.findViewById(R.id.user_name);
        passwordEditText = (EditText) rootView.findViewById(R.id.password);
        agreedCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox);
        progressBar = (RelativeLayout) rootView.findViewById(R.id.progress_bar);

        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

        SpannableString termsOfServiceStart = new SpannableString(getString(R.string.read_terms));
        SpannableString termsOfServiceText = new SpannableString(getString(R.string.terms_of_service));
        SpannableString andText = new SpannableString(getString(R.string.and));
        SpannableString privacyPolicyText = new SpannableString(getString(R.string.privacy_policy));

        termsOfServiceText.setSpan(new UnderlineSpan(), 0, termsOfServiceText.length(), 0);
        termsOfServiceText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                ((LoginActivity) getActivity()).showTermsOfService();
            }
        }, 0, termsOfServiceText.length(), 0);

        privacyPolicyText.setSpan(new UnderlineSpan(), 0, privacyPolicyText.length(), 0);
        privacyPolicyText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                ((LoginActivity) getActivity()).showPrivacyPolicy();
            }
        }, 0, privacyPolicyText.length(), 0);

        agreementTextView.setMovementMethod(LinkMovementMethod.getInstance());
        agreementTextView.setText(TextUtils.concat(termsOfServiceStart, " ", termsOfServiceText, " ", andText, " ", privacyPolicyText));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up:
                if (agreedCheckBox.isChecked()) {
                    attemptSignUp();
                }
                else {
                    Toast.makeText(getActivity(), R.string.agree_terms, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.login:
                attemptLogin();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        userNameEditText = null;
        passwordEditText = null;
        agreedCheckBox = null;
        progressBar = null;
    }

    public void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void attemptSignUp() {

        // Reset errors.
        userNameEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.

        final String customerID = userNameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(customerID)) {
            userNameEditText.setError(getString(R.string.error_field_required));
            focusView = userNameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);

            // Attempt login on a seperate thread, but
            // Observe the response of the login attemp on the main thread.

            showProgress(true);

            // Save new user data into Parse.com Data Storage
            ParseUser user = new ParseUser();
            user.setUsername(customerID);
            user.setPassword(password);
            user.signUpInBackground(this);
        }
    }

    private void attemptLogin() {

        // Reset errors.
        userNameEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.

        final String customerID = userNameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(customerID)) {
            userNameEditText.setError(getString(R.string.error_field_required));
            focusView = userNameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);

            // Attempt login on a seperate thread, but
            // Observe the response of the login attemp on the main thread.

            showProgress(true);

            // Send data to Parse.com for verification
            ParseUser.logInInBackground(customerID, password, this);
        }
    }


    private boolean isPasswordValid(String password) {
        return !(password == null || password.length() < 5);
    }

    // Parse login callback
    @Override
    public void done(ParseUser parseUser, ParseException e) {
        showProgress(false);

        if (parseUser != null) {
            Intent i = new Intent(getActivity(), HomeActivity.class);
            startActivity(i);
            getActivity().finish();
        }
        else {
            Toast.makeText(getActivity(),
                    getString(R.string.sign_in_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Parse sign up callback
    @Override
    public void done(ParseException e) {
        showProgress(false);

        if (e == null) {
            // Show a dialog indicating success

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            })
                    .setTitle(getString(R.string.sign_up_success))
                    .setMessage(getString(R.string.please_login))
                    .create()
                    .show();
        }
        else {
            Toast.makeText(getActivity(),
                    getString(R.string.sign_in_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
