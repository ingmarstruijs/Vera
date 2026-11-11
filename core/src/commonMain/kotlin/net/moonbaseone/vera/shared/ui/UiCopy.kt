package net.moonbaseone.vera.shared.ui

import net.moonbaseone.vera.shared.i18n.StringCatalog

/**
 * Compatibility facade over [StringCatalog] (EN/NL/DE/FR packs).
 * Prefer [StringCatalog.current] for new call sites.
 */
object UiCopy {
    private val s get() = StringCatalog.current

    val APP_NAME get() = s.appName
    val APP_TAGLINE get() = s.appTagline

    val SENTINEL_LOCAL_TITLE get() = s.sentinelLocalTitle
    val SENTINEL_LOCAL_SUBTITLE get() = s.sentinelLocalSubtitle

    val AUTH_TITLE get() = s.authTitle
    val AUTH_BIOMETRIC_PROMPT get() = s.authBiometricPrompt
    val AUTH_UNLOCK_BUTTON get() = s.authUnlockButton
    val AUTH_FAILED_PREFIX get() = s.authFailedPrefix
    val AUTH_SELF_DESTRUCT_WARNING get() = s.authSelfDestructWarning
    val AUTH_SELF_DESTRUCT_DONE get() = s.authSelfDestructDone
    val AUTH_SELF_DESTRUCT_TITLE get() = s.authSelfDestructTitle
    val AUTH_CREATE_TITLE get() = s.authCreateTitle
    val AUTH_CREATE_BODY get() = s.authCreateBody
    val AUTH_CREATE_BUTTON get() = s.authCreateButton
    val AUTH_LOADING get() = s.authLoading
    val OK get() = s.ok

    /** @deprecated Use [NAV_CARE_PLAN]; kept for older call sites. */
    val NAV_BLUEPRINT get() = s.navCarePlan
    val NAV_CARE_PLAN get() = s.navCarePlan
    val NAV_DECODER get() = s.navDecoder
    val NAV_APPOINTMENTS get() = s.navAppointments

    val BLUEPRINT_TITLE get() = s.blueprintTitle
    val BLUEPRINT_SUBTITLE get() = s.blueprintSubtitle
    val BLUEPRINT_PHASE_COMPLETED get() = s.blueprintPhaseCompleted
    val BLUEPRINT_PHASE_CURRENT get() = s.blueprintPhaseCurrent
    val BLUEPRINT_PHASE_FUTURE get() = s.blueprintPhaseFuture
    val BLUEPRINT_IKNL_SOURCE get() = s.blueprintIknlSource
    val BLUEPRINT_NO_DATA get() = s.blueprintNoData
    val BLUEPRINT_PLAIN_LANGUAGE get() = s.blueprintPlainLanguage
    val BLUEPRINT_ACTION_POINTS get() = s.blueprintActionPoints
    val BLUEPRINT_IKNL_LINK get() = s.blueprintIknlLink
    val BLUEPRINT_SCHEDULED_PREFIX get() = s.blueprintScheduledPrefix
    val BLUEPRINT_INSTRUCTION get() = s.blueprintInstruction
    val BLUEPRINT_NEXT_STEPS get() = s.blueprintNextSteps
    val BLUEPRINT_TAP_TERM_HINT get() = s.blueprintTapTermHint
    val BLUEPRINT_MODE_PLAIN get() = s.blueprintModePlain
    val BLUEPRINT_MODE_CLINICAL get() = s.blueprintModeClinical
    val BLUEPRINT_CLINICAL_LABEL get() = s.blueprintClinicalLabel

    val DECODER_TITLE get() = s.decoderTitle
    val DECODER_SUBTITLE get() = s.decoderSubtitle
    val DECODER_SEARCH_HINT get() = s.decoderSearchHint
    val DECODER_LANGUAGE_LABEL get() = s.decoderLanguageLabel
    val DECODER_PRIVACY_NOTE get() = s.decoderPrivacyNote
    val DECODER_ACTION_LABEL get() = s.decoderActionLabel
    val DECODER_IKNL_BUTTON get() = s.decoderIknlButton
    val DECODER_TRANSLATION_UNAVAILABLE get() = s.decoderTranslationUnavailable
    val DECODER_DOWNLOADING_MODEL get() = s.decoderDownloadingModel
    val DECODER_DOWNLOAD_FAILED get() = s.decoderDownloadFailed
    val DECODER_TRANSLATING get() = s.decoderTranslating
    val DECODER_NO_RESULTS get() = s.decoderNoResults

