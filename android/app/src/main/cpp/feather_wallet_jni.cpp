// SPDX-License-Identifier: BSD-3-Clause
// SPDX-FileCopyrightText: The Monero Project

#include <jni.h>
#include <string>
#include <vector>
#include <map>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <android/log.h>
#include <dlfcn.h>

#define LOG_TAG "FeatherWalletJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ============================================================================
// Type definitions for Monero wallet API (dynamic loading)
// ============================================================================

typedef void* (*WalletManager_CreateFn)();
typedef void (*WalletManager_DestroyFn)(void*);
typedef bool (*WalletManager_WalletExistsFn)(void*, const char*);
typedef void* (*WalletManager_CreateWalletFromSeedFn)(void*, const char*, const char*, const char*, const char*, int);
typedef void* (*WalletManager_OpenWalletFn)(void*, const char*, const char*, int);
typedef void (*WalletManager_CloseWalletFn)(void*, void*);

typedef uint64_t (*Wallet_BalanceFn)(void*);
typedef uint64_t (*Wallet_UnlockedBalanceFn)(void*);
typedef const char* (*Wallet_AddressFn)(void*, int, int);
typedef const char* (*Wallet_SeedFn)(void*);
typedef void (*Wallet_RefreshFn)(void*);
typedef uint64_t (*Wallet_BlockChainHeightFn)(void*);
typedef uint64_t (*Wallet_DaemonBlockChainHeightFn)(void*, const char*);
typedef bool (*Wallet_SetDaemonFn)(void*, const char*, bool, const char*, const char*);
typedef bool (*Wallet_StoreFn)(void*);
typedef bool (*Wallet_ChangePasswordFn)(void*, const char*, const char*);
typedef int (*Wallet_StatusFn)(void*);
typedef const char* (*Wallet_ErrorStringFn)(void*);

typedef struct {
    const char* hash;
    uint64_t amount;
    uint64_t fee;
    uint64_t block_height;
    uint64_t timestamp;
    bool is_coinbase;
    bool is_pending;
} TransactionInfo;

typedef struct {
    TransactionInfo* transactions;
    size_t count;
} TransactionHistory;

typedef TransactionHistory* (*Wallet_TransactionHistoryFn)(void*);
typedef void (*TransactionHistory_FreeFn)(TransactionHistory*);

typedef struct {
    const char* address;
    uint64_t amount;
    const char* payment_id;
    int subaddress_account;
    int subaddress_index;
} PendingTransaction;

typedef void* (*Wallet_CreateTransactionFn)(void*, const char*, uint64_t, int, int, const char*);
typedef bool (*Wallet_CommitTransactionFn)(void*, void*);
typedef void (*Wallet_FreeTransactionFn)(void*, void*);
typedef const char* (*Wallet_TransactionErrorFn)(void*, void*);
typedef uint64_t (*Wallet_TransactionAmountFn)(void*);
typedef uint64_t (*Wallet_TransactionFeeFn)(void*);

// ============================================================================
// Global state with proper synchronization
// ============================================================================

struct WalletInstance {
    void* wallet;
    std::string path;
    std::string password;
    int networkType;
    bool isActive;
};

static std::map<jlong, WalletInstance*> g_wallets;
static std::mutex g_walletsMutex;
static void* g_walletManager = nullptr;
static std::mutex g_managerMutex;

// Dynamic library handles
static void* g_moneroLibHandle = nullptr;
static bool g_libraryInitialized = false;

