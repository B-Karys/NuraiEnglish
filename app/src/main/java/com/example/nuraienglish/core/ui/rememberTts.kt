package com.example.nuraienglish.core.ui

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Returns a stable lambda that speaks [text] in English using the device TTS engine.
 * The engine is created once and shut down when the composable leaves the composition.
 */
@Composable
fun rememberSpeakEnglish(): (String) -> Unit {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.setLanguage(Locale.US)
            }
        }
        tts = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
            tts = null
        }
    }

    return remember(tts) { { text: String ->
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }}
}
