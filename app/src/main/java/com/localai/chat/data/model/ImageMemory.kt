package com.localai.chat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageMemory(
    val id: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long
)
