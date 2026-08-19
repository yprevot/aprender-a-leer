# Aprender a Leer — juego con voz para Android

Juego en español para que un niño de 5 años aprenda a leer, con **voz de ida y vuelta**: el juego lee en voz alta y también escucha al niño y comprueba si pronunció bien. Funciona en móvil y en tablet, **sin conexión** y **sin pedir ningún permiso**.

Guiado por **Lalo**, un búho animado que explica, anima y da pistas concretas cuando algo cuesta.

---

## 1. Qué enseña y en qué orden

El currículo sigue el **método fonético-silábico**, que es el que mejor encaja con el español por ser una lengua de ortografía transparente (casi cada letra suena siempre igual).

| Bloque | Contenido | Lecciones |
|---|---|---|
| **Sesión 1 · Vocales** | a, e, i, o, u — una por lección + dos repasos | 7 |
| **Sesión 2 · Alfabeto** | Las 27 letras, en grupos de 5 + repaso final | 7 |
| **Sesión 3 · Sílabas** | Cada consonante con las 5 vocales (`pa pe pi po pu`) | 27 |
| **Fase 2 · Palabras de 2 sílabas** | ga-to, lu-na, ca-sa… | 6 |
| **Fase 2 · Palabras de 3 sílabas** | pe-lo-ta, za-pa-to, ven-ta-na… | 6 |

**53 lecciones**, hasta 12 ejercicios cada una (tandas de ~5 minutos, que es lo que aguanta la atención a esta edad).

### Nombre de la letra ≠ sonido de la letra

Es la distinción central del juego, tal y como se pidió. Al presentar una letra, Lalo dice literalmente:

> «Esta es la letra M, m. En el alfabeto se llama **eme**. Pero dentro de una palabra se llama **m**, y suena **mmm**. Se combina con las vocales así: ma, me, mi, mo, mu. Por ejemplo: mamá.»

Y hay ejercicios **separados** para cada cosa:
- «¿Cuál letra se llama *eme*?» → tocar la grafía
- «Esta letra suena *mmm*. Tócala» → tocar la grafía
- «¿Cómo se llama esta letra?» → el niño lo **dice** y el juego lo verifica

La pantalla de enseñanza muestra las dos fichas lado a lado (**Se llama** / **En la palabra suena**), y cada una se puede tocar para volver a oírla.

### Orden de las consonantes

`m, p, s, l, t, d, n, f, r, rr, c, qu, g, gu, b, v, ll, ñ, ch, j, h, z, ce/ci, y, k, x, w`

Empieza por consonantes continuas y de alta frecuencia (se pueden alargar y pronunciar aisladas: *mmm*, *sss*, *lll*) y deja para el final las de ortografía complicada (`c/z`, `g/gu`, `qu`, `x`, `w`). Los dígrafos `ch`, `ll`, `rr`, `qu`, `gu` y la `c` suave tienen su propia lección.

---

## 2. Tipos de ejercicio

| Tipo | Cómo funciona |
|---|---|
| **Escucha y toca** | Lalo dice una letra/sílaba/palabra; el niño toca la opción correcta |
| **Nombre → letra** | «¿Cuál se llama *eme*?» |
| **Sonido → letra** | «¿Cuál suena *mmm*?» |
| **Mira y di** | Se muestra `Mm` / `pa` / `gato`; el niño lo pronuncia y el juego lo evalúa |
| **Arma la palabra** | Sílabas desordenadas que hay que colocar en orden |

Se alternan a propósito ejercicios **receptivos** (reconocer, más fácil) y **productivos** (producir, más difícil), en proporción 2:1, y siempre empieza por los receptivos.

---

## 3. Cómo funciona la voz — sin pedir permisos

**El manifiesto no declara ni un solo permiso.** Se puede comprobar en `app/src/main/AndroidManifest.xml`.

**Salida de voz (TTS).** `android.speech.tts.TextToSpeech`, configurado en español (`es-US → es-ES → es-MX → es`), a velocidad reducida (0,68–0,88) y tono ligeramente agudo. No requiere permisos ni red.

**Entrada de voz (reconocimiento).** Aquí está el truco: en vez de instanciar un `SpeechRecognizer` dentro del proceso —lo que obligaría a declarar y solicitar `RECORD_AUDIO`— se lanza `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`. El **sistema** abre su propio diálogo de micrófono, graba, transcribe y devuelve solo texto. La app nunca toca el micrófono.

