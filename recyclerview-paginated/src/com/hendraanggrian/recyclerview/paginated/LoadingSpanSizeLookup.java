package com.hendraanggrian.recyclerview.paginated;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * SpanSizeLookup that will be used to determine the span of loading list item.
 * Only relevant when {@link android.support.v7.widget.GridLayoutManager} is used.
 *
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public abstract class LoadingSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    @NonNull
    public static LoadingSpanSizeLookup getDefault(@NonNull RecyclerView.LayoutManager lm) {
        final int spanSize = lm instanceof GridLayoutManager ? ((GridLayoutManager) lm).getSpanCount() : 1;
        return new LoadingSpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return spanSize;
            }
        };
    }
}