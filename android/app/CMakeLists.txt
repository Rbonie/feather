cmake_minimum_required(VERSION 3.22.1)

project("FeatherWalletJNI")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_package(JNI REQUIRED)

# Include JNI headers
include_directories(${JNI_INCLUDE_DIRS})

# Define source files for JNI wrapper
set(JNI_SOURCES
    src/main/cpp/feather_wallet_jni.cpp
)

# Create shared library
add_library(feather_wallet_jni SHARED ${JNI_SOURCES})

# Link libraries
target_link_libraries(feather_wallet_jni
    android
    log
    ${JNI_LIBRARIES}
)

# Compiler flags
target_compile_options(feather_wallet_jni PRIVATE
    -Wall
    -Wextra
    -fPIC
)
