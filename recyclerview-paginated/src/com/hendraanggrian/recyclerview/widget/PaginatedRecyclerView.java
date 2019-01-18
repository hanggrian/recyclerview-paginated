package com.hendraanggrian.recyclerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hendraanggrian.recyclerview.paginated.R;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class PaginatedRecyclerView extends RecyclerView {

    // Cache for placeholder and error adapters
    private static final ThreadLocal<Map<String, Constructor<BaseAdapter>>> ADAPTER_CONSTRUCTORS =
        new ThreadLocal<>();

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
            paginatedAdapter.notifyDataSetChanged();
            calculatePagination();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeInserted(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeChanged(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeChanged(
            int positionStart,
            int itemCount,
            @Nullable Object payload
        ) {
            paginatedAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            calculatePagination();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            paginatedAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            calculatePagination();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            paginatedAdapter.notifyItemMoved(fromPosition, toPosition);
            calculatePagination();
        }

        private void calculatePagination() {
            paginatedAdapter.updateState(PaginationState.LOADING);
            calculateEndOffset();
        }
    };

    private Pagination pagination;
    private PaginatedAdapter paginatedAdapter;
    private PaginatedSpanSizeLookup spanSizeLookup;

    private PlaceholderAdapter placeholderAdapter;
    private ErrorAdapter errorAdapter;
    private int threshold = 3;

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
        final TypedArray a = context.obtainStyledAttributes(
            attrs, R.styleable.PaginatedRecyclerView, defStyle, 0);
        if (a.hasValue(R.styleable.PaginatedRecyclerView_placeholderAdapter)) {
            placeholderAdapter = (PlaceholderAdapter) parseAdapter(
                context, a.getString(R.styleable.PaginatedRecyclerView_placeholderAdapter));
        }
        if (a.hasValue(R.styleable.PaginatedRecyclerView_errorAdapter)) {
            errorAdapter = (ErrorAdapter) parseAdapter(
                context, a.getString(R.styleable.PaginatedRecyclerView_errorAdapter));
        }
        setThreshold(a.getInteger(R.styleable.PaginatedRecyclerView_paginationThreshold, 0));
        a.recycle();
    }

    @Nullable
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(@Nullable Pagination pagination) {
        if (getLayoutManager() == null) {
            throw new IllegalStateException("LayoutManager must be initialized");
        }
        if (getAdapter() == null) {
            throw new IllegalStateException("Adapter must be initialized");
        }
        this.pagination = pagination;
        if (pagination != null) {
            // Trigger initial check since adapter might not have any items initially so no scrolling events upon
            // RecyclerView (that triggers check) will occur
            pagination.paginate();

            addOnScrollListener(onScrollListener);

            getAdapter().registerAdapterDataObserver(observer);
            paginatedAdapter = new PaginatedAdapter(getAdapter());
            setAdapter(paginatedAdapter);

            pagination.setOnCompleted(new Runnable() {
                @Override
                public void run() {
                    paginatedAdapter.updateState(PaginationState.COMPLETE);
                }
            });
            pagination.setOnError(new Runnable() {
                @Override
                public void run() {
                    paginatedAdapter.updateState(PaginationState.ERROR);
                }
            });

            // For GridLayoutManager use separate/customisable span lookup for loading row
            if (getLayoutManager() instanceof GridLayoutManager) {
                final int spanCount = ((GridLayoutManager) getLayoutManager()).getSpanCount();
                spanSizeLookup = new PaginatedSpanSizeLookup(
                    ((GridLayoutManager) getLayoutManager()).getSpanSizeLookup(),
                    new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return spanCount;
                        }
                    },
                    paginatedAdapter
                );
                ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(spanSizeLookup);
            }
        } else {
            removeOnScrollListener(onScrollListener);
            if (getAdapter() instanceof PaginatedAdapter) {
                final PaginatedAdapter paginatedAdapter =
                    (PaginatedAdapter) getAdapter();
                final Adapter originalAdapter = paginatedAdapter.originalAdapter;
                originalAdapter.unregisterAdapterDataObserver(observer);
                setAdapter(originalAdapter);
            }
            if (getLayoutManager() instanceof GridLayoutManager && spanSizeLookup != null) {
                ((GridLayoutManager) getLayoutManager())
                    .setSpanSizeLookup(spanSizeLookup.originalLookup);
            }
            paginatedAdapter = null;
            spanSizeLookup = null;
        }
    }

    @Nullable
    public PlaceholderAdapter getPlaceholderAdapter() {
        return placeholderAdapter;
    }

    public void setPlaceholderAdapter(@Nullable PlaceholderAdapter adapter) {
        if (placeholderAdapter != adapter) {
            placeholderAdapter = adapter;
            forceResetPagination();
        }
    }

    @Nullable
    public ErrorAdapter getErrorAdapter() {
        return errorAdapter;
    }

    public void setErrorAdapter(@Nullable ErrorAdapter adapter) {
        if (errorAdapter != adapter) {
            errorAdapter = adapter;
            forceResetPagination();
        }
    }

    /**
     * Tells scrolling listener to start to load next page when you have
     * scrolled to n items from last item.
     *
     * @return threshold, always larger than 0
     */
    @IntRange(from = 1, to = Integer.MAX_VALUE)
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(@IntRange(from = 1, to = Integer.MAX_VALUE) int threshold) {
        if (threshold > 0) {
            this.threshold = threshold;
        }
    }

    private void forceResetPagination() {
        if (pagination != null) {
            final Pagination copy = pagination;
            setPagination(null);
            setPagination(copy);
        }
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
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + threshold ||
            totalItemCount == 0) {
            // Call paginate more only if loading is not currently in progress and if there is more items to paginate
            if (pagination.state == PaginationState.LOADED) {
                pagination.paginate();
            }
        }
    }

    /**
     * Stolen from {@code CoordinatorLayout.parseBehavior(Context, AttributeSet, String)}.
     */
    @SuppressWarnings("unchecked")
    private static BaseAdapter parseAdapter(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        final String fullName;
        if (name.startsWith(".")) {
            // Relative to the app package. Prepend the app package name.
            fullName = context.getPackageName() + name;
        } else if (name.indexOf('.') >= 0) {
            // Fully qualified package name.
            fullName = name;
        } else {
            // Assume stock behavior in this package.
            fullName = "com.hendraanggrian.recyclerview.widget.PaginatedRecyclerview." + name;
        }
        try {
            Map<String, Constructor<BaseAdapter>> constructors = ADAPTER_CONSTRUCTORS.get();
            if (constructors == null) {
                constructors = new HashMap<>();
                ADAPTER_CONSTRUCTORS.set(constructors);
            }
            Constructor<BaseAdapter> c = constructors.get(fullName);
            if (c == null) {
                final Class<BaseAdapter> clazz = (Class<BaseAdapter>) context.getClassLoader()
                    .loadClass(fullName);
                c = clazz.getConstructor();
                c.setAccessible(true);
                constructors.put(fullName, c);
            }
            return c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not inflate adapter subclass " + fullName, e);
        }
    }

    /**
     * Class that controls pagination behavior of {@link RecyclerView},
     * much like {@link androidx.recyclerview.widget.RecyclerView.Adapter} controlling item view behavior.
     */
    public static abstract class Pagination {
        private int page = getPageStart();
        private PaginationState state;
        private Runnable onCompleted;
        private Runnable onError;

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
            state = PaginationState.LOADING;
            onPaginate(page);
        }

        void setOnCompleted(Runnable onCompleted) {
            this.onCompleted = onCompleted;
        }

        void setOnError(Runnable onError) {
            this.onError = onError;
        }

        /**
         * Returns current page of this pagination.
         */
        public final int getPage() {
            return page;
        }

        /**
         * Returns the current state of this pagination.
         */
        @NonNull
        public final PaginationState getState() {
            return state;
        }

        /**
         * Notify this pagination that loading has completed,
         * therefore loading row should be hidden.
         */
        public final void notifyPageLoaded() {
            if (state == PaginationState.LOADING) {
                state = PaginationState.LOADED;
                page++;
            }
        }

        /**
         * Notify this pagination that an error has occured while loading page,
         * therefore stopping pagination.
         */
        public final void notifyPageError() {
            state = PaginationState.ERROR;
            page--;
            onError.run();
        }

        /**
         * Notify this pagination that it has successfully loaded all items and
         * should not attempt to load any more.
         */
        public final void notifyPaginationCompleted() {
            state = PaginationState.COMPLETE;
            onCompleted.run();
        }

        /**
         * Notify that this pagination should start from the beginning.
         */
        public final void notifyPaginationRestart() {
            page = getPageStart();
            paginate();
        }
    }

    public enum PaginationState {
        LOADING, LOADED, ERROR, COMPLETE
    }

    /**
     * Base loading adapter that will be displayed when pagination is in progress.
     * When extending this class, only {@link androidx.recyclerview.widget.RecyclerView.Adapter#onCreateViewHolder} and
     * {@link BaseAdapter#onBindViewHolder} is relevant and should be implemented.
     */
    public static abstract class BaseAdapter<VH extends ViewHolder> extends Adapter<VH> {

        /**
         * It doesn't matter if this adapter is empty,
         * placeholder and error adapter is always only displayed as 1 item.
         */
        @Override
        public int getItemCount() {
            return 0;
        }

        /**
         * By default, there is no binding for placeholder row, override this method otherwise.
         */
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
        }
    }

    public static abstract class PlaceholderAdapter<VH extends ViewHolder> extends BaseAdapter<VH> {

        /**
         * Default placeholder adapter, which is just an indeterminate progress bar.
         */
        public static final PlaceholderAdapter DEFAULT = new PlaceholderAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.recyclerview_paginated_placeholder, parent, false)) {
                };
            }
        };
    }

    public static abstract class ErrorAdapter<VH extends ViewHolder> extends BaseAdapter<VH> {

        /**
         * Default placeholder adapter, which is just an error text.
         */
        public static final ErrorAdapter DEFAULT = new ErrorAdapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.recyclerview_paginated_error, parent, false)) {
                };
            }
        };
    }
}