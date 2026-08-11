package com.aprenderaleer.engine

/**
 * Evalúa si lo que dijo el niño coincide con lo que se le pidió.
 *
 * El reconocedor de voz de Android devuelve TEXTO, no fonemas, así que
 * comparar cadenas tal cual es demasiado estricto: el niño dice "be" y el
 * motor transcribe "b", "ve", "bé"... Por eso normalizamos a una CLAVE
 * FONÉTICA del español antes de comparar:
 *
 *   · se quitan tildes y la hache muda
 *   · seseo:      z, ce, ci  ->  s
 *   · yeísmo:     ll, y      ->  Y
 *   · b = v = w   (no se distinguen en español)
 *   · c, k, qu    ->  k
 *   · ge, gi = je, ji
 *   · x -> ks
 *   · se colapsan letras repetidas ("mmm" -> "m", "aaa" -> "a")
 *
 * Con eso, "mmm", "eme" y "M" caen en claves comparables y el juego deja de
 * castigar al niño por un fallo del transcriptor.
 */
object PronunciationMatcher {

    enum class Resultado { CORRECTO, CASI, INCORRECTO }

    data class Evaluacion(
        val resultado: Resultado,
        /** La alternativa del reconocedor que mejor encajó (para mostrarla). */
        val mejorCoincidencia: String?,
        val distancia: Int
    ) {
        val esCorrecto: Boolean get() = resultado == Resultado.CORRECTO
    }

    private val MULETILLAS = listOf(
        "la letra", "el sonido", "la silaba", "la sílaba", "la palabra",
        "letra", "silaba", "sílaba", "palabra", "se llama", "dice", "es la", "es el", "es "
    )

    /** Quita tildes, signos y muletillas. */
    fun normalizar(texto: String): String {
        var t = texto.lowercase().trim()
        MULETILLAS.forEach { t = t.replace(it, " ") }
        t = t
            .replace('á', 'a').replace('é', 'e').replace('í', 'i')
            .replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')
            .replace('à', 'a').replace('è', 'e').replace('ì', 'i')
            .replace('ò', 'o').replace('ù', 'u')
        t = t.filter { it.isLetter() || it.isWhitespace() }
        return t.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")
    }

    /** Convierte una cadena en su clave fonética del español. */
    fun clave(texto: String): String {
        var t = normalizar(texto).replace(" ", "")
        if (t.isEmpty()) return ""

        // 1) Dígrafos primero, con centinelas en MAYÚSCULA para que las
        //    sustituciones posteriores (en minúscula) no los toquen.
        t = t.replace("ch", "C")
        t = t.replace("ll", "Y")
        t = t.replace("rr", "R")
        // "qu" pasa a 'k' minúscula, igual que hará luego la 'c', para que
        // "queso" y "keso" produzcan exactamente la misma clave.
        t = t.replace("qu", "k")
        // "gue/gui" usan centinela en mayúscula para que la regla ge/gi -> je/ji
        // no los toque; al final toda 'g' acaba en 'G', así que es consistente.
        t = t.replace("gue", "Ge").replace("gui", "Gi")

        // 2) La hache no suena (ya extrajimos "ch").
        t = t.replace("h", "")

        // 3) Seseo y equivalencias ortográficas.
        t = t.replace("ce", "se").replace("ci", "si")
        t = t.replace("ge", "je").replace("gi", "ji")
        t = t.replace("z", "s")
        t = t.replace("c", "k")
        t = t.replace("v", "b")
        t = t.replace("w", "b")
        t = t.replace("x", "ks")
        t = t.replace("y", "Y")
        t = t.replace("g", "G")

        // 4) Colapsa repeticiones: "mmm" -> "m", "aaa" -> "a".
        val sb = StringBuilder()
        for (c in t) if (sb.isEmpty() || sb.last() != c) sb.append(c)
        return sb.toString()
    }

    /** Distancia de edición clásica. */
    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val coste = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + coste)
            }
            val tmp = prev; prev = cur; cur = tmp
        }
        return prev[b.length]
    }

    /**
     * Cuánta diferencia se perdona, según lo largo que sea el objetivo.
     *
     * En objetivos cortos (letras y sílabas) la tolerancia es CERO a propósito:
     * ahí una sola letra de diferencia no es ruido del transcriptor, es otra
     * respuesta ("pa" vs "ba", "eme" vs "ene", "gato" vs "pato"). Solo en
     * palabras largas se perdona algún carácter, donde sí suele ser ruido
     * (plurales, artículos pegados, terminaciones).
     */
    private fun tolerancia(longitud: Int): Int = when {
        longitud <= 4 -> 0
        longitud <= 7 -> 1
        else -> 2
    }

    /**
     * Por debajo de esta longitud no existe el "casi": o es, o no es.
     * En pares mínimos cortos ("pato"/"gato", "mesa"/"pesa") decirle "casi"
     * al niño sería engañarlo: son palabras distintas, no una aproximación.
     */
    private const val LONGITUD_MINIMA_CASI = 6

    /**
     * @param escuchado alternativas devueltas por el reconocedor (de mejor a peor)
     * @param aceptadas formas válidas (objetivo + sinónimos del currículo)
     */
    fun evaluar(escuchado: List<String>, aceptadas: List<String>): Evaluacion {
        if (escuchado.isEmpty() || aceptadas.isEmpty()) {
            return Evaluacion(Resultado.INCORRECTO, null, Int.MAX_VALUE)
        }

        val objetivos = aceptadas.map { clave(it) }.filter { it.isNotEmpty() }.distinct()
        if (objetivos.isEmpty()) return Evaluacion(Resultado.INCORRECTO, null, Int.MAX_VALUE)
        var mejorDist = Int.MAX_VALUE
        var mejorTexto: String? = null
        // Longitud del objetivo que produjo la mejor distancia: la tolerancia
        // se calcula sobre ESE, no sobre el más corto de la lista.
        var mejorObjLen = objetivos.minOf { it.length }

        for (alt in escuchado) {
            // Probamos la frase completa y también cada palabra suelta:
            // el motor suele añadir ruido ("dice pa", "pa pa").
            val candidatos = buildList {
                add(alt)
                addAll(normalizar(alt).split(" ").filter { it.isNotBlank() })
            }
            for (cand in candidatos) {
                val k = clave(cand)
                if (k.isEmpty()) continue
                for (obj in objetivos) {
                    // Repetición infantil: "papa" cuando se pidió "pa".
                    if (obj.length >= 2 && k == obj + obj) {
                        return Evaluacion(Resultado.CORRECTO, alt, 0)
                    }
                    val d = levenshtein(k, obj)
                    if (d < mejorDist) {
                        mejorDist = d
                        mejorTexto = alt
                        mejorObjLen = obj.length
                    }
                }
            }
        }

        val tol = tolerancia(mejorObjLen)
        val resultado = when {
            mejorDist <= tol -> Resultado.CORRECTO
            mejorObjLen >= LONGITUD_MINIMA_CASI && mejorDist <= tol + 1 -> Resultado.CASI
            else -> Resultado.INCORRECTO
        }
        return Evaluacion(resultado, mejorTexto, mejorDist)
    }
}
