package com.aprenderaleer.engine

import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.Ejercicio
import com.aprenderaleer.data.Leccion
import com.aprenderaleer.data.Letra
import com.aprenderaleer.data.Modulo
import com.aprenderaleer.data.Opcion
import com.aprenderaleer.data.Palabra
import com.aprenderaleer.data.TipoEjercicio
import kotlin.random.Random

/**
 * Convierte una lección en una tanda de ejercicios.
 *
 * Principios aplicados:
 *  · Alternancia receptivo / productivo: escuchar-y-tocar (reconocer) se
 *    intercala con mirar-y-decir (producir). Reconocer siempre va primero,
 *    porque es la tarea más fácil (andamiaje).
 *  · Distractores con sentido: se prefieren letras que el niño confunde de
 *    verdad (b/d, p/q, m/n, a/e...), no opciones aleatorias que regalan el
 *    acierto.
 *  · Repetición espaciada: los ítems con caja Leitner baja aparecen más veces.
 *  · Tandas cortas (10-12 ejercicios) para no agotar la atención de un niño
 *    de 5 años.
 */
object ExerciseGenerator {

    private const val MAX_EJERCICIOS = 12

    /** Pares que los niños confunden realmente, por forma o por sonido. */
    private val CONFUSIONES: Map<String, List<String>> = mapOf(
        "a" to listOf("e", "o"),
        "e" to listOf("a", "i"),
        "i" to listOf("e", "u", "l"),
        "o" to listOf("a", "u"),
        "u" to listOf("o", "i", "v"),
        "b" to listOf("d", "p", "v"),
        "d" to listOf("b", "p", "t"),
        "p" to listOf("q", "b", "d"),
        "q" to listOf("p", "g", "c"),
        "m" to listOf("n", "ñ", "w"),
        "n" to listOf("m", "ñ", "u"),
        "ñ" to listOf("n", "m"),
        "s" to listOf("z", "c", "x"),
        "z" to listOf("s", "c"),
        "c" to listOf("s", "k", "q", "o"),
        "k" to listOf("c", "q"),
        "f" to listOf("t", "j"),
        "t" to listOf("f", "d"),
        "l" to listOf("ll", "i", "t"),
        "ll" to listOf("l", "y"),
        "y" to listOf("ll", "i", "j"),
        "g" to listOf("j", "q", "c"),
        "j" to listOf("g", "y", "i"),
        "r" to listOf("rr", "n"),
        "rr" to listOf("r"),
        "v" to listOf("b", "u", "w"),
        "w" to listOf("v", "m"),
        "x" to listOf("k", "s"),
        "h" to listOf("n", "b"),
        "ch" to listOf("c", "h")
    )

    /**
     * @param peso función que devuelve la prioridad de un ítem (Leitner).
     *   Se recibe como función y no como ProgressStore para que el generador
     *   sea comprobable con tests JVM puros, sin Android.
     */
    fun generar(
        leccion: Leccion,
        peso: (String) -> Int,
        incluirVoz: Boolean,
        rnd: Random = Random(System.currentTimeMillis())
    ): List<Ejercicio> {
        val base = when (leccion.modulo) {
            Modulo.VOCALES -> generarVocales(leccion, rnd, incluirVoz)
            Modulo.ALFABETO -> generarAlfabeto(leccion, rnd, incluirVoz)
            Modulo.SILABAS -> generarSilabas(leccion, rnd, incluirVoz)
            Modulo.PALABRAS_2, Modulo.PALABRAS_3 -> generarPalabras(leccion, rnd, incluirVoz)
        }
        return priorizar(base, peso, rnd).take(MAX_EJERCICIOS)
    }

