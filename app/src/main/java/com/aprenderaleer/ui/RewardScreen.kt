package com.aprenderaleer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprenderaleer.data.Insignia
import com.aprenderaleer.ui.theme.Amarillo
import com.aprenderaleer.ui.theme.Azul
import com.aprenderaleer.ui.theme.Morado
import com.aprenderaleer.ui.theme.Naranja
import com.aprenderaleer.ui.theme.Verde
import kotlin.math.sin
import kotlin.random.Random

/** Pantalla de recompensa al terminar una lección. */
@Composable
fun PantallaRecompensa(
    tituloLeccion: String,
    estrellas: Int,
    perfecta: Boolean,
    insignias: List<Insignia>,
    haySiguiente: Boolean,
    onSiguiente: () -> Unit,
    onMapa: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val esGrande = maxWidth >= 700.dp
        LluviaConfeti()

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.widthIn(max = if (esGrande) 480.dp else Dp.Unspecified),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            AvatarLalo(AvatarMood.CELEBRANDO, tamano = if (esGrande) 220.dp else 180.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                if (perfecta) "¡Perfecto!" else "¡Lo lograste!",
                style = MaterialTheme.typography.displaySmall,
                color = Verde
            )
            Text(
                tituloLeccion,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Amarillo.copy(alpha = 0.2f),
                border = BorderStroke(3.dp, Amarillo)
            ) {
                Row(
                    Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 40.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("+$estrellas", style = MaterialTheme.typography.displaySmall)
                }
            }

            if (insignias.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text("¡Nueva medalla!", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                insignias.forEach { ins ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Morado.copy(alpha = 0.12f),
                        border = BorderStroke(2.dp, Morado),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ins.emoji, fontSize = 34.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(ins.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    ins.descripcion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            if (haySiguiente) {
                Button(
                    onClick = onSiguiente,
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Siguiente lección ➡", fontSize = 20.sp, modifier = Modifier.padding(6.dp))
                }
                Spacer(Modifier.height(10.dp))
            }
            OutlinedButton(onClick = onMapa, modifier = Modifier.fillMaxWidth(0.8f)) {
                Text("🗺️ Volver al mapa", fontSize = 18.sp, modifier = Modifier.padding(4.dp))
            }
            }
        }
    }
}

@Composable
private fun LluviaConfeti() {
    val t = rememberInfiniteTransition(label = "confeti")
    val p by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "caida"
    )
    val rnd = remember0()
    val colores = listOf(Amarillo, Naranja, Verde, Azul, Morado)

    Canvas(Modifier.fillMaxSize()) {
        repeat(40) { i ->
            val x = rnd[i * 2] * size.width
            val fase = (p + rnd[i * 2 + 1]) % 1f
            val y = fase * size.height
            val vaiven = sin((fase * 6.28f) + i) * 14f
            drawCircle(
                color = colores[i % colores.size].copy(alpha = 0.75f),
                radius = 6f + (i % 3) * 2f,
                center = Offset(x + vaiven, y)
            )
        }
    }
}

/** Semilla estable para que el confeti no salte en cada recomposición. */
@Composable
private fun remember0(): FloatArray {
    return androidx.compose.runtime.remember {
        val r = Random(7)
        FloatArray(100) { r.nextFloat() }
    }
}
