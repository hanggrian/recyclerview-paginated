package com.example.recyclerview.paginated

import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

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

    fun create(): Service {
        if (BuildConfig.DEBUG && !okhttp.interceptors().contains(loggingInterceptor)) {
            okhttp.addInterceptor(loggingInterceptor)
            retrofit.client(okhttp.build())
            cache = retrofit.build()
        }
        return cache.create(Service::class.java)
    }

    interface Service {

        @GET("posts/{id}")
        fun posts(@Path("id") id: Int): Single<Post>
    }
}