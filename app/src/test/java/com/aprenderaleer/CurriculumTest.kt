package com.aprenderaleer

import com.aprenderaleer.data.Curriculum
import com.aprenderaleer.data.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `el alfabeto espanol tiene 27 letras en orden`() {
        val esperado = "a b c d e f g h i j k l m n ñ o p q r s t u v w x y z".split(" ")
        assertEquals(esperado, Curriculum.alfabeto.map { it.id })
    }

    @Test
    fun `hay cinco vocales y todas tienen sonido sostenido`() {
        assertEquals(5, Curriculum.vocales.size)
        assertTrue(Curriculum.vocales.all { it.sonidoSostenido != null })
    }

    @Test
    fun `toda consonante tiene familia silabica`() {
        Curriculum.alfabeto.filter { !it.esVocal }.forEach {
            assertTrue("${it.id} sin sílabas", it.silabas.isNotEmpty())
        }
    }

    @Test
    fun `la palabra de ejemplo empieza por su letra`() {
        Curriculum.alfabeto
            .filter { it.id !in listOf("w", "x") } // préstamos: wifi, xilófono
            .forEach {
                assertTrue(
                    "${it.id}: '${it.palabraEjemplo}'",
                    it.palabraEjemplo.lowercase().startsWith(it.minuscula)
                )
            }
    }

    @Test
    fun `las silabas reconstruyen exactamente la palabra`() {
        (Curriculum.palabras2 + Curriculum.palabras3).forEach {
            assertEquals(it.texto, it.silabas.joinToString(""))
        }
    }

    @Test
    fun `palabras2 tiene dos silabas y palabras3 al menos tres`() {
        assertTrue(Curriculum.palabras2.all { it.numSilabas == 2 })
        assertTrue(Curriculum.palabras3.all { it.numSilabas >= 3 })
    }

    @Test
    fun `el orden pedagogico empieza por las consonantes mas faciles`() {
        assertEquals(listOf("m", "p", "s", "l", "t"), Curriculum.ordenSilabico.take(5))
        Curriculum.ordenSilabico.forEach {
            assertTrue("'$it' no existe", Curriculum.letra(it) != null)
        }
    }

    @Test
    fun `todos los modulos tienen lecciones y los ids son unicos`() {
        val ids = Curriculum.lecciones.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        Modulo.values().forEach {
            assertTrue("$it vacío", Curriculum.leccionesDe(it).isNotEmpty())
        }
    }

    @Test
    fun `el guion distingue el nombre del sonido`() {
        val eme = Curriculum.letra("m")!!
        val guion = eme.guionEnsenanza()
        assertTrue(guion.contains("se llama eme"))
        assertTrue(guion.contains("mmm"))
        assertTrue(guion.contains("ma, me, mi, mo, mu"))
    }
}
