package com.example.recyclerview.paginated

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class PostAdapterList(context: Context) : PostAdapter<PostAdapterList.ViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_post_list, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = list[position].let {
        holder.textViewTitle.text = it.title
        holder.textViewId.text = it.id.toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewId: TextView = itemView.findViewById(R.id.textViewId)
    }
}