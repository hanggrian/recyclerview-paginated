package com.hendraanggrian.recyclerview.widget;

import androidx.recyclerview.widget.GridLayoutManager;

final class PaginatedSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private final GridLayoutManager.SpanSizeLookup originalLookup;
    private final GridLayoutManager.SpanSizeLookup fakeLookup;
    private final PaginatedAdapter adapter;

    PaginatedSpanSizeLookup(
        GridLayoutManager.SpanSizeLookup originalLookup,
        GridLayoutManager.SpanSizeLookup fakeLookup,
        PaginatedAdapter adapter
    ) {
        this.originalLookup = originalLookup;
        this.fakeLookup = fakeLookup;
        this.adapter = adapter;
    }

    GridLayoutManager.SpanSizeLookup getOriginalLookup() {
        return originalLookup;
    }

    @Override
    public int getSpanSize(int position) {
        return adapter.isPlaceholderRow(position) || adapter.isErrorRow(position)
            ? fakeLookup.getSpanSize(position)
            : originalLookup.getSpanSize(position);
    }
}