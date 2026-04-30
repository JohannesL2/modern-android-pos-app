cmake_minimum_required(VERSION 3.22.1)

project("printerengine")

add_library(
    printerengine
    SHARED
    escpos_engine.cpp
    jni_bridge.cpp
)

find_library(log-lib log)

target_link_libraries(
    printerengine
    ${log-lib}
)