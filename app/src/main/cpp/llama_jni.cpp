#include <jni.h>
#include <string>
#include <android/log.h>

// 包含llama.cpp的头文件
#include "llama.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "LLaMA", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "LLaMA", __VA_ARGS__)

// 全局变量
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_localai_chat_utils_LlamaBridge_loadModel(JNIEnv* env, jobject thiz, jstring model_path) {
    const char* path = env->GetStringUTFChars(model_path, nullptr);
    
    LOGI("Loading model from: %s", path);
    
    // 加载模型
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0; // CPU only for Android
    
    g_model = llama_load_model_from_file(path, model_params);
    
    if (g_model == nullptr) {
        LOGE("Failed to load model");
        env->ReleaseStringUTFChars(model_path, path);
        return false;
    }
    
    // 创建上下文
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048; // 上下文长度
    ctx_params.n_threads = 4; // 线程数
    
    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    
    if (g_ctx == nullptr) {
        LOGE("Failed to create context");
        llama_free_model(g_model);
        g_model = nullptr;
        env->ReleaseStringUTFChars(model_path, path);
        return false;
    }
    
    env->ReleaseStringUTFChars(model_path, path);
    
    LOGI("Model loaded successfully");
    return true;
}

JNIEXPORT jstring JNICALL
Java_com_localai_chat_utils_LlamaBridge_generate(
    JNIEnv* env, 
    jobject thiz, 
    jstring prompt, 
    jint max_tokens, 
    jfloat temperature
) {
    const char* prompt_str = env->GetStringUTFChars(prompt, nullptr);
    
    LOGI("Generating with prompt: %s", prompt_str);
    
    if (g_model == nullptr || g_ctx == nullptr) {
        env->ReleaseStringUTFChars(prompt, prompt_str);
        return env->NewStringUTF("Error: Model not loaded");
    }
    
    // 分词
    std::vector<llama_token> tokens_list;
    tokens_list.reserve(max_tokens);
    
    int n_tokens = llama_tokenize(
        g_ctx,
        prompt_str,
        strlen(prompt_str),
        tokens_list.data(),
        tokens_list.capacity(),
        true,
        false
    );
    
    if (n_tokens < 0) {
        env->ReleaseStringUTFChars(prompt, prompt_str);
        return env->NewStringUTF("Error: Tokenization failed");
    }
    
    tokens_list.resize(n_tokens);
    
    // 生成回复
    std::string response;
    int n_gen = 0;
    
    for (int i = 0; i < max_tokens && n_gen < max_tokens; i++) {
        // 解码
        if (llama_decode(g_ctx, llama_batch_get_one(tokens_list.data(), tokens_list.size(), 0, 0)) != 0) {
            break;
        }
        
        // 采样
        llama_token new_token_id = llama_sampler_sample(nullptr, g_ctx, -1);
        
        // 检查是否结束
        if (llama_token_is_eog(g_model, new_token_id)) {
            break;
        }
        
        // 转换为文本
        char buf[256];
        int n = llama_token_to_piece(g_model, new_token_id, buf, sizeof(buf), 0, true);
        if (n > 0) {
            response.append(buf, n);
        }
        
        // 准备下一个token
        tokens_list.clear();
        tokens_list.push_back(new_token_id);
        n_gen++;
    }
    
    env->ReleaseStringUTFChars(prompt, prompt_str);
    
    if (response.empty()) {
        return env->NewStringUTF("Error: Generation failed");
    }
    
    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_localai_chat_utils_LlamaBridge_unloadModel(JNIEnv* env, jobject thiz) {
    LOGI("Unloading model");
    
    if (g_ctx != nullptr) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    
    if (g_model != nullptr) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_localai_chat_utils_LlamaBridge_isModelLoaded(JNIEnv* env, jobject thiz) {
    return g_model != nullptr && g_ctx != nullptr;
}

JNIEXPORT jlong JNICALL
Java_com_localai_chat_utils_LlamaBridge_getModelSize(JNIEnv* env, jobject thiz) {
    if (g_model == nullptr) return 0;
    return llama_model_size(g_model);
}

} // extern "C"
