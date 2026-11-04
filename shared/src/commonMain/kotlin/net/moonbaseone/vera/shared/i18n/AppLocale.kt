package net.moonbaseone.vera.shared.i18n

/** App UI locales (easily extensible). Repo docs stay English-only. */
enum class AppLocale(val tag: String) {
    EN("en"),
    NL("nl"),
    DE("de"),
    FR("fr"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLocale {
            val normalized = tag?.lowercase()?.substringBefore('-').orEmpty()
            return entries.firstOrNull { it.tag == normalized } ?: NL
        }
    }
}