    val APPOINTMENTS_TITLE get() = s.appointmentsTitle
    val APPOINTMENTS_SUBTITLE get() = s.appointmentsSubtitle
    val APPOINTMENTS_POCKET_GUIDE get() = s.appointmentsPocketGuide
    val APPOINTMENTS_POCKET_TITLE get() = s.appointmentsPocketTitle
    val APPOINTMENTS_POCKET_PANEL get() = s.appointmentsPocketPanel
    val APPOINTMENTS_POCKET_FOOTER get() = s.appointmentsPocketFooter
    val APPOINTMENTS_REASON get() = s.appointmentsReason
    val APPOINTMENTS_SYMPTOMS_HEADER get() = s.appointmentsSymptomsHeader
    val APPOINTMENTS_SYMPTOMS_EMPTY get() = s.appointmentsSymptomsEmpty
    val APPOINTMENTS_SYMPTOMS_SECTION get() = s.appointmentsSymptomsSection
    val APPOINTMENTS_VOICE_LOG get() = s.appointmentsVoiceLog
    val APPOINTMENTS_VOICE_LISTENING get() = s.appointmentsVoiceListening
    val APPOINTMENTS_VOICE_STOP get() = s.appointmentsVoiceStop
    val APPOINTMENTS_SUGGESTED_QUESTIONS get() = s.appointmentsSuggestedQuestions
    val APPOINTMENTS_NO_DATA get() = s.appointmentsNoData
    val APPOINTMENTS_SPEECH_UNAVAILABLE get() = s.appointmentsSpeechUnavailable
    val APPOINTMENTS_CLEAR get() = s.appointmentsClear
    val APPOINTMENTS_READ_ALOUD get() = s.appointmentsReadAloud
    val APPOINTMENTS_NOTES_LABEL get() = s.appointmentsNotesLabel
    val APPOINTMENTS_META_DATE get() = s.appointmentsMetaDate
    val APPOINTMENTS_META_TYPE get() = s.appointmentsMetaType
    val APPOINTMENTS_META_LOCATION get() = s.appointmentsMetaLocation
    val APPOINTMENTS_MEMO get() = s.appointmentsMemo
    val APPOINTMENTS_EXTRA_NOTES get() = s.appointmentsExtraNotes
    val APPOINTMENTS_EXTRA_NOTES_HINT get() = s.appointmentsExtraNotesHint
    val APPOINTMENTS_OPEN_FULL get() = s.appointmentsOpenFull
    val APPOINTMENTS_CLOSE_FULL get() = s.appointmentsCloseFull
    val APPOINTMENTS_SAVE_GUIDE get() = s.appointmentsSaveGuide
    val APPOINTMENTS_GUIDE_SAVED get() = s.appointmentsGuideSaved
    val APPOINTMENTS_OPEN_SAVED get() = s.appointmentsOpenSaved
    val APPOINTMENTS_SAVED_GUIDES get() = s.appointmentsSavedGuides
    val APPOINTMENTS_DELETE_NOTE get() = s.appointmentsDeleteNote
    val APPOINTMENTS_RE_RECORD get() = s.appointmentsReRecord
    val APPOINTMENTS_READ_SYMPTOM get() = s.appointmentsReadSymptom
    val APPOINTMENTS_CONTACTS_TITLE get() = s.appointmentsContactsTitle
    val APPOINTMENTS_CONTACTS_SUBTITLE get() = s.appointmentsContactsSubtitle
    val APPOINTMENTS_CALL get() = s.appointmentsCall

    val APPOINTMENTS_DEFAULT_QUESTIONS: List<String>
        get() = s.appointmentsDefaultQuestions
}
