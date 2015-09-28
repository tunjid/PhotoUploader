package com.tunjid.projects.avantphotouploader.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.tunjid.projects.avantphotouploader.R;
import com.tunjid.projects.avantphotouploader.abstractclasses.CoreAdapter;
import com.tunjid.projects.avantphotouploader.helpers.Utils;

import java.util.ArrayList;


/**
 * Adapter for viewing uploaded files.
 */
public class UploadedFilesAdapter extends CoreAdapter<UploadedFilesAdapter.ViewHolder> {

    private static final int LOADING = 0;
    private static final int FILE_TYPE = 1;

    private ArrayList<String> parseFiles = new ArrayList<>();

    private AdapterListener adapterListener;

    private Context context;

    /**
     * Default constructor, takes no data as all data needed can be obtained statically. It however takes
     * the listener it needs.
     */

    public UploadedFilesAdapter(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
        refreshData();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        context = viewGroup.getContext();
        View itemView;

        switch (viewType) {
            case LOADING:
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_loading, viewGroup, false);
                break;
            default:
                itemView = LayoutInflater.from(context).inflate(R.layout.fragment_uploaded_files_row, viewGroup, false);
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
            case FILE_TYPE:
                final String formType = parseFiles.get(recyclerViewPosition);

                viewHolder.formName.setTextColor(ContextCompat.getColor(context, R.color.white));

                switch (formType) {
                    case Utils.W2_FORM:
                        viewHolder.formName.setText(context.getString(R.string.w2_tax_form));
                        break;
                    case Utils.VOIDED_CHECK:
                        viewHolder.formName.setText(context.getString(R.string.voided_check));
                        break;
                    case Utils.UTILITY_BILL:
                        viewHolder.formName.setText(context.getString(R.string.utility_bill));
                        break;
                }

                viewHolder.viewHolderListener = new ViewHolder.ViewHolderListener() {
                    @Override
                    public void onFormClicked() {
                        adapterListener.onFormClicked(formType);
                    }
                };
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (loadingData) {
            return LOADING;
        }
        else {
            return FILE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        if (loadingData) {
            return 1;
        }
        else {
            return parseFiles.size();
        }
    }

    public void refreshData() {

        parseFiles.clear();

        final ParseUser signedInUser = ParseUser.getCurrentUser();

        final ParseObject w2Form = signedInUser.getParseObject(Utils.W2_FORM);
        final ParseObject voidCheck = signedInUser.getParseObject(Utils.VOIDED_CHECK);
        final ParseObject utilityBill = signedInUser.getParseObject(Utils.UTILITY_BILL);

        if (w2Form != null) {
            parseFiles.add(Utils.W2_FORM);
        }
        if (voidCheck != null) {
            parseFiles.add(Utils.VOIDED_CHECK);
        }
        if (utilityBill != null) {
            parseFiles.add(Utils.UTILITY_BILL);
        }

        adapterListener.validateData(parseFiles.size() > 0);

        super.refreshData();
    }

    // ViewHolder for actual content
    public final static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public int viewType;        // Used to specify the view type.

        TextView formName;
        TextView dateUploaded;

        public ViewHolderListener viewHolderListener;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);
            this.viewType = ViewType;

            switch (ViewType) {
                case LOADING:
                    // Do nothing
                    break;
                case FILE_TYPE:
                    this.viewType = FILE_TYPE;
                    formName = (TextView) itemView.findViewById(R.id.text);
                    dateUploaded = (TextView) itemView.findViewById(R.id.sub_text);
                    itemView.setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch ((v.getId())) {
                case R.id.container:
                    viewHolderListener.onFormClicked();
                    break;
            }
        }

        public interface ViewHolderListener {
            void onFormClicked();
        }
    }

    public interface AdapterListener {
        void onFormClicked(String formType);

        void validateData(boolean hasData);
    }

}
