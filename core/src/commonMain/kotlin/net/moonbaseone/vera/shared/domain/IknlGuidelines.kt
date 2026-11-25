package net.moonbaseone.vera.shared.domain

/**
 * Canonical public URLs for Dutch oncology guidelines on Richtlijnendatabase
 * (Federatie Medisch Specialisten / IKNL-ondersteunde richtlijnen).
 *
 * Prefer these over outdated iknl.nl guideline path aliases.
 */
object IknlGuidelines {
    const val BREAST =
        "https://richtlijnendatabase.nl/richtlijn/borstkanker/startpagina_-_borstkanker.html"
    const val COLON =
        "https://richtlijnendatabase.nl/richtlijn/colorectaal_carcinoom_crc/startpagina_-_colorectaal_carcinoom.html"
    const val LUNG_NSCLC =
        "https://richtlijnendatabase.nl/richtlijn/niet_kleincellig_longcarcinoom/niet_kleincellig_longcarcinoom.html"
}
