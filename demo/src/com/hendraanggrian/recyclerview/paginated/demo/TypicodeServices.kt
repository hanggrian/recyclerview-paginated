package com.hendraanggrian.recyclerview.paginated.demo

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object TypicodeServices {

    interface Service {

        @GET("posts/{id}")
        fun posts(@Path("id") id: Int): Single<Post>
    }

    private const val API = "https://jsonplaceholder.typicode.com/"

    private val loggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)
    private val clientBuilder = OkHttpClient.Builder()
    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl(API)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
    private var retrofit = retrofitBuilder.build()

    fun create(): Service {
        if (BuildConfig.DEBUG && !clientBuilder.interceptors().contains(loggingInterceptor)) {
            clientBuilder.addInterceptor(loggingInterceptor)
            retrofitBuilder.client(clientBuilder.build())
            retrofit = retrofitBuilder.build()
        }
        return retrofit.create(Service::class.java)
    }
}