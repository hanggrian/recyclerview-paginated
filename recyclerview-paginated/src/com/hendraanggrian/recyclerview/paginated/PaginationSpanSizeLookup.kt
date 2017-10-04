package com.hendraanggrian.recyclerview.paginated

import android.support.v7.widget.GridLayoutManager

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class PaginationSpanSizeLookup(
        val originalLookup: GridLayoutManager.SpanSizeLookup,
        private val loadingLookup: LoadingSpanSizeLookup,
        private val paginatedAdapter: PaginationAdapter
) : GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int): Int =
            if (paginatedAdapter.isLoadingRow(position)) loadingLookup.getSpanSize(position)
            else originalLookup.getSpanSize(position)
}