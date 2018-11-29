package com.hendraanggrian.recyclerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hendraanggrian.recyclerview.paginated.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class PaginatedRecyclerView extends RecyclerView {

    private final OnScrollListener onScrollListener = new OnScrollListener() {
        // Each time when list is scrolled check if end of the list is reached
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            calculateEndOffset();
        }
    };
    private final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            adapterWrapper.notifyDataSetChanged();
            calculatePagination();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeChanged(
            int positionStart,
            int itemCount,
            @Nullable Object payload
        ) {
            adapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
            calculatePagination();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            adapterWrapper.notifyItemMoved(fromPosition, toPosition);
            calculatePagination();
        }

        private void calculatePagination() {
            adapterWrapper.setDisplaying(!pagination.isFinished);
            calculateEndOffset();
        }
    };

    private Pagination pagination;
    private PaginationAdapterWrapper adapterWrapper;
    private PaginationSpanSizeLookup paginationLookup;

    private LoadingAdapter loadingAdapter;
    private int loadingThreshold;

    public PaginatedRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public PaginatedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaginatedRecyclerView(
        @NonNull Context context,
        @Nullable AttributeSet attrs,
        int defStyle
    ) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView,
            defStyle, 0
        );
        if (a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, true)) {
            loadingAdapter = LoadingAdapter.DEFAULT;
        }
        loadingThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadingThreshold, 5);
        a.recycle();
    }

    @Nullable
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(@Nullable Pagination pagination) {
        final LayoutManager manager = getLayoutManager();
        if (manager == null) {
            throw new IllegalStateException("LayoutManager must be initialized before Pagination!");
        }
        final Adapter adapter = getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("Adapter must be initialized before Pagination!");
        }
        if (pagination != null) {
            this.pagination = pagination;
            pagination.paginate();
            addOnScrollListener(onScrollListener);
            if (loadingAdapter != null) {
                adapter.registerAdapterDataObserver(observer);
                adapterWrapper = new PaginationAdapterWrapper(adapter, loadingAdapter);
                setAdapter(adapterWrapper);

                pagination.setOnFinishLoading(new Runnable() {
                    @Override
                    public void run() {
                        adapterWrapper.setDisplaying(false);
                    }
                });

                // For GridLayoutManager use separate/customisable span lookup for loading row
                if (manager instanceof GridLayoutManager) {
                    paginationLookup = new PaginationSpanSizeLookup(
                        ((GridLayoutManager) manager).getSpanSizeLookup(),
                        new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                return ((GridLayoutManager) manager).getSpanCount();
                            }
                        },
                        adapterWrapper
                    );
                    ((GridLayoutManager) manager).setSpanSizeLookup(paginationLookup);
                }
            }
            // Trigger initial check since adapter might not have any items initially so no scrolling events upon
            // RecyclerView (that triggers check) will occur
            calculateEndOffset();
        } else {
            removeOnScrollListener(onScrollListener);
            if (adapter instanceof PaginationAdapterWrapper) {
                final PaginationAdapterWrapper paginatedAdapter =
                    (PaginationAdapterWrapper) adapter;
                final Adapter actualAdapter = paginatedAdapter.getActualAdapter();
                actualAdapter.unregisterAdapterDataObserver(observer);
                setAdapter(actualAdapter);
            }
            if (manager instanceof GridLayoutManager && paginationLookup != null) {
                ((GridLayoutManager) manager)
                    .setSpanSizeLookup(paginationLookup.getOriginalLookup());
            }
            this.pagination = null;
            adapterWrapper = null;
            paginationLookup = null;
        }
    }

    /**
     * Mimicking {@link #setAdapter} and {@link #getAdapter()}, it sets adapter for loading row.
     * {@link LoadingAdapter#DEFAULT} is used by default.
     */
    @Nullable
    public LoadingAdapter getLoadingAdapter() {
        return loadingAdapter;
    }

    public void setLoadingAdapter(@Nullable LoadingAdapter adapter) {
        loadingAdapter = adapter;
        if (pagination != null) {
            final Pagination temp = pagination;
            pagination = null;
            pagination = temp;
        }
    }

    /**
     * Tells scrolling listener to start to load next page when you have
     * scrolled to n items from last item.
     */
    public int getLoadingThreshold() {
        return loadingThreshold;
    }

    public void setLoadingThreshold(int threshold) {
        loadingThreshold = threshold;
    }

    private void calculateEndOffset() {
        final int firstVisibleItemPosition;
        final LayoutManager manager = getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            firstVisibleItemPosition =
                ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            // https://code.google.com/p/android/issues/detail?id=181461
            firstVisibleItemPosition = manager.getChildCount() > 0
                ? ((StaggeredGridLayoutManager) manager).findFirstVisibleItemPositions(null)[0]
                : 0;
        } else {
            throw new IllegalStateException(
                "LayoutManager needs to subclass LinearLayoutManager or " +
                    "StaggeredGridLayoutManager");
        }
        // Check if end of the list is reached (counting threshold) or if there is no items at all
        final int visibleItemCount = getChildCount();
        final int totalItemCount = getLayoutManager().getItemCount();
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + loadingThreshold || totalItemCount == 0) {
            // Call paginate more only if loading is not currently in progress and if there is more items to paginate
            if (!pagination.isLoading && !pagination.isFinished) {
                pagination.paginate();
            }
        }
    }

    /**
     * Class that controls pagination behavior of {@link RecyclerView},
     * much like {@link Adapter} controlling item view behavior.
     */
    public static abstract class Pagination {
        private int page = getPageStart();
        private boolean isLoading = true;
        private boolean isFinished = false;
        private Runnable onFinishLoading;

        /**
         * Returns the initial page of which pagination should start to.
         */
        public int getPageStart() {
            return 1;
        }

        /**
         * Where the logic of data population should be.
         */
        public abstract void onPaginate(int page);

        void paginate() {
            notifyLoadingStarted();
            onPaginate(page++);
        }

        void setOnFinishLoading(Runnable runnable) {
            onFinishLoading = runnable;
        }

        /**
         * Returns current page of this pagination.
         */
        public int getPage() {
            return page;
        }

        /**
         * Indicates whether or not this pagination is currently loading.
         */
        public boolean isLoading() {
            return isLoading;
        }

        /**
         * Indicated whether or not this pagination has successfully loaded all items.
         */
        public boolean isFinished() {
            return isFinished;
        }

        /**
         * Notify this pagination that a loading has started and should display a loading row.
         */
        public void notifyLoadingStarted() {
            isLoading = true;
        }

        /**
         * Notify this pagination that loading has completed,
         * therefore loading row should be hidden.
         */
        public void notifyLoadingCompleted() {
            isLoading = false;
        }

        /**
         * Notify this pagination that it has successfully loaded all items and
         * should not attempt to load any more.
         */
        public void notifyPaginationFinished() {
            isFinished = true;
            onFinishLoading.run();
        }

        public void notifyPaginationReset() {
            isFinished = false;
            page = getPageStart();
            paginate();
        }
    }

    /**
     * Base loading adapter that will be displayed when pagination is in progress.
     * When extending this class, only {@link LoadingAdapter#onCreateViewHolder} and
     * {@link LoadingAdapter#onBindViewHolder} is relevant and should be implemented.
     */
    public static abstract class LoadingAdapter<VH extends ViewHolder> extends Adapter<VH> {

        /**
         * Default {@link LoadingAdapter}, which is just an indeterminate {@link android.widget.ProgressBar}.
         */
        public static LoadingAdapter<ViewHolder> DEFAULT =
            new LoadingAdapter<ViewHolder>() {
                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new ViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.paginated_loading_row, parent, false)) {
                    };
                }
            };

        /**
         * By default, there is no binding for loading row. Override this method otherwise.
         */
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
        }

        /**
         * It doesn't matter if this adapter is empty,
         * loading adapter is always only displayed as 1 item.
         */
        @Override
        public int getItemCount() {
            return 0;
        }
    }
}