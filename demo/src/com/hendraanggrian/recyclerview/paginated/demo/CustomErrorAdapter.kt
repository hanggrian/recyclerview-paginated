package com.hendraanggrian.recyclerview.paginated.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView

class CustomErrorAdapter :
    PaginatedRecyclerView.ErrorAdapter<CustomErrorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.custom_error, parent, false)
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}