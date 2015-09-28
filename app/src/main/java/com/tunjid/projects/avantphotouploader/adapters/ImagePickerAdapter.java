package com.tunjid.projects.avantphotouploader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreAdapter;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

import java.io.File;

/**
 * Adapter for holding the list gotten off the BestOfResponse Object
 */
public class ImagePickerAdapter extends CoreAdapter<ImagePickerAdapter.ImagePickerViewHolder>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADING_DATA = 0;
    private static final int INTENT = 1;
    private static final int PHOTO = 2;


    private Context context;

    private Cursor cursor;

    private AdapterListener adapterListener;

    public ImagePickerAdapter(Context context) {
        this.context = context;

        ((AppCompatActivity) this.context).getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public ImagePickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();

        View itemView = null; // Initialize the item view.

        switch (viewType) {
            case LOADING_DATA:
                itemView = LayoutInflater.from(context).
                        inflate(R.layout.fragment_loading, viewGroup, false);
                break;
            case INTENT:
                itemView = LayoutInflater.from(context).
                        inflate(R.layout.fragment_imagepicker_intent_row, viewGroup, false);
                break;
            case PHOTO:
                itemView = LayoutInflater.from(context).
                        inflate(R.layout.fragment_imagepicker_picture_row, viewGroup, false);
                break;
        }
        return new ImagePickerViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ImagePickerViewHolder viewHolder, final int position) {

        switch (viewHolder.viewType) {
            case LOADING_DATA:
                // Do nothing.
                break;
            case INTENT:
                int holder = 0;
                switch (position) {
                    case 0:
                        holder = Utils.CAMERA_INTENT;
                        viewHolder.imageIntent.setText(R.string.camera);
                        viewHolder.imageIntent.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                        viewHolder.imageIntent.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_camera_alt_white_24dp, 0, 0);
                        break;
                    case 1:
                        holder = Utils.GALLERY_INTENT;
                        viewHolder.imageIntent.setText(R.string.phone_gallery);
                        viewHolder.imageIntent.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_600));
                        viewHolder.imageIntent.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_photo_library_white_24dp, 0, 0);
                        break;
                }
                final int clickIntent = holder;
                viewHolder.viewHolderListener = new ImagePickerViewHolder.ViewHolderListener() {
                    @Override
                    public void onImageIntentClicked() {
                        adapterListener.onImageIntentClicked(clickIntent, "dummyString");
                    }
                };

                break;
            case PHOTO:
                cursor.moveToPosition(position - 2);

                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                final String imagePath = cursor.getString(columnIndex);

                Picasso.with(context)
                        .load(new File(imagePath))
                        .placeholder(R.drawable.bg_rectangle_grey_400)
                        .fit()
                        .centerCrop()
                        .into(viewHolder.photo);

                viewHolder.viewHolderListener = new ImagePickerViewHolder.ViewHolderListener() {
                    @Override
                    public void onImageIntentClicked() {
                        adapterListener.onImageIntentClicked(Utils.RECENT_IMAGES, imagePath);
                    }
                };
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (loadingData) {
            return LOADING_DATA;
        }
        else {
            switch (position) {
                case 0:
                case 1:
                    return INTENT;
                default:
                    return PHOTO;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (loadingData) {
            return 1;
        }
        else { //
            return cursor.getCount() + 2;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Images.Media.DATA
        };

        return new CursorLoader(
                context,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;

        if (cursor != null) {
            cursor.moveToFirst();
            loadingData = false;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    /**
     * Set the adapterListener to be notified when an item has been clicked.
     */
    public void setAdapterListener(AdapterListener listener) {
        this.adapterListener = listener;
    }

    /**
     * Called when a search item has been clicked.
     */
    public interface AdapterListener {
        void onImageIntentClicked(int purpose, String imagePath);
    }

    // ViewHolder for actual content
    public final static class ImagePickerViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Used to specify the view type.
        int viewType;

        // For intents
        TextView imageIntent;

        // For images
        ImageView photo;

        public ViewHolderListener viewHolderListener;

        public ImagePickerViewHolder(View itemView, int ViewType) {
            super(itemView);
            switch (ViewType) {
                case LOADING_DATA:
                    viewType = LOADING_DATA;
                    break;
                case INTENT:
                    viewType = INTENT;
                    imageIntent = (TextView) itemView.findViewById(R.id.image_intent);
                    imageIntent.setOnClickListener(this);
                    break;
                case PHOTO:
                    viewType = PHOTO;
                    photo = (ImageView) itemView.findViewById(R.id.photo);
                    photo.setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.photo:
                case R.id.container:
                case R.id.image_intent:
                case R.id.recent_images:
                    viewHolderListener.onImageIntentClicked();
                    break;
            }

        }

        public interface ViewHolderListener {
            void onImageIntentClicked();
        }
    }
}
