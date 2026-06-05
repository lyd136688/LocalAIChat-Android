#include <jni.h>
#include <string>
#include <android/log.h>
#include "llama.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
static llama_sampler *g_sampler = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeInit(
    JNIEnv *env, jobject /* this */) {
    LOGI("LlamaJNI initializing...");
    llama_backend_init();
    LOGI("Llama backend initialized successfully");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeLoadModel(
    JNIEnv *env, jobject /* this */, jstring model_path) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Loading model from: %s", path);

    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }
    if (g_sampler) {
        llama_sampler_free(g_sampler);
        g_sampler = nullptr;
    }

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    g_model = llama_load_model_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }

    LOGI("Model loaded successfully, n_vocab=%d, n_ctx_train=%d",
         llama_n_vocab(g_model), llama_n_ctx_train(g_model));

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_batch = 512;
    ctx_params.n_ubatch = 512;

    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    if (!g_ctx) {
        LOGE("Failed to create context");
        llama_free_model(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    LOGI("Context created successfully, n_ctx=%d", llama_n_ctx(g_ctx));

    g_sampler = llama_sampler_init_greedy();
    if (!g_sampler) {
        LOGE("Failed to create sampler");
        llama_free(g_ctx);
        g_ctx = nullptr;
        llama_free_model(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeIsModelLoaded(
    JNIEnv *env, jobject /* this */) {
    return g_model != nullptr && g_ctx != nullptr ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeGenerate(
    JNIEnv *env, jobject /* this */, jstring prompt, jint max_tokens) {
    if (!g_model || !g_ctx || !g_sampler) {
        LOGE("Model not loaded");
        return env->NewStringUTF("Error: Model not loaded");
    }

    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating response for prompt (length=%zu)", strlen(prompt_str));

    std::vector<llama_token> prompt_tokens;
    prompt_tokens.resize(512);

    int n_tokens = llama_tokenize(
        g_model,
        prompt_str,
        strlen(prompt_str),
        prompt_tokens.data(),
        prompt_tokens.size(),
        true,
        true
    );

    if (n_tokens < 0) {
        LOGE("Tokenization failed");
        env->ReleaseStringUTFChars(prompt, prompt_str);
        return env->NewStringUTF("Error: Tokenization failed");
    }

    prompt_tokens.resize(n_tokens);
    LOGI("Tokenized prompt: %d tokens", n_tokens);

    llama_kv_cache_clear(g_ctx);

    int n_batch = 512;
    for (int i = 0; i < n_tokens; i += n_batch) {
        int n_eval = n_tokens - i;
        if (n_eval > n_batch) n_eval = n_batch;

        llama_batch batch = llama_batch_init(n_eval, 0, 1);
        for (int j = 0; j < n_eval; j++) {
            batch.token[j] = prompt_tokens[i + j];
            batch.pos[j] = i + j;
            batch.n_seq_id[j] = 1;
            batch.seq_id[j][0] = 0;
            batch.logits[j] = false;
        }

        if (llama_decode(g_ctx, batch) != 0) {
            LOGE("Failed to decode prompt batch at position %d", i);
            llama_batch_free(batch);
            env->ReleaseStringUTFChars(prompt, prompt_str);
            return env->NewStringUTF("Error: Decode failed");
        }
        llama_batch_free(batch);
    }

    LOGI("Prompt decoded, starting generation (max_tokens=%d)", max_tokens);

    std::string response;
    const llama_token eos = llama_token_eos(g_model);

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token = llama_sampler_sample(g_sampler, g_ctx, -1);

        if (new_token == eos) {
            LOGI("EOS token reached at position %d", i);
            break;
        }

        char buf[256];
        int n = llama_token_to_piece(g_model, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            response.append(buf, n);
        }

        llama_batch batch = llama_batch_init(1, 0, 1);
        batch.token[0] = new_token;
        batch.pos[0] = n_tokens + i;
        batch.n_seq_id[0] = 1;
        batch.seq_id[0][0] = 0;
        batch.logits[0] = true;

        if (llama_decode(g_ctx, batch) != 0) {
            LOGE("Failed to decode generated token at position %d", i);
            break;
        }
        llama_batch_free(batch);
    }

    env->ReleaseStringUTFChars(prompt, prompt_str);
    LOGI("Generation complete, response length=%zu", response.length());

    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeUnloadModel(
    JNIEnv *env, jobject /* this */) {
    LOGI("Unloading model...");

    if (g_sampler) {
        llama_sampler_free(g_sampler);
        g_sampler = nullptr;
    }
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_free_model(g_model);
        g_model = nullptr;
    }

    LOGI("Model unloaded");
}

extern "C" JNIEXPORT void JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeFree(
    JNIEnv *env, jobject /* this */) {
    LOGI("Freeing llama backend...");
    Java_com_localai_chat_native_LlamaBridge_nativeUnloadModel(env, nullptr);
    llama_backend_free();
    LOGI("Llama backend freed");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_localai_chat_native_LlamaBridge_nativeGetModelInfo(
    JNIEnv *env, jobject /* this */) {
    if (!g_model) {
        return env->NewStringUTF("No model loaded");
    }

    char info[512];
    snprintf(info, sizeof(info),
        "Model Info:\n"
        "  Vocab size: %d\n"
        "  Context train: %d\n"
        "  Embedding dim: %d\n"
        "  Head count: %d\n"
        "  Layer count: %d",
        llama_n_vocab(g_model),
        llama_n_ctx_train(g_model),
        llama_n_embd(g_model),
        llama_n_head(g_model),
        llama_n_layer(g_model)
    );

    return env->NewStringUTF(info);
}

