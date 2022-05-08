package com.xiangning.simplelauncher.retrofit

import io.reactivex.Observable
import retrofit2.http.*

interface DynamicApi {

    @GET
    fun get(@Url url: String) : Observable<String>

    @POST
    @FormUrlEncoded
    fun <T> post(@Url url: String, @Body body: T) : Observable<String>

}

fun <R> DynamicApi.get(url: String, cls: Class<R>): Observable<R> {
    return this.get(url)
        .map { RetrofitServiceFactory.GSON.fromJson(it, cls) }
}

fun <T, R> DynamicApi.post(url: String, body: T, cls: Class<R>): Observable<R> {
    return this.post(url, body)
        .map { RetrofitServiceFactory.GSON.fromJson(it, cls) }
}