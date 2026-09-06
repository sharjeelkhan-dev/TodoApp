package com.todoapp.data.remote

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceToTextParser @Inject constructor(
    private val app: Application
) : RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    val state = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun initRecognizer() {
        if (recognizer == null && SpeechRecognizer.isRecognitionAvailable(app)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
                setRecognitionListener(this@VoiceToTextParser)
            }
        }
    }

    fun startListening(languageCode: String = "en-US") {
        // Ensure execution happens on the Main Thread to avoid SpeechRecognizer thread checks exception
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(app)) {
                _state.update {
                    it.copy(
                        error = "Speech recognition is not available on this device",
                        isSpeaking = false
                    )
                }
                return@post
            }

            initRecognizer()

            // Reset previous state on new listening session
            _state.update {
                VoiceToTextParserState(
                    isSpeaking = true,
                    spokenText = "",
                    error = null
                )
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            recognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        mainHandler.post {
            _state.update { it.copy(isSpeaking = false) }
            recognizer?.stopListening()
        }
    }

    fun destroy() {
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update { it.copy(error = null) }
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) {
        _state.update { it.copy(rmsValue = rmsdB) }
    }

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_NO_MATCH) {
            _state.update { it.copy(isSpeaking = false) }
            return
        }

        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timeout"
            else -> "Speech recognition error ($error)"
        }

        _state.update {
            it.copy(
                error = errorMessage,
                isSpeaking = false
            )
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { result ->
                _state.update {
                    it.copy(
                        spokenText = result,
                        isSpeaking = false
                    )
                }
            }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let { result ->
                _state.update {
                    it.copy(spokenText = result)
                }
            }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val rmsValue: Float = 0f
)