    /**
     * Ordena la tanda: primero un par de ejercicios fáciles (para arrancar con
     * un acierto), luego los ítems flojos según Leitner, y el resto barajado.
     */
    private fun priorizar(
        ejercicios: List<Ejercicio>,
        peso: (String) -> Int,
        rnd: Random
    ): List<Ejercicio> {
        if (ejercicios.size <= 3) return ejercicios
        val receptivos = ejercicios.filter { it.tipo != TipoEjercicio.MIRA_Y_DI }
        val productivos = ejercicios.filter { it.tipo == TipoEjercicio.MIRA_Y_DI }

        // OJO: la clave de orden se calcula UNA vez por ejercicio. Si se
        // sortease con una lambda que llama a rnd, el comparador dejaría de
        // ser consistente y TimSort lanza "Comparison method violates its
        // general contract!" en tiempo de ejecución.
        val ordenadosReceptivos = receptivos
            .map { it to (peso(it.claveItem) * 10 + rnd.nextInt(10)) }
            .sortedByDescending { it.second }
            .map { it.first }
        val ordenadosProductivos = productivos.shuffled(rnd)

        // Intercalado: 2 receptivos por cada productivo.
        val salida = mutableListOf<Ejercicio>()
        var i = 0
        var j = 0
        while (i < ordenadosReceptivos.size || j < ordenadosProductivos.size) {
            repeat(2) {
                if (i < ordenadosReceptivos.size) salida += ordenadosReceptivos[i++]
            }
            if (j < ordenadosProductivos.size) salida += ordenadosProductivos[j++]
        }
        return salida
    }

    // ---------------------------------------------------------------- VOCALES

    private fun generarVocales(l: Leccion, rnd: Random, voz: Boolean): List<Ejercicio> {
        val out = mutableListOf<Ejercicio>()
        val pool = Curriculum.vocales

        l.letrasEnsenadas.forEach { v ->
            // Escuchar el sonido y tocar la vocal.
            out += Ejercicio(
                id = "voc_son_${v.id}",
                tipo = TipoEjercicio.SONIDO_A_LETRA,
                consigna = "¿Cuál vocal suena así?",
                consignaHablada = "Escucha bien... ${v.sonidoSostenido}. Toca la vocal que suena ${v.sonidoSostenido}.",
                opciones = opcionesLetra(v, pool, 4, rnd),
                idCorrecto = v.id,
                claveItem = "letra_${v.id}",
                pista = "Vuelve a escuchar. La boca hace ${v.sonidoSostenido} ... es la ${v.mayuscula}."
            )
            // Escuchar el nombre y tocar la vocal (en vocales, nombre = sonido).
            out += Ejercicio(
                id = "voc_nom_${v.id}",
                tipo = TipoEjercicio.NOMBRE_A_LETRA,
                consigna = "Toca la vocal ${v.mayuscula}${v.minuscula}",
                consignaHablada = "Toca la vocal ${v.nombreAlfabeto}.",
                opciones = opcionesLetra(v, pool, 4, rnd),
                idCorrecto = v.id,
                claveItem = "letra_${v.id}",
                pista = "Busca la que se ve así: ${v.mayuscula} ${v.minuscula}."
            )
            // Palabra que empieza con esa vocal.
            out += Ejercicio(
                id = "voc_pal_${v.id}",
                tipo = TipoEjercicio.ESCUCHA_Y_TOCA,
                consigna = "¿Cuál empieza con ${v.mayuscula}${v.minuscula}?",
                consignaHablada = "¿Cuál de estos empieza con el sonido ${v.sonidoSostenido}?",
                opciones = opcionesPalabraEjemplo(v, pool, 3, rnd),
                idCorrecto = v.id,
                claveItem = "ini_${v.id}",
                pista = "Di las palabras despacio y escucha cómo empiezan."
            )
            if (voz) {
                out += Ejercicio(
                    id = "voc_di_${v.id}",
                    tipo = TipoEjercicio.MIRA_Y_DI,
                    consigna = "Di esta vocal en voz alta",
                    consignaHablada = "Ahora te toca a ti. Mira la letra y dila fuerte.",
                    objetivoVisible = "${v.mayuscula}${v.minuscula}",
                    respuestasAceptadas = listOf(v.nombreAlfabeto, v.minuscula) + v.variantesNombre,
                    claveItem = "voz_${v.id}",
                    pista = "Abre bien la boca y dilo fuerte: ${v.nombreAlfabeto}."
                )
            }
        }
        return out
    }

