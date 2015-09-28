package com.tunjid.projects.avantphotouploader.dialogfragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreActivity;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreDialogFragment;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

/**
 * Fragment used to sign in to Instagram
 */
public class WebViewDialogFragment extends CoreDialogFragment {

    public static final String TAG = WebViewDialogFragment.class.getSimpleName();

    private WebView webView;

    public static WebViewDialogFragment newInstance(int position, String urlToLoad) {
        WebViewDialogFragment fragment = new WebViewDialogFragment();

        Bundle args = new Bundle();
        args.putInt(Utils.FRAGMENT_TAG, position);
        args.putString(Utils.GENERIC_FLAG, urlToLoad);
        fragment.setArguments(args);
        return fragment;
    }

    public WebViewDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_webview_dialog, container, false);
        initializeViewComponents(rootView);
        return rootView;
    }


    private void initializeViewComponents(View rootView) {
        webView = (WebView) rootView.findViewById(R.id.web_oauth);

        if (((CoreActivity) getActivity()).hasActionBar()) {

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) webView.getLayoutParams();

            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.triple_and_half_margin), 0, 0);

            webView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String urlToLoad = getArguments().getString(Utils.GENERIC_FLAG);

        //load the url of the oAuth login page
        webView.loadUrl(urlToLoad);

        //set the web client
        webView.setWebViewClient(new WebViewClient());

    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
