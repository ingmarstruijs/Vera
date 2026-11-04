package net.moonbaseone.vera.shared.vault

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS Keychain-backed [SecureKeyStore] using generic password items.
 * Accessibility: when unlocked, this device only.
 */
@OptIn(ExperimentalForeignApi::class)
class IosSecureKeyStore(
    private val service: String = "net.moonbaseone.vera.vault",
) : SecureKeyStore {
    override suspend fun read(key: String): String? {
        val query = baseQuery(key).apply {
            setObject(true, forKey = kSecReturnData)
            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit)
        }
        memScoped {
            val out = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query.asCFDictionary(), out.ptr)
            if (status == errSecItemNotFound) return null
            check(status == errSecSuccess) { "Keychain read failed: $status" }
            val data = CFBridgingRelease(out.value) as? NSData ?: return null
            return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }
    }

    override suspend fun write(key: String, value: String) {
        val payload = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: error("Failed to encode Keychain value")
        val query = baseQuery(key)
        val attrs = NSMutableDictionary().apply {
            setObject(payload, forKey = kSecValueData)
        }
        val update = SecItemUpdate(query.asCFDictionary(), attrs.asCFDictionary())
        if (update == errSecSuccess) return
        if (update != errSecItemNotFound) error("Keychain update failed: $update")

        val add = baseQuery(key).apply {
            setObject(payload, forKey = kSecValueData)
        }
        val addStatus = SecItemAdd(add.asCFDictionary(), null)
        if (addStatus == errSecDuplicateItem) {
            val retry = SecItemUpdate(query.asCFDictionary(), attrs.asCFDictionary())
            check(retry == errSecSuccess) { "Keychain rewrite failed: $retry" }
        } else {
            check(addStatus == errSecSuccess) { "Keychain add failed: $addStatus" }
        }
    }

    override suspend fun delete(key: String) {
        val status = SecItemDelete(baseQuery(key).asCFDictionary())
        check(status == errSecSuccess || status == errSecItemNotFound) {
            "Keychain delete failed: $status"
        }
    }

    private fun baseQuery(account: String): NSMutableDictionary =
        NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass)
            setObject(service, forKey = kSecAttrService)
            setObject(account, forKey = kSecAttrAccount)
            setObject(kSecAttrAccessibleWhenUnlockedThisDeviceOnly, forKey = kSecAttrAccessible)
        }
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
private fun NSMutableDictionary.asCFDictionary(): CFDictionaryRef =
    this as CFDictionaryRef
