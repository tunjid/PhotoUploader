package com.tunjid.projects.avantphotouploader.fragments;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreActivity;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreFragment;
import com.tunjid.projects.avantphotouploader.activities.HomeActivity;
import com.tunjid.projects.avantphotouploader.adapters.UploadedFilesAdapter;
import com.tunjid.projects.avantphotouploader.helpers.FloatingActionButton;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

/**
 * Fragment used to edit a selection in a ranking
 */
public class UploadedFilesFragment extends CoreFragment
        implements View.OnClickListener {

    public static UploadedFilesFragment newInstance(String tag) {
        UploadedFilesFragment fragment = new UploadedFilesFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    public UploadedFilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.wrapped_recyclerview_base_top_margin, container, false);
        initializeViewComponents(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = ((CoreActivity) getActivity()).getFAB();
        fab.setOnClickListener(this);

        callAfterActivityCreated();
    }


    private void initializeViewComponents(final View rootView) {

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        final UploadedFilesAdapter uploadedFilesAdapter = new UploadedFilesAdapter();

        uploadedFilesAdapter.setAdapterListener(new UploadedFilesAdapter.AdapterListener() {
            @Override
            public void onFormClicked(String formType) {
                ((HomeActivity) getActivity()).showViewFileFragment(formType);
            }

            @Override
            public void onNoData() {
                final TextView noDataTextView = (TextView) rootView.findViewById(R.id.text);
                noDataTextView.setText(getString(R.string.no_uploads));
            }
        });

        recyclerView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(uploadedFilesAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (getView() != null) {
                    final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
                    ((UploadedFilesAdapter) recyclerView.getAdapter()).refreshData();
                }
                break;
        }
    }
}