cmake_minimum_required(VERSION 3.10.2)

project("r8brain_free_src_jni")
include_directories(r8brain-free-src)

add_library(
        r8brain_free_src_jni
        SHARED
        r8brain_free_src_jni.cpp
        r8brain-free-src/r8bbase.cpp
)

target_link_libraries(r8brain_free_src_jni log)
