package com.hendraanggrian.recyclerview.paginated;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class PaginatedSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    @NonNull private final GridLayoutManager.SpanSizeLookup originalLookup;
    @NonNull private final LoadingSpanSizeLookup loadingLookup;
    @NonNull private final PaginatedAdapter paginatedAdapter;

    public PaginatedSpanSizeLookup(
            @NonNull GridLayoutManager.SpanSizeLookup gridSpanSizeLookup,
            @NonNull LoadingSpanSizeLookup loadingLookup,
            @NonNull PaginatedAdapter paginatedAdapter) {
        this.originalLookup = gridSpanSizeLookup;
        this.loadingLookup = loadingLookup;
        this.paginatedAdapter = paginatedAdapter;
    }

    @Override
    public int getSpanSize(int position) {
        if (paginatedAdapter.isLoadingRow(position)) {
            return loadingLookup.getSpanSize(position);
        } else {
            return originalLookup.getSpanSize(position);
        }
    }

    @NonNull
    public GridLayoutManager.SpanSizeLookup getOriginalLookup() {
        return originalLookup;
    }
}