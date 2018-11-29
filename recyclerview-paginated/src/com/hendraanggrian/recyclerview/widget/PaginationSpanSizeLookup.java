package com.hendraanggrian.recyclerview.widget;

import androidx.recyclerview.widget.GridLayoutManager;

final class PaginationSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private final GridLayoutManager.SpanSizeLookup originalLookup;
    private final GridLayoutManager.SpanSizeLookup loadingLookup;
    private final PaginationAdapterWrapper paginationAdapterWrapper;

    PaginationSpanSizeLookup(
        GridLayoutManager.SpanSizeLookup originalLookup,
        GridLayoutManager.SpanSizeLookup loadingLookup,
        PaginationAdapterWrapper paginationAdapterWrapper
    ) {
        this.originalLookup = originalLookup;
        this.loadingLookup = loadingLookup;
        this.paginationAdapterWrapper = paginationAdapterWrapper;
    }

    GridLayoutManager.SpanSizeLookup getOriginalLookup() {
        return originalLookup;
    }

    @Override
    public int getSpanSize(int position) {
        return paginationAdapterWrapper.isPlaceholderRow(position) ||
            paginationAdapterWrapper.isErrorRow(position)
            ? loadingLookup.getSpanSize(position)
            : originalLookup.getSpanSize(position);
    }
}