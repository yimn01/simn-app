# 🟩🟧 SIMN — App del cliente (v3, la buena)

Después de leer tu `api.php` quedó clarísimo: **tu portal web ya hace todo** —
login, llamadas **VRI (video)** y **OPI (audio)** con su softphone SIP/WebRTC,
historial, ratings, facturas. Así que la app ahora es un **shell nativo que abre
tu portal** dentro de un WebView, con cámara y micrófono habilitados.

## Por qué así (y no rehacerlo nativo)

Las llamadas de tu sistema se hacen con un **softphone SIP/WebRTC en el navegador**
(las credenciales SIP vienen en `client_login`). Reimplementar ese motor en Kotlin
sería semanas de trabajo, frágil, y tendría que calzar exacto con tu Asterisk.
En cambio, **envolver tu portal** te da llamadas **reales** de inmediato,
reusando el 100% de lo que ya te funciona. Es lo que hacen las apps serias que
tienen un portal web maduro.

## Qué hace la app

- Abre **`https://quack-o.com/`** a pantalla completa (tu portal del cliente).
- Pide permiso de **cámara y micrófono** al abrir → el video VRI conecta al toque.
- Mantiene la **sesión** (cookies) como en el navegador.
- **Botón atrás** navega dentro del portal.
- Ícono y splash con el **logo SIMN**; barra de estado en teal.
- Abre `tel:` / `mailto:` con las apps del teléfono; lo demás (portal + pagos Stripe) se queda dentro de la app.

## Cómo subir esta v3 al repo (igual que antes)

Todos los cambios están dentro de **`app/`**. No toques `.github` ni `gradle`.

1. Descomprime el nuevo `QuackoBilling.zip`.
2. En tu repo → **Add file → Upload files** → arrastra **solo la carpeta `app`**.
3. **Commit changes**. GitHub recompila solo (pestaña **Actions**).
4. Cuando salga ✅, baja a **Artifacts**, descarga el APK y reinstálalo. Si Android reclama, desinstala la versión anterior primero.

## Probarla

Ábrela: verás tu portal. Entra con una **cuenta real de cliente** (código de acceso + contraseña — la misma que usan en el navegador). Prueba una llamada de **video**: la app pedirá cámara/mic y conectará por tu sistema. 🎥

## Si el portal del cliente NO está en la raíz

Si tu página de cliente no es `https://quack-o.com/` sino otra ruta (por ejemplo `.../index.html` o `.../portal`), dímelo y cambio **una línea** en `app/src/main/java/com/quacko/billing/data/Config.kt`:

```kotlin
var portalUrl: String = "https://quack-o.com/"
```

## Notas

- El look en el teléfono depende de qué tan "responsive" sea tu `index.html`. Si se ve apretado en móvil, después le metemos unos ajustes de CSS al portal para que se vea de app. La funcionalidad ya queda desde el día 1.
- Cuando quieras, limpiamos el código viejo (las pantallas nativas que quedaron sin usar) — no estorban al build, solo son de la versión anterior.
