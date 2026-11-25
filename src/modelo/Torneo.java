package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.EquipoDuplicadoException;
import excepciones.ResultadoInvalidoException;
import excepciones.TorneoException;

// Modelo principal: liga "todos contra todos" de e-sports con series al mejor de 3.
// - Observer: es el SUJETO (lista de TorneoListener y notificar()).
// - Strategy: es el CONTEXTO (usa una EstrategiaSimulacion sin saber cual es).
// - Command: es el RECEPTOR (los comandos llaman a sus metodos).
// No importa nada de javax.swing ni imprime en consola: se puede probar sin ventana.
public class Torneo {
	public static final int PUNTOS_VICTORIA = 3;
	public static final int MIN_EQUIPOS = 2;
	public static final int MAX_EQUIPOS = 16;

	// orden de la tabla: puntos, diferencia de mapas, mapas a favor y nombre
	public static final Comparator<Equipo> ORDEN_TABLA = Comparator.comparingInt(Equipo::getPuntos).reversed()
			.thenComparing(Comparator.comparingInt(Equipo::getDiferencia).reversed())
			.thenComparing(Comparator.comparingInt(Equipo::getMapasFavor).reversed())
			.thenComparing(Equipo::getNombre, String.CASE_INSENSITIVE_ORDER);

	private String nombre;
	private final List<Equipo> equipos = new ArrayList<>();
	private final List<Partida> partidas = new ArrayList<>();
	private EstrategiaSimulacion estrategia;
	private final List<TorneoListener> listeners = new ArrayList<>();
	private String mensaje = "Agrega equipos para armar el calendario.";

	public Torneo(String nombre, EstrategiaSimulacion estrategia) {
		if (estrategia == null) {
			throw new IllegalArgumentException("La estrategia de simulación es obligatoria");
		}
		this.nombre = nombre;
		this.estrategia = estrategia;
	}

	// ===================== Observer =====================

	public void addListener(TorneoListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(TorneoListener listener) {
		listeners.remove(listener);
	}

	public int getCantidadListeners() {
		return listeners.size();
	}

	// recorre una copia: un observador puede darse de baja sin romper el recorrido
	private void notificar() {
		for (TorneoListener listener : new ArrayList<>(listeners)) {
			listener.torneoActualizado(this);
		}
	}

	// ===================== Strategy =====================

	public void setEstrategia(EstrategiaSimulacion nueva) {
		if (nueva == null) {
			throw new IllegalArgumentException("La estrategia no puede ser null");
		}
		this.estrategia = nueva;
		mensaje = "Estrategia de simulación: " + nueva.getNombre();
		notificar();
	}

	public EstrategiaSimulacion getEstrategia() {
		return estrategia;
	}

	// la estrategia decide el resultado; este metodo NO cambia el estado del torneo
	public int[] calcularResultado(Partida partida) {
		verificarPartida(partida);
		return estrategia.simular(partida.getLocal(), partida.getVisitante());
	}

	// ===================== Equipos =====================

	public void agregarEquipo(Equipo equipo) throws TorneoException {
		agregarEquipo(equipos.size(), equipo);
	}

	// la posicion sirve para que "deshacer eliminar" deje al equipo donde estaba
	public void agregarEquipo(int posicion, Equipo equipo) throws TorneoException {
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo no puede ser null");
		}
		verificarSinResultados("agregar equipos");
		if (equipos.size() >= MAX_EQUIPOS) {
			throw new TorneoException("El torneo admite como máximo " + MAX_EQUIPOS + " equipos.");
		}
		if (buscarEquipo(equipo.getNombre()) != null) {
			throw new EquipoDuplicadoException(equipo.getNombre());
		}
		int pos = Math.max(0, Math.min(posicion, equipos.size()));
		equipos.add(pos, equipo);
		generarCalendario();
		mensaje = "Equipo agregado: " + equipo.getNombre() + " (nivel " + equipo.getNivel() + ")";
		notificar();
	}

