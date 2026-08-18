package com.aprenderaleer

import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.TipoEjercicio
import com.aprenderaleer.engine.ExerciseGenerator
import com.aprenderaleer.engine.PronunciationMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Ningún ejercicio generado puede ser imposible de resolver: la respuesta
 * correcta tiene que estar entre las opciones, las opciones no pueden
 * repetirse y lo que se pide decir tiene que validarse a sí mismo.
 */
class ExerciseGeneratorTest {

    private fun todos(voz: Boolean) = Curriculum.lecciones.flatMap { lec ->
        ExerciseGenerator.generar(lec, { 3 }, incluirVoz = voz, rnd = Random(1234))
            .map { lec.id to it }
    }

    @Test
    fun `toda leccion genera ejercicios`() {
        Curriculum.lecciones.forEach { lec ->
            val ejs = ExerciseGenerator.generar(lec, { 3 }, incluirVoz = true, rnd = Random(1))
            assertTrue("${lec.id} sin ejercicios", ejs.isNotEmpty())
            assertTrue("${lec.id} demasiado larga", ejs.size <= 12)
        }
    }

    @Test
    fun `la respuesta correcta siempre esta entre las opciones`() {
        todos(true).forEach { (lecId, e) ->
            if (e.tipo != TipoEjercicio.MIRA_Y_DI && e.tipo != TipoEjercicio.ARMA_LA_PALABRA) {
                assertNotNull("$lecId/${e.id} sin respuesta", e.idCorrecto)
                assertTrue(
                    "$lecId/${e.id}: '${e.idCorrecto}' no está entre las opciones",
                    e.opciones.any { it.id == e.idCorrecto }
                )
                assertTrue("$lecId/${e.id}: menos de 2 opciones", e.opciones.size >= 2)
            }
        }
    }

    @Test
    fun `no hay dos opciones iguales`() {
        todos(true).forEach { (lecId, e) ->
            if (e.tipo != TipoEjercicio.ARMA_LA_PALABRA) {
                val textos = e.opciones.map { it.texto }
                assertTrue("$lecId/${e.id}: opciones repetidas $textos", textos.distinct().size == textos.size)
            }
            val ids = e.opciones.map { it.id }
            assertTrue("$lecId/${e.id}: ids repetidos", ids.distinct().size == ids.size)
        }
    }

    @Test
    fun `las fichas de armar palabra son exactamente las silabas`() {
        todos(true).filter { it.second.tipo == TipoEjercicio.ARMA_LA_PALABRA }
            .forEach { (lecId, e) ->
                assertTrue(
                    "$lecId/${e.id}: fichas != sílabas",
                    e.opciones.map { it.texto }.sorted() == e.secuenciaCorrecta.sorted()
                )
            }
    }

    @Test
    fun `lo que se pide pronunciar se acepta a si mismo`() {
        todos(true).filter { it.second.tipo == TipoEjercicio.MIRA_Y_DI }
            .forEach { (lecId, e) ->
                assertTrue("$lecId/${e.id} sin objetivo", !e.objetivoVisible.isNullOrBlank())
                assertTrue("$lecId/${e.id} sin respuestas", e.respuestasAceptadas.isNotEmpty())
                e.respuestasAceptadas.forEach { resp ->
                    assertTrue(
                        "$lecId/${e.id}: '$resp' no se acepta a sí misma",
                        PronunciationMatcher.evaluar(listOf(resp), e.respuestasAceptadas).esCorrecto
                    )
                }
            }
    }

    @Test
    fun `sin voz no se generan ejercicios de hablar`() {
        assertTrue(todos(false).none { it.second.tipo == TipoEjercicio.MIRA_Y_DI })
    }

    @Test
    fun `identificar una letra nunca muestra el par de cajas juntas`() {
        todos(true).forEach { (lecId, e) ->
            if (e.tipo == TipoEjercicio.NOMBRE_A_LETRA || e.tipo == TipoEjercicio.SONIDO_A_LETRA) {
                e.opciones.forEach { op ->
                    // "Aa" no es ni todo minúsculas ni todo mayúsculas: si la
                    // opción trae las dos cajas, el niño acierta reconociendo
                    // solo una de ellas.
                    assertTrue(
                        "$lecId/${e.id}: la opción '${op.texto}' no es una letra sola",
                        op.texto == op.texto.lowercase() || op.texto == op.texto.uppercase()
                    )
                }
            }
        }
    }

    @Test
    fun `una vocal se identifica en sus cuatro presentaciones`() {
        val lec = Curriculum.leccion("voc_a")!!
        val ejs = ExerciseGenerator.generar(lec, { 3 }, incluirVoz = true, rnd = Random(7))
        val identificar = ejs.filter {
            it.tipo == TipoEjercicio.NOMBRE_A_LETRA || it.tipo == TipoEjercicio.SONIDO_A_LETRA
        }
        val presentaciones = identificar.map { e ->
            val correcta = e.opciones.first { it.id == e.idCorrecto }
            correcta.texto to correcta.manuscrita
        }
        assertEquals(
            setOf("a" to false, "A" to false, "a" to true, "A" to true),
            presentaciones.toSet()
        )
    }

    @Test
    fun `todo ejercicio tiene consigna hablada y clave de repaso`() {
        todos(true).forEach { (lecId, e) ->
            assertTrue("$lecId/${e.id} sin consigna", e.consigna.isNotBlank())
            assertTrue("$lecId/${e.id} sin consigna hablada", e.consignaHablada.isNotBlank())
            assertTrue("$lecId/${e.id} sin claveItem", e.claveItem.isNotBlank())
        }
    }
}
