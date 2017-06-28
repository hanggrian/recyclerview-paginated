package com.example.recyclerview_paginated

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
interface Typicode {

    @GET("posts/{id}")
    fun posts(@Path("id") id: Int): Observable<Post>
}