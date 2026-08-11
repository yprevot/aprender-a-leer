import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.Modulo
import com.aprenderaleer.data.TipoEjercicio
import com.aprenderaleer.engine.ExerciseGenerator
import com.aprenderaleer.engine.PronunciationMatcher
import kotlin.random.Random

/**
 * Verificación del contenido y de la lógica pura del juego.
 *
 * Se ejecuta FUERA de Android (no necesita SDK ni emulador) para comprobar
 * que el currículo es coherente y que ningún ejercicio generado es imposible
 * de resolver. Ver README → "Cómo verificar".
 */

private var fallos = 0

private fun check(condicion: Boolean, mensaje: String) {
    if (!condicion) {
        fallos++
        println("  ✗ $mensaje")
    }
}

fun main() {
    println("== 1. Alfabeto ==")
    check(Curriculum.alfabeto.size == 27, "El alfabeto debe tener 27 letras, tiene ${Curriculum.alfabeto.size}")
    check(Curriculum.vocales.size == 5, "Debe haber 5 vocales")
    val idsEsperados = "a b c d e f g h i j k l m n ñ o p q r s t u v w x y z".split(" ")
    check(Curriculum.alfabeto.map { it.id } == idsEsperados, "Orden del alfabeto incorrecto: ${Curriculum.alfabeto.map { it.id }}")
    Curriculum.alfabeto.forEach { l ->
        check(l.nombreAlfabeto.isNotBlank(), "${l.id}: falta el nombre en el alfabeto")
        check(l.palabraEjemplo.isNotBlank(), "${l.id}: falta palabra de ejemplo")
        check(l.emojiEjemplo.isNotBlank(), "${l.id}: falta emoji")
        if (!l.esVocal) check(l.silabas.isNotEmpty(), "${l.id}: consonante sin familia silábica")
        check(l.guionEnsenanza().length > 30, "${l.id}: guion de enseñanza demasiado corto")
    }
    // La palabra de ejemplo debe empezar de verdad por esa letra.
    Curriculum.alfabeto.filter { it.id !in listOf("w", "x") }.forEach { l ->
        check(
            l.palabraEjemplo.lowercase().startsWith(l.minuscula),
            "${l.id}: la palabra ejemplo '${l.palabraEjemplo}' no empieza por '${l.minuscula}'"
        )
    }

    println("== 2. Orden silábico ==")
    Curriculum.ordenSilabico.forEach { id ->
        val l = Curriculum.letra(id)
        check(l != null, "ordenSilabico contiene '$id' pero no existe esa letra")
        check(l == null || l.silabas.isNotEmpty(), "'$id' está en ordenSilabico sin sílabas")
    }
    check(
        Curriculum.ordenSilabico.size == Curriculum.ordenSilabico.distinct().size,
        "Hay ids repetidos en ordenSilabico"
    )
    check(
        Curriculum.ordenSilabico.take(5) == listOf("m", "p", "s", "l", "t"),
        "El orden pedagógico debe empezar por m, p, s, l, t"
    )

    println("== 3. Palabras y sílabas ==")
    (Curriculum.palabras2 + Curriculum.palabras3).forEach { p ->
        check(
            p.silabas.joinToString("") == p.texto,
            "La división silábica de '${p.texto}' no reconstruye la palabra: ${p.silabas}"
        )
        check(p.emoji.isNotBlank(), "'${p.texto}' sin emoji")
    }
    check(Curriculum.palabras2.all { it.numSilabas == 2 }, "Hay palabras en palabras2 que no tienen 2 sílabas")
    check(
        Curriculum.palabras3.all { it.numSilabas >= 3 },
        "Hay palabras en palabras3 con menos de 3 sílabas"
    )
    check(
        Curriculum.palabras2.map { it.texto }.distinct().size == Curriculum.palabras2.size,
        "Palabras de 2 sílabas repetidas"
    )
    check(
        Curriculum.palabras3.map { it.texto }.distinct().size == Curriculum.palabras3.size,
        "Palabras de 3 sílabas repetidas"
    )

    println("== 4. Lecciones ==")
    val lecciones = Curriculum.lecciones
    check(lecciones.map { it.id }.distinct().size == lecciones.size, "Hay ids de lección repetidos")
    Modulo.values().forEach { m ->
        check(Curriculum.leccionesDe(m).isNotEmpty(), "El módulo $m no tiene lecciones")
    }
    println("   ${lecciones.size} lecciones en total")
    Modulo.values().forEach { m ->
        println("   · ${m.titulo}: ${Curriculum.leccionesDe(m).size}")
    }

    println("== 5. Ejercicios generados ==")
    var totalEjercicios = 0
    // Se prueban muchas semillas: los ejercicios se barajan, y un fallo puede
    // aparecer solo en ciertas combinaciones de distractores.
    val semillas = (1..40).toList()
    lecciones.forEach { lec ->
      semillas.forEach { semilla ->
        val ejs = ExerciseGenerator.generar(lec, { 3 }, incluirVoz = true, rnd = Random(semilla))
        check(ejs.isNotEmpty(), "La lección '${lec.id}' no genera ningún ejercicio")
        check(ejs.size <= 12, "La lección '${lec.id}' genera ${ejs.size} ejercicios (máx. 12)")
        totalEjercicios += ejs.size
        ejs.forEach { e ->
            check(e.consigna.isNotBlank(), "${lec.id}/${e.id}: consigna vacía")
            check(e.consignaHablada.isNotBlank(), "${lec.id}/${e.id}: consigna hablada vacía")
            when (e.tipo) {
                TipoEjercicio.MIRA_Y_DI -> {
                    check(!e.objetivoVisible.isNullOrBlank(), "${lec.id}/${e.id}: sin objetivo visible")
                    check(e.respuestasAceptadas.isNotEmpty(), "${lec.id}/${e.id}: sin respuestas aceptadas")
                    // Autoconsistencia: la propia respuesta canónica debe validar.
                    e.respuestasAceptadas.forEach { r ->
                        val ev = PronunciationMatcher.evaluar(listOf(r), e.respuestasAceptadas)
                        check(ev.esCorrecto, "${lec.id}/${e.id}: '$r' NO se acepta a sí misma")
                    }
                }
                TipoEjercicio.ARMA_LA_PALABRA -> {
                    check(e.secuenciaCorrecta.isNotEmpty(), "${lec.id}/${e.id}: sin secuencia correcta")
                    check(
                        e.opciones.map { it.texto }.sorted() == e.secuenciaCorrecta.sorted(),
                        "${lec.id}/${e.id}: las fichas no coinciden con la palabra"
                    )
                }
                else -> {
                    check(e.opciones.size >= 2, "${lec.id}/${e.id}: menos de 2 opciones")
                    check(
                        e.opciones.map { it.id }.distinct().size == e.opciones.size,
                        "${lec.id}/${e.id}: opciones con id repetido"
                    )
                    check(
                        e.idCorrecto != null && e.opciones.any { it.id == e.idCorrecto },
                        "${lec.id}/${e.id}: la respuesta correcta '${e.idCorrecto}' no está entre las opciones"
                    )
                    check(
                        e.opciones.map { it.texto }.distinct().size == e.opciones.size,
                        "${lec.id}/${e.id}: dos opciones muestran el mismo texto"
                    )
                }
            }
            check(e.claveItem.isNotBlank(), "${lec.id}/${e.id}: sin clave de repetición espaciada")
        }
      }
      // Sin voz no debe aparecer ningún ejercicio de hablar.
      val sinVoz = ExerciseGenerator.generar(lec, { 3 }, incluirVoz = false, rnd = Random(7))
      check(
          sinVoz.none { it.tipo == TipoEjercicio.MIRA_Y_DI },
          "${lec.id}: se generan ejercicios de voz con la voz desactivada"
      )
    }
    println("   $totalEjercicios ejercicios generados sin inconsistencias (${semillas.size} semillas)")

    println("== 6. Reconocimiento de pronunciación ==")
    data class Caso(val oido: List<String>, val aceptadas: List<String>, val esperado: PronunciationMatcher.Resultado)
    val C = PronunciationMatcher.Resultado.CORRECTO
    val I = PronunciationMatcher.Resultado.INCORRECTO
    val casos = listOf(
        Caso(listOf("eme"), listOf("eme", "m", "me"), C),
        Caso(listOf("M"), listOf("eme", "m", "me"), C),
        Caso(listOf("la letra eme"), listOf("eme", "m", "me"), C),
        Caso(listOf("ene"), listOf("eme", "m", "me"), I),
        Caso(listOf("ha"), listOf("a", "ha", "ah"), C),
        Caso(listOf("hola"), listOf("a", "ha", "ah"), I),
        Caso(listOf("papá"), listOf("pa", "papa"), C),
        Caso(listOf("ba"), listOf("pa", "papa"), I),
        Caso(listOf("dice pa"), listOf("pa"), C),
        Caso(listOf("keso"), listOf("queso"), C),
        Caso(listOf("kasa"), listOf("casa"), C),
        Caso(listOf("baca"), listOf("vaca"), C),
        Caso(listOf("sapato"), listOf("zapato"), C),
        Caso(listOf("yave"), listOf("llave"), C),
        Caso(listOf("oja"), listOf("hoja"), C),
        Caso(listOf("jirasol"), listOf("girasol"), C),
        Caso(listOf("bentana"), listOf("ventana"), C),
        Caso(listOf("ya"), listOf("lla"), C),
        Caso(listOf("ge"), listOf("gue"), I),
        Caso(listOf("pato"), listOf("gato"), I),
        Caso(listOf("el gato"), listOf("gato"), C),
        Caso(listOf("sombreros"), listOf("sombrero"), C),
        Caso(listOf("mmm"), listOf("mmm", "m"), C),
        Caso(listOf("eme"), listOf("mmm", "m"), I),
        Caso(emptyList(), listOf("pa"), I)
    )
    casos.forEach { c ->
        val r = PronunciationMatcher.evaluar(c.oido, c.aceptadas).resultado
        check(r == c.esperado, "voz ${c.oido} vs ${c.aceptadas}: esperado ${c.esperado}, obtenido $r")
    }
    println("   ${casos.size} casos de voz comprobados")

    println()
    if (fallos == 0) println("✅ TODO CORRECTO — sin fallos") else println("❌ $fallos fallo(s)")
}
