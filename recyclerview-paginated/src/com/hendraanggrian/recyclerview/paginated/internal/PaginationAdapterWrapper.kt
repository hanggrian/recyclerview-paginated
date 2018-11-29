package com.hendraanggrian.recyclerview.paginated.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.widget.PaginatedRecyclerView

internal class PaginationAdapterWrapper(
    val actualAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    private val loadingAdapter: PaginatedRecyclerView.LoadingAdapter<RecyclerView.ViewHolder>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_LOADING = Integer.MAX_VALUE - 50 // Magic
    }

    var isDisplaying: Boolean = true
        set(displaying) {
            if (field != displaying) {
                field = displaying
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_LOADING -> loadingAdapter.onCreateViewHolder(parent, viewType)
            else -> actualAdapter.onCreateViewHolder(parent, viewType)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int): Unit =
        when {
            isLoadingRow(position) -> loadingAdapter.onBindViewHolder(holder, position)
            else -> actualAdapter.onBindViewHolder(holder, position)
        }

    override fun getItemCount(): Int = when {
        isDisplaying -> actualAdapter.itemCount + 1
        else -> actualAdapter.itemCount
    }

    override fun getItemViewType(position: Int): Int = when {
        isLoadingRow(position) -> TYPE_LOADING
        else -> actualAdapter.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long = when {
        isLoadingRow(position) -> RecyclerView.NO_ID
        else -> actualAdapter.getItemId(position)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        actualAdapter.setHasStableIds(hasStableIds)
    }

    private val loadingRowPosition: Int
        get() = when {
            isDisplaying -> itemCount - 1
            else -> -1
        }

    internal fun isLoadingRow(position: Int): Boolean = isDisplaying &&
        position == loadingRowPosition
}