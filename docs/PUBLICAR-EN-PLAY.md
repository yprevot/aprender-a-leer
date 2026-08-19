# Publicar «Aprender a Leer» en Google Play

Estado del proyecto a 18 de agosto de 2026: el AAB firmado ya está generado en
`dist/AprenderALeer-1.0-play.aab` y cumple los requisitos técnicos de la tienda.
Lo que queda es trabajo de ficha y de políticas, que se hace en Play Console.

---

## 1. Requisitos técnicos (ya cumplidos)

| Requisito de Play | Estado |
|---|---|
| `targetSdk` ≥ 36 (Android 16) para apps y actualizaciones nuevas desde el **31/08/2026** | ✅ `targetSdk = 36` |
| Formato Android App Bundle (el APK ya no se acepta para apps nuevas) | ✅ `dist/AprenderALeer-1.0-play.aab` |
| Firmado con una clave RSA ≥ 2048 bits y validez más allá de 2033 | ✅ RSA 4096, válida hasta 2054 |
| Soporte de páginas de 16 KB (obligatorio para apps con código nativo) | ✅ verificado con `zipalign -c -P 16` |
| 64 bits | ✅ no hay código nativo propio; las de Compose vienen en arm64/x86_64 |

Comandos de comprobación:

```bash
zipalign -c -P 16 -v 4 dist/AprenderALeer-1.0-release.apk | tail -1
apksigner verify --print-certs dist/AprenderALeer-1.0-release.apk
```

## 2. Antes de subir: decidir versión y clave

- `versionCode` **sube en cada subida** (1 → 2 → 3…). Play rechaza repetir uno.
  Se edita en `app/build.gradle.kts`.
- `versionName` es el texto que ve la gente («1.0», «1.1»).
- La clave de subida está en `keystore/upload-keystore.jks` (contraseña en
  `keystore.properties`). **Haz copia de seguridad de ambos, fuera del portátil.**
- Activa **Play App Signing** al crear la app: Google guarda la clave de firma
  final y tu `.jks` pasa a ser solo la clave de *subida*, que sí se puede
  restablecer si se pierde.

## 3. Subir

1. Play Console → *Crear app* → nombre «Aprender a Leer», español, gratuita, app (no juego… ver nota).
2. *Producción* (o mejor: **prueba interna** primero) → *Crear versión*.
3. Sube `dist/AprenderALeer-1.0-play.aab`.
4. Sube también `dist/mapping-1.0.txt` como *fichero de desofuscación*: sin él,
   los informes de fallos de R8 llegan ilegibles.

## 4. Ficha de la tienda (mínimo obligatorio)

- Icono 512×512 PNG y gráfico destacado 1024×500.
- Al menos 2 capturas de teléfono; si declaras compatibilidad con tablet, también
  de tablet 7" y 10" (la app es adaptativa, así que conviene).
- Descripción corta (80 caracteres) y completa (4000).
- Categoría sugerida: *Educación*. Etiqueta de contenido: apto para todos.

## 5. Políticas — esto es lo delicado, porque es una app para niños

La app está dirigida a niños de 5 años, así que entra en la **política de
familias** de Google Play, más exigente que la normal:

- **Público objetivo**: marca solo el tramo *hasta 5 años*. Al hacerlo, la app
  queda sujeta a la política de familias y a la revisión correspondiente.
- **Política de privacidad**: es **obligatoria** una URL pública, incluso sin
  recoger datos. Debe decir explícitamente que no se recopila nada.
- **Seguridad de los datos**: declara *no se recopilan ni se comparten datos*.
  Es cierto: no hay red (ni permiso `INTERNET`), ni analítica, ni cuentas; el
  progreso vive en `SharedPreferences` del dispositivo.
- **Anuncios**: declara que no hay. Y que siga así: la publicidad en apps para
  menores de 13 años tiene reglas propias muy estrictas.
- **Reconocimiento de voz**: la app **no graba**; abre el diálogo del sistema,
  que transcribe y devuelve texto. Conviene explicarlo tal cual en la política
  de privacidad y en la ficha, porque un revisor verá la pantalla de micrófono.
- **Cuestionario de clasificación de contenido**: sin violencia, sin compras,
  sin interacción entre usuarios, sin ubicación.
- **Cuenta de desarrollador**: verificación de identidad y, si la cuenta es
  personal y se creó después de noviembre de 2023, **prueba cerrada con 12
  testers durante 14 días** antes de poder publicar en producción.

## 6. Después de publicar

- Vigila *Android vitals* (ANR y fallos) las primeras semanas.
- Cada actualización: sube `versionCode`, vuelve a generar el AAB y sube el
  `mapping.txt` correspondiente.
- El requisito de `targetSdk` se renueva cada agosto: en 2027 tocará API 37.

## 7. Sacar un APK de un AAB (instalación directa, sin tienda)

Para repartir la app a mano (familia, colegio) sirve el APK de release ya
firmado: `dist/AprenderALeer-1.0-release.apk`. Si quieres exactamente lo que
Play generaría a partir del bundle:

```bash
bundletool build-apks --bundle=dist/AprenderALeer-1.0-play.aab \
  --output=dist/AprenderALeer.apks --mode=universal \
  --ks=keystore/upload-keystore.jks --ks-key-alias=upload
```
