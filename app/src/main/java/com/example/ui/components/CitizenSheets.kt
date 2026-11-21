package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CampaignConstants
import com.example.model.GestionPropuesta
import com.example.model.Municipio
import com.example.model.TipoContenido
import com.example.model.Vereda

@Composable
fun CitizenDepartmentBottomCard(
    candidatoNombre: String,
    onOpenDeptProposals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Asamblea de Casanare · $candidatoNombre",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Toca cualquiera de los 19 municipios en el mapa para ver gestión, propuestas y buzón veredal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onOpenDeptProposals,
                modifier = Modifier.testTag("dept_proposals_button"),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Propuestas\nDepto.", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun DepartamentalProposalsContent(
    items: List<GestionPropuesta>,
    selectedEje: String?,
    onSelectEje: (String?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deptItems = remember(items, selectedEje) {
        items.filter {
            it.visiblePublico &&
                it.municipioId == CampaignConstants.DEPARTAMENTAL_ID &&
                (selectedEje == null || it.categoria == selectedEje)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_casanare_hero),
                contentDescription = "Paisaje llanero de Casanare",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC072B20))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Propuestas Departamentales · Casanare",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Compromisos para los 19 municipios desde la Asamblea",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        EjeFilterRow(selectedEje = selectedEje, onSelectEje = onSelectEje)
        Spacer(Modifier.height(10.dp))

        if (deptItems.isEmpty()) {
            EmptyCivicState(
                title = "Sin propuestas en este eje",
                subtitle = "Prueba otro eje temático o vuelve a ver todas las propuestas."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(340.dp)
            ) {
                items(deptItems, key = { it.id }) { item ->
                    GestionPropuestaCard(item = item)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar")
        }
    }
}

@Composable
fun CitizenMunicipioContent(
    municipio: Municipio,
    veredas: List<Vereda>,
    selectedVeredaId: String?,
    gestionPropuestas: List<GestionPropuesta>,
    selectedEje: String?,
    isBusy: Boolean,
    onSelectEje: (String?) -> Unit,
    onSubmitReporte: (veredaId: String, veredaNombre: String, categoria: String, descripcion: String, telefono: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(municipio.codigoDane) { mutableIntStateOf(0) }

    val publicItems = remember(gestionPropuestas, municipio.codigoDane) {
        gestionPropuestas.filter { it.visiblePublico && it.municipioId == municipio.codigoDane }
    }
    val gestiones = remember(publicItems) {
        publicItems.filter { it.tipo == TipoContenido.GESTION }
    }
    val propuestas = remember(publicItems, selectedEje) {
        publicItems.filter {
            it.tipo == TipoContenido.PROPUESTA && (selectedEje == null || it.categoria == selectedEje)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Cabecera Municipal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = municipio.nombre,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    text = "Subregión ${municipio.subregion} · DANE ${municipio.codigoDane} · ${veredas.size} zonas/veredas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (municipio.resumenPublico.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = municipio.resumenPublico,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(10.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Lo que hemos hecho", maxLines = 1) },
                modifier = Modifier.testTag("tab_gestion")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Propuestas", maxLines = 1) },
                modifier = Modifier.testTag("tab_propuestas")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Tu Vereda", maxLines = 1) },
                modifier = Modifier.testTag("tab_buzon_vereda")
            )
        }

        Spacer(Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                if (gestiones.isEmpty()) {
                    EmptyCivicState(
                        title = "Aún no hay gestiones publicadas en ${municipio.nombre}",
                        subtitle = "El equipo administrativo puede publicar avances y obras desde el Panel CMS."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(320.dp)
                    ) {
                        items(gestiones, key = { it.id }) { item ->
                            GestionPropuestaCard(item = item)
                        }
                    }
                }
            }
            1 -> {
                EjeFilterRow(selectedEje = selectedEje, onSelectEje = onSelectEje)
                Spacer(Modifier.height(8.dp))
                if (propuestas.isEmpty()) {
                    EmptyCivicState(
                        title = "Sin propuestas locales registradas",
                        subtitle = "Consulta también nuestras Propuestas Departamentales o cambia el filtro de eje."
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(propuestas, key = { it.id }) { item ->
                            GestionPropuestaCard(item = item)
                        }
                    }
                }
            }
            2 -> {
                BuzonVeredalForm(
                    municipio = municipio,
                    veredas = veredas,
                    preselectedVeredaId = selectedVeredaId,
                    isBusy = isBusy,
                    onSubmitReporte = onSubmitReporte
                )
            }
        }
    }
}

