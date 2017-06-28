package com.example.recyclerview_paginated

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
        fun newPagination(adapter: PostAdapter, useCustomLoadingAdapter: Boolean): PaginatedRecyclerView.Pagination {
            return object : PaginatedRecyclerView.Pagination() {
                var loading: Boolean = true

                override fun onLoadMore(page: Int) {
                    loading = true
                    TypicodeServices.create(Typicode::class.java)
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

                override fun isLoading(): Boolean {
                    return loading
                }

                override fun isFinished(page: Int): Boolean {
                    return page >= 50
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

    val adapter = PostAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.pagination = newPagination(adapter, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemCustom -> {
                adapter.clear()

                item.isChecked = !item.isChecked
                recyclerView.releasePagination()
                recyclerView.pagination = newPagination(adapter, item.isChecked)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class CustomLoadingAdapter : LoadingAdapter<CustomLoadingAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomLoadingAdapter.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.custom_loading_row, parent, false))
        }

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    }
}