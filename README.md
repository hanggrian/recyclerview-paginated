PaginatedRecyclerView
=====================
Customizable pagination in Android RecyclerView.

![demo_list][demo_list] ![demo_grid][demo_grid]

Usage
-----
#### Use `PaginatedRecyclerView`
It has several attributes:
 * `loadingEnabled` - Will enable loading row while paginating, default is true.
 * `loadingThreshold` - Set the offset from the end of the list at which the paginate more event needs to be triggered, default is 5.

```xml
<com.hendraanggrian.widget.PaginatedRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### Create `Pagination`
```java
public class PostPagination extends PaginatedRecyclerView.Pagination {
    @Override
    public boolean getPageStart(int page) {
        return 0;
    }
    
    @Override
    public void onPaginate(int page) {
        if (loadItemSuccess) {
            populateItems(); // add items to adapter
            notifyLoadingCompleted();
        }
        if (reachPageEnd) {
            notifyPaginationFinished();
        }
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
Create custom loading adapter, and supply it to `PaginatedRecyclerView`.
```java
public class CustomLoadingAdapter extends LoadingAdapter {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ...
    }
}

CustomLoadingAdapter loadingAdapter = new CustomLoadingAdapter();
recyclerView.setLoadingAdapter(loadingAdapter);
```

Download
--------
```gradle
repositories {
    google()
    jcenter()
}

dependencies {
    compile 'com.android.support:recyclerview-v7:26.1.0'
    compile 'com.hendraanggrian:recyclerview-paginated:0.5'
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