    // --------------------------------------------------------------- ALFABETO

    private fun generarAlfabeto(l: Leccion, rnd: Random, voz: Boolean): List<Ejercicio> {
        val out = mutableListOf<Ejercicio>()
        val pool = Curriculum.alfabeto

        l.letrasEnsenadas.forEach { letra ->
            // "Se llama eme" -> tocar la grafía. Este es el ejercicio clave que
            // separa NOMBRE de SONIDO.
            out += Ejercicio(
                id = "alf_nom_${letra.id}",
                tipo = TipoEjercicio.NOMBRE_A_LETRA,
                consigna = "¿Cuál letra se llama «${letra.nombreAlfabeto}»?",
                consignaHablada = "En el alfabeto hay una letra que se llama ${letra.nombreAlfabeto}. Tócala.",
                opciones = opcionesLetra(letra, pool, 4, rnd),
                idCorrecto = letra.id,
                claveItem = "nombre_${letra.id}",
                pista = "Se llama ${letra.nombreAlfabeto} y se escribe ${letra.mayuscula} ${letra.minuscula}."
            )

            // "Suena mmm" -> tocar la grafía.
            val consignaSonido = if (letra.id == "h") {
                "Hay una letra que no suena, es muda. ¿Cuál es?"
            } else if (letra.sonidoSostenido != null) {
                "Dentro de una palabra, esta letra suena ${letra.sonidoSostenido}. Tócala."
            } else {
                "Es la letra con la que empieza ${letra.palabraEjemplo}. Tócala."
            }
            out += Ejercicio(
                id = "alf_son_${letra.id}",
                tipo = TipoEjercicio.SONIDO_A_LETRA,
                consigna = if (letra.id == "h") "¿Cuál letra es muda?"
                else "¿Cuál suena así, como en «${letra.palabraEjemplo}»?",
                consignaHablada = consignaSonido,
                opciones = opcionesLetra(letra, pool, 4, rnd),
                idCorrecto = letra.id,
                claveItem = "sonido_${letra.id}",
                pista = "Piensa en ${letra.palabraEjemplo}. ${letra.emojiEjemplo} Empieza con esa letra."
            )

            if (voz) {
                out += Ejercicio(
                    id = "alf_di_${letra.id}",
                    tipo = TipoEjercicio.MIRA_Y_DI,
                    consigna = "¿Cómo se llama esta letra en el alfabeto?",
                    consignaHablada = "Mira la letra y di cómo se llama en el alfabeto.",
                    objetivoVisible = "${letra.mayuscula}${letra.minuscula}",
                    respuestasAceptadas = listOf(letra.nombreAlfabeto) + letra.variantesNombre,
                    claveItem = "voz_nombre_${letra.id}",
                    emoji = letra.emojiEjemplo,
                    pista = "Recuerda: esta letra se llama ${letra.nombreAlfabeto}."
                )
            }
        }
        return out
    }

    // ---------------------------------------------------------------- SÍLABAS

