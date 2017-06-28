package com.example.recyclerview_paginated

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class PostAdapter(val context: Context) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    val list: MutableList<Post> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].let {
            holder.textViewPosition.text = position.toString()
            holder.textViewId.text = it.id.toString()
            holder.textViewTitle.text = it.title
        }
    }

    fun add(item: Post) {
        list.add(item)
        notifyItemInserted(list.indexOf(item))
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPosition = itemView.findViewById(R.id.textViewPosition) as TextView
        val textViewId = itemView.findViewById(R.id.textViewId) as TextView
        val textViewTitle = itemView.findViewById(R.id.textViewTitle) as TextView
    }
}