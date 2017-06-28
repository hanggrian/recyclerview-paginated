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
import com.hendraanggrian.recyclerview.paginated.PaginationAdapter;
import com.hendraanggrian.recyclerview.paginated.PaginationSpanSizeLookup;
import com.hendraanggrian.recyclerview.paginated.R;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class PaginatedRecyclerView extends RecyclerView {

    private static final boolean DEFAULT_LOADING_ENABLED = true;
    private static final int DEFAULT_LOADING_THRESHOLD = 5;
    private static final int DEFAULT_INITIAL_PAGE = 1;
    private static final boolean DEFAULT_SHOULD_START_ON_LOAD = true;

    private boolean loadingEnabled;
    private int loadingThreshold;
    private int currentPage;
    private boolean shouldStartOnLoad;

    @Nullable private Pagination pagination;
    @Nullable private PaginationAdapter paginationAdapter;
    @Nullable private PaginationSpanSizeLookup paginationLookup;
    @NonNull private final RecyclerView.OnScrollListener paginationOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            checkEndOffset(); // Each time when list is scrolled check if end of the list is reached
        }
    };
    @NonNull private final RecyclerView.AdapterDataObserver paginationObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyDataSetChanged();
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyItemRangeInserted(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyItemRangeChanged(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            onAdapterDataChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (paginationAdapter == null) throw new NullPointerException();
            paginationAdapter.notifyItemMoved(fromPosition, toPosition);
            onAdapterDataChanged();
        }

        private void onAdapterDataChanged() {
            if (paginationAdapter == null || pagination == null) throw new NullPointerException();
            paginationAdapter.setDisplaying(!pagination.isFinished(currentPage));
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
        loadingEnabled = a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, DEFAULT_LOADING_ENABLED);
        loadingThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadingThreshold, DEFAULT_LOADING_THRESHOLD);
        currentPage = a.getInteger(R.styleable.PaginatedRecyclerView_initialPage, DEFAULT_INITIAL_PAGE);
        shouldStartOnLoad = a.getBoolean(R.styleable.PaginatedRecyclerView_shouldStartOnLoad, DEFAULT_SHOULD_START_ON_LOAD);
        a.recycle();
    }

    public boolean isLoadingEnabled() {
        return loadingEnabled;
    }

    public void setLoadingEnabled(boolean loadingEnabled) {
        this.loadingEnabled = loadingEnabled;
        if (pagination != null) {
            Pagination temp = pagination;
            releasePagination();
            setPagination(temp);
        }
    }

    public int getLoadingThreshold() {
        return loadingThreshold;
    }

    public void setLoadingThreshold(int loadingThreshold) {
        this.loadingThreshold = loadingThreshold;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isShouldStartOnLoad() {
        return shouldStartOnLoad;
    }

    public void setShouldStartOnLoad(boolean shouldStartOnLoad) {
        this.shouldStartOnLoad = shouldStartOnLoad;
    }

    @Nullable
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(@Nullable Pagination pagination) {
        if (pagination == null) throw new NullPointerException();
        this.pagination = pagination;
        if (shouldStartOnLoad) {
            pagination.onLoadMore(currentPage++);
        }

        addOnScrollListener(paginationOnScrollListener);
        if (loadingEnabled) {
            // Wrap existing adapter with new adapter that will add loading row
            RecyclerView.Adapter adapter = getAdapter();
            paginationAdapter = new PaginationAdapter(adapter, pagination.getLoadingAdapter());
            adapter.registerAdapterDataObserver(paginationObserver);
            setAdapter(paginationAdapter);

            // For GridLayoutManager use separate/customisable span lookup for loading row
            if (getLayoutManager() instanceof GridLayoutManager) {
                paginationLookup = new PaginationSpanSizeLookup(
                        ((GridLayoutManager) getLayoutManager()).getSpanSizeLookup(),
                        pagination.getLoadingSpanSizeLookup(getLayoutManager()),
                        paginationAdapter);
                ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(paginationLookup);
            }
        }

        // Trigger initial check since adapter might not have any items initially so no scrolling events upon
        // RecyclerView (that triggers check) will occur
        checkEndOffset();
    }

    public void releasePagination() {
        removeOnScrollListener(paginationOnScrollListener);
        if (getAdapter() instanceof PaginationAdapter) {
            PaginationAdapter paginatedAdapter = (PaginationAdapter) getAdapter();
            RecyclerView.Adapter adapter = paginatedAdapter.getOriginalAdapter();
            adapter.unregisterAdapterDataObserver(paginationObserver);
            setAdapter(adapter);
        }
        if (getLayoutManager() instanceof GridLayoutManager && paginationLookup != null) {
            GridLayoutManager.SpanSizeLookup spanSizeLookup = paginationLookup.getOriginalLookup();
            ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(spanSizeLookup);
        }
        pagination = null;
    }

    private void checkEndOffset() {
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
        int visibleItemCount = getChildCount();
        int totalItemCount = getLayoutManager().getItemCount();
        if ((totalItemCount - visibleItemCount) <= (firstVisibleItemPosition + loadingThreshold) || totalItemCount == 0) {
            // Call load more only if loading is not currently in progress and if there is more items to load
            if (pagination == null) throw new NullPointerException();
            if (!pagination.isLoading() && !pagination.isFinished(currentPage)) {
                pagination.onLoadMore(currentPage++);
            }
        }
    }

    public void setHasMoreDataToLoad(boolean hasMoreDataToLoad) {
        if (paginationAdapter != null) {
            paginationAdapter.setDisplaying(hasMoreDataToLoad);
        }
    }

    public static abstract class Pagination {

        public abstract void onLoadMore(int page);

        public abstract boolean isLoading();

        public abstract boolean isFinished(int page);

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