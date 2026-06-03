#ifndef GGML_H
#define GGML_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// 数据类型
enum ggml_type {
    GGML_TYPE_F32,
    GGML_TYPE_F16,
    GGML_TYPE_Q4_0,
    GGML_TYPE_Q4_1,
    GGML_TYPE_Q5_0,
    GGML_TYPE_Q5_1,
    GGML_TYPE_Q8_0,
    GGML_TYPE_Q8_1,
    GGML_TYPE_COUNT,
};

// 张量结构
struct ggml_tensor {
    enum ggml_type type;
    int n_dims;
    int64_t ne[4];
    size_t nb[4];
    void* data;
    char name[64];
};

// 上下文
struct ggml_context;

// 函数声明
struct ggml_context* ggml_init(size_t mem_size);
void ggml_free(struct ggml_context* ctx);

struct ggml_tensor* ggml_new_tensor_1d(struct ggml_context* ctx, enum ggml_type type, int64_t ne0);
struct ggml_tensor* ggml_new_tensor_2d(struct ggml_context* ctx, enum ggml_type type, int64_t ne0, int64_t ne1);

void ggml_set_param(struct ggml_context* ctx, struct ggml_tensor* tensor);

size_t ggml_used_mem(const struct ggml_context* ctx);

#ifdef __cplusplus
}
#endif

#endif // GGML_H
