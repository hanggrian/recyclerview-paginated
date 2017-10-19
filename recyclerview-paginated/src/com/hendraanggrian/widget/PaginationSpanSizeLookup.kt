package com.hendraanggrian.widget

import android.support.v7.widget.GridLayoutManager

internal class PaginationSpanSizeLookup(
        val originalLookup: GridLayoutManager.SpanSizeLookup,
        val loadingLookup: GridLayoutManager.SpanSizeLookup,
        val paginationAdapterWrapper: PaginationAdapterWrapper
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int =
            if (paginationAdapterWrapper.isLoadingRow(position)) loadingLookup.getSpanSize(position)
            else originalLookup.getSpanSize(position)
}