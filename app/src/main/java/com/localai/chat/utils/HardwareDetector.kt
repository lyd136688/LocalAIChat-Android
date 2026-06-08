package com.localai.chat.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.localai.chat.data.models.ModelInfo

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
        val hasGpu = try {
            am.deviceConfigurationInfo.glEsVersion >= 3.0f
        } catch (_: Throwable) { false }
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

    fun getRecommendedModels(source: String = "HuggingFace"): List<ModelInfo> {
        val allModels = listOf(
            ModelInfo(
                id = "HuggingFaceTB/SmolLM2-135M-Instruct-GGUF",
                name = "SmolLM2-135M Q4_K_M",
                params = "135M",
                quantization = "Q4_K_M",
                size = 95.4f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "轻量"),
                source = source,
                description = "极小尺寸的演示级模型，运行极快，适合低配设备。"
            ),
            ModelInfo(
                id = "Qwen/Qwen2-0.5B-Instruct-GGUF",
                name = "Qwen2-0.5B Q4_K_M",
                params = "0.5B",
                quantization = "Q4_K_M",
                size = 400f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "中文"),
                source = source,
                description = "超小模型，中文友好，速度快。"
            ),
            ModelInfo(
                id = "Qwen/Qwen2-1.5B-Instruct-GGUF",
                name = "Qwen2-1.5B Q4_K_M",
                params = "1.5B",
                quantization = "Q4_K_M",
                size = 1100f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "中文"),
                source = source,
                description = "通义千问2 的 1.5B 小版本，中文友好，适合低端设备。"
            ),
            ModelInfo(
                id = "Qwen/Qwen2-7B-Instruct-GGUF",
                name = "Qwen2-7B Q4_K_M",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4800f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "中文", "高性能"),
                source = source,
                description = "通义千问2 中文大模型，中文理解与生成能力出色。"
            ),
            ModelInfo(
                id = "meta-llama/Llama-2-7b-chat-hf-GGUF",
                name = "Llama-2-7B-Chat Q4_K_M",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4200f,
                sizeUnit = "MB",
                license = "Llama 2",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "英文", "通用"),
                source = source,
                description = "Meta 发布的开源对话模型，通用领域表现稳定，适合多轮对话。"
            ),
            ModelInfo(
                id = "mistralai/Mistral-7B-Instruct-v0.2-GGUF",
                name = "Mistral-7B-Instruct Q4_K_M",
                params = "7B",
                quantization = "Q4_K_M",
                size = 4100f,
                sizeUnit = "MB",
                license = "Apache-2.0",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "推理强", "快速"),
                source = source,
                description = "Mistral 7B 指令调优版本，推理能力强，响应速度快。"
            ),
            ModelInfo(
                id = "microsoft/phi-2-GGUF",
                name = "Phi-2 Q4_K_M",
                params = "2.7B",
                quantization = "Q4_K_M",
                size = 1600f,
                sizeUnit = "MB",
                license = "MIT",
                tags = listOf("LLM", "Chat", "GGUF", "Q4_K_M", "小模型", "推理"),
                source = source,
                description = "微软发布的 2.7B 小型语言模型，在推理与代码任务上表现突出。"
            )
        )

        val deviceInfo = getDeviceInfo()
        val thresholdMB = deviceInfo.availableRamMB * 0.6f

        return allModels.filter { model ->
            val sizeMB = if (model.sizeUnit == "GB") model.size * 1024 else model.size
            sizeMB < thresholdMB
        }.ifEmpty { allModels.take(3) }
    }
}