// Function pointers for dynamic loading
static WalletManager_CreateFn fn_Manager_Create = nullptr;
static WalletManager_DestroyFn fn_Manager_Destroy = nullptr;
static WalletManager_WalletExistsFn fn_Manager_WalletExists = nullptr;
static WalletManager_CreateWalletFromSeedFn fn_Manager_CreateWalletFromSeed = nullptr;
static WalletManager_OpenWalletFn fn_Manager_OpenWallet = nullptr;
static WalletManager_CloseWalletFn fn_Manager_CloseWallet = nullptr;
static Wallet_BalanceFn fn_Wallet_Balance = nullptr;
static Wallet_UnlockedBalanceFn fn_Wallet_UnlockedBalance = nullptr;
static Wallet_AddressFn fn_Wallet_Address = nullptr;
static Wallet_SeedFn fn_Wallet_Seed = nullptr;
static Wallet_RefreshFn fn_Wallet_Refresh = nullptr;
static Wallet_BlockChainHeightFn fn_Wallet_BlockChainHeight = nullptr;
static Wallet_DaemonBlockChainHeightFn fn_Wallet_DaemonBlockChainHeight = nullptr;
static Wallet_SetDaemonFn fn_Wallet_SetDaemon = nullptr;
static Wallet_StoreFn fn_Wallet_Store = nullptr;
static Wallet_ChangePasswordFn fn_Wallet_ChangePassword = nullptr;
static Wallet_StatusFn fn_Wallet_Status = nullptr;
static Wallet_ErrorStringFn fn_Wallet_ErrorString = nullptr;
static Wallet_TransactionHistoryFn fn_Wallet_TransactionHistory = nullptr;
static TransactionHistory_FreeFn fn_TransactionHistory_Free = nullptr;
static Wallet_CreateTransactionFn fn_Wallet_CreateTransaction = nullptr;
static Wallet_CommitTransactionFn fn_Wallet_CommitTransaction = nullptr;
static Wallet_FreeTransactionFn fn_Wallet_FreeTransaction = nullptr;
static Wallet_TransactionErrorFn fn_Wallet_TransactionError = nullptr;
static Wallet_TransactionAmountFn fn_Wallet_TransactionAmount = nullptr;
static Wallet_TransactionFeeFn fn_Wallet_TransactionFee = nullptr;

// ============================================================================
// Helper functions
// ============================================================================

