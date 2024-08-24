# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.30.2)
project("litterallyless")
set(CMAKE_CXX_FLAGS "-Wall -Wextra ${CMAKE_CXX_FLAGS}")

find_library(log-lib
        log )

add_library(
        yuv2rgb-lib
        SHARED
        yuv2rgb-jni.cc
        yuv2rgb.cc)

target_link_libraries( # Specifies the target library.
        yuv2rgb-lib
        -O3
        # Links the target library to the log library included in the NDK.
        ${log-lib} )
