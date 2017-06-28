package com.example.recyclerview_paginated

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
object TypicodeServices {

    private const val API = "https://jsonplaceholder.typicode.com/"

    private val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    private val okhttp = OkHttpClient.Builder()
    private val retrofit = Retrofit.Builder()
            .baseUrl(API)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
    private var cache = retrofit.build()

    fun <S> create(serviceClass: Class<S>): S {
        if (BuildConfig.DEBUG && !okhttp.interceptors().contains(loggingInterceptor)) {
            okhttp.addInterceptor(loggingInterceptor)
            retrofit.client(okhttp.build())
            cache = retrofit.build()
        }
        return cache.create(serviceClass)
    }
}