package com.example.recyclerview.paginated;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hendraanggrian.widget.PaginatedRecyclerView;

@SuppressLint("Registered")
public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaginatedRecyclerView recyclerView = new PaginatedRecyclerView(this);
        recyclerView.setLoadingAdapter(new PaginatedRecyclerView.LoadingAdapter<RecyclerView.ViewHolder>() {
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