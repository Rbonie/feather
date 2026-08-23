# Feather Wallet Android - Build Instructions

## Overview

Feather Wallet for Android is a Monero wallet application built with Kotlin, Jetpack Compose, and native C++ JNI integration with the Monero Core library.

## Prerequisites

### Required Software
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **Android SDK**: API Level 21+ (Android 5.0)
- **Android NDK**: r25c or newer
- **CMake**: 3.22.1 or newer
- **JDK**: 11 or newer

### For Building Monero Core Libraries (Optional)
- **Linux**: Ubuntu 20.04 or newer recommended
- **Git**: For cloning repositories
- **Build tools**: gcc, g++, make, clang
- **Dependencies**: See Monero build documentation

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/org/monero/feather/
│   │   │   ├── data/           # Data layer (repositories, JNI wrapper)
│   │   │   ├── domain/         # Domain layer (models, use cases)
│   │   │   ├── ui/             # UI layer (screens, viewmodels, theme)
│   │   │   ├── di/             # Dependency injection modules
│   │   │   └── *.kt            # Application entry points
│   │   ├── cpp/                # Native C++ code (JNI implementation)
│   │   │   └── feather_wallet_jni.cpp
│   │   ├── res/                # Android resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── CMakeLists.txt
├── build.gradle.kts
├── settings.gradle.kts
└── build-monero-android.sh     # Script to build Monero libraries
```

## Quick Start (Stub Mode)

The project can be built and run in "stub mode" without the Monero Core libraries for UI development and testing:

1. **Open the project** in Android Studio
2. **Sync Gradle** files
3. **Run** on an emulator or device (API 21+)

In stub mode:
- All UI screens are functional
- Wallet operations return mock data
- No actual blockchain interaction occurs

## Full Build (With Monero Core)

### Step 1: Install Android NDK

1. Open Android Studio
2. Go to **SDK Manager** → **SDK Tools**
3. Check **NDK (Side by side)** and install
4. Note the NDK path (usually `$HOME/android-sdk/ndk/x.x.x`)

### Step 2: Set Environment Variables

```bash
export ANDROID_NDK_HOME=$HOME/android-sdk/ndk/25.2.9519653
export PATH=$PATH:$ANDROID_NDK_HOME
```

### Step 3: Build Monero Core Libraries

```bash
cd android
chmod +x build-monero-android.sh
./build-monero-android.sh
```

This script will:
- Clone the Monero repository
- Build `libmonero_wallet.so` for arm64-v8a, armeabi-v7a, and x86_64
- Copy libraries to `app/src/main/jniLibs/`

**Note**: This process takes 30-60 minutes depending on your hardware.

### Step 4: Enable Native Library Linking

Edit `app/CMakeLists.txt` and uncomment the lines at the bottom:

```cmake
set(MONERO_LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${CMAKE_ANDROID_ARCH_ABI})
if(EXISTS ${MONERO_LIB_DIR}/libmonero_wallet.so)
    target_link_libraries(feather_wallet_jni
        ${MONERO_LIB_DIR}/libmonero_wallet.so
    )
    target_include_directories(feather_wallet_jni PRIVATE
        ${CMAKE_SOURCE_DIR}/src/main/cpp/monero/include
    )
    message(STATUS "Linking against pre-built Monero wallet library")
else()
    message(STATUS "Monero wallet library not found - building in stub mode")
endif()
```

### Step 5: Build and Run

1. **Sync Gradle** in Android Studio
2. **Build** the project (Build → Make Project)
3. **Run** on a device or emulator

## Architecture

### Clean Architecture Layers

1. **Presentation Layer** (`ui/`)
   - Jetpack Compose UI components
   - ViewModels for state management
   - Navigation graph

2. **Domain Layer** (`domain/`)
   - Business logic (Use Cases)
   - Data models
   - Repository interfaces

3. **Data Layer** (`data/`)
   - Repository implementations
   - JNI wrapper for native calls
   - Local storage (encrypted)

### JNI Integration

The `WalletLibrary.kt` class provides Kotlin interface to native C++ code:
- Dynamic loading of Monero libraries via `dlopen()`
- Thread-safe wallet instance management
- Automatic fallback to stub mode if libraries unavailable

## Security Features

### Implemented
- Password-protected wallet encryption
- Secure memory handling for sensitive data
- Biometric authentication support (planned)

### Recommended Enhancements
- Android Keystore for password storage
- Hardware-backed encryption
- Memory protection flags
- Anti-tampering measures

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### JNI Tests
Native code tests require the full Monero library build.

## Troubleshooting

### Common Issues

**1. "No implementation found" errors**
- Ensure `feather_wallet_jni.cpp` compiles successfully
- Check that library name matches: `System.loadLibrary("feather_wallet_jni")`

**2. NDK build failures**
- Verify NDK version compatibility (r25c+)
- Check CMake configuration in `app/CMakeLists.txt`

**3. Monero library build fails**
- Ensure all build dependencies are installed
- Check available disk space (requires ~5GB)
- Try building for a single architecture first

**4. App crashes on startup**
- Check LogCat for JNI errors
- Verify AndroidManifest permissions
- Ensure minimum SDK version is 21+

### Debug Mode

Enable verbose logging:
```kotlin
// In WalletLibrary.kt init block
if (BuildConfig.DEBUG) {
    System.loadLibrary("feather_wallet_jni_debug")
}
```

## Performance Considerations

- Wallet synchronization runs in background threads
- Large transaction histories are paginated
- Database operations use Room with coroutines
- Native library uses dynamic loading to reduce APK size

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

BSD-3-Clause License - See LICENSE file for details.

## Support

For issues and questions:
- GitHub Issues: [feather-wallet/android](https://github.com/feather-wallet/feather/issues)
- Documentation: See `README.md` in the root directory

---

**Last Updated**: 2024
**Version**: 1.0.0-alpha