	// devuelve la posicion que tenia el equipo
	public int quitarEquipo(Equipo equipo) throws TorneoException {
		verificarSinResultados("eliminar equipos");
		int pos = equipos.indexOf(equipo);
		if (pos < 0) {
			throw new TorneoException("El equipo no pertenece al torneo.");
		}
		equipos.remove(pos);
		generarCalendario();
		mensaje = "Equipo eliminado: " + equipo.getNombre();
		notificar();
		return pos;
	}

	public Equipo buscarEquipo(String nombreEquipo) {
		for (Equipo e : equipos) {
			if (e.getNombre().equalsIgnoreCase(nombreEquipo.trim())) {
				return e;
			}
		}
		return null;
	}

	private void verificarSinResultados(String accion) throws TorneoException {
		if (hayResultados()) {
			throw new TorneoException("No se puede " + accion + " cuando ya hay partidas jugadas.\n"
					+ "Deshaz las simulaciones o usa Torneo > Reiniciar resultados.");
		}
	}

	// ===================== Resultados =====================

	// metodo del diagrama: la estrategia calcula y el torneo registra
	public int[] simular(Partida partida) throws TorneoException {
		int[] resultado = calcularResultado(partida);
		registrarResultado(partida, resultado);
		return resultado;
	}

	public void registrarResultado(Partida partida, int[] resultado) throws TorneoException {
		verificarPartida(partida);
		if (partida.isJugada()) {
			throw new TorneoException("La partida ya tiene resultado: " + partida);
		}
		if (resultado == null || resultado.length != 2) {
			throw new ResultadoInvalidoException("El resultado debe tener los mapas de los dos equipos.");
		}
		Validador.resultado(resultado[0], resultado[1]);
		partida.setResultado(resultado);
		recalcular();
		Equipo campeon = getCampeon();
		if (campeon != null) {
			mensaje = "¡" + campeon.getNombre() + " es campeón del torneo con " + campeon.getPuntos() + " puntos!";
		} else {
			mensaje = "Jornada " + partida.getJornada() + ": " + partida;
		}
		notificar();
	}

	public void anularResultado(Partida partida) {
		verificarPartida(partida);
		if (!partida.isJugada()) {
			return;
		}
		partida.setResultado(null);
		recalcular();
		mensaje = "Resultado anulado: " + partida.getLocal() + " vs " + partida.getVisitante();
		notificar();
	}

	// borra todos los resultados y devuelve una copia para poder restaurarlos (deshacer)
	public Map<Partida, int[]> reiniciarResultados() {
		Map<Partida, int[]> respaldo = new LinkedHashMap<>();
		for (Partida p : partidas) {
			if (p.isJugada()) {
				respaldo.put(p, p.getResultado());
				p.setResultado(null);
			}
		}
		recalcular();
		mensaje = "Resultados reiniciados: " + respaldo.size() + " partidas vuelven a estar pendientes.";
		notificar();
		return respaldo;
	}

	public void restaurarResultados(Map<Partida, int[]> respaldo) {
		for (Map.Entry<Partida, int[]> entrada : respaldo.entrySet()) {
			if (partidas.contains(entrada.getKey())) {
				entrada.getKey().setResultado(entrada.getValue());
			}
		}
		recalcular();
		mensaje = "Se restauraron " + respaldo.size() + " resultados.";
		notificar();
	}

	// ===================== Estado completo (nuevo torneo y carga desde la BD) =====================

	public void restaurar(String nuevoNombre, EstrategiaSimulacion nuevaEstrategia, List<Equipo> nuevosEquipos,
			List<Partida> nuevasPartidas) {
		this.nombre = nuevoNombre;
		if (nuevaEstrategia != null) {
			this.estrategia = nuevaEstrategia;
		}
		equipos.clear();
		equipos.addAll(nuevosEquipos);
		partidas.clear();
		partidas.addAll(nuevasPartidas);
		recalcular();
		mensaje = "Torneo \"" + nombre + "\": " + equipos.size() + " equipos y " + partidas.size() + " partidas.";
		notificar();
	}

	public void setNombre(String nuevoNombre) {
		this.nombre = nuevoNombre;
		mensaje = "El torneo ahora se llama \"" + nuevoNombre + "\".";
		notificar();
	}

