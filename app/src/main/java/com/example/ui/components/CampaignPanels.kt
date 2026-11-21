package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.AuthManager
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
import com.example.ui.theme.AlertCritical
import com.example.ui.theme.AlertOk
import com.example.ui.theme.AlertWarning
import com.example.ui.viewmodel.CampaignKpis
import com.google.firebase.auth.FirebaseUser

@Composable
fun CampaignKpiAndLayerBar(
    kpis: CampaignKpis,
    activeLayer: CapaMapa,
    showLeaderPins: Boolean,
    isMunicipioLevel: Boolean,
    veredaFilter: FiltroVereda,
    onSelectLayer: (CapaMapa) -> Unit,
    onToggleLeaderPins: () -> Unit,
    onSelectVeredaFilter: (FiltroVereda) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Fila de 4 KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiMiniCard("Líderes", "${kpis.totalLideresActivos}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                KpiMiniCard("Simpatizantes", "${kpis.totalSimpatizantes}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                KpiMiniCard("% Meta", "${kpis.porcentajeMeta}%", AlertOk, Modifier.weight(1f))
                KpiMiniCard("En Alerta", "${kpis.municipiosEnAlerta}", AlertCritical, Modifier.weight(1f))
            }

            Spacer(Modifier.height(6.dp))

            // Selector de Capas sobre el Mapa + Toggle Pines de Líderes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                CapaMapa.entries.forEach { capa ->
                    FilterChip(
                        selected = activeLayer == capa,
                        onClick = { onSelectLayer(capa) },
                        label = { Text(capa.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("layer_${capa.name.lowercase()}")
                    )
                }
                FilterChip(
                    selected = showLeaderPins,
                    onClick = onToggleLeaderPins,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    label = { Text("Pines Líderes", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("toggle_leader_pins_chip")
                )
            }

            // Filtros rápidos de veredas cuando está dentro de un municipio
            if (isMunicipioLevel) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Veredas:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FiltroVereda.entries.forEach { filtro ->
                        FilterChip(
                            selected = veredaFilter == filtro,
                            onClick = { onSelectVeredaFilter(filtro) },
                            label = { Text(filtro.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("vereda_filter_${filtro.name.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CampaignVeredaDetailContent(
    municipio: Municipio,
    vereda: Vereda,
    simpatizantes: List<Simpatizante>,
    reportes: List<ReporteCiudadano>,
    isBusy: Boolean,
    onRegisterSimpatizante: (
        nombre: String,
        cedula: String,
        telefono: String,
        puesto: String,
        mesa: String,
        habeasData: Boolean,
        onDone: () -> Unit
    ) -> Unit,
    onUpdateReporteEstado: (reporteId: String, estado: EstadoReporte) -> Unit,
    modifier: Modifier = Modifier
) {
    var showForm by remember(vereda.id) { mutableStateOf(false) }
    val progress = if (vereda.metaVotos > 0) {
        (simpatizantes.size.toFloat() / vereda.metaVotos.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera Veredal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vereda.nombre,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${municipio.nombre} · Zona ${vereda.zona.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (vereda.liderUid.isBlank()) AlertCritical.copy(alpha = 0.14f) else AlertOk.copy(alpha = 0.14f)
            ) {
                Text(
                    text = if (vereda.liderUid.isBlank()) "Huérfana (Sin líder)" else "Líder activo",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (vereda.liderUid.isBlank()) AlertCritical else AlertOk,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Tarjeta de Líder, Puesto y Meta
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Líder asignado: ${vereda.liderNombre.ifBlank { "Sin asignar (Requiere cobertura)" }}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Puesto de votación: ${vereda.puestoVotacion.ifBlank { "Por definir" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meta Electoral: ${simpatizantes.size} / ${vereda.metaVotos} votos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Button(
            onClick = { showForm = !showForm },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("toggle_register_simpatizante"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (showForm) "Ocultar Formulario" else "+ Registrar Simpatizante")
        }

        if (showForm) {
            SimpatizanteFormCard(
                defaultPuesto = vereda.puestoVotacion,
                isBusy = isBusy,
                onSubmit = { nom, ced, tel, puesto, mesa, habeas ->
                    onRegisterSimpatizante(nom, ced, tel, puesto, mesa, habeas) {
                        showForm = false
                    }
                }
            )
        }

        // Lista de referidos / simpatizantes
        Text(
            text = "Simpatizantes registrados (${simpatizantes.size})",
            style = MaterialTheme.typography.titleSmall
        )
        if (simpatizantes.isEmpty()) {
            EmptyCivicState(
                title = "Sin referidos en esta vereda",
                subtitle = "Usa '+ Registrar Simpatizante' para sumar apoyos con autorización Habeas Data."
            )
        } else {
            simpatizantes.forEach { simp ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(simp.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "CC ${simp.cedula} · Tel ${simp.telefono} · ${simp.puestoVotacion} ${if (simp.mesa.isNotBlank()) "Mesa ${simp.mesa}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Habeas Data aceptado",
                            tint = AlertOk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Buzón de Reportes Ciudadanos de esta vereda/municipio
        Text(
            text = "Buzón Ciudadano en esta zona (${reportes.size})",
            style = MaterialTheme.typography.titleSmall
        )
        if (reportes.isEmpty()) {
            Text(
                text = "No hay reportes ciudadanos pendientes en esta vereda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            reportes.forEach { rep ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rep.categoria, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = rep.estado.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (rep.estado) {
                                    EstadoReporte.PENDIENTE -> AlertCritical
                                    EstadoReporte.REVISADO -> AlertWarning
                                    EstadoReporte.GESTIONADO -> AlertOk
                                }
                            )
                        }
                        Text(rep.descripcion, style = MaterialTheme.typography.bodySmall)
                        if (!rep.telefonoOpcional.isNullOrBlank()) {
                            Text("Contacto: ${rep.telefonoOpcional}", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EstadoReporte.entries.forEach { est ->
                                FilterChip(
                                    selected = rep.estado == est,
                                    onClick = { onUpdateReporteEstado(rep.id, est) },
                                    label = { Text(est.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpatizanteFormCard(
    defaultPuesto: String,
    isBusy: Boolean,
    onSubmit: (nombre: String, cedula: String, telefono: String, puesto: String, mesa: String, habeas: Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var puesto by remember(defaultPuesto) { mutableStateOf(defaultPuesto) }
    var mesa by remember { mutableStateOf("") }
    var aceptoHabeas by remember { mutableStateOf(false) }

    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nuevo Simpatizante / Referido", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("simp_nombre_input")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Cédula *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("simp_cedula_input")
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Celular *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f).testTag("simp_telefono_input")
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = puesto,
                    onValueChange = { puesto = it },
                    label = { Text("Puesto de votación") },
                    singleLine = true,
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = mesa,
                    onValueChange = { mesa = it },
                    label = { Text("Mesa") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { aceptoHabeas = !aceptoHabeas }
            ) {
                Checkbox(
                    checked = aceptoHabeas,
                    onCheckedChange = { aceptoHabeas = it },
                    modifier = Modifier.testTag("simp_habeas_checkbox")
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.habeas_data_label),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = { onSubmit(nombre, cedula, telefono, puesto, mesa, aceptoHabeas) },
                enabled = !isBusy && aceptoHabeas && nombre.isNotBlank() && cedula.length >= 4 && telefono.length >= 7,
                modifier = Modifier.fillMaxWidth().testTag("simp_submit_button")
            ) {
                Text("Guardar Simpatizante")
            }
        }
    }
}

@Composable
fun AuthTeamDialog(
    firebaseUser: FirebaseUser?,
    isLoadingAuth: Boolean,
    authDeniedReason: String?,
    onDismiss: () -> Unit,
    onActivateDemoLocal: (RolCampana) -> Unit,
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val clipboard = LocalClipboardManager.current
    var isGoogleLoading by remember { mutableStateOf(false) }
    var demoRole by remember { mutableStateOf(RolCampana.ADMIN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Acceso Equipo de Campaña") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Inicia sesión con tu cuenta de Google autorizada en Firebase Auth para desbloquear el Mapa de Calor Electoral, registro de simpatizantes y Panel CMS.",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        isGoogleLoading = true
                        AuthManager.onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { isGoogleLoading = false },
                            onAuthError = { msg ->
                                isGoogleLoading = false
                                onAuthError(msg)
                            },
                            scope = scope,
                            onAuthCancelled = { isGoogleLoading = false }
                        )
                    },
                    enabled = !isGoogleLoading && !isLoadingAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_signin_button")
                ) {
                    if (isGoogleLoading || isLoadingAuth) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(id = R.string.btn_google_signin))
                }

                if (authDeniedReason != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AlertCritical.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = authDeniedReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertCritical
                            )
                            if (firebaseUser != null) {
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(firebaseUser.uid))
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copiar UID: ${firebaseUser.uid.take(10)}…")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Pruebas inmediatas (sin configurar usuarios)",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RolCampana.entries.forEach { r ->
                        FilterChip(
                            selected = demoRole == r,
                            onClick = { demoRole = r },
                            label = { Text(r.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onActivateDemoLocal(demoRole) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_local_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(id = R.string.btn_demo_mode))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun AdminCmsContent(
    config: ConfiguracionCampana,
    municipios: List<Municipio>,
    gestionPropuestas: List<GestionPropuesta>,
    usuarios: List<UsuarioCampana>,
    isBusy: Boolean,
    onSaveConfig: (ConfiguracionCampana) -> Unit,
    onSaveGestionPropuesta: (
        existingId: String?,
        tipo: TipoContenido,
        municipioId: String,
        categoria: String,
        titulo: String,
        descripcion: String,
        imagenUrl: String,
        fechaTexto: String,
        visiblePublico: Boolean
    ) -> Unit,
    onDeleteGestionPropuesta: (String) -> Unit,
    onSaveUsuario: (UsuarioCampana) -> Unit,
    onSeedFirestore: () -> Unit,
    onClose: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Panel CMS · Admin", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onClose) { Text("Cerrar") }
        }

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Gestión / Propuestas") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Marca / Colores") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Equipo y Nube") })
        }

        Spacer(Modifier.height(10.dp))

        when (tab) {
            0 -> CmsContentTab(
                municipios = municipios,
                items = gestionPropuestas,
                isBusy = isBusy,
                onSave = onSaveGestionPropuesta,
                onDelete = onDeleteGestionPropuesta
            )
            1 -> CmsConfigTab(
                config = config,
                isBusy = isBusy,
                onSave = onSaveConfig
            )
            2 -> CmsTeamAndSeedTab(
                municipios = municipios,
                usuarios = usuarios,
                isBusy = isBusy,
                onSaveUsuario = onSaveUsuario,
                onSeedFirestore = onSeedFirestore
            )
        }
    }
}

@Composable
private fun CmsContentTab(
    municipios: List<Municipio>,
    items: List<GestionPropuesta>,
    isBusy: Boolean,
    onSave: (String?, TipoContenido, String, String, String, String, String, String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var editingId by remember { mutableStateOf<String?>(null) }
    var tipo by remember { mutableStateOf(TipoContenido.GESTION) }
    var municipioId by remember { mutableStateOf("85010") }
    var categoria by remember { mutableStateOf(CampaignConstants.EJES_PROPUESTAS.first()) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("Noviembre 2026") }
    var visiblePublico by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoContenido.entries.forEach { t ->
                FilterChip(
                    selected = tipo == t,
                    onClick = { tipo = t },
                    label = { Text(t.label) }
                )
            }
        }

        // Selector de Municipio o Departamental
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = municipioId == CampaignConstants.DEPARTAMENTAL_ID,
                onClick = { municipioId = CampaignConstants.DEPARTAMENTAL_ID },
                label = { Text("Departamental") }
            )
            municipios.forEach { m ->
                FilterChip(
                    selected = municipioId == m.codigoDane,
                    onClick = { municipioId = m.codigoDane },
                    label = { Text(m.nombre) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CampaignConstants.EJES_PROPUESTAS.forEach { eje ->
                FilterChip(
                    selected = categoria == eje,
                    onClick = { categoria = eje },
                    label = { Text(eje) }
                )
            }
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título de la tarjeta *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("cms_titulo_input")
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción *") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("cms_desc_input")
        )
        OutlinedTextField(
            value = fechaTexto,
            onValueChange = { fechaTexto = it },
            label = { Text("Fecha o etiqueta (ej. Octubre 2026)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Visible al público (Vista Ciudadana)", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = visiblePublico,
                onCheckedChange = { visiblePublico = it },
                modifier = Modifier.testTag("cms_visible_switch")
            )
        }
        Button(
            onClick = {
                val img = if (categoria == "Agro y Ganadería" || categoria == "Vías Terciarias") "local:agro" else "local:social"
                onSave(editingId, tipo, municipioId, categoria, titulo, descripcion, img, fechaTexto, visiblePublico)
                editingId = null
                titulo = ""
                descripcion = ""
            },
            enabled = !isBusy && titulo.trim().length >= 3 && descripcion.trim().length >= 5,
            modifier = Modifier.fillMaxWidth().testTag("cms_save_content_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (editingId == null) "Publicar Tarjeta" else "Actualizar Tarjeta")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text("Tarjetas registradas (${items.size})", style = MaterialTheme.typography.titleSmall)

        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                editingId = item.id
                                tipo = item.tipo
                                municipioId = item.municipioId
                                categoria = item.categoria
                                titulo = item.titulo
                                descripcion = item.descripcion
                                fechaTexto = item.fechaTexto
                                visiblePublico = item.visiblePublico
                            }
                    ) {
                        Text(item.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${item.tipo.label} · ${if (item.visiblePublico) "Público" else "Borrador"} · ${item.categoria}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = AlertCritical)
                    }
                }
            }
        }
    }
}

@Composable
private fun CmsConfigTab(
    config: ConfiguracionCampana,
    isBusy: Boolean,
    onSave: (ConfiguracionCampana) -> Unit
) {
    var nombre by remember(config) { mutableStateOf(config.nombreCandidato) }
    var eslogan by remember(config) { mutableStateOf(config.eslogan) }
    var colorPrimario by remember(config) { mutableStateOf(config.colorPrimario) }
    var colorSecundario by remember(config) { mutableStateOf(config.colorSecundario) }
    var logoUrl by remember(config) { mutableStateOf(config.logoUrl) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Candidato / Movimiento *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("cms_candidato_input")
        )
        OutlinedTextField(
            value = eslogan,
            onValueChange = { eslogan = it },
            label = { Text("Eslogan de campaña *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = colorPrimario,
                onValueChange = { colorPrimario = it },
                label = { Text("Color Primario (#RRGGBB)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = colorSecundario,
                onValueChange = { colorSecundario = it },
                label = { Text("Color Secundario (#RRGGBB)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Verde Llanero" to ("#0B6E4F" to "#D97706"),
                "Azul Institucional" to ("#1D4ED8" to "#0EA5E9"),
                "Terracota" to ("#B91C1C" to "#F59E0B")
            ).forEach { (label, pair) ->
                FilterChip(
                    selected = colorPrimario.equals(pair.first, ignoreCase = true),
                    onClick = {
                        colorPrimario = pair.first
                        colorSecundario = pair.second
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        OutlinedTextField(
            value = logoUrl,
            onValueChange = { logoUrl = it },
            label = { Text("URL del Logo (opcional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onSave(
                    config.copy(
                        nombreCandidato = nombre,
                        eslogan = eslogan,
                        colorPrimario = colorPrimario,
                        colorSecundario = colorSecundario,
                        logoUrl = logoUrl
                    )
                )
            },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().testTag("cms_save_config_button")
        ) {
            Text("Guardar Identidad de Campaña")
        }
    }
}

@Composable
private fun CmsTeamAndSeedTab(
    municipios: List<Municipio>,
    usuarios: List<UsuarioCampana>,
    isBusy: Boolean,
    onSaveUsuario: (UsuarioCampana) -> Unit,
    onSeedFirestore: () -> Unit
) {
    var uid by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(RolCampana.LIDER_VEREDAL) }
    var muniId by remember { mutableStateOf("85010") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onSeedFirestore,
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth().testTag("cms_seed_button")
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sincronizar Catálogo Casanare (19 municipios) en Firestore")
        }

        HorizontalDivider()
        Text("Asignar Rol en Equipo (usuarios_campana)", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = uid,
            onValueChange = { uid = it },
            label = { Text("UID de Firebase Auth *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Google") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RolCampana.entries.forEach { r ->
                FilterChip(
                    selected = rol == r,
                    onClick = { rol = r },
                    label = { Text(r.label) }
                )
            }
        }
        Button(
            onClick = {
                onSaveUsuario(
                    UsuarioCampana(
                        uid = uid.trim(),
                        nombre = nombre.trim(),
                        email = email.trim(),
                        rol = rol,
                        municipioId = muniId,
                        activo = true
                    )
                )
                uid = ""
                nombre = ""
                email = ""
            },
            enabled = !isBusy && uid.isNotBlank() && nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Groups, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Guardar Usuario de Campaña")
        }

        Text("Miembros activos (${usuarios.size})", style = MaterialTheme.typography.titleSmall)
        usuarios.forEach { u ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("${u.nombre} · ${u.rol.label}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "UID: ${u.uid} · ${u.email.ifBlank { "Sin correo" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderPinFichaDialog(
    vereda: Vereda,
    municipioNombre: String,
    referidosCount: Int,
    onDismiss: () -> Unit,
    onOpenVeredaDetail: () -> Unit
) {
    val hasLeader = vereda.liderUid.isNotBlank()
    val rolText = when {
        !hasLeader -> "Sin líder asignado (Zona huérfana)"
        vereda.zona.name == "URBANA" -> "Coordinación Urbana / Municipal"
        else -> "Líder Veredal / Corregimiento"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = if (hasLeader) AlertOk else AlertCritical
            )
        },
        title = {
            Text(
                text = if (hasLeader) vereda.liderNombre else "Zona sin Líder Asignado",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = (if (hasLeader) AlertOk else AlertCritical).copy(alpha = 0.14f)
                ) {
                    Text(
                        text = rolText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (hasLeader) AlertOk else AlertCritical,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Territorio: $municipioNombre · ${vereda.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Referidos registrados: $referidosCount / ${vereda.metaVotos} votos meta",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Energía territorial: ${vereda.nivelEnergia}% · Puesto: ${vereda.puestoVotacion.ifBlank { "Por definir" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenVeredaDetail) {
                Text("Ver Ficha y Referidos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
