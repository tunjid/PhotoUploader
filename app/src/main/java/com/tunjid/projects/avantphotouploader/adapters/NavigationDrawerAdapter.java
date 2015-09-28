package com.tunjid.projects.avantphotouploader.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreAdapter;


/**
 * Adapter for holding the list gotten off the BestOfResponse Object
 */
public class NavigationDrawerAdapter extends CoreAdapter<NavigationDrawerAdapter.ViewHolder> {

    private static final int LOADING = 0;
    private static final int HEADER = 1;
    private static final int NAVIGATION_ITEM = 2;

    private int selectedPostion = 0;

    private AdapterListener adapterListener;

    private String[] navigationItems;

    private int[] homeNavigationIconsGrey =
            {R.drawable.ic_file_upload_white_24dp, R.drawable.ic_folder_white_24dp};

    private int[] homeNavigationIconsPrimary =
            {R.drawable.ic_file_upload_accent_24dp, R.drawable.ic_folder_accent_24dp};

    private ParseUser user = ParseUser.getCurrentUser();

    private Context context;

    /**
     * Default constructor, takes no data as nothing has been recieved from the API
     */

    public NavigationDrawerAdapter(String[] navigationItems) {
        this.navigationItems = navigationItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        context = viewGroup.getContext();
        View itemView;

        switch (viewType) {
            case LOADING:
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_loading, viewGroup, false);
                break;
            case HEADER:
                itemView = LayoutInflater.from(context).inflate(R.layout.header, viewGroup, false);
                break;
            default:            // Load default Message layout as bucketLists all have the same layout
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_navigation_drawer_row, viewGroup, false);
                break;
        }

        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int recyclerViewPosition) {

        final int viewType = getItemViewType(recyclerViewPosition);

        switch (viewType) {
            case LOADING:
                //Do nothing
                break;
            case HEADER:

                Picasso.with(context)
                        .load("http://www.droid-life.com/wp-content/uploads/2014/11/14-88.png")
                        .placeholder(R.drawable.bg_rectangle_grey_400)
                        .fit()
                        .into(viewHolder.headerBackground);

                viewHolder.fullNameTextView.setText(user.getUsername());
                viewHolder.fullNameTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
                break;
            default:
                viewHolder.fullNameTextView.setText(navigationItems[recyclerViewPosition - 1]);
                viewHolder.fullNameTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                                ((selectedPostion == (recyclerViewPosition - 1))
                                        ? homeNavigationIconsPrimary[recyclerViewPosition - 1]
                                        : homeNavigationIconsGrey[recyclerViewPosition - 1])
                                , 0, 0, 0);

                viewHolder.fullNameTextView.setTextColor((selectedPostion == (recyclerViewPosition - 1))
                        ? ContextCompat.getColor(context, R.color.accent)
                        : ContextCompat.getColor(context, R.color.white));

                break;
        }

        viewHolder.viewHolderListener = new ViewHolder.ViewHolderListener() {
            @Override
            public void onNavigationItemClicked() {
                if (viewType == HEADER) {
                    adapterListener.onNavigationItemClicked(1);
                }
                else if (viewType == NAVIGATION_ITEM) {
                    adapterListener.onNavigationItemClicked(recyclerViewPosition - 1);
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (loadingData) {
            return LOADING;
        }
        else if (position == 0) {
            return HEADER;
        }
        else {
            return NAVIGATION_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        if (loadingData) {
            return 1;
        }
        else {
            return (navigationItems.length) + 1;
        }
    }

    public void setSelectedPostion(int selectedPostion) {
        this.selectedPostion = selectedPostion;
        notifyItemRangeChanged(1, 4);
    }

    // ViewHolder for actual content
    public final static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public int viewType;        // Used to specify the view type.

        TextView fullNameTextView;
        ImageView headerBackground;

        public ViewHolderListener viewHolderListener;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);
            this.viewType = ViewType;

            switch (ViewType) {
                case LOADING:
                    // Do nothing
                    break;
                case HEADER:
                    this.viewType = HEADER;
                    fullNameTextView = (TextView) itemView.findViewById(R.id.name);
                    headerBackground = (ImageView) itemView.findViewById(R.id.header_background);
                    break;
                default:
                    this.viewType = NAVIGATION_ITEM;
                    fullNameTextView = (TextView) itemView;
                    itemView.setOnClickListener(this); // Set listener for the whole relative layout
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch ((v.getId())) {
                case R.id.row_text:
                    viewHolderListener.onNavigationItemClicked();
                    break;
            }
        }

        public interface ViewHolderListener {
            void onNavigationItemClicked();
        }
    }

    /**
     * Set the adapterListener to be notified when an item has been clicked.
     */
    public void setAdapterListener(AdapterListener listener) {
        this.adapterListener = listener;
    }


    public interface AdapterListener {
        void onNavigationItemClicked(int position);
    }

}
