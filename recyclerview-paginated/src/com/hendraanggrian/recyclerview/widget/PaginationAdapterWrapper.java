package com.hendraanggrian.recyclerview.widget;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class PaginationAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PLACEHOLDER = Integer.MAX_VALUE - 50; // magic
    private static final int TYPE_ERROR = Integer.MAX_VALUE - 100; // magic

    private final RecyclerView.Adapter actualAdapter;
    private final PaginatedRecyclerView.NonBindingAdapter placeholderAdapter;
    private final PaginatedRecyclerView.NonBindingAdapter errorAdapter;

    private boolean isPlaceholder = true;
    private boolean isError = false;

    PaginationAdapterWrapper(
        RecyclerView.Adapter actualAdapter,
        PaginatedRecyclerView.NonBindingAdapter placeholderAdapter,
        PaginatedRecyclerView.NonBindingAdapter errorAdapter
    ) {
        this.actualAdapter = actualAdapter;
        this.placeholderAdapter = placeholderAdapter;
        this.errorAdapter = errorAdapter;
    }

    RecyclerView.Adapter getActualAdapter() {
        return actualAdapter;
    }

    void update(boolean isPlaceholder, boolean isError) {
        this.isPlaceholder = isPlaceholder;
        this.isError = isError;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PLACEHOLDER:
                return placeholderAdapter.onCreateViewHolder(parent, viewType);
            case TYPE_ERROR:
                return errorAdapter.onCreateViewHolder(parent, viewType);
            default:
                return actualAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isErrorRow(position)) {
            errorAdapter.onBindViewHolder(holder, position);
        } else if (isPlaceholderRow(position)) {
            placeholderAdapter.onBindViewHolder(holder, position);
        } else {
            actualAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return isPlaceholder || isError
            ? actualAdapter.getItemCount() + 1
            : actualAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isErrorRow(position)) {
            return TYPE_ERROR;
        } else if (isPlaceholderRow(position)) {
            return TYPE_PLACEHOLDER;
        } else {
            return actualAdapter.getItemViewType(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return isPlaceholderRow(position) || isErrorRow(position)
            ? RecyclerView.NO_ID
            : actualAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        actualAdapter.setHasStableIds(hasStableIds);
    }

    boolean isPlaceholderRow(int position) {
        return isPlaceholder && position == getPlaceholderRowPosition();
    }

    private int getPlaceholderRowPosition() {
        return isPlaceholder ? getItemCount() - 1 : -1;
    }

    boolean isErrorRow(int position) {
        return isError && position == getErrorRowPosition();
    }

    private int getErrorRowPosition() {
        return isError ? getItemCount() - 1 : -1;
    }
}