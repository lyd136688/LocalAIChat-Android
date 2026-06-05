package com.example.localaichat.native

import android.util.Log

/**
 * 天玑9300+ 专属优化配置
 * 
 * 硬件特性：
 * - CPU: 1× Cortex-X4 @ 3.0GHz + 3× Cortex-X4 @ 2.7GHz + 4× Cortex-A720 @ 2.0GHz
 * - GPU: Immortalis-G720 MC12
 * - NPU: MediaTek APU 790 (790 TOPS INT8)
 * - 内存: LPDDR5X 9600Mbps
 * 
 * 优化方向：
 * - ARMv9 指令集加速 (fp16, dotprod, i8mm)
 * - Vulkan GPU 推理加速
 * - NPU INT8/INT4 量化推理
 * - 大核优先调度
 */
object Dimensity9300Config {

    const val TAG = "Dimensity9300"

    // ==================== 编译优化参数 ====================
    // 下次编译 libllama.so 时使用这些 CMake 参数
    const val CMAKE_FLAGS = """
        -DGGML_CPU_ARM_ARCH=armv9-a
        -DGGML_VULKAN=ON
        -DCMAKE_C_FLAGS="-march=armv9-a+fp16+dotprod+i8mm -O3"
        -DCMAKE_CXX_FLAGS="-march=armv9-a+fp16+dotprod+i8mm -O3"
        -DGGML_LLAMAFILE=OFF
        -DGGML_CPU=ON
    """

    // ==================== 运行时配置 ====================
    val RUNTIME_CONFIG = DeviceProfile(
        name = "Dimensity 9300+",
        nCtx = 8192,           // 超大上下文窗口
        nBatch = 1024,         // 大批处理
        nThreads = 8,           // 8个大核全部使用
        gpuLayers = 33,        // 全部层 offload 到 GPU
        maxSessionMessages = 200,
        maxSessionTokens = 8192,
        localStorageLimitMB = 2048,
        useNpu = true,
        useGpu = true
    )

    // ==================== 线程调度策略 ====================
    /**
     * 天玑9300+ 的 CPU 拓扑：
     * - 大核: 4× Cortex-X4 (性能核心)
     * - 小核: 4× Cortex-A720 (效率核心)
     * 
     * 策略：推理任务绑定大核，UI 任务绑定小核
     */
    object ThreadAffinity {
        const val BIG_CORES_MASK = 0xF0       // 核心 4-7
        const val LITTLE_CORES_MASK = 0x0F    // 核心 0-3
        const val ALL_CORES_MASK = 0xFF

        // 推理使用大核
        const val INFERENCE_AFFINITY = BIG_CORES_MASK
    }

    // ==================== 内存配置 ====================
    object MemoryConfig {
        // KV-Cache 大小估算（7B Q4_0 模型）
        // 32 layers × 4096 embd × 8192 ctx × 2 (K+V) × 2 bytes (fp16)
        // ≈ 4GB - 需要分片处理
        const val KV_CACHE_CHUNK_SIZE = 512 * 1024 * 1024L  // 512MB 分片

        // 模型内存映射
        const val USE_MMAP = true
        const val USE_MLOCK = false  // 不锁定内存，让系统管理

        // 内存压力阈值
        const val MEMORY_PRESSURE_LOW = 0.6    // 60% 内存使用
        const val MEMORY_PRESSURE_HIGH = 0.85   // 85% 内存使用
    }

    // ==================== 量化配置 ====================
    object QuantizationConfig {
        // 推荐量化精度
        const val RECOMMENDED_QUANT = "Q4_K_M"  // 速度和质量的最佳平衡

        // NPU 量化
        const val NPU_QUANT_BITS = 4    // INT4 量化
        const val NPU_USE_SPARSE = true // 稀疏注意力

        // 不同场景的量化建议
        val SCENARIO_QUANTS = mapOf(
            "chat" to "Q4_K_M",       // 聊天：平衡
            "creative" to "Q5_K_M",   // 创作：更高质量
            "code" to "Q4_K_S",        // 编程：更快速度
            "long_context" to "Q3_K_M" // 长上下文：更省内存
        )
    }

    // ==================== 性能监控 ====================
    object PerformanceMonitor {
        // 目标生成速度
        const val TARGET_TOKENS_PER_SECOND = 15f  // 15 tokens/s

        // 温度阈值
        const val THERMAL_NORMAL = 45    // 正常温度
        const val THERMAL_WARNING = 55  // 温度警告
        const val THERMAL_THROTTLE = 65  // 降频阈值

        // 降频策略
        object ThrottleStrategy {
            const val REDUCE_THREADS = 4      // 减少线程
            const val REDUCE_BATCH = 512       // 减少批处理
            const val REDUCE_CTX = 4096        // 减少上下文
        }
    }

    /**
     * 根据设备温度动态调整配置
     */
    fun getThrottledConfig(temperature: Int): DeviceProfile {
        return when {
            temperature >= PerformanceMonitor.THERMAL_THROTTLE -> {
                Log.w(TAG, "Temperature $temperature°C, heavy throttling")
                DeviceProfile(
                    name = "Dimensity 9300+ (Throttled)",
                    nCtx = PerformanceMonitor.ThrottleStrategy.REDUCE_CTX,
                    nBatch = PerformanceMonitor.ThrottleStrategy.REDUCE_BATCH,
                    nThreads = PerformanceMonitor.ThrottleStrategy.REDUCE_THREADS,
                    gpuLayers = 10,  // 减少 GPU 层数
                    maxSessionMessages = 100,
                    maxSessionTokens = 4096,
                    localStorageLimitMB = 2048,
                    useNpu = false,  // 关闭 NPU 降低发热
                    useGpu = true
                )
            }
            temperature >= PerformanceMonitor.THERMAL_WARNING -> {
                Log.w(TAG, "Temperature $temperature°C, light throttling")
                RUNTIME_CONFIG.copy(
                    nThreads = 6,
                    nBatch = 768,
                    gpuLayers = 20
                )
            }
            else -> {
                Log.i(TAG, "Temperature $temperature°C, normal operation")
                RUNTIME_CONFIG
            }
        }
    }
}
