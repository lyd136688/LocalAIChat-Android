cmake_minimum_required(VERSION 3.18.1)

project("llama")

# 设置C++标准
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 查找预编译的llama库
add_library(llama SHARED IMPORTED)
set_target_properties(llama PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libllama.so
)

# 查找预编译的ggml库
add_library(ggml SHARED IMPORTED)
set_target_properties(ggml PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libggml.so
)

# 查找预编译的ggml-base库
add_library(ggml-base SHARED IMPORTED)
set_target_properties(ggml-base PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libggml-base.so
)

# 查找预编译的ggml-cpu库
add_library(ggml-cpu SHARED IMPORTED)
set_target_properties(ggml-cpu PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libggml-cpu-android_armv8.2_1.so
)

# JNI桥接文件
add_library(
    llama_jni
    SHARED
    llama_jni.cpp
)

# 查找Android日志库
find_library(
    log-lib
    log
)

# 链接所有库
target_link_libraries(
    llama_jni
    llama
    ggml
    ggml-base
    ggml-cpu
    ${log-lib}
)
