package com.hendraanggrian.widget

import android.support.v7.widget.GridLayoutManager

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
internal class PaginationSpanSizeLookup(
        val originalLookup: GridLayoutManager.SpanSizeLookup,
        val loadingLookup: GridLayoutManager.SpanSizeLookup,
        val paginatedAdapter: PaginationAdapter
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int =
            if (paginatedAdapter.isLoadingRow(position)) loadingLookup.getSpanSize(position)
            else originalLookup.getSpanSize(position)
}