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

    fun startListening(languageCode: String = "en-US") {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(app)) {
                _state.update {
                    it.copy(
                        error = "Speech recognition is not available",
                        isSpeaking = false
                    )
                }
                return@post
            }

            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
                setRecognitionListener(this@VoiceToTextParser)
            }

            _state.update {
                VoiceToTextParserState(
                    isSpeaking = true,
                    spokenText = "",
                    error = null,
                    isFinal = false
                )
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Attempt to extend silence timeouts
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
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

    fun reset() {
        _state.update { VoiceToTextParserState() }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update { it.copy(error = null) }
    }

    override fun onBeginningOfSpeech() {
        _state.update { it.copy(isSpeaking = true) }
    }

    override fun onRmsChanged(rmsdB: Float) {
        _state.update { it.copy(rmsValue = rmsdB) }
    }

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun onError(error: Int) {
        mainHandler.post {
            // Error 7 is NO_MATCH, Error 6 is SPEECH_TIMEOUT
            if (error == 7 || error == 6) {
                if (_state.value.spokenText.isNotBlank()) {
                    _state.update { it.copy(isSpeaking = false, isFinal = true) }
                } else {
                    _state.update { it.copy(isSpeaking = false, error = "No speech detected") }
                }
                return@post
            }

            if (error == 5) { // ERROR_CLIENT
                _state.update { it.copy(isSpeaking = false) }
                return@post
            }

            _state.update {
                it.copy(
                    error = "Error code: $error",
                    isSpeaking = false
                )
            }
        }
    }

    override fun onResults(results: Bundle?) {
        mainHandler.post {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.getOrNull(0)
                ?.let { result ->
                    _state.update {
                        it.copy(
                            spokenText = result,
                            isSpeaking = false,
                            isFinal = true
                        )
                    }
                }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        mainHandler.post {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.getOrNull(0)
                ?.let { result ->
                    _state.update {
                        it.copy(spokenText = result)
                    }
                }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val rmsValue: Float = 0f,
    val isFinal: Boolean = false
)