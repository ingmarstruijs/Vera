package net.moonbaseone.vera.shared.ui

/**
 * Dutch-primary UI copy for the P5 shell / auth / blueprint port.
 * Full i18n (EN/NL/DE/FR) lands in a later phase.
 */
object UiCopy {
    const val APP_NAME = "Vera"
    const val APP_TAGLINE = "Jouw persoonlijke oncologie kompas"

    const val SENTINEL_LOCAL_TITLE = "Gegevens lokaal beveiligd"
    const val SENTINEL_LOCAL_SUBTITLE = "Geen medische gegevens hebben uw apparaat verlaten."

    const val AUTH_TITLE = "Ontgrendel Vera"
    const val AUTH_BIOMETRIC_PROMPT =
        "Gebruik uw biometrische gegevens om uw Vera kluis te openen"
    const val AUTH_UNLOCK_BUTTON = "Ontgrendelen"
    const val AUTH_FAILED_PREFIX = "Verificatie mislukt. Pogingen resterend: "
    const val AUTH_SELF_DESTRUCT_WARNING =
        "WAARSCHUWING: Na nog één mislukte poging wordt de kluis gewist."
    const val AUTH_SELF_DESTRUCT_DONE =
        "Kluis definitief gewist ter bescherming van uw privacy."
    const val AUTH_SELF_DESTRUCT_TITLE = "Kluis gewist"
    const val AUTH_CREATE_TITLE = "Kluis aanmaken"
    const val AUTH_CREATE_BODY =
        "Uw medische gegevens worden versleuteld opgeslagen met AES-256. " +
            "Stel een PIN of biometrie in als extra beveiligingslaag."
    const val AUTH_CREATE_BUTTON = "Beveiligde kluis aanmaken"
    const val AUTH_LOADING = "Bezig…"
    const val OK = "OK"

    const val NAV_BLUEPRINT = "Tijdlijn"
    const val NAV_DECODER = "Decoder"
    const val NAV_APPOINTMENTS = "Afspraken"

    const val BLUEPRINT_TITLE = "Uw Tijdlijn"
    const val BLUEPRINT_SUBTITLE = "Uw diagnose afgezet tegen IKNL-richtlijnen"
    const val BLUEPRINT_PHASE_COMPLETED = "Afgerond"
    const val BLUEPRINT_PHASE_CURRENT = "Huidig"
    const val BLUEPRINT_PHASE_FUTURE = "Gepland"
    const val BLUEPRINT_IKNL_SOURCE = "Bron: IKNL Richtlijnendatabase"
    const val BLUEPRINT_NO_DATA =
        "Geen tijdlijngegevens beschikbaar. Importeer een FHIR-rapport."
    const val BLUEPRINT_PLAIN_LANGUAGE = "Begrijpelijke uitleg"
    const val BLUEPRINT_ACTION_POINTS = "Actiepunten:"
    const val BLUEPRINT_IKNL_LINK = "IKNL Richtlijn raadplegen"
    const val BLUEPRINT_SCHEDULED_PREFIX = "Gepland: "

    const val DECODER_PLACEHOLDER =
        "Medische vaktaal-decoder volgt in een latere fase."
    const val APPOINTMENTS_PLACEHOLDER =
        "Afspraaknavigator volgt in een latere fase."
}
