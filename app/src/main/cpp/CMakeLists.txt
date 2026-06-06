cmake_minimum_required(VERSION 3.22.1)

project("localaichat_jni")

# 添加 JNI 封装库
add_library(
    localaichat_jni
    SHARED
    llama_jni.cpp
)

# 链接日志库
find_library(log-lib log)

target_link_libraries(
    localaichat_jni
    ${log-lib}
    dl
)

