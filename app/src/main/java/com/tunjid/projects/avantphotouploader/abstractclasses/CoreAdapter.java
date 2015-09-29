package com.tunjid.projects.avantphotouploader.abstractclasses;

import android.support.v7.widget.RecyclerView;

/**
 * Class for adapters that load data.
 */
public abstract class CoreAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected boolean loadingData = true;
    protected boolean onDeconstructViewCalled = false;

    /**
     * Used for adapters whose size depends on more than just the backing data set size
     *
     * @param recyclerViewPosition the position specified by {@link #getItemViewType(int)}
     * @return the recyclerView position of the model
     */
    public int getModelPosition(int recyclerViewPosition) {
        return recyclerViewPosition;
    }

    @Override
    public void onBindViewHolder(VH holder, int recyclerViewPosition) {
        // Nothing special here
    }


    public void setLoadingData(boolean loadingData) {
        this.loadingData = loadingData;
        notifyDataSetChanged();
    }

    /**
     * Used by image heavy recyclerviews who need to clear the bitmaps in the {@link android.widget.ImageView}
     * {@link android.support.v7.widget.RecyclerView.ViewHolder} when added to the backstack
     */
    public void onDeconstructView() {
        onDeconstructViewCalled = true;
        notifyDataSetChanged();
    }

    public void refreshData() {
        loadingData = false;
        onDeconstructViewCalled = false;
        notifyDataSetChanged();
    }

    public boolean isLoadingData() {
        return loadingData;
    }
}
