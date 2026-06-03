#ifndef LLAMA_H
#define LLAMA_H

#include <cstddef>
#include <cstdint>

#ifdef __cplusplus
extern "C" {
#endif

// 类型定义
typedef int llama_token;
typedef int32_t llama_pos;

// 模型结构
struct llama_model;
struct llama_context;

// 参数结构
struct llama_model_params {
    int n_gpu_layers;
    bool main_gpu;
    bool vocab_only;
    bool use_mmap;
    bool use_mlock;
};

struct llama_context_params {
    uint32_t seed;
    int n_ctx;
    int n_batch;
    int n_threads;
    int n_threads_batch;
    bool logits_all;
    bool embedding;
};

struct llama_batch {
    int32_t n_tokens;
    llama_token* token;
    llama_pos* pos;
    int32_t* n_seq_id;
    llama_token** seq_id;
    int8_t* logits;
};

struct llama_sampling_params {
    int n_prev;
    int n_probs;
    float temp;
    float top_p;
    float top_k;
    float repeat_penalty;
    int repeat_last_n;
    float presence_penalty;
    float frequency_penalty;
};

// 函数声明
llama_model* llama_load_model_from_file(const char* path, llama_model_params params);
void llama_free_model(llama_model* model);

llama_context* llama_new_context_with_model(llama_model* model, llama_context_params params);
void llama_free(llama_context* ctx);

int llama_tokenize(llama_context* ctx, const char* text, int* tokens, int n_max_tokens, bool add_bos);
int llama_decode(llama_context* ctx, llama_batch batch);
llama_token llama_sample_token(llama_context* ctx, llama_sampling_params params);
int llama_token_to_piece(llama_context* ctx, llama_token token, char* buf, int length);

size_t llama_model_size(const llama_model* model);
const char* llama_model_desc(const llama_model* model);

// 默认参数
inline llama_model_params llama_model_default_params() {
    llama_model_params params = {};
    params.n_gpu_layers = 0;
    params.main_gpu = false;
    params.vocab_only = false;
    params.use_mmap = true;
    params.use_mlock = false;
    return params;
}

inline llama_context_params llama_context_default_params() {
    llama_context_params params = {};
    params.seed = 0xFFFFFFFF;
    params.n_ctx = 512;
    params.n_batch = 512;
    params.n_threads = 4;
    params.n_threads_batch = 4;
    params.logits_all = false;
    params.embedding = false;
    return params;
}

#ifdef __cplusplus
}
#endif

#endif // LLAMA_H