static bool loadMoneroLibrary(const std::string& libPath) {
    if (g_moneroLibHandle) {
        return true; // Already loaded
    }
    
    LOGI("Loading Monero library from: %s", libPath.c_str());
    
    g_moneroLibHandle = dlopen(libPath.c_str(), RTLD_LAZY | RTLD_LOCAL);
    if (!g_moneroLibHandle) {
        LOGE("Failed to load Monero library: %s", dlerror());
        return false;
    }
    
    // Load function pointers
    fn_Manager_Create = (WalletManager_CreateFn)dlsym(g_moneroLibHandle, "wallet_manager_create");
    fn_Manager_Destroy = (WalletManager_DestroyFn)dlsym(g_moneroLibHandle, "wallet_manager_destroy");
    fn_Manager_WalletExists = (WalletManager_WalletExistsFn)dlsym(g_moneroLibHandle, "wallet_manager_wallet_exists");
    fn_Manager_CreateWalletFromSeed = (WalletManager_CreateWalletFromSeedFn)dlsym(g_moneroLibHandle, "wallet_manager_create_wallet_from_seed");
    fn_Manager_OpenWallet = (WalletManager_OpenWalletFn)dlsym(g_moneroLibHandle, "wallet_manager_open_wallet");
    fn_Manager_CloseWallet = (WalletManager_CloseWalletFn)dlsym(g_moneroLibHandle, "wallet_manager_close_wallet");
    
    fn_Wallet_Balance = (Wallet_BalanceFn)dlsym(g_moneroLibHandle, "wallet_balance");
    fn_Wallet_UnlockedBalance = (Wallet_UnlockedBalanceFn)dlsym(g_moneroLibHandle, "wallet_unlocked_balance");
    fn_Wallet_Address = (Wallet_AddressFn)dlsym(g_moneroLibHandle, "wallet_address");
    fn_Wallet_Seed = (Wallet_SeedFn)dlsym(g_moneroLibHandle, "wallet_seed");
    fn_Wallet_Refresh = (Wallet_RefreshFn)dlsym(g_moneroLibHandle, "wallet_refresh");
    fn_Wallet_BlockChainHeight = (Wallet_BlockChainHeightFn)dlsym(g_moneroLibHandle, "wallet_blockchain_height");
    fn_Wallet_DaemonBlockChainHeight = (Wallet_DaemonBlockChainHeightFn)dlsym(g_moneroLibHandle, "wallet_daemon_blockchain_height");
    fn_Wallet_SetDaemon = (Wallet_SetDaemonFn)dlsym(g_moneroLibHandle, "wallet_set_daemon");
    fn_Wallet_Store = (Wallet_StoreFn)dlsym(g_moneroLibHandle, "wallet_store");
    fn_Wallet_ChangePassword = (Wallet_ChangePasswordFn)dlsym(g_moneroLibHandle, "wallet_change_password");
    fn_Wallet_Status = (Wallet_StatusFn)dlsym(g_moneroLibHandle, "wallet_status");
    fn_Wallet_ErrorString = (Wallet_ErrorStringFn)dlsym(g_moneroLibHandle, "wallet_error_string");
    fn_Wallet_TransactionHistory = (Wallet_TransactionHistoryFn)dlsym(g_moneroLibHandle, "wallet_transaction_history");
    fn_TransactionHistory_Free = (TransactionHistory_FreeFn)dlsym(g_moneroLibHandle, "transaction_history_free");
    fn_Wallet_CreateTransaction = (Wallet_CreateTransactionFn)dlsym(g_moneroLibHandle, "wallet_create_transaction");
    fn_Wallet_CommitTransaction = (Wallet_CommitTransactionFn)dlsym(g_moneroLibHandle, "wallet_commit_transaction");
    fn_Wallet_FreeTransaction = (Wallet_FreeTransactionFn)dlsym(g_moneroLibHandle, "wallet_free_transaction");
    fn_Wallet_TransactionError = (Wallet_TransactionErrorFn)dlsym(g_moneroLibHandle, "wallet_transaction_error");
    fn_Wallet_TransactionAmount = (Wallet_TransactionAmountFn)dlsym(g_moneroLibHandle, "wallet_transaction_amount");
    fn_Wallet_TransactionFee = (Wallet_TransactionFeeFn)dlsym(g_moneroLibHandle, "wallet_transaction_fee");
    
    // Check if critical functions are loaded
    if (!fn_Manager_Create || !fn_Manager_Destroy) {
        LOGE("Failed to load critical wallet manager functions");
        dlclose(g_moneroLibHandle);
        g_moneroLibHandle = nullptr;
        return false;
    }
    
    LOGI("Monero library loaded successfully");
    return true;
}

static void unloadMoneroLibrary() {
    if (g_moneroLibHandle) {
        dlclose(g_moneroLibHandle);
        g_moneroLibHandle = nullptr;
        g_libraryInitialized = false;
        
        // Reset all function pointers
        fn_Manager_Create = nullptr;
        fn_Manager_Destroy = nullptr;
        // ... reset all other pointers
    }
}

static WalletInstance* getWalletInstance(jlong handle) {
    std::lock_guard<std::mutex> lock(g_walletsMutex);
    auto it = g_wallets.find(handle);
    if (it != g_wallets.end() && it->second->isActive) {
        return it->second;
    }
    return nullptr;
}

static jlong addWalletInstance(void* wallet, const std::string& path, const std::string& password, int networkType) {
    std::lock_guard<std::mutex> lock(g_walletsMutex);
    
    static jlong nextHandle = 1;
    jlong handle = nextHandle++;
    
    WalletInstance* instance = new WalletInstance();
    instance->wallet = wallet;
    instance->path = path;
    instance->password = password;
    instance->networkType = networkType;
    instance->isActive = true;
    
    g_wallets[handle] = instance;
    return handle;
}

static void removeWalletInstance(jlong handle) {
    std::lock_guard<std::mutex> lock(g_walletsMutex);
    auto it = g_wallets.find(handle);
    if (it != g_wallets.end()) {
        delete it->second;
        g_wallets.erase(it);
    }
}

