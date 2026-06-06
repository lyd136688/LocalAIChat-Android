cmake_minimum_required(VERSION 3.22.1)

project("localaichat_jni")

# 设置 JNI 库输出路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# 查找预编译的共享库
find_library(LOG_LIB log)

# 添加本地 JNI 封装库
add_library(
    localaichat_jni
    SHARED
    llama_jni.cpp
)

# 链接预编译的 llama.cpp 库
target_link_libraries(
    localaichat_jni
    ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libllama.so
    ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libggml.so
    ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libggml-base.so
    ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libggml-cpu-android_armv8.2_1.so
    ${LOG_LIB}
)

