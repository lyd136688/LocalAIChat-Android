package com.localai.chat.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private const val HUGGINGFACE_BASE_URL = "https://huggingface.co/"
    private const val MODELSCOPE_BASE_URL = "https://modelscope.cn/api/v1/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val huggingFaceRetrofit = Retrofit.Builder()
        .baseUrl(HUGGINGFACE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val modelScopeRetrofit = Retrofit.Builder()
        .baseUrl(MODELSCOPE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val huggingFaceApi: ApiService = huggingFaceRetrofit.create(ApiService::class.java)
    val modelScopeApi: ApiService = modelScopeRetrofit.create(ApiService::class.java)
    
    fun getDownloadUrl(modelId: String, fileName: String, source: String): String {
        return if (source == "HuggingFace") {
            "https://huggingface.co/$modelId/resolve/main/$fileName"
        } else {
            "https://modelscope.cn/models/$modelId/resolve/master/$fileName"
        }
    }
}
