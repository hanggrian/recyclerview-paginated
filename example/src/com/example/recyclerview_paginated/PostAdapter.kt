package com.example.recyclerview_paginated

import android.content.Context
import android.support.v7.widget.RecyclerView

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
abstract class PostAdapter<VH : RecyclerView.ViewHolder>(val context: Context) : RecyclerView.Adapter<VH>() {

    val list: MutableList<Post> = ArrayList()

    fun add(item: Post) {
        list.add(item)
        notifyItemInserted(list.indexOf(item))
    }
}