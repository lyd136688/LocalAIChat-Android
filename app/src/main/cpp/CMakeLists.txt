cmake_minimum_required(VERSION 3.22.1)
project("localaichat")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(JNI_LIBS_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a)

add_library(llama SHARED IMPORTED)
set_target_properties(llama PROPERTIES
    IMPORTED_LOCATION ${JNI_LIBS_DIR}/libllama.so
)

add_library(ggml SHARED IMPORTED)
set_target_properties(ggml PROPERTIES
    IMPORTED_LOCATION ${JNI_LIBS_DIR}/libggml.so
)

add_library(ggml_base SHARED IMPORTED)
set_target_properties(ggml_base PROPERTIES
    IMPORTED_LOCATION ${JNI_LIBS_DIR}/libggml-base.so
)

add_library(ggml_cpu SHARED IMPORTED)
set_target_properties(ggml_cpu PROPERTIES
    IMPORTED_LOCATION ${JNI_LIBS_DIR}/libggml-cpu-android_armv8.2_1.so
)

add_library(localaichat_jni SHARED
    llama_jni.cpp
)

target_include_directories(localaichat_jni PRIVATE
    ${CMAKE_SOURCE_DIR}
)

target_link_libraries(localaichat_jni
    llama
    ggml
    ggml_base
    ggml_cpu
    android
    log
)
