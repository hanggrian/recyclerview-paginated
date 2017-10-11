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
    private var mPaginationAdapter: PaginationAdapter? = null
    private var mPaginationLookup: PaginationSpanSizeLookup? = null
    private val mPaginationOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) = checkEndOffset() // Each time when list is scrolled check if end of the list is reached
    }
    private val mPaginationObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            mPaginationAdapter!!.notifyDataSetChanged()
            notifyPagination()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mPaginationAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
            notifyPagination()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mPaginationAdapter!!.notifyItemRangeChanged(positionStart, itemCount)
            notifyPagination()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            mPaginationAdapter!!.notifyItemRangeChanged(positionStart, itemCount, payload)
            notifyPagination()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mPaginationAdapter!!.notifyItemRangeRemoved(positionStart, itemCount)
            notifyPagination()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mPaginationAdapter!!.notifyItemMoved(fromPosition, toPosition)
            notifyPagination()
        }

        private fun notifyPagination() {
            mPaginationAdapter!!.isDisplaying = !mPagination!!.isFinished
            checkEndOffset()
        }
    }

    private var mLoadingAdapter: LoadingAdapter<RecyclerView.ViewHolder>? = null
    private var mLoadingThreshold: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView, defStyle, 0)
        if (a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, true)) {
            mLoadingAdapter = LoadingAdapter.DEFAULT
        }
        mLoadingThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadingThreshold, 5)
        a.recycle()
    }

    open var pagination: Pagination?
        get() = mPagination
        set(pagination) {
            if (pagination != null) {
                mPagination = pagination
                addOnScrollListener(mPaginationOnScrollListener)
                if (mLoadingAdapter != null) {
                    // Wrap existing adapter with new adapter that will add loading row
                    val mAdapter = adapter
                    mAdapter.registerAdapterDataObserver(mPaginationObserver)
                    mPaginationAdapter = PaginationAdapter(mAdapter, mLoadingAdapter!!)
                    adapter = mPaginationAdapter

                    // For GridLayoutManager use separate/customisable span lookup for loading row
                    if (layoutManager is GridLayoutManager) {
                        mPaginationLookup = PaginationSpanSizeLookup(
                                (layoutManager as GridLayoutManager).spanSizeLookup,
                                object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int): Int = (layoutManager as GridLayoutManager).spanCount
                                },
                                mPaginationAdapter!!)
                        (layoutManager as GridLayoutManager).spanSizeLookup = mPaginationLookup
                    }
                }
                // Trigger initial check since adapter might not have any items initially so no scrolling events upon
                // RecyclerView (that triggers check) will occur
                checkEndOffset()
            } else {
                removeOnScrollListener(mPaginationOnScrollListener)
                if (adapter is PaginationAdapter) {
                    val paginatedAdapter = adapter as PaginationAdapter
                    val adapter = paginatedAdapter.actualAdapter
                    adapter.unregisterAdapterDataObserver(mPaginationObserver)
                    setAdapter(adapter)
                }
                if (layoutManager is GridLayoutManager && mPaginationLookup != null) {
                    (layoutManager as GridLayoutManager).spanSizeLookup = mPaginationLookup!!.originalLookup
                }
                mPagination = null
                mPaginationAdapter = null
                mPaginationLookup = null
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
                pagination = temp
            }
        }

    open var loadingThreshold: Int
        get() = mLoadingThreshold
        set(threshold) {
            mLoadingThreshold = threshold
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
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + mLoadingThreshold || totalItemCount == 0) {
            // Call paginate more only if loading is not currently in progress and if there is more items to paginate
            if (!mPagination!!.isLoading && !mPagination!!.isFinished) {
                mPagination!!.paginate()
            }
        }
    }

    abstract class Pagination {
        /** Returns the initial page of which pagination should start to. */
        open fun getPageStart(): Int = 1

        /** Where the logic of data population should be. Returns true to indicate whether pagination should continue. */
        abstract fun onPaginate(page: Int): Boolean

        private var mPage: Int = getPageStart()
        private var mLoading: Boolean = true
        private var mFinished: Boolean = false

        init {
            paginate()
        }

        internal fun paginate() {
            notifyPopulateStarted()
            mFinished = !onPaginate(mPage++)
        }

        val page: Int get() = mPage
        val isLoading: Boolean get() = mLoading
        val isFinished: Boolean get() = mFinished

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