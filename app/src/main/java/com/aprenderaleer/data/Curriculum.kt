package com.aprenderaleer.data

/**
 * Todo el contenido del juego. Sin red, sin assets: 100% offline.
 *
 * Notas de diseño de audio:
 *  - `sonidoSostenido` solo se rellena en consonantes CONTINUAS (m, n, s, f, l, r, z, j, ñ, ll, y, ch)
 *    porque un TTS en español pronuncia razonablemente "mmm", "sss", etc.
 *  - En las OCLUSIVAS (p, t, k, b, d, g) es acústicamente imposible aislar el
 *    fonema sin añadir una vocal, así que el sonido se enseña con la familia
 *    silábica ("pa, pe, pi, po, pu"), que es lo que hace un maestro real.
 */
object Curriculum {

    // ---------------------------------------------------------------- VOCALES

    val vocales: List<Letra> = listOf(
        Letra(
            id = "a", mayuscula = "A", minuscula = "a", nombreAlfabeto = "a",
            sonidoSostenido = "a", esVocal = true,
            palabraEjemplo = "araña", emojiEjemplo = "🕷️",
            variantesNombre = listOf("a", "ha", "ah")
        ),
        Letra(
            id = "e", mayuscula = "E", minuscula = "e", nombreAlfabeto = "e",
            sonidoSostenido = "e", esVocal = true,
            palabraEjemplo = "elefante", emojiEjemplo = "🐘",
            variantesNombre = listOf("e", "he", "eh")
        ),
        Letra(
            id = "i", mayuscula = "I", minuscula = "i", nombreAlfabeto = "i",
            sonidoSostenido = "i", esVocal = true,
            palabraEjemplo = "isla", emojiEjemplo = "🏝️",
            variantesNombre = listOf("i", "y", "hi", "ih")
        ),
        Letra(
            id = "o", mayuscula = "O", minuscula = "o", nombreAlfabeto = "o",
            sonidoSostenido = "o", esVocal = true,
            palabraEjemplo = "oso", emojiEjemplo = "🐻",
            variantesNombre = listOf("o", "ho", "oh")
        ),
        Letra(
            id = "u", mayuscula = "U", minuscula = "u", nombreAlfabeto = "u",
            sonidoSostenido = "u", esVocal = true,
            palabraEjemplo = "uva", emojiEjemplo = "🍇",
            variantesNombre = listOf("u", "hu", "uh")
        )
    )

    // ------------------------------------------------------------ CONSONANTES