    private fun generarSilabas(l: Leccion, rnd: Random, voz: Boolean): List<Ejercicio> {
        val out = mutableListOf<Ejercicio>()
        val letra = l.letrasEnsenadas.firstOrNull() ?: return out
        val familia = letra.silabas
        if (familia.isEmpty()) return out

        // Familias ya vistas antes, para usarlas como distractores reales.
        val indice = Curriculum.ordenSilabico.indexOf(letra.id)
        val vistas = Curriculum.ordenSilabico
            .take(maxOf(indice, 0))
            .mapNotNull { Curriculum.letra(it) }
            .flatMap { it.silabas }

        familia.forEach { silaba ->
            // Dentro de la familia: solo cambia la vocal. Discriminación fina.
            out += Ejercicio(
                id = "sil_fam_${silaba}",
                tipo = TipoEjercicio.ESCUCHA_Y_TOCA,
                consigna = "Toca la sílaba que escuchaste",
                consignaHablada = "Escucha... $silaba. Toca $silaba.",
                opciones = familia.shuffled(rnd).map { Opcion(it, it, audio = it) },
                idCorrecto = silaba,
                claveItem = "sil_$silaba",
                pista = "Empieza con ${letra.minuscula} y termina con ${silaba.last()}."
            )

            // Mezclada con otras familias: discriminación de consonante.
            if (vistas.isNotEmpty()) {
                val otras = vistas.shuffled(rnd).filter { it != silaba }.take(3)
                if (otras.size >= 2) {
                    out += Ejercicio(
                        id = "sil_mix_${silaba}",
                        tipo = TipoEjercicio.ESCUCHA_Y_TOCA,
                        consigna = "Toca la sílaba que escuchaste",
                        consignaHablada = "Escucha con atención... $silaba.",
                        opciones = (otras + silaba).shuffled(rnd).map { Opcion(it, it, audio = it) },
                        idCorrecto = silaba,
                        claveItem = "silmix_$silaba",
                        pista = "Fíjate en cómo EMPIEZA la sílaba, no solo en cómo termina."
                    )
                }
            }
        }

        // Producir 2 sílabas de la familia con la voz.
        if (voz) {
            familia.shuffled(rnd).take(3).forEach { silaba ->
                out += Ejercicio(
                    id = "sil_di_$silaba",
                    tipo = TipoEjercicio.MIRA_Y_DI,
                    consigna = "Lee esta sílaba en voz alta",
                    consignaHablada = "Ahora léela tú. Junta las dos letras en un solo sonido.",
                    objetivoVisible = silaba,
                    respuestasAceptadas = listOf(silaba, silaba + silaba),
                    claveItem = "voz_sil_$silaba",
                    pista = "Une los sonidos sin parar en medio: $silaba."
                )
            }
        }

        // Sílaba inicial de una palabra real.
        val palabraEj = (Curriculum.palabras2 + Curriculum.palabras3)
            .firstOrNull { it.silabas.first() == familia.firstOrNull() }
        if (palabraEj != null) {
            val distract = Curriculum.todasLasSilabas
                .filter { it != palabraEj.silabas.first() }
                .shuffled(rnd).take(3)
            out += Ejercicio(
                id = "sil_ini_${palabraEj.texto}",
                tipo = TipoEjercicio.ESCUCHA_Y_TOCA,
                consigna = "¿Con qué sílaba empieza «${palabraEj.texto}»?",
                consignaHablada = "${palabraEj.texto}. ¿Con qué sílaba empieza ${palabraEj.texto}?",
                opciones = (distract + palabraEj.silabas.first()).shuffled(rnd)
                    .map { Opcion(it, it, audio = it) },
                idCorrecto = palabraEj.silabas.first(),
                emoji = palabraEj.emoji,
                claveItem = "ini_${palabraEj.texto}",
                pista = "Di la palabra despacio, sílaba a sílaba: ${palabraEj.silabas.joinToString(" - ")}."
            )
        }
        return out
    }

    // --------------------------------------------------------------- PALABRAS

