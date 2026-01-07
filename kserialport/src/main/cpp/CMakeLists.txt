cmake_minimum_required(VERSION 3.22.1)

project("serialport")

add_library(${CMAKE_PROJECT_NAME} SHARED
        kserialport.cpp)

find_library(log-lib
        log)

target_link_libraries(${CMAKE_PROJECT_NAME}
        ${log-lib})

# Ensure 16 KB page alignment for Android 15+ compatibility
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")