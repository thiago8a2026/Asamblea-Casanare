package com.example.data

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
import com.google.firebase.Timestamp

object CasanareSeedData {

    val defaultConfig = ConfiguracionCampana(
        id = "general",
        nombreCandidato = "[Nombre del Candidato]",
        eslogan = "[Eslogan de Campaña · Asamblea de Casanare]",
        colorPrimario = "#0B6E4F",
        colorSecundario = "#D97706",
        logoUrl = ""
    )

    // Los 19 municipios oficiales del Departamento de Casanare (Código DANE 85)
    // Los polígonos reales DANE se cargan desde app/src/main/assets/casanare_municipios.geojson
    val municipios: List<Municipio> = listOf(
        Municipio(
            codigoDane = "85001", nombre = "Yopal", subregion = "Centro",
            metaVotos = 8500, nivelEnergia = 84,
            resumenPublico = "[Resumen Municipal]: Capital departamental. Impulso a empleo juvenil, vías rurales de El Morro y La Chaparrera, y educación superior.",
            lat = 5.3377, lon = -72.3958
        ),
        Municipio(
            codigoDane = "85010", nombre = "Aguazul", subregion = "Piedemonte",
            metaVotos = 4200, nivelEnergia = 88,
            resumenPublico = "[Resumen Municipal]: Capital arrocera. Fortalecimiento agroindustrial, vías terciarias de Cupiagua y San José del Bubuy, y agua potable.",
            lat = 5.1728, lon = -72.5472
        ),
        Municipio(
            codigoDane = "85440", nombre = "Villanueva", subregion = "Sur",
            metaVotos = 3600, nivelEnergia = 79,
            resumenPublico = "[Resumen Municipal]: Motor agroexportador del sur. Palma, arroz, ganadería sostenible y centro regional de emprendimiento.",
            lat = 4.6083, lon = -72.9278
        ),
        Municipio(
            codigoDane = "85162", nombre = "Monterrey", subregion = "Sur",
            metaVotos = 2400, nivelEnergia = 74,
            resumenPublico = "[Resumen Municipal]: Puerta del llano sur. Turismo ecológico, agroindustria láctea y pavimentación de corredores veredales.",
            lat = 4.8758, lon = -72.8936
        ),
        Municipio(
            codigoDane = "85410", nombre = "Tauramena", subregion = "Sur",
            metaVotos = 2900, nivelEnergia = 65,
            resumenPublico = "[Resumen Municipal]: Transición energética productiva, apoyo al campesinado y conectividad rural en Paso Cusiana.",
            lat = 5.0167, lon = -72.7519
        ),
        Municipio(
            codigoDane = "85250", nombre = "Paz de Ariporo", subregion = "Norte",
            metaVotos = 3100, nivelEnergia = 58,
            resumenPublico = "[Resumen Municipal]: Corazón ganadero del norte. Bancos de maquinaria agrícola, mejoramiento de hatos y salud rural.",
            lat = 5.8819, lon = -71.8917
        ),
        Municipio(
            codigoDane = "85125", nombre = "Hato Corozal", subregion = "Norte",
            metaVotos = 1600, nivelEnergia = 42,
            resumenPublico = "[Resumen Municipal]: Seguridad vial en la Marginal de la Selva, apoyo a ganadería extensiva y brigadas médicas.",
            lat = 6.1567, lon = -71.7636
        ),
        Municipio(
            codigoDane = "85263", nombre = "Pore", subregion = "Norte",
            metaVotos = 1350, nivelEnergia = 63,
            resumenPublico = "[Resumen Municipal]: Patrimonio histórico de la Ruta Libertadora. Turismo cultural, riego agrícola y escuelas rurales.",
            lat = 5.7294, lon = -71.9958
        ),
        Municipio(
            codigoDane = "85430", nombre = "Trinidad", subregion = "Oriente",
            metaVotos = 1500, nivelEnergia = 49,
            resumenPublico = "[Resumen Municipal]: Sabana productiva. Mantenimiento de terraplenes invernales, ganadería y acceso a internet veredal.",
            lat = 5.4083, lon = -71.6625
        ),
        Municipio(
            codigoDane = "85325", nombre = "San Luis de Palenque", subregion = "Oriente",
            metaVotos = 1100, nivelEnergia = 36,
            resumenPublico = "[Resumen Municipal]: Protección ribereña del río Pauto, impulso al agro de sabana y fortalecimiento del puesto de salud.",
            lat = 5.4208, lon = -71.7314
        ),
        Municipio(
            codigoDane = "85230", nombre = "Orocué", subregion = "Oriente",
            metaVotos = 1250, nivelEnergia = 31,
            resumenPublico = "[Resumen Municipal]: Puerto sobre el río Meta. Turismo fluvial y cultural, logística agrícola y atención a comunidades indígenas.",
            lat = 4.7917, lon = -71.3417
        ),
        Municipio(
            codigoDane = "85139", nombre = "Maní", subregion = "Centro",
            metaVotos = 1750, nivelEnergia = 68,
            resumenPublico = "[Resumen Municipal]: Capital bandolera. Agroindustria arrocera, vías rurales hacia Santa Helena y apoyo al deporte juvenil.",
            lat = 4.8172, lon = -72.2869
        ),
        Municipio(
            codigoDane = "85225", nombre = "Nunchía", subregion = "Piedemonte",
            metaVotos = 1100, nivelEnergia = 54,
            resumenPublico = "[Resumen Municipal]: Piedemonte caficultor y ganadero. Placas huella en zonas de ladera y acueductos veredales.",
            lat = 5.6358, lon = -72.1944
        ),
        Municipio(
            codigoDane = "85400", nombre = "Támara", subregion = "Cordillera",
            metaVotos = 950, nivelEnergia = 46,
            resumenPublico = "[Resumen Municipal]: Tierra cafetera de Casanare. Comercialización directa del café, estabilización de vías y educación media.",
            lat = 5.8333, lon = -72.1619
        ),
        Municipio(
            codigoDane = "85300", nombre = "Sabanalarga", subregion = "Sur",
            metaVotos = 750, nivelEnergia = 44,
            resumenPublico = "[Resumen Municipal]: Municipio citrícola y ganadero. Infraestructura vial terciaria y respaldo a pequeños productores.",
            lat = 4.8542, lon = -73.0389
        ),
        Municipio(
            codigoDane = "85015", nombre = "Chámeza", subregion = "Cordillera",
            metaVotos = 520, nivelEnergia = 28,
            resumenPublico = "[Resumen Municipal]: Municipio de cordillera. Mitigación de riesgos viales, turismo termal y salinero, y salud preventiva.",
            lat = 5.2119, lon = -72.8758
        ),
        Municipio(
            codigoDane = "85279", nombre = "Recetor", subregion = "Cordillera",
            metaVotos = 480, nivelEnergia = 24,
            resumenPublico = "[Resumen Municipal]: Conectividad de montaña, apoyo a piscicultura y ganadería de ladera, y telemedicina rural.",
            lat = 5.2292, lon = -72.7611
        ),
        Municipio(
            codigoDane = "85315", nombre = "Sácama", subregion = "Cordillera",
            metaVotos = 420, nivelEnergia = 22,
            resumenPublico = "[Resumen Municipal]: Corredor andino-llanero (Ruta Los Libertadores). Mejoramiento de la vía del Cusiana Norte y agro.",
            lat = 6.0989, lon = -72.2489
        ),
        Municipio(
            codigoDane = "85136", nombre = "La Salina", subregion = "Cordillera",
            metaVotos = 360, nivelEnergia = 19,
            resumenPublico = "[Resumen Municipal]: Atención prioritaria al norte cordillerano. Vía de acceso, escuelas veredales y cadenas productivas.",
            lat = 6.1278, lon = -72.3333
        )
    )

