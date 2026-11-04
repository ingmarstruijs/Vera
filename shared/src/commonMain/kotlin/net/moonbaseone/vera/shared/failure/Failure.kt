package net.moonbaseone.vera.shared.failure

/** Typed failure hierarchy for Vera (exhaustive when sealed). */
sealed class Failure(val message: String)

// ── Vault ──────────────────────────────────────────────────────────────────

class VaultLockedFailure : Failure("Kluis is vergrendeld.")

class VaultKeyNotFoundFailure :
    Failure("Kluis-sleutel niet gevonden. Maak een nieuwe kluis aan.")

class VaultEncryptionFailure(message: String) : Failure(message)

class VaultSelfDestructFailure :
    Failure("Kluis gewist na te veel mislukte pogingen.")

class VaultNotSupportedFailure :
    Failure("Lokale kluis is niet beschikbaar op dit platform.")

// ── Auth ───────────────────────────────────────────────────────────────────

class AuthBiometricFailure(message: String) : Failure(message)

class AuthPinFailure(message: String) : Failure(message)

class AuthMaxAttemptsFailure :
    Failure("Maximum aantal pogingen bereikt. Kluis wordt gewist.")

// ── Translation / NER ──────────────────────────────────────────────────────

class TranslationModelNotDownloadedFailure(message: String) : Failure(message)

class TranslationFailure(message: String) : Failure(message)

class NerInitFailure(message: String) : Failure(message)

// ── FHIR / Data ────────────────────────────────────────────────────────────

class FhirParseFailure(message: String) : Failure(message)

class NetworkFailure(message: String) : Failure(message)

class UnexpectedFailure(message: String) : Failure(message)
