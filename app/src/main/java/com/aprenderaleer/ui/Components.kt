@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aprenderaleer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aprenderaleer.ui.theme.Amarillo
import com.aprenderaleer.ui.theme.Azul
import com.aprenderaleer.ui.theme.Imprenta
import com.aprenderaleer.ui.theme.Manuscrita
import com.aprenderaleer.ui.theme.Rojo
import com.aprenderaleer.ui.theme.RojoSuave
import com.aprenderaleer.ui.theme.Verde
import com.aprenderaleer.ui.theme.VerdeSuave

/** Estado visual de una tarjeta de opción. */
enum class EstadoOpcion { NEUTRO, CORRECTA, INCORRECTA, ATENUADA }

/**
 * Tarjeta grande y tocable. El área mínima es deliberadamente enorme
 * (>= 96 dp): a los 5 años la motricidad fina todavía falla y un objetivo
 * pequeño produce errores que NO son de lectura.
 */
@Composable
fun TarjetaOpcion(
    texto: String,
    emoji: String?,
    estado: EstadoOpcion,
    habilitada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tamanoTexto: Int = 56
) {
    val escala by animateFloatAsState(
        targetValue = when (estado) {
            EstadoOpcion.CORRECTA -> 1.06f
            EstadoOpcion.ATENUADA -> 0.94f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaOpcion"
    )

    val fondo = when (estado) {
        EstadoOpcion.CORRECTA -> VerdeSuave
        EstadoOpcion.INCORRECTA -> RojoSuave
        else -> MaterialTheme.colorScheme.surface
    }
    val borde = when (estado) {
        EstadoOpcion.CORRECTA -> Verde
        EstadoOpcion.INCORRECTA -> Rojo
        EstadoOpcion.ATENUADA -> Color(0x22000000)
        EstadoOpcion.NEUTRO -> Azul.copy(alpha = 0.35f)
    }
    val alfa = if (estado == EstadoOpcion.ATENUADA) 0.45f else 1f

    Card(
        onClick = { if (habilitada) onClick() },
        modifier = modifier.scale(escala),
        enabled = habilitada,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = fondo.copy(alpha = alfa)),
        border = BorderStroke(if (estado == EstadoOpcion.NEUTRO) 3.dp else 5.dp, borde),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 44.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = texto,
                fontSize = tamanoTexto.sp,
                lineHeight = (tamanoTexto * 1.15f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

/** Botón circular grande: "escúchalo otra vez". */
@Composable
fun BotonEscuchar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tamano: Dp = 72.dp,
    etiqueta: String = "Escuchar"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Azul,
            modifier = Modifier.size(tamano)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = etiqueta,
                    tint = Color.White,
                    modifier = Modifier.size(tamano * 0.5f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
    }
}

/** Botón de micrófono. Pulsa mientras escucha. */
@Composable
fun BotonMicrofono(
    escuchando: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tamano: Dp = 104.dp
) {
    val escala by animateFloatAsState(
        targetValue = if (escuchando) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "mic"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Surface(
            onClick = { if (habilitado) onClick() },
            enabled = habilitado,
            shape = CircleShape,
            color = if (escuchando) Verde else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(tamano)
                .scale(escala)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Hablar",
                    tint = Color.White,
                    modifier = Modifier.size(tamano * 0.48f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (escuchando) "Te escucho..." else "Toca y habla",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** Barra de estrellas ganadas en la lección. */
@Composable
fun FilaEstrellas(ganadas: Int, total: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(total.coerceAtMost(5)) { i ->
            Text(
                text = if (i < ganadas) "⭐" else "☆",
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
        if (total > 5) {
            Spacer(Modifier.width(6.dp))
            Text("$ganadas", style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Barra de avance de la lección. */
@Composable
fun BarraAvance(actual: Int, total: Int, modifier: Modifier = Modifier) {
    val objetivo = if (total == 0) 0f else actual.toFloat() / total
    val animado by animateFloatAsState(objetivo, label = "avance")
    Column(modifier) {
        LinearProgressIndicator(
            progress = { animado },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            color = Amarillo,
            trackColor = Color(0x22000000),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/** Bocadillo de diálogo del avatar. */
@Composable
fun Bocadillo(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface
) {
    AnimatedVisibility(
        visible = texto.isNotBlank(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = color,
            shadowElevation = 3.dp,
            border = BorderStroke(2.dp, Color(0x22000000))
        ) {
            Text(
                text = texto,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start
            )
        }
    }
}

/** Cartel grande para mostrar la letra/sílaba/palabra objetivo. */
@Composable
fun CartelObjetivo(
    texto: String,
    emoji: String?,
    modifier: Modifier = Modifier,
    tamano: Int = 96
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(4.dp, Amarillo),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (emoji != null) Text(emoji, fontSize = 56.sp)
            Text(
                texto,
                fontSize = tamano.sp,
                lineHeight = (tamano * 1.1f).sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}

/**
 * Cartel de enseñanza de UNA letra en UNA caja (minúscula o mayúscula).
 *
 * Muestra el mismo glifo en las dos formas con las que el niño se lo va a
 * encontrar —de imprenta y manuscrito— porque son visualmente muy distintas
 * (la "a" de imprenta y la "a" ligada apenas se parecen) y hay que enseñar
 * explícitamente que son la misma letra. Cada forma se toca para oírla.
 */
@Composable
fun CartelLetra(
    glifo: String,
    etiquetaCaja: String,
    emoji: String?,
    onEscuchar: () -> Unit,
    modifier: Modifier = Modifier,
    tamano: Int = 84
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(4.dp, Amarillo),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 44.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(etiquetaCaja, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormaLetra("De imprenta", glifo, Imprenta, tamano, Modifier.weight(1f), onEscuchar)
                FormaLetra("A mano", glifo, Manuscrita, tamano, Modifier.weight(1f), onEscuchar)
            }
        }
    }
}

@Composable
private fun FormaLetra(
    etiqueta: String,
    glifo: String,
    familia: FontFamily,
    tamano: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Azul.copy(alpha = 0.07f),
        border = BorderStroke(2.dp, Azul.copy(alpha = 0.35f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = glifo,
                fontFamily = familia,
                fontSize = tamano.sp,
                // La cursiva tiene trazos que suben y bajan mucho: sin este
                // aire de más, la ligadura se recorta por arriba o por abajo.
                lineHeight = (tamano * 1.35f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text("🔊", fontSize = 16.sp)
        }
    }
}

/** Cinta superior con estrellas totales y racha. */
@Composable
fun CintaEstado(estrellas: Int, racha: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(Color(0x14000000), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⭐ $estrellas", style = MaterialTheme.typography.titleMedium)
        if (racha >= 2) {
            Spacer(Modifier.width(14.dp))
            Text("🔥 $racha", style = MaterialTheme.typography.titleMedium)
        }
    }
}
