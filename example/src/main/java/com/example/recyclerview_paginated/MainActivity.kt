package com.example.recyclerview_paginated

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.hendraanggrian.recyclerview.paginated.LoadingAdapter
import com.hendraanggrian.widget.PaginatedRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class MainActivity : AppCompatActivity() {

    companion object {
        fun newPagination(adapter: PostAdapter<*>, useCustomLoadingAdapter: Boolean): PaginatedRecyclerView.Pagination {
            return object : PaginatedRecyclerView.Pagination() {
                var loading: Boolean = true

                override fun onLoadMore(page: Int) {
                    loading = true
                    TypicodeServices.create()
                            .posts(page)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ post ->
                                loading = false
                                adapter.add(post)
                            }, {
                                loading = false
                            })
                }

                override fun isLoading(page: Int): Boolean {
                    return loading
                }

                override fun isFinished(page: Int): Boolean {
                    return page > 100
                }

                override fun getLoadingAdapter(): LoadingAdapter<*> {
                    if (useCustomLoadingAdapter) {
                        return CustomLoadingAdapter()
                    } else {
                        return super.getLoadingAdapter()
                    }
                }
            }
        }
    }

    var toggle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        populate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemToggle -> {
                toggle = !toggle
                item.icon = ContextCompat.getDrawable(this, when (toggle) {
                    true -> R.drawable.ic_view_module_white_24dp
                    else -> R.drawable.ic_view_list_white_24dp
                })
                populate()
            }
            R.id.itemCustom -> {
                item.isChecked = !item.isChecked
                recyclerView.releasePagination()
                populate()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun populate() {
        if (toggle) {
            recyclerView.layoutManager = GridLayoutManager(this, 3)
            recyclerView.adapter = PostAdapterGrid(this)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = PostAdapterList(this)
        }
        recyclerView.pagination = newPagination(recyclerView.adapter as PostAdapter, false)
    }

    class CustomLoadingAdapter : LoadingAdapter<CustomLoadingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomLoadingAdapter.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_loading_row, parent, false))
        }

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    }
}