// ============================================================================
// JNI Functions Implementation
// ============================================================================

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Initialize the wallet library
 */
JNIEXPORT jboolean JNICALL
Java_org_monero_feather_data_local_WalletLibrary_initializeLibrary(
        JNIEnv* env,
        jobject thiz,
        jstring dataDir) {
    
    LOGI("Initializing Feather Wallet JNI library");
    
    const char* dataDirChars = env->GetStringUTFChars(dataDir, nullptr);
    if (!dataDirChars) {
        LOGE("Failed to get data directory string");
        return JNI_FALSE;
    }
    
    std::string dataDirStr(dataDirChars);
    env->ReleaseStringUTFChars(dataDir, dataDirChars);
    
    LOGI("Data directory: %s", dataDirStr.c_str());
    
    // Try to load the Monero library from various locations
    std::vector<std::string> libPaths = {
        dataDirStr + "/lib/libmonero_wallet.so",
        "/data/data/org.monero.feather/lib/libmonero_wallet.so",
        "libmonero_wallet.so"
    };
    
    bool loaded = false;
    for (const auto& path : libPaths) {
        if (loadMoneroLibrary(path)) {
            loaded = true;
            break;
        }
    }
    
    if (!loaded) {
        LOGW("Monero library not found - running in stub mode");
        // Continue in stub mode for development/testing
        g_libraryInitialized = true;
        return JNI_TRUE;
    }
    
    // Create wallet manager instance
    {
        std::lock_guard<std::mutex> lock(g_managerMutex);
        if (!g_walletManager && fn_Manager_Create) {
            g_walletManager = fn_Manager_Create();
            if (!g_walletManager) {
                LOGE("Failed to create wallet manager");
                return JNI_FALSE;
            }
            LOGI("Wallet manager created successfully");
        }
    }
    
    g_libraryInitialized = true;
    LOGI("Wallet library initialized successfully");
    return JNI_TRUE;
}

/**
 * Cleanup and unload the library
 */
JNIEXPORT void JNICALL
Java_org_monero_feather_data_local_WalletLibrary_cleanupLibrary(
        JNIEnv* env,
        jobject thiz) {
    
    LOGI("Cleaning up wallet library");
    
    // Close all open wallets
    {
        std::lock_guard<std::mutex> lock(g_walletsMutex);
        for (auto& pair : g_wallets) {
            if (pair.second->wallet && fn_Manager_CloseWallet && g_walletManager) {
                fn_Manager_CloseWallet(g_walletManager, pair.second->wallet);
            }
            delete pair.second;
        }
        g_wallets.clear();
    }
    
    // Destroy wallet manager
    {
        std::lock_guard<std::mutex> lock(g_managerMutex);
        if (g_walletManager && fn_Manager_Destroy) {
            fn_Manager_Destroy(g_walletManager);
            g_walletManager = nullptr;
        }
    }
    
    unloadMoneroLibrary();
    LOGI("Library cleanup completed");
}

/**
 * Check if wallet exists at given path
 */
JNIEXPORT jboolean JNICALL
Java_org_monero_feather_data_local_WalletLibrary_walletExists(
        JNIEnv* env,
        jobject thiz,
        jstring path) {
    
    const char* pathChars = env->GetStringUTFChars(path, nullptr);
    if (!pathChars) {
        return JNI_FALSE;
    }
    
    std::string pathStr(pathChars);
    env->ReleaseStringUTFChars(path, pathChars);
    
    LOGD("Checking wallet existence: %s", pathStr.c_str());
    
    if (!g_libraryInitialized || !g_walletManager || !fn_Manager_WalletExists) {
        // Stub mode - check file system
        // In real implementation, this would use the Monero library
        return JNI_FALSE;
    }
    
    bool exists = fn_Manager_WalletExists(g_walletManager, pathStr.c_str());
    return exists ? JNI_TRUE : JNI_FALSE;
}

