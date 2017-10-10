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
import io.reactivex.schedulers.Schedulers
import kota.contents.getDrawable2
import kota.layoutInflater
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class MainActivity : AppCompatActivity() {

    companion object {
        fun newPagination(adapter: PostAdapter<*>, useCustomLoadingAdapter: Boolean): PaginatedRecyclerView.Pagination = object : PaginatedRecyclerView.Pagination() {
            override fun onPreparePage(page: Int): Boolean = page < 100

            override fun onPopulatePage(page: Int) {
                TypicodeServices.create()
                        .posts(page)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ post ->
                            notifyPopulateCompleted()
                            adapter.add(post)
                        }, {
                            notifyPopulateCompleted()
                        })
            }

            override val loadingAdapter: PaginatedRecyclerView.LoadingAdapter
                get() = if (useCustomLoadingAdapter) CustomLoadingAdapter()
                else super.loadingAdapter
        }
    }

    private var toggle = false

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
                item.icon = getDrawable2(if (toggle) R.drawable.ic_view_module_black_24dp else R.drawable.ic_view_list_black_24dp)
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

    private fun populate() {
        if (toggle) {
            recyclerView.layoutManager = GridLayoutManager(this, 3)
            recyclerView.adapter = PostAdapterGrid(this)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = PostAdapterList(this)
        }
        recyclerView.pagination = newPagination(recyclerView.adapter as PostAdapter, false)
    }

    class CustomLoadingAdapter : PaginatedRecyclerView.LoadingAdapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(parent.context.layoutInflater.inflate(R.layout.custom_loading_row, parent, false))

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    }
}