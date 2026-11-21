package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthManager
import com.example.data.CasanareRepository
import com.example.data.CasanareSeedData
import com.example.model.AppSessionMode
import com.example.model.CampaignConstants
import com.example.model.CapaMapa
import com.example.model.ConfiguracionCampana
import com.example.model.EstadoReporte
import com.example.model.FiltroVereda
import com.example.model.GestionPropuesta
import com.example.model.Municipio
import com.example.model.ReporteCiudadano
import com.example.model.RolCampana
import com.example.model.Simpatizante
import com.example.model.TipoContenido
import com.example.model.UsuarioCampana
import com.example.model.Vereda
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CampaignKpis(
    val totalLideresActivos: Int = 0,
    val totalSimpatizantes: Int = 0,
    val metaDepartamental: Int = 1,
    val porcentajeMeta: Int = 0,
    val municipiosEnAlerta: Int = 0
)

data class CasanareUiState(
    val sessionMode: AppSessionMode = AppSessionMode.Ciudadano,
    val firebaseUser: FirebaseUser? = null,
    val authDeniedReason: String? = null,
    val configuracion: ConfiguracionCampana = CasanareSeedData.defaultConfig,
    val municipios: List<Municipio> = CasanareSeedData.municipios,
    val veredas: List<Vereda> = CasanareSeedData.veredas,
    val gestionPropuestas: List<GestionPropuesta> = CasanareSeedData.gestionPropuestas,
    val simpatizantes: List<Simpatizante> = emptyList(),
    val reportes: List<ReporteCiudadano> = emptyList(),
    val usuariosCampana: List<UsuarioCampana> = emptyList(),
    val selectedMunicipioId: String? = null,
    val hoveredMunicipioId: String? = null,
    val selectedVeredaId: String? = null,
    val activeLayer: CapaMapa = CapaMapa.CALOR_ELECTORAL,
    val showLeaderPins: Boolean = true,
    val inspectedLeaderVeredaId: String? = null,
    val veredaFilter: FiltroVereda = FiltroVereda.TODAS,
    val selectedEjeFilter: String? = null,
    val isAuthDialogVisible: Boolean = false,
    val isDeptProposalsVisible: Boolean = false,
    val isCmsSheetVisible: Boolean = false,
    val isLoadingAuth: Boolean = false,
    val isBusy: Boolean = false,
    val snackbarMessage: String? = null
) {
    val selectedMunicipio: Municipio?
        get() = municipios.firstOrNull { it.codigoDane == selectedMunicipioId }

    val selectedVereda: Vereda?
        get() = veredas.firstOrNull { it.id == selectedVeredaId }

    val inspectedLeaderVereda: Vereda?
        get() = veredas.firstOrNull { it.id == inspectedLeaderVeredaId }

    val isCampaignMode: Boolean
        get() = sessionMode !is AppSessionMode.Ciudadano

    val activeCampaignUser: UsuarioCampana?
        get() = when (sessionMode) {
            is AppSessionMode.Ciudadano -> null
            is AppSessionMode.CampanaFirebase -> sessionMode.perfil
            is AppSessionMode.CampanaDemoLocal -> sessionMode.perfil
        }

    val isDemoLocal: Boolean
        get() = sessionMode is AppSessionMode.CampanaDemoLocal

    val canOpenCms: Boolean
        get() = activeCampaignUser?.rol == RolCampana.ADMIN

    fun simpatizantesCountForMunicipio(codigoDane: String): Int {
        val real = simpatizantes.count { it.municipioId == codigoDane }
        return if (isDemoLocal && real == 0) 0 else real
    }

    fun simpatizantesCountForVereda(veredaId: String): Int =
        simpatizantes.count { it.veredaId == veredaId }

    fun activeLideresCountForMunicipio(codigoDane: String): Int =
        veredas.count { it.municipioId == codigoDane && it.liderUid.isNotBlank() }

    val kpis: CampaignKpis
        get() {
            val lideres = veredas.count { it.liderUid.isNotBlank() }
            val totalSimp = simpatizantes.size
            val totalMeta = municipios.sumOf { it.metaVotos }.coerceAtLeast(1)
            val pct = ((totalSimp * 100f) / totalMeta).toInt().coerceIn(0, 999)
            val alertas = municipios.count { it.nivelEnergia < 45 }
            return CampaignKpis(
                totalLideresActivos = lideres,
                totalSimpatizantes = totalSimp,
                metaDepartamental = totalMeta,
                porcentajeMeta = pct,
                municipiosEnAlerta = alertas
            )
        }
}

