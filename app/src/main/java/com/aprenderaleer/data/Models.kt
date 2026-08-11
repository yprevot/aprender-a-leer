package com.aprenderaleer.data

/**
 * Modelos del currículo.
 *
 * Base pedagógica (ver README):
 *  - Método fonético-silábico: vocal -> letra (nombre + sonido) -> familia silábica CV
 *    -> palabras de 2 sílabas -> palabras de 3 sílabas.
 *  - Se distingue SIEMPRE el NOMBRE de la letra en el alfabeto ("eme") del
 *    SONIDO que tiene dentro de una palabra ("mmm"), porque la investigación
 *    muestra que son conocimientos distintos y que el nombre por sí solo no
 *    predice bien la lectura si no se ancla en conciencia fonológica.
 */

/** Una letra del alfabeto español (27 letras) o un dígrafo (ch, ll, rr, qu, gu). */
data class Letra(
    val id: String,
    val mayuscula: String,
    val minuscula: String,
    /** Cómo se llama en el alfabeto. Ej: "eme" */
    val nombreAlfabeto: String,
    /**
     * Texto que el TTS lee para imitar el SONIDO dentro de la palabra.
     * Solo para consonantes continuas (se pueden alargar) y vocales.
     * null en oclusivas: el sonido se enseña con la familia silábica.
     */
    val sonidoSostenido: String?,
    val esVocal: Boolean,
    val palabraEjemplo: String,
    val emojiEjemplo: String,
    /** Familia silábica CV. Vacía en vocales y en la h muda. */
    val silabas: List<String> = emptyList(),
    /** Variantes aceptables cuando el niño pronuncia el NOMBRE de la letra. */
    val variantesNombre: List<String> = emptyList(),
    /** Nota didáctica opcional que el avatar cuenta ("la h no suena", etc.). */
    val notaDidactica: String? = null
) {
    val glifo: String get() = "$mayuscula$minuscula"

    /** Guion que el avatar narra al presentar la letra. */
    fun guionEnsenanza(): String {
        val sb = StringBuilder()
        sb.append("Esta es la letra $mayuscula, $minuscula. ")
        sb.append("En el alfabeto se llama $nombreAlfabeto. ")
        if (esVocal) {
            sb.append("Y dentro de una palabra también suena $nombreAlfabeto. ")
        } else if (id == "h") {
            sb.append("Pero dentro de una palabra no suena. Es muda. ")
        } else {
            val sonido = sonidoSostenido ?: silabas.joinToString(", ")
            sb.append("Pero dentro de una palabra se llama $minuscula, y suena $sonido. ")
            if (silabas.isNotEmpty()) {
                sb.append("Se combina con las vocales así: ${silabas.joinToString(", ")}. ")
            }
        }
        sb.append("Por ejemplo: $palabraEjemplo.")
        return sb.toString()
    }
}

/** Una palabra objetivo, ya separada en sílabas. */
data class Palabra(
    val texto: String,
    val silabas: List<String>,
    val emoji: String
) {
    val numSilabas: Int get() = silabas.size
}

/** Tipos de ejercicio disponibles. */
enum class TipoEjercicio {
    /** El avatar dice algo; el niño toca la opción correcta. */
    ESCUCHA_Y_TOCA,

    /** Se muestra algo en pantalla; el niño lo pronuncia y el juego lo verifica. */
    MIRA_Y_DI,

    /** El avatar dice el NOMBRE de la letra ("eme"); el niño toca la grafía. */
    NOMBRE_A_LETRA,

    /** El avatar dice el SONIDO ("mmm"); el niño toca la grafía. */
    SONIDO_A_LETRA,

    /** El niño arma la palabra tocando las sílabas en orden. */
    ARMA_LA_PALABRA
}

/** Una opción tocable dentro de un ejercicio. */
data class Opcion(
    val id: String,
    /** Lo que se ve en grande. Ej: "Mm", "pa", "gato" */
    val texto: String,
    /** Emoji opcional bajo el texto. */
    val emoji: String? = null,
    /** Lo que dice el TTS si el niño toca la opción para escucharla. */
    val audio: String = texto
)

/** Un ejercicio concreto ya listo para presentarse. */
data class Ejercicio(
    val id: String,
    val tipo: TipoEjercicio,
    /** Instrucción en pantalla. */
    val consigna: String,
    /** Lo que narra el avatar al presentar el ejercicio. */
    val consignaHablada: String,
    val opciones: List<Opcion> = emptyList(),
    val idCorrecto: String? = null,
    /** Para MIRA_Y_DI: lo que se muestra en grande. */
    val objetivoVisible: String? = null,
    /** Para MIRA_Y_DI: formas aceptadas al reconocer la voz. */
    val respuestasAceptadas: List<String> = emptyList(),
    /** Para ARMA_LA_PALABRA: orden correcto de sílabas. */
    val secuenciaCorrecta: List<String> = emptyList(),
    val emoji: String? = null,
    /** Clave del ítem para la repetición espaciada (Leitner). */
    val claveItem: String = id,
    /** Pista que da el avatar tras un fallo. */
    val pista: String? = null
)

/** Una lección: unidad mínima que el niño completa de una sentada. */
data class Leccion(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val emoji: String,
    val modulo: Modulo,
    /** Letras que se presentan antes de los ejercicios (fase de enseñanza). */
    val letrasEnsenadas: List<Letra> = emptyList(),
    val palabras: List<Palabra> = emptyList()
)

/** Los grandes bloques del juego, en orden. */
enum class Modulo(val titulo: String, val descripcion: String, val emoji: String) {
    VOCALES("Sesión 1 · Las vocales", "a, e, i, o, u", "🎈"),
    ALFABETO("Sesión 2 · El alfabeto", "Las 27 letras y su nombre", "🔤"),
    SILABAS("Sesión 3 · Sílabas", "Cada consonante con las vocales", "🧩"),
    PALABRAS_2("Fase 2 · Palabras de 2 sílabas", "ga-to, lu-na, ca-sa", "🐱"),
    PALABRAS_3("Fase 2 · Palabras de 3 sílabas", "pe-lo-ta, za-pa-to", "⚽")
}
