package io.github.differentsyntax.karado.guide

import android.content.Context
import android.speech.tts.TextToSpeech
import io.github.differentsyntax.karado.recipes.Lang
import java.util.Locale

/** Speaks each step with the phone's own (offline-capable) text-to-speech. */
internal class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pending: Pair<String, Lang>? = null

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        pending?.let { (text, lang) -> say(text, lang) }
        pending = null
    }

    fun say(text: String, lang: Lang) {
        if (!ready) {
            pending = text to lang
            return
        }
        tts.language = when (lang) {
            Lang.HI -> Locale("hi", "IN")
            Lang.EN -> Locale("en", "IN")
        }
        tts.setSpeechRate(0.85f) // a little slower than default for older listeners
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kara-do-step")
    }

    fun stop() = tts.stop()

    fun shutdown() = tts.shutdown()
}
