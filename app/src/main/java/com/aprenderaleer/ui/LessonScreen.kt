@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aprenderaleer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprenderaleer.data.Ejercicio
import com.aprenderaleer.data.Insignia
import com.aprenderaleer.data.Leccion
import com.aprenderaleer.data.ProgressStore
import com.aprenderaleer.data.TipoEjercicio
import com.aprenderaleer.engine.ExerciseGenerator
import com.aprenderaleer.engine.SpeechEngine
import com.aprenderaleer.engine.rememberVoiceRecognizer
import com.aprenderaleer.ui.theme.Amarillo
import com.aprenderaleer.ui.theme.Azul
import com.aprenderaleer.ui.theme.Verde
import kotlinx.coroutines.delay

@Composable
fun PantallaLeccion(
    leccion: Leccion,
    progreso: ProgressStore,
    voz: SpeechEngine,
    onSalir: () -> Unit,
    onCompletada: (Int, List<Insignia>) -> Unit
) {
    val hayVoz = progreso.vozActivada
    val ejercicios = remember(leccion.id) {
        ExerciseGenerator.generar(leccion, progreso::peso, incluirVoz = hayVoz)
    }
    val ctrl = remember(leccion.id) { LessonController(leccion, ejercicios, progreso, voz) }

    val reconocedor = rememberVoiceRecognizer { textos -> ctrl.procesarVoz(textos) }

    // Si el dispositivo no tiene reconocedor, avisamos y el ejercicio de voz
    // se puede validar a mano (el adulto confirma) para no bloquear el juego.
    LaunchedEffect(leccion.id) {
        if (ctrl.fase == FaseLeccion.ENSENANZA) ctrl.narrarEnsenanza() else ctrl.presentarEjercicio()
    }

    // Avance automático tras acertar (o tras revelar la respuesta).
    LaunchedEffect(ctrl.fase, ctrl.indice, ctrl.acierto, ctrl.revelada) {
        if (ctrl.fase == FaseLeccion.RESULTADO) {
            delay(if (ctrl.revelada) 3400L else 1700L)
            if (ctrl.fase == FaseLeccion.RESULTADO) ctrl.avanzar()
        }
    }

    LaunchedEffect(ctrl.fase) {
        if (ctrl.fase == FaseLeccion.FINAL) {
            delay(400)
            onCompletada(ctrl.estrellas, ctrl.insigniasGanadas.toList())
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            val ancho = maxWidth
            val alto = maxHeight
            val esGrande = ancho >= 700.dp
            val apaisado = ancho > alto

            Column(Modifier.fillMaxSize()) {
                BarraSuperior(ctrl, onSalir)

                if (esGrande || apaisado) {
                    Row(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            Modifier
                                .width(if (esGrande) 300.dp else 230.dp)
                                .fillMaxHeight()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarLalo(ctrl.mood, tamano = if (esGrande) 190.dp else 140.dp)
                            Spacer(Modifier.height(8.dp))
                            Bocadillo(ctrl.mensaje)
                            Spacer(Modifier.height(12.dp))
                            BotonEscuchar(
                                onClick = { ctrl.repetirConsigna() },
                                etiqueta = "Otra vez"
                            )
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp)
                        ) {
                            ContenidoLeccion(ctrl, reconocedor.disponible, esGrande) { prompt ->
                                ctrl.pedirVoz()
                                reconocedor.escuchar(prompt)
                            }
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarLalo(ctrl.mood, tamano = 104.dp)
                            Spacer(Modifier.width(6.dp))
                            Bocadillo(ctrl.mensaje, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.weight(1f)) {
                            ContenidoLeccion(ctrl, reconocedor.disponible, esGrande) { prompt ->
                                ctrl.pedirVoz()
                                reconocedor.escuchar(prompt)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraSuperior(ctrl: LessonController, onSalir: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSalir) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
        }
        Column(Modifier.weight(1f)) {
            Text(
                ctrl.leccion.titulo,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            BarraAvance(ctrl.indice, ctrl.total)
        }
        Spacer(Modifier.width(10.dp))
        CintaEstado(ctrl.estrellas, ctrl.racha)
    }
}

// ---------------------------------------------------------------- contenido

@Composable
private fun ContenidoLeccion(
    ctrl: LessonController,
    hayReconocedor: Boolean,
    esGrande: Boolean,
    onHablar: (String) -> Unit
) {
    when (ctrl.fase) {
        FaseLeccion.ENSENANZA -> BloqueEnsenanza(ctrl, esGrande)
        else -> {
            val ej = ctrl.ejercicio
            if (ej == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("¡Listo!", style = MaterialTheme.typography.displaySmall)
                }
            } else {
                when (ej.tipo) {
                    TipoEjercicio.MIRA_Y_DI ->
                        BloqueMiraYDi(ctrl, ej, hayReconocedor, onHablar)

                    TipoEjercicio.ARMA_LA_PALABRA ->
                        BloqueArmaPalabra(ctrl, ej, esGrande)

                    else -> BloqueElegirOpcion(ctrl, ej, esGrande)
                }
            }
        }
    }
}

// ------------------------------------------------------------- enseñanza

@Composable
private fun BloqueEnsenanza(ctrl: LessonController, esGrande: Boolean) {
    val paso = ctrl.pasoEnsenanza ?: return
    val letra = paso.letra
    // Los dígrafos ocupan dos caracteres ("CH", "rr"): hay que bajar el
    // cuerpo para que quepan las dos formas una al lado de la otra.
    val tamanoGlifo = when {
        paso.glifo.length > 1 -> if (esGrande) 62 else 44
        esGrande -> 104
        else -> 72
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CartelLetra(
            glifo = paso.glifo,
            etiquetaCaja = "La ${letra.nombreAlfabeto} ${paso.etiquetaCaja}",
            emoji = letra.emojiEjemplo,
            onEscuchar = { ctrl.escucharPasoEnsenanza() },
            tamano = tamanoGlifo,
            modifier = Modifier.fillMaxWidth(if (esGrande) 0.7f else 1f)
        )
        Spacer(Modifier.height(14.dp))

        // Nombre vs sonido: la distinción que pide el método. El sonido solo
        // se explica en la minúscula; la mayúscula ya es la misma letra.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FichaDato(
                titulo = "Se llama",
                valor = letra.nombreAlfabeto,
                color = Azul,
                modifier = Modifier.weight(1f)
            ) { ctrl.escucharOpcion("Se llama ${letra.nombreAlfabeto}") }

            if (!paso.esMayuscula) {
                FichaDato(
                    titulo = "En la palabra suena",
                    valor = when {
                        letra.id == "h" -> "no suena"
                        letra.sonidoSostenido != null -> letra.sonidoSostenido
                        else -> letra.silabas.firstOrNull() ?: letra.minuscula
                    },
                    color = Verde,
                    modifier = Modifier.weight(1f)
                ) { ctrl.escucharSonido() }
            }
        }

        if (paso.esMayuscula) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x14000000)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "🔠 La mayúscula se usa al empezar una frase y en los nombres.",
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!paso.esMayuscula && letra.silabas.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Se combina así:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                letra.silabas.forEach { s ->
                    Surface(
                        onClick = { ctrl.escucharOpcion(s) },
                        shape = RoundedCornerShape(18.dp),
                        color = Amarillo.copy(alpha = 0.25f),
                        border = BorderStroke(2.dp, Amarillo),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            s,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = if (esGrande) 34.sp else 26.sp,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        if (!paso.esMayuscula) {
            letra.notaDidactica?.let {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x14000000)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "💡 $it",
                        Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { ctrl.narrarEnsenanza() }) {
                Text("🔊 Repetir")
            }
            Button(
                onClick = { ctrl.siguienteEnsenanza() },
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) {
                Text(ctrl.etiquetaSiguienteEnsenanza, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Letra ${ctrl.numeroLetraEnsenanza} de ${ctrl.leccion.letrasEnsenadas.size}" +
                " · ${paso.etiquetaCaja}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FichaDato(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(2.dp, color)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "«$valor»",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text("🔊", fontSize = 18.sp)
        }
    }
}

// ---------------------------------------------------------- elegir opción

@Composable
private fun BloqueElegirOpcion(ctrl: LessonController, ej: Ejercicio, esGrande: Boolean) {
    // Andamiaje: al segundo fallo dejamos solo la correcta y un distractor.
    val atenuadas = remember(ej.id, ctrl.mostrarPista) {
        if (!ctrl.mostrarPista) emptySet()
        else ej.opciones.map { it.id }.filter { it != ej.idCorrecto }.drop(1).toSet()
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            ej.consigna,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        ej.emoji?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, fontSize = 58.sp)
        }
        Spacer(Modifier.height(8.dp))
        BotonEscuchar(onClick = { ctrl.repetirConsigna() }, tamano = 60.dp, etiqueta = "Escuchar")
        Spacer(Modifier.height(10.dp))

        val esTexto = ej.opciones.any { it.texto.length > 3 }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (esGrande) 200.dp else 150.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ej.opciones, key = { it.id }) { op ->
                val estado = when {
                    ctrl.revelada && op.id == ej.idCorrecto -> EstadoOpcion.CORRECTA
                    ctrl.acierto == true && op.id == ctrl.seleccion -> EstadoOpcion.CORRECTA
                    ctrl.acierto == false && op.id == ctrl.seleccion -> EstadoOpcion.INCORRECTA
                    op.id in atenuadas -> EstadoOpcion.ATENUADA
                    else -> EstadoOpcion.NEUTRO
                }
                TarjetaOpcion(
                    texto = op.texto,
                    emoji = op.emoji,
                    estado = estado,
                    habilitada = ctrl.acierto != true && !ctrl.revelada,
                    onClick = { ctrl.responderToque(op.id) },
                    tamanoTexto = if (esTexto) 30 else if (esGrande) 62 else 48,
                    manuscrita = op.manuscrita
                )
            }
        }
        PieResultado(ctrl)
    }
}

