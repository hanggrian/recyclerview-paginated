package com.hendraanggrian.recyclerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.hendraanggrian.recyclerview.paginated.R;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class PaginatedRecyclerView extends RecyclerView {

    private static final ThreadLocal<Map<String, Constructor<PlaceholderAdapter>>>
        PLACEHOLDER_CONSTRUCTORS = new ThreadLocal<>();

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
            Log.d("AAAAA", "calculatePagination()");
            adapterWrapper.setPlaceholder(!pagination.isCompleted);
            calculateEndOffset();
        }
    };

    private Pagination pagination;
    private PaginationAdapterWrapper adapterWrapper;
    private PaginationSpanSizeLookup spanSizeLookup;

    private PlaceholderAdapter placeholderAdapter;
    private ErrorAdapter errorAdapter;
    private int threshold;

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
            placeholderAdapter = parseAdapter(
                context,
                a.getString(R.styleable.PaginatedRecyclerView_placeholderAdapter)
            );
        }
        threshold = a.getInteger(R.styleable.PaginatedRecyclerView_paginationThreshold, 5);
        a.recycle();
    }

    @Nullable
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(@Nullable Pagination pagination) {
        if (getLayoutManager() == null) {
            throw new IllegalStateException("LayoutManager must be initialized before Pagination!");
        }
        if (getAdapter() == null) {
            throw new IllegalStateException("Adapter must be initialized before Pagination!");
        }
        if (pagination != null) {
            this.pagination = pagination;
            pagination.paginate();
            addOnScrollListener(onScrollListener);

            getAdapter().registerAdapterDataObserver(observer);
            adapterWrapper = new PaginationAdapterWrapper(
                getAdapter(), getPlaceholderAdapter(), getErrorAdapter());
            setAdapter(adapterWrapper);

            pagination.setOnCompleted(new Runnable() {
                @Override
                public void run() {
                    adapterWrapper.setPlaceholder(false);
                    adapterWrapper.setError(false);
                }
            });
            pagination.setOnError(new Runnable() {
                @Override
                public void run() {
                    adapterWrapper.setError(true);
                }
            });

            // For GridLayoutManager use separate/customisable span lookup for loading row
            if (getLayoutManager() instanceof GridLayoutManager) {
                spanSizeLookup = new PaginationSpanSizeLookup(
                    ((GridLayoutManager) getLayoutManager()).getSpanSizeLookup(),
                    new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return ((GridLayoutManager) getLayoutManager()).getSpanCount();
                        }
                    },
                    adapterWrapper
                );
                ((GridLayoutManager) getLayoutManager()).setSpanSizeLookup(spanSizeLookup);
            }

            // Trigger initial check since adapter might not have any items initially so no scrolling events upon
            // RecyclerView (that triggers check) will occur
            calculateEndOffset();
        } else {
            removeOnScrollListener(onScrollListener);
            if (getAdapter() instanceof PaginationAdapterWrapper) {
                final PaginationAdapterWrapper paginatedAdapter =
                    (PaginationAdapterWrapper) getAdapter();
                final Adapter actualAdapter = paginatedAdapter.getActualAdapter();
                actualAdapter.unregisterAdapterDataObserver(observer);
                setAdapter(actualAdapter);
            }
            if (getLayoutManager() instanceof GridLayoutManager && spanSizeLookup != null) {
                ((GridLayoutManager) getLayoutManager())
                    .setSpanSizeLookup(spanSizeLookup.getOriginalLookup());
            }
            this.pagination = null;
            adapterWrapper = null;
            spanSizeLookup = null;
        }
    }

    /**
     * Mimicking {@link #setAdapter} and {@link #getAdapter()}, it sets adapter for loading row.
     * {@link PlaceholderAdapter#DEFAULT} is used by default.
     */
    @NonNull
    public PlaceholderAdapter getPlaceholderAdapter() {
        return placeholderAdapter != null
            ? placeholderAdapter
            : PlaceholderAdapter.DEFAULT;
    }

    public void setPlaceholderAdapter(@NonNull PlaceholderAdapter adapter) {
        if (placeholderAdapter != adapter) {
            placeholderAdapter = adapter;
            forceResetPagination();
        }
    }

    @NonNull
    public ErrorAdapter getErrorAdapter() {
        return errorAdapter != null
            ? errorAdapter
            : ErrorAdapter.DEFAULT;
    }

    public void setErrorAdapter(@NonNull ErrorAdapter adapter) {
        if (errorAdapter != adapter) {
            errorAdapter = adapter;
            forceResetPagination();
        }
    }

    /**
     * Tells scrolling listener to start to load next page when you have
     * scrolled to n items from last item.
     */
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
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
            firstVisibleItemPosition = ((LinearLayoutManager) manager)
                .findFirstVisibleItemPosition();
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
            if (!pagination.isPlaceholder && !pagination.isError && !pagination.isCompleted) {
                pagination.paginate();
            }
        }
    }

    /**
     * Stolen from {@code CoordinatorLayout.parseBehavior(Context, AttributeSet, String)}.
     */
    @SuppressWarnings("unchecked")
    private static PlaceholderAdapter parseAdapter(Context context, String name) {
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
            fullName = "com.hendraanggrian.recyclerview.widget.PaginatedRecyclerview" + name;
        }
        try {
            Map<String, Constructor<PlaceholderAdapter>> constructors =
                PLACEHOLDER_CONSTRUCTORS.get();
            if (constructors == null) {
                constructors = new HashMap<>();
                PLACEHOLDER_CONSTRUCTORS.set(constructors);
            }
            Constructor<PlaceholderAdapter> c = constructors.get(fullName);
            if (c == null) {
                final Class<PlaceholderAdapter> clazz = (Class<PlaceholderAdapter>) Class
                    .forName(fullName, true, context.getClassLoader());
                c = clazz.getConstructor();
                constructors.put(fullName, c);
            }
            return c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                "Could not inflate placeholder adapter subclass " + fullName, e);
        }
    }

    /**
     * Class that controls pagination behavior of {@link RecyclerView},
     * much like {@link Adapter} controlling item view behavior.
     */
    public static abstract class Pagination {
        private int page = getPageStart();
        private boolean isPlaceholder = true;
        private boolean isError = false;
        private boolean isCompleted = false;
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
            isPlaceholder = true;
            isError = false;
            onPaginate(page++);
        }

        void setOnCompleted(Runnable runnable) {
            onCompleted = runnable;
        }

        void setOnError(Runnable runnable) {
            onError = runnable;
        }

        /**
         * Returns current page of this pagination.
         */
        public final int getPage() {
            return page;
        }

        /**
         * Indicates whether or not this pagination is currently loading.
         */
        public final boolean isPlaceholder() {
            return isPlaceholder;
        }

        public final boolean isError() {
            return isError;
        }

        /**
         * Indicated whether or not this pagination has successfully loaded all items.
         */
        public final boolean isCompleted() {
            return isCompleted;
        }

        /**
         * Notify this pagination that loading has completed,
         * therefore loading row should be hidden.
         */
        public final void notifyPageLoaded() {
            isPlaceholder = false;
        }

        public final void notifyPageError() {
            isError = true;
            onError.run();
        }

        /**
         * Notify this pagination that it has successfully loaded all items and
         * should not attempt to load any more.
         */
        public final void notifyPaginationCompleted() {
            isCompleted = true;
            onCompleted.run();
        }

        public final void notifyPaginationRestart() {
            isCompleted = false;
            page = getPageStart();
            paginate();
        }
    }

    public static abstract class NonBindingAdapter<VH extends ViewHolder> extends Adapter<VH> {


        /**
         * It doesn't matter if this adapter is empty,
         * placeholder and error adapter is always only displayed as 1 item.
         */
        @Override
        public int getItemCount() {
            return 0;
        }
    }

    /**
     * Base loading adapter that will be displayed when pagination is in progress.
     * When extending this class, only {@link PlaceholderAdapter#onCreateViewHolder} and
     * {@link PlaceholderAdapter#onBindViewHolder} is relevant and should be implemented.
     */
    public static abstract class PlaceholderAdapter<VH extends ViewHolder> extends NonBindingAdapter<VH> {

        /**
         * By default, there is no binding for placeholder row, override this method otherwise.
         */
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
        }

        /**
         * Default placeholder adapter, which is just an indeterminate progress bar.
         */
        public static final PlaceholderAdapter<ViewHolder> DEFAULT =
            new PlaceholderAdapter<ViewHolder>() {
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

    public static abstract class ErrorAdapter<VH extends ViewHolder> extends NonBindingAdapter<VH> {

        @Override
        public void onBindViewHolder(@NonNull final VH holder, int position) {
            if (!(holder instanceof ErrorViewHolder)) {
                throw new RuntimeException("ErrorAdapter must pass ErrorViewHolder generic");
            }
            ((ErrorViewHolder) holder).retryButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(((ErrorViewHolder) holder).retryButton.getContext(), "Clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public static final ErrorAdapter<ViewHolder> DEFAULT =
            new ErrorAdapter<ViewHolder>() {
                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new ErrorViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.recyclerview_paginated_error, parent, false));
                }
            };
    }

    public static class ErrorViewHolder extends ViewHolder {
        public final Button retryButton;

        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            retryButton = itemView.findViewById(android.R.id.button1);
        }
    }
}