    val consonantes: List<Letra> = listOf(
        Letra("b", "B", "b", "be", null, false, "bebé", "👶",
            listOf("ba", "be", "bi", "bo", "bu"),
            listOf("be", "b", "ve", "uve", "be larga", "be grande")),

        Letra("c", "C", "c", "ce", null, false, "casa", "🏠",
            listOf("ca", "co", "cu"),
            listOf("ce", "c", "se"),
            notaDidactica = "Con la a, la o y la u suena fuerte: ca, co, cu. " +
                "Con la e y la i suena suave: ce, ci."),

        Letra("d", "D", "d", "de", null, false, "dado", "🎲",
            listOf("da", "de", "di", "do", "du"),
            listOf("de", "d")),

        Letra("f", "F", "f", "efe", "fff", false, "foca", "🦭",
            listOf("fa", "fe", "fi", "fo", "fu"),
            listOf("efe", "f", "fe")),

        Letra("g", "G", "g", "ge", null, false, "gato", "🐱",
            listOf("ga", "go", "gu"),
            listOf("ge", "g", "je"),
            notaDidactica = "Con la a, la o y la u suena fuerte: ga, go, gu. " +
                "Para que suene fuerte con la e y la i le ponemos una u: gue, gui."),

        // Ojo: "h" a secas no es pronunciable, así que no se admite como
        // respuesta hablada; solo su nombre ("hache" / "ache").
        Letra("h", "H", "h", "hache", null, false, "hoja", "🍃",
            listOf("ha", "he", "hi", "ho", "hu"),
            listOf("hache", "ache"),
            notaDidactica = "La hache es muda: se escribe pero no suena."),

        Letra("j", "J", "j", "jota", "jjj", false, "jirafa", "🦒",
            listOf("ja", "je", "ji", "jo", "ju"),
            listOf("jota", "j")),

        Letra("k", "K", "k", "ka", null, false, "koala", "🐨",
            listOf("ka", "ke", "ki", "ko", "ku"),
            listOf("ka", "k", "ca")),

        Letra("l", "L", "l", "ele", "lll", false, "luna", "🌙",
            listOf("la", "le", "li", "lo", "lu"),
            listOf("ele", "l", "le")),

        Letra("m", "M", "m", "eme", "mmm", false, "mamá", "👩",
            listOf("ma", "me", "mi", "mo", "mu"),
            listOf("eme", "m", "me")),

        Letra("n", "N", "n", "ene", "nnn", false, "nube", "☁️",
            listOf("na", "ne", "ni", "no", "nu"),
            listOf("ene", "n", "ne")),

        Letra("ñ", "Ñ", "ñ", "eñe", "ññ", false, "ñu", "🦬",
            listOf("ña", "ñe", "ñi", "ño", "ñu"),
            listOf("eñe", "ñ", "ñe", "enie"),
            notaDidactica = "Casi ninguna palabra empieza por eñe: casi siempre va " +
                "en medio, como en niño, año o España."),

        Letra("p", "P", "p", "pe", null, false, "pato", "🦆",
            listOf("pa", "pe", "pi", "po", "pu"),
            listOf("pe", "p")),

        Letra("q", "Q", "q", "cu", null, false, "queso", "🧀",
            listOf("que", "qui"),
            listOf("cu", "q", "ku"),
            notaDidactica = "La cu siempre va con una u que no suena: que, qui."),

        Letra("r", "R", "r", "erre", "rrr", false, "ratón", "🐭",
            listOf("ra", "re", "ri", "ro", "ru"),
            listOf("erre", "ere", "r", "re")),

        Letra("s", "S", "s", "ese", "sss", false, "sol", "☀️",
            listOf("sa", "se", "si", "so", "su"),
            listOf("ese", "s", "se")),

        Letra("t", "T", "t", "te", null, false, "tomate", "🍅",
            listOf("ta", "te", "ti", "to", "tu"),
            listOf("te", "t")),

        Letra("v", "V", "v", "uve", null, false, "vaca", "🐄",
            listOf("va", "ve", "vi", "vo", "vu"),
            listOf("uve", "ve", "v", "be", "ve corta", "ve chica")),

        Letra("w", "W", "w", "uve doble", null, false, "wifi", "📶",
            listOf("wa", "we", "wi", "wo", "wu"),
            listOf("uve doble", "doble ve", "doble u", "w")),

        Letra("x", "X", "x", "equis", "ks", false, "xilófono", "🎹",
            listOf("xa", "xe", "xi", "xo", "xu"),
            listOf("equis", "x", "ekis")),

        Letra("y", "Y", "y", "ye", "yyy", false, "yema", "🥚",
            listOf("ya", "ye", "yi", "yo", "yu"),
            listOf("ye", "i griega", "y", "ll")),

        Letra("z", "Z", "z", "zeta", "zzz", false, "zapato", "👟",
            listOf("za", "zo", "zu"),
            listOf("zeta", "seta", "z", "ceta"))
    )

