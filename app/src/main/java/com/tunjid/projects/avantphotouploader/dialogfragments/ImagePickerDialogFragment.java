package com.tunjid.projects.avantphotouploader.dialogfragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreDialogFragment;
import com.tunjid.projects.avantphotouploader.adapters.ImagePickerAdapter;
import com.tunjid.projects.avantphotouploader.helpers.Utils;
import com.tunjid.projects.avantphotouploader.interfaces.ImageReceiver;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.yqritc.recyclerviewflexibledivider.VerticalDividerItemDecoration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DialogFragment used to pick photos
 */

public class ImagePickerDialogFragment extends CoreDialogFragment
        implements View.OnClickListener {

    private static final String LAST_USED_REQUEST_CODE = "LAST_USED_REQUEST_CODE";

    private int lastUsedRequestCode = 0;

    private String imageToUpload;
    private String imagePath;

    private ImageButton removePhoto;
    private ImageView photoThumbnail;
    private RecyclerView recyclerView;


    public static ImagePickerDialogFragment newInstance(String tag) {
        ImagePickerDialogFragment fragment = new ImagePickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    public ImagePickerDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastUsedRequestCode = savedInstanceState.getInt(LAST_USED_REQUEST_CODE);
            imagePath = savedInstanceState.getString(Utils.GENERIC_FLAG);
            imageToUpload = savedInstanceState.getString(Utils.GENERIC_FLAG_2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_image_picker, container, false);
        initializeViewComponents(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (imageToUpload != null && lastUsedRequestCode != 0) {
            onImagePicked(imageToUpload, lastUsedRequestCode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        if (dialog != null) { // Only do this if returning a dialog, not a fragment
            alignDialogToTop(dialog, HIDE_KEYBOARD, STATE_RESUMED);
        }
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        alignDialogToTop(dialog, HIDE_KEYBOARD, STATE_CREATED);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                deleteFile(imagePath, true);
            }
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_USED_REQUEST_CODE, lastUsedRequestCode);
        outState.putString(Utils.GENERIC_FLAG, imagePath);
        outState.putString(Utils.GENERIC_FLAG_2, imageToUpload);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.done:
                if (imageToUpload != null && getParentFragment() instanceof ImageReceiver) {
                    ((ImageReceiver) getParentFragment()).onImagePicked(imageToUpload);
                    dismiss();
                }
                else {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                break;
            case R.id.back_button:
                dismiss();
                break;
            case R.id.remove_image:
                photoThumbnail.setVisibility(View.INVISIBLE);
                removePhoto.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initializeViewComponents(View rootView) {
        final ImageButton doneCheckMark = (ImageButton) rootView.findViewById(R.id.done);
        final ImageButton backButton = (ImageButton) rootView.findViewById(R.id.back_button);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        removePhoto = (ImageButton) rootView.findViewById(R.id.remove_image);
        photoThumbnail = (ImageView) rootView.findViewById(R.id.image_thumbnail);

        doneCheckMark.setOnClickListener(this);
        backButton.setOnClickListener(this);
        removePhoto.setOnClickListener(this);

        final ImagePickerAdapter imagePickerAdapter = new ImagePickerAdapter(getActivity());

        imagePickerAdapter.setAdapterListener(new ImagePickerAdapter.AdapterListener() {
            @Override
            public void onImageIntentClicked(int purpose, String imagePath) {
                switch (purpose) {
                    case Utils.CAMERA_INTENT:
                        // create Intent to take a picture and return control to the calling application
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        // start the image capture Intent
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) { // Check if a camera exists

                            // Create photo directory
                            File photo = null;

                            try {
                                photo = createImageFile();
                            }

                            catch (IOException ex) {
                                Toast.makeText(getActivity(), R.string.image_capture_failed, Toast.LENGTH_SHORT).show();
                            }

                            // Continue only if the File was successfully created
                            if (photo != null) {
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                                // Call this using the parent fragment
                                getParentFragment().startActivityForResult(intent, Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                            }
                        }
                        break;
                    case Utils.GALLERY_INTENT:

                        intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        getParentFragment().startActivityForResult(Intent.createChooser(intent, "Select Picture"), Utils.GALLERY_ACTIVITY_REQUEST_CODE);

                        break;
                    case Utils.NULL:
                        break;

                    case Utils.RECENT_IMAGES:
                        onImagePicked(imagePath, Utils.GALLERY_ACTIVITY_REQUEST_CODE);
                        break;
                }
            }
        });

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                if (imagePickerAdapter.isLoadingData()) {
                    return 3;
                }
                return 1;
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(imagePickerAdapter);

        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(ContextCompat.getColor(getContext(), R.color.transparent))
                .sizeResId(R.dimen.eigth_margin)
                .build());

        recyclerView.addItemDecoration(new VerticalDividerItemDecoration.Builder(getActivity())
                .color(ContextCompat.getColor(getContext(), R.color.transparent))
                .sizeResId(R.dimen.eigth_margin)
                .build());
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "foodie_" + timeStamp + "_";

        File myFab5Directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/MyFab5/");

        boolean directoryMade = false;

        if (!myFab5Directory.exists()) {
            directoryMade = myFab5Directory.mkdir();
        }

        if (!directoryMade && !myFab5Directory.exists()) {
            throw new IOException("Failed to create image");
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                myFab5Directory      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents (prepend "file:"?)
        imagePath = image.getAbsolutePath();

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                onImagePicked(imagePath, Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), R.string.image_capture_cancelled, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), R.string.image_capture_failed, Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == Utils.GALLERY_ACTIVITY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                Uri selectedImageUri = data.getData();
                String imagePath;

                try {
                    imagePath = getPath(getActivity(), selectedImageUri);
                }
                catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.image_capture_failed, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }

                if (imagePath != null) {
                    onImagePicked(imagePath, Utils.GALLERY_ACTIVITY_REQUEST_CODE);
                }
                else {
                    Toast.makeText(getActivity(), R.string.image_capture_failed, Toast.LENGTH_SHORT).show();
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), R.string.image_capture_cancelled, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), R.string.image_capture_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Used to distinguish if the photo came from the gallery or camera
    public void onImagePicked(String imagePath, int requestCode) {

        imageToUpload = imagePath;
        lastUsedRequestCode = requestCode;

        // File from gallery or camera after cropping
        Picasso.with(getActivity())
                .load(new File(imageToUpload))
                .fit()
                .centerInside()
                .into(photoThumbnail);


        photoThumbnail.setVisibility(View.VISIBLE);
        removePhoto.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