	// ===================== Consultas =====================

	public String getNombre() {
		return nombre;
	}

	public String getMensaje() {
		return mensaje;
	}

	public List<Equipo> getEquipos() {
		return Collections.unmodifiableList(equipos);
	}

	public List<Partida> getPartidas() {
		return Collections.unmodifiableList(partidas);
	}

	// tabla de posiciones ordenada
	public List<Equipo> getTabla() {
		List<Equipo> tabla = new ArrayList<>(equipos);
		tabla.sort(ORDEN_TABLA);
		return tabla;
	}

	public int getPosicion(Equipo equipo) {
		return getTabla().indexOf(equipo) + 1;
	}

	public int getPartidasJugadas() {
		int jugadas = 0;
		for (Partida p : partidas) {
			if (p.isJugada()) {
				jugadas++;
			}
		}
		return jugadas;
	}

	public boolean hayResultados() {
		return getPartidasJugadas() > 0;
	}

	public boolean hayPendientes() {
		return getSiguientePendiente() != null;
	}

	public List<Partida> getPendientes() {
		List<Partida> pendientes = new ArrayList<>();
		for (Partida p : partidas) {
			if (!p.isJugada()) {
				pendientes.add(p);
			}
		}
		return pendientes;
	}

	public Partida getSiguientePendiente() {
		for (Partida p : partidas) {
			if (!p.isJugada()) {
				return p;
			}
		}
		return null;
	}

	// jornada de la siguiente partida pendiente (0 si no queda ninguna)
	public int getJornadaActual() {
		Partida siguiente = getSiguientePendiente();
		return siguiente == null ? 0 : siguiente.getJornada();
	}

	public List<Partida> getPendientesDeJornada(int jornada) {
		List<Partida> lista = new ArrayList<>();
		for (Partida p : partidas) {
			if (p.getJornada() == jornada && !p.isJugada()) {
				lista.add(p);
			}
		}
		return lista;
	}

	public int getTotalJornadas() {
		int maximo = 0;
		for (Partida p : partidas) {
			maximo = Math.max(maximo, p.getJornada());
		}
		return maximo;
	}

	public boolean isTerminado() {
		return !partidas.isEmpty() && !hayPendientes();
	}

	public Equipo getCampeon() {
		return isTerminado() ? getTabla().get(0) : null;
	}

	// ===================== Internos =====================

	private void verificarPartida(Partida partida) {
		if (partida == null || !partidas.contains(partida)) {
			throw new IllegalArgumentException("La partida no pertenece al torneo");
		}
	}

	// recalcula las estadisticas de todos los equipos a partir de las partidas jugadas
	private void recalcular() {
		for (Equipo e : equipos) {
			e.reiniciarEstadisticas();
		}
		for (Partida p : partidas) {
			if (p.isJugada()) {
				int[] r = p.getResultado();
				p.getLocal().sumarSerie(r[0], r[1]);
				p.getVisitante().sumarSerie(r[1], r[0]);
			}
		}
	}

	// calendario "todos contra todos" con el metodo del circulo:
	// un equipo queda fijo y los demas rotan; si son impares, uno descansa cada jornada
	private void generarCalendario() {
		partidas.clear();
		List<Equipo> rueda = new ArrayList<>(equipos);
		if (rueda.size() >= MIN_EQUIPOS) {
			if (rueda.size() % 2 != 0) {
				rueda.add(null); // null = descanso
			}
			int n = rueda.size();
			for (int jornada = 1; jornada < n; jornada++) {
				for (int i = 0; i < n / 2; i++) {
					Equipo a = rueda.get(i);
					Equipo b = rueda.get(n - 1 - i);
					if (a != null && b != null) {
						// se alterna la localia para que sea mas justo
						partidas.add(jornada % 2 == 1 ? new Partida(jornada, a, b) : new Partida(jornada, b, a));
					}
				}
				rueda.add(1, rueda.remove(n - 1)); // rota todos menos el primero
			}
		}
		recalcular();
	}
}
