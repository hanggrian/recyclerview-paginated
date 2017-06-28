package com.example.recyclerview_paginated

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.hendraanggrian.widget.PaginatedRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class MainActivity : AppCompatActivity() {

    val adapter = PostAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.pagination = object : PaginatedRecyclerView.Pagination() {
            var loading: Boolean = true

            override fun onLoadMore(position: Int) {
                loading = true
                TypicodeServices.create(Typicode::class.java)
                        .posts(position)
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

            override fun hasLoadedAllItems(): Boolean {
                return adapter.size() >= 50
            }
        }
    }
}