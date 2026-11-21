package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.CapaMapa
import com.example.model.FiltroVereda
import com.example.model.Municipio
import com.example.model.Vereda
import com.example.ui.theme.AlertCritical
import com.example.ui.theme.AlertOk
import com.example.ui.theme.AlertWarning
import com.example.ui.theme.HeatHigh
import com.example.ui.theme.HeatLow
import com.example.ui.theme.HeatMid
import com.example.ui.theme.HeatZero
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CasanareGeoMap(
    municipios: List<Municipio>,
    veredas: List<Vereda>,
    selectedMunicipio: Municipio?,
    selectedVeredaId: String?,
    isCampaignMode: Boolean,
    activeLayer: CapaMapa,
    showLeaderPins: Boolean,
    veredaFilter: FiltroVereda,
    simpatizantesCountByMunicipio: (String) -> Int,
    simpatizantesCountByVereda: (String) -> Int,
    lideresCountByMunicipio: (String) -> Int,
    onSelectMunicipio: (String) -> Unit,
    onBackToDepartment: () -> Unit,
    onSelectVereda: (Vereda) -> Unit,
    onLeaderPinClicked: (Vereda) -> Unit,
    onOpenMunicipioFicha: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var basemapKey by remember { mutableStateOf("calles") }
    var showMuniDropdown by remember { mutableStateOf(false) }
    var showAllMunicipiosGrid by remember { mutableStateOf(false) }

    val sortedMunicipios = remember(municipios) {
        municipios.sortedBy { it.nombre }
    }

    val latestOnSelectMunicipio by rememberUpdatedState(onSelectMunicipio)
    val latestOnSelectVereda by rememberUpdatedState(onSelectVereda)
    val latestOnLeaderPinClicked by rememberUpdatedState(onLeaderPinClicked)
    val latestVeredas by rememberUpdatedState(veredas)

    val stateJson = remember(
        municipios,
        veredas,
        selectedMunicipio?.codigoDane,
        selectedVeredaId,
        isCampaignMode,
        activeLayer,
        showLeaderPins,
        veredaFilter,
        primaryColor,
        secondaryColor
    ) {
        buildStateJson(
            municipios = municipios,
            veredas = veredas,
            selectedMunicipioId = selectedMunicipio?.codigoDane,
            selectedVeredaId = selectedVeredaId,
            isCampaignMode = isCampaignMode,
            activeLayer = activeLayer,
            showLeaderPins = showLeaderPins,
            veredaFilter = veredaFilter,
            primaryHex = colorToHex(primaryColor),
            secondaryHex = colorToHex(secondaryColor),
            simpatizantesCountByMunicipio = simpatizantesCountByMunicipio,
            simpatizantesCountByVereda = simpatizantesCountByVereda,
            lideresCountByMunicipio = lideresCountByMunicipio
        )
    }

    LaunchedEffect(isMapReady, stateJson) {
        if (isMapReady) {
            webViewRef?.evaluateJavascript("window.updateMapState($stateJson);", null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(if (selectedMunicipio == null) "casanare_dept_map" else "casanare_muni_map")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.parseColor("#E6EFE8"))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webChromeClient = WebChromeClient()

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onMunicipioSelected(codigoDane: String) {
                                post {
                                    showAllMunicipiosGrid = false
                                    latestOnSelectMunicipio(codigoDane)
                                }
                            }

                            @JavascriptInterface
                            fun onVeredaSelected(veredaId: String) {
                                post {
                                    latestVeredas.firstOrNull { it.id == veredaId }?.let {
                                        latestOnSelectVereda(it)
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun onLeaderPinClicked(veredaId: String) {
                                post {
                                    latestVeredas.firstOrNull { it.id == veredaId }?.let {
                                        latestOnLeaderPinClicked(it)
                                    }
                                }
                            }
                        },
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            view?.evaluateJavascript(
                                "window.updateMapState($stateJson);",
                                null
                            )
                            isMapReady = true
                        }
                    }

                    loadUrl("file:///android_asset/casanare_map.html")
                    webViewRef = this
                }
            }
        )

        // Controles superiores en Nivel 2 vs Chips rápidos en Nivel 1
        if (selectedMunicipio != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onBackToDepartment,
                    modifier = Modifier.testTag("back_to_casanare_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    ),
                    elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver al mapa departamental",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Volver a Casanare",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (!isCampaignMode) {
                    Surface(
                        onClick = onOpenMunicipioFicha,
                        shape = RoundedCornerShape(50),
                        color = primaryColor,
                        shadowElevation = 4.dp,
                        modifier = Modifier.testTag("open_muni_ficha_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Ficha ${selectedMunicipio.nombre}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector desplegable oficial con los 19 municipios
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { showMuniDropdown = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                            border = BorderStroke(1.dp, outlineColor),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("muni_dropdown_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Elegir municipio (19)",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Abrir lista de 19 municipios"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMuniDropdown,
                            onDismissRequest = { showMuniDropdown = false },
                            modifier = Modifier.heightIn(max = 340.dp)
                        ) {
                            sortedMunicipios.forEach { muni ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${muni.nombre} (${muni.subregion})",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        showMuniDropdown = false
                                        showAllMunicipiosGrid = false
                                        onSelectMunicipio(muni.codigoDane)
                                    }
                                )
                            }
                        }
                    }

                    // Botón desplegable "Ver los 19 Municipios" en cuadrícula
                    FilledTonalButton(
                        onClick = { showAllMunicipiosGrid = !showAllMunicipiosGrid },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                        modifier = Modifier.testTag("toggle_all_19_municipios")
                    ) {
                        Icon(
                            imageVector = if (showAllMunicipiosGrid) Icons.Default.Close else Icons.Default.GridView,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (showAllMunicipiosGrid) "Ocultar" else "Ver los 19 Municipios",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Panel desplegable en cuadrícula con los 19 municipios oficiales
                if (showAllMunicipiosGrid) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        border = BorderStroke(1.dp, outlineColor),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 310.dp)
                            .testTag("grid_19_municipios_panel")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Los 19 Municipios Oficiales de Casanare",
                                style = MaterialTheme.typography.titleSmall,
                                color = primaryColor
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sortedMunicipios, key = { it.codigoDane }) { muni ->
                                    Surface(
                                        onClick = {
                                            showAllMunicipiosGrid = false
                                            onSelectMunicipio(muni.codigoDane)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.6f)),
                                        modifier = Modifier.testTag("grid_muni_${muni.codigoDane}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCampaignMode && muni.nivelEnergia < 45) AlertCritical else primaryColor
                                                    )
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = muni.nombre,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = muni.subregion,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selector de Mapa Base (Calles / Relieve / Satélite) + Leyenda en esquina inferior izquierda
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.7f)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = primaryColor
                    )
                    listOf("calles" to "Calles", "relieve" to "Relieve", "satelite" to "Satélite").forEach { (key, label) ->
                        val active = basemapKey == key
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color = if (active) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    basemapKey = key
                                    webViewRef?.evaluateJavascript("window.setBasemap('$key');", null)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.7f)),
                shadowElevation = 3.dp
            ) {
                MapLegendContent(
                    isCampaignMode = isCampaignMode,
                    activeLayer = activeLayer,
                    isVeredaLevel = selectedMunicipio != null,
                    primaryColor = primaryColor
                )
            }
        }

        // Controles de Zoom en esquina inferior derecha
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { webViewRef?.evaluateJavascript("window.zoomIn();", null) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar mapa")
            }
            SmallFloatingActionButton(
                onClick = { webViewRef?.evaluateJavascript("window.zoomOut();", null) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar mapa")
            }
            SmallFloatingActionButton(
                onClick = { webViewRef?.evaluateJavascript("window.resetView();", null) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = primaryColor,
                modifier = Modifier.testTag("map_zoom_reset")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar mapa")
            }
        }
    }
}

