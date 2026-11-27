package net.moonbaseone.vera.shared.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech as AndroidTts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class AndroidSpeechToText(
    private val context: Context,
) : SpeechToText {
    private var recognizer: SpeechRecognizer? = null

    override suspend fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    override suspend fun listen(
        localeTag: String,
        onPartial: (String) -> Unit,
    ): Result<String> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                cont.resume(Result.failure(IllegalStateException("Spraakherkenning niet beschikbaar")))
                return@suspendCancellableCoroutine
            }

            val finished = AtomicBoolean(false)
            lateinit var speech: SpeechRecognizer

            fun finish(result: Result<String>) {
                if (!finished.compareAndSet(false, true)) return
                try {
                    speech.destroy()
                } catch (_: Exception) {
                    // Emulator / missing speech service can throw on destroy.
                }
                if (recognizer === speech) recognizer = null
                if (cont.isActive) cont.resume(result)
            }

            try {
                speech = SpeechRecognizer.createSpeechRecognizer(context)
            } catch (e: Exception) {
                cont.resume(
                    Result.failure(
                        IllegalStateException("Spraakherkenning niet beschikbaar", e),
                    ),
                )
                return@suspendCancellableCoroutine
            }
            recognizer = speech

            speech.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onPartialResults(partialResults: Bundle?) {
                    val texts = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    texts?.firstOrNull()?.let(onPartial)
                }

                override fun onResults(results: Bundle?) {
                    val texts = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = texts?.firstOrNull().orEmpty()
                    finish(Result.success(text))
                }

                override fun onError(error: Int) {
                    // Soft-fail codes that are common on emulators / quiet rooms.
                    if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                    ) {
                        finish(Result.success(""))
                        return
                    }
                    finish(
                        Result.failure(IllegalStateException(speechErrorMessage(error))),
                    )
                }
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag.replace('_', '-'))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            try {
                speech.startListening(intent)
            } catch (e: Exception) {
                finish(
                    Result.failure(
                        IllegalStateException("Spraakherkenning kon niet starten", e),
                    ),
                )
                return@suspendCancellableCoroutine
            }

            cont.invokeOnCancellation {
                try {
                    speech.cancel()
                } catch (_: Exception) {
                }
                // Empty success: caller treats cancel as a no-op, not an error toast.
                finish(Result.success(""))
            }
        }
    }

    override fun stop() {
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    override fun cancel() {
        val current = recognizer
        recognizer = null
        try {
            current?.cancel()
        } catch (_: Exception) {
        }
        try {
            current?.destroy()
        } catch (_: Exception) {
        }
    }
}

private fun speechErrorMessage(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_AUDIO -> "Microfoon niet beschikbaar"
    SpeechRecognizer.ERROR_CLIENT -> "Spraakdienst niet beschikbaar op dit apparaat"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Geen microfoontoegang"
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
        "Netwerkfout bij spraakherkenning"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Spraakherkenning is bezet"
    SpeechRecognizer.ERROR_SERVER -> "Spraakdienst gaf een fout"
    else -> "Spraakfout ($error)"
}

class AndroidTextToSpeech(
    context: Context,
) : TextToSpeech {
    private val tts = AndroidTts(context) { /* status ignored; speak waits for ready */ }
    private var ready = false

    init {
        tts.setOnUtteranceProgressListener(null)
        // Mark ready after engine binds; speak() re-checks language.
        tts.setLanguage(Locale("nl", "NL"))
        ready = true
    }

    override suspend fun speak(text: String, localeTag: String) {
        val parts = localeTag.replace('-', '_').split('_')
        val locale = when (parts.size) {
            2 -> Locale(parts[0], parts[1])
            else -> Locale(parts[0])
        }
        tts.language = locale
        tts.setSpeechRate(0.9f)
        tts.speak(text, AndroidTts.QUEUE_FLUSH, null, "vera-pocket")
    }

    override fun stop() {
        tts.stop()
    }
}
