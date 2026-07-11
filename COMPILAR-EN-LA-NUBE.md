# ☁️ Compilar el APK en la nube (sin instalar NADA en tu compu)

Este es el camino más fácil si no quieres instalar Android Studio. Un servidor
gratuito de **GitHub** compila el APK por ti y te lo deja para descargar. Solo
necesitas una cuenta de GitHub (gratis) y un navegador.

> ⏱️ Toma ~10 min la primera vez. Lo único que instalas en tu teléfono al final es el APK.

---

## Paso 1 — Crea una cuenta de GitHub (si no tienes)

1. Ve a **https://github.com** y dale **Sign up**. Es gratis.
2. Confirma tu correo.

## Paso 2 — Crea un repositorio nuevo

1. Ya con sesión iniciada, ve a **https://github.com/new**
2. En **Repository name** escribe: `quacko-billing`
3. Déjalo en **Public** o **Private** (cualquiera sirve).
4. **NO** marques "Add a README". Dale **Create repository**.

## Paso 3 — Sube los archivos del proyecto

Tienes el `QuackoBilling.zip` que te mandé. **Descomprímelo** primero (clic derecho → Extraer). Queda una carpeta `QuackoBilling` con todo adentro.

En la página de tu repo recién creado:

1. Dale al link **"uploading an existing file"** (o ve a **Add file → Upload files**).
2. Abre la carpeta `QuackoBilling` en tu explorador de archivos, **selecciona TODO lo que está adentro** (incluyendo las carpetas `app`, `gradle`, `.github`, y los archivos `gradlew`, `settings.gradle.kts`, etc.) y **arrástralo** a la ventana del navegador.
   - 💡 Importante: sube el **contenido** de la carpeta, no la carpeta en sí. Es decir, en el repo debe quedar `app/`, `gradle/`, `gradlew`… en la raíz, no `QuackoBilling/app/`.
   - 💡 Si tu explorador esconde archivos que empiezan con punto (como `.github`), actívalos: en Windows, pestaña *Vista → Elementos ocultos*; en Mac, `Cmd + Shift + .`. La carpeta **`.github` es la que dispara la compilación**, así que asegúrate de subirla.
3. Abajo dale al botón verde **Commit changes**.

## Paso 4 — Espera a que compile (automático)

En cuanto subes los archivos, GitHub arranca la compilación solo.

1. En tu repo, dale a la pestaña **Actions** (arriba).
2. Verás una ejecución llamada **"Build APK"** con un punto amarillo girando 🟡 (compilando). Tarda 2–5 min.
3. Cuando termine bien, se pone un ✅ verde. (Si sale ❌ rojo, ve a la sección *Si algo falla* abajo.)

## Paso 5 — Descarga tu APK

1. Haz clic en la ejecución que terminó con ✅.
2. Baja hasta la sección **Artifacts** (al final de la página).
3. Descarga **`quacko-billing-debug-apk`**. Es un `.zip`.
4. Descomprímelo → adentro está **`app-debug.apk`**. ¡Ese es tu app! 🎉

## Paso 6 — Instálalo en tu teléfono

1. Pásate el `app-debug.apk` al teléfono (por cable USB, o mándatelo por WhatsApp/correo/Drive a ti mismo y ábrelo desde el teléfono).
2. Tócalo para instalar. La primera vez Android pide permiso:
   - Toca **Configuración** en el aviso → activa **"Permitir de esta fuente"** (instalar apps desconocidas).
   - Regresa y dale **Instalar**.
   - Si sale "Play Protect", toca **"Instalar de todos modos"**.
3. Abre **Quacko Billing** y entra con **admin / admin** o **client / client** (viene en modo demo con datos de ejemplo).

---

## Volver a compilar después de un cambio

Cada vez que cambies algo, solo sube el archivo nuevo (Add file → Upload files, o edita en GitHub) y **Commit**. GitHub recompila solo y genera un APK nuevo en Actions. También puedes forzarlo a mano: pestaña **Actions → Build APK → Run workflow**.

---

## Si algo falla (❌ rojo en Actions)

1. Entra a la ejecución roja y haz clic en el paso que falló (normalmente **"Compilar APK de debug"**) para ver el error.
2. **Copia ese texto del error y pégamelo aquí** — con eso lo arreglo enseguida. Los tropiezos típicos de la primera vez son que no se subió la carpeta `.github` o que faltó algún archivo del wrapper (`gradlew`, `gradle/wrapper/gradle-wrapper.jar`); ambos ya vienen en el zip, solo hay que asegurarse de subirlos todos.

---

## ¿Prefieres no pelear con la subida manual?

Si me das acceso (creando un repo y un token, o subiendo el zip a un repo), puedo
ayudarte a dejar el repo listo. O si te animas con Android Studio, la otra guía
(`GUIA-COMPLETA.md`) te lleva por ese camino paso a paso. Dime cuál prefieres.