// ----------------------------------------------------------- mira y di

@Composable
private fun BloqueMiraYDi(
    ctrl: LessonController,
    ej: Ejercicio,
    hayReconocedor: Boolean,
    onHablar: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            ej.consigna,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        CartelObjetivo(
            texto = ej.objetivoVisible ?: "",
            emoji = ej.emoji,
            tamano = if ((ej.objetivoVisible?.length ?: 0) > 4) 52 else 96,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        Spacer(Modifier.height(16.dp))

        if (hayReconocedor) {
            BotonMicrofono(
                escuchando = ctrl.esperandoVoz,
                habilitado = ctrl.acierto != true,
                onClick = { onHablar("Di: ${ej.objetivoVisible}") }
            )
        } else {
            Text(
                "Este dispositivo no tiene reconocimiento de voz.\n" +
                    "Dilo en voz alta y toca «Ya lo dije».",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { ctrl.procesarVoz(ej.respuestasAceptadas.take(1)) }) {
                Text("✅ Ya lo dije")
            }
        }

        ctrl.textoEscuchado?.let {
            Spacer(Modifier.height(10.dp))
            Text("Escuché: «$it»", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { ctrl.escucharOpcion(ej.objetivoVisible ?: "") }) {
                Text("🔊 Óyelo")
            }
            OutlinedButton(onClick = { ctrl.avanzar() }) {
                Text("Saltar ➡")
            }
        }
        PieResultado(ctrl)
        Spacer(Modifier.height(20.dp))
    }
}

