package controlador;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import excepciones.PersistenciaException;
import excepciones.TorneoException;
import modelo.Campeon;
import modelo.Equipo;
import modelo.EstrategiaSimulacion;
import modelo.Partida;
import modelo.Torneo;
import modelo.Validador;
import persistencia.ExportadorCsv;
import persistencia.TorneoDAO;
import vista.DialogoEquipo;
import vista.VentanaPrincipal;

// Controlador (MVC): recibe las acciones de la vista, crea los comandos, usa el DAO
// y convierte las excepciones en mensajes para el usuario. No dibuja nada ni tiene reglas de negocio.
public class TorneoController {
	// equipos de ejemplo para probar rapido (nombres inventados)
	private static final String[] NOMBRES_EJEMPLO = { "Andes Titans", "Misti Wolves", "Inca Phoenix",
			"Colca Condors", "Chachani Raiders", "Lima Vipers", "Cusco Knights", "Titicaca Sharks" };
	private static final int[] NIVELES_EJEMPLO = { 88, 81, 76, 70, 64, 58, 52, 45 };

	private final Torneo torneo;
	private final VentanaPrincipal vista;
	private final TorneoDAO dao;
	private final List<EstrategiaSimulacion> estrategias;
	private final HistorialComandos historial = new HistorialComandos();

	private boolean baseDisponible;
	private boolean cambiosSinGuardar;
	private SimulacionWorker worker;
	private final List<Comando> loteSimulado = new ArrayList<>();

	public TorneoController(Torneo torneo, VentanaPrincipal vista, TorneoDAO dao,
			List<EstrategiaSimulacion> estrategias) {
		this.torneo = torneo;
		this.vista = vista;
		this.dao = dao;
		this.estrategias = estrategias;
	}

	// conecta la vista, abre la base de datos y carga el ultimo torneo guardado
	public void iniciar() {
		vista.conectar(this);
		vista.setVisible(true);
		try {
			dao.conectar();
			baseDisponible = true;
			if (dao.hayTorneoGuardado()) {
				dao.cargar(torneo, estrategias);
				vista.mostrarAviso("Se cargó el último torneo guardado.");
			}
			actualizarCampeones();
		} catch (PersistenciaException e) {
			baseDisponible = false;
			vista.mostrarError(e.getMessage() + "\n\nEl programa funciona igual, pero no podrá guardar ni cargar.");
		}
		vista.setBaseDisponible(baseDisponible);
		actualizarHistorial();
	}

	public List<EstrategiaSimulacion> getEstrategias() {
		return estrategias;
	}

	// ===================== Equipos =====================

	// abre el dialogo; si los datos son invalidos muestra el error y lo vuelve a abrir con lo escrito
	public void agregarEquipo() {
		DialogoEquipo dialogo = vista.crearDialogoEquipo();
		while (dialogo.mostrar()) {
			try {
				agregarEquipo(dialogo.getNombre(), dialogo.getNivel());
				return;
			} catch (TorneoException e) {
				vista.mostrarError(e.getMessage());
			}
		}
	}

	// metodo del diagrama: valida y ejecuta el comando
	public void agregarEquipo(String nombre, int nivel) throws TorneoException {
		String nombreLimpio = Validador.nombreEquipo(nombre);
		int nivelValido = Validador.nivel(nivel);
		ejecutar(new AgregarEquipoCmd(torneo, new Equipo(nombreLimpio, nivelValido)));
	}