    private fun generarPalabras(l: Leccion, rnd: Random, voz: Boolean): List<Ejercicio> {
        val out = mutableListOf<Ejercicio>()
        val pool = if (l.modulo == Modulo.PALABRAS_2) Curriculum.palabras2 else Curriculum.palabras3

        l.palabras.forEach { p ->
            // Armar la palabra con sus sílabas: hace visible la estructura.
            out += Ejercicio(
                id = "pal_arma_${p.texto}",
                tipo = TipoEjercicio.ARMA_LA_PALABRA,
                consigna = "Arma la palabra tocando las sílabas en orden",
                consignaHablada = "Vamos a armar ${p.texto}. ${p.silabas.joinToString(", ")}. ${p.texto}.",
                opciones = revolverSinDejarloIgual(p.silabas, rnd)
                    .mapIndexed { i, s -> Opcion("${s}_$i", s, audio = s) },
                secuenciaCorrecta = p.silabas,
                emoji = p.emoji,
                claveItem = "arma_${p.texto}",
                pista = "Escucha otra vez y busca la sílaba con la que EMPIEZA: ${p.silabas.first()}."
            )

            // Reconocer la palabra escrita entre distractores parecidos.
            out += Ejercicio(
                id = "pal_lee_${p.texto}",
                tipo = TipoEjercicio.ESCUCHA_Y_TOCA,
                consigna = "Toca la palabra que escuchaste",
                consignaHablada = "Busca la palabra ${p.texto}.",
                opciones = opcionesPalabra(p, pool, 3, rnd),
                idCorrecto = p.texto,
                claveItem = "pal_${p.texto}",
                pista = "Mira la primera sílaba de cada palabra: ${p.texto} empieza con ${p.silabas.first()}."
            )

            if (voz) {
                out += Ejercicio(
                    id = "pal_di_${p.texto}",
                    tipo = TipoEjercicio.MIRA_Y_DI,
                    consigna = "Lee esta palabra en voz alta",
                    consignaHablada = "Léela tú. Primero por sílabas y después de corrido.",
                    objetivoVisible = p.texto,
                    respuestasAceptadas = listOf(p.texto, p.silabas.joinToString(" ")),
                    emoji = p.emoji,
                    claveItem = "voz_pal_${p.texto}",
                    pista = "Ve despacio: ${p.silabas.joinToString(" - ")}. Ahora todo junto."
                )
            }
        }
        return out
    }

    // ------------------------------------------------------------- utilidades

    private fun opcionesLetra(
        correcta: Letra,
        pool: List<Letra>,
        cuantas: Int,
        rnd: Random
    ): List<Opcion> {
        val confusas = (CONFUSIONES[correcta.id] ?: emptyList())
            .mapNotNull { id -> pool.firstOrNull { it.id == id } }
        val resto = pool.filter { it.id != correcta.id && it !in confusas }.shuffled(rnd)
        val distractores = (confusas.shuffled(rnd) + resto).take(cuantas - 1)
        return (distractores + correcta).shuffled(rnd).map {
            Opcion(
                id = it.id,
                texto = "${it.mayuscula}${it.minuscula}",
                audio = it.nombreAlfabeto
            )
        }
    }

    private fun opcionesPalabraEjemplo(
        correcta: Letra,
        pool: List<Letra>,
        cuantas: Int,
        rnd: Random
    ): List<Opcion> {
        val otras = pool.filter { it.id != correcta.id }.shuffled(rnd).take(cuantas - 1)
        return (otras + correcta).shuffled(rnd).map {
            Opcion(
                id = it.id,
                texto = it.palabraEjemplo,
                emoji = it.emojiEjemplo,
                audio = it.palabraEjemplo
            )
        }
    }

    private fun opcionesPalabra(
        correcta: Palabra,
        pool: List<Palabra>,
        cuantas: Int,
        rnd: Random
    ): List<Opcion> {
        // Distractores que comparten longitud o alguna sílaba: obligan a leer
        // de verdad en vez de adivinar por la forma general.
        val parecidas = pool.filter { otra ->
            otra.texto != correcta.texto &&
                (otra.numSilabas == correcta.numSilabas &&
                    (otra.texto.first() == correcta.texto.first() ||
                        otra.silabas.any { it in correcta.silabas }))
        }.shuffled(rnd)
        val resto = pool.filter { it.texto != correcta.texto && it !in parecidas }.shuffled(rnd)
        val distractores = (parecidas + resto).take(cuantas - 1)
        return (distractores + correcta).shuffled(rnd).map {
            Opcion(id = it.texto, texto = it.texto, emoji = it.emoji, audio = it.texto)
        }
    }

    /** Baraja evitando devolver el orden correcto (frustra el ejercicio). */
    private fun revolverSinDejarloIgual(items: List<String>, rnd: Random): List<String> {
        if (items.size < 2) return items
        var intento = items.shuffled(rnd)
        var guardas = 0
        while (intento == items && guardas < 10) {
            intento = items.shuffled(rnd)
            guardas++
        }
        return intento
    }
}
