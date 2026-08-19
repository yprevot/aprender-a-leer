package com.aprenderaleer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Progreso del niño. SharedPreferences: local, sin red, sin permisos.
 *
 * Guarda además las "cajas" de Leitner por ítem (letra, sílaba o palabra):
 * cada acierto sube el ítem de caja (se preguntará menos), cada fallo lo baja
 * a la caja 0 (se preguntará mucho). Es repetición espaciada clásica adaptada
 * a sesiones cortas de preescolar.
 */
class ProgressStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("progreso_leer", Context.MODE_PRIVATE)

    // ------------------------------------------------------------- estrellas

    var estrellas: Int
        get() = prefs.getInt(K_ESTRELLAS, 0)
        set(v) = prefs.edit { putInt(K_ESTRELLAS, v) }

    fun sumarEstrellas(n: Int) {
        estrellas += n
    }

    var mejorRacha: Int
        get() = prefs.getInt(K_MEJOR_RACHA, 0)
        set(v) = prefs.edit { putInt(K_MEJOR_RACHA, v) }

    var nombreNino: String
        get() = prefs.getString(K_NOMBRE, "") ?: ""
        set(v) = prefs.edit { putString(K_NOMBRE, v) }

    /** El adulto puede desactivar los ejercicios de hablar (p. ej. en el bus). */
    var vozActivada: Boolean
        get() = prefs.getBoolean(K_VOZ, true)
        set(v) = prefs.edit { putBoolean(K_VOZ, v) }

    // ------------------------------------------------------------- lecciones

    fun leccionesCompletadas(): Set<String> =
        prefs.getStringSet(K_LECCIONES, emptySet())?.toSet() ?: emptySet()

    fun estaCompletada(idLeccion: String): Boolean = idLeccion in leccionesCompletadas()

    fun completarLeccion(idLeccion: String, estrellas: Int) {
        val set = leccionesCompletadas().toMutableSet()
        set += idLeccion
        prefs.edit { putStringSet(K_LECCIONES, set) }
        val previas = estrellasDeLeccion(idLeccion)
        if (estrellas > previas) {
            prefs.edit { putInt("$K_ESTRELLAS_LECCION$idLeccion", estrellas) }
        }
    }

    fun estrellasDeLeccion(idLeccion: String): Int =
        prefs.getInt("$K_ESTRELLAS_LECCION$idLeccion", 0)

    /**
     * Una lección está desbloqueada si es la primera o si la anterior ya se
     * completó. Además, el adulto siempre puede saltar con el botón "libre".
     */
    fun estaDesbloqueada(idLeccion: String, todas: List<Leccion>): Boolean {
        val idx = todas.indexOfFirst { it.id == idLeccion }
        if (idx <= 0) return true
        return estaCompletada(todas[idx - 1].id)
    }

    fun siguienteLeccion(todas: List<Leccion>): Leccion? =
        todas.firstOrNull { !estaCompletada(it.id) } ?: todas.lastOrNull()

    // --------------------------------------------------------------- Leitner

    fun caja(claveItem: String): Int = prefs.getInt("$K_CAJA$claveItem", 0)

    fun registrarRespuesta(claveItem: String, acierto: Boolean) {
        val actual = caja(claveItem)
        val nueva = if (acierto) minOf(actual + 1, CAJA_MAX) else 0
        prefs.edit { putInt("$K_CAJA$claveItem", nueva) }
        val kAciertos = "$K_ACIERTOS$claveItem"
        val kFallos = "$K_FALLOS$claveItem"
        if (acierto) {
            prefs.edit { putInt(kAciertos, prefs.getInt(kAciertos, 0) + 1) }
        } else {
            prefs.edit { putInt(kFallos, prefs.getInt(kFallos, 0) + 1) }
        }
    }

    fun aciertos(claveItem: String): Int = prefs.getInt("$K_ACIERTOS$claveItem", 0)
    fun fallos(claveItem: String): Int = prefs.getInt("$K_FALLOS$claveItem", 0)

    /** Ítems que más se le atragantan, para el repaso inteligente. */
    fun itemsFlojos(candidatos: List<String>, cuantos: Int): List<String> =
        candidatos
            .filter { fallos(it) > 0 || caja(it) < 2 }
            .sortedWith(compareBy({ caja(it) }, { -fallos(it) }))
            .take(cuantos)

    /** Peso de un ítem al sortear ejercicios: caja baja = sale más veces. */
    fun peso(claveItem: String): Int = when (caja(claveItem)) {
        0 -> 5
        1 -> 4
        2 -> 3
        3 -> 2
        else -> 1
    }

    // -------------------------------------------------------------- insignias

    fun insignias(): Set<String> = prefs.getStringSet(K_INSIGNIAS, emptySet())?.toSet() ?: emptySet()

    fun otorgarInsignia(id: String): Boolean {
        val set = insignias().toMutableSet()
        if (id in set) return false
        set += id
        prefs.edit { putStringSet(K_INSIGNIAS, set) }
        return true
    }

    fun reiniciarTodo() {
        prefs.edit { clear() }
    }

    companion object {
        const val CAJA_MAX = 4
        private const val K_ESTRELLAS = "estrellas"
        private const val K_MEJOR_RACHA = "mejor_racha"
        private const val K_NOMBRE = "nombre"
        private const val K_VOZ = "voz_activada"
        private const val K_LECCIONES = "lecciones"
        private const val K_ESTRELLAS_LECCION = "est_lec_"
        private const val K_CAJA = "caja_"
        private const val K_ACIERTOS = "ok_"
        private const val K_FALLOS = "ko_"
        private const val K_INSIGNIAS = "insignias"
    }
}

/** Catálogo de insignias que el avatar entrega. */
data class Insignia(val id: String, val titulo: String, val emoji: String, val descripcion: String)

object Insignias {
    val TODAS = listOf(
        Insignia("primera", "¡Primer paso!", "🌟", "Terminaste tu primera lección"),
        Insignia("vocales", "Rey de las vocales", "🎈", "Dominaste a, e, i, o, u"),
        Insignia("alfabeto", "Dueño del alfabeto", "🔤", "Conoces las 27 letras"),
        Insignia("silabas", "Maestro de sílabas", "🧩", "Sabes combinar consonantes y vocales"),
        Insignia("palabras2", "Lector de palabras", "📖", "Lees palabras de 2 sílabas"),
        Insignia("palabras3", "¡Lector campeón!", "🏆", "Lees palabras largas"),
        Insignia("racha10", "Racha de 10", "🔥", "10 respuestas correctas seguidas"),
        Insignia("voz", "Buena voz", "🎤", "Pronunciaste 20 veces correctamente"),
        Insignia("perfecta", "Lección perfecta", "💯", "Una lección sin ningún fallo")
    )

    fun de(id: String): Insignia? = TODAS.firstOrNull { it.id == id }
}
