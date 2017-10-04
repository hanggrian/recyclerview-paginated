package com.hendraanggrian.recyclerview.paginated

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class PaginationAdapter(
        val actualAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        val loadingAdapter: LoadingAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM_VIEW_TYPE_LOADING = Integer.MAX_VALUE - 50 // Magic
    }

    internal var isDisplaying = true
        set(value) {
            if (isDisplaying != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == ITEM_VIEW_TYPE_LOADING) loadingAdapter.onCreateViewHolder(parent, viewType)
            else actualAdapter.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit =
            if (isLoadingRow(position)) loadingAdapter.onBindViewHolder(holder, position)
            else actualAdapter.onBindViewHolder(holder, position)

    override fun getItemCount(): Int = if (isDisplaying) actualAdapter.itemCount + 1 else actualAdapter.itemCount

    override fun getItemViewType(position: Int): Int = if (isLoadingRow(position)) ITEM_VIEW_TYPE_LOADING else actualAdapter.getItemViewType(position)

    override fun getItemId(position: Int): Long = if (isLoadingRow(position)) RecyclerView.NO_ID else actualAdapter.getItemId(position)

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        actualAdapter.setHasStableIds(hasStableIds)
    }

    internal fun isLoadingRow(position: Int): Boolean = isDisplaying && position == loadingRowPosition

    private val loadingRowPosition: Int get() = if (isDisplaying) itemCount - 1 else -1
}