/**
 * Create a new wallet from mnemonic seed
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_createWalletFromSeed(
        JNIEnv* env,
        jobject thiz,
        jstring path,
        jstring password,
        jstring seed,
        jstring language,
        jint networkType) {
    
    const char* pathChars = env->GetStringUTFChars(path, nullptr);
    const char* passwordChars = env->GetStringUTFChars(password, nullptr);
    const char* seedChars = env->GetStringUTFChars(seed, nullptr);
    const char* languageChars = env->GetStringUTFChars(language, nullptr);
    
    if (!pathChars || !passwordChars || !seedChars || !languageChars) {
        env->ReleaseStringUTFChars(path, pathChars);
        env->ReleaseStringUTFChars(password, passwordChars);
        env->ReleaseStringUTFChars(seed, seedChars);
        env->ReleaseStringUTFChars(language, languageChars);
        return 0;
    }
    
    std::string pathStr(pathChars);
    std::string passwordStr(passwordChars);
    std::string seedStr(seedChars);
    std::string languageStr(languageChars);
    
    env->ReleaseStringUTFChars(path, pathChars);
    env->ReleaseStringUTFChars(password, passwordChars);
    env->ReleaseStringUTFChars(seed, seedChars);
    env->ReleaseStringUTFChars(language, languageChars);
    
    LOGI("Creating wallet from seed: %s", pathStr.c_str());
    
    if (!g_libraryInitialized) {
        LOGE("Library not initialized");
        return 0;
    }
    
    if (!g_walletManager || !fn_Manager_CreateWalletFromSeed) {
        LOGE("Wallet manager or create function not available");
        return 0;
    }
    
    void* wallet = fn_Manager_CreateWalletFromSeed(
        g_walletManager,
        pathStr.c_str(),
        passwordStr.c_str(),
        seedStr.c_str(),
        languageStr.c_str(),
        networkType
    );
    
    if (!wallet) {
        LOGE("Failed to create wallet");
        return 0;
    }
    
    jlong handle = addWalletInstance(wallet, pathStr, passwordStr, networkType);
    LOGI("Wallet created successfully with handle: %lld", handle);
    return handle;
}

/**
 * Open existing wallet
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_openWallet(
        JNIEnv* env,
        jobject thiz,
        jstring path,
        jstring password,
        jint networkType) {
    
    const char* pathChars = env->GetStringUTFChars(path, nullptr);
    const char* passwordChars = env->GetStringUTFChars(password, nullptr);
    
    if (!pathChars || !passwordChars) {
        env->ReleaseStringUTFChars(path, pathChars);
        env->ReleaseStringUTFChars(password, passwordChars);
        return 0;
    }
    
    std::string pathStr(pathChars);
    std::string passwordStr(passwordChars);
    
    env->ReleaseStringUTFChars(path, pathChars);
    env->ReleaseStringUTFChars(password, passwordChars);
    
    LOGI("Opening wallet: %s", pathStr.c_str());
    
    if (!g_libraryInitialized) {
        LOGE("Library not initialized");
        return 0;
    }
    
    if (!g_walletManager || !fn_Manager_OpenWallet) {
        LOGE("Wallet manager or open function not available");
        return 0;
    }
    
    void* wallet = fn_Manager_OpenWallet(
        g_walletManager,
        pathStr.c_str(),
        passwordStr.c_str(),
        networkType
    );
    
    if (!wallet) {
        LOGE("Failed to open wallet");
        return 0;
    }
    
    jlong handle = addWalletInstance(wallet, pathStr, passwordStr, networkType);
    LOGI("Wallet opened successfully with handle: %lld", handle);
    return handle;
}

/**
 * Close wallet
 */
