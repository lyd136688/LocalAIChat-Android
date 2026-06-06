cmake_minimum_required(VERSION 3.22.1)

project("localaichat_jni")

add_library(
    localaichat_jni
    SHARED
    llama_jni.cpp
)

find_library(log-lib log)

target_link_libraries(
    localaichat_jni
    ${log-lib}
    dl
)