    // Puntos reales (lat, lon) de cascos urbanos, corregimientos y veredas por municipio.
    // Los polígonos GeoJSON de veredas pueden cargarse progresivamente desde Firestore (`vereda.geojson`) o assets sin inventar bloques.
    val veredas: List<Vereda> = listOf(
        // Yopal (85001)
        Vereda("ver_85001_urb", "85001", "Casco Urbano Yopal", ZonaVereda.URBANA, 3800, 91, "demo_lider_yopal_0", "Andrés Barrera", "Col. Braulio González", 5.3377, -72.3958),
        Vereda("ver_85001_morro", "85001", "Correg. El Morro", ZonaVereda.RURAL, 940, 82, "demo_lider_yopal_1", "Lucía Castañeda", "I.E. Antonio Nariño El Morro", 5.4480, -72.4560),
        Vereda("ver_85001_chapa", "85001", "Correg. La Chaparrera", ZonaVereda.RURAL, 940, 76, "demo_lider_yopal_2", "Jairo Pirabán", "I.E. Luis Hernández Vargas", 5.4920, -72.2560),
        Vereda("ver_85001_tilo", "85001", "Correg. Tilodirán", ZonaVereda.RURAL, 940, 55, "demo_lider_yopal_3", "Sandra Holguín", "I.E. Jorge Eliécer Gaitán", 5.1820, -72.2010),
        Vereda("ver_85001_tacar", "85001", "Correg. Tacarimena", ZonaVereda.RURAL, 940, 38, "", "", "Salón Comunal Tacarimena", 5.2760, -72.2750),
        Vereda("ver_85001_matap", "85001", "Vereda Mata de Pantano", ZonaVereda.RURAL, 940, 44, "demo_lider_yopal_5", "Héctor Niño", "Escuela Mata de Pantano", 5.3680, -72.3250),

        // Aguazul (85010)
        Vereda("ver_85010_urb", "85010", "Casco Urbano Aguazul", ZonaVereda.URBANA, 1900, 93, "demo_lider_aguazul_0", "Martha Salamanca", "I.E. Camilo Torres", 5.1728, -72.5472),
        Vereda("ver_85010_cupia", "85010", "Correg. Cupiagua", ZonaVereda.RURAL, 460, 85, "demo_lider_aguazul_1", "Wilmer Torres", "Salón Comunal Cupiagua", 5.2170, -72.6060),
        Vereda("ver_85010_bubuy", "85010", "Correg. San José del Bubuy", ZonaVereda.RURAL, 460, 79, "demo_lider_aguazul_2", "Dora Chaparro", "I.E. San José del Bubuy", 5.1060, -72.5150),
        Vereda("ver_85010_unete", "85010", "Correg. Únete", ZonaVereda.RURAL, 460, 41, "demo_lider_aguazul_3", "Óscar Perilla", "Escuela Rural Únete", 5.2050, -72.5180),
        Vereda("ver_85010_monter", "85010", "Vereda Monterralo", ZonaVereda.RURAL, 460, 68, "demo_lider_aguazul_4", "Nubia Camargo", "Escuela Monterralo", 5.2350, -72.6600),
        Vereda("ver_85010_turua", "85010", "Vereda Turúa", ZonaVereda.RURAL, 460, 25, "", "", "Salón Comunal Turúa", 5.1400, -72.4600),

        // Monterrey (85162)
        Vereda("ver_85162_urb", "85162", "Casco Urbano Monterrey", ZonaVereda.URBANA, 1150, 86, "demo_lider_monterrey_0", "Diego Bohórquez", "I.E. Normal Superior", 4.8758, -72.8936),
        Vereda("ver_85162_brisa", "85162", "Vereda Brisas del Llano", ZonaVereda.RURAL, 310, 72, "demo_lider_monterrey_1", "Patricia León", "Escuela Brisas del Llano", 4.9020, -72.9050),
        Vereda("ver_85162_porve", "85162", "Correg. El Porvenir", ZonaVereda.RURAL, 310, 61, "demo_lider_monterrey_2", "Fabián Roldán", "Puesto Rural El Porvenir", 4.7900, -72.8100),
        Vereda("ver_85162_palo", "85162", "Vereda Palomas Agua Clara", ZonaVereda.RURAL, 310, 33, "demo_lider_monterrey_3", "Erika Bernal", "Escuela Palomas Agua Clara", 4.8550, -72.9350),
        Vereda("ver_85162_garrap", "85162", "Vereda Garrapatal", ZonaVereda.RURAL, 310, 22, "", "", "Salón Comunal Garrapatal", 4.8250, -72.8600),

        // Villanueva (85440)
        Vereda("ver_85440_urb", "85440", "Casco Urbano Villanueva", ZonaVereda.URBANA, 1800, 89, "demo_lider_villanueva_0", "Carlos Julio Vega", "I.E. Ezequiel Moreno", 4.6083, -72.9278),
        Vereda("ver_85440_carib", "85440", "Correg. Caribayona", ZonaVereda.RURAL, 450, 77, "demo_lider_villanueva_1", "Diana Acosta", "Sede Rural Caribayona", 4.5450, -72.8600),
        Vereda("ver_85440_sana", "85440", "Correg. Santa Helena", ZonaVereda.RURAL, 450, 64, "demo_lider_villanueva_2", "Mauricio López", "Escuela Santa Helena", 4.6400, -72.7900),
        Vereda("ver_85440_banq", "85440", "Vereda Banquetas", ZonaVereda.RURAL, 450, 48, "demo_lider_villanueva_3", "Gladys Sarmiento", "Escuela Banquetas", 4.5950, -72.9800),
        Vereda("ver_85440_caim", "85440", "Vereda Caimán", ZonaVereda.RURAL, 450, 29, "", "", "Salón Comunal Caimán", 4.5300, -72.9400),

        // Resto de municipios (cabeceras urbanas reales + puntos veredales listos para escalar GeoJSON)
        Vereda("ver_85410_urb", "85410", "Casco Urbano Tauramena", ZonaVereda.URBANA, 1450, 72, "demo_lider_85410_0", "Líder Tauramena Centro", "I.E. José María Córdoba", 5.0167, -72.7519),
        Vereda("ver_85410_cus", "85410", "Correg. Paso Cusiana", ZonaVereda.RURAL, 720, 58, "demo_lider_85410_1", "Líder Paso Cusiana", "Escuela Paso Cusiana", 4.9600, -72.6900),
        Vereda("ver_85410_acei", "85410", "Vereda Aceite Alto", ZonaVereda.RURAL, 730, 34, "", "", "Escuela Aceite Alto", 5.0600, -72.7950),

        Vereda("ver_85250_urb", "85250", "Casco Urbano Paz de Ariporo", ZonaVereda.URBANA, 1600, 68, "demo_lider_85250_0", "Líder Paz Urbano", "I.E. Juan José Rondón", 5.8819, -71.8917),
        Vereda("ver_85250_mont", "85250", "Correg. Montañas del Totumo", ZonaVereda.RURAL, 750, 49, "demo_lider_85250_1", "Líder Totumo", "Escuela Montañas del Totumo", 5.7900, -71.6800),
        Vereda("ver_85250_cano", "85250", "Correg. Caño Chiquito", ZonaVereda.RURAL, 750, 31, "", "", "Puesto Caño Chiquito", 5.9500, -71.9700),

        Vereda("ver_85125_urb", "85125", "Casco Urbano Hato Corozal", ZonaVereda.URBANA, 850, 52, "demo_lider_85125_0", "Líder Hato Corozal", "I.E. Antonio Martínez", 6.1567, -71.7636),
        Vereda("ver_85125_chire", "85125", "Correg. Chire", ZonaVereda.RURAL, 400, 32, "", "", "Escuela Chire", 6.0800, -71.8800),

        Vereda("ver_85263_urb", "85263", "Casco Urbano Pore", ZonaVereda.URBANA, 750, 69, "demo_lider_85263_0", "Líder Pore", "I.E. Rafael Uribe", 5.7294, -71.9958),
        Vereda("ver_85263_plat", "85263", "Correg. La Plata", ZonaVereda.RURAL, 600, 48, "demo_lider_85263_1", "Líder La Plata", "Escuela La Plata", 5.6900, -71.9200),

        Vereda("ver_85430_urb", "85430", "Casco Urbano Trinidad", ZonaVereda.URBANA, 800, 56, "demo_lider_85430_0", "Líder Trinidad", "I.E. Rafael García Herreros", 5.4083, -71.6625),
        Vereda("ver_85430_bocas", "85430", "Correg. Bocas del Pauto", ZonaVereda.RURAL, 700, 38, "", "", "Puesto Bocas del Pauto", 5.2900, -71.4300),

        Vereda("ver_85325_urb", "85325", "Casco Urbano San Luis de Palenque", ZonaVereda.URBANA, 650, 44, "demo_lider_85325_0", "Líder San Luis", "I.E. Técnico Agropecuario", 5.4208, -71.7314),
        Vereda("ver_85325_vent", "85325", "Vereda La Venturosa", ZonaVereda.RURAL, 450, 27, "", "", "Escuela La Venturosa", 5.3700, -71.5600),

        Vereda("ver_85230_urb", "85230", "Casco Urbano Orocué", ZonaVereda.URBANA, 700, 40, "demo_lider_85230_0", "Líder Orocué", "I.E. Luis Carlos Galán", 4.7917, -71.3417),
        Vereda("ver_85230_cravo", "85230", "Correg. El Algarrobo", ZonaVereda.RURAL, 550, 22, "", "", "Salón El Algarrobo", 4.8600, -71.4800),

        Vereda("ver_85139_urb", "85139", "Casco Urbano Maní", ZonaVereda.URBANA, 1000, 74, "demo_lider_85139_0", "Líder Maní", "I.E. Jesús Bernal Pinzón", 4.8172, -72.2869),
        Vereda("ver_85139_sh", "85139", "Correg. Santa Helena de Cúsiva", ZonaVereda.RURAL, 750, 55, "demo_lider_85139_1", "Líder Santa Helena", "Escuela Santa Helena", 4.6900, -72.1800),

        Vereda("ver_85225_urb", "85225", "Casco Urbano Nunchía", ZonaVereda.URBANA, 650, 61, "demo_lider_85225_0", "Líder Nunchía", "I.E. Salvador Camacho", 5.6358, -72.1944),
        Vereda("ver_85225_yop", "85225", "Correg. La Yopalosa", ZonaVereda.RURAL, 450, 43, "", "", "Salón La Yopalosa", 5.5650, -72.1550),

        Vereda("ver_85400_urb", "85400", "Casco Urbano Támara", ZonaVereda.URBANA, 550, 52, "demo_lider_85400_0", "Líder Támara", "I.E. Arturo Salazar", 5.8333, -72.1619),
        Vereda("ver_85400_tab", "85400", "Correg. El Tablón", ZonaVereda.RURAL, 400, 34, "", "", "Escuela El Tablón", 5.7950, -72.1200),

        Vereda("ver_85300_urb", "85300", "Casco Urbano Sabanalarga", ZonaVereda.URBANA, 450, 49, "demo_lider_85300_0", "Líder Sabanalarga", "I.E. Jorge Eliécer Gaitán", 4.8542, -73.0389),
        Vereda("ver_85300_ag", "85300", "Correg. Aguaclara", ZonaVereda.RURAL, 300, 35, "", "", "Escuela Aguaclara", 4.8900, -73.0100),

        Vereda("ver_85015_urb", "85015", "Casco Urbano Chámeza", ZonaVereda.URBANA, 320, 35, "demo_lider_85015_0", "Líder Chámeza", "I.E. José Antonio Galán", 5.2119, -72.8758),
        Vereda("ver_85015_mor", "85015", "Vereda Mundo Viejo", ZonaVereda.RURAL, 200, 21, "", "", "Escuela Mundo Viejo", 5.1900, -72.8900),

        Vereda("ver_85279_urb", "85279", "Casco Urbano Recetor", ZonaVereda.URBANA, 300, 31, "demo_lider_85279_0", "Líder Recetor", "I.E. Fernando Rodríguez", 5.2292, -72.7611),
        Vereda("ver_85279_pue", "85279", "Vereda Pueblo Nuevo", ZonaVereda.RURAL, 180, 18, "", "", "Escuela Pueblo Nuevo", 5.2500, -72.7450),

        Vereda("ver_85315_urb", "85315", "Casco Urbano Sácama", ZonaVereda.URBANA, 260, 29, "demo_lider_85315_0", "Líder Sácama", "I.E. Antonio Nariño", 6.0989, -72.2489),
        Vereda("ver_85315_cab", "85315", "Vereda El Tablón Sácama", ZonaVereda.RURAL, 160, 17, "", "", "Escuela Rural Sácama", 6.0750, -72.2200),

        Vereda("ver_85136_urb", "85136", "Casco Urbano La Salina", ZonaVereda.URBANA, 220, 26, "demo_lider_85136_0", "Líder La Salina", "I.E. La Salina", 6.1278, -72.3333),
        Vereda("ver_85136_chin", "85136", "Vereda Chinivaque", ZonaVereda.RURAL, 140, 15, "", "", "Escuela Chinivaque", 6.1450, -72.3100)
    )

