package com.hendraanggrian.widget

import android.support.v7.widget.GridLayoutManager

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
internal class PaginatedSpanSizeLookup(
        val originalLookup: GridLayoutManager.SpanSizeLookup,
        val loadingLookup: GridLayoutManager.SpanSizeLookup,
        val paginatedAdapter: PaginatedAdapter
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int = when {
        paginatedAdapter.isLoadingRow(position) -> loadingLookup.getSpanSize(position)
        else -> originalLookup.getSpanSize(position)
    }
}