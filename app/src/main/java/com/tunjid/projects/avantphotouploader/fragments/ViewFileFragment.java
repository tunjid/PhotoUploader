package com.tunjid.projects.avantphotouploader.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreFragment;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Fragment used to edit a selection in a ranking
 */
public class ViewFileFragment extends CoreFragment
        implements
        GetDataCallback,
        Observer<File> {

    private byte[] photoData;
    private String imagePath;

    public static ViewFileFragment newInstance(String tag, String formType) {
        ViewFileFragment fragment = new ViewFileFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        args.putString(Utils.GENERIC_FLAG, formType);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewFileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString(Utils.GENERIC_FLAG);
            photoData = savedInstanceState.getByteArray(Utils.GENERIC_FLAG_2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_file, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utils.GENERIC_FLAG, imagePath);
        outState.putByteArray(Utils.GENERIC_FLAG_2, photoData);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (imagePath != null) {
            loadImagePath();
        }
        else if (photoData != null) {
            createFileAsync(photoData);
        }
        else {

            final String formType = getArguments().getString(Utils.GENERIC_FLAG);

            assert formType != null;

            final ParseUser signedInUser = ParseUser.getCurrentUser();
            signedInUser.getParseObject(formType).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject formDataFile, ParseException e) {

                    imagePath = formDataFile.getString(Utils.IMAGE_PATH);

                    File imageFile = new File(imagePath);

                    if (imageFile.exists()) {  // Picture is still on the phone
                        loadImagePath();
                    }
                    else { // Picture is gone for some reason
                        imagePath = null;
                        requestFile(formDataFile);
                    }
                }
            });

        }
        callAfterActivityCreated();
    }

    @Override
    public void done(byte[] bytes, ParseException e) {
        if (e == null && bytes != null) {
            photoData = bytes;
            createFileAsync(bytes);
        }
        else if (e == null) {
            Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void createFileAsync(final byte[] photoData) {

        Observable.defer(new Func0<Observable<File>>() {
            @Override
            public Observable<File> call() {
                final String formType = getArguments().getString(Utils.GENERIC_FLAG);
                File imageFile = new File(getActivity().getCacheDir(), formType + ".jpg");

                try {
                    FileOutputStream fos = new FileOutputStream(imageFile, false);
                    fos.write(photoData);
                    fos.flush();
                    fos.close();

                    return Observable.just(imageFile);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(12, TimeUnit.SECONDS)
                .subscribe(this);
    }

    /**
     * Loads the photo if it still exists on the phone's storage
     */
    private void loadImagePath() {
        if (getView() != null) {
            final View rootView = getView();
            final ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

            Picasso.with(getContext())
                    .load(new File(imagePath))
                    .fit()
                    .centerInside()
                    .into(photo);

            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Request for the raw byte array that makes up the photo
     * @param formDataFile The custom Parse object the firle was saved into
     */
    private void requestFile(final ParseObject formDataFile) {
        if (getView() != null) {
            getView().post(new Runnable() { // Show on UI thread
                @Override
                public void run() {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder
                            .setTitle(R.string.error_plain)
                            .setMessage(R.string.deleted_photo)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    formDataFile.getParseFile(Utils.FORM_UPLOAD).getDataInBackground(ViewFileFragment.this);
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    getActivity().onBackPressed();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNext(File imageFile) {
        if (getView() != null && imageFile != null) {
            View rootView = getView();
            final ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
            final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

            Picasso.with(getActivity())
                    .load(imageFile)
                    .fit()
                    .centerInside()
                    .into(photo);

            progressBar.setVisibility(View.GONE);
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }
}