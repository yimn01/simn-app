# 🦆 Quacko Billing — App Android nativa (Kotlin/Compose)

Guía **desde cero** para compilar el APK, pasarlo al teléfono y probarlo en vivo.
Escrita asumiendo que **nunca** has hecho esto. Lee de arriba hacia abajo.

---

## 0. Qué recibiste y qué hace la app

Es el **proyecto completo** de una app Android nativa. Tiene:

- **Login con roles**: la misma app muestra la vista de **cliente** o la de **admin** según quién entra.
- **Vista de cliente**: tarjeta de "monto a pagar", minutos web vs 800, número de llamadas, alerta de llamadas capadas, e **historial de llamadas** con idioma, intérprete, origen (Web/800) y duración.
- **Vista de admin**: filtro por cliente, tarjetas de totales (facturado, minutos, llamadas, clientes, capadas), y sub-tabs **"Billing by client"** y **"Call detail"** — igual que tu reporte del portal.
- **Modo demo integrado**: la app trae datos de ejemplo adentro, así que **funciona y se puede explorar sin backend**. Cuando quieras, apagas el modo demo y la conectas a tu `api.php` real.

> ⚠️ **Importante**: yo **no pude compilar el APK** en mi entorno porque Google (dl.google.com), Google Maven y Maven Central están bloqueados ahí. Pero tu computadora **sí** tiene internet, así que el APK se genera en tu máquina siguiendo esta guía. La primera compilación descarga ~1 GB (Android SDK + librerías); es normal y solo pasa una vez.

---

## 1. Qué es un APK (30 segundos)

Un **APK** es el archivo instalable de una app Android (como un `.exe` en Windows, pero para Android). Compilar = convertir este código en ese archivo `.apk`. Luego lo copias al teléfono y lo instalas.

Hay dos "sabores":
- **debug** → el que usaremos. Fácil, se instala directo, ideal para probar.
- **release** → el firmado para publicar en Play Store. Eso lo vemos después si quieres.

---

## 2. CAMINO A — Con Android Studio (RECOMENDADO para ti)

Android Studio es el programa oficial de Google. Trae **todo** adentro (el Android SDK, el compilador, un emulador de teléfono) y hace el trabajo pesado por ti. Para alguien que nunca ha compilado, **este es el camino**.

### 2.1 Instalar Android Studio

1. Ve a **https://developer.android.com/studio**
2. Descarga **Android Studio** (botón grande verde). Pesa ~1 GB.
3. Instálalo:
   - **Windows**: abre el `.exe` y dale Siguiente → Siguiente → Instalar.
   - **Mac**: abre el `.dmg` y arrastra Android Studio a la carpeta *Applications*.
4. Ábrelo. La primera vez sale un asistente ("Setup Wizard"):
   - Elige **Standard** y dale Next/Finish.
   - Va a **descargar el Android SDK** automáticamente (otra descarga grande, déjalo terminar).
   - Si te pide **aceptar licencias**, dale *Accept* a todas y Finish.

### 2.2 Abrir el proyecto

1. Descomprime el archivo `QuackoBilling.zip` que te envié en una carpeta fácil de encontrar (ej. tu Escritorio). Queda una carpeta llamada **`QuackoBilling`**.
2. En Android Studio: **File → Open** (o "Open" en la pantalla de bienvenida).
3. Navega y selecciona la carpeta **`QuackoBilling`** (la carpeta, no un archivo de adentro). Dale **OK**.
4. Android Studio abre el proyecto y arranca solo el **"Gradle Sync"** (abajo verás una barrita de progreso que dice *"Sync"* y va bajando librerías).
   - **Esto necesita internet y tarda varios minutos la primera vez.** Es normal.
   - Si sale un aviso arriba tipo *"Gradle JDK"* o te pide instalar algún componente del SDK (por ejemplo *"Install build tools"* o *"Android SDK Platform 34"*), dale al link/botón para instalarlo y acepta licencias.
5. Cuando abajo diga **"Sync finished"** (o desaparezca la barra sin errores en rojo), ya está listo.

> Si el Sync falla, casi siempre es (a) sin internet, o (b) falta aceptar una licencia del SDK. Ve a la sección **8. Problemas comunes**.

### 2.3 Compilar el APK

1. En el menú de arriba: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
2. Espera. Abajo a la derecha saldrá una notificación cuando termine: **"APK(s) generated successfully"** con un link que dice **"locate"**.
3. Dale a **"locate"** y se abre la carpeta con tu archivo. El APK está en:

   ```
   QuackoBilling/app/build/outputs/apk/debug/app-debug.apk
   ```

   Ese `app-debug.apk` es tu app. 🎉

