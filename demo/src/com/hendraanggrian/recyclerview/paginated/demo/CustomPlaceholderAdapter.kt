package com.hendraanggrian.recyclerview.paginated.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView

class CustomPlaceholderAdapter :
    PaginatedRecyclerView.PlaceholderAdapter<CustomPlaceholderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.custom_loading_row,
            parent,
            false
        )
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}