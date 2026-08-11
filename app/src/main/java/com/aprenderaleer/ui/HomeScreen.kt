@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aprenderaleer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.Insignias
import com.aprenderaleer.data.Leccion
import com.aprenderaleer.data.Modulo
import com.aprenderaleer.data.ProgressStore
import com.aprenderaleer.engine.SpeechEngine
import com.aprenderaleer.ui.theme.Amarillo
import com.aprenderaleer.ui.theme.Azul
import com.aprenderaleer.ui.theme.Verde
import kotlin.random.Random

@Composable
fun PantallaInicio(
    progreso: ProgressStore,
    voz: SpeechEngine,
    refresco: Int,
    onAbrirLeccion: (Leccion) -> Unit
) {
    var version by remember { mutableIntStateOf(0) }
    var mostrarAjustes by remember { mutableStateOf(false) }
    var puertaAdulto by remember { mutableStateOf(false) }

    val lecciones = Curriculum.lecciones
    val siguiente = remember(version, refresco) { progreso.siguienteLeccion(lecciones) }
    val completadas = remember(version, refresco) { progreso.leccionesCompletadas() }
    val insignias = remember(version, refresco) { progreso.insignias() }

    // Solo saluda la primera vez que se abre la app, no cada vez que se vuelve.
    LaunchedEffect(voz.listo) {
        if (voz.listo && refresco == 0) voz.decir(Coach.bienvenida(progreso.nombreNino))
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val esGrande = maxWidth >= 700.dp

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = if (esGrande) 28.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarLalo(
                        if (voz.hablando) AvatarMood.HABLANDO else AvatarMood.FELIZ,
                        tamano = if (esGrande) 150.dp else 110.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Aprender a leer",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "con Lalo el búho",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CintaEstado(progreso.estrellas, progreso.mejorRacha)
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { puertaAdulto = true }) { Text("⚙️") }
                        }
                    }
                }
            }

            // Botón grande de continuar: el 90 % de las sesiones empiezan aquí.
            item {
                siguiente?.let { lec ->
                    Card(
                        onClick = { onAbrirLeccion(lec) },
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Verde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lec.emoji, fontSize = 44.sp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "▶  Seguir jugando",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    lec.titulo,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            if (insignias.isNotEmpty()) {
                item {
                    Column {
                        Text("Tus medallas", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Insignias.TODAS.filter { it.id in insignias }) { ins ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = Amarillo.copy(alpha = 0.22f),
                                    border = BorderStroke(2.dp, Amarillo)
                                ) {
                                    Column(
                                        Modifier
                                            .padding(10.dp)
                                            .width(100.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(ins.emoji, fontSize = 30.sp)
                                        Text(
                                            ins.titulo,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Modulo.values().forEach { modulo ->
                val delModulo = Curriculum.leccionesDe(modulo)
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(modulo.emoji, fontSize = 26.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(modulo.titulo, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "${delModulo.count { it.id in completadas }} de ${delModulo.size} · ${modulo.descripcion}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(delModulo, key = { it.id }) { lec ->
                                val hecha = lec.id in completadas
                                val abierta = progreso.estaDesbloqueada(lec.id, lecciones)
                                TarjetaLeccion(
                                    leccion = lec,
                                    completada = hecha,
                                    desbloqueada = abierta,
                                    estrellas = progreso.estrellasDeLeccion(lec.id),
                                    ancho = if (esGrande) 210.dp else 168.dp,
                                    onClick = { onAbrirLeccion(lec) }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }

    if (puertaAdulto) {
        PuertaAdultos(
            onCancelar = { puertaAdulto = false },
            onOk = { puertaAdulto = false; mostrarAjustes = true }
        )
    }

    if (mostrarAjustes) {
        DialogoAjustes(
            progreso = progreso,
            onCerrar = { mostrarAjustes = false; version++ }
        )
    }
}

@Composable
private fun TarjetaLeccion(
    leccion: Leccion,
    completada: Boolean,
    desbloqueada: Boolean,
    estrellas: Int,
    ancho: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val color = when {
        completada -> Verde.copy(alpha = 0.16f)
        desbloqueada -> MaterialTheme.colorScheme.surface
        else -> Color(0x0D000000)
    }
    Card(
        onClick = { if (desbloqueada) onClick() },
        enabled = desbloqueada,
        modifier = Modifier.width(ancho),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(
            2.dp,
            if (completada) Verde else if (desbloqueada) Azul.copy(alpha = 0.4f) else Color(0x1A000000)
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (desbloqueada) leccion.emoji else "🔒", fontSize = 34.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                leccion.titulo,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                leccion.subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (completada) {
                Spacer(Modifier.height(4.dp))
                FilaEstrellas(minOf(estrellas / 6, 3), 3)
            }
        }
    }
}

/** Pequeña barrera para que el niño no entre solo en los ajustes. */
@Composable
private fun PuertaAdultos(onCancelar: () -> Unit, onOk: () -> Unit) {
    val a = remember { Random.nextInt(4, 9) }
    val b = remember { Random.nextInt(4, 9) }
    var elegido by remember { mutableStateOf<Int?>(null) }
    val correcto = a * b
    val opciones = remember { (listOf(correcto, correcto + 7, correcto - 5, correcto + 12)).shuffled() }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Solo para adultos") },
        text = {
            Column {
                Text("¿Cuánto es $a × $b?")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    opciones.forEach { n ->
                        Button(
                            onClick = { elegido = n; if (n == correcto) onOk() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (elegido == n && n != correcto)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        ) { Text("$n") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoAjustes(progreso: ProgressStore, onCerrar: () -> Unit) {
    var voz by remember { mutableStateOf(progreso.vozActivada) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Ajustes") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Ejercicios de hablar", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Usa el micrófono del sistema. Desactívalo en lugares ruidosos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = voz,
                        onCheckedChange = { voz = it; progreso.vozActivada = it }
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("Estrellas: ${progreso.estrellas}")
                Text("Mejor racha: ${progreso.mejorRacha}")
                Text("Lecciones hechas: ${progreso.leccionesCompletadas().size}")
                Spacer(Modifier.height(14.dp))
                if (!confirmarBorrado) {
                    TextButton(onClick = { confirmarBorrado = true }) {
                        Text("Borrar todo el progreso")
                    }
                } else {
                    Text("¿Seguro? Esto no se puede deshacer.")
                    Row {
                        TextButton(onClick = { progreso.reiniciarTodo(); onCerrar() }) {
                            Text("Sí, borrar")
                        }
                        TextButton(onClick = { confirmarBorrado = false }) { Text("No") }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0x11000000), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        "Esta app no pide permisos, no usa internet y no recoge datos. " +
                            "El progreso se guarda solo en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Listo") } }
    )
}
