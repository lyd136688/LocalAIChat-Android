cmake_minimum_required(VERSION 3.18.1)

project("llama")

# 设置C++标准
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 添加llama.cpp源文件
add_library(
    llama
    SHARED
    llama.cpp
    llama_jni.cpp
)

# 查找Android日志库
find_library(
    log-lib
    log
)

# 链接库
target_link_libraries(
    llama
    ${log-lib}
)
