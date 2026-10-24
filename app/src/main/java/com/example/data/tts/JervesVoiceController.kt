package com.example.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

private const val TAG = "JervesVoiceController"

class JervesVoiceController(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(0.95f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.let { engine ->
                // Try British English for JARVIS flavor, fallback to default English
                val ukLocale = Locale.UK
                val result = engine.setLanguage(ukLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.language = Locale.ENGLISH
                }
                engine.setPitch(_pitch.value)
                engine.setSpeechRate(_speechRate.value)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
            Log.d(TAG, "TTS initialized successfully.")
        } else {
            Log.w(TAG, "TTS initialization failed with code: $status")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready for speak: $text")
            return
        }
        val utteranceId = UUID.randomUUID().toString()
        // Strip cybernetic markdown or raw bracket labels for cleaner pronunciation
        val cleanText = text
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("[*#_`]"), "")
            .trim()

        if (cleanText.isNotBlank()) {
            tts?.speak(cleanText, queueMode, null, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setPitch(newPitch: Float) {
        _pitch.value = newPitch
        tts?.setPitch(newPitch)
    }

    fun setSpeechRate(newRate: Float) {
        _speechRate.value = newRate
        tts?.setSpeechRate(newRate)
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