    val gestionPropuestas: List<GestionPropuesta> = listOf(
        GestionPropuesta(
            id = "gp_dep_1",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85000",
            categoria = "Agro y Ganadería",
            titulo = "[Propuesta Agro Departamental: Fondo Casanare Productivo]",
            descripcion = "Creación de líneas de cofinanciación y bancos de maquinaria verde para asociaciones campesinas y ganaderas de los 19 municipios.",
            imagenUrl = "local:agro",
            fechaTexto = "Eje Estratégico 2027",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_dep_2",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85000",
            categoria = "Vías Terciarias",
            titulo = "[Propuesta Vías: Plan Mil Kilómetros de Placa Huella]",
            descripcion = "Priorización presupuestal en la Asamblea Departamental para corredores productivos veredales y alcantarillado rural.",
            imagenUrl = "local:agro",
            fechaTexto = "Eje Estratégico 2027",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_dep_3",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85000",
            categoria = "Educación",
            titulo = "[Propuesta Educación: Conectividad y Becas Llaneras]",
            descripcion = "Becas de educación técnica y universitaria con enfoque agroindustrial, tecnológico y de energías limpias para jóvenes casanareños.",
            imagenUrl = "local:social",
            fechaTexto = "Eje Estratégico 2027",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_yop_1",
            tipo = TipoContenido.GESTION,
            municipioId = "85001",
            categoria = "Vías Terciarias",
            titulo = "[Gestión Realizada: Mejoramiento Vial Corregimiento El Morro]",
            descripcion = "Acompañamiento comunitario y gestión ante infraestructura departamental para estabilización de puntos críticos en la vía a El Morro.",
            imagenUrl = "local:agro",
            fechaTexto = "Octubre 2026",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_yop_2",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85001",
            categoria = "Empleo y Emprendimiento",
            titulo = "[Propuesta Local: Centro de Innovación y Primer Empleo Yopal]",
            descripcion = "Incentivos departamentales para empresas locales que contraten jóvenes egresados y mujeres cabeza de hogar en Yopal.",
            imagenUrl = "local:social",
            fechaTexto = "Plan Municipal",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_agu_1",
            tipo = TipoContenido.GESTION,
            municipioId = "85010",
            categoria = "Agro y Ganadería",
            titulo = "[Gestión Realizada: Mesa Técnica Arrocera de Aguazul]",
            descripcion = "Articulación con gremios y productores de San José del Bubuy para acceder a asistencia técnica y secado comunitario.",
            imagenUrl = "local:agro",
            fechaTexto = "Septiembre 2026",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_agu_2",
            tipo = TipoContenido.GESTION,
            municipioId = "85010",
            categoria = "Educación",
            titulo = "[Gestión Realizada: Dotación Escolar Cupiagua y Únete]",
            descripcion = "Gestión comunitaria para aulas solares y conectividad educativa en escuelas rurales del piedemonte aguazuleño.",
            imagenUrl = "local:social",
            fechaTexto = "Agosto 2026",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_agu_3",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85010",
            categoria = "Vías Terciarias",
            titulo = "[Propuesta Local: Anillo Productivo Cupiagua - Monterralo]",
            descripcion = "Ordenanza de inversión multianual para pavimentación y mantenimiento permanente de vías veredales estratégicas.",
            imagenUrl = "local:agro",
            fechaTexto = "Plan Municipal",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_mon_1",
            tipo = TipoContenido.GESTION,
            municipioId = "85162",
            categoria = "Salud",
            titulo = "[Gestión Realizada: Brigada de Salud Veredal El Porvenir]",
            descripcion = "Jornada de atención médica preventiva y entrega de botiquines comunitarios en articulación con líderes veredales.",
            imagenUrl = "local:social",
            fechaTexto = "Octubre 2026",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_mon_2",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85162",
            categoria = "Agro y Ganadería",
            titulo = "[Propuesta Local: Cadena Láctea y Turismo Ecológico Monterrey]",
            descripcion = "Fortalecimiento de centros de acopio lechero y promoción ecoturística de cascadas y piedemonte en Monterrey.",
            imagenUrl = "local:agro",
            fechaTexto = "Plan Municipal",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_vil_1",
            tipo = TipoContenido.GESTION,
            municipioId = "85440",
            categoria = "Empleo y Emprendimiento",
            titulo = "[Gestión Realizada: Feria Agroempresarial del Sur]",
            descripcion = "Impulso a 45 emprendimientos rurales y de mujeres transformadoras en Caribayona y casco urbano de Villanueva.",
            imagenUrl = "local:social",
            fechaTexto = "Septiembre 2026",
            visiblePublico = true
        ),
        GestionPropuesta(
            id = "gp_vil_2",
            tipo = TipoContenido.PROPUESTA,
            municipioId = "85440",
            categoria = "Seguridad",
            titulo = "[Propuesta Local: Red de Seguridad Rural y Fincas Seguras]",
            descripcion = "Implementación de alarmas comunitarias, iluminación solar en cruces rurales y coordinación institucional en el sur.",
            imagenUrl = "local:agro",
            fechaTexto = "Plan Municipal",
            visiblePublico = true
        )
    )

