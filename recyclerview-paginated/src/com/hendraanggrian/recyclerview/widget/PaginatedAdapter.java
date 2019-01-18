package com.hendraanggrian.recyclerview.widget;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView.PaginationState;

final class PaginatedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PLACEHOLDER = Integer.MAX_VALUE - 50; // magic
    private static final int TYPE_ERROR = Integer.MAX_VALUE - 100; // magic

    final RecyclerView.Adapter originalAdapter;
    private PaginatedRecyclerView view;
    private PaginatedRecyclerView.PaginationState state; // only has to know about placeholder and error

    PaginatedAdapter(RecyclerView.Adapter originalAdapter) {
        this.originalAdapter = originalAdapter;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        view = (PaginatedRecyclerView) recyclerView;
    }

    void notifyStateChanged(PaginatedRecyclerView.PaginationState state) {
        this.state = state;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PLACEHOLDER:
                return view.getPlaceholderAdapter().onCreateViewHolder(parent, viewType);
            case TYPE_ERROR:
                return view.getErrorAdapter().onCreateViewHolder(parent, viewType);
            default:
                return originalAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isErrorRow(position)) {
            view.getErrorAdapter().onBindViewHolder(holder, position);
        } else if (isPlaceholderRow(position)) {
            view.getPlaceholderAdapter().onBindViewHolder(holder, position);
        } else {
            originalAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return isState(PaginationState.LOADING, PaginationState.ERROR)
            ? originalAdapter.getItemCount() + 1
            : originalAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isErrorRow(position)) {
            return TYPE_ERROR;
        } else if (isPlaceholderRow(position)) {
            return TYPE_PLACEHOLDER;
        } else {
            return originalAdapter.getItemViewType(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return isPlaceholderRow(position) || isErrorRow(position)
            ? RecyclerView.NO_ID
            : originalAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        originalAdapter.setHasStableIds(hasStableIds);
    }

    boolean isPlaceholderRow(int position) {
        return isState(PaginationState.LOADING) && position == getPlaceholderRowPosition();
    }

    private int getPlaceholderRowPosition() {
        return isState(PaginationState.LOADING) ? getItemCount() - 1 : -1;
    }

    boolean isErrorRow(int position) {
        return isState(PaginationState.ERROR) && position == getErrorRowPosition();
    }

    private int getErrorRowPosition() {
        return isState(PaginationState.ERROR) ? getItemCount() - 1 : -1;
    }

    private boolean isState(PaginationState... states) {
        for (final PaginationState state : states) {
            if (this.state == state) {
                return true;
            }
        }
        return false;
    }
}