class CasanareViewModel(
    private val repository: CasanareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasanareUiState())
    val uiState: StateFlow<CasanareUiState> = _uiState.asStateFlow()

    private var campaignJobs: List<Job> = emptyList()

    // Respaldo en memoria para Demo Local (mutable sin tocar Firestore real)
    private var demoSimpatizantes = CasanareSeedData.demoSimpatizantes.toMutableList()
    private var demoReportes = CasanareSeedData.demoReportes.toMutableList()
    private var demoGestion = CasanareSeedData.gestionPropuestas.toMutableList()
    private var demoConfig = CasanareSeedData.defaultConfig
    private var demoUsuarios = CasanareSeedData.demoUsuarios.toMutableList()

    init {
        observePublicStreams()
        observeAuthChanges()
    }

    private fun observePublicStreams() {
        viewModelScope.launch {
            repository.observeConfiguracion()
                .catch { /* Mantener configuración por defecto si aún no existe en nube */ }
                .collect { cfg ->
                    if (cfg != null && !_uiState.value.isDemoLocal) {
                        _uiState.update { it.copy(configuracion = cfg) }
                    }
                }
        }
        viewModelScope.launch {
            repository.observeMunicipios()
                .catch { /* Mantener catálogo base local si Firestore falla u offline sin caché */ }
                .collect { list ->
                    if (list.isNotEmpty()) {
                        _uiState.update { it.copy(municipios = list) }
                    }
                }
        }
        viewModelScope.launch {
            repository.observeVeredas()
                .catch { /* Mantener veredas base local */ }
                .collect { list ->
                    if (list.isNotEmpty()) {
                        _uiState.update { it.copy(veredas = list) }
                    }
                }
        }
        viewModelScope.launch {
            repository.observePublicGestionPropuestas()
                .catch { /* Mantener gestión base local */ }
                .collect { list ->
                    if (list.isNotEmpty() && _uiState.value.sessionMode is AppSessionMode.Ciudadano) {
                        _uiState.update { it.copy(gestionPropuestas = list) }
                    }
                }
        }
    }

    private fun observeAuthChanges() {
        viewModelScope.launch {
            AuthManager.authStateFlow(Firebase.auth).collect { user ->
                _uiState.update { it.copy(firebaseUser = user, authDeniedReason = null) }
                if (user == null) {
                    stopCampaignListeners()
                    if (_uiState.value.sessionMode is AppSessionMode.CampanaFirebase) {
                        _uiState.update {
                            it.copy(
                                sessionMode = AppSessionMode.Ciudadano,
                                simpatizantes = emptyList(),
                                reportes = emptyList(),
                                usuariosCampana = emptyList(),
                                isCmsSheetVisible = false
                            )
                        }
                    }
                } else {
                    resolveCampaignProfile(user)
                }
            }
        }
    }

    private fun resolveCampaignProfile(user: FirebaseUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAuth = true) }
            val result = repository.fetchUsuarioCampana(user.uid)
            result.onSuccess { perfil ->
                if (perfil != null && perfil.activo) {
                    _uiState.update {
                        it.copy(
                            sessionMode = AppSessionMode.CampanaFirebase(perfil),
                            isLoadingAuth = false,
                            isAuthDialogVisible = false,
                            authDeniedReason = null,
                            snackbarMessage = "Modo Campaña activado (${perfil.rol.label})"
                        )
                    }
                    startCampaignListeners(user.uid, perfil.rol == RolCampana.ADMIN)
                } else {
                    val reason = if (perfil == null) {
                        "Tu cuenta (${user.email ?: user.uid}) aún no tiene rol asignado en usuarios_campana."
                    } else {
                        "Tu usuario de campaña se encuentra desactivado (activo = false)."
                    }
                    _uiState.update {
                        it.copy(
                            isLoadingAuth = false,
                            authDeniedReason = reason
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingAuth = false,
                        authDeniedReason = err.localizedMessage ?: "No se pudo validar tu perfil en Firestore."
                    )
                }
            }
        }
    }

    private fun startCampaignListeners(uid: String, isAdmin: Boolean) {
        stopCampaignListeners()
        val jobs = mutableListOf<Job>()

        jobs += viewModelScope.launch {
            repository.observeSimpatizantes(uid, isAdmin)
                .catch { err -> _uiState.update { it.copy(snackbarMessage = err.localizedMessage) } }
                .collect { list -> _uiState.update { it.copy(simpatizantes = list) } }
        }
        jobs += viewModelScope.launch {
            repository.observeReportesCiudadanos(uid, isAdmin)
                .catch { err -> _uiState.update { it.copy(snackbarMessage = err.localizedMessage) } }
                .collect { list -> _uiState.update { it.copy(reportes = list) } }
        }
        jobs += viewModelScope.launch {
            repository.observeUsuariosCampana(isAdmin, uid)
                .catch { /* No bloquear UI si el usuario no es admin */ }
                .collect { list -> _uiState.update { it.copy(usuariosCampana = list) } }
        }
        if (isAdmin) {
            jobs += viewModelScope.launch {
                repository.observeAllGestionPropuestas()
                    .catch { /* Fallback a público */ }
                    .collect { list ->
                        if (list.isNotEmpty()) {
                            _uiState.update { it.copy(gestionPropuestas = list) }
                        }
                    }
            }
        }
        campaignJobs = jobs
    }

    private fun stopCampaignListeners() {
        campaignJobs.forEach { it.cancel() }
        campaignJobs = emptyList()
    }

    // --- ACCIONES DE MODO DEMO LOCAL ---

    fun activateDemoLocal(rol: RolCampana = RolCampana.ADMIN) {
        stopCampaignListeners()
        val demoPerfil = demoUsuarios.firstOrNull { it.rol == rol } ?: demoUsuarios.first()
        _uiState.update {
            it.copy(
                sessionMode = AppSessionMode.CampanaDemoLocal(demoPerfil),
                configuracion = demoConfig,
                gestionPropuestas = demoGestion.toList(),
                simpatizantes = demoSimpatizantes.toList(),
                reportes = demoReportes.toList(),
                usuariosCampana = demoUsuarios.toList(),
                isAuthDialogVisible = false,
                authDeniedReason = null,
                snackbarMessage = "Modo Demo Local activo como ${rol.label}"
            )
        }
    }

    fun switchDemoRole(rol: RolCampana) {
        if (_uiState.value.isDemoLocal) {
            activateDemoLocal(rol)
        }
    }

    fun exitDemoToCitizen() {
        _uiState.update {
            it.copy(
                sessionMode = AppSessionMode.Ciudadano,
                simpatizantes = emptyList(),
                reportes = emptyList(),
                usuariosCampana = emptyList(),
                isCmsSheetVisible = false,
                snackbarMessage = "Has regresado a Vista Ciudadana"
            )
        }
    }

    // --- NAVEGACIÓN DE MAPA Y FILTROS ---

    fun selectMunicipio(codigoDane: String?) {
        _uiState.update {
            it.copy(
                selectedMunicipioId = codigoDane,
                selectedVeredaId = null,
                hoveredMunicipioId = null
            )
        }
    }

    fun hoverMunicipio(codigoDane: String?) {
        _uiState.update { it.copy(hoveredMunicipioId = codigoDane) }
    }

    fun selectVereda(veredaId: String?) {
        _uiState.update { it.copy(selectedVeredaId = veredaId) }
    }

    fun setActiveLayer(capa: CapaMapa) {
        _uiState.update { it.copy(activeLayer = capa) }
    }

    fun toggleLeaderPins() {
        _uiState.update { it.copy(showLeaderPins = !it.showLeaderPins) }
    }

    fun inspectLeaderPin(veredaId: String?) {
        _uiState.update { it.copy(inspectedLeaderVeredaId = veredaId) }
    }

    fun setVeredaFilter(filtro: FiltroVereda) {
        _uiState.update { it.copy(veredaFilter = filtro) }
    }

    fun setEjeFilter(eje: String?) {
        _uiState.update { it.copy(selectedEjeFilter = eje) }
    }

    fun setAuthDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isAuthDialogVisible = visible) }
    }

    fun setDeptProposalsVisible(visible: Boolean) {
        _uiState.update { it.copy(isDeptProposalsVisible = visible) }
    }

    fun setCmsSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isCmsSheetVisible = visible) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    // --- OPERACIONES DE DATOS (CIUDADANO + CAMPAÑA) ---

    fun submitReporteCiudadano(
        municipioId: String,
        veredaId: String,
        veredaNombre: String,
        categoria: String,
        descripcion: String,
        telefonoOpcional: String?,
        onDone: () -> Unit = {}
    ) {
        if (descripcion.trim().length < 5) {
            showMessage("Por favor describe la situación en al menos 5 caracteres.")
            return
        }
        val state = _uiState.value
        if (state.isDemoLocal) {
            val nuevo = ReporteCiudadano(
                id = "rep_demo_${UUID.randomUUID().toString().take(6)}",
                municipioId = municipioId,
                veredaId = veredaId,
                veredaNombre = veredaNombre,
                categoria = categoria,
                descripcion = descripcion.trim(),
                telefonoOpcional = telefonoOpcional?.trim()?.takeIf { it.isNotEmpty() },
                estado = EstadoReporte.PENDIENTE,
                createdAt = Timestamp.now()
            )
            demoReportes = (listOf(nuevo) + demoReportes).toMutableList()
            _uiState.update {
                it.copy(
                    reportes = demoReportes.toList(),
                    snackbarMessage = "¡Gracias! Reporte enviado (Demo Local)."
                )
            }
            onDone()
            return
        }

        val muni = state.municipios.firstOrNull { it.codigoDane == municipioId }
        val ver = state.veredas.firstOrNull { it.id == veredaId }
        val authList = buildList {
            if (!ver?.liderUid.isNullOrBlank() && !ver!!.liderUid.startsWith("demo_")) add(ver.liderUid)
            muni?.coordUids?.filter { !it.startsWith("demo_") }?.let { addAll(it) }
        }.distinct().take(10)

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val res = repository.submitReporteCiudadano(
                municipioId = municipioId,
                veredaId = veredaId,
                veredaNombre = veredaNombre,
                categoria = categoria,
                descripcion = descripcion,
                telefonoOpcional = telefonoOpcional,
                authorizedUids = authList
            )
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        snackbarMessage = "¡Gracias! Tu reporte veredal fue guardado en la nube."
                    )
                }
                onDone()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        snackbarMessage = err.localizedMessage ?: "No se pudo enviar el reporte."
                    )
                }
            }
        }
    }

    fun registerSimpatizante(
        nombre: String,
        cedula: String,
        telefono: String,
        municipioId: String,
        veredaId: String,
        puestoVotacion: String,
        mesa: String,
        aceptoHabeasData: Boolean,
        onDone: () -> Unit = {}
    ) {
        val state = _uiState.value
        val activeUser = state.activeCampaignUser ?: run {
            showMessage("Inicia sesión en Modo Campaña para registrar simpatizantes.")
            return
        }
        if (!aceptoHabeasData) {
            showMessage("Debe marcar la autorización de Habeas Data (Ley 1581 de 2012).")
            return
        }
        if (nombre.trim().length < 2 || cedula.trim().length < 4 || telefono.trim().length < 7) {
            showMessage("Verifica nombre, cédula (mín. 4) y teléfono (mín. 7).")
            return
        }

        if (state.isDemoLocal) {
            val nuevo = Simpatizante(
                id = "ds_${UUID.randomUUID().toString().take(6)}",
                nombre = nombre.trim(),
                cedula = cedula.trim(),
                telefono = telefono.trim(),
                municipioId = municipioId,
                veredaId = veredaId,
                puestoVotacion = puestoVotacion.trim(),
                mesa = mesa.trim(),
                liderUid = activeUser.uid,
                liderNombre = activeUser.nombre,
                authorizedUids = listOf(activeUser.uid),
                aceptoHabeasData = true,
                fechaHabeasData = Timestamp.now(),
                createdAt = Timestamp.now()
            )
            demoSimpatizantes = (listOf(nuevo) + demoSimpatizantes).toMutableList()
            _uiState.update {
                it.copy(
                    simpatizantes = demoSimpatizantes.toList(),
                    snackbarMessage = "Simpatizante registrado con Habeas Data (Demo Local)."
                )
            }
            onDone()
            return
        }

        val muni = state.municipios.firstOrNull { it.codigoDane == municipioId }
        val authList = (listOf(activeUser.uid) + (muni?.coordUids.orEmpty())).distinct().take(10)

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val res = repository.registerSimpatizante(
                nombre = nombre,
                cedula = cedula,
                telefono = telefono,
                municipioId = municipioId,
                veredaId = veredaId,
                puestoVotacion = puestoVotacion,
                mesa = mesa,
                liderNombre = activeUser.nombre,
                authorizedUids = authList,
                aceptoHabeasData = aceptoHabeasData
            )
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        snackbarMessage = "Simpatizante registrado con Habeas Data en Firestore."
                    )
                }
                onDone()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        snackbarMessage = err.localizedMessage ?: "Error al guardar simpatizante."
                    )
                }
            }
        }
    }

    fun updateReporteEstado(reporteId: String, estado: EstadoReporte) {
        val state = _uiState.value
        if (state.isDemoLocal) {
            demoReportes = demoReportes.map {
                if (it.id == reporteId) it.copy(estado = estado) else it
            }.toMutableList()
            _uiState.update {
                it.copy(
                    reportes = demoReportes.toList(),
                    snackbarMessage = "Estado actualizado a ${estado.label}"
                )
            }
            return
        }
        viewModelScope.launch {
            repository.updateReporteEstado(reporteId, estado)
                .onSuccess { showMessage("Estado actualizado a ${estado.label}") }
                .onFailure { err -> showMessage(err.localizedMessage ?: "Error al actualizar reporte") }
        }
    }

    fun saveGestionPropuesta(
        existingId: String?,
        tipo: TipoContenido,
        municipioId: String,
        categoria: String,
        titulo: String,
        descripcion: String,
        imagenUrl: String,
        fechaTexto: String,
        visiblePublico: Boolean,
        onDone: () -> Unit = {}
    ) {
        val state = _uiState.value
        if (state.isDemoLocal) {
            val id = existingId?.takeIf { it.isNotBlank() } ?: "gp_demo_${UUID.randomUUID().toString().take(6)}"
            val item = GestionPropuesta(
                id = id,
                tipo = tipo,
                municipioId = municipioId,
                categoria = categoria,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                imagenUrl = imagenUrl.trim(),
                fechaTexto = fechaTexto.trim(),
                visiblePublico = visiblePublico,
                updatedAt = Timestamp.now()
            )
            demoGestion = if (demoGestion.any { it.id == id }) {
                demoGestion.map { if (it.id == id) item else it }.toMutableList()
            } else {
                (listOf(item) + demoGestion).toMutableList()
            }
            _uiState.update {
                it.copy(
                    gestionPropuestas = demoGestion.toList(),
                    snackbarMessage = "Contenido CMS guardado (Demo Local)."
                )
            }
            onDone()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            repository.saveGestionPropuesta(
                existingId, tipo, municipioId, categoria, titulo, descripcion, imagenUrl, fechaTexto, visiblePublico
            ).onSuccess {
                _uiState.update {
                    it.copy(isBusy = false, snackbarMessage = "Contenido publicado en Firestore.")
                }
                onDone()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isBusy = false, snackbarMessage = err.localizedMessage ?: "Error al guardar contenido.")
                }
            }
        }
    }

    fun deleteGestionPropuesta(itemId: String) {
        if (_uiState.value.isDemoLocal) {
            demoGestion.removeAll { it.id == itemId }
            _uiState.update {
                it.copy(gestionPropuestas = demoGestion.toList(), snackbarMessage = "Elemento eliminado (Demo Local).")
            }
            return
        }
        viewModelScope.launch {
            repository.deleteGestionPropuesta(itemId)
                .onSuccess { showMessage("Elemento eliminado.") }
                .onFailure { err -> showMessage(err.localizedMessage ?: "Error al eliminar.") }
        }
    }

    fun saveConfiguracion(config: ConfiguracionCampana, onDone: () -> Unit = {}) {
        if (_uiState.value.isDemoLocal) {
            demoConfig = config
            _uiState.update {
                it.copy(configuracion = config, snackbarMessage = "Configuración actualizada (Demo Local).")
            }
            onDone()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            repository.saveConfiguracion(config)
                .onSuccess {
                    _uiState.update { it.copy(isBusy = false, snackbarMessage = "Configuración de campaña guardada.") }
                    onDone()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isBusy = false, snackbarMessage = err.localizedMessage ?: "Error al guardar.") }
                }
        }
    }

    fun saveUsuarioCampana(usuario: UsuarioCampana, onDone: () -> Unit = {}) {
        if (_uiState.value.isDemoLocal) {
            demoUsuarios = if (demoUsuarios.any { it.uid == usuario.uid }) {
                demoUsuarios.map { if (it.uid == usuario.uid) usuario else it }.toMutableList()
            } else {
                (listOf(usuario) + demoUsuarios).toMutableList()
            }
            _uiState.update {
                it.copy(usuariosCampana = demoUsuarios.toList(), snackbarMessage = "Usuario guardado (Demo Local).")
            }
            onDone()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            repository.saveUsuarioCampana(usuario)
                .onSuccess {
                    _uiState.update { it.copy(isBusy = false, snackbarMessage = "Usuario de campaña guardado.") }
                    onDone()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isBusy = false, snackbarMessage = err.localizedMessage ?: "Error al guardar usuario.") }
                }
        }
    }

    fun seedInitialCatalog() {
        if (_uiState.value.isDemoLocal) {
            showMessage("En Modo Demo Local los 19 municipios y veredas ya están precargados.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            repository.seedInitialCatalog()
                .onSuccess {
                    _uiState.update {
                        it.copy(isBusy = false, snackbarMessage = "Catálogo Casanare (19 municipios y veredas) sincronizado en Firestore.")
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isBusy = false, snackbarMessage = err.localizedMessage ?: "Error al sincronizar catálogo.")
                    }
                }
        }
    }
}
