# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI classes
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Feather Wallet JNI
-keep class org.monero.feather.jni.** { *; }
-keep class org.monero.feather.data.local.** { *; }

# Monero wallet C++ classes (if needed)
-keep class monero.** { *; }

# Hilt
-dontwarn dagger.**
-dontwarn javax.inject.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }
