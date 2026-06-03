package com.localai.chat.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    /**
     * HuggingFace 搜索模型
     */
    @GET("api/models")
    suspend fun searchModels(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: Int = -1,
        @Query("filter") filter: String = "text-generation"
    ): HuggingFaceSearchResponse
    
    /**
     * 获取模型详情
     */
    @GET("api/models/{modelId}")
    suspend fun getModelInfo(
        @Path("modelId") modelId: String
    ): HuggingFaceModelDetail
    
    /**
     * 获取模型文件列表
     */
    @GET("api/models/{modelId}/tree/main")
    suspend fun getModelFiles(
        @Path("modelId") modelId: String
    ): List<HuggingFaceFile>
    
    /**
     * ModelScope 搜索模型
     */
    @GET("api/v1/dolphin/models")
    suspend fun searchModelScopeModels(
        @Query("SearchToken") query: String,
        @Query("PageSize") pageSize: Int = 20,
        @Query("PageNumber") pageNumber: Int = 1
    ): ModelScopeSearchResponse
}

// HuggingFace 响应数据类
data class HuggingFaceSearchResponse(
    val items: List<HuggingFaceModelItem>
)

data class HuggingFaceModelItem(
    val id: String,
    val modelId: String,
    val author: String?,
    val sha: String?,
    val lastModified: String?,
    val private: Boolean = false,
    val disabled: Boolean = false,
    val gated: String?,
    val likes: Int = 0,
    val downloads: Int = 0,
    val tags: List<String> = emptyList(),
    val pipeline_tag: String?,
    val library_name: String?,
    val createdAt: String?
)

data class HuggingFaceModelDetail(
    val id: String,
    val modelId: String,
    val author: String?,
    val sha: String?,
    val lastModified: String?,
    val private: Boolean = false,
    val disabled: Boolean = false,
    val pipeline_tag: String?,
    val tags: List<String> = emptyList(),
    val downloads: Int = 0,
    val likes: Int = 0,
    val modelCard: String?,
    val siblings: List<HuggingFaceSibling> = emptyList()
)

data class HuggingFaceSibling(
    val rfilename: String,
    val size: Long?,
    val downloadUrl: String?
)

data class HuggingFaceFile(
    val type: String,
    val path: String,
    val size: Long?,
    val oid: String?,
    val lfs: HuggingFaceLfsInfo?
)

data class HuggingFaceLfsInfo(
    val oid: String,
    val size: Long,
    val pointerSize: Int
)

// ModelScope 响应数据类
data class ModelScopeSearchResponse(
    val Data: ModelScopeData?,
    val Success: Boolean,
    val Message: String?
)

data class ModelScopeData(
    val Models: List<ModelScopeModel>?
)

data class ModelScopeModel(
    val ModelId: String,
    val ModelName: String?,
    val UserName: String?,
    val Downloads: Int?,
    val Tags: List<String>?,
    val Introduction: String?,
    val UpdatedTime: String?
)

