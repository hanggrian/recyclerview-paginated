package com.hendraanggrian.recyclerview.paginated.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ViewGroup;

import com.hendraanggrian.recyclerview.widget.PaginatedRecyclerView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint("Registered")
public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaginatedRecyclerView recyclerView = new PaginatedRecyclerView(this);
        recyclerView
            .setLoadingAdapter(new PaginatedRecyclerView.LoadingAdapter<RecyclerView.ViewHolder>() {
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return null;
                }
            });
        PaginatedRecyclerView.Pagination pagination = new PaginatedRecyclerView.Pagination() {
            @Override
            public int getPageStart() {
                return super.getPageStart();
            }

            @Override
            public void onPaginate(int page) {

            }
        };
    }
}