- Contrapartida: aparece la ventana del sistema mientras el niño habla. Se compensa avisándole antes («Toca y habla») y con un prompt grande dentro del diálogo.
- Si el dispositivo no tiene reconocedor, el juego no se bloquea: muestra un botón «Ya lo dije» para que el adulto valide.
- El bloque `<queries>` del manifiesto **no es un permiso**: solo permite resolver los servicios de voz del sistema en Android 11+.

### Evaluar la pronunciación sin ser injusto

El reconocedor devuelve **texto**, no fonemas, así que comparar cadenas tal cual sería demasiado estricto: el niño dice «be» y el motor transcribe «b», «ve» o «bé». Por eso todo se normaliza a una **clave fonética del español** antes de comparar (`PronunciationMatcher.kt`):

- se quitan tildes y la hache muda
- seseo: `z`, `ce`, `ci` → `s`
- yeísmo: `ll` = `y`
- `b` = `v` = `w`
- `c`, `k`, `qu` → `k`; `ge/gi` = `je/ji`; `x` → `ks`
- se colapsan letras repetidas (`mmm` → `m`)
- se prueban las 8 alternativas del reconocedor y también cada palabra suelta (para ignorar ruido tipo «dice pa»)
- se acepta la repetición infantil («papá» cuando se pidió «pa»)

Y, deliberadamente, **la tolerancia es cero en objetivos cortos**: en letras y sílabas, una letra de diferencia no es ruido del transcriptor, es otra respuesta. `pa` ≠ `ba`, `eme` ≠ `ene`, `gato` ≠ `pato`. Solo en palabras largas se perdona algún carácter.

---

## 4. Lalo: refuerzo y consejos

- El elogio describe el **esfuerzo o la estrategia** («escuchaste con atención»), no la capacidad («qué listo eres»).
- Nunca se dice «mal». Política de reintentos:
  1. **1er fallo** → ánimo y otra oportunidad.
  2. **2º fallo** → Lalo da la **pista concreta** del ejercicio y se atenúan los distractores hasta dejar solo dos opciones (andamiaje).
  3. **3er fallo** → se muestra la respuesta, se explica y se pasa. Nunca se deja al niño atascado.
- Consejos de estrategia («escucha cómo *empieza* la palabra», «di la sílaba bajito antes de tocar»), no solo «te equivocaste».
- El avatar está dibujado con `Canvas` de Compose: flota, parpadea, abre el pico al hablar, salta al celebrar, frunce el ceño al animar y emite ondas al escuchar. Sin archivos externos ni licencias, y escala perfecto de móvil a tablet.

### Recompensas
Estrellas (2 si acierta a la primera, 1 si necesitó intentos), racha, barra de avance y **9 medallas**: primera lección, rey de las vocales, dueño del alfabeto, maestro de sílabas, lector de palabras, lector campeón, racha de 10, buena voz y lección perfecta.

### Repetición espaciada
Cada ítem (letra, sílaba, palabra) tiene una **caja de Leitner** 0–4. Acierto → sube de caja y sale menos. Fallo → vuelve a la caja 0 y sale mucho. Los ítems flojos aparecen antes dentro de la tanda.

---

## 5. Interfaz móvil y tablet

Un solo layout adaptativo con `BoxWithConstraints` (sin dependencias extra):

- **Móvil vertical**: avatar y bocadillo arriba en fila, ejercicio debajo.
- **Tablet o apaisado** (≥ 700 dp o ancho > alto): columna lateral fija con Lalo, el bocadillo y el botón «Otra vez»; ejercicio a la derecha.
- Rejilla de opciones con `GridCells.Adaptive` (150 dp en móvil, 200 dp en tablet): reflúye sola.
- Objetivos táctiles **enormes** (≥ 96 dp): a los 5 años la motricidad fina falla y un botón pequeño produce errores que no son de lectura.
- Tema siempre claro y de alto contraste, tipografía muy grande, `configChanges` en el manifiesto para que girar el dispositivo no pierda el ejercicio a medias, y la pantalla no se apaga durante una explicación.
- **De borde a borde.** Desde `targetSdk 35` Android dibuja siempre bajo las barras de estado y navegación y ya no se puede renunciar a ello, así que la app lo declara (`enableEdgeToEdge()` con barras transparentes e iconos oscuros) y aparta el contenido con `safeDrawingPadding()`. El fondo crema se pinta en toda la ventana —también bajo las barras y en la franja del recorte de cámara en apaisado— y también como `windowBackground`, para que no haya destello blanco al abrir.
- Sin `screenOrientation` fijo: Android 16 ignora esa restricción en la mayoría de dispositivos, y el layout ya es adaptativo.

---

## 6. Estructura del proyecto

