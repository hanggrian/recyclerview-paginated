PaginatedRecyclerView
=====================
Customizable pagination in Android RecyclerView.

Usage
-----
![demo_list][demo_list] ![demo_grid][demo_grid]

#### Use `PaginatedRecyclerView`
It has several attributes:
 * `loadingEnabled` - Will enable loading row while paginating, default is true.
 * `loadingThreshold` - Set the offset from the end of the list at which the load more event needs to be triggered, default is 5.
 * `loadOnStart` - Will call onLoadMore() when new pagination is set, default is true.
  
```xml
<com.hendraanggrian.widget.PaginatedRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:loadOnStart="false" />
```

#### Create `Pagination`
```java
public class PostPagination extends PaginatedRecyclerView.Pagination {
    @Override
    public void onLoadMode(int currentPage) {
        ...
    }
    
    @Override
    public boolean isLoading(int currentPage) {
        ...
    }
    
    @Override
    public boolean isFinished(int currentPage) {
        ...
    }
}
```

#### Attach `Pagination` to `PaginatedRecyclerView`
```java
recyclerView.setLayoutManager(lm)
recyclerView.setAdapter(adapter)
recyclerView.setPagination(pagination)
```

Customization
-------------
#### Use custom loading row
Create custom loading adapter.
```java
public class CustomLoadingAdapter extends LoadingAdapter {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ...
    }
}
```

When creating `Pagination`, override `getLoadingAdpater()` to use this custom adapter.
```java
public class MyPagination extends PaginatedRecyclerView.Pagination {

    @Override
    public LoadingAdapter getLoadingAdapter() {
        return new CustomLoadingAdapter();
    }
}
```

Download
--------
```gradle
repositories {
    google()
    jcenter()
}

dependencies {
    compile 'com.hendraanggrian:recyclerview-paginated:0.2.0'
}
```


License
-------
    Copyright 2017 Hendra Anggrian

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
[demo_list]: /art/demo_list.gif
[demo_grid]: /art/demo_grid.gif
