const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_EMAIL = "appcasanare2027@gmail.com";
const ADMIN_UID = "admin_uid_1";
const COORD_UID = "coord_aguazul_1";
const LIDER_UID = "lider_monterrey_1";
const STRANGER_UID = "stranger_999";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Public citizen: can read configuracion_campana, municipios, veredas and visible_publico gestion_propuestas", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("configuracion_campana").doc("general").set({
      nombre_candidato: "[Nombre del Candidato]",
      eslogan: "Casanare Productivo",
      color_primario: "#0B6E4F",
      color_secundario: "#D97706",
    });
    await db.collection("municipios").doc("85010").set({
      codigo_dane: "85010",
      nombre: "Aguazul",
      subregion: "Centro",
      meta_votos: 3200,
      nivel_energia: 78,
      resumen_publico: "Corazón arrocero y energético",
    });
    await db.collection("gestion_propuestas").doc("pub_1").set({
      tipo: "GESTION",
      municipio_id: "85010",
      categoria: "Vías Terciarias",
      titulo: "Mejoramiento vía Cupiagua",
      descripcion: "Gestión de placa huella rural",
      visible_publico: true,
      created_at: new Date(Date.now() - 5000),
      updated_at: new Date(Date.now() - 5000),
    });
    await db.collection("gestion_propuestas").doc("draft_1").set({
      tipo: "PROPUESTA",
      municipio_id: "85010",
      categoria: "Salud",
      titulo: "Borrador interno",
      descripcion: "No publicado",
      visible_publico: false,
      created_at: new Date(Date.now() - 5000),
      updated_at: new Date(Date.now() - 5000),
    });
  });

  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauth.collection("configuracion_campana").doc("general").get());
  await assertSucceeds(unauth.collection("municipios").get());
  await assertSucceeds(
    unauth.collection("gestion_propuestas").where("visible_publico", "==", true).get()
  );
  await assertFails(unauth.collection("gestion_propuestas").doc("draft_1").get());
  await assertFails(unauth.collection("gestion_propuestas").get());
});

test("Public citizen: can create valid reporte_ciudadano with estado PENDIENTE, cannot read list", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    unauth.collection("reportes_ciudadanos").doc("rep_1").set({
      municipio_id: "85010",
      vereda_id: "ver_85010_1",
      vereda_nombre: "San José del Bubuy",
      categoria: "Vías Terciarias",
      descripcion: "Alcantarilla obstruida en el paso principal.",
      estado: "PENDIENTE",
      authorized_uids: [LIDER_UID],
      created_at: new Date(Date.now() - 1000),
    })
  );

  // Reject non-PENDIENTE state on public create
  await assertFails(
    unauth.collection("reportes_ciudadanos").doc("rep_2").set({
      municipio_id: "85010",
      vereda_id: "ver_85010_1",
      vereda_nombre: "San José del Bubuy",
      categoria: "Vías Terciarias",
      descripcion: "Intento de forzar estado.",
      estado: "GESTIONADO",
      authorized_uids: [],
      created_at: new Date(Date.now() - 1000),
    })
  );

  await assertFails(unauth.collection("reportes_ciudadanos").get());
});

test("Public & stranger: blocked from usuarios_campana and simpatizantes", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("usuarios_campana").doc(LIDER_UID).set({
      uid: LIDER_UID,
      nombre: "Líder Monterrey",
      rol: "LIDER_VEREDAL",
      municipio_id: "85162",
      vereda_id: "ver_85162_1",
      activo: true,
      created_at: new Date(Date.now() - 10000),
    });
    await db.collection("simpatizantes").doc("simp_1").set({
      nombre: "Carlos Pérez",
      cedula: "1118555111",
      telefono: "3105551234",
      municipio_id: "85162",
      vereda_id: "ver_85162_1",
      lider_uid: LIDER_UID,
      authorized_uids: [LIDER_UID, COORD_UID],
      acepto_habeas_data: true,
      fecha_habeas_data: new Date(Date.now() - 5000),
      created_at: new Date(Date.now() - 5000),
    });
  });

  const unauth = testEnv.unauthenticatedContext().firestore();
  const stranger = testEnv.authenticatedContext(STRANGER_UID).firestore();

  await assertFails(unauth.collection("usuarios_campana").doc(LIDER_UID).get());
  await assertFails(unauth.collection("simpatizantes").doc("simp_1").get());
  await assertFails(stranger.collection("simpatizantes").doc("simp_1").get());
  await assertFails(stranger.collection("simpatizantes").get());
});

test("Team member: lider can create & list own simpatizantes with Habeas Data; Admin can manage config & CMS", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("usuarios_campana").doc(LIDER_UID).set({
      uid: LIDER_UID,
      nombre: "Líder Monterrey",
      rol: "LIDER_VEREDAL",
      municipio_id: "85162",
      vereda_id: "ver_85162_1",
      activo: true,
      created_at: new Date(Date.now() - 10000),
    });
  });

  const liderDb = testEnv.authenticatedContext(LIDER_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    liderDb.collection("simpatizantes").doc("simp_lider_1").set({
      nombre: "María Rodríguez",
      cedula: "47432100",
      telefono: "3124445566",
      municipio_id: "85162",
      vereda_id: "ver_85162_1",
      puesto_votacion: "Col. Normal Superior",
      mesa: "4",
      lider_uid: LIDER_UID,
      lider_nombre: "Líder Monterrey",
      authorized_uids: [LIDER_UID],
      acepto_habeas_data: true,
      fecha_habeas_data: now,
      created_at: now,
    })
  );

  // Reject Habeas Data = false
  await assertFails(
    liderDb.collection("simpatizantes").doc("simp_lider_2").set({
      nombre: "Sin Permiso",
      cedula: "47432199",
      telefono: "3124445599",
      municipio_id: "85162",
      vereda_id: "ver_85162_1",
      lider_uid: LIDER_UID,
      authorized_uids: [LIDER_UID],
      acepto_habeas_data: false,
      fecha_habeas_data: now,
      created_at: now,
    })
  );

  await assertSucceeds(
    liderDb.collection("simpatizantes").where("authorized_uids", "array-contains", LIDER_UID).get()
  );

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("configuracion_campana").doc("general").set({
      nombre_candidato: "Candidato Casanare",
      eslogan: "Compromiso con el Llano",
      color_primario: "#0B6E4F",
      color_secundario: "#D97706",
      updated_at: now,
    })
  );
});
