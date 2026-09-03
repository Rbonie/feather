# Monero Native Libraries Directory

This directory contains the compiled Monero Core libraries for Android.

## Current Status: ⚠️ Libraries Not Yet Compiled

The directories are set up but empty. You need to compile the Monero Core library for each architecture.

## Required Libraries

For each architecture (arm64-v8a, armeabi-v7a, x86_64), you need to place:

- `libmonero_wallet.so` - Main wallet library
- `libwallet2.a` - Wallet2 static library  
- `libepee.a` - Epee utility library
- Other dependencies (Boost, OpenSSL, etc.)

## How to Compile

Run the build script from the project root:

```bash
cd /path/to/feather-wallet-android
./build-monero-android.sh
```

Or follow manual compilation instructions in `BUILD_INSTRUCTIONS.md`.

## Directory Structure

```
jniLibs/
├── arm64-v8a/      # 64-bit ARM (most modern devices)
│   └── libmonero_wallet.so
├── armeabi-v7a/    # 32-bit ARM (older devices)
│   └── libmonero_wallet.so
└── x86_64/         # 64-bit x86 (emulators, some tablets)
    └── libmonero_wallet.so
```

## Stub Mode

If libraries are not present, the application will run in **stub mode**:
- UI is fully functional
- All wallet operations return simulated data
- Useful for development and testing
- No real blockchain interaction

## After Placing Libraries

1. Clean and rebuild the project:
   ```bash
   ./gradlew clean assembleDebug
   ```

2. Test on a real device or emulator

3. Check logcat for any loading errors:
   ```bash
   adb logcat | grep FeatherWalletJNI
   ```
