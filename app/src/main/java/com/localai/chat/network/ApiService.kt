// ✅ 修复后的代码
package com.localai.chat.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    // 搜索模型列表
    @GET("api/models")
    suspend fun searchModels(@Query("search") query: String): List<ModelInfo>
    
    // 获取模型文件列表
    @GET("api/models/{modelId}/tree/main")
    suspend fun getModelFiles(@Path("modelId") modelId: String): List<FileInfo>
    
    // 获取模型信息
    @GET("api/models/{modelId}")
    suspend fun getModelInfo(@Path("modelId") modelId: String): ModelDetailInfo
}

// 搜索结果模型
data class ModelInfo(
    val id: String,
    val modelId: String? = null
)

// 文件信息模型
data class FileInfo(
    val path: String,
    val size: Long?,
    val type: String?
)

// 模型详情
data class ModelDetailInfo(
    val id: String,
    val siblings: List<FileRef>? = null
)

data class FileRef(
    val rfilename: String,
    val size: Long?
)

object RetrofitClient {
    private const val BASE_URL = "https://huggingface.co/"

    val apiService: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