@Composable
private fun MapLegendContent(
    isCampaignMode: Boolean,
    activeLayer: CapaMapa,
    isVeredaLevel: Boolean,
    primaryColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(
            text = when {
                !isCampaignMode && !isVeredaLevel -> "Casanare Real · 19 Municipios DANE"
                !isCampaignMode && isVeredaLevel -> "Territorio Real · Veredas y Casco Urbano"
                activeLayer == CapaMapa.CALOR_ELECTORAL -> "Calor Electoral (% Meta)"
                activeLayer == CapaMapa.ENERGIA_ALERTAS -> "Mapa de Energía / Alertas"
                else -> "Estructura de Líderes"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        if (!isCampaignMode) {
            LegendDot(primaryColor.copy(alpha = 0.78f), "Toca un municipio o vereda real")
        } else {
            when (activeLayer) {
                CapaMapa.CALOR_ELECTORAL -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendDot(HeatZero, "0%")
                        LegendDot(HeatLow, "<35%")
                        LegendDot(HeatMid, "50%")
                        LegendDot(HeatHigh, "80%+")
                    }
                }
                CapaMapa.ENERGIA_ALERTAS -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendDot(AlertCritical, "Alerta (<40%)")
                        LegendDot(AlertWarning, "Media")
                        LegendDot(AlertOk, "Fuerte")
                    }
                }
                CapaMapa.DENSIDAD_LIDERES -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendDot(AlertCritical, "Huérfana")
                        LegendDot(AlertOk, "Con líder")
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

private fun buildStateJson(
    municipios: List<Municipio>,
    veredas: List<Vereda>,
    selectedMunicipioId: String?,
    selectedVeredaId: String?,
    isCampaignMode: Boolean,
    activeLayer: CapaMapa,
    showLeaderPins: Boolean,
    veredaFilter: FiltroVereda,
    primaryHex: String,
    secondaryHex: String,
    simpatizantesCountByMunicipio: (String) -> Int,
    simpatizantesCountByVereda: (String) -> Int,
    lideresCountByMunicipio: (String) -> Int
): String {
    val muniArray = JSONArray().apply {
        municipios.forEach { m ->
            put(
                JSONObject().apply {
                    put("codigoDane", m.codigoDane)
                    put("nombre", m.nombre)
                    put("subregion", m.subregion)
                    put("metaVotos", m.metaVotos)
                    put("nivelEnergia", m.nivelEnergia)
                    put("simpatizantesCount", simpatizantesCountByMunicipio(m.codigoDane))
                    put("lideresCount", lideresCountByMunicipio(m.codigoDane))
                }
            )
        }
    }
    val verArray = JSONArray().apply {
        veredas.forEach { v ->
            put(
                JSONObject().apply {
                    put("id", v.id)
                    put("municipioId", v.municipioId)
                    put("nombre", v.nombre)
                    put("zona", v.zona.name)
                    put("metaVotos", v.metaVotos)
                    put("nivelEnergia", v.nivelEnergia)
                    put("liderUid", v.liderUid)
                    put("liderNombre", v.liderNombre)
                    put("lat", v.lat)
                    put("lon", v.lon)
                    put("geojson", v.geojson)
                    put("simpatizantesCount", simpatizantesCountByVereda(v.id))
                }
            )
        }
    }
    return JSONObject().apply {
        put("selectedMunicipioId", selectedMunicipioId ?: JSONObject.NULL)
        put("selectedVeredaId", selectedVeredaId ?: JSONObject.NULL)
        put("isCampaignMode", isCampaignMode)
        put("activeLayer", activeLayer.name)
        put("showLeaderPins", showLeaderPins)
        put("veredaFilter", veredaFilter.name)
        put("primaryColor", primaryHex)
        put("secondaryColor", secondaryHex)
        put("municipios", muniArray)
        put("veredas", verArray)
    }.toString()
}
