package com.hendraanggrian.widget

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import com.hendraanggrian.recyclerview.paginated.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class PaginatedRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = R.attr.paginatedRecyclerViewStyle
) : RecyclerView(context, attrs, defStyle) {

    private var mPaginationAdapter: PaginationAdapter? = null
    private var mPaginationLookup: PaginationSpanSizeLookup? = null
    private val mPaginationOnScrollListener = object : RecyclerView.OnScrollListener() {
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
            checkNotNull(pagination)
            mPaginationAdapter!!.isDisplaying = !pagination!!.isFinished(pagination!!.currentPage)
            checkEndOffset()
        }
    }

    var pagination: Pagination? = null
        set(value) {
            if (value == null) throw UnsupportedOperationException()
            field = value
            if (loadOnStart) {
                field!!.onLoadMore(field!!.currentPage++)
            }
            addOnScrollListener(mPaginationOnScrollListener)
            if (loadingEnabled) {
                // Wrap existing adapter with new adapter that will add loading row
                val adapter = adapter
                mPaginationAdapter = PaginationAdapter(adapter, field!!.loadingAdapter)
                adapter.registerAdapterDataObserver(mPaginationObserver)
                setAdapter(mPaginationAdapter)

                // For GridLayoutManager use separate/customisable span lookup for loading row
                if (layoutManager is GridLayoutManager) {
                    mPaginationLookup = PaginationSpanSizeLookup(
                            (layoutManager as GridLayoutManager).spanSizeLookup,
                            field!!.getLoadingSpanSizeLookup(layoutManager),
                            mPaginationAdapter!!)
                    (layoutManager as GridLayoutManager).spanSizeLookup = mPaginationLookup
                }
            }
            // Trigger initial check since adapter might not have any items initially so no scrolling events upon
            // RecyclerView (that triggers check) will occur
            checkEndOffset()
        }

    var loadingEnabled: Boolean = false
        set(value) {
            field = value
            if (pagination != null) {
                val temp = pagination
                releasePagination()
                pagination = temp
            }
        }
    var loadingThreshold: Int
    var loadOnStart: Boolean

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PaginatedRecyclerView, defStyle, 0)
        loadingEnabled = a.getBoolean(R.styleable.PaginatedRecyclerView_loadingEnabled, true)
        loadingThreshold = a.getInteger(R.styleable.PaginatedRecyclerView_loadingThreshold, 5)
        loadOnStart = a.getBoolean(R.styleable.PaginatedRecyclerView_loadOnStart, true)
        a.recycle()
    }

    fun releasePagination() {
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
        pagination = null
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
        if (totalItemCount - visibleItemCount <= firstVisibleItemPosition + loadingThreshold || totalItemCount == 0) {
            // Call load more only if loading is not currently in progress and if there is more items to load
            checkNotNull(pagination)
            if (!pagination!!.isLoading(pagination!!.currentPage) && !pagination!!.isFinished(pagination!!.currentPage)) {
                pagination!!.onLoadMore(pagination!!.currentPage++)
            }
        }
    }

    fun setHasMoreDataToLoad(hasMoreDataToLoad: Boolean) {
        mPaginationAdapter?.isDisplaying = hasMoreDataToLoad
    }

    abstract class Pagination @JvmOverloads constructor(initialPage: Int = 1) {
        internal var currentPage: Int = initialPage

        abstract fun onLoadMore(page: Int)
        abstract fun isLoading(page: Int): Boolean
        abstract fun isFinished(page: Int): Boolean

        open val loadingAdapter: LoadingAdapter get() = LoadingAdapter.DEFAULT
        open fun getLoadingSpanSizeLookup(lm: RecyclerView.LayoutManager): LoadingSpanSizeLookup = LoadingSpanSizeLookup.getDefault(lm)
    }
}