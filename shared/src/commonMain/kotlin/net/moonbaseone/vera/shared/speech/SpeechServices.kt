package net.moonbaseone.vera.shared.speech

/**
 * On-device speech-to-text (appointments symptom logging).
 * Platform actuals: Android SpeechRecognizer / iOS SFSpeech.
 */
interface SpeechToText {
    suspend fun isAvailable(): Boolean

    /**
     * Starts a recognition session. Implementations may deliver partial updates
     * via [onPartial]; the returned [Result] is the final utterance (or failure).
     */
    suspend fun listen(
        localeTag: String = "nl-NL",
        onPartial: (String) -> Unit = {},
    ): Result<String>

    fun stop()
    fun cancel()
}

/** On-device text-to-speech (pocket guide read-aloud). */
interface TextToSpeech {
    suspend fun speak(text: String, localeTag: String = "nl-NL")
    fun stop()
}

class FakeSpeechToText(
    private val available: Boolean = true,
    private val transcript: String = "Vermoeidheid en lichte misselijkheid.",
) : SpeechToText {
    var listenCalls: Int = 0
        private set

    override suspend fun isAvailable(): Boolean = available

    override suspend fun listen(
        localeTag: String,
        onPartial: (String) -> Unit,
    ): Result<String> {
        listenCalls++
        onPartial(transcript)
        return Result.success(transcript)
    }

    override fun stop() = Unit
    override fun cancel() = Unit
}

class FakeTextToSpeech : TextToSpeech {
    var lastSpoken: String? = null
        private set

    override suspend fun speak(text: String, localeTag: String) {
        lastSpoken = text
    }

    override fun stop() = Unit
}
