package net.moonbaseone.vera.shared.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists the user's app UI language preference. */
interface LocalePreferenceStore {
    fun read(): AppLocale?
    fun write(locale: AppLocale)
}

class InMemoryLocalePreferenceStore : LocalePreferenceStore {
    private var stored: AppLocale? = null

    override fun read(): AppLocale? = stored

    override fun write(locale: AppLocale) {
        stored = locale
    }
}

/** Applies and remembers [StringCatalog] locale selection. */
class LocaleController(
    private val preferenceStore: LocalePreferenceStore,
) {
    private val _switchInProgress = MutableStateFlow(false)
    /** True while a user-driven (or pending) language switch is being applied/translated. */
    val switchInProgress: StateFlow<Boolean> = _switchInProgress.asStateFlow()

    fun bootstrap() {
        preferenceStore.read()?.let { StringCatalog.setLocale(it) }
    }

    fun select(locale: AppLocale) {
        if (locale == StringCatalog.activeLocale) return
        _switchInProgress.value = true
        StringCatalog.setLocale(locale)
        preferenceStore.write(locale)
    }

    fun markSwitchFinished() {
        _switchInProgress.value = false
    }
}
