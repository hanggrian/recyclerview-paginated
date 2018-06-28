package com.hendraanggrian.recyclerview.paginated.internal

import android.support.v7.widget.GridLayoutManager

internal class PaginationSpanSizeLookup(
    val originalLookup: GridLayoutManager.SpanSizeLookup,
    private val loadingLookup: GridLayoutManager.SpanSizeLookup,
    private val paginationAdapterWrapper: PaginationAdapterWrapper
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int = when {
        paginationAdapterWrapper.isLoadingRow(position) -> loadingLookup.getSpanSize(position)
        else -> originalLookup.getSpanSize(position)
    }
}