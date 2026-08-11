package com.aprenderaleer.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aprenderaleer.ui.theme.Amarillo
import com.aprenderaleer.ui.theme.Morado
import com.aprenderaleer.ui.theme.Naranja
import com.aprenderaleer.ui.theme.Verde
import kotlin.math.cos
import kotlin.math.sin

/** Estados de ánimo del avatar. */
enum class AvatarMood { FELIZ, HABLANDO, CELEBRANDO, ANIMANDO, PENSANDO, ESCUCHANDO }

private val CUERPO = Morado
private val CUERPO_OSCURO = Color(0xFF63498B)
private val PANZA = Color(0xFFF4E9FF)
private val OJO_BLANCO = Color(0xFFFFFFFF)
private val PUPILA = Color(0xFF2B2118)

/**
 * "Lalo", el búho guía. Vectorial y animado con Compose: sin archivos
 * externos, sin licencias y escala perfecto de móvil a tablet.
 */
@Composable
fun AvatarLalo(
    mood: AvatarMood,
    tamano: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val transicion = rememberInfiniteTransition(label = "lalo")

    // Flotación suave permanente.
    val bob by transicion.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "bob"
    )

    // Ciclo de parpadeo (3.4 s).
    val cicloParpadeo by transicion.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "parpadeo"
    )

    // Boca al hablar.
    val cicloBoca by transicion.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Restart),
        label = "boca"
    )

    // Pulso al escuchar / celebrar.
    val pulso by transicion.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "pulso"
    )

    val salto by animateFloatAsState(
        targetValue = if (mood == AvatarMood.CELEBRANDO) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "salto"
    )

    val inclinacion by animateFloatAsState(
        targetValue = when (mood) {
            AvatarMood.ANIMANDO -> -9f
            AvatarMood.PENSANDO -> 7f
            AvatarMood.CELEBRANDO -> 4f
            else -> 0f
        },
        animationSpec = tween(420),
        label = "inclinacion"
    )

    val abrirOjos by animateFloatAsState(
        targetValue = when (mood) {
            AvatarMood.CELEBRANDO -> 1.15f
            AvatarMood.ANIMANDO -> 0.82f
            AvatarMood.PENSANDO -> 0.9f
            else -> 1f
        },
        animationSpec = tween(300),
        label = "ojos"
    )

    Box(modifier = modifier.size(tamano)) {
        Canvas(Modifier.size(tamano)) {
            val u = size.minDimension / 100f          // unidad de dibujo
            val cx = size.width / 2f

            val flotacion = sin(bob) * 2.2f * u
            val brinco = -salto * 10f * u
            // Parpadeo: solo en el último 6 % del ciclo.
            val parpadeando = cicloParpadeo > 0.94f && mood != AvatarMood.CELEBRANDO
            val aperturaOjo = (if (parpadeando) 0.12f else 1f) * abrirOjos
            val aperturaBoca = when (mood) {
                AvatarMood.HABLANDO -> 0.5f + 0.5f * ((sin(cicloBoca) + 1f) / 2f)
                AvatarMood.CELEBRANDO -> 1f
                else -> 0.15f
            }

            translate(top = flotacion + brinco) {
                rotate(degrees = inclinacion, pivot = Offset(cx, size.height * 0.82f)) {
                    dibujarBuho(u, cx, aperturaOjo, aperturaBoca, mood, pulso)
                }
            }

            if (mood == AvatarMood.CELEBRANDO) dibujarConfeti(u, pulso)
            if (mood == AvatarMood.ESCUCHANDO) dibujarOndas(u, cx, pulso)
        }
    }
}