---

## 3. Instalar el APK en tu teléfono

Ya tienes el `app-debug.apk`. Ahora al teléfono. Elige el método que te sea más cómodo.

### Método fácil: pasar el archivo al teléfono

1. Conecta el teléfono a la compu con cable USB (o usa Google Drive / WhatsApp "a ti mismo" / correo para enviarte el `app-debug.apk`).
2. Copia el `app-debug.apk` a la carpeta **Descargas** del teléfono.
3. En el teléfono, abre la app **Archivos** (o "Mis archivos"), entra a **Descargas** y toca **`app-debug.apk`**.
4. La primera vez Android te dirá que **no puede instalar apps de fuentes desconocidas**. Es normal por seguridad:
   - Toca **"Configuración"** en ese aviso.
   - Activa **"Permitir de esta fuente"** (o "Instalar apps desconocidas") para la app desde la que estás instalando (Archivos o Chrome).
   - Vuelve atrás y toca **Instalar**.
5. Abre la app **Quacko Billing** (ícono de patito teal). Listo.

> Nota: como es un APK *debug*, Android puede mostrar una advertencia de "Play Protect". Es esperado para apps que no vienen de la tienda; toca **"Instalar de todos modos"**.

---

## 4. CORRER EN VIVO desde Android Studio (lo mejor para probar)

Esto es lo más cómodo: conectas el teléfono por USB y con **un botón** la app se compila, se instala y se abre sola. Además ves los mensajes/errores en vivo (Logcat).

### 4.1 Activar el modo desarrollador en tu teléfono (solo una vez)

1. En el teléfono: **Ajustes → Acerca del teléfono**.
2. Busca **"Número de compilación"** (Build number) y **tócalo 7 veces seguidas**. Saldrá "¡Ya eres desarrollador!".
3. Regresa a Ajustes → ahora aparece **"Opciones de desarrollador"** (a veces dentro de *Sistema*). Éntrale.
4. Activa **"Depuración por USB"** (USB debugging).

### 4.2 Conectar y correr

1. Conecta el teléfono a la compu por USB.
2. En el teléfono saldrá **"¿Permitir depuración USB?"** → marca *Siempre* y **Permitir**.
3. En Android Studio, arriba al centro, verás un menú de dispositivos. Debe aparecer **tu teléfono** por su nombre. Selecciónalo.
4. Dale al botón **▶ Run** (el triángulo verde), o menú **Run → Run 'app'**.
5. La app se compila, se instala y **se abre sola en tu teléfono**. 🚀
6. Abajo, la pestaña **"Logcat"** muestra en vivo lo que hace la app (útil si algo falla).

> ¿No tienes cable o no aparece el teléfono? Puedes usar el **emulador**: en Android Studio, menú **Device Manager** (ícono de teléfono a la derecha) → **Create device** → elige un Pixel → descarga una imagen (ej. API 34) → Finish. Luego lo eliges en el menú de dispositivos y le das ▶ Run.

---

## 5. Probar la app (qué hacer adentro)

La app arranca en **modo demo** (datos de ejemplo, sin backend). En la pantalla de login:

- Toca **"admin / admin"** y luego **Sign in** → entras a la **consola de admin**: verás totales, el filtro por cliente, y las tabs *Billing by client* y *Call detail*. Fíjate en las llamadas marcadas con **⚠ CAP** en rojo (las que pasaron de 120 min).
- Cierra sesión (ícono arriba a la derecha) y entra con **"client / client"** → verás la **vista de cliente**: monto a pagar, minutos web vs 800 e historial de llamadas.

Todo eso son **datos de demostración**. Cuando lo conectemos a tu `api.php`, se llena con datos reales.

---

## 6. Conectar la app a tu `api.php` real

Cuando quieras dejar el demo y usar datos de verdad:

1. En la pantalla de **login**, apaga el switch **"Demo mode"**.
2. Aparece el campo **"Server URL"**. Por defecto trae `https://quack-o.com`. Déjalo o cámbialo.
3. Entra con un usuario real.

La app espera que `api.php` responda **JSON**. El contrato que ya dejé cableado (y que es fácil de ajustar) es:

**Login** — `POST api.php` con `action=login&username=...&password=...`
```json
{ "ok": true, "data": {
    "username": "gil",
    "role": "admin",              // "admin" o "client"
    "client_id": "100482",        // solo para client; vacío para admin
    "client_name": "Mercy General Hospital"
} }
```

