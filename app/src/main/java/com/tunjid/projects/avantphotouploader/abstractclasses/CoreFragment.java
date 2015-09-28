package com.tunjid.projects.avantphotouploader.abstractclasses;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.services.AvantApi;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class CoreFragment extends Fragment {


    public CoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_STOPPED, false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_PAUSED, false);
            args.putBoolean(Utils.WAS_STOPPED, false);
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, false);
        }
    }

    @Override
    public void onPause() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_PAUSED, true);
        }

        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (viewDeconstructed()) {
            reconstructView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.WAS_STOPPED, true);
        }
        deconstructView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DESTROYED, true);
        }

        if (!viewDeconstructed()) {
            deconstructView();
        }

    }

    /**
     * Method used to clean up resources when the fragment is stopped
     */
    public void deconstructView() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, true);
        }
    }

    /**
     * Method used to restore Fragment
     */
    public void reconstructView() {
        Bundle args = getArguments();
        if (args != null) {
            args.putBoolean(Utils.VIEW_DECONSTRUCTED, false);
        }
    }

    public void callAfterActivityCreated() {
        ((CoreActivity) getActivity()).afterActivityCreated(
                getArguments().getString(Utils.FRAGMENT_TAG));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean wasStopped() {
        return getArguments().containsKey(Utils.WAS_STOPPED)
                && getArguments().getBoolean(Utils.WAS_STOPPED);
    }

    public boolean viewDeconstructed() {
        return getArguments().containsKey(Utils.VIEW_DECONSTRUCTED)
                && getArguments().getBoolean(Utils.VIEW_DECONSTRUCTED);
    }

    public boolean viewDestroyed() {
        return getArguments().containsKey(Utils.VIEW_DESTROYED)
                && getArguments().getBoolean(Utils.VIEW_DESTROYED);
    }

    public AvantApi getApi() {
        return ((CoreActivity) getActivity()).getApi();
    }

}
