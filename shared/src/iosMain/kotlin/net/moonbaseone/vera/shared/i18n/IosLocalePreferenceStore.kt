package net.moonbaseone.vera.shared.i18n

import platform.Foundation.NSUserDefaults

class IosLocalePreferenceStore : LocalePreferenceStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): AppLocale? {
        val tag = defaults.stringForKey(KEY) ?: return null
        return AppLocale.fromTag(tag)
    }

    override fun write(locale: AppLocale) {
        defaults.setObject(locale.tag, KEY)
    }

    companion object {
        private const val KEY = "vera_app_locale"
    }
}
