package com.localai.chat.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build

data class LocalModelInfo(
    val id: String,
    val name: String,
    val params: String?,
    val quantization: String?,
    val size: Float,
    val sizeUnit: String,
    val license: String?,
    val tags: List<String>,
    val source: String,
    val description: String?
)

class HardwareDetector(private val context: Context) {

    data class DeviceInfo(
        val totalRamMB: Long,
        val availableRamMB: Long,
        val cpuCores: Int,
        val cpuAbi: String,
        val hasNpu: Boolean,
        val hasGpu: Boolean,
        val socName: String,
        val recommendedModelSize: String
    )

    fun getDeviceInfo(): DeviceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val totalRamMB = mi.totalMem / (1024 * 1024)
        val availableRamMB = mi.availMem / (1024 * 1024)
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        // glEsVersion 是 Android packed float（GLES 2.0 = 0x20000，GLES 3.0 = 0x30000）
        val hasGpu = try {
            val glVersion = am.deviceConfigurationInfo.glEsVersion
            glVersion >= 0x30000f
        } catch (e: Throwable) {
            false
        }

        val socName = Build.HARDWARE

        val recommendedModelSize = when {
            totalRamMB < 4000 -> "135M-500M"
            totalRamMB < 6000 -> "500M-1.5B"
            totalRamMB < 8000 -> "1.5B-3B"
            totalRamMB < 12000 -> "3B-7B"
            else -> "7B-14B"
        }

        return DeviceInfo(
            totalRamMB = totalRamMB,
            availableRamMB = availableRamMB,
            cpuCores = cpuCores,
            cpuAbi = cpuAbi,
            hasNpu = false,
            hasGpu = hasGpu,
            socName = socName,
            recommendedModelSize = recommendedModelSize
        )
    }

    fun getRecommendedModels(source: String = "HuggingFace"): List<LocalModelInfo> {
        val models = listOf(
            LocalModelInfo(
                id = "huggingface/smollm-135m-instruct",
                name = "SmolLM2-135M",
                params = "135M",
                quantization = "Q4_K_M",
                size = 95f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("轻量", "快速", "演示"),
                source = source,
                description = "极小尺寸的演示级模型，运行极快，适合低配设备。"
            ),
            LocalModelInfo(
                id = "Qwen/Qwen2.5-0.5B-Instruct",
                name = "Qwen2.5-0.5B",
                params = "500M",
                quantization = "Q4_K_M",
                size = 350f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("轻量", "中文", "快速"),
                source = source,
                description = "超小模型，中文友好，响应迅速。"
            ),
            LocalModelInfo(
                id = "Qwen/Qwen2.5-1.5B-Instruct",
                name = "Qwen2.5-1.5B",
                params = "1.5B",
                quantization = "Q4_K_M",
                size = 1100f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("中文", "均衡", "推荐"),
                source = source,
                description = "通义千问2.5 小版本，中文能力优秀，性价比高。"
            ),
            LocalModelInfo(
                id = "Qwen/Qwen2.5-7B-Instruct",
                name = "Qwen2.5-7B",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4800f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("中文", "高质量", "推理强"),
                source = source,
                description = "通义千问2.5 中文大模型，通用场景表现出色。"
            ),
            LocalModelInfo(
                id = "meta-llama/Llama-2-7b-chat-hf",
                name = "Llama-2-7B-Chat",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4200f,
                sizeUnit = "MB",
                license = "Llama 2",
                tags = listOf("英文", "通用"),
                source = source,
                description = "Meta 发布的开源对话模型，通用领域稳定。"
            ),
            LocalModelInfo(
                id = "mistralai/Mistral-7B-Instruct-v0.2",
                name = "Mistral-7B-Instruct",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4100f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("英文", "推理", "快速"),
                source = source,
                description = "Mistral 7B 指令调优版本，推理能力强。"
            )
        )

        val deviceInfo = getDeviceInfo()
        val availableMB = deviceInfo.availableRamMB
        val thresholdMB = if (availableMB > 0) availableMB * 0.6f else 2000f

        return models.filter { model: LocalModelInfo ->
            val sizeMB = if (model.sizeUnit == "GB") model.size * 1024 else model.size
            sizeMB < thresholdMB
        }.ifEmpty { models.take(3) }
    }
}

