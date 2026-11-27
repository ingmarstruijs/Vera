package net.moonbaseone.vera.shared.i18n

import android.content.Context

class AndroidLocalePreferenceStore(
    context: Context,
    prefsName: String = "vera_locale_prefs",
) : LocalePreferenceStore {
    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun read(): AppLocale? {
        val tag = prefs.getString(KEY, null) ?: return null
        return AppLocale.fromTag(tag)
    }

    override fun write(locale: AppLocale) {
        prefs.edit().putString(KEY, locale.tag).apply()
    }

    companion object {
        private const val KEY = "app_locale"
    }
}
