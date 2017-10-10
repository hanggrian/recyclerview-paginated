package com.hendraanggrian.recyclerview.paginated

import android.support.v7.widget.GridLayoutManager

inline val GridLayoutManager.loadingSizeSpanLookup: GridLayoutManager.SpanSizeLookup
    get() = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int = spanCount
    }