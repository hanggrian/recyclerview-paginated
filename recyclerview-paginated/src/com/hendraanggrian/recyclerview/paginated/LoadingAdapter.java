package com.hendraanggrian.recyclerview.paginated;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * @see PaginationAdapter
 */
public abstract class LoadingAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * It doesn't matter if this adapter is empty, only {@link LoadingAdapter#onCreateViewHolder(ViewGroup, int)}
     * and {@link LoadingAdapter#onBindViewHolder(RecyclerView.ViewHolder, int)} will be called.
     */
    @Override
    public int getItemCount() {
        return 0;
    }

    /**
     * By default, there is no binding for loading row. Override this method otherwise.
     */
    @Override
    public void onBindViewHolder(VH holder, int position) {
    }

    public static final LoadingAdapter DEFAULT = new LoadingAdapter() {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.loadingadapter_default, parent, false)) {
            };
        }
    };
}