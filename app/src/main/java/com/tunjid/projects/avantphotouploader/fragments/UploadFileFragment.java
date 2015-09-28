package com.tunjid.projects.avantphotouploader.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreActivity;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreFragment;
import com.tunjid.projects.avantphotouploader.dialogfragments.ImagePickerDialogFragment;
import com.tunjid.projects.avantphotouploader.helpers.FloatingActionButton;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.interfaces.ImageReceiver;

import java.io.IOException;


public class UploadFileFragment extends CoreFragment
        implements
        ImageReceiver,
        View.OnClickListener {

    public static final String IMAGE_PICKER = "IMAGE_PICKER";

    private String formType;
    private String imagePath;

    public static UploadFileFragment newInstance(String tag) {
        UploadFileFragment fragment = new UploadFileFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    public UploadFileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            formType = savedInstanceState.getString(Utils.GENERIC_FLAG);
            imagePath = savedInstanceState.getString(Utils.GENERIC_FLAG_2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_upload_photo, container, false);
        initializeViewCoompnents(rootView);

        return rootView;
    }

    public void initializeViewCoompnents(View rootView) {
        final RadioButton w2FormButton = (RadioButton) rootView.findViewById(R.id.w2);
        final RadioButton voidedCheckButton = (RadioButton) rootView.findViewById(R.id.void_check);
        final RadioButton utilityBillButton = (RadioButton) rootView.findViewById(R.id.utility_bill);

        w2FormButton.setOnClickListener(this);
        voidedCheckButton.setOnClickListener(this);
        utilityBillButton.setOnClickListener(this);

        w2FormButton.performClick();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = ((CoreActivity) getActivity()).getFAB();

        fab.setOnClickListener(this);

        ((CoreActivity) getActivity()).setToolbarTitle(getResources().getStringArray(R.array.home_navigation)[0]);
        callAfterActivityCreated();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImagePickerDialogFragment imagePickerDialogFragment = (ImagePickerDialogFragment)
                getChildFragmentManager().findFragmentByTag(IMAGE_PICKER);

        if (imagePickerDialogFragment != null) {
            imagePickerDialogFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utils.GENERIC_FLAG, formType);
        outState.putString(Utils.GENERIC_FLAG_2, imagePath);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.w2:
                formType = Utils.W2_FORM;
                break;
            case R.id.void_check:
                formType = Utils.VOIDED_CHECK;
                break;
            case R.id.utility_bill:
                formType = Utils.UTILITY_BILL;
                break;
            case R.id.fab:
                ImagePickerDialogFragment imagePickerDialogFragment =
                        ImagePickerDialogFragment.newInstance(IMAGE_PICKER);

                imagePickerDialogFragment.show(getChildFragmentManager(), IMAGE_PICKER);
                break;
        }

        ((CoreActivity) getActivity()).getFAB().showTranslate();
    }

    @Override
    public void onImagePicked(String imagePath) {
        this.imagePath = imagePath;

        String orientation = getOrientationFromExif(imagePath);

        if (!TextUtils.isEmpty(orientation)) {

            boolean requestedPotriat = formType.equals(Utils.W2_FORM)
                    || formType.equals(Utils.UTILITY_BILL);

            boolean confirmPotrait = orientation.equals(Utils.ORIENTATION_ROTATE_90)
                    || orientation.equals(Utils.ORIENTATION_ROTATE_270);

            if (requestedPotriat != confirmPotrait) {
                this.imagePath = null;

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                        .setTitle(getString(R.string.error_plain))
                        .setMessage(R.string.wrong_photo_orientation)
                        .create()
                        .show();
            }
            else if (getApi() != null) {
                getApi().createParseFileAsync(imagePath, formType, orientation);
            }
            else {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), getString(R.string.error_non_camera), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getOrientationFromExif(String imagePath) {
        String orientation = "";

        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                // Upside down potrait
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = Utils.ORIENTATION_ROTATE_270;
                    break;
                // Upside down landscape
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = Utils.ORIENTATION_ROTATE_180;
                    break;
                // Normal potrait
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = Utils.ORIENTATION_ROTATE_90;
                    break;
                // Normal Landscape
                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = Utils.ORIENTATION_NORMAL;
                    break;
                default:
                    break;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return orientation;
    }

}