```
Leer/
├── app/src/main/
│   ├── AndroidManifest.xml            ← cero permisos
│   ├── java/com/aprenderaleer/
│   │   ├── MainActivity.kt            ← Activity única + navegación
│   │   ├── data/
│   │   │   ├── Models.kt              ← Letra, Palabra, Ejercicio, Leccion…
│   │   │   ├── Curriculum.kt          ← TODO el contenido (alfabeto, sílabas, palabras, lecciones)
│   │   │   └── ProgressStore.kt       ← progreso + Leitner + medallas (SharedPreferences)
│   │   ├── engine/
│   │   │   ├── SpeechEngine.kt        ← TTS en español
│   │   │   ├── VoiceRecognizer.kt     ← reconocimiento vía Intent (sin permisos)
│   │   │   ├── PronunciationMatcher.kt← clave fonética + evaluación
│   │   │   └── ExerciseGenerator.kt   ← genera las tandas de ejercicios
│   │   └── ui/
│   │       ├── Avatar.kt              ← Lalo, dibujado y animado en Canvas
│   │       ├── Coach.kt               ← guion, elogios, pistas y consejos
│   │       ├── Components.kt          ← tarjetas, micrófono, estrellas, bocadillo
│   │       ├── LessonController.kt    ← máquina de estados de una lección
│   │       ├── LessonScreen.kt        ← pantalla de lección (adaptativa)
│   │       ├── HomeScreen.kt          ← mapa, medallas, ajustes
│   │       ├── RewardScreen.kt        ← celebración final
│   │       └── theme/Theme.kt
│   └── res/                           ← icono (con capa monocroma), tema, strings
├── app/src/test/                      ← tests JVM (3 clases, 31 casos)
├── gradle/libs.versions.toml          ← catálogo: único sitio con las versiones
├── keystore.properties                ← credenciales de firma (NO va a git)
├── dist/                              ← APK y AAB generados (NO va a git)
└── tools/Verificacion.kt              ← verificación sin Android SDK
```

---

## 7. Cómo compilar, verificar y publicar

### Requisitos

| Herramienta | Versión | Nota |
|---|---|---|
| JDK | 17 o superior | vale el que trae Android Studio (`Contents/jbr`) |
| Gradle | 9.7 | lo baja solo el *wrapper* (`./gradlew`), ya incluido en el repo |
| Android Gradle Plugin | 9.3.1 | |
| Kotlin | 2.4.10 | |
| Compose BOM | 2026.08.00 | |
| SDK de compilación | `compileSdk 37`, `targetSdk 36`, `minSdk 24` | AGP descarga la plataforma que falte |

Las versiones están en un único sitio: `gradle/libs.versions.toml`.

### Compilar

```bash
./gradlew assembleDebug      # APK de pruebas (applicationId .debug: convive con la instalada)
./gradlew assembleRelease    # APK firmado y optimizado con R8 (~1 MB)
./gradlew bundleRelease      # AAB para Google Play
```

Los artefactos quedan en `app/build/outputs/` (y se copian a mano a `dist/` para distribuir).

### Firma

`app/build.gradle.kts` lee `keystore.properties` de la raíz:

```properties
storeFile=keystore/upload-keystore.jks
storePassword=…
keyAlias=upload
keyPassword=…
```

Si ese fichero no existe, el *release* se firma con la clave de depuración y **sigue compilando**: el repositorio se puede clonar y compilar sin secretos. Ni el `.jks` ni el `.properties` entran en git.

Para crear una clave de subida nueva:

```bash
keytool -genkeypair -v -keystore keystore/upload-keystore.jks -storetype PKCS12 \
        -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

> Guarda copia del `.jks` y de su contraseña. Sin ellos no se pueden firmar actualizaciones para la misma ficha de Play (aunque con *Play App Signing* activo, Google puede restablecer la clave de subida).

### Cómo verificar

**Tests JVM** (no requieren emulador):
```bash
./gradlew test
```
Comprueban el alfabeto y su orden, que cada consonante tenga familia silábica, que la división en sílabas reconstruya la palabra, que ningún ejercicio generado sea irresoluble y ~25 casos de reconocimiento de voz.

**Lint** (se ejecuta solo en cada `assembleRelease`, y aborta el build si encuentra un error):
```bash
./gradlew lintRelease     # informe en app/build/reports/lint-results-release.html
```

**Verificación completa sin Android SDK** (`tools/Verificacion.kt`), que recorre las 53 lecciones con 40 semillas aleatorias distintas:
```bash
kotlinc app/src/main/java/com/aprenderaleer/data/Models.kt \
        app/src/main/java/com/aprenderaleer/data/Curriculum.kt \
        app/src/main/java/com/aprenderaleer/engine/PronunciationMatcher.kt \
        app/src/main/java/com/aprenderaleer/engine/ExerciseGenerator.kt \
        tools/Verificacion.kt -include-runtime -d verif.jar