**Detalle de llamadas** — `GET api.php?action=call_detail&client_id=...&from=YYYY-MM-DD&to=YYYY-MM-DD`
```json
{ "ok": true, "data": [
    {
      "id": "CDR-1001",
      "date": "2026-07-10 14:32",
      "client_id": "100482",
      "client_name": "Mercy General Hospital",
      "origin": "web",            // "web" o "800"
      "language": "Spanish",
      "interpreter": "amaya.r",
      "minutes": 42,
      "capped": false
    }
] }
```

Con eso la app calcula sola los totales, el billing por cliente y las alertas de capadas (no necesita más endpoints para empezar). Si tu `api.php` devuelve los campos con otros nombres, se ajusta en un solo archivo: `app/src/main/java/com/quacko/billing/data/Repository.kt` (función `parseCalls`). **Pásame la respuesta real de tu `api.php` y te lo dejo cableado exacto.**

> Detalle técnico: el `AndroidManifest.xml` ya permite tráfico HTTP en claro (`usesCleartextTraffic`) por si pruebas contra `http://`. Para producción conviene usar `https://`.

---

## 7. CAMINO B — Compilar por línea de comandos (opcional)

Si prefieres la terminal en vez de Android Studio (igual necesitas el Android SDK instalado y la variable `ANDROID_HOME` apuntando a él):

```bash
cd QuackoBilling
./gradlew assembleDebug         # en Windows: gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

Para instalarlo directo en un teléfono conectado con depuración USB:
```bash
./gradlew installDebug
```

> La forma más simple de tener el SDK sin Android Studio es instalar los *command-line tools* de Android y correr `sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"`. Pero para tu primer APK, **usa el Camino A**; es mucho menos enredado.

---

## 8. Problemas comunes (y cómo resolverlos)

- **"Gradle sync failed" / se queda descargando para siempre** → revisa internet. La primera vez baja ~1 GB; ten paciencia. Si estás detrás de un proxy/VPN corporativo, desactívalo o configúralo en *Settings → Build → Gradle*.
- **"Failed to install the following SDK components" / "licenses not accepted"** → en Android Studio: *Settings → Languages & Frameworks → Android SDK → SDK Tools*, marca **Android SDK Command-line Tools** y aplica. O corre `sdkmanager --licenses` y acepta todo.
- **"Minimum supported Gradle version" o error de JDK** → en *Settings → Build → Gradle*, pon **Gradle JDK = 17** (o 21). El proyecto usa AGP 8.5 + Kotlin 2.0, compatibles con JDK 17 y 21.
- **El teléfono no aparece al conectarlo por USB** → cambia el modo USB del teléfono a **"Transferir archivos (MTP)"**, confirma el aviso de *"Permitir depuración USB"*, y prueba otro cable (algunos cables son solo de carga).
- **"App not installed" al tocar el APK** → si ya tenías una versión instalada, desinstálala primero. El id de la app en debug es `com.quacko.billing.debug`.
- **Play Protect bloquea la instalación** → toca **"Instalar de todos modos"** / "More details" → "Install anyway". Es normal en APKs que no vienen de la tienda.

---

## 9. ¿Prefieres que yo compile el APK por ti?

Si tienes la **app de escritorio de Claude** abierta y me das acceso a una carpeta de tu compu, puedo intentar compilar el APK **directamente en tu máquina** por el puente (tu compu sí llega a los repos de Google). Solo dímelo y lo intento; si no, con esta guía lo sacas en ~20 min la primera vez.

---

## Estructura del proyecto (referencia rápida)

```
QuackoBilling/
├─ app/
│  ├─ build.gradle.kts            ← dependencias y config del módulo
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/quacko/billing/
│     │  ├─ MainActivity.kt        ← punto de entrada + navegación por rol
│     │  ├─ data/
│     │  │  ├─ Config.kt           ← URL del server + switch de modo demo
│     │  │  ├─ ApiClient.kt        ← cliente HTTP contra api.php
│     │  │  ├─ Repository.kt       ← lógica: demo vs red, totales, rollups
│     │  │  ├─ DemoData.kt         ← datos de ejemplo
│     │  │  └─ model/Models.kt     ← tipos (llamada, resumen, sesión, cap 120)
│     │  └─ ui/
│     │     ├─ AppViewModel.kt     ← estado de sesión/login
│     │     ├─ auth/LoginScreen.kt
│     │     ├─ client/ClientScreens.kt
│     │     ├─ admin/AdminScreens.kt
│     │     ├─ common/Common.kt    ← tarjetas, pills, formato de moneda
│     │     └─ theme/              ← colores/tipografía Quacko
│     └─ res/                      ← ícono y recursos
├─ build.gradle.kts / settings.gradle.kts / gradle.properties
└─ gradlew / gradlew.bat / gradle/wrapper/   ← el "wrapper" (no borrar)
```
