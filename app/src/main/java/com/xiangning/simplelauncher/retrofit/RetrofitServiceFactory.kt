package com.xiangning.simplelauncher.retrofit

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


object RetrofitServiceFactory {

    private val RETROFITS = HashMap<String, Retrofit>(4)

    val GSON = Gson()

    private fun initRetrofit(baseUri: String, daynamic: Boolean=false): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { this.level = HttpLoggingInterceptor.Level.BODY })
            .build()

        val builder = Retrofit.Builder()
            .client(httpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

        return (if (daynamic) {
            builder.baseUrl(baseUri)
                .addConverterFactory(ScalarsConverterFactory.create())
        } else {
            builder.baseUrl(baseUri)
                .addConverterFactory(GsonConverterFactory.create())
        }).build()
    }

    fun <T> getApi(baseUri: String, service: Class<T>): T {
        return RETROFITS.getOrPut(baseUri, { initRetrofit(baseUri) }).create(service)
    }

    val dynamic: DynamicApi = initRetrofit("http://invalid.com", true).create(DynamicApi::class.java)

}
