package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class ConfiguracionCampana(
    val id: String = "general",
    val nombreCandidato: String = "[Nombre del Candidato]",
    val eslogan: String = "[Eslogan de Campaña · Asamblea de Casanare]",
    val colorPrimario: String = "#0B6E4F",
    val colorSecundario: String = "#D97706",
    val logoUrl: String = "",
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Municipio(
    val codigoDane: String = "",
    val nombre: String = "",
    val subregion: String = "",
    val metaVotos: Int = 0,
    val nivelEnergia: Int = 50,
    val resumenPublico: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val coordUids: List<String> = emptyList()
)

enum class ZonaVereda {
    RURAL,
    URBANA;

    companion object {
        fun fromRaw(raw: String?): ZonaVereda =
            entries.firstOrNull { it.name == raw?.uppercase() } ?: RURAL
    }
}

@Keep
@IgnoreExtraProperties
data class Vereda(
    val id: String = "",
    val municipioId: String = "",
    val nombre: String = "",
    val zona: ZonaVereda = ZonaVereda.RURAL,
    val metaVotos: Int = 0,
    val nivelEnergia: Int = 50,
    val liderUid: String = "",
    val liderNombre: String = "",
    val puestoVotacion: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val geojson: String = ""
)

enum class RolCampana(val label: String) {
    ADMIN("Administrador"),
    COORD_MUNICIPAL("Coord. Municipal"),
    LIDER_VEREDAL("Líder Veredal");

    companion object {
        fun fromRaw(raw: String?): RolCampana =
            entries.firstOrNull { it.name == raw?.uppercase() } ?: LIDER_VEREDAL
    }
}

@Keep
@IgnoreExtraProperties
data class UsuarioCampana(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val cedula: String = "",
    val telefono: String = "",
    val rol: RolCampana = RolCampana.LIDER_VEREDAL,
    val municipioId: String = "",
    val veredaId: String = "",
    val activo: Boolean = true,
    val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Simpatizante(
    val id: String = "",
    val nombre: String = "",
    val cedula: String = "",
    val telefono: String = "",
    val municipioId: String = "",
    val veredaId: String = "",
    val puestoVotacion: String = "",
    val mesa: String = "",
    val liderUid: String = "",
    val liderNombre: String = "",
    val authorizedUids: List<String> = emptyList(),
    val aceptoHabeasData: Boolean = false,
    val fechaHabeasData: Timestamp? = null,
    val createdAt: Timestamp? = null
)

enum class TipoContenido(val label: String) {
    GESTION("Lo que hemos hecho"),
    PROPUESTA("Propuesta");

    companion object {
        fun fromRaw(raw: String?): TipoContenido =
            entries.firstOrNull { it.name == raw?.uppercase() } ?: PROPUESTA
    }
}

@Keep
@IgnoreExtraProperties
data class GestionPropuesta(
    val id: String = "",
    val tipo: TipoContenido = TipoContenido.GESTION,
    val municipioId: String = "85000",
    val categoria: String = "Agro y Ganadería",
    val titulo: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val fechaTexto: String = "",
    val visiblePublico: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

enum class EstadoReporte(val label: String) {
    PENDIENTE("Pendiente"),
    REVISADO("Revisado"),
    GESTIONADO("Gestionado");

    companion object {
        fun fromRaw(raw: String?): EstadoReporte =
            entries.firstOrNull { it.name == raw?.uppercase() } ?: PENDIENTE
    }
}

@Keep
@IgnoreExtraProperties
data class ReporteCiudadano(
    val id: String = "",
    val municipioId: String = "",
    val veredaId: String = "",
    val veredaNombre: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val telefonoOpcional: String? = null,
    val estado: EstadoReporte = EstadoReporte.PENDIENTE,
    val authorizedUids: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)

enum class CapaMapa(val label: String) {
    CALOR_ELECTORAL("Mapa de Calor"),
    ENERGIA_ALERTAS("Energía / Alertas"),
    DENSIDAD_LIDERES("Densidad Líderes")
}

enum class FiltroVereda(val label: String) {
    TODAS("Todas"),
    HUERFANAS("Sin líder"),
    ALERTA_BAJA("Alerta baja")
}

sealed interface AppSessionMode {
    data object Ciudadano : AppSessionMode
    data class CampanaFirebase(val perfil: UsuarioCampana) : AppSessionMode
    data class CampanaDemoLocal(val perfil: UsuarioCampana) : AppSessionMode
}

object CampaignConstants {
    const val DEPARTAMENTAL_ID = "85000"
    const val BOOTSTRAP_ADMIN_EMAIL = "appcasanare2027@gmail.com"

    val EJES_PROPUESTAS = listOf(
        "Agro y Ganadería",
        "Vías Terciarias",
        "Empleo y Emprendimiento",
        "Salud",
        "Educación",
        "Seguridad"
    )

    val CATEGORIAS_REPORTES = listOf(
        "Vías Terciarias",
        "Agua y Saneamiento",
        "Agro y Ganadería",
        "Salud",
        "Educación",
        "Seguridad",
        "Conectividad y Energía"
    )
}
