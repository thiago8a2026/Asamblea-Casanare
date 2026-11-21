package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.AuthManager
import com.example.data.CasanareRepository
import com.example.ui.components.AdminCmsContent
import com.example.ui.components.AuthTeamDialog
import com.example.ui.components.CampaignKpiAndLayerBar
import com.example.ui.components.CampaignVeredaDetailContent
import com.example.ui.components.CasanareGeoMap
import com.example.ui.components.CitizenDepartmentBottomCard
import com.example.ui.components.CitizenMunicipioContent
import com.example.ui.components.DepartamentalProposalsContent
import com.example.ui.components.EmptyCivicState
import com.example.ui.components.LeaderPinFichaDialog
import com.example.ui.components.TopCampaignBar
import com.example.ui.theme.CasanareTheme
import com.example.ui.viewmodel.CasanareUiState
import com.example.ui.viewmodel.CasanareViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CasanareViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        CasanareViewModel(CasanareRepository(app))
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CasanareTheme(
                primaryHex = uiState.configuracion.colorPrimario,
                secondaryHex = uiState.configuracion.colorSecundario
            ) {
                CasanareAppShell(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CasanareAppShell(
    uiState: CasanareUiState,
    viewModel: CasanareViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showCitizenMuniSheet by remember(uiState.selectedMunicipioId, uiState.isCampaignMode) {
        mutableStateOf(uiState.selectedMunicipioId != null && !uiState.isCampaignMode)
    }

    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    // BackHandler para volver de Nivel 2 (Municipal/Veredal) a Nivel 1 (Casanare)
    BackHandler(enabled = uiState.selectedMunicipioId != null || uiState.selectedVeredaId != null) {
        if (uiState.selectedVeredaId != null) {
            viewModel.selectVereda(null)
        } else {
            viewModel.selectMunicipio(null)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopCampaignBar(
                    config = uiState.configuracion,
                    sessionMode = uiState.sessionMode,
                    municipios = uiState.municipios,
                    selectedMunicipio = uiState.selectedMunicipio,
                    onSelectMunicipio = { code ->
                        viewModel.selectMunicipio(code)
                        if (!uiState.isCampaignMode) showCitizenMuniSheet = true
                    },
                    onOpenAuthDialog = { viewModel.setAuthDialogVisible(true) },
                    onSignOut = {
                        if (uiState.isDemoLocal) {
                            viewModel.exitDemoToCitizen()
                        } else {
                            AuthManager.signOut(credentialManager, scope)
                        }
                    },
                    onOpenCms = { viewModel.setCmsSheetVisible(true) },
                    onSwitchDemoRole = { rol -> viewModel.switchDemoRole(rol) }
                )

                if (uiState.isCampaignMode) {
                    CampaignKpiAndLayerBar(
                        kpis = uiState.kpis,
                        activeLayer = uiState.activeLayer,
                        showLeaderPins = uiState.showLeaderPins,
                        isMunicipioLevel = uiState.selectedMunicipio != null,
                        veredaFilter = uiState.veredaFilter,
                        onSelectLayer = viewModel::setActiveLayer,
                        onToggleLeaderPins = viewModel::toggleLeaderPins,
                        onSelectVeredaFilter = viewModel::setVeredaFilter
                    )
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpandedLayout = maxWidth >= 720.dp

            if (isExpandedLayout) {
                // Diseño Adaptativo Tablet / Plegable (Mapa + Panel Lateral)
                Row(modifier = Modifier.fillMaxSize()) {
                    CasanareGeoMap(
                        municipios = uiState.municipios,
                        veredas = uiState.veredas,
                        selectedMunicipio = uiState.selectedMunicipio,
                        selectedVeredaId = uiState.selectedVeredaId,
                        isCampaignMode = uiState.isCampaignMode,
                        activeLayer = uiState.activeLayer,
                        showLeaderPins = uiState.showLeaderPins,
                        veredaFilter = uiState.veredaFilter,
                        simpatizantesCountByMunicipio = uiState::simpatizantesCountForMunicipio,
                        simpatizantesCountByVereda = uiState::simpatizantesCountForVereda,
                        lideresCountByMunicipio = uiState::activeLideresCountForMunicipio,
                        onSelectMunicipio = { code -> viewModel.selectMunicipio(code) },
                        onBackToDepartment = { viewModel.selectMunicipio(null) },
                        onSelectVereda = { vereda -> viewModel.selectVereda(vereda.id) },
                        onLeaderPinClicked = { vereda -> viewModel.inspectLeaderPin(vereda.id) },
                        onOpenMunicipioFicha = { showCitizenMuniSheet = true },
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                    )

                    Surface(
                        tonalElevation = 1.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                    ) {
                        ExpandedDetailPane(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            } else {
                // Diseño Móvil (Mapa de pantalla completa + Tarjetas / ModalBottomSheet)
                Box(modifier = Modifier.fillMaxSize()) {
                    CasanareGeoMap(
                        municipios = uiState.municipios,
                        veredas = uiState.veredas,
                        selectedMunicipio = uiState.selectedMunicipio,
                        selectedVeredaId = uiState.selectedVeredaId,
                        isCampaignMode = uiState.isCampaignMode,
                        activeLayer = uiState.activeLayer,
                        showLeaderPins = uiState.showLeaderPins,
                        veredaFilter = uiState.veredaFilter,
                        simpatizantesCountByMunicipio = uiState::simpatizantesCountForMunicipio,
                        simpatizantesCountByVereda = uiState::simpatizantesCountForVereda,
                        lideresCountByMunicipio = uiState::activeLideresCountForMunicipio,
                        onSelectMunicipio = { code ->
                            viewModel.selectMunicipio(code)
                            if (!uiState.isCampaignMode) showCitizenMuniSheet = true
                        },
                        onBackToDepartment = { viewModel.selectMunicipio(null) },
                        onSelectVereda = { vereda -> viewModel.selectVereda(vereda.id) },
                        onLeaderPinClicked = { vereda -> viewModel.inspectLeaderPin(vereda.id) },
                        onOpenMunicipioFicha = { showCitizenMuniSheet = true },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!uiState.isCampaignMode && uiState.selectedMunicipio == null) {
                        CitizenDepartmentBottomCard(
                            candidatoNombre = uiState.configuracion.nombreCandidato,
                            onOpenDeptProposals = { viewModel.setDeptProposalsVisible(true) },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    if (uiState.isCampaignMode && uiState.selectedMunicipio != null && uiState.selectedVereda == null) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Toca una vereda o casco urbano de ${uiState.selectedMunicipio?.nombre} para abrir su Ficha de Vereda y registrar simpatizantes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                // ModalBottomSheet: Ficha Municipal Pública (Modo Ciudadano)
                val selectedMuni = uiState.selectedMunicipio
                if (!uiState.isCampaignMode && selectedMuni != null && showCitizenMuniSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCitizenMuniSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CitizenMunicipioContent(
                            municipio = selectedMuni,
                            veredas = uiState.veredas.filter { it.municipioId == selectedMuni.codigoDane },
                            selectedVeredaId = uiState.selectedVeredaId,
                            gestionPropuestas = uiState.gestionPropuestas,
                            selectedEje = uiState.selectedEjeFilter,
                            isBusy = uiState.isBusy,
                            onSelectEje = viewModel::setEjeFilter,
                            onSubmitReporte = { vId, vNom, cat, desc, tel ->
                                viewModel.submitReporteCiudadano(
                                    municipioId = selectedMuni.codigoDane,
                                    veredaId = vId,
                                    veredaNombre = vNom,
                                    categoria = cat,
                                    descripcion = desc,
                                    telefonoOpcional = tel
                                )
                            }
                        )
                    }
                }

                // ModalBottomSheet: Ficha de Vereda (Modo Campaña)
                val selectedVereda = uiState.selectedVereda
                if (uiState.isCampaignMode && selectedMuni != null && selectedVereda != null) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.selectVereda(null) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CampaignVeredaDetailContent(
                            municipio = selectedMuni,
                            vereda = selectedVereda,
                            simpatizantes = uiState.simpatizantes.filter { it.veredaId == selectedVereda.id },
                            reportes = uiState.reportes.filter {
                                it.veredaId == selectedVereda.id ||
                                    (it.municipioId == selectedMuni.codigoDane && it.veredaId.isBlank())
                            },
                            isBusy = uiState.isBusy,
                            onRegisterSimpatizante = { nom, ced, tel, puesto, mesa, habeas, onDone ->
                                viewModel.registerSimpatizante(
                                    nombre = nom,
                                    cedula = ced,
                                    telefono = tel,
                                    municipioId = selectedMuni.codigoDane,
                                    veredaId = selectedVereda.id,
                                    puestoVotacion = puesto,
                                    mesa = mesa,
                                    aceptoHabeasData = habeas,
                                    onDone = onDone
                                )
                            },
                            onUpdateReporteEstado = viewModel::updateReporteEstado
                        )
                    }
                }
            }
        }
    }

    // ModalBottomSheet: Propuestas Departamentales
    if (uiState.isDeptProposalsVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setDeptProposalsVisible(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            DepartamentalProposalsContent(
                items = uiState.gestionPropuestas,
                selectedEje = uiState.selectedEjeFilter,
                onSelectEje = viewModel::setEjeFilter,
                onClose = { viewModel.setDeptProposalsVisible(false) }
            )
        }
    }

    // ModalBottomSheet: Panel CMS Admin
    if (uiState.isCmsSheetVisible && uiState.canOpenCms) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setCmsSheetVisible(false) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AdminCmsContent(
                config = uiState.configuracion,
                municipios = uiState.municipios,
                gestionPropuestas = uiState.gestionPropuestas,
                usuarios = uiState.usuariosCampana,
                isBusy = uiState.isBusy,
                onSaveConfig = { viewModel.saveConfiguracion(it) },
                onSaveGestionPropuesta = { id, tipo, muniId, cat, tit, desc, img, fecha, pub ->
                    viewModel.saveGestionPropuesta(id, tipo, muniId, cat, tit, desc, img, fecha, pub)
                },
                onDeleteGestionPropuesta = viewModel::deleteGestionPropuesta,
                onSaveUsuario = { viewModel.saveUsuarioCampana(it) },
                onSeedFirestore = viewModel::seedInitialCatalog,
                onClose = { viewModel.setCmsSheetVisible(false) }
            )
        }
    }

    // Diálogo Ficha del Líder al tocar un pin en el mapa
    val inspectedVereda = uiState.inspectedLeaderVereda
    if (uiState.isCampaignMode && inspectedVereda != null) {
        val muniName = uiState.municipios.firstOrNull { it.codigoDane == inspectedVereda.municipioId }?.nombre ?: "Casanare"
        LeaderPinFichaDialog(
            vereda = inspectedVereda,
            municipioNombre = muniName,
            referidosCount = uiState.simpatizantesCountForVereda(inspectedVereda.id),
            onDismiss = { viewModel.inspectLeaderPin(null) },
            onOpenVeredaDetail = {
                viewModel.inspectLeaderPin(null)
                viewModel.selectMunicipio(inspectedVereda.municipioId)
                viewModel.selectVereda(inspectedVereda.id)
            }
        )
    }

    // Diálogo de Acceso Equipo (Google Sign-In + Modo Demo Local)
    if (uiState.isAuthDialogVisible) {
        AuthTeamDialog(
            firebaseUser = uiState.firebaseUser,
            isLoadingAuth = uiState.isLoadingAuth,
            authDeniedReason = uiState.authDeniedReason,
            onDismiss = { viewModel.setAuthDialogVisible(false) },
            onActivateDemoLocal = { rol -> viewModel.activateDemoLocal(rol) },
            onAuthError = { msg -> viewModel.showMessage(msg) }
        )
    }
}

@Composable
private fun ExpandedDetailPane(
    uiState: CasanareUiState,
    viewModel: CasanareViewModel
) {
    val selectedMuni = uiState.selectedMunicipio
    val selectedVereda = uiState.selectedVereda

    Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        when {
            uiState.isCampaignMode && selectedMuni != null && selectedVereda != null -> {
                CampaignVeredaDetailContent(
                    municipio = selectedMuni,
                    vereda = selectedVereda,
                    simpatizantes = uiState.simpatizantes.filter { it.veredaId == selectedVereda.id },
                    reportes = uiState.reportes.filter { it.veredaId == selectedVereda.id },
                    isBusy = uiState.isBusy,
                    onRegisterSimpatizante = { nom, ced, tel, puesto, mesa, habeas, onDone ->
                        viewModel.registerSimpatizante(
                            nombre = nom,
                            cedula = ced,
                            telefono = tel,
                            municipioId = selectedMuni.codigoDane,
                            veredaId = selectedVereda.id,
                            puestoVotacion = puesto,
                            mesa = mesa,
                            aceptoHabeasData = habeas,
                            onDone = onDone
                        )
                    },
                    onUpdateReporteEstado = viewModel::updateReporteEstado
                )
            }
            !uiState.isCampaignMode && selectedMuni != null -> {
                CitizenMunicipioContent(
                    municipio = selectedMuni,
                    veredas = uiState.veredas.filter { it.municipioId == selectedMuni.codigoDane },
                    selectedVeredaId = uiState.selectedVeredaId,
                    gestionPropuestas = uiState.gestionPropuestas,
                    selectedEje = uiState.selectedEjeFilter,
                    isBusy = uiState.isBusy,
                    onSelectEje = viewModel::setEjeFilter,
                    onSubmitReporte = { vId, vNom, cat, desc, tel ->
                        viewModel.submitReporteCiudadano(
                            municipioId = selectedMuni.codigoDane,
                            veredaId = vId,
                            veredaNombre = vNom,
                            categoria = cat,
                            descripcion = desc,
                            telefonoOpcional = tel
                        )
                    }
                )
            }
            !uiState.isCampaignMode -> {
                DepartamentalProposalsContent(
                    items = uiState.gestionPropuestas,
                    selectedEje = uiState.selectedEjeFilter,
                    onSelectEje = viewModel::setEjeFilter,
                    onClose = {}
                )
            }
            else -> {
                Column(modifier = Modifier.padding(16.dp)) {
                    EmptyCivicState(
                        title = "Panel Territorial de Campaña",
                        subtitle = "Selecciona un municipio en el mapa y luego toca una vereda o casco urbano para registrar simpatizantes y revisar reportes."
                    )
                }
            }
        }
    }
}
