package com.hendraanggrian.recyclerview.widget;

import androidx.recyclerview.widget.GridLayoutManager;

final class PaginatedSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    final GridLayoutManager.SpanSizeLookup originalLookup;
    private final GridLayoutManager.SpanSizeLookup fakeLookup;
    private final PaginatedAdapter adapter;

    PaginatedSpanSizeLookup(
        GridLayoutManager.SpanSizeLookup originalLookup,
        final int spanCount,
        PaginatedAdapter adapter
    ) {
        this.originalLookup = originalLookup;
        this.fakeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return spanCount;
            }
        };
        this.adapter = adapter;
    }

    @Override
    public int getSpanSize(int position) {
        return adapter.isPlaceholderRow(position) || adapter.isErrorRow(position)
            ? fakeLookup.getSpanSize(position)
            : originalLookup.getSpanSize(position);
    }
}