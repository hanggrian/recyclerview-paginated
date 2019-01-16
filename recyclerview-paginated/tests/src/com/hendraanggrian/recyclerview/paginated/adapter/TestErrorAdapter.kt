package com.hendraanggrian.recyclerview.paginated.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView

class TestErrorAdapter : PaginatedRecyclerView.ErrorAdapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        object : RecyclerView.ViewHolder(TextView(parent.context).apply { text = "Error" }) {}
}