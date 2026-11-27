package net.moonbaseone.vera.shared.i18n

/** App UI locales (easily extensible). Repo docs stay English-only. */
enum class AppLocale(val tag: String) {
    NL("nl"),
    EN("en"),
    DE("de"),
    FR("fr"),
    ;

    /** Native language name for the picker (not translated). */
    val displayName: String
        get() = when (this) {
            NL -> "Nederlands"
            EN -> "English"
            DE -> "Deutsch"
            FR -> "Français"
        }

    companion object {
        fun fromTag(tag: String?): AppLocale {
            val normalized = tag?.lowercase()?.substringBefore('-').orEmpty()
            return entries.firstOrNull { it.tag == normalized } ?: NL
        }
    }
}
