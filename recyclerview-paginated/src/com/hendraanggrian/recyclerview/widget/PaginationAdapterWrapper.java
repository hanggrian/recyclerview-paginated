package com.hendraanggrian.recyclerview.widget;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class PaginationAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int TYPE_LOADING = Integer.MAX_VALUE - 50; // magic

    private final RecyclerView.Adapter actualAdapter;
    private final PaginatedRecyclerView.PlaceholderAdapter placeholderAdapter;
    private boolean isDisplaying = true;

    PaginationAdapterWrapper(
        RecyclerView.Adapter actualAdapter,
        PaginatedRecyclerView.PlaceholderAdapter placeholderAdapter
    ) {
        this.actualAdapter = actualAdapter;
        this.placeholderAdapter = placeholderAdapter;
    }

    RecyclerView.Adapter getActualAdapter() {
        return actualAdapter;
    }

    void setDisplaying(boolean display) {
        if (isDisplaying != display) {
            isDisplaying = display;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_LOADING
            ? placeholderAdapter.onCreateViewHolder(parent, viewType)
            : actualAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isLoadingRow(position)) {
            placeholderAdapter.onBindViewHolder(holder, position);
        } else {
            actualAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return isDisplaying
            ? actualAdapter.getItemCount() + 1
            : actualAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return isLoadingRow(position)
            ? TYPE_LOADING
            : actualAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return isLoadingRow(position)
            ? RecyclerView.NO_ID
            : actualAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        actualAdapter.setHasStableIds(hasStableIds);
    }

    boolean isLoadingRow(int position) {
        return isDisplaying && position == getLoadingRowPosition();
    }

    private int getLoadingRowPosition() {
        return isDisplaying ? getItemCount() - 1 : -1;
    }
}