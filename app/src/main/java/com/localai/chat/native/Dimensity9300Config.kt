package com.localai.chat.native

object Dimensity9300Config {
    fun getRecommendedQuantization(): String = "Q4_K_M"
    fun getMaxContextSize(): Int = 4096
    fun supportsBigLittle(): Boolean = true
}
