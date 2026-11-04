package net.moonbaseone.vera.shared.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech as AndroidTts
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
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
    ): Result<String> = suspendCancellableCoroutine { cont ->
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            cont.resume(Result.failure(IllegalStateException("Spraakherkenning niet beschikbaar")))
            return@suspendCancellableCoroutine
        }

        val speech = SpeechRecognizer.createSpeechRecognizer(context)
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
                if (cont.isActive) {
                    cont.resume(Result.success(text))
                }
                speech.destroy()
                recognizer = null
            }

            override fun onError(error: Int) {
                if (cont.isActive) {
                    cont.resume(
                        Result.failure(IllegalStateException("Spraakfout ($error)")),
                    )
                }
                speech.destroy()
                recognizer = null
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
        speech.startListening(intent)

        cont.invokeOnCancellation {
            speech.cancel()
            speech.destroy()
            recognizer = null
        }
    }

    override fun stop() {
        recognizer?.stopListening()
    }

    override fun cancel() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }
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