    /** Dígrafos: no son letras del alfabeto pero sí sonidos que hay que dominar. */
    val digrafos: List<Letra> = listOf(
        Letra("ch", "CH", "ch", "che", "chchch", false, "chocolate", "🍫",
            listOf("cha", "che", "chi", "cho", "chu"),
            listOf("che", "ce hache", "ch")),
        Letra("ll", "LL", "ll", "elle", "yyy", false, "llave", "🔑",
            listOf("lla", "lle", "lli", "llo", "llu"),
            listOf("elle", "doble ele", "ll", "ye")),
        Letra("rr", "RR", "rr", "doble erre", "rrrr", false, "perro", "🐕",
            listOf("rra", "rre", "rri", "rro", "rru"),
            listOf("doble erre", "erre", "rr"),
            notaDidactica = "La doble erre solo va entre dos vocales: pe-rro."),
        Letra("qu", "QU", "qu", "cu u", null, false, "queso", "🧀",
            listOf("que", "qui"),
            listOf("cu", "que", "qu")),
        Letra("gu", "GU", "gu", "ge u", null, false, "guitarra", "🎸",
            listOf("gue", "gui"),
            listOf("ge u", "gu", "gue")),
        Letra("ce", "C", "c", "ce suave", "sss", false, "cebra", "🦓",
            listOf("ce", "ci"),
            listOf("ce", "se"))
    )

