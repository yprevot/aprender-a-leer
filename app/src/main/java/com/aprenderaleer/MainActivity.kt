package com.aprenderaleer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.Insignia
import com.aprenderaleer.data.ProgressStore
import com.aprenderaleer.engine.SpeechEngine
import com.aprenderaleer.ui.PantallaInicio
import com.aprenderaleer.ui.PantallaLeccion
import com.aprenderaleer.ui.PantallaRecompensa
import com.aprenderaleer.ui.theme.AprenderALeerTheme
import com.aprenderaleer.ui.theme.Crema

/**
 * Actividad única. Sin permisos, sin red.
 *
 * · El TTS vive aquí para no reiniciarse al cambiar de pantalla.
 * · configChanges en el manifiesto evita recrear la Activity al girar el
 *   dispositivo, así el niño no pierde el ejercicio a medias.
 * · Desde targetSdk 35 el sistema dibuja SIEMPRE de borde a borde y ya no se
 *   puede renunciar a ello: se declara explícitamente con enableEdgeToEdge()
 *   —barras transparentes con iconos oscuros, que es lo que pide un tema
 *   claro— y el contenido se separa de barras y recortes con
 *   safeDrawingPadding().
 */
class MainActivity : ComponentActivity() {

    private lateinit var voz: SpeechEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        val barras = SystemBarStyle.light(Crema.toArgb(), Crema.toArgb())
        enableEdgeToEdge(statusBarStyle = barras, navigationBarStyle = barras)
        super.onCreate(savedInstanceState)
        // La pantalla no se apaga mientras el niño escucha una explicación.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        voz = SpeechEngine(this)
        val progreso = ProgressStore(this)

        setContent {
            AprenderALeerTheme {
                // El Surface pinta TODA la ventana (también detrás de las
                // barras y del recorte de la cámara, que en apaisado deja una
                // franja lateral); el contenido se aparta con
                // safeDrawingPadding dentro.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.safeDrawingPadding()) {
                        AppRaiz(progreso, voz)
                    }
                }
            }
        }
    }

    override fun onStop() {
        voz.callar()
        super.onStop()
    }

    override fun onDestroy() {
        voz.liberar()
        super.onDestroy()
    }
}

private sealed interface Ruta {
    data object Inicio : Ruta
    data class EnLeccion(val idLeccion: String) : Ruta
    data class Recompensa(
        val idLeccion: String,
        val estrellas: Int,
        val perfecta: Boolean,
        val insignias: List<Insignia>
    ) : Ruta
}

@Composable
private fun AppRaiz(progreso: ProgressStore, voz: SpeechEngine) {
    var ruta by remember { mutableStateOf<Ruta>(Ruta.Inicio) }
    var refresco by remember { mutableIntStateOf(0) }
    val todas = Curriculum.lecciones

    when (val actual = ruta) {
        is Ruta.Inicio -> PantallaInicio(
            progreso = progreso,
            voz = voz,
            refresco = refresco,
            onAbrirLeccion = { lec -> ruta = Ruta.EnLeccion(lec.id) }
        )

        is Ruta.EnLeccion -> {
            val lec = Curriculum.leccion(actual.idLeccion)
            if (lec == null) {
                ruta = Ruta.Inicio
            } else {
                PantallaLeccion(
                    leccion = lec,
                    progreso = progreso,
                    voz = voz,
                    onSalir = { voz.callar(); refresco++; ruta = Ruta.Inicio },
                    onCompletada = { estrellas, insignias ->
                        refresco++
                        ruta = Ruta.Recompensa(
                            idLeccion = lec.id,
                            estrellas = estrellas,
                            perfecta = insignias.any { it.id == "perfecta" },
                            insignias = insignias
                        )
                    }
                )
            }
        }

        is Ruta.Recompensa -> {
            val lec = Curriculum.leccion(actual.idLeccion)
            val idx = todas.indexOfFirst { it.id == actual.idLeccion }
            val siguiente = todas.getOrNull(idx + 1)
            PantallaRecompensa(
                tituloLeccion = lec?.titulo ?: "",
                estrellas = actual.estrellas,
                perfecta = actual.perfecta,
                insignias = actual.insignias,
                haySiguiente = siguiente != null,
                onSiguiente = {
                    voz.callar()
                    siguiente?.let { ruta = Ruta.EnLeccion(it.id) } ?: run { ruta = Ruta.Inicio }
                },
                onMapa = { voz.callar(); refresco++; ruta = Ruta.Inicio }
            )
        }
    }
}