// --------------------------------------------------------- armar palabra

@Composable
private fun BloqueArmaPalabra(ctrl: LessonController, ej: Ejercicio, esGrande: Boolean) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            ej.consigna,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        ej.emoji?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 64.sp)
        }
        Spacer(Modifier.height(10.dp))

        // Casillas destino
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ej.secuenciaCorrecta.indices.forEach { i ->
                val texto = ctrl.armado.getOrNull(i)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (texto == null) Color(0x11000000) else Amarillo.copy(alpha = 0.3f),
                    border = BorderStroke(3.dp, if (texto == null) Color(0x33000000) else Amarillo),
                    modifier = Modifier.size(
                        width = if (esGrande) 104.dp else 78.dp,
                        height = if (esGrande) 82.dp else 66.dp
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            texto ?: "_",
                            fontSize = if (esGrande) 40.sp else 30.sp,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
            IconButton(onClick = { ctrl.borrarUltimaSilaba() }) {
                Icon(Icons.Filled.Backspace, contentDescription = "Borrar")
            }
        }

        Spacer(Modifier.height(8.dp))
        BotonEscuchar(onClick = { ctrl.repetirConsigna() }, tamano = 56.dp, etiqueta = "Escuchar")
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (esGrande) 170.dp else 120.dp),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ej.opciones, key = { it.id }) { op ->
                val yaUsada = ctrl.armado.count { it == op.texto } >=
                    ej.opciones.count { it.texto == op.texto }
                TarjetaOpcion(
                    texto = op.texto,
                    emoji = null,
                    estado = if (yaUsada) EstadoOpcion.ATENUADA else EstadoOpcion.NEUTRO,
                    habilitada = !yaUsada && ctrl.acierto != true && !ctrl.revelada,
                    onClick = { ctrl.agregarSilaba(op.texto) },
                    tamanoTexto = if (esGrande) 44 else 34
                )
            }
        }
        PieResultado(ctrl)
    }
}

// ------------------------------------------------------------------- pie

@Composable
private fun PieResultado(ctrl: LessonController) {
    AnimatedVisibility(visible = ctrl.fase == FaseLeccion.RESULTADO) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (ctrl.acierto == true) "¡Muy bien! ⭐" else "Mira la respuesta 👀",
                style = MaterialTheme.typography.titleLarge,
                color = if (ctrl.acierto == true) Verde else MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { ctrl.avanzar() },
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) {
                Text("Seguir ➡", fontSize = 18.sp)
            }
        }
    }
}
