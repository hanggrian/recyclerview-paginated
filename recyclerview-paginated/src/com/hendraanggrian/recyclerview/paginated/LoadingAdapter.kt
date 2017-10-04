package com.hendraanggrian.recyclerview.paginated

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * @see PaginationAdapter
 */
abstract class LoadingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val DEFAULT: LoadingAdapter = object : LoadingAdapter() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                    object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.paginated_loading_row, parent, false)) {
                    }
        }
    }

    /** By default, there is no binding for loading row. Override this method otherwise. */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    /** It doesn't matter if this adapter is empty, only [LoadingAdapter.onCreateViewHolder] and [LoadingAdapter.onBindViewHolder] will be called. */
    override fun getItemCount(): Int = 0
}