package com.localai.chat.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @GET("api/models")
    suspend fun searchModels(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("author") author: String? = null
    ): List<ModelSearchResult>
    
    @GET("api/models/{modelId}")
    suspend fun getModelInfo(
        @Path("modelId") modelId: String
    ): ModelDetail
    
    @GET("api/models/{modelId}/tree/main")
    suspend fun getModelFiles(
        @Path("modelId") modelId: String
    ): List<ModelFile>
    
    @GET("api/models/{modelId}/siblings")
    suspend fun getModelSiblings(
        @Path("modelId") modelId: String
    ): List<ModelSibling>
}

data class ModelSearchResult(
    val id: String,
    val modelId: String,
    val author: String,
    val sha: String,
    val lastModified: String,
    val private: Boolean,
    val disabled: Boolean,
    val gated: String?,
    val likes: Int,
    val downloads: Int
)

data class ModelDetail(
    val id: String,
    val author: String,
    val sha: String,
    val lastModified: String,
    val private: Boolean,
    val disabled: Boolean,
    val pipeline_tag: String?,
    val tags: List<String>,
    val downloads: Int,
    val likes: Int,
    val modelCard: String?
)

data class ModelFile(
    val rfilename: String,
    val size: Long,
    val blobId: String
)

data class ModelSibling(
    val rfilename: String,
    val size: Long,
    val downloadUrl: String?
)

