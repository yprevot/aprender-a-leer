package com.aprenderaleer

import com.aprenderaleer.engine.PronunciationMatcher
import com.aprenderaleer.engine.PronunciationMatcher.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El reconocedor de Android devuelve texto, no fonemas. Estos tests fijan
 * qué transcripciones deben darse por buenas y cuáles no, para que el juego
 * ni castigue al niño por un error del transcriptor ni le regale aciertos.
 */
class PronunciationMatcherTest {

    private fun r(oido: List<String>, aceptadas: List<String>) =
        PronunciationMatcher.evaluar(oido, aceptadas).resultado

    @Test
    fun `acepta el nombre de la letra en sus variantes`() {
        val eme = listOf("eme", "m", "me")
        assertEquals(Resultado.CORRECTO, r(listOf("eme"), eme))
        assertEquals(Resultado.CORRECTO, r(listOf("M"), eme))
        assertEquals(Resultado.CORRECTO, r(listOf("la letra eme"), eme))
    }

    @Test
    fun `no confunde letras distintas`() {
        assertEquals(Resultado.INCORRECTO, r(listOf("ene"), listOf("eme", "m", "me")))
        assertEquals(Resultado.INCORRECTO, r(listOf("ba"), listOf("pa", "papa")))
        assertEquals(Resultado.INCORRECTO, r(listOf("pato"), listOf("gato")))
        assertEquals(Resultado.INCORRECTO, r(listOf("ge"), listOf("gue")))
    }

    @Test
    fun `absorbe las equivalencias ortograficas del espanol`() {
        assertEquals(Resultado.CORRECTO, r(listOf("keso"), listOf("queso")))   // qu = k
        assertEquals(Resultado.CORRECTO, r(listOf("kasa"), listOf("casa")))    // c  = k
        assertEquals(Resultado.CORRECTO, r(listOf("baca"), listOf("vaca")))    // b  = v
        assertEquals(Resultado.CORRECTO, r(listOf("sapato"), listOf("zapato"))) // seseo
        assertEquals(Resultado.CORRECTO, r(listOf("yave"), listOf("llave")))   // yeísmo
        assertEquals(Resultado.CORRECTO, r(listOf("oja"), listOf("hoja")))     // h muda
        assertEquals(Resultado.CORRECTO, r(listOf("jirasol"), listOf("girasol")))
    }

    @Test
    fun `ignora muletillas y palabras sueltas alrededor`() {
        assertEquals(Resultado.CORRECTO, r(listOf("dice pa"), listOf("pa")))
        assertEquals(Resultado.CORRECTO, r(listOf("el gato"), listOf("gato")))
    }

    @Test
    fun `acepta la repeticion infantil de una silaba`() {
        assertEquals(Resultado.CORRECTO, r(listOf("papa"), listOf("pa")))
    }

    @Test
    fun `distingue el sonido del nombre de la letra`() {
        assertEquals(Resultado.CORRECTO, r(listOf("mmm"), listOf("mmm", "m")))
        assertEquals(Resultado.INCORRECTO, r(listOf("eme"), listOf("mmm", "m")))
    }

    @Test
    fun `sin audio no hay acierto`() {
        assertEquals(Resultado.INCORRECTO, r(emptyList(), listOf("pa")))
        assertEquals(Resultado.INCORRECTO, r(listOf(""), listOf("pa")))
    }

    @Test
    fun `la clave fonetica colapsa repeticiones y tildes`() {
        assertEquals("m", PronunciationMatcher.clave("mmm"))
        assertEquals("a", PronunciationMatcher.clave("aaa"))
        assertEquals(PronunciationMatcher.clave("papa"), PronunciationMatcher.clave("papá"))
    }

    @Test
    fun `perro y pero no son la misma palabra`() {
        assertTrue(r(listOf("pero"), listOf("perro")) != Resultado.CORRECTO)
    }
}
