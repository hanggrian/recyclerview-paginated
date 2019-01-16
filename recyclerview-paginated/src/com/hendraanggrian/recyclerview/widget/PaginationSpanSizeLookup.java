package com.hendraanggrian.recyclerview.widget;

import androidx.recyclerview.widget.GridLayoutManager;

final class PaginationSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private final GridLayoutManager.SpanSizeLookup originalLookup;
    private final GridLayoutManager.SpanSizeLookup fakeLookup;
    private final PaginationAdapterWrapper adapterWrapper;

    PaginationSpanSizeLookup(
        GridLayoutManager.SpanSizeLookup originalLookup,
        GridLayoutManager.SpanSizeLookup fakeLookup,
        PaginationAdapterWrapper adapterWrapper
    ) {
        this.originalLookup = originalLookup;
        this.fakeLookup = fakeLookup;
        this.adapterWrapper = adapterWrapper;
    }

    GridLayoutManager.SpanSizeLookup getOriginalLookup() {
        return originalLookup;
    }

    @Override
    public int getSpanSize(int position) {
        return adapterWrapper.isPlaceholderRow(position) || adapterWrapper.isErrorRow(position)
            ? fakeLookup.getSpanSize(position)
            : originalLookup.getSpanSize(position);
    }
}