    private val ORDEN_ALFABETO = listOf(
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "ñ", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    /** Alfabeto español oficial: 27 letras, en orden. */
    val alfabeto: List<Letra> = (vocales + consonantes).sortedBy { ORDEN_ALFABETO.indexOf(it.id) }

    /**
     * Orden pedagógico para la sesión 3 (sílabas).
     * Se empieza por consonantes continuas y de alta frecuencia (m, p, s, l, t)
     * porque son las más fáciles de segmentar y de pronunciar aisladas, y se
     * dejan para el final las de ortografía compleja (c/z, g/gu, qu, x, w).
     */
    val ordenSilabico: List<String> = listOf(
        "m", "p", "s", "l", "t", "d", "n", "f", "r", "rr",
        "c", "qu", "g", "gu", "b", "v", "ll", "ñ", "ch", "j",
        "h", "z", "ce", "y", "k", "x", "w"
    )

    private val porId: Map<String, Letra> =
        (vocales + consonantes + digrafos).associateBy { it.id }

    fun letra(id: String): Letra? = porId[id]

    /** Todas las letras/dígrafos con familia silábica, en orden pedagógico. */
    val letrasSilabicas: List<Letra> = ordenSilabico.mapNotNull { porId[it] }

    /** Todas las sílabas CV del idioma que se enseñan, sin repetir. */
    val todasLasSilabas: List<String> =
        letrasSilabicas.flatMap { it.silabas }.distinct()

    // -------------------------------------------------------------- PALABRAS

    val palabras2: List<Palabra> = listOf(
        Palabra("mamá", listOf("ma", "má"), "👩"),
        Palabra("papá", listOf("pa", "pá"), "👨"),
        Palabra("mesa", listOf("me", "sa"), "🍽️"),
        Palabra("luna", listOf("lu", "na"), "🌙"),
        Palabra("casa", listOf("ca", "sa"), "🏠"),
        Palabra("dedo", listOf("de", "do"), "👆"),
        Palabra("pato", listOf("pa", "to"), "🦆"),
        Palabra("sopa", listOf("so", "pa"), "🍲"),
        Palabra("gato", listOf("ga", "to"), "🐱"),
        Palabra("foca", listOf("fo", "ca"), "🦭"),
        Palabra("mano", listOf("ma", "no"), "✋"),
        Palabra("pino", listOf("pi", "no"), "🌲"),
        Palabra("taza", listOf("ta", "za"), "☕"),
        Palabra("nube", listOf("nu", "be"), "☁️"),
        Palabra("rana", listOf("ra", "na"), "🐸"),
        Palabra("vaca", listOf("va", "ca"), "🐄"),
        Palabra("cama", listOf("ca", "ma"), "🛏️"),
        Palabra("lobo", listOf("lo", "bo"), "🐺"),
        Palabra("dado", listOf("da", "do"), "🎲"),
        Palabra("pelo", listOf("pe", "lo"), "💇"),
        Palabra("mono", listOf("mo", "no"), "🐵"),
        Palabra("hoja", listOf("ho", "ja"), "🍃"),
        Palabra("niño", listOf("ni", "ño"), "🧒"),
        Palabra("queso", listOf("que", "so"), "🧀"),
        Palabra("boca", listOf("bo", "ca"), "👄"),
        Palabra("cuna", listOf("cu", "na"), "🍼"),
        Palabra("jugo", listOf("ju", "go"), "🧃"),
        Palabra("silla", listOf("si", "lla"), "🪑"),
        Palabra("pera", listOf("pe", "ra"), "🍐"),
        Palabra("uva", listOf("u", "va"), "🍇"),
        Palabra("oso", listOf("o", "so"), "🐻"),
        Palabra("ojo", listOf("o", "jo"), "👁️"),
        Palabra("llave", listOf("lla", "ve"), "🔑"),
        Palabra("coche", listOf("co", "che"), "🚗"),
        Palabra("perro", listOf("pe", "rro"), "🐕"),
        Palabra("toro", listOf("to", "ro"), "🐂")
    )

    val palabras3: List<Palabra> = listOf(
        Palabra("pelota", listOf("pe", "lo", "ta"), "⚽"),
        Palabra("camisa", listOf("ca", "mi", "sa"), "👕"),
        Palabra("zapato", listOf("za", "pa", "to"), "👟"),
        Palabra("ventana", listOf("ven", "ta", "na"), "🪟"),
        Palabra("muñeca", listOf("mu", "ñe", "ca"), "🪆"),
        Palabra("tomate", listOf("to", "ma", "te"), "🍅"),
        Palabra("banana", listOf("ba", "na", "na"), "🍌"),
        Palabra("caballo", listOf("ca", "ba", "llo"), "🐴"),
        Palabra("conejo", listOf("co", "ne", "jo"), "🐰"),
        Palabra("girasol", listOf("gi", "ra", "sol"), "🌻"),
        Palabra("pirata", listOf("pi", "ra", "ta"), "🏴"),
        Palabra("cocina", listOf("co", "ci", "na"), "🍳"),
        Palabra("manzana", listOf("man", "za", "na"), "🍎"),
        Palabra("araña", listOf("a", "ra", "ña"), "🕷️"),
        Palabra("botella", listOf("bo", "te", "lla"), "🍾"),
        Palabra("campana", listOf("cam", "pa", "na"), "🔔"),
        Palabra("cuchara", listOf("cu", "cha", "ra"), "🥄"),
        Palabra("gusano", listOf("gu", "sa", "no"), "🐛"),
        Palabra("helado", listOf("he", "la", "do"), "🍦"),
        Palabra("jirafa", listOf("ji", "ra", "fa"), "🦒"),
        Palabra("martillo", listOf("mar", "ti", "llo"), "🔨"),
        Palabra("naranja", listOf("na", "ran", "ja"), "🍊"),
        Palabra("pescado", listOf("pes", "ca", "do"), "🐟"),
        Palabra("sombrero", listOf("som", "bre", "ro"), "🎩"),
        Palabra("tijera", listOf("ti", "je", "ra"), "✂️"),
        Palabra("ballena", listOf("ba", "lle", "na"), "🐋"),
        Palabra("estrella", listOf("es", "tre", "lla"), "⭐"),
        Palabra("guitarra", listOf("gui", "ta", "rra"), "🎸"),
        Palabra("maleta", listOf("ma", "le", "ta"), "🧳"),
        Palabra("paloma", listOf("pa", "lo", "ma"), "🕊️"),
        Palabra("semilla", listOf("se", "mi", "lla"), "🌱"),
        Palabra("tortuga", listOf("tor", "tu", "ga"), "🐢"),
        Palabra("galleta", listOf("ga", "lle", "ta"), "🍪"),
        Palabra("montaña", listOf("mon", "ta", "ña"), "⛰️"),
        Palabra("zanahoria", listOf("za", "na", "ho", "ria"), "🥕"),
        Palabra("mariposa", listOf("ma", "ri", "po", "sa"), "🦋")
    )

    // ------------------------------------------------------------- LECCIONES

    /** Construye todas las lecciones del juego en orden. */
    val lecciones: List<Leccion> by lazy { construirLecciones() }

    private fun construirLecciones(): List<Leccion> {
        val out = mutableListOf<Leccion>()

        // Sesión 1 — Vocales. Una lección por vocal + una de repaso.
        vocales.forEachIndexed { i, v ->
            out += Leccion(
                id = "voc_${v.id}",
                titulo = "La vocal ${v.mayuscula}${v.minuscula}",
                subtitulo = "Escúchala, tócala y dila",
                emoji = v.emojiEjemplo,
                modulo = Modulo.VOCALES,
                letrasEnsenadas = listOf(v)
            )
            if (i == 2) {
                out += Leccion(
                    id = "voc_repaso1", titulo = "Repaso: a, e, i",
                    subtitulo = "Las tres primeras vocales", emoji = "🎈",
                    modulo = Modulo.VOCALES, letrasEnsenadas = vocales.take(3)
                )
            }
        }
        out += Leccion(
            id = "voc_repaso2", titulo = "Repaso: las 5 vocales",
            subtitulo = "a, e, i, o, u", emoji = "🌈",
            modulo = Modulo.VOCALES, letrasEnsenadas = vocales
        )

        // Sesión 2 — Alfabeto completo, en grupos de 5 letras.
        alfabeto.chunked(5).forEachIndexed { i, grupo ->
            out += Leccion(
                id = "alf_$i",
                titulo = "Alfabeto ${grupo.first().mayuscula} – ${grupo.last().mayuscula}",
                subtitulo = grupo.joinToString(" ") { it.mayuscula },
                emoji = "🔤",
                modulo = Modulo.ALFABETO,
                letrasEnsenadas = grupo
            )
        }
        out += Leccion(
            id = "alf_final", titulo = "El alfabeto completo",
            subtitulo = "Las 27 letras", emoji = "🏅",
            modulo = Modulo.ALFABETO, letrasEnsenadas = alfabeto
        )

        // Sesión 3 — Una lección por consonante con su familia silábica.
        letrasSilabicas.forEach { l ->
            out += Leccion(
                id = "sil_${l.id}",
                titulo = "${l.mayuscula}${l.minuscula}  →  ${l.silabas.joinToString(", ")}",
                subtitulo = "La familia de la ${l.nombreAlfabeto}",
                emoji = l.emojiEjemplo,
                modulo = Modulo.SILABAS,
                letrasEnsenadas = listOf(l)
            )
        }

        // Fase 2 — Palabras de 2 sílabas, en tandas de 6.
        palabras2.chunked(6).forEachIndexed { i, tanda ->
            out += Leccion(
                id = "pal2_$i",
                titulo = "Palabras de 2 sílabas · ${i + 1}",
                subtitulo = tanda.joinToString(", ") { it.texto },
                emoji = tanda.first().emoji,
                modulo = Modulo.PALABRAS_2,
                palabras = tanda
            )
        }

        // Fase 2 — Palabras de 3 sílabas, en tandas de 6.
        palabras3.chunked(6).forEachIndexed { i, tanda ->
            out += Leccion(
                id = "pal3_$i",
                titulo = "Palabras largas · ${i + 1}",
                subtitulo = tanda.joinToString(", ") { it.texto },
                emoji = tanda.first().emoji,
                modulo = Modulo.PALABRAS_3,
                palabras = tanda
            )
        }

        return out
    }

    fun leccion(id: String): Leccion? = lecciones.firstOrNull { it.id == id }

    fun leccionesDe(modulo: Modulo): List<Leccion> = lecciones.filter { it.modulo == modulo }
}
