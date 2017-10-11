package com.hendraanggrian.widget

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hendraanggrian.recyclerview.paginated.R

open class PaginatedRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var mPagination: Pagination? = null
    private var mPaginatedAdapter: PaginatedAdapter? = null
    private var mPaginatedLookup: PaginatedSpanSizeLookup? = null
    private val mPaginatedOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) = checkEndOffset() // Each time when list is scrolled check if end of the list is reached
    }
    private val mPaginatedObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyDataSetChanged()
            onAdapterDataChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyItemRangeChanged(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyItemRangeChanged(positionStart, itemCount, payload)
            onAdapterDataChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyItemRangeRemoved(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            checkNotNull(mPaginatedAdapter)
            mPaginatedAdapter!!.notifyItemMoved(fromPosition, toPosition)
            onAdapterDataChanged()
        }

        private fun onAdapterDataChanged() {
            checkNotNull(mPaginatedAdapter)
            checkNotNull(mPagination)
            mPaginatedAdapter!!.isDisplaying = !mPagination!!.isFinished
            checkEndOffset()
        }
    }

    private var mLoadingAdapter: LoadingAdapter<RecyclerView.ViewHolder>? = null
    private var mPaginateThreshold: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView, defStyle, 0)
        if (a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, true)) {
            mLoadingAdapter = LoadingAdapter.DEFAULT
        }
        mPaginateThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_paginateThreshold, 5)
        a.recycle()
    }

    open var pagination: Pagination?
        get() = mPagination
        set(pagination) {
            if (pagination != null) {
                mPagination = pagination
                addOnScrollListener(mPaginatedOnScrollListener)
                if (mLoadingAdapter != null) {
                    // Wrap existing adapter with new adapter that will add loading row
                    val mAdapter = adapter
                    mAdapter.registerAdapterDataObserver(mPaginatedObserver)
                    mPaginatedAdapter = PaginatedAdapter(mAdapter, mLoadingAdapter!!)
                    adapter = mPaginatedAdapter

                    // For GridLayoutManager use separate/customisable span lookup for loading row
                    if (layoutManager is GridLayoutManager) {
                        mPaginatedLookup = PaginatedSpanSizeLookup(
                                (layoutManager as GridLayoutManager).spanSizeLookup,
                                object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int): Int = (layoutManager as GridLayoutManager).spanCount
                                },
                                mPaginatedAdapter!!)
                        (layoutManager as GridLayoutManager).spanSizeLookup = mPaginatedLookup
                    }
                }
                // Trigger initial check since adapter might not have any items initially so no scrolling events upon
                // RecyclerView (that triggers check) will occur
                checkEndOffset()
            } else {
                removeOnScrollListener(mPaginatedOnScrollListener)
                if (adapter is PaginatedAdapter) {
                    val paginatedAdapter = adapter as PaginatedAdapter
                    val adapter = paginatedAdapter.actualAdapter
                    adapter.unregisterAdapterDataObserver(mPaginatedObserver)
                    setAdapter(adapter)
                }
                if (layoutManager is GridLayoutManager && mPaginatedLookup != null) {
                    (layoutManager as GridLayoutManager).spanSizeLookup = mPaginatedLookup!!.originalLookup
                }
                mPagination = null
                mPaginatedAdapter = null
                mPaginatedLookup = null
            }
        }


    @Suppress("UNCHECKED_CAST")
    open var loadingAdapter: LoadingAdapter<out RecyclerView.ViewHolder>?
        get() = mLoadingAdapter
        set(adapter) {
            mLoadingAdapter = adapter as LoadingAdapter<RecyclerView.ViewHolder>
            if (mPagination != null) {
                val temp = mPagination
                pagination = null
                mPagination = temp
            }
        }

    open var paginateThreshold: Int
        get() = mPaginateThreshold
        set(threshold) {
            mPaginateThreshold = threshold
        }

    private fun checkEndOffset() {
        val firstVisibleItemPosition: Int = when (layoutManager) {
            is LinearLayoutManager -> (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> if (layoutManager.childCount > 0) (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)[0] else 0 // https://code.google.com/p/android/issues/detail?id=181461
            else -> error("LayoutManager needs to subclass LinearLayoutManager or StaggeredGridLayoutManager")
        }
        // Check if end of the list is reached (counting threshold) or if there is no items at all
        val visibleItemCount = childCount
        val totalItemCount = layoutManager.itemCount
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + mPaginateThreshold || totalItemCount == 0) {
            // Call paginate more only if loading is not currently in progress and if there is more items to paginate
            checkNotNull(mPagination)
            if (!mPagination!!.isLoading && !mPagination!!.isFinished) {
                mPagination!!.paginate()
            }
        }
    }

    abstract class Pagination {
        private var mPage: Int = getPageStart()
        private var mLoading: Boolean = true
        private var mFinished: Boolean = false

        init {
            paginate()
        }

        val page: Int get() = mPage
        val isLoading: Boolean get() = mLoading
        val isFinished: Boolean get() = mFinished

        /** Returns the initial page of which pagination should start to. */
        open fun getPageStart(): Int = 1

        /** Where the logic of data population should be. Returns true to indicate whether pagination should continue. */
        abstract fun onPaginate(page: Int): Boolean

        internal fun paginate() {
            notifyPopulateStarted()
            mFinished = !onPaginate(mPage++)
        }

        fun notifyPopulateStarted() {
            mLoading = true
        }

        fun notifyPopulateCompleted() {
            mLoading = false
        }
    }

    abstract class LoadingAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
        /** By default, there is no binding for loading row. Override this method otherwise. */
        override fun onBindViewHolder(holder: VH, position: Int) {}

        /** It doesn't matter if this adapter is empty, only [LoadingAdapter.onCreateViewHolder] and [LoadingAdapter.onBindViewHolder] will be called. */
        override fun getItemCount(): Int = 0

        companion object {
            val DEFAULT = object : LoadingAdapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                        object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.paginated_loading_row, parent, false)) {
                        }
            }
        }
    }
}