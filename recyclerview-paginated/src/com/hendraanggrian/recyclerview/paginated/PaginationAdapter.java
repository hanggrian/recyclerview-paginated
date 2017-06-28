package com.hendraanggrian.recyclerview.paginated;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_LOADING = Integer.MAX_VALUE - 50; // Magic

    @NonNull private final RecyclerView.Adapter originalAdapter;
    @NonNull private final LoadingAdapter loadingAdapter;
    private boolean isDisplaying = true;

    public PaginationAdapter(@NonNull RecyclerView.Adapter adapter, @NonNull LoadingAdapter loadingAdapter) {
        this.originalAdapter = adapter;
        this.loadingAdapter = loadingAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_LOADING) {
            return loadingAdapter.onCreateViewHolder(parent, viewType);
        } else {
            return originalAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isLoadingRow(position)) {
            loadingAdapter.onBindViewHolder(holder, position);
        } else {
            originalAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return isDisplaying ? originalAdapter.getItemCount() + 1 : originalAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return isLoadingRow(position) ? ITEM_VIEW_TYPE_LOADING : originalAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return isLoadingRow(position) ? RecyclerView.NO_ID : originalAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        originalAdapter.setHasStableIds(hasStableIds);
    }

    @NonNull
    public RecyclerView.Adapter getOriginalAdapter() {
        return originalAdapter;
    }

    boolean isDisplaying() {
        return isDisplaying;
    }

    public void setDisplaying(boolean isDisplaying) {
        if (this.isDisplaying != isDisplaying) {
            this.isDisplaying = isDisplaying;
            notifyDataSetChanged();
        }
    }

    boolean isLoadingRow(int position) {
        return isDisplaying && position == getLoadingRowPosition();
    }

    private int getLoadingRowPosition() {
        return isDisplaying ? getItemCount() - 1 : -1;
    }
}