@Composable
private fun EjeFilterRow(
    selectedEje: String?,
    onSelectEje: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedEje == null,
            onClick = { onSelectEje(null) },
            label = { Text("Todos") }
        )
        CampaignConstants.EJES_PROPUESTAS.forEach { eje ->
            FilterChip(
                selected = selectedEje == eje,
                onClick = { onSelectEje(if (selectedEje == eje) null else eje) },
                label = { Text(eje) }
            )
        }
    }
}

@Composable
fun GestionPropuestaCard(item: GestionPropuesta) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (item.imagenUrl.isNotBlank()) {
                val imgMod = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                when (item.imagenUrl) {
                    "local:agro" -> Image(
                        painter = painterResource(id = R.drawable.img_gestion_agro),
                        contentDescription = item.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = imgMod
                    )
                    "local:social" -> Image(
                        painter = painterResource(id = R.drawable.img_gestion_social),
                        contentDescription = item.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = imgMod
                    )
                    else -> AsyncImage(
                        model = item.imagenUrl,
                        contentDescription = item.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = imgMod
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.categoria,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (item.fechaTexto.isNotBlank()) {
                        Text(
                            text = item.fechaTexto,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BuzonVeredalForm(
    municipio: Municipio,
    veredas: List<Vereda>,
    preselectedVeredaId: String?,
    isBusy: Boolean,
    onSubmitReporte: (veredaId: String, veredaNombre: String, categoria: String, descripcion: String, telefono: String?) -> Unit
) {
    var selectedVereda by remember(municipio.codigoDane, preselectedVeredaId) {
        mutableStateOf(
            veredas.firstOrNull { it.id == preselectedVeredaId } ?: veredas.firstOrNull()
        )
    }
    var veredaExpanded by remember { mutableStateOf(false) }
    var selectedCategoria by remember { mutableStateOf(CampaignConstants.CATEGORIAS_REPORTES.first()) }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Cuéntanos qué pasa en tu vereda o barrio de ${municipio.nombre}",
            style = MaterialTheme.typography.titleSmall
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedVereda?.nombre ?: "Selecciona tu vereda / barrio",
                onValueChange = {},
                readOnly = true,
                label = { Text("Vereda o Casco Urbano") },
                trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { veredaExpanded = true }
                    .testTag("buzon_vereda_selector")
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { veredaExpanded = true }
            )
            DropdownMenu(
                expanded = veredaExpanded,
                onDismissRequest = { veredaExpanded = false }
            ) {
                veredas.forEach { vereda ->
                    DropdownMenuItem(
                        text = { Text(vereda.nombre) },
                        onClick = {
                            selectedVereda = vereda
                            veredaExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CampaignConstants.CATEGORIAS_REPORTES.forEach { cat ->
                FilterChip(
                    selected = selectedCategoria == cat,
                    onClick = { selectedCategoria = cat },
                    label = { Text(cat) }
                )
            }
        }

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                submitted = false
            },
            label = { Text("¿Cuál es la necesidad o propuesta de tu sector?") },
            minLines = 3,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("buzon_descripcion_input")
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono / WhatsApp (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("buzon_telefono_input")
        )

        if (submitted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tu reporte fue recibido por el equipo de campaña.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = {
                val ver = selectedVereda ?: return@Button
                onSubmitReporte(ver.id, ver.nombre, selectedCategoria, descripcion, telefono)
                if (descripcion.trim().length >= 5) {
                    descripcion = ""
                    telefono = ""
                    submitted = true
                }
            },
            enabled = !isBusy && selectedVereda != null && descripcion.trim().length >= 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("buzon_submit_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Enviar Reporte Ciudadano")
        }
    }
}

@Composable
fun EmptyCivicState(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
