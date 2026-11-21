package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Territorial Llanera por defecto (parametrizable desde configuracion_campana)
val DefaultPrimaryHex = "#0B6E4F"
val DefaultSecondaryHex = "#D97706"

val EmeraldLlanero = Color(0xFF0B6E4F)
val EmeraldDeep = Color(0xFF073B2B)
val EmeraldSoft = Color(0xFFD6F3E8)
val AmberSabana = Color(0xFFD97706)
val AmberSoft = Color(0xFFFEF3C7)

val SurfaceWarmPaper = Color(0xFFF6F8F6)
val SurfaceCardLight = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFEAF0EC)
val InkDark = Color(0xFF10211A)
val InkMuted = Color(0xFF4A6157)

// Colores semánticos de Capas GIS
val HeatZero = Color(0xFFD1D5DB)
val HeatLow = Color(0xFFEF4444)
val HeatMid = Color(0xFFF59E0B)
val HeatHigh = Color(0xFF10B981)
val HeatMax = Color(0xFF0284C7)

val AlertCritical = Color(0xFFDC2626)
val AlertWarning = Color(0xFFF97316)
val AlertOk = Color(0xFF16A34A)

fun parseHexColorOrDefault(hex: String, fallback: Color): Color {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        if (cleaned.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleaned"))
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
