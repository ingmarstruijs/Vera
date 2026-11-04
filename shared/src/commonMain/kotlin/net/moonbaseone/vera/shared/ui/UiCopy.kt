package net.moonbaseone.vera.shared.ui

/**
 * Dutch-primary UI copy for shell / auth / blueprint / decoder / appointments.
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

    const val DECODER_TITLE = "Slim Systeem Decoder"
    const val DECODER_SUBTITLE =
        "Medische vaktaal vertaald naar begrijpelijk Nederlands"
    const val DECODER_SEARCH_HINT = "Zoek medische term…"
    const val DECODER_LANGUAGE_LABEL = "Doeltaal"
    const val DECODER_PRIVACY_NOTE =
        "Vertaling vindt volledig op uw apparaat plaats. " +
            "Geen tekst wordt naar externe servers verstuurd."
    const val DECODER_ACTION_LABEL = "Actiepunt"
    const val DECODER_IKNL_BUTTON = "IKNL Richtlijn"
    const val DECODER_TRANSLATION_PENDING =
        "On-device vertaling naar andere talen volgt in P7 (ML Kit). " +
            "De Nederlandse uitleg blijft beschikbaar."
    const val DECODER_NO_RESULTS = "Geen termen gevonden voor deze zoekopdracht."

    const val APPOINTMENTS_TITLE = "Afspraak Navigator"
    const val APPOINTMENTS_SUBTITLE = "Uw gepersonaliseerde afsprakengids"
    const val APPOINTMENTS_POCKET_GUIDE = "Genereer Pocket Gids"
    const val APPOINTMENTS_POCKET_TITLE = "# Pocket Gids voor uw afspraak"
    const val APPOINTMENTS_POCKET_PANEL = "Uw Pocket Gids"
    const val APPOINTMENTS_POCKET_FOOTER =
        "Gegenereerd door Vera – IKNL Richtlijn Borstkanker 2023"
    const val APPOINTMENTS_REASON = "Reden van bezoek"
    const val APPOINTMENTS_SYMPTOMS_HEADER = "Uw ingesproken symptomen"
    const val APPOINTMENTS_SYMPTOMS_EMPTY =
        "(geen symptomen geregistreerd – gebruik de spraakfunctie)"
    const val APPOINTMENTS_SYMPTOMS_SECTION = "Symptomen registreren"
    const val APPOINTMENTS_VOICE_LOG = "Symptomen inspreken"
    const val APPOINTMENTS_VOICE_LISTENING = "Luistert…"
    const val APPOINTMENTS_VOICE_STOP = "Stop opname"
    const val APPOINTMENTS_SUGGESTED_QUESTIONS =
        "Aanbevolen vragen voor uw arts"
    const val APPOINTMENTS_NO_DATA = "Geen aankomende afspraken gevonden."
    const val APPOINTMENTS_SPEECH_UNAVAILABLE =
        "Spraakregistratie is niet beschikbaar op dit apparaat."
    const val APPOINTMENTS_CLEAR = "Wissen"
    const val APPOINTMENTS_READ_ALOUD = "Voorlezen"
    const val APPOINTMENTS_NOTES_LABEL = "Geregistreerde symptomen:"

    val APPOINTMENTS_DEFAULT_QUESTIONS: List<String> = listOf(
        "Wat zijn de resultaten van mijn laatste LVEF-meting?",
        "Zijn er bijwerkingen die ik moet melden?",
        "Wanneer verwacht u de volgende behandelstap?",
        "Wat kan ik zelf doen om mijn herstel te bevorderen?",
        "Zijn er richtlijnwijzigingen die invloed hebben op mijn zorgplan?",
    )
}
