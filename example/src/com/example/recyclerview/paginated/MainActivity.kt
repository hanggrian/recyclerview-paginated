package com.example.recyclerview.paginated

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.hendraanggrian.widget.PaginatedRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kota.collections.add
import kota.collections.clear
import kota.layoutInflater
import kota.resources.getDrawable2
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var toggle: Boolean = false
    private val list: MutableList<Post> = mutableListOf()
    private var adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null
    private val disposables: MutableList<Disposable> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        adapter = PostAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.pagination = object : PaginatedRecyclerView.Pagination() {
            override fun onPaginate(page: Int) {
                disposables.add(TypicodeServices.create()
                        .posts(page)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ post ->
                            notifyLoadingCompleted()
                            list.add(post, adapter!!)
                        }) {
                            notifyPaginationFinished()
                        })
                if (page == 50) notifyPaginationFinished()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemToggle -> {
                toggle = !toggle
                if (toggle) {
                    item.icon = getDrawable2(R.drawable.ic_view_module_black_24dp)
                    recyclerView.layoutManager = GridLayoutManager(this, 3)
                } else {
                    item.icon = getDrawable2(R.drawable.ic_view_list_black_24dp)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }
            R.id.itemCustom -> {
                item.isChecked = !item.isChecked
                recyclerView.loadingAdapter = if (item.isChecked) CustomLoadingAdapter() else PaginatedRecyclerView.LoadingAdapter.DEFAULT
            }
        }
        list.clear(adapter!!)
        disposables.forEach { it.dispose() }
        disposables.clear()
        recyclerView.pagination!!.notifyPaginationReset()
        return super.onOptionsItemSelected(item)
    }

    class CustomLoadingAdapter : PaginatedRecyclerView.LoadingAdapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.context.layoutInflater.inflate(R.layout.custom_loading_row, parent, false))
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}