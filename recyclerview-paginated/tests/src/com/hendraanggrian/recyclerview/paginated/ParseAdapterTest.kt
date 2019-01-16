package com.hendraanggrian.recyclerview.paginated

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.hendraanggrian.recyclerview.paginated.activity.ParseAdapterActivity
import com.hendraanggrian.recyclerview.paginated.adapter.TestErrorAdapter
import com.hendraanggrian.recyclerview.paginated.adapter.TestPlaceholderAdapter
import com.hendraanggrian.recyclerview.paginated.test.R
import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ParseAdapterTest {

    @Rule @JvmField val rule = ActivityTestRule(ParseAdapterActivity::class.java)

    @Test
    fun main() {
        Espresso.onView(ViewMatchers.withId(R.id.paginatedRecyclerView))
            .check { view, _ ->
                val recyclerView = view as PaginatedRecyclerView
                assert(recyclerView.placeholderAdapter is TestPlaceholderAdapter)
                assert(recyclerView.errorAdapter is TestErrorAdapter)
            }
    }
}