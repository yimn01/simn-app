# 🦆➡️🟩🟧 SIMN — App del cliente (v2)

Esta versión convierte el proyecto en la **app del cliente de un LSP**: login,
solicitar intérprete **VRI (video)** u **OPI (audio)**, pantalla de videollamada,
**rating** y **reporte de uso** — con los colores y el logo de SIMN.

---

## Qué cambió respecto a la v1

- 🎨 **Branding SIMN**: colores teal + naranja, logo en el login y como ícono de la app. El nombre de la app ahora es **SIMN**.
- 🔐 **Login del cliente** contra tu `api.php` (con modo demo de respaldo).
- 🏠 **Pantalla principal** con dos botones grandes: **Video (VRI)** y **Audio (OPI)**, selector de idioma, y un resumen de uso.
- 🎥 **Pantalla de videollamada**: un WebView a pantalla completa que abre la **sala que genera QI**, con permisos de cámara/micrófono. Para OPI muestra número + PIN y botón para marcar.
- ⭐ **Rating** después de cada sesión (estrellas + comentario).
- 📊 **Reporte de uso** del cliente (sesiones, minutos, idioma, intérprete).

> En **modo demo**, el botón de Video abre una sala real de prueba (Jitsi) para que veas el WebView funcionando de punta a punta, y el de Audio muestra un número demo. Con datos reales, todo eso lo devuelve tu QI.

---

## Cómo actualizar tu repositorio (rápido)

**Todos los cambios están dentro de la carpeta `app/`.** No tienes que tocar `.github`, `gradle`, ni los archivos de la raíz otra vez. Solo re-subes `app/`:

1. Descomprime el nuevo `QuackoBilling.zip` que te mandé.
2. En tu repo de GitHub: **Add file → Upload files**.
3. Arrastra **solo la carpeta `app`** (completa) a la zona de subida. GitHub va a **actualizar los archivos que cambiaron y agregar los nuevos** en un solo commit.
4. Baja y dale **Commit changes**.
5. GitHub recompila solo (pestaña **Actions**). Cuando salga ✅, baja a **Artifacts**, descarga el APK nuevo y reinstálalo en tu teléfono.

> Al reinstalar sobre la versión vieja, si Android reclama, desinstala primero la app anterior y vuelve a instalar.

**Probar en demo:** entra con **client / client** → prueba el botón de **Video** (te pedirá permiso de cámara/mic y abrirá la sala) y el de **Audio**. Al terminar una llamada verás la pantalla de **rating**.

---

## Para conectarla a tus datos reales (lo que necesito de ti)

La app ya está cableada; solo falta apuntar a los endpoints reales. En la pantalla de login, **apaga "Demo mode"** y pon tu **Server URL** (`https://quack-o.com`). La app espera que `api.php` responda **JSON** con este contrato (los nombres se ajustan fácil en `Repository.kt` si los tuyos difieren):

### 1) Login del cliente
`POST api.php`  → `action=client_login&username=...&password=...`
```json
{ "ok": true, "data": {
    "client_id": "100482",
    "client_name": "Mercy General Hospital",
    "token": "abc123",
    "role": "client"
}}
```

### 2) Solicitar sesión (VRI/OPI) — el corazón de la llamada
`POST api.php`  → `action=request_session&client_id=...&token=...&modality=VRI|OPI&language=Spanish`

QI hace su ruteo/matching de intérprete (lo que ya tienes) y devuelve **cómo conectar**:
```json
{ "ok": true, "data": {
    "session_id": "S-8891",
    "mode": "room",                 // "room" (VRI/video) o "dial" (OPI/teléfono)
    "room_url": "https://<tu-sala-de-QI>/abc",   // para mode=room
    "dial_number": "+18005551234",  // para mode=dial
    "pin": "4827",                  // para mode=dial
    "interpreter": "amaya.r"
}}
```
- **VRI** → `mode:"room"` + `room_url`: la app abre esa URL en el WebView (video nativo con cámara/mic).
- **OPI** → `mode:"dial"` + `dial_number` (+ `pin`): la app ofrece marcar. O si prefieres callback, me dices y lo cambio.

### 3) Guardar rating
`POST api.php`  → `action=rate_session&session_id=...&token=...&stars=5&comment=...`
```json
{ "ok": true }
```

### 4) Reporte de uso
`GET api.php?action=usage&client_id=...&token=...`
```json
{ "ok": true, "data": [
    { "id":"CDR-1001","date":"2026-07-10 14:32","client_id":"100482",
      "client_name":"Mercy General","origin":"web","language":"Spanish",
      "interpreter":"amaya.r","minutes":42,"capped":false }
]}
```

---

## Lo único que de verdad necesito de ti para dejarla 100% conectada

1. **El login real**: pégame la parte de tu `api.php` que maneja el login del cliente (o dime el `action`, los parámetros y qué devuelve). Con eso ajusto el paso 1 exacto.
2. **Cómo se dispara una llamada hoy**: cuando un cliente pide intérprete en el portal web actual, ¿qué pasa por debajo? ¿QI te da una **URL de sala** (Jitsi/Twilio/Daily/otra)? ¿un número? Con eso ajusto el paso 2 a tu realidad — es lo único que no puedo adivinar.

Mándame esos dos datos y te dejo la app conectada de verdad a tu quack-o/QI.