private fun DrawScope.dibujarBuho(
    u: Float,
    cx: Float,
    aperturaOjo: Float,
    aperturaBoca: Float,
    mood: AvatarMood,
    pulso: Float
) {
    val cy = size.height * 0.55f

    // --- Orejitas (penachos)
    val oreja = Path().apply {
        moveTo(cx - 26 * u, cy - 22 * u)
        lineTo(cx - 30 * u, cy - 44 * u)
        lineTo(cx - 10 * u, cy - 30 * u)
        close()
    }
    drawPath(oreja, CUERPO_OSCURO)
    val oreja2 = Path().apply {
        moveTo(cx + 26 * u, cy - 22 * u)
        lineTo(cx + 30 * u, cy - 44 * u)
        lineTo(cx + 10 * u, cy - 30 * u)
        close()
    }
    drawPath(oreja2, CUERPO_OSCURO)

    // --- Patas
    listOf(-11f, 11f).forEach { dx ->
        drawRoundRectCompat(
            color = Naranja,
            left = cx + (dx - 5) * u,
            top = cy + 30 * u,
            right = cx + (dx + 5) * u,
            bottom = cy + 38 * u,
            radio = 3 * u
        )
    }

    // --- Cuerpo
    drawOval(
        color = CUERPO,
        topLeft = Offset(cx - 32 * u, cy - 32 * u),
        size = Size(64 * u, 66 * u)
    )

    // --- Alas
    drawOval(
        color = CUERPO_OSCURO,
        topLeft = Offset(cx - 38 * u, cy - 8 * u),
        size = Size(18 * u, 34 * u)
    )
    drawOval(
        color = CUERPO_OSCURO,
        topLeft = Offset(cx + 20 * u, cy - 8 * u),
        size = Size(18 * u, 34 * u)
    )

    // --- Panza
    drawOval(
        color = PANZA,
        topLeft = Offset(cx - 21 * u, cy - 4 * u),
        size = Size(42 * u, 36 * u)
    )

    // --- Ojos
    val ojoY = cy - 12 * u
    val rOjo = 15 * u
    listOf(-15f, 15f).forEach { dx ->
        val ox = cx + dx * u
        drawOval(
            color = OJO_BLANCO,
            topLeft = Offset(ox - rOjo, ojoY - rOjo * aperturaOjo),
            size = Size(rOjo * 2, rOjo * 2 * aperturaOjo)
        )
        drawOval(
            color = CUERPO_OSCURO,
            topLeft = Offset(ox - rOjo, ojoY - rOjo * aperturaOjo),
            size = Size(rOjo * 2, rOjo * 2 * aperturaOjo),
            style = Stroke(width = 1.6f * u)
        )
        if (aperturaOjo > 0.35f) {
            // Pupila: mira ligeramente hacia el centro al pensar.
            val desvio = if (mood == AvatarMood.PENSANDO) -3f * u else 0f
            drawCircle(PUPILA, radius = 6.4f * u, center = Offset(ox + desvio, ojoY))
            drawCircle(
                Color.White,
                radius = 2.2f * u,
                center = Offset(ox + desvio + 2.2f * u, ojoY - 2.4f * u)
            )
        }
    }

    // --- Cejas (cambian el gesto)
    val cejaOffset = when (mood) {
        AvatarMood.ANIMANDO -> 3f
        AvatarMood.PENSANDO -> 1f
        AvatarMood.CELEBRANDO -> -4f
        else -> -1f
    } * u
    listOf(-1f, 1f).forEach { lado ->
        val ox = cx + lado * 15 * u
        drawArcCompat(
            color = CUERPO_OSCURO,
            left = ox - 12 * u,
            top = ojoY - 26 * u + cejaOffset,
            ancho = 24 * u,
            alto = 20 * u,
            inicio = 200f,
            barrido = 140f,
            grosor = 2.6f * u
        )
    }

    // --- Pico (se abre al hablar)
    val picoY = cy + 4 * u
    val abre = aperturaBoca * 7f * u
    val picoSup = Path().apply {
        moveTo(cx - 7 * u, picoY)
        lineTo(cx + 7 * u, picoY)
        lineTo(cx, picoY + 6 * u)
        close()
    }
    drawPath(picoSup, Amarillo)
    if (abre > 1.2f * u) {
        val picoInf = Path().apply {
            moveTo(cx - 5.5f * u, picoY + 6 * u)
            lineTo(cx + 5.5f * u, picoY + 6 * u)
            lineTo(cx, picoY + 6 * u + abre)
            close()
        }
        drawPath(picoInf, Color(0xFFE0A100))
    }

    // --- Mejillas al celebrar
    if (mood == AvatarMood.CELEBRANDO || mood == AvatarMood.FELIZ) {
        listOf(-24f, 24f).forEach { dx ->
            drawCircle(
                Color(0x33FF7043),
                radius = 6.5f * u,
                center = Offset(cx + dx * u, cy + 2 * u)
            )
        }
    }

    // --- Auricular/foco al escuchar
    if (mood == AvatarMood.ESCUCHANDO) {
        drawCircle(
            color = Verde.copy(alpha = 0.35f + 0.25f * sin(pulso * 6.28f)),
            radius = 40f * u,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f * u)
        )
    }
}

private fun DrawScope.dibujarConfeti(u: Float, progreso: Float) {
    val colores = listOf(Amarillo, Naranja, Verde, Morado, Color(0xFF3F8EFC))
    repeat(14) { i ->
        val ang = (i / 14f) * 2f * Math.PI.toFloat()
        val radio = (18f + progreso * 34f) * u
        val x = size.width / 2f + cos(ang) * radio
        val y = size.height * 0.5f + sin(ang) * radio * 0.8f
        val alpha = (1f - progreso).coerceIn(0f, 1f)
        drawCircle(
            color = colores[i % colores.size].copy(alpha = alpha),
            radius = 2.6f * u,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.dibujarOndas(u: Float, cx: Float, progreso: Float) {
    repeat(3) { i ->
        val p = ((progreso + i / 3f) % 1f)
        drawCircle(
            color = Verde.copy(alpha = (1f - p) * 0.4f),
            radius = (30f + p * 26f) * u,
            center = Offset(cx, size.height * 0.55f),
            style = Stroke(width = 2f * u)
        )
    }
}

// --- Ayudantes de dibujo -----------------------------------------------------

private fun DrawScope.drawRoundRectCompat(
    color: Color,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radio: Float
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radio, radio)
    )
}

private fun DrawScope.drawArcCompat(
    color: Color,
    left: Float,
    top: Float,
    ancho: Float,
    alto: Float,
    inicio: Float,
    barrido: Float,
    grosor: Float
) {
    val r = Rect(left, top, left + ancho, top + alto)
    drawArc(
        color = color,
        startAngle = inicio,
        sweepAngle = barrido,
        useCenter = false,
        topLeft = Offset(r.left, r.top),
        size = Size(r.width, r.height),
        style = Stroke(width = grosor)
    )
}
