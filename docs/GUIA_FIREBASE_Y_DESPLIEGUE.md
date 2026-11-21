# Guía de Despliegue Web en la Nube — Casanare 2027 (Asamblea Departamental)

Esta guía describe la plataforma web **SPA / PWA** (`/web/index.html` + `/web/manifest.json`) construida con **HTML5 + Tailwind CSS + Leaflet.js + ES Modules**, conectada a **Firebase Authentication + Cloud Firestore** y lista para publicarse mediante un enlace web único en **Firebase Hosting** (`https://gen-lang-client-0565836876.web.app`).

---

## 1. Arquitectura Web en la Nube (Doble Capa sobre Geografía Real)

- **Acceso por Enlace Único (Desktop y Móvil):** Diseño fluido `100vw × 100vh`, sin instalación ni empaquetado de APK.
- **Mapas Base Abiertos sin Marcas de Agua:**
  - **Calles (Esri World Street Map)** como capa predeterminada.
  - **OpenStreetMap** como respaldo automático ante fallos de red y capa seleccionable.
  - **Relieve Topográfico (Esri)** y **Satélite (Esri)**.
  - **Respaldo Geográfico Vectorial:** Retícula georreferenciada de Casanare y corredor Marginal de la Selva que garantizan visibilidad territorial en todo momento.
- **Geografía Real de Casanare (DANE 85 — 19 Municipios):**
  - Límites municipales reales WGS84 (`EPSG:4326`) embebidos en `/web/index.html` para carga instantánea sin bloqueos CORS.
  - **Modo Ciudadano (Público):** Consulta propuestas, gestión realizada y buzón veredal de los 19 municipios sin exponer datos internos.
  - **Modo Campaña (Privado con Firebase Auth):** Mapa de calor electoral, alertas de energía territorial, pines interactivos de líderes con conteo de referidos, captura de simpatizantes con Habeas Data (Ley 1581) y Panel CMS editable.

---

## 2. Proyecto Firebase Configurado

- **Project ID:** `gen-lang-client-0565836876`
- **Firestore Database ID:** `ai-studio-android-casanare-02962cc5-e063-4443-bfba-7d9b407aafea`
- **URL Pública de Hosting:** `https://gen-lang-client-0565836876.web.app`

### Archivos Listos para Hosting
- `firebase.json`: Configura `"hosting": { "public": "web" }` con reescritura SPA (`** -> /index.html`), cabeceras sin caché para el HTML principal y enlace a `firestore.rules`.
- `.firebaserc`: Apunta por defecto al proyecto `gen-lang-client-0565836876`.
- `web/manifest.json`: Manifiesto PWA instalable desde el navegador.

### Colecciones en Cloud Firestore
1. `configuracion_campana/general`: Identidad parametrizable (`nombre_candidato`, `eslogan`, `color_primario`, `color_secundario`, `logo_url`, `updated_at`).
2. `municipios/{codigo_dane}`: 19 municipios oficiales (`85001`–`85440`), metas, energía y resumen público.
3. `veredas/{vereda_id}`: Corregimientos y veredas con `lat`, `lon`, `lider_uid`, `lider_nombre`, meta y campo opcional `geojson`.
4. `usuarios_campana/{uid}`: Roles `ADMIN`, `COORD_MUNICIPAL` y `LIDER_VEREDAL`.
5. `simpatizantes/{simpatizante_id}`: Registro veredal protegido con `authorized_uids` y aceptación explícita de Habeas Data.
6. `gestion_propuestas/{item_id}`: Tarjetas de gestión realizada y propuestas filtradas por `visible_publico`.
7. `reportes_ciudadanos/{reporte_id}`: Buzón veredal ciudadano con creación pública validada (`estado == "PENDIENTE"`).

---

## 3. Pasos para Publicar el Enlace Web en Firebase Hosting

1. **Registrar la App Web en Firebase Console:**
   - Ve a **Firebase Console → Project Settings → Your apps → Web (`</>`)** en el proyecto `gen-lang-client-0565836876`.
   - Copia `apiKey` y `appId` y reemplaza `"TU_API_KEY"` y `"TU_APP_ID"` dentro de `firebaseConfig` en `/web/index.html`.
2. **Habilitar Proveedores de Autenticación y Dominio:**
   - En **Authentication → Sign-in method**, activa **Google** y **Correo electrónico/contraseña**.
   - Verifica que `gen-lang-client-0565836876.web.app` esté en **Authorized domains**.
3. **Desplegar a la Nube con Firebase CLI:**
   ```bash
   firebase login
   firebase use gen-lang-client-0565836876
   firebase deploy --only hosting,firestore:rules
   ```
4. **Primer Acceso `ADMIN` y Carga de Catálogo:**
   - Abre `https://gen-lang-client-0565836876.web.app`.
   - Inicia sesión en **Acceso Equipo de Campaña** con `appcasanare2027@gmail.com` (administrador inicial en `firestore.rules`) o registra otro usuario en `/usuarios_campana/{uid}` con `rol: "ADMIN"` y `activo: true`.
   - Abre **Panel CMS → Sincronizar Catálogo Base a Firestore** para poblar la configuración inicial y los 19 municipios en la nube.

---

## 4. Cómo Cargar Polígonos GeoJSON Reales de Veredas por Etapas

1. Descarga la capa oficial de veredas (DANE / IGAC / Datos Abiertos Colombia) filtrada por `MPIO_CCDGO` (`85010` Aguazul, `85162` Monterrey, `85440` Villanueva, `85001` Yopal, etc.).
2. Simplifica la geometría en [mapshaper.org](https://mapshaper.org/) (~5%–10%) para mantener cada `Feature` liviano (< 20 KB).
3. Guarda el JSON serializado en el campo `geojson` del documento correspondiente en `veredas/{vereda_id}` dentro de Firestore; `/web/index.html` lo renderizará automáticamente sobre el mapa base sin inventar polígonos.
