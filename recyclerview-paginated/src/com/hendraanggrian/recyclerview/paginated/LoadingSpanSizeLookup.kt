package com.hendraanggrian.recyclerview.paginated

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * SpanSizeLookup that will be used to determine the span of loading list item.
 * Only relevant when [android.support.v7.widget.GridLayoutManager] is used.
 *
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
abstract class LoadingSpanSizeLookup : GridLayoutManager.SpanSizeLookup() {

    companion object {
        fun getDefault(lm: RecyclerView.LayoutManager): LoadingSpanSizeLookup = object : LoadingSpanSizeLookup() {
            val spanSize = (lm as? GridLayoutManager)?.spanCount ?: 1
            override fun getSpanSize(position: Int): Int = spanSize
        }
    }
}