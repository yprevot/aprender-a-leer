package com.aprenderaleer.engine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Reconocimiento de voz SIN PEDIR PERMISOS.
 *
 * En lugar de instanciar un SpeechRecognizer dentro del proceso (que exigiría
 * declarar y solicitar android.permission.RECORD_AUDIO), lanzamos
 * RecognizerIntent.ACTION_RECOGNIZE_SPEECH: el propio sistema abre su cuadro
 * de diálogo de micrófono, graba, transcribe y nos devuelve SOLO el texto.
 * Nuestra app nunca accede al micrófono, así que el manifiesto queda limpio
 * y el niño (o su familia) no ve ningún diálogo de permisos.
 *
 * Contrapartida: aparece la ventana del sistema durante la grabación. Se
 * compensa con un prompt grande y claro y con el aviso previo del avatar.
 */
class VoiceRecognizer internal constructor(
    private val lanzar: (Intent) -> Unit
) {
    var disponible by mutableStateOf(true)
        internal set

    var escuchando by mutableStateOf(false)
        internal set

    /**
     * Abre el diálogo del sistema para que el niño hable.
     * @param textoPrompt lo que se muestra dentro del diálogo ("Di: pa")
     */
    fun escuchar(textoPrompt: String) {
        if (!disponible) return
        val intent = crearIntent(textoPrompt)
        try {
            escuchando = true
            lanzar(intent)
        } catch (e: ActivityNotFoundException) {
            escuchando = false
            disponible = false
        }
    }

    companion object {
        fun crearIntent(textoPrompt: String): Intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                // Pedimos varias alternativas: el evaluador fonético las prueba
                // todas, lo que reduce muchísimo los falsos negativos.
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 8)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_PROMPT, textoPrompt)
                // Márgenes de silencio generosos: un niño tarda en arrancar.
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1800L
                )
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 900L)
            }

        fun hayReconocedor(context: Context): Boolean {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            val pm = context.packageManager
            return pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
        }
    }
}

/**
 * Crea el reconocedor ligado al ciclo de vida de la composición.
 * @param onResultado recibe la lista de transcripciones (vacía si no se oyó nada)
 */
@Composable
fun rememberVoiceRecognizer(onResultado: (List<String>) -> Unit): VoiceRecognizer {
    val context = LocalContext.current
    // Caja mutable normal (no estado de Compose) para romper la dependencia
    // circular launcher <-> recognizer sin provocar recomposiciones.
    val caja = remember { arrayOfNulls<VoiceRecognizer>(1) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        caja[0]?.escuchando = false
        val textos = if (resultado.resultCode == Activity.RESULT_OK) {
            resultado.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.toList()
                ?: emptyList()
        } else {
            emptyList()
        }
        onResultado(textos)
    }

    return remember {
        VoiceRecognizer { intent -> launcher.launch(intent) }.also {
            it.disponible = VoiceRecognizer.hayReconocedor(context)
            caja[0] = it
        }
    }
}
