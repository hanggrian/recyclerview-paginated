package com.hendraanggrian.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.hendraanggrian.recyclerview.paginated.LoadingAdapter;
import com.hendraanggrian.recyclerview.paginated.LoadingSpanSizeLookup;
import com.hendraanggrian.recyclerview.paginated.PaginatedAdapter;
import com.hendraanggrian.recyclerview.paginated.PaginatedSpanSizeLookup;
import com.hendraanggrian.recyclerview.paginated.R;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class PaginatedRecyclerView extends RecyclerView {

    private static final int DEFAULT_THRESHOLD = 5;

    private boolean loadingEnabled;
    private int loadingThreshold;

    private Pagination pagination;
    private PaginatedAdapter paginatedAdapter;
    private PaginatedSpanSizeLookup paginatedLookup;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            checkEndOffset(); // Each time when list is scrolled check if end of the list is reached
        }
    };

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            paginatedAdapter.notifyDataSetChanged();
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeInserted(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeChanged(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            paginatedAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            paginatedAdapter.notifyItemMoved(fromPosition, toPosition);
            onAdapterDataChanged();
        }

        private void onAdapterDataChanged() {
            paginatedAdapter.setDisplaying(!pagination.hasLoadedAllItems());
            checkEndOffset();
        }
    };

    public PaginatedRecyclerView(Context context) {
        this(context, null);
    }

    public PaginatedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.paginatedRecyclerViewStyle);
    }

    public PaginatedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView, defStyle, 0);
        loadingEnabled = a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, true);
        loadingThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadingThreshold, DEFAULT_THRESHOLD);
        a.recycle();
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        setPagination(pagination, true);
    }

    public void setPagination(Pagination pagination, boolean startOnLoad) {
        this.pagination = pagination;
        if (startOnLoad) {
            pagination.onLoadMore(pagination.position);
            pagination.position++;
        }

        addOnScrollListener(mOnScrollListener);
        if (loadingEnabled) {
            // Wrap existing adapter with new adapter that will add loading row
            RecyclerView.Adapter adapter = getAdapter();
            paginatedAdapter = new PaginatedAdapter(adapter, pagination.getLoadingAdapter());
            adapter.registerAdapterDataObserver(mDataObserver);
            setAdapter(paginatedAdapter);

            // For GridLayoutManager use separate/customisable span lookup for loading row
            if (getLayoutManager() instanceof GridLayoutManager) {
                paginatedLookup = new PaginatedSpanSizeLookup(
                        ((GridLayoutManager) getLayoutManager()).getSpanSizeLookup(),
                        pagination.getLoadingSpanSizeLookup(getLayoutManager()),
                        paginatedAdapter);
                ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(paginatedLookup);
            }
        }

        // Trigger initial check since adapter might not have any items initially so no scrolling events upon
        // RecyclerView (that triggers check) will occur
        checkEndOffset();
    }

    private void checkEndOffset() {
        int visibleItemCount = getChildCount();
        int totalItemCount = getLayoutManager().getItemCount();

        int firstVisibleItemPosition;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            // https://code.google.com/p/android/issues/detail?id=181461
            if (getLayoutManager().getChildCount() > 0) {
                firstVisibleItemPosition = ((StaggeredGridLayoutManager) getLayoutManager()).findFirstVisibleItemPositions(null)[0];
            } else {
                firstVisibleItemPosition = 0;
            }
        } else {
            throw new IllegalStateException("LayoutManager needs to subclass LinearLayoutManager or StaggeredGridLayoutManager");
        }

        // Check if end of the list is reached (counting threshold) or if there is no items at all
        if ((totalItemCount - visibleItemCount) <= (firstVisibleItemPosition + loadingThreshold) || totalItemCount == 0) {
            // Call load more only if loading is not currently in progress and if there is more items to load
            if (!pagination.isLoading() && !pagination.hasLoadedAllItems()) {
                pagination.onLoadMore(pagination.position);
                pagination.position++;
            }
        }
    }

    public void setHasMoreDataToLoad(boolean hasMoreDataToLoad) {
        if (paginatedAdapter != null) {
            paginatedAdapter.setDisplaying(hasMoreDataToLoad);
        }
    }

    public void unbind() {
        removeOnScrollListener(mOnScrollListener);   // Remove scroll listener
        if (getAdapter() instanceof PaginatedAdapter) {
            PaginatedAdapter paginatedAdapter = (PaginatedAdapter) getAdapter();
            RecyclerView.Adapter adapter = paginatedAdapter.getOriginalAdapter();
            adapter.unregisterAdapterDataObserver(mDataObserver); // Remove data observer
            setAdapter(adapter);                     // Swap back original adapter
        }
        if (getLayoutManager() instanceof GridLayoutManager && paginatedLookup != null) {
            // Swap back original SpanSizeLookup
            GridLayoutManager.SpanSizeLookup spanSizeLookup = paginatedLookup.getOriginalLookup();
            ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(spanSizeLookup);
        }
    }

    public static abstract class Pagination {

        private int position = 1;

        public abstract void onLoadMore(int position);

        public abstract boolean isLoading();

        public abstract boolean hasLoadedAllItems();

        @NonNull
        public LoadingAdapter getLoadingAdapter() {
            return LoadingAdapter.DEFAULT;
        }

        @NonNull
        public LoadingSpanSizeLookup getLoadingSpanSizeLookup(@NonNull LayoutManager lm) {
            return LoadingSpanSizeLookup.getDefault(lm);
        }
    }
}