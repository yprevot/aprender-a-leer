package com.aprenderaleer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aprenderaleer.data.Ejercicio
import com.aprenderaleer.data.Insignia
import com.aprenderaleer.data.Insignias
import com.aprenderaleer.data.Leccion
import com.aprenderaleer.data.Modulo
import com.aprenderaleer.data.PasoEnsenanza
import com.aprenderaleer.data.ProgressStore
import com.aprenderaleer.data.TipoEjercicio
import com.aprenderaleer.engine.PronunciationMatcher
import com.aprenderaleer.engine.SpeechEngine

/** Fases por las que pasa una lección. */
enum class FaseLeccion { ENSENANZA, EJERCICIO, RESULTADO, FINAL }

/**
 * Cerebro de una lección: lleva el índice, la puntuación, la racha, los
 * reintentos y decide qué dice el avatar en cada momento.
 *
 * Política de reintentos (clave para no frustrar a un niño de 5 años):
 *  1er fallo  -> ánimo + puede reintentar.
 *  2º  fallo  -> el avatar da la PISTA y se atenúan los distractores obvios.
 *  3er fallo  -> se muestra la respuesta, se explica y se pasa. Nunca se
 *                deja al niño atascado en el mismo ítem.
 */
class LessonController(
    val leccion: Leccion,
    val ejercicios: List<Ejercicio>,
    private val progreso: ProgressStore,
    private val voz: SpeechEngine
) {
    /** Cada letra en dos pasos: primero la minúscula, después la mayúscula. */
    val pasosEnsenanza: List<PasoEnsenanza> = leccion.pasosEnsenanza

    var fase by mutableStateOf(
        if (pasosEnsenanza.isNotEmpty()) FaseLeccion.ENSENANZA else FaseLeccion.EJERCICIO
    )
        private set

    var indiceEnsenanza by mutableStateOf(0)
        private set

    var indice by mutableStateOf(0)
        private set

    var estrellas by mutableStateOf(0)
        private set

    var racha by mutableStateOf(0)
        private set

    var mejorRachaSesion by mutableStateOf(0)
        private set

    var fallosTotales by mutableStateOf(0)
        private set

    var intentos by mutableStateOf(0)
        private set

    var seleccion by mutableStateOf<String?>(null)
        private set

    /** null = sin responder todavía. */
    var acierto by mutableStateOf<Boolean?>(null)
        private set

    var revelada by mutableStateOf(false)
        private set

    var mostrarPista by mutableStateOf(false)
        private set

    var mensaje by mutableStateOf(Coach.bienvenida())
        private set

    var mood by mutableStateOf(AvatarMood.FELIZ)
        private set

    var textoEscuchado by mutableStateOf<String?>(null)
        private set

    var esperandoVoz by mutableStateOf(false)
        private set

    val armado = mutableStateListOf<String>()

    val insigniasGanadas = mutableListOf<Insignia>()

    val ejercicio: Ejercicio? get() = ejercicios.getOrNull(indice)

    val total: Int get() = ejercicios.size

    val perfecta: Boolean get() = fallosTotales == 0

    // ------------------------------------------------------------- enseñanza

    /** El paso que se está enseñando ahora mismo. */
    val pasoEnsenanza: PasoEnsenanza? get() = pasosEnsenanza.getOrNull(indiceEnsenanza)

    /** Índice (1..n) de la letra actual, que ocupa dos pasos. */
    val numeroLetraEnsenanza: Int get() = indiceEnsenanza / 2 + 1

    val etiquetaSiguienteEnsenanza: String
        get() {
            val paso = pasoEnsenanza
            return when {
                indiceEnsenanza >= pasosEnsenanza.lastIndex -> "¡A jugar! 🎮"
                paso != null && !paso.esMayuscula -> "Ahora la mayúscula ➡"
                else -> "Siguiente letra ➡"
            }
        }

    fun narrarEnsenanza() {
        val paso = pasoEnsenanza ?: return
        mood = AvatarMood.HABLANDO
        mensaje = "Mira bien la ${paso.nombreHablado}."
        voz.decir(paso.guion(), lento = true) {
            mood = AvatarMood.FELIZ
            // La nota didáctica va con la minúscula, que es donde se explica
            // la letra; repetirla en la mayúscula solo alarga la espera.
            if (!paso.esMayuscula) {
                paso.letra.notaDidactica?.let { nota ->
                    mensaje = nota
                    voz.decir(nota, lento = true)
                }
            }
        }
    }

    /** El niño toca el glifo para volver a oír cómo se llama. */
    fun escucharPasoEnsenanza() {
        val paso = pasoEnsenanza ?: return
        voz.decir(paso.nombreHablado, lento = true)
    }

    fun siguienteEnsenanza() {
        voz.callar()
        if (indiceEnsenanza < pasosEnsenanza.lastIndex) {
            indiceEnsenanza++
            narrarEnsenanza()
        } else {
            fase = FaseLeccion.EJERCICIO
            presentarEjercicio()
        }
    }

    fun saltarEnsenanza() {
        voz.callar()
        fase = FaseLeccion.EJERCICIO
        presentarEjercicio()
    }

    // ------------------------------------------------------------- ejercicios

    fun presentarEjercicio() {
        val ej = ejercicio ?: run { finalizar(); return }
        seleccion = null
        acierto = null
        revelada = false
        mostrarPista = false
        intentos = 0
        textoEscuchado = null
        armado.clear()
        mood = AvatarMood.HABLANDO
        mensaje = ej.consigna
        voz.decir(ej.consignaHablada, lento = ej.tipo != TipoEjercicio.ESCUCHA_Y_TOCA) {
            mood = if (ej.tipo == TipoEjercicio.MIRA_Y_DI) AvatarMood.PENSANDO else AvatarMood.FELIZ
        }
    }

    fun repetirConsigna(lento: Boolean = true) {
        val ej = ejercicio ?: return
        mood = AvatarMood.HABLANDO
        voz.decir(ej.consignaHablada, lento = lento) { mood = AvatarMood.FELIZ }
    }

    /** El niño toca una opción para oírla (exploración libre, siempre permitida). */
    fun escucharOpcion(audio: String) {
        voz.decir(audio, lento = true)
    }

    // ---------------------------------------------------------------- tocar

    fun responderToque(idOpcion: String) {
        val ej = ejercicio ?: return
        if (acierto == true || revelada) return
        seleccion = idOpcion
        val ok = idOpcion == ej.idCorrecto
        procesarResultado(ok, ej)
    }

    // --------------------------------------------------------- armar palabra

    fun agregarSilaba(texto: String) {
        val ej = ejercicio ?: return
        if (acierto == true || revelada) return
        armado += texto
        voz.decir(texto, lento = true)
        if (armado.size == ej.secuenciaCorrecta.size) {
            val ok = armado.toList() == ej.secuenciaCorrecta
            procesarResultado(ok, ej)
            if (!ok) armado.clear()
        }
    }

    fun borrarUltimaSilaba() {
        if (armado.isNotEmpty()) armado.removeAt(armado.lastIndex)
    }

    // ------------------------------------------------------------------- voz

    fun pedirVoz() {
        esperandoVoz = true
        mood = AvatarMood.ESCUCHANDO
        mensaje = Coach.animoVoz()
    }

    fun procesarVoz(alternativas: List<String>) {
        val ej = ejercicio ?: return
        esperandoVoz = false
        if (alternativas.isEmpty()) {
            mood = AvatarMood.ANIMANDO
            mensaje = Coach.vozNoEntendida()
            voz.decir(mensaje)
            return
        }
        textoEscuchado = alternativas.firstOrNull()
        val ev = PronunciationMatcher.evaluar(alternativas, ej.respuestasAceptadas)
        when (ev.resultado) {
            PronunciationMatcher.Resultado.CORRECTO -> procesarResultado(true, ej)
            PronunciationMatcher.Resultado.CASI -> {
                intentos++
                mood = AvatarMood.ANIMANDO
                mensaje = Coach.casi() + " Escucha cómo suena."
                voz.decirSecuencia(listOf(mensaje, ej.objetivoVisible ?: ""), lento = true)
                if (intentos >= 3) revelar(ej)
            }
            PronunciationMatcher.Resultado.INCORRECTO -> procesarResultado(false, ej)
        }
    }

    // -------------------------------------------------------------- resultado

    private fun procesarResultado(ok: Boolean, ej: Ejercicio) {
        acierto = ok
        progreso.registrarRespuesta(ej.claveItem, ok)
        if (ok) {
            racha++
            mejorRachaSesion = maxOf(mejorRachaSesion, racha)
            // Menos estrellas si necesitó intentos: sigue premiando, pero
            // distingue el dominio real.
            estrellas += if (intentos == 0) 2 else 1
            mood = AvatarMood.CELEBRANDO
            mensaje = Coach.acierto(racha)
            voz.decir(mensaje)
            if (racha >= 10) otorgar("racha10")
            fase = FaseLeccion.RESULTADO
        } else {
            intentos++
            fallosTotales++
            racha = 0
            mood = AvatarMood.ANIMANDO
            when {
                intentos >= 3 -> revelar(ej)
                intentos == 2 -> {
                    mostrarPista = true
                    mensaje = Coach.fallo(intentos) + " " + (ej.pista ?: Coach.consejo())
                    voz.decir(mensaje, lento = true)
                }
                else -> {
                    mensaje = Coach.fallo(intentos)
                    voz.decir(mensaje)
                }
            }
        }
    }

    private fun revelar(ej: Ejercicio) {
        revelada = true
        mostrarPista = true
        acierto = false
        val respuesta = ej.idCorrecto
            ?: ej.objetivoVisible
            ?: ej.secuenciaCorrecta.joinToString("")
        mensaje = "Mira, la respuesta es «$respuesta». ${Coach.consejo()}"
        mood = AvatarMood.HABLANDO
        voz.decirSecuencia(listOf("La respuesta es $respuesta.", Coach.consejo()), lento = true)
        fase = FaseLeccion.RESULTADO
    }

    fun reintentar() {
        seleccion = null
        acierto = null
        armado.clear()
        mood = AvatarMood.FELIZ
        repetirConsigna()
    }

    fun avanzar() {
        if (indice < ejercicios.lastIndex) {
            indice++
            fase = FaseLeccion.EJERCICIO
            presentarEjercicio()
        } else {
            finalizar()
        }
    }

    // --------------------------------------------------------------- final

    private fun finalizar() {
        fase = FaseLeccion.FINAL
        mood = AvatarMood.CELEBRANDO
        mensaje = Coach.finLeccion(perfecta)
        voz.decir(mensaje)

        progreso.completarLeccion(leccion.id, estrellas)
        progreso.sumarEstrellas(estrellas)
        if (mejorRachaSesion > progreso.mejorRacha) progreso.mejorRacha = mejorRachaSesion

        otorgar("primera")
        if (perfecta) otorgar("perfecta")
        if (mejorRachaSesion >= 10) otorgar("racha10")

        // Insignia por terminar un módulo completo.
        val hermanas = com.aprenderaleer.data.Curriculum.leccionesDe(leccion.modulo)
        if (hermanas.all { progreso.estaCompletada(it.id) }) {
            when (leccion.modulo) {
                Modulo.VOCALES -> otorgar("vocales")
                Modulo.ALFABETO -> otorgar("alfabeto")
                Modulo.SILABAS -> otorgar("silabas")
                Modulo.PALABRAS_2 -> otorgar("palabras2")
                Modulo.PALABRAS_3 -> otorgar("palabras3")
            }
        }
    }

    private fun otorgar(id: String) {
        if (progreso.otorgarInsignia(id)) {
            Insignias.de(id)?.let { insigniasGanadas += it }
        }
    }
}