    val demoSimpatizantes: List<Simpatizante> = listOf(
        Simpatizante("ds_1", "Luis Eduardo Abril", "74852100", "3112204455", "85010", "ver_85010_urb", "I.E. Camilo Torres", "Mesa 3", "demo_lider_aguazul_0", "Martha Salamanca", listOf("demo_lider_aguazul_0"), true, Timestamp.now(), Timestamp.now()),
        Simpatizante("ds_2", "Yamileth Cárdenas", "1118541200", "3204418899", "85010", "ver_85010_cupia", "Salón Comunal Cupiagua", "Mesa 1", "demo_lider_aguazul_1", "Wilmer Torres", listOf("demo_lider_aguazul_1"), true, Timestamp.now(), Timestamp.now()),
        Simpatizante("ds_3", "Jhonatan Holguín", "1116549800", "3138890011", "85001", "ver_85001_urb", "Col. Braulio González", "Mesa 12", "demo_lider_yopal_0", "Andrés Barrera", listOf("demo_lider_yopal_0"), true, Timestamp.now(), Timestamp.now()),
        Simpatizante("ds_4", "Rosa Elvira Mendivelso", "47429800", "3145562233", "85162", "ver_85162_urb", "I.E. Normal Superior", "Mesa 2", "demo_lider_monterrey_0", "Diego Bohórquez", listOf("demo_lider_monterrey_0"), true, Timestamp.now(), Timestamp.now()),
        Simpatizante("ds_5", "Hernando Cuevas", "74861990", "3109987766", "85440", "ver_85440_urb", "I.E. Ezequiel Moreno", "Mesa 5", "demo_lider_villanueva_0", "Carlos Julio Vega", listOf("demo_lider_villanueva_0"), true, Timestamp.now(), Timestamp.now()),
        Simpatizante("ds_6", "Claudia Patricia Niño", "1118560312", "3157741122", "85010", "ver_85010_bubuy", "I.E. San José del Bubuy", "Mesa 1", "demo_lider_aguazul_2", "Dora Chaparro", listOf("demo_lider_aguazul_2"), true, Timestamp.now(), Timestamp.now())
    )