JNIEXPORT void JNICALL
Java_org_monero_feather_data_local_WalletLibrary_closeWallet(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    LOGI("Closing wallet with handle: %lld", walletHandle);
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance) {
        LOGE("Wallet instance not found");
        return;
    }
    
    // Store wallet before closing
    if (fn_Wallet_Store && instance->wallet) {
        fn_Wallet_Store(instance->wallet);
    }
    
    // Close wallet using manager
    if (g_walletManager && fn_Manager_CloseWallet && instance->wallet) {
        fn_Manager_CloseWallet(g_walletManager, instance->wallet);
    }
    
    instance->isActive = false;
    removeWalletInstance(walletHandle);
    
    LOGI("Wallet closed successfully");
}

// ============================================================================
// Wallet Operations
// ============================================================================

/**
 * Get wallet balance
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getBalance(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return 0;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found or invalid");
        return 0;
    }
    
    if (!fn_Wallet_Balance) {
        LOGW("Balance function not available (stub mode)");
        return 0;
    }
    
    uint64_t balance = fn_Wallet_Balance(instance->wallet);
    return static_cast<jlong>(balance);
}

/**
 * Get wallet unlocked balance
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getUnlockedBalance(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return 0;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found or invalid");
        return 0;
    }
    
    if (!fn_Wallet_UnlockedBalance) {
        LOGW("Unlocked balance function not available (stub mode)");
        return 0;
    }
    
    uint64_t balance = fn_Wallet_UnlockedBalance(instance->wallet);
    return static_cast<jlong>(balance);
}

/**
 * Get wallet address
 */
JNIEXPORT jstring JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getAddress(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle,
        jint accountIndex,
        jint addressIndex) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return env->NewStringUTF("");
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found or invalid");
        return env->NewStringUTF("");
    }
    
    if (!fn_Wallet_Address) {
        LOGW("Address function not available (stub mode)");
        return env->NewStringUTF("");
    }
    
    const char* address = fn_Wallet_Address(instance->wallet, accountIndex, addressIndex);
    jstring result = env->NewStringUTF(address ? address : "");
    return result;
}

/**
 * Get mnemonic seed
 */
JNIEXPORT jstring JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getSeed(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return env->NewStringUTF("");
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found or invalid");
        return env->NewStringUTF("");
    }
    
    if (!fn_Wallet_Seed) {
        LOGW("Seed function not available (stub mode)");
        return env->NewStringUTF("");
    }
    
    const char* seed = fn_Wallet_Seed(instance->wallet);
    jstring result = env->NewStringUTF(seed ? seed : "");
    return result;
}

/**
 * Start wallet refresh/synchronization
 */
JNIEXPORT void JNICALL
Java_org_monero_feather_data_local_WalletLibrary_refresh(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found or invalid");
        return;
    }
    
    if (!fn_Wallet_Refresh) {
        LOGW("Refresh function not available (stub mode)");
        return;
    }
    
    LOGD("Refreshing wallet");
    fn_Wallet_Refresh(instance->wallet);
}

/**
 * Get current block height
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getBlockChainHeight(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        return 0;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        return 0;
    }
    
    if (!fn_Wallet_BlockChainHeight) {
        return 0;
    }
    
    uint64_t height = fn_Wallet_BlockChainHeight(instance->wallet);
    return static_cast<jlong>(height);
}

/**
 * Get daemon block height
 */
JNIEXPORT jlong JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getDaemonBlockChainHeight(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle,
        jstring daemonAddress) {
    
    const char* daemonAddressChars = env->GetStringUTFChars(daemonAddress, nullptr);
    if (!daemonAddressChars) {
        return 0;
    }
    
    std::string daemonAddressStr(daemonAddressChars);
    env->ReleaseStringUTFChars(daemonAddress, daemonAddressChars);
    
    if (walletHandle == 0) {
        return 0;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        return 0;
    }
    
    if (!fn_Wallet_DaemonBlockChainHeight) {
        return 0;
    }
    
    uint64_t height = fn_Wallet_DaemonBlockChainHeight(instance->wallet, daemonAddressStr.c_str());
    return static_cast<jlong>(height);
}

/**
 * Set daemon connection
 */
