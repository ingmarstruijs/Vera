package net.moonbaseone.vera.shared

/** Technical / behavioural constants (user-facing copy lives in i18n later). */
object AppConstants {
    const val MAX_FAILED_AUTH_ATTEMPTS = 5
    const val VAULT_KEY_STORAGE_KEY = "vera_vault_key_v1"
    const val FAILED_ATTEMPTS_KEY = "vera_failed_attempts"
    const val AES_KEY_BYTES = 32
    const val GCM_NONCE_BYTES = 12
    const val GCM_TAG_BITS = 128
    const val REDACTION_TOKEN = "[GEANONIMISEERD]"

    /** Dutch BSN is 8 or 9 digits; validated via 11-proof (elfproef). */
    val BSN_PATTERN = Regex("""\b\d{8,9}\b""")
}
