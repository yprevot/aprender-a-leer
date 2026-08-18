package com.aprenderaleer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta cálida y de alto contraste. Se evita el modo oscuro a propósito:
// para lectura inicial conviene fondo claro y letra muy contrastada.
val Naranja = Color(0xFFFF7043)
val NaranjaSuave = Color(0xFFFFCCBC)
val Azul = Color(0xFF3F8EFC)
val AzulSuave = Color(0xFFD6E7FF)
val Verde = Color(0xFF34A853)
val VerdeSuave = Color(0xFFD8F3DC)
val Rojo = Color(0xFFE05A4F)
val RojoSuave = Color(0xFFFFDAD6)
val Amarillo = Color(0xFFFFC300)
val Crema = Color(0xFFFFF8ED)
val Tinta = Color(0xFF2B2118)
val Morado = Color(0xFF7B5EA7)

/**
 * Letra ligada (manuscrita), la que el niño escribe en el cuaderno.
 *
 * Usa la familia "cursive" del sistema —DancingScript en Android— en vez de
 * empaquetar un .ttf: no engorda el APK, cubre el alfabeto español entero
 * (ñ incluida) y, si un dispositivo no la trae, cae en la fuente normal, con
 * lo que el niño sigue viendo la letra correcta aunque no ligada.
 */
val Manuscrita: FontFamily = FontFamily.Cursive

/** De imprenta: la de los libros y las pantallas. */
val Imprenta: FontFamily = FontFamily.Default

private val Esquema = lightColorScheme(
    primary = Naranja,
    onPrimary = Color.White,
    primaryContainer = NaranjaSuave,
    onPrimaryContainer = Tinta,
    secondary = Azul,
    onSecondary = Color.White,
    secondaryContainer = AzulSuave,
    onSecondaryContainer = Tinta,
    tertiary = Verde,
    onTertiary = Color.White,
    tertiaryContainer = VerdeSuave,
    onTertiaryContainer = Tinta,
    background = Crema,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
    error = Rojo,
    onError = Color.White,
    errorContainer = RojoSuave,
    onErrorContainer = Tinta
)

private val Tipografia = Typography(
    displayLarge = TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Black),
    displayMedium = TextStyle(fontSize = 68.sp, fontWeight = FontWeight.Black),
    displaySmall = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
)

/** Siempre tema claro: para lectura inicial conviene máximo contraste. */
@Composable
fun AprenderALeerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = Tipografia,
        content = content
    )
}
