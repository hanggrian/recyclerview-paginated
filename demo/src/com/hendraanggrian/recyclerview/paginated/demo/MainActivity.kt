package com.hendraanggrian.recyclerview.paginated.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
        recyclerView.pagination = object : PaginatedRecyclerView.Pagination() {
            override fun onPaginate(page: Int) {
                disposables.add(TypicodeServices.create()
                    .posts(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ post ->
                        notifyLoadingCompleted()
                        if (list.add(post)) {
                            adapter!!.notifyItemInserted(list.size - 1)
                        }
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
                    item.icon =
                        ContextCompat.getDrawable(this, R.drawable.ic_view_module_black_24dp)
                    recyclerView.layoutManager = GridLayoutManager(this, 3)
                } else {
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_view_list_black_24dp)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }
            R.id.itemCustom -> {
                item.isChecked = !item.isChecked
                recyclerView.placeholderAdapter = when {
                    item.isChecked -> CustomPlaceholderAdapter()
                    else -> PaginatedRecyclerView.PlaceholderAdapter.DEFAULT
                }
            }
        }

        val size = list.size
        list.clear()
        adapter!!.notifyItemRangeRemoved(0, size)

        disposables.forEach { it.dispose() }
        disposables.clear()
        recyclerView.pagination!!.notifyPaginationReset()
        return super.onOptionsItemSelected(item)
    }
}