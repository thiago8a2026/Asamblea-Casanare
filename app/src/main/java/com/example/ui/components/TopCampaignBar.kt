package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppSessionMode
import com.example.model.ConfiguracionCampana
import com.example.model.Municipio
import com.example.model.RolCampana

@Composable
fun TopCampaignBar(
    config: ConfiguracionCampana,
    sessionMode: AppSessionMode,
    municipios: List<Municipio>,
    selectedMunicipio: Municipio?,
    onSelectMunicipio: (String) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onSignOut: () -> Unit,
    onOpenCms: () -> Unit,
    onSwitchDemoRole: (RolCampana) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val isCampaign = sessionMode !is AppSessionMode.Ciudadano
    val isDemo = sessionMode is AppSessionMode.CampanaDemoLocal
    val activePerfil = when (sessionMode) {
        is AppSessionMode.CampanaFirebase -> sessionMode.perfil
        is AppSessionMode.CampanaDemoLocal -> sessionMode.perfil
        is AppSessionMode.Ciudadano -> null
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (config.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = config.logoUrl,
                            contentDescription = "Logo de campaña",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_casanare),
                            contentDescription = "Emblema Casanare 2027",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.nombreCandidato,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = config.eslogan,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Botón Buscador Rápido de 19 Municipios
                IconButton(
                    onClick = { showSearchDialog = true },
                    modifier = Modifier.testTag("search_municipios_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar entre los 19 municipios"
                    )
                }

                // Botón CMS (Solo ADMIN)
                if (activePerfil?.rol == RolCampana.ADMIN) {
                    IconButton(
                        onClick = onOpenCms,
                        modifier = Modifier.testTag("open_cms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Abrir Panel CMS y Configuración",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Botón Acceso Equipo / Cerrar Sesión
                if (!isCampaign) {
                    OutlinedButton(
                        onClick = onOpenAuthDialog,
                        modifier = Modifier.testTag("team_access_button"),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Equipo", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión de campaña",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Fila de Insignia de Modo + Municipio activo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isCampaign) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = isDemo) { showRoleMenu = true }
                            .testTag("mode_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCampaign) Icons.Default.AdminPanelSettings else Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(5.dp))
                            val modeText = when (sessionMode) {
                                is AppSessionMode.Ciudadano -> "Vista Ciudadana · Pública"
                                is AppSessionMode.CampanaFirebase -> "Vista Campaña · ${sessionMode.perfil.rol.label}"
                                is AppSessionMode.CampanaDemoLocal -> "Demo Local · ${sessionMode.perfil.rol.label} ▾"
                            }
                            Text(
                                text = modeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        RolCampana.entries.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text("Probar rol: ${rol.label}") },
                                onClick = {
                                    showRoleMenu = false
                                    onSwitchDemoRole(rol)
                                }
                            )
                        }
                    }
                }

                Text(
                    text = selectedMunicipio?.let { "${it.nombre} (${it.subregion})" }
                        ?: "Casanare · DANE 85",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showSearchDialog = true }
                )
            }
        }
    }

    if (showSearchDialog) {
        MunicipioSearchDialog(
            municipios = municipios,
            onDismiss = { showSearchDialog = false },
            onSelect = { code ->
                showSearchDialog = false
                onSelectMunicipio(code)
            }
        )
    }
}

@Composable
private fun MunicipioSearchDialog(
    municipios: List<Municipio>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, municipios) {
        if (query.isBlank()) municipios.sortedBy { it.nombre }
        else municipios.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                it.subregion.contains(query, ignoreCase = true) ||
                it.codigoDane.contains(query)
        }.sortedBy { it.nombre }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("19 Municipios de Casanare") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar municipio o subregión…") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("municipio_search_input")
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(filtered, key = { it.codigoDane }) { muni ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(muni.codigoDane) }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                .testTag("search_result_${muni.codigoDane}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = muni.nombre,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Subregión ${muni.subregion} · DANE ${muni.codigoDane}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