    val demoReportes: List<ReporteCiudadano> = listOf(
        ReporteCiudadano(
            id = "rep_demo_1",
            municipioId = "85010",
            veredaId = "ver_85010_turua",
            veredaNombre = "Vereda Turúa",
            categoria = "Vías Terciarias",
            descripcion = "Se requiere motoniveladora y afirmado en el tramo de ingreso hacia la escuela rural antes de lluvias.",
            telefonoOpcional = "3124559876",
            estado = EstadoReporte.PENDIENTE,
            createdAt = Timestamp.now()
        ),
        ReporteCiudadano(
            id = "rep_demo_2",
            municipioId = "85162",
            veredaId = "ver_85162_palo",
            veredaNombre = "Vereda Palomas Agua Clara",
            categoria = "Agua y Saneamiento",
            descripcion = "Bocatoma veredal necesita mantenimiento en filtro comunitario.",
            telefonoOpcional = null,
            estado = EstadoReporte.REVISADO,
            createdAt = Timestamp.now()
        )
    )

    val demoUsuarios: List<UsuarioCampana> = listOf(
        UsuarioCampana(
            uid = "demo_admin_1",
            nombre = "Coordinación General (Demo)",
            email = "admin.demo@casanare2027.co",
            cedula = "74850000",
            telefono = "3100000001",
            rol = RolCampana.ADMIN,
            municipioId = "",
            veredaId = "",
            activo = true
        ),
        UsuarioCampana(
            uid = "demo_coord_aguazul",
            nombre = "Coord. Aguazul (Demo)",
            email = "aguazul.demo@casanare2027.co",
            cedula = "47430000",
            telefono = "3100000002",
            rol = RolCampana.COORD_MUNICIPAL,
            municipioId = "85010",
            veredaId = "",
            activo = true
        ),
        UsuarioCampana(
            uid = "demo_lider_aguazul_1",
            nombre = "Líder Cupiagua (Demo)",
            email = "cupiagua.demo@casanare2027.co",
            cedula = "1118550000",
            telefono = "3100000003",
            rol = RolCampana.LIDER_VEREDAL,
            municipioId = "85010",
            veredaId = "ver_85010_cupia",
            activo = true
        )
    )
}
