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
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyDataSetChanged()
            onAdapterDataChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyItemRangeChanged(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyItemRangeChanged(positionStart, itemCount, payload)
            onAdapterDataChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyItemRangeRemoved(positionStart, itemCount)
            onAdapterDataChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            checkNotNull(mPaginationAdapter)
            mPaginationAdapter!!.notifyItemMoved(fromPosition, toPosition)
            onAdapterDataChanged()
        }

        private fun onAdapterDataChanged() {
            checkNotNull(mPaginationAdapter)
            checkNotNull(mPagination)
            mPaginationAdapter!!.isDisplaying = mPagination!!.onPreparePage(mPagination!!.mPage)
            checkEndOffset()
        }
    }

    private var mLoadEnabled: Boolean
    private var mLoadThreshold: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView, defStyle, 0)
        mLoadEnabled = a.getBoolean(R.styleable.PaginatedRecyclerView_loadEnabled, true)
        mLoadThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadThreshold, 5)
        a.recycle()
    }

    open var loadEnabled: Boolean
        get() = mLoadEnabled
        set(enabled) {
            mLoadEnabled = enabled
            if (mPagination != null) {
                val temp = mPagination
                releasePagination()
                mPagination = temp
            }
        }

    open var loadThreshold: Int
        get() = mLoadThreshold
        set(threshold) {
            mLoadThreshold = threshold
        }

    open var pagination: Pagination?
        get() = mPagination
        set(pagination) {
            if (pagination == null) throw UnsupportedOperationException()
            mPagination = pagination
            addOnScrollListener(mPaginationOnScrollListener)
            if (mLoadEnabled) {
                // Wrap existing adapter with new adapter that will add loading row
                val adapter = adapter
                mPaginationAdapter = PaginationAdapter(adapter, mPagination!!.loadingAdapter)
                adapter.registerAdapterDataObserver(mPaginationObserver)
                setAdapter(mPaginationAdapter)

                // For GridLayoutManager use separate/customisable span lookup for loading row
                if (layoutManager is GridLayoutManager) {
                    mPaginationLookup = PaginationSpanSizeLookup(
                            (layoutManager as GridLayoutManager).spanSizeLookup,
                            mPagination!!.getLoadingSpanSizeLookup(layoutManager),
                            mPaginationAdapter!!)
                    (layoutManager as GridLayoutManager).spanSizeLookup = mPaginationLookup
                }
            }
            // Trigger initial check since adapter might not have any items initially so no scrolling events upon
            // RecyclerView (that triggers check) will occur
            checkEndOffset()
        }

    open fun releasePagination() {
        removeOnScrollListener(mPaginationOnScrollListener)
        if (adapter is PaginationAdapter) {
            val paginatedAdapter = adapter as PaginationAdapter
            val adapter = paginatedAdapter.actualAdapter
            adapter.unregisterAdapterDataObserver(mPaginationObserver)
            setAdapter(adapter)
        }
        if (layoutManager is GridLayoutManager && mPaginationLookup != null) {
            val spanSizeLookup = mPaginationLookup!!.originalLookup
            (layoutManager as GridLayoutManager).spanSizeLookup = spanSizeLookup
        }
        mPagination = null
        mPaginationAdapter = null
        mPaginationLookup = null
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
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + mLoadThreshold || totalItemCount == 0) {
            // Call populatePage more only if loading is not currently in progress and if there is more items to populatePage
            checkNotNull(mPagination)
            if (!mPagination!!.mLoading && mPagination!!.onPreparePage(mPagination!!.mPage)) {
                mPagination!!.populatePage()
            }
        }
    }

    abstract class Pagination @JvmOverloads constructor(initialPage: Int = 1, populateOnStart: Boolean = true) {
        internal var mPage: Int = initialPage
        internal var mLoading: Boolean = populateOnStart

        init {
            if (populateOnStart) populatePage()
        }

        internal fun populatePage() {
            notifyPopulateStarted()
            onPopulatePage(mPage++)
        }

        abstract fun onPreparePage(page: Int): Boolean

        abstract fun onPopulatePage(page: Int)

        open val loadingAdapter: LoadingAdapter get() = LoadingAdapter.getDefault()

        open fun getLoadingSpanSizeLookup(layout: RecyclerView.LayoutManager): GridLayoutManager.SpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            val spanSize = (layout as? GridLayoutManager)?.spanCount ?: 1
            override fun getSpanSize(position: Int): Int = spanSize
        }

        fun notifyPopulateStarted() {
            mLoading = true
        }

        fun notifyPopulateCompleted() {
            mLoading = false
        }
    }

    abstract class LoadingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        /** By default, there is no binding for loading row. Override this method otherwise. */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

        /** It doesn't matter if this adapter is empty, only [LoadingAdapter.onCreateViewHolder] and [LoadingAdapter.onBindViewHolder] will be called. */
        override fun getItemCount(): Int = 0

        companion object {
            private var DEFAULT: LoadingAdapter? = null

            internal fun getDefault(): LoadingAdapter {
                if (DEFAULT == null) {
                    DEFAULT = object : LoadingAdapter() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                                object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.paginated_loading_row, parent, false)) {
                                }
                    }
                }
                return DEFAULT!!
            }
        }
    }
}