java -jar verif.jar
```
(requiere un stub de `ProgressStore` sin Android; ver comentario en el archivo)

> Esta verificación ya detectó y se corrigieron tres fallos reales durante el desarrollo: una palabra de ejemplo que no empezaba por su letra (`ñ`), una respuesta hablada imposible de validar (`h`), y un `sortedByDescending` con aleatoriedad dentro del comparador que hacía **crashear la app** con `Comparison method violates its general contract!`.

**En dispositivo o emulador** (Android 7.0 / API 24 o superior):
```bash
./gradlew installRelease
adb shell am start -n com.aprenderaleer/.MainActivity
```

Para que la voz funcione bien en un emulador, instala el motor de TTS en español y Google App (en un teléfono real suele venir todo listo).

### Publicar en Google Play

Paso a paso en [docs/PUBLICAR-EN-PLAY.md](docs/PUBLICAR-EN-PLAY.md).

---

## 8. Privacidad

Sin permisos, sin internet, sin analítica, sin cuentas. El progreso se guarda solo en `SharedPreferences` del dispositivo. El único momento en que sale audio del proceso es cuando el **sistema operativo** abre su diálogo de reconocimiento de voz, que el usuario ve en pantalla.

Comprobable sobre el APK ya construido:

```bash
aapt2 dump permissions dist/AprenderALeer-1.0-release.apk
```

Devuelve un único permiso, `com.aprenderaleer.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`: lo declara y lo usa la propia app, lo añade `androidx.core` para que sus `BroadcastReceiver` internos no queden expuestos, es de nivel *signature* y **no se le muestra al usuario**. Ni `INTERNET`, ni `RECORD_AUDIO`, ni almacenamiento: sin permiso de red, la app no puede conectarse aunque quisiera.

---

## 9. Limitaciones conocidas y siguientes pasos

- El **sonido aislado** de las oclusivas (p, t, k, b, d, g) no se puede sintetizar sin añadir una vocal: es una limitación acústica real, no del TTS. Por eso en esas letras el sonido se enseña con la familia silábica (`pa, pe, pi, po, pu`), que es lo que hace un maestro. Las continuas (m, n, s, f, l, r, z) sí se alargan (`mmm`, `sss`).
- El reconocimiento depende del motor del dispositivo y suele necesitar conexión en algunos teléfonos (Google permite descargar el paquete de voz en español para uso sin conexión).
- Aún no hay **sílabas trabadas** (`pla, tra, bra…`) ni **inversas** (`ar, es, in…`). Son la continuación natural de la Sesión 3 y el modelo de datos ya las admite: basta añadir entradas en `Curriculum.digrafos` / `ordenSilabico`.
- No hay grabación ni informe para el adulto más allá del panel de ajustes.

---

## 10. Base pedagógica

- Método silábico y fonético, y por qué empezar por las vocales y seguir con sílabas simples antes que trabadas o inversas: [Guía del método silábico (Luca)](https://lucaedu.com/blog/metodo-silabico/) · [Método fonético (Luca)](https://lucaedu.com/blog/metodo-fonetico/) · [El método fonético para enseñar a leer (GuiaInfantil)](https://www.guiainfantil.com/educacion/lectura/el-metodo-fonetico-para-ensenar-a-los-ninos-a-leer/) · [Método silábico: características (Psicología y Mente)](https://psicologiaymente.com/desarrollo/metodo-silabico)
- Que el **nombre** y el **sonido** de las letras son conocimientos distintos, y que el nombre por sí solo no basta si no se ancla en conciencia fonológica: [El nombre y el sonido de las letras: ¿conocimientos diferenciables?](https://www.researchgate.net/publication/326900932_El_nombre_y_el_sonido_de_las_letras_conocimientos_diferenciables) · [La conciencia fonológica y la lectura (Signorini)](http://www.lecturayvida.fahce.unlp.edu.ar/numeros/a19n3/19_03_Signorini.pdf) · [Conciencia fonológica y desarrollo lector](https://isfdsarmiento-cha.infd.edu.ar/sitio/upload/conciencia_fonologica.pdf)
- Repetición espaciada con cajas (sistema Leitner) para decidir qué ítems repasar y cuánto: [The Leitner System](https://e-student.org/leitner-system/) · [Spaced repetition en el aula](https://thirdspacelearning.com/blog/spaced-repetition/)
