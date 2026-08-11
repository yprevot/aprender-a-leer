package com.aprenderaleer.ui

import kotlin.random.Random

/**
 * Guion del avatar "Lalo".
 *
 * Reglas de refuerzo aplicadas:
 *  · El elogio describe el ESFUERZO o la ESTRATEGIA ("escuchaste con
 *    atención"), no la capacidad ("qué listo eres"): sostiene mejor la
 *    motivación y no se desinfla cuando algo cuesta.
 *  · Tras un error nunca se dice "mal": se reencuadra ("casi", "vamos otra
 *    vez") y se ofrece una PISTA concreta.
 *  · A partir del segundo fallo seguido, el avatar baja la exigencia: da la
 *    pista, quita un distractor y propone respirar. Nunca insiste tres veces
 *    sobre el mismo ítem.
 */
object Coach {

    private val rnd = Random(System.nanoTime())

    val NOMBRE_AVATAR = "Lalo"

    private val BIENVENIDA = listOf(
        "¡Hola! Soy Lalo. ¿Jugamos a leer?",
        "¡Qué bueno verte! Vamos a descubrir letras.",
        "¡Hola! Hoy vas a aprender algo nuevo. ¿Listo?"
    )

    private val ACIERTO = listOf(
        "¡Muy bien! Escuchaste con mucha atención.",
        "¡Correcto! Te fijaste en el sonido.",
        "¡Eso es! Lo hiciste tú solo.",
        "¡Perfecto! Cada vez te sale más rápido.",
        "¡Sí señor! Esa es.",
        "¡Bravo! Estás leyendo de verdad."
    )

    private val ACIERTO_RACHA = listOf(
        "¡Uy, llevas varias seguidas! Estás volando.",
        "¡Racha! No te detengas.",
        "¡Qué bien vas! Tres seguidas."
    )

    private val CASI = listOf(
        "¡Casi, casi! Se parecía mucho.",
        "Estuviste cerquita. Prueba otra vez.",
        "Casi lo tienes. Escucha una vez más."
    )

    private val FALLO_1 = listOf(
        "No pasa nada, probemos de nuevo.",
        "Tranquilo, equivocarse ayuda a aprender.",
        "Uy, esa no era. ¡Otra vez!",
        "Casi. Vamos a intentarlo juntos."
    )

    private val FALLO_2 = listOf(
        "Respira. Yo te ayudo con una pista.",
        "Vamos despacio, sin prisa. Escucha bien.",
        "Te doy una ayudita. Mira con calma."
    )

    private val ESPERA = listOf(
        "Tómate tu tiempo, no hay prisa.",
        "Piensa tranquilo. Yo te espero.",
        "¿Quieres escucharlo otra vez? Toca la oreja."
    )

    private val ANIMO_VOZ = listOf(
        "Di la letra fuerte y claro, como un león.",
        "Acerca la boca y dilo con ganas.",
        "Habla despacito, yo te escucho."
    )

    private val VOZ_NO_ENTENDIDA = listOf(
        "No te escuché bien. ¿Lo dices otra vez?",
        "Había mucho ruido. Prueba de nuevo, un poquito más fuerte.",
        "Casi no te oigo. Acércate y dilo otra vez."
    )

    private val FIN_LECCION = listOf(
        "¡Terminaste! Estoy muy orgulloso de ti.",
        "¡Lección completa! Qué bien trabajaste.",
        "¡Lo lograste! Mira cuántas estrellas ganaste."
    )

    private val FIN_PERFECTO = listOf(
        "¡Sin ningún error! Eso fue increíble.",
        "¡Perfecto de principio a fin! Mereces una medalla."
    )

    private val DESCANSO = listOf(
        "Llevamos un buen rato. ¿Descansamos y seguimos luego?",
        "Tus ojos merecen un descanso. Volvemos en un ratito, ¿va?"
    )

    /** Consejos de estrategia, para que el niño sepa QUÉ hacer, no solo que falló. */
    private val CONSEJOS = listOf(
        "Truco: escucha cómo EMPIEZA la palabra. Ahí está la pista.",
        "Truco: mira la forma de la letra. ¿Tiene panza? ¿Tiene palito?",
        "Truco: di la sílaba en voz bajita antes de tocar.",
        "Truco: junta los dos sonidos sin parar en medio.",
        "Truco: si dudas, toca la oreja y escúchalo otra vez.",
        "Truco: separa la palabra en pedacitos, sílaba por sílaba."
    )

    private fun uno(l: List<String>) = l[rnd.nextInt(l.size)]

    fun bienvenida(nombre: String = ""): String =
        if (nombre.isBlank()) uno(BIENVENIDA) else "¡Hola, $nombre! Soy Lalo. ¿Jugamos a leer?"

    fun acierto(racha: Int): String =
        if (racha >= 3 && rnd.nextBoolean()) uno(ACIERTO_RACHA) else uno(ACIERTO)

    fun casi(): String = uno(CASI)

    fun fallo(consecutivos: Int): String =
        if (consecutivos >= 2) uno(FALLO_2) else uno(FALLO_1)

    fun espera(): String = uno(ESPERA)
    fun animoVoz(): String = uno(ANIMO_VOZ)
    fun vozNoEntendida(): String = uno(VOZ_NO_ENTENDIDA)
    fun consejo(): String = uno(CONSEJOS)
    fun descanso(): String = uno(DESCANSO)

    fun finLeccion(perfecta: Boolean): String =
        if (perfecta) uno(FIN_PERFECTO) else uno(FIN_LECCION)

    fun presentacionModulo(titulo: String): String = "Vamos con $titulo. ¡Atento!"
}
