package com.localai.chat.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("api/models/{modelId}")
    suspend fun getModelInfo(@Path("modelId") modelId: String): ModelInfoResponse
}

data class ModelInfoResponse(val id: String, val files: List<FileInfo>)
data class FileInfo(val rfilename: String, val size: Long)

object RetrofitClient {
    private const val BASE_URL = "https://huggingface.co/"

    val apiService: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().setLenient().create()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
