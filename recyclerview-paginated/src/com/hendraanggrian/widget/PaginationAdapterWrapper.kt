package com.hendraanggrian.widget

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

internal class PaginationAdapterWrapper(
        val actualAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        val loadingAdapter: PaginatedRecyclerView.LoadingAdapter<RecyclerView.ViewHolder>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_LOADING: Int = Integer.MAX_VALUE - 50 // Magic
    }

    var isDisplaying: Boolean = true
        set(displaying) {
            if (field != displaying) {
                field = displaying
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == TYPE_LOADING) loadingAdapter.onCreateViewHolder(parent, viewType)
            else actualAdapter.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit =
            if (isLoadingRow(position)) loadingAdapter.onBindViewHolder(holder, position)
            else actualAdapter.onBindViewHolder(holder, position)

    override fun getItemCount(): Int = if (isDisplaying) actualAdapter.itemCount + 1 else actualAdapter.itemCount

    override fun getItemViewType(position: Int): Int = if (isLoadingRow(position)) TYPE_LOADING else actualAdapter.getItemViewType(position)

    override fun getItemId(position: Int): Long = if (isLoadingRow(position)) RecyclerView.NO_ID else actualAdapter.getItemId(position)

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        actualAdapter.setHasStableIds(hasStableIds)
    }

    private val loadingRowPosition: Int get() = if (isDisplaying) itemCount - 1 else -1

    internal fun isLoadingRow(position: Int): Boolean = isDisplaying && position == loadingRowPosition
}