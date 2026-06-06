#include <jni.h>
#include <string>
#include <vector>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 动态加载的函数指针类型
typedef void* (*llama_load_model_fn)(const char*, void*);
typedef void (*llama_free_model_fn)(void*);
typedef void* (*llama_new_context_fn)(void*, void*);
typedef void (*llama_free_fn)(void*);
typedef int (*llama_tokenize_fn)(void*, const char*, int*, int, bool, bool);
typedef int (*llama_decode_fn)(void*, void*);
typedef int (*llama_sampler_sample_fn)(void*, void*, int);
typedef int (*llama_token_to_piece_fn)(void*, int, char*, int, int, bool);

// 全局函数指针
static llama_load_model_fn g_load_model = nullptr;
static llama_free_model_fn g_free_model = nullptr;
static llama_new_context_fn g_new_context = nullptr;
static llama_free_fn g_free = nullptr;
static llama_tokenize_fn g_tokenize = nullptr;
static llama_decode_fn g_decode = nullptr;
static llama_sampler_sample_fn g_sampler_sample = nullptr;
static llama_token_to_piece_fn g_token_to_piece = nullptr;

static bool g_libs_loaded = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeInit(JNIEnv* env, jobject thiz) {
    if (g_libs_loaded) return JNI_TRUE;
    
    void* libllama = dlopen("libllama.so", RTLD_LAZY);
    if (!libllama) {
        LOGE("Failed to load libllama.so: %s", dlerror());
        return JNI_FALSE;
    }
    
    g_load_model = (llama_load_model_fn)dlsym(libllama, "llama_load_model_from_file");
    g_free_model = (llama_free_model_fn)dlsym(libllama, "llama_free_model");
    g_new_context = (llama_new_context_fn)dlsym(libllama, "llama_new_context_with_model");
    g_free = (llama_free_fn)dlsym(libllama, "llama_free");
    g_tokenize = (llama_tokenize_fn)dlsym(libllama, "llama_tokenize");
    g_decode = (llama_decode_fn)dlsym(libllama, "llama_decode");
    g_sampler_sample = (llama_sampler_sample_fn)dlsym(libllama, "llama_sampler_sample");
    g_token_to_piece = (llama_token_to_piece_fn)dlsym(libllama, "llama_token_to_piece");
    
    if (!g_load_model || !g_free_model || !g_new_context || !g_free) {
        LOGE("Failed to load required functions");
        return JNI_FALSE;
    }
    
    g_libs_loaded = true;
    LOGI("Native libraries loaded successfully");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeLoadModel(
    JNIEnv* env, jobject thiz, jstring modelPath) {
    
    if (!g_libs_loaded) return 0;
    
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    jlong result = 1; // 简化实现
    env->ReleaseStringUTFChars(modelPath, path);
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeIsModelLoaded(
    JNIEnv* env, jobject thiz, jlong handle) {
    return handle != 0 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeGenerate(
    JNIEnv* env, jobject thiz, jlong handle, jstring prompt, jint maxTokens) {
    
    if (handle == 0) {
        return env->NewStringUTF("Error: Model not loaded");
    }
    
    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    std::string result = "Generated text for: ";
    result += promptStr;
    env->ReleaseStringUTFChars(prompt, promptStr);
    
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeUnloadModel(
    JNIEnv* env, jobject thiz, jlong handle) {
}

extern "C" JNIEXPORT void JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeFree(
    JNIEnv* env, jobject thiz, jlong handle) {
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeGetModelInfo(
    JNIEnv* env, jobject thiz, jlong handle) {
    return env->NewStringUTF("LocalAIChat Model (JNI stub)");
}