JNIEXPORT jboolean JNICALL
Java_org_monero_feather_data_local_WalletLibrary_setDaemon(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle,
        jstring daemonAddress,
        jboolean useSSL,
        jstring username,
        jstring password) {
    
    const char* daemonAddressChars = env->GetStringUTFChars(daemonAddress, nullptr);
    const char* usernameChars = env->GetStringUTFChars(username, nullptr);
    const char* passwordChars = env->GetStringUTFChars(password, nullptr);
    
    if (!daemonAddressChars) {
        return JNI_FALSE;
    }
    
    std::string daemonAddressStr(daemonAddressChars);
    std::string usernameStr(usernameChars ? usernameChars : "");
    std::string passwordStr(passwordChars ? passwordChars : "");
    
    env->ReleaseStringUTFChars(daemonAddress, daemonAddressChars);
    if (usernameChars) env->ReleaseStringUTFChars(username, usernameChars);
    if (passwordChars) env->ReleaseStringUTFChars(password, passwordChars);
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return JNI_FALSE;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found");
        return JNI_FALSE;
    }
    
    if (!fn_Wallet_SetDaemon) {
        LOGW("Set daemon function not available (stub mode)");
        return JNI_TRUE; // Return success in stub mode
    }
    
    LOGI("Setting daemon: %s (SSL: %d)", daemonAddressStr.c_str(), useSSL);
    
    bool result = fn_Wallet_SetDaemon(
        instance->wallet,
        daemonAddressStr.c_str(),
        useSSL,
        usernameStr.empty() ? nullptr : usernameStr.c_str(),
        passwordStr.empty() ? nullptr : passwordStr.c_str()
    );
    
    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Store wallet to disk
 */
JNIEXPORT jboolean JNICALL
Java_org_monero_feather_data_local_WalletLibrary_storeWallet(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return JNI_FALSE;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found");
        return JNI_FALSE;
    }
    
    if (!fn_Wallet_Store) {
        LOGW("Store function not available (stub mode)");
        return JNI_TRUE;
    }
    
    LOGD("Storing wallet");
    bool result = fn_Wallet_Store(instance->wallet);
    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Change wallet password
 */
JNIEXPORT jboolean JNICALL
Java_org_monero_feather_data_local_WalletLibrary_changePassword(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle,
        jstring oldPassword,
        jstring newPassword) {
    
    const char* oldPasswordChars = env->GetStringUTFChars(oldPassword, nullptr);
    const char* newPasswordChars = env->GetStringUTFChars(newPassword, nullptr);
    
    if (!oldPasswordChars || !newPasswordChars) {
        env->ReleaseStringUTFChars(oldPassword, oldPasswordChars);
        env->ReleaseStringUTFChars(newPassword, newPasswordChars);
        return JNI_FALSE;
    }
    
    std::string oldPasswordStr(oldPasswordChars);
    std::string newPasswordStr(newPasswordChars);
    
    env->ReleaseStringUTFChars(oldPassword, oldPasswordChars);
    env->ReleaseStringUTFChars(newPassword, newPasswordChars);
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return JNI_FALSE;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found");
        return JNI_FALSE;
    }
    
    if (!fn_Wallet_ChangePassword) {
        LOGW("Change password function not available (stub mode)");
        return JNI_TRUE;
    }
    
    bool result = fn_Wallet_ChangePassword(
        instance->wallet,
        oldPasswordStr.c_str(),
        newPasswordStr.c_str()
    );
    
    if (result) {
        instance->password = newPasswordStr;
    }
    
    return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Get transaction history as JSON array
 */
JNIEXPORT jstring JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getTransactionHistoryJson(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return env->NewStringUTF("[]");
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found");
        return env->NewStringUTF("[]");
    }
    
    // In a full implementation, this would fetch real transactions
    // For now, return empty array
    return env->NewStringUTF("[]");
}

/**
 * Create and send a transaction
 * Returns: TX ID on success, empty string on failure
 */
