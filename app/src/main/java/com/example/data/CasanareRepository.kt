package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CampaignConstants
import com.example.model.ConfiguracionCampana
import com.example.model.EstadoReporte
import com.example.model.GestionPropuesta
import com.example.model.Municipio
import com.example.model.ReporteCiudadano
import com.example.model.RolCampana
import com.example.model.Simpatizante
import com.example.model.TipoContenido
import com.example.model.UsuarioCampana
import com.example.model.Vereda
import com.example.model.ZonaVereda
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID

class CasanareRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        createConfiguredFirestore(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        fun createConfiguredFirestore(databaseId: String): FirebaseFirestore {
            val instance = FirebaseFirestore.getInstance(databaseId)
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                instance.firestoreSettings = settings
            } catch (_: IllegalStateException) {
                // Settings already applied before first Firestore operation
            }
            return instance
        }
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Debes iniciar sesión con Google para realizar esta acción.")

    private fun isBootstrappedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(CampaignConstants.BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true) &&
            user.isEmailVerified
    }

    private fun <T> Flow<T>.handleStreamErrors(
        operationType: OperationType,
        path: String,
        requireAuth: Boolean = false
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && (!requireAuth || auth.currentUser != null)) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    // --- STREAMS PÚBLICOS (MODO CIUDADANO) ---

    fun observeConfiguracion(): Flow<ConfiguracionCampana?> {
        val path = "configuracion_campana/general"
        return db.collection("configuracion_campana").document("general")
            .snapshots()
            .map { doc ->
                if (!doc.exists()) null
                else ConfiguracionCampana(
                    id = doc.id,
                    nombreCandidato = doc.getString("nombre_candidato") ?: "[Nombre del Candidato]",
                    eslogan = doc.getString("eslogan") ?: "[Eslogan de Campaña]",
                    colorPrimario = doc.getString("color_primario") ?: "#0B6E4F",
                    colorSecundario = doc.getString("color_secundario") ?: "#D97706",
                    logoUrl = doc.getString("logo_url") ?: "",
                    updatedAt = doc.getTimestamp("updated_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                )
            }
            .handleStreamErrors(OperationType.GET, path)
    }

    fun observeMunicipios(): Flow<List<Municipio>> {
        val path = "municipios"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toMunicipio() }
            }
            .handleStreamErrors(OperationType.LIST, path)
    }

    fun observeVeredas(): Flow<List<Vereda>> {
        val path = "veredas"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toVereda() }
            }
            .handleStreamErrors(OperationType.LIST, path)
    }

    fun observePublicGestionPropuestas(): Flow<List<GestionPropuesta>> {
        val path = "gestion_propuestas"
        return db.collection(path)
            .whereEqualTo("visible_publico", true)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toGestionPropuesta() }
            }
            .handleStreamErrors(OperationType.LIST, path)
    }

    // --- STREAMS PRIVADOS (MODO CAMPAÑA - SOLO CON SESIÓN ACTIVA) ---

    suspend fun fetchUsuarioCampana(uid: String): Result<UsuarioCampana?> {
        val path = "usuarios_campana/$uid"
        return try {
            val doc = db.collection("usuarios_campana").document(uid).get().awaitTask()
            val user = auth.currentUser
            if (!doc.exists()) {
                if (isBootstrappedAdmin() && user != null) {
                    Result.success(
                        UsuarioCampana(
                            uid = user.uid,
                            nombre = user.displayName ?: "Admin Campaña",
                            email = user.email ?: "",
                            rol = RolCampana.ADMIN,
                            activo = true
                        )
                    )
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(doc.toUsuarioCampana())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    fun observeAllGestionPropuestas(): Flow<List<GestionPropuesta>> {
        val path = "gestion_propuestas"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toGestionPropuesta() }
            }
            .handleStreamErrors(OperationType.LIST, path, requireAuth = true)
    }

    fun observeSimpatizantes(uid: String, isAdminUser: Boolean): Flow<List<Simpatizante>> {
        val path = "simpatizantes"
        val query = if (isAdminUser && isBootstrappedAdmin()) {
            db.collection(path)
        } else {
            db.collection(path).whereArrayContains("authorized_uids", uid)
        }
        return query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toSimpatizante() }
            }
            .handleStreamErrors(OperationType.LIST, path, requireAuth = true)
    }

    fun observeReportesCiudadanos(uid: String, isAdminUser: Boolean): Flow<List<ReporteCiudadano>> {
        val path = "reportes_ciudadanos"
        val query = if (isAdminUser && isBootstrappedAdmin()) {
            db.collection(path)
        } else {
            db.collection(path).whereArrayContains("authorized_uids", uid)
        }
        return query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toReporteCiudadano() }
            }
            .handleStreamErrors(OperationType.LIST, path, requireAuth = true)
    }

    fun observeUsuariosCampana(isAdminUser: Boolean, uid: String): Flow<List<UsuarioCampana>> {
        val path = "usuarios_campana"
        val query = if (isAdminUser && isBootstrappedAdmin()) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("uid", uid)
        }
        return query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc -> doc.toUsuarioCampana() }
            }
            .handleStreamErrors(OperationType.LIST, path, requireAuth = true)
    }

    // --- MUTACIONES CON RESULT<UNIT> ---

    suspend fun submitReporteCiudadano(
        municipioId: String,
        veredaId: String,
        veredaNombre: String,
        categoria: String,
        descripcion: String,
        telefonoOpcional: String?,
        authorizedUids: List<String>
    ): Result<Unit> {
        val docId = "rep_${UUID.randomUUID().toString().take(12)}"
        val path = "reportes_ciudadanos/$docId"
        return try {
            require(municipioId.matches(Regex("^85[0-9]{3}$"))) { "Municipio inválido" }
            require(veredaId.isNotBlank() && veredaNombre.isNotBlank()) { "Selecciona una vereda o barrio" }
            require(descripcion.trim().length in 5..1000) { "La descripción debe tener entre 5 y 1000 caracteres" }

            val cleanPhone = telefonoOpcional?.trim()?.takeIf { it.isNotEmpty() }?.take(20)
            val payload = mutableMapOf<String, Any?>(
                "municipio_id" to municipioId,
                "vereda_id" to veredaId.take(128),
                "vereda_nombre" to veredaNombre.take(100),
                "categoria" to categoria.take(60),
                "descripcion" to descripcion.trim().take(1000),
                "telefono_opcional" to cleanPhone,
                "estado" to EstadoReporte.PENDIENTE.name,
                "authorized_uids" to authorizedUids.distinct().take(10),
                "created_at" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            db.collection("reportes_ciudadanos").document(docId).set(payload).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun registerSimpatizante(
        nombre: String,
        cedula: String,
        telefono: String,
        municipioId: String,
        veredaId: String,
        puestoVotacion: String,
        mesa: String,
        liderNombre: String,
        authorizedUids: List<String>,
        aceptoHabeasData: Boolean
    ): Result<Unit> {
        val docId = "simp_${UUID.randomUUID().toString().take(12)}"
        val path = "simpatizantes/$docId"
        return try {
            val uid = requireUserId()
            require(aceptoHabeasData) { "Es obligatorio aceptar la autorización de Habeas Data (Ley 1581 de 2012)." }
            require(nombre.trim().length in 2..100) { "Nombre inválido (2 a 100 caracteres)" }
            require(cedula.trim().length in 4..20) { "Cédula inválida (4 a 20 dígitos)" }
            require(telefono.trim().length in 7..20) { "Teléfono inválido (7 a 20 dígitos)" }
            require(municipioId.matches(Regex("^85[0-9]{3}$"))) { "Municipio inválido" }

            val authList = (listOf(uid) + authorizedUids).distinct().take(10)
            val payload = mapOf(
                "nombre" to nombre.trim().take(100),
                "cedula" to cedula.trim().take(20),
                "telefono" to telefono.trim().take(20),
                "municipio_id" to municipioId,
                "vereda_id" to veredaId.take(128),
                "puesto_votacion" to puestoVotacion.trim().take(140),
                "mesa" to mesa.trim().take(20),
                "lider_uid" to uid,
                "lider_nombre" to liderNombre.trim().take(100),
                "authorized_uids" to authList,
                "acepto_habeas_data" to true,
                "fecha_habeas_data" to FieldValue.serverTimestamp(),
                "created_at" to FieldValue.serverTimestamp()
            )
            db.collection("simpatizantes").document(docId).set(payload).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveGestionPropuesta(
        existingId: String?,
        tipo: TipoContenido,
        municipioId: String,
        categoria: String,
        titulo: String,
        descripcion: String,
        imagenUrl: String,
        fechaTexto: String,
        visiblePublico: Boolean
    ): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "gp_${UUID.randomUUID().toString().take(10)}"
        val path = "gestion_propuestas/$docId"
        return try {
            requireUserId()
            require(titulo.trim().length in 3..140) { "Título debe tener entre 3 y 140 caracteres" }
            require(descripcion.trim().length in 5..1200) { "Descripción debe tener entre 5 y 1200 caracteres" }

            if (existingId.isNullOrBlank()) {
                val payload = mapOf(
                    "tipo" to tipo.name,
                    "municipio_id" to municipioId,
                    "categoria" to categoria.take(60),
                    "titulo" to titulo.trim().take(140),
                    "descripcion" to descripcion.trim().take(1200),
                    "imagen_url" to imagenUrl.trim().take(500),
                    "fecha_texto" to fechaTexto.trim().take(40),
                    "visible_publico" to visiblePublico,
                    "created_at" to FieldValue.serverTimestamp(),
                    "updated_at" to FieldValue.serverTimestamp()
                )
                db.collection("gestion_propuestas").document(docId).set(payload).awaitTask()
            } else {
                val updates = mapOf(
                    "tipo" to tipo.name,
                    "municipio_id" to municipioId,
                    "categoria" to categoria.take(60),
                    "titulo" to titulo.trim().take(140),
                    "descripcion" to descripcion.trim().take(1200),
                    "imagen_url" to imagenUrl.trim().take(500),
                    "fecha_texto" to fechaTexto.trim().take(40),
                    "visible_publico" to visiblePublico,
                    "updated_at" to FieldValue.serverTimestamp()
                )
                db.collection("gestion_propuestas").document(docId).update(updates).awaitTask()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteGestionPropuesta(itemId: String): Result<Unit> {
        val path = "gestion_propuestas/$itemId"
        return try {
            requireUserId()
            db.collection("gestion_propuestas").document(itemId).delete().awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveConfiguracion(config: ConfiguracionCampana): Result<Unit> {
        val path = "configuracion_campana/general"
        return try {
            requireUserId()
            require(config.nombreCandidato.trim().length in 2..100) { "Nombre de candidato inválido" }
            require(config.eslogan.trim().length in 2..160) { "Eslogan inválido" }
            val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")
            require(config.colorPrimario.matches(hexRegex)) { "Color primario debe ser formato #RRGGBB" }
            require(config.colorSecundario.matches(hexRegex)) { "Color secundario debe ser formato #RRGGBB" }

            val payload = mapOf(
                "nombre_candidato" to config.nombreCandidato.trim().take(100),
                "eslogan" to config.eslogan.trim().take(160),
                "color_primario" to config.colorPrimario.trim(),
                "color_secundario" to config.colorSecundario.trim(),
                "logo_url" to config.logoUrl.trim().take(500),
                "updated_at" to FieldValue.serverTimestamp()
            )
            db.collection("configuracion_campana").document("general").set(payload).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateReporteEstado(reporteId: String, nuevoEstado: EstadoReporte): Result<Unit> {
        val path = "reportes_ciudadanos/$reporteId"
        return try {
            requireUserId()
            db.collection("reportes_ciudadanos").document(reporteId)
                .update(mapOf("estado" to nuevoEstado.name))
                .awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveUsuarioCampana(usuario: UsuarioCampana): Result<Unit> {
        val path = "usuarios_campana/${usuario.uid}"
        return try {
            requireUserId()
            require(usuario.uid.trim().matches(Regex("^[a-zA-Z0-9_\\-]+$"))) { "UID inválido" }
            require(usuario.nombre.trim().length in 2..100) { "Nombre inválido" }

            val payload = mapOf(
                "uid" to usuario.uid.trim(),
                "nombre" to usuario.nombre.trim().take(100),
                "email" to usuario.email.trim().take(120),
                "cedula" to usuario.cedula.trim().take(20),
                "telefono" to usuario.telefono.trim().take(20),
                "rol" to usuario.rol.name,
                "municipio_id" to usuario.municipioId.trim().take(10),
                "vereda_id" to usuario.veredaId.trim().take(128),
                "activo" to usuario.activo,
                "created_at" to (usuario.createdAt ?: FieldValue.serverTimestamp())
            )
            db.collection("usuarios_campana").document(usuario.uid.trim()).set(payload).awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun seedInitialCatalog(): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val cfgRef = db.collection("configuracion_campana").document("general")
            batch.set(
                cfgRef,
                mapOf(
                    "nombre_candidato" to CasanareSeedData.defaultConfig.nombreCandidato,
                    "eslogan" to CasanareSeedData.defaultConfig.eslogan,
                    "color_primario" to CasanareSeedData.defaultConfig.colorPrimario,
                    "color_secundario" to CasanareSeedData.defaultConfig.colorSecundario,
                    "logo_url" to "",
                    "updated_at" to FieldValue.serverTimestamp()
                )
            )
            CasanareSeedData.municipios.forEach { muni ->
                val ref = db.collection("municipios").document(muni.codigoDane)
                batch.set(
                    ref,
                    mapOf(
                        "codigo_dane" to muni.codigoDane,
                        "nombre" to muni.nombre,
                        "subregion" to muni.subregion,
                        "meta_votos" to muni.metaVotos,
                        "nivel_energia" to muni.nivelEnergia,
                        "resumen_publico" to muni.resumenPublico,
                        "lat" to muni.lat,
                        "lon" to muni.lon,
                        "coord_uids" to muni.coordUids
                    )
                )
            }
            CasanareSeedData.veredas.forEach { ver ->
                val ref = db.collection("veredas").document(ver.id)
                batch.set(
                    ref,
                    mapOf(
                        "municipio_id" to ver.municipioId,
                        "nombre" to ver.nombre,
                        "zona" to ver.zona.name,
                        "meta_votos" to ver.metaVotos,
                        "nivel_energia" to ver.nivelEnergia,
                        "lider_uid" to ver.liderUid,
                        "lider_nombre" to ver.liderNombre,
                        "puesto_votacion" to ver.puestoVotacion,
                        "lat" to ver.lat,
                        "lon" to ver.lon,
                        "geojson" to ver.geojson
                    )
                )
            }
            CasanareSeedData.gestionPropuestas.forEach { gp ->
                val ref = db.collection("gestion_propuestas").document(gp.id)
                batch.set(
                    ref,
                    mapOf(
                        "tipo" to gp.tipo.name,
                        "municipio_id" to gp.municipioId,
                        "categoria" to gp.categoria,
                        "titulo" to gp.titulo,
                        "descripcion" to gp.descripcion,
                        "imagen_url" to gp.imagenUrl,
                        "fecha_texto" to gp.fechaTexto,
                        "visible_publico" to gp.visiblePublico,
                        "created_at" to FieldValue.serverTimestamp(),
                        "updated_at" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().awaitTask()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed_initial_catalog")
            Result.failure(e)
        }
    }

    // --- SAFE SNAPSHOT MAPPERS ---

    private fun parseStringList(raw: Any?): List<String> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { it as? String }
    }

    private fun DocumentSnapshot.toMunicipio(): Municipio? {
        val codigo = getString("codigo_dane") ?: id
        val nombre = getString("nombre") ?: return null
        return Municipio(
            codigoDane = codigo,
            nombre = nombre,
            subregion = getString("subregion") ?: "Casanare",
            metaVotos = (getLong("meta_votos") ?: 0L).toInt(),
            nivelEnergia = (getLong("nivel_energia") ?: 50L).toInt(),
            resumenPublico = getString("resumen_publico") ?: "",
            lat = getDouble("lat") ?: 5.3,
            lon = getDouble("lon") ?: -72.3,
            coordUids = parseStringList(get("coord_uids"))
        )
    }

    private fun DocumentSnapshot.toVereda(): Vereda? {
        val muniId = getString("municipio_id") ?: return null
        val nombre = getString("nombre") ?: return null
        return Vereda(
            id = id,
            municipioId = muniId,
            nombre = nombre,
            zona = ZonaVereda.fromRaw(getString("zona")),
            metaVotos = (getLong("meta_votos") ?: 0L).toInt(),
            nivelEnergia = (getLong("nivel_energia") ?: 50L).toInt(),
            liderUid = getString("lider_uid") ?: "",
            liderNombre = getString("lider_nombre") ?: "",
            puestoVotacion = getString("puesto_votacion") ?: "",
            lat = getDouble("lat") ?: 5.3,
            lon = getDouble("lon") ?: -72.3,
            geojson = getString("geojson") ?: ""
        )
    }

    private fun DocumentSnapshot.toUsuarioCampana(): UsuarioCampana {
        return UsuarioCampana(
            uid = getString("uid") ?: id,
            nombre = getString("nombre") ?: "Equipo Campaña",
            email = getString("email") ?: "",
            cedula = getString("cedula") ?: "",
            telefono = getString("telefono") ?: "",
            rol = RolCampana.fromRaw(getString("rol")),
            municipioId = getString("municipio_id") ?: "",
            veredaId = getString("vereda_id") ?: "",
            activo = getBoolean("activo") ?: true,
            createdAt = getTimestamp("created_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    private fun DocumentSnapshot.toSimpatizante(): Simpatizante? {
        val nombre = getString("nombre") ?: return null
        return Simpatizante(
            id = id,
            nombre = nombre,
            cedula = getString("cedula") ?: "",
            telefono = getString("telefono") ?: "",
            municipioId = getString("municipio_id") ?: "",
            veredaId = getString("vereda_id") ?: "",
            puestoVotacion = getString("puesto_votacion") ?: "",
            mesa = getString("mesa") ?: "",
            liderUid = getString("lider_uid") ?: "",
            liderNombre = getString("lider_nombre") ?: "",
            authorizedUids = parseStringList(get("authorized_uids")),
            aceptoHabeasData = getBoolean("acepto_habeas_data") ?: false,
            fechaHabeasData = getTimestamp("fecha_habeas_data", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            createdAt = getTimestamp("created_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    private fun DocumentSnapshot.toGestionPropuesta(): GestionPropuesta? {
        val titulo = getString("titulo") ?: return null
        return GestionPropuesta(
            id = id,
            tipo = TipoContenido.fromRaw(getString("tipo")),
            municipioId = getString("municipio_id") ?: "85000",
            categoria = getString("categoria") ?: "Agro y Ganadería",
            titulo = titulo,
            descripcion = getString("descripcion") ?: "",
            imagenUrl = getString("imagen_url") ?: "",
            fechaTexto = getString("fecha_texto") ?: "",
            visiblePublico = getBoolean("visible_publico") ?: true,
            createdAt = getTimestamp("created_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            updatedAt = getTimestamp("updated_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    private fun DocumentSnapshot.toReporteCiudadano(): ReporteCiudadano? {
        val desc = getString("descripcion") ?: return null
        return ReporteCiudadano(
            id = id,
            municipioId = getString("municipio_id") ?: "",
            veredaId = getString("vereda_id") ?: "",
            veredaNombre = getString("vereda_nombre") ?: "",
            categoria = getString("categoria") ?: "",
            descripcion = desc,
            telefonoOpcional = getString("telefono_opcional"),
            estado = EstadoReporte.fromRaw(getString("estado")),
            authorizedUids = parseStringList(get("authorized_uids")),
            createdAt = getTimestamp("created_at", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }
}