	public void agregarEquiposDeEjemplo() {
		List<Comando> comandos = new ArrayList<>();
		int espacio = Torneo.MAX_EQUIPOS - torneo.getEquipos().size();
		for (int i = 0; i < NOMBRES_EJEMPLO.length && comandos.size() < espacio; i++) {
			if (torneo.buscarEquipo(NOMBRES_EJEMPLO[i]) == null) {
				comandos.add(new AgregarEquipoCmd(torneo, new Equipo(NOMBRES_EJEMPLO[i], NIVELES_EJEMPLO[i])));
			}
		}
		if (comandos.isEmpty()) {
			vista.mostrarInfo("Los equipos de ejemplo ya están en el torneo (o no hay espacio).");
			return;
		}
		try {
			ejecutar(new ComandoCompuesto("Agregar " + comandos.size() + " equipos de ejemplo", comandos));
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void eliminarEquipo() {
		if (torneo.getEquipos().isEmpty()) {
			vista.mostrarInfo("No hay equipos para eliminar.");
			return;
		}
		Equipo equipo = vista.elegirEquipo(torneo.getEquipos(), vista.getEquipoSeleccionado());
		if (equipo == null) {
			return; // cancelo
		}
		try {
			ejecutar(new EliminarEquipoCmd(torneo, equipo));
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	// ===================== Simulacion =====================

	public void simularSiguiente() {
		Partida partida = torneo.getSiguientePendiente();
		if (partida == null) {
			vista.mostrarInfo("No quedan partidas pendientes.");
			return;
		}
		try {
			ejecutar(new SimularPartidaCmd(torneo, partida));
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void simularJornada() {
		int jornada = torneo.getJornadaActual();
		if (jornada == 0) {
			vista.mostrarInfo("No quedan partidas pendientes.");
			return;
		}
		List<Comando> comandos = new ArrayList<>();
		for (Partida p : torneo.getPendientesDeJornada(jornada)) {
			comandos.add(new SimularPartidaCmd(torneo, p));
		}
		try {
			ejecutar(new ComandoCompuesto("Simular jornada " + jornada, comandos));
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	// metodo del diagrama: simula todo lo pendiente con un SwingWorker y barra de progreso
	public void simularTodo() {
		if (worker != null) {
			return; // ya hay una simulacion en curso
		}
		List<Partida> pendientes = torneo.getPendientes();
		if (pendientes.isEmpty()) {
			vista.mostrarInfo("No quedan partidas pendientes.");
			return;
		}
		loteSimulado.clear();
		int pausa = Math.max(25, 3000 / pendientes.size()); // la simulacion dura unos 3 segundos
		worker = new SimulacionWorker(torneo, pendientes, this, pausa);
		worker.addPropertyChangeListener(evento -> {
			if ("progress".equals(evento.getPropertyName())) {
				vista.setProgreso((Integer) evento.getNewValue());
			}
		});
		vista.setSimulando(true);
		worker.execute();
	}

	public void cancelarSimulacion() {
		if (worker != null) {
			worker.cancel(true);
		}
	}

	// lo llama el worker desde process() (ya estamos en el EDT)
	void aplicarSimulado(SimularPartidaCmd comando) {
		try {
			comando.ejecutar();
			loteSimulado.add(comando);
		} catch (TorneoException e) {
			// la partida ya tenia resultado: se ignora
		}
	}

	// lo llama el worker desde done(): todo lo simulado queda como UN solo comando (un Ctrl+Z)
	void simulacionTerminada(boolean cancelada) {
		worker = null;
		vista.setSimulando(false);
		if (!loteSimulado.isEmpty()) {
			historial.registrarEjecutado(
					new ComandoCompuesto("Simular " + loteSimulado.size() + " partidas", loteSimulado));
			loteSimulado.clear();
			cambiosSinGuardar = true;
		}
		actualizarHistorial();
		if (cancelada) {
			vista.mostrarAviso("Simulación cancelada: se conservan las partidas ya jugadas.");
		} else if (torneo.isTerminado()) {
			celebrarCampeon();
		}
	}

	// registrar a mano el resultado de una partida (doble clic en la pestaña Partidos)
	public void registrarResultado(Partida partida) {
		if (worker != null) {
			return;
		}
		if (partida.isJugada()) {
			vista.mostrarInfo("Esa partida ya se jugó: " + partida + "\nUsa Deshacer si quieres cambiarla.");
			return;
		}
		int[] resultado = vista.pedirResultado(partida, null);
		while (resultado != null) {
			try {
				Validador.resultado(resultado[0], resultado[1]);
				ejecutar(new SimularPartidaCmd(torneo, partida, resultado, true));
				return;
			} catch (TorneoException e) {
				vista.mostrarError(e.getMessage());
				resultado = vista.pedirResultado(partida, resultado);
			}
		}
	}

	public void reiniciarResultados() {
		if (!torneo.hayResultados()) {
			vista.mostrarInfo("Todavía no hay resultados.");
			return;
		}
		if (vista.confirmar("Todas las partidas volverán a estar pendientes (se puede deshacer).\n¿Continuar?")) {
			try {
				ejecutar(new ReiniciarResultadosCmd(torneo));
			} catch (TorneoException e) {
				vista.mostrarError(e.getMessage());
			}
		}
	}

	// ===================== Deshacer / Rehacer =====================

	public void deshacer() {
		if (worker != null || !historial.puedeDeshacer()) {
			return;
		}
		try {
			Comando comando = historial.deshacer();
			cambiosSinGuardar = true;
			vista.mostrarAviso("Deshecho: " + comando.getDescripcion());
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
		actualizarHistorial();
	}

	public void rehacer() {
		if (worker != null || !historial.puedeRehacer()) {
			return;
		}
		try {
			Comando comando = historial.rehacer();
			cambiosSinGuardar = true;
			vista.mostrarAviso("Rehecho: " + comando.getDescripcion());
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
		actualizarHistorial();
	}

	// ===================== Torneo =====================

	public void cambiarEstrategia(EstrategiaSimulacion estrategia) {
		if (estrategia == null || estrategia == torneo.getEstrategia() || worker != null) {
			return;
		}
		torneo.setEstrategia(estrategia);
		cambiosSinGuardar = true;
		actualizarHistorial();
	}

	public void renombrarTorneo() {
		String texto = vista.pedirTexto("Renombrar torneo", "Nombre del torneo:", torneo.getNombre());
		if (texto == null) {
			return;
		}
		try {
			torneo.setNombre(Validador.nombreTorneo(texto));
			cambiosSinGuardar = true;
			actualizarHistorial();
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void nuevoTorneo() {
		String texto = vista.pedirTexto("Nuevo torneo", "Nombre del nuevo torneo:", "Liga LP3");
		if (texto == null) {
			return;
		}
		try {
			ejecutar(new NuevoTorneoCmd(torneo, Validador.nombreTorneo(texto)));
			vista.mostrarAviso("Torneo nuevo. Ctrl+Z recupera el anterior.");
		} catch (TorneoException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	// ===================== Persistencia =====================

	public void guardar() {
		if (!verificarBase()) {
			return;
		}
		try {
			dao.guardar(torneo);
			cambiosSinGuardar = false;
			actualizarHistorial();
			vista.mostrarAviso("Guardado en torneo.db: " + torneo.getEquipos().size() + " equipos, "
					+ torneo.getPartidas().size() + " partidas.");
		} catch (PersistenciaException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void cargar() {
		if (!verificarBase()) {
			return;
		}
		if (cambiosSinGuardar && !vista.confirmar("Hay cambios sin guardar que se perderán.\n¿Cargar de todos modos?")) {
			return;
		}
		try {
			if (!dao.cargar(torneo, estrategias)) {
				vista.mostrarInfo("Todavía no hay ningún torneo guardado.");
				return;
			}
			historial.limpiar(); // lo cargado es el nuevo punto de partida
			cambiosSinGuardar = false;
			actualizarHistorial();
			vista.mostrarAviso("Torneo cargado desde torneo.db.");
		} catch (PersistenciaException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void exportarCsv() {
		File archivo = vista.elegirArchivoCsv(torneo.getNombre().replaceAll("[^\\p{L}0-9]+", "_") + ".csv");
		if (archivo == null) {
			return; // cancelo
		}
		try {
			ExportadorCsv.exportar(torneo, archivo);
			vista.mostrarAviso("Exportado a " + archivo.getName());
		} catch (IOException e) {
			vista.mostrarError("No se pudo exportar: " + e.getMessage());
		}
	}

	public void registrarCampeon() {
		Equipo campeon = torneo.getCampeon();
		if (campeon == null) {
			vista.mostrarInfo("El torneo todavía no termina.");
			return;
		}
		if (!verificarBase()) {
			return;
		}
		try {
			dao.guardarCampeon(new Campeon(torneo.getNombre(), campeon.getNombre(), campeon.getPuntos(),
					torneo.getEstrategia().getNombre(), LocalDateTime.now()));
			actualizarCampeones();
			vista.mostrarPestanaCampeones();
			vista.mostrarAviso(campeon.getNombre() + " entró al Salón de la Fama.");
		} catch (PersistenciaException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void actualizarCampeones() {
		if (!baseDisponible) {
			return;
		}
		try {
			vista.mostrarCampeones(dao.listarCampeones(), dao.contarTitulos());
		} catch (PersistenciaException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	public void vaciarCampeones() {
		if (!verificarBase()) {
			return;
		}
		if (!vista.confirmar("Se borrarán todos los campeones registrados. Esto NO se puede deshacer.\n¿Continuar?")) {
			return;
		}
		try {
			int borrados = dao.borrarCampeones();
			actualizarCampeones();
			vista.mostrarAviso("Se borraron " + borrados + " registros del Salón de la Fama.");
		} catch (PersistenciaException e) {
			vista.mostrarError(e.getMessage());
		}
	}

	// pregunta si hay cambios sin guardar y cierra la conexion
	public void salir() {
		if (cambiosSinGuardar && baseDisponible) {
			int respuesta = vista.preguntarGuardarAntesDeSalir();
			if (respuesta == VentanaPrincipal.CANCELAR) {
				return;
			}
			if (respuesta == VentanaPrincipal.GUARDAR) {
				guardar();
				if (cambiosSinGuardar) {
					return; // no se pudo guardar: no se cierra
				}
			}
		}
		if (worker != null) {
			worker.cancel(true);
		}
		dao.cerrar();
		vista.dispose();
		System.exit(0);
	}

	// ===================== Ayudantes =====================

	// todo cambio que se pueda deshacer pasa por aqui
	private void ejecutar(Comando comando) throws TorneoException {
		boolean estabaTerminado = torneo.isTerminado();
		historial.ejecutar(comando);
		cambiosSinGuardar = true;
		actualizarHistorial();
		if (!estabaTerminado && torneo.isTerminado()) {
			celebrarCampeon();
		}
	}

	private void celebrarCampeon() {
		Equipo campeon = torneo.getCampeon();
		vista.celebrarCampeon(campeon);
		if (baseDisponible && vista.confirmar("¡" + campeon.getNombre() + " es el campeón de " + torneo.getNombre()
				+ " con " + campeon.getPuntos() + " puntos!\n\n¿Registrarlo en el Salón de la Fama?")) {
			registrarCampeon();
		}
	}

	private boolean verificarBase() {
		if (!baseDisponible) {
			vista.mostrarError("La base de datos no está disponible (revisa que el .jar de SQLite esté en el Build Path).");
		}
		return baseDisponible;
	}

	private void actualizarHistorial() {
		vista.setHistorial(historial.puedeDeshacer(), historial.getDescripcionDeshacer(), historial.puedeRehacer(),
				historial.getDescripcionRehacer());
		vista.setCambiosSinGuardar(cambiosSinGuardar);
	}
}