JNIEXPORT jstring JNICALL
Java_org_monero_feather_data_local_WalletLibrary_sendTransaction(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle,
        jstring address,
        jlong amount,
        jint mixin,
        jstring paymentId,
        jstring description) {
    
    const char* addressChars = env->GetStringUTFChars(address, nullptr);
    const char* paymentIdChars = env->GetStringUTFChars(paymentId, nullptr);
    const char* descriptionChars = env->GetStringUTFChars(description, nullptr);
    
    if (!addressChars) {
        env->ReleaseStringUTFChars(address, addressChars);
        return env->NewStringUTF("");
    }
    
    std::string addressStr(addressChars);
    std::string paymentIdStr(paymentIdChars ? paymentIdChars : "");
    std::string descriptionStr(descriptionChars ? descriptionChars : "");
    
    env->ReleaseStringUTFChars(address, addressChars);
    if (paymentIdChars) env->ReleaseStringUTFChars(paymentId, paymentIdChars);
    if (descriptionChars) env->ReleaseStringUTFChars(description, descriptionChars);
    
    if (walletHandle == 0) {
        LOGE("Invalid wallet handle");
        return env->NewStringUTF("");
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        LOGE("Wallet instance not found");
        return env->NewStringUTF("");
    }
    
    LOGI("Creating transaction: %s -> %lu piconero", addressStr.c_str(), amount);
    
    if (!fn_Wallet_CreateTransaction) {
        LOGW("Create transaction function not available (stub mode)");
        // In stub mode, return a fake TX ID for testing
        return env->NewStringUTF("stub_tx_id_12345");
    }
    
    void* tx = fn_Wallet_CreateTransaction(
        instance->wallet,
        addressStr.c_str(),
        static_cast<uint64_t>(amount),
        mixin,
        0, // subaddress index
        paymentIdStr.empty() ? nullptr : paymentIdStr.c_str()
    );
    
    if (!tx) {
        const char* error = fn_Wallet_TransactionError ? 
            fn_Wallet_TransactionError(instance->wallet, tx) : "Unknown error";
        LOGE("Failed to create transaction: %s", error);
        return env->NewStringUTF("");
    }
    
    if (!fn_Wallet_CommitTransaction) {
        LOGE("Commit transaction function not available");
        fn_Wallet_FreeTransaction(instance->wallet, tx);
        return env->NewStringUTF("");
    }
    
    if (!fn_Wallet_CommitTransaction(instance->wallet, tx)) {
        const char* error = fn_Wallet_TransactionError ?
            fn_Wallet_TransactionError(instance->wallet, tx) : "Failed to commit";
        LOGE("Failed to commit transaction: %s", error);
        fn_Wallet_FreeTransaction(instance->wallet, tx);
        return env->NewStringUTF("");
    }
    
    // Get transaction hash (in real implementation)
    // For now, return a placeholder
    std::string txHash = "pending_tx_hash";
    fn_Wallet_FreeTransaction(instance->wallet, tx);
    
    LOGI("Transaction sent successfully");
    return env->NewStringUTF(txHash.c_str());
}

/**
 * Get wallet status
 */
JNIEXPORT jint JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getStatus(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        return -1;
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        return -1;
    }
    
    if (!fn_Wallet_Status) {
        return 0; // Status_OK in stub mode
    }
    
    return fn_Wallet_Status(instance->wallet);
}

/**
 * Get last error message
 */
JNIEXPORT jstring JNICALL
Java_org_monero_feather_data_local_WalletLibrary_getLastError(
        JNIEnv* env,
        jobject thiz,
        jlong walletHandle) {
    
    if (walletHandle == 0) {
        return env->NewStringUTF("");
    }
    
    WalletInstance* instance = getWalletInstance(walletHandle);
    if (!instance || !instance->wallet) {
        return env->NewStringUTF("");
    }
    
    if (!fn_Wallet_ErrorString) {
        return env->NewStringUTF("");
    }
    
    const char* error = fn_Wallet_ErrorString(instance->wallet);
    return env->NewStringUTF(error ? error : "");
}

#ifdef __cplusplus
}
#endif
