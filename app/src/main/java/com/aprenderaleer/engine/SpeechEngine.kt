package com.aprenderaleer.engine

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * Voz del juego (Text-To-Speech del sistema).
 *
 * No necesita permisos ni conexión: usa el motor TTS instalado en el
 * dispositivo. Se configura en español y con velocidad reducida, porque a
 * 5 años el niño necesita oír la sílaba completa antes de decidir.
 */
class SpeechEngine(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private val main = Handler(Looper.getMainLooper())
    private val contador = AtomicLong(0)
    private val callbacks = HashMap<String, () -> Unit>()

    /** El motor ya se inicializó correctamente. */
    var listo by mutableStateOf(false)
        private set

    /** Hay voz en español disponible. */
    var vozEspanolDisponible by mutableStateOf(true)
        private set

    /** Está sonando algo ahora mismo (para animar la boca del avatar). */
    var hablando by mutableStateOf(false)
        private set

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                aplicarIdioma()
                tts?.setSpeechRate(VELOCIDAD_NORMAL)
                tts?.setPitch(TONO)
                tts?.setOnUtteranceProgressListener(listener)
                listo = true
            } else {
                listo = false
            }
        }
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            main.post { hablando = true }
        }

        override fun onDone(utteranceId: String?) {
            main.post {
                hablando = false
                utteranceId?.let { id -> callbacks.remove(id)?.invoke() }
            }
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onError(utteranceId: String?) {
            main.post {
                hablando = false
                utteranceId?.let { id -> callbacks.remove(id)?.invoke() }
            }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            onError(utteranceId)
        }
    }

    private fun aplicarIdioma() {
        val candidatos = listOf(
            Locale("es", "US"),
            Locale("es", "ES"),
            Locale("es", "MX"),
            Locale("es")
        )
        for (loc in candidatos) {
            val r = tts?.setLanguage(loc) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                vozEspanolDisponible = true
                return
            }
        }
        vozEspanolDisponible = false
    }

    /**
     * Dice un texto.
     * @param lento se usa al presentar una letra o sílaba nueva.
     */
    fun decir(texto: String, lento: Boolean = false, alTerminar: (() -> Unit)? = null) {
        val motor = tts ?: return
        if (texto.isBlank()) { alTerminar?.invoke(); return }
        motor.setSpeechRate(if (lento) VELOCIDAD_LENTA else VELOCIDAD_NORMAL)
        val id = "u${contador.incrementAndGet()}"
        alTerminar?.let { callbacks[id] = it }
        motor.speak(texto, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
    }

    /**
     * Encadena varios fragmentos con una pausa entre ellos. Útil para
     * "ma... me... mi... mo... mu" o para separar el nombre del sonido.
     */
    fun decirSecuencia(
        partes: List<String>,
        pausaMs: Long = 320,
        lento: Boolean = true,
        alTerminar: (() -> Unit)? = null
    ) {
        val motor = tts ?: return
        val limpias = partes.filter { it.isNotBlank() }
        if (limpias.isEmpty()) { alTerminar?.invoke(); return }
        motor.setSpeechRate(if (lento) VELOCIDAD_LENTA else VELOCIDAD_NORMAL)

        limpias.forEachIndexed { i, parte ->
            val modo = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val id = "u${contador.incrementAndGet()}"
            if (i == limpias.lastIndex) alTerminar?.let { callbacks[id] = it }
            motor.speak(parte, modo, Bundle(), id)
            if (i != limpias.lastIndex) {
                motor.playSilentUtterance(pausaMs, TextToSpeech.QUEUE_ADD, "s$id")
            }
        }
    }

    /** Deletrea una palabra sílaba a sílaba y luego la dice entera. */
    fun decirPorSilabas(silabas: List<String>, palabra: String, alTerminar: (() -> Unit)? = null) {
        decirSecuencia(silabas + listOf(palabra), pausaMs = 380, lento = true, alTerminar = alTerminar)
    }

    fun callar() {
        tts?.stop()
        hablando = false
        callbacks.clear()
    }

    fun liberar() {
        callbacks.clear()
        tts?.stop()
        tts?.shutdown()
        tts = null
        listo = false
    }

    private companion object {
        const val VELOCIDAD_NORMAL = 0.88f
        const val VELOCIDAD_LENTA = 0.68f
        const val TONO = 1.08f
    }
}
