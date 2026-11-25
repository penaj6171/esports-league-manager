package pruebas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import controlador.AgregarEquipoCmd;
import controlador.ComandoCompuesto;
import controlador.Comando;
import controlador.HistorialComandos;
import controlador.ReiniciarResultadosCmd;
import controlador.SimularPartidaCmd;
import excepciones.EquipoDuplicadoException;
import excepciones.EquipoInvalidoException;
import excepciones.PersistenciaException;
import excepciones.ResultadoInvalidoException;
import excepciones.TorneoException;
import modelo.Campeon;
import modelo.Equipo;
import modelo.EstrategiaSimulacion;
import modelo.Partida;
import modelo.SimulacionAleatoria;
import modelo.SimulacionPorNivel;
import modelo.SimulacionPorRacha;
import modelo.Torneo;
import modelo.Validador;
import persistencia.TorneoDAO;

// Pruebas del modelo, los patrones y el DAO SIN abrir ninguna ventana.
// No importa nada del paquete vista: demuestra que el modelo es independiente de Swing.
// Ejecutar: clic derecho > Run As > Java Application. Debe terminar con "X de X pruebas correctas".
public class PruebaModelo {
	private static int total;
	private static int correctas;

	public static void main(String[] args) throws Exception {
		probarCalendario();
		probarObserver();
		probarStrategy();
		probarCommand();
		probarValidacion();
		probarDao();
		System.out.println();
		System.out.println(correctas + " de " + total + " pruebas correctas");
		if (correctas != total) {
			System.exit(1);
		}
	}

	private static void verificar(String descripcion, boolean condicion) {
		total++;
		if (condicion) {
			correctas++;
		}
		System.out.println("  " + (condicion ? "OK   " : "FALLA") + " " + descripcion);
	}

	private static Torneo torneoCon(int cantidad, EstrategiaSimulacion estrategia) throws TorneoException {
		Torneo t = new Torneo("Prueba", estrategia);
		for (int i = 1; i <= cantidad; i++) {
			t.agregarEquipo(new Equipo("Equipo " + i, 10 * i));
		}
		return t;
	}

	private static void probarCalendario() throws TorneoException {
		System.out.println("== Calendario todos contra todos ==");
		for (int n : new int[] { 4, 5, 8 }) {
			Torneo t = torneoCon(n, new SimulacionAleatoria(new Random(1)));
			verificar(n + " equipos generan " + (n * (n - 1) / 2) + " partidas", t.getPartidas().size() == n * (n - 1) / 2);
			boolean nadieDosVeces = true;
			for (int j = 1; j <= t.getTotalJornadas(); j++) {
				Set<Equipo> vistos = new HashSet<>();
				for (Partida p : t.getPartidas()) {
					if (p.getJornada() == j && (!vistos.add(p.getLocal()) || !vistos.add(p.getVisitante()))) {
						nadieDosVeces = false;
					}
				}
			}
			verificar(n + " equipos: nadie juega dos veces en la misma jornada", nadieDosVeces);
			Set<String> cruces = new HashSet<>();
			for (Partida p : t.getPartidas()) {
				String a = p.getLocal().getNombre();
				String b = p.getVisitante().getNombre();
				cruces.add(a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a);
			}
			verificar(n + " equipos: cada cruce aparece una sola vez", cruces.size() == t.getPartidas().size());
		}
	}

	private static void probarObserver() throws TorneoException {
		System.out.println("== Observer ==");
		Torneo t = torneoCon(4, new SimulacionPorNivel(new Random(7)));
		int[] avisosA = { 0 };
		int[] avisosB = { 0 };
		modelo.TorneoListener a = torneo -> avisosA[0]++;
		modelo.TorneoListener b = torneo -> avisosB[0]++;
		t.addListener(a);
		t.addListener(b);
		t.simular(t.getSiguientePendiente());
		verificar("Dos observadores reciben el aviso al simular", avisosA[0] == 1 && avisosB[0] == 1);
		t.setEstrategia(new SimulacionAleatoria());
		verificar("Cambiar de estrategia también notifica", avisosA[0] == 2);
		t.removeListener(b);
		t.simular(t.getSiguientePendiente());
		verificar("Un observador quitado ya no recibe avisos", avisosA[0] == 3 && avisosB[0] == 2);
	}

	private static void probarStrategy() throws TorneoException {
		System.out.println("== Strategy ==");
		Equipo fuerte = new Equipo("Fuerte", 100);
		Equipo debil = new Equipo("Debil", 5);
		int ganaFuerte = contarVictorias(new SimulacionPorNivel(new Random(42)), fuerte, debil, 2000);
		int ganaAzar = contarVictorias(new SimulacionAleatoria(new Random(42)), fuerte, debil, 2000);
		int ganaRacha = contarVictorias(new SimulacionPorRacha(new Random(42)), fuerte, debil, 2000);
		System.out.printf("       nivel 100 vs 5 gana: Por nivel %.1f %% | Aleatoria %.1f %% | Nivel + racha %.1f %%%n",
				ganaFuerte / 20.0, ganaAzar / 20.0, ganaRacha / 20.0);
		verificar("Por nivel: el equipo fuerte gana más del 90 %", ganaFuerte > 1800);
		verificar("Aleatoria: el resultado ronda el 50 %", ganaAzar > 900 && ganaAzar < 1100);
		verificar("Nivel + racha: el fuerte también es favorito", ganaRacha > 1600);
		boolean todosValidos = true;
		EstrategiaSimulacion[] todas = { new SimulacionPorNivel(), new SimulacionAleatoria(), new SimulacionPorRacha() };
		for (EstrategiaSimulacion e : todas) {
			for (int i = 0; i < 300; i++) {
				int[] r = e.simular(fuerte, debil);
				try {
					Validador.resultado(r[0], r[1]);
				} catch (ResultadoInvalidoException ex) {
					todosValidos = false;
				}
			}
		}
		verificar("Las 3 estrategias siempre dan un Bo3 válido (2-0, 2-1, 1-2, 0-2)", todosValidos);
	}

	private static int contarVictorias(EstrategiaSimulacion e, Equipo local, Equipo visitante, int veces) {
		int ganadas = 0;
		for (int i = 0; i < veces; i++) {
			int[] r = e.simular(local, visitante);
			if (r[0] > r[1]) {
				ganadas++;
			}
		}
		return ganadas;
	}

	private static void probarCommand() throws TorneoException {
		System.out.println("== Command (deshacer / rehacer) ==");
		Torneo t = torneoCon(4, new SimulacionAleatoria(new Random(3)));
		HistorialComandos historial = new HistorialComandos();

		historial.ejecutar(new AgregarEquipoCmd(t, new Equipo("Nuevo", 50)));
		verificar("Agregar equipo: 5 equipos y 10 partidas", t.getEquipos().size() == 5 && t.getPartidas().size() == 10);
		historial.deshacer();
		verificar("Deshacer agregar: vuelven 4 equipos y 6 partidas", t.getEquipos().size() == 4 && t.getPartidas().size() == 6);
		historial.rehacer();
		historial.deshacer();

		Partida p = t.getSiguientePendiente();
		historial.ejecutar(new SimularPartidaCmd(t, p));
		String marcador = p.getMarcador();
		historial.deshacer();
		verificar("Deshacer simular deja la partida pendiente", !p.isJugada() && !t.hayResultados());
		historial.rehacer();
		verificar("Rehacer repite el MISMO marcador (no vuelve a sortear)", p.getMarcador().equals(marcador));

		historial.deshacer();
		historial.ejecutar(new SimularPartidaCmd(t, t.getPartidas().get(1)));
		verificar("Una acción nueva borra lo que se podía rehacer", !historial.puedeRehacer());

		List<Comando> jornada = new ArrayList<>();
		for (Partida q : t.getPendientes()) {
			jornada.add(new SimularPartidaCmd(t, q));
		}
		historial.ejecutar(new ComandoCompuesto("todo", jornada));
		verificar("Comando compuesto: el torneo termina con campeón", t.isTerminado() && t.getCampeon() != null);
		historial.deshacer();
		verificar("Un solo deshacer revierte todo el comando compuesto", t.getPartidasJugadas() == 1);

		historial.rehacer();
		Map<String, Integer> puntosAntes = puntos(t);
		historial.ejecutar(new ReiniciarResultadosCmd(t));
		verificar("Reiniciar resultados deja todo pendiente", !t.hayResultados());
		historial.deshacer();
		verificar("Deshacer el reinicio recupera la misma tabla", puntos(t).equals(puntosAntes));

		try {
			t.agregarEquipo(new Equipo("Tarde", 40));
			verificar("No se puede agregar equipos con partidas jugadas", false);
		} catch (TorneoException e) {
			verificar("No se puede agregar equipos con partidas jugadas", true);
		}
	}

	private static Map<String, Integer> puntos(Torneo t) {
		Map<String, Integer> mapa = new java.util.HashMap<>();
		for (Equipo e : t.getEquipos()) {
			mapa.put(e.getNombre(), e.getPuntos());
		}
		return mapa;
	}

	private static void probarValidacion() throws TorneoException {
		System.out.println("== Validación y excepciones propias ==");
		verificar("Rechaza un nombre vacío", lanza(() -> Validador.nombreEquipo("   "), EquipoInvalidoException.class));
		verificar("Rechaza un nombre de 21 caracteres",
				lanza(() -> Validador.nombreEquipo("NombreDemasiadoLargo1"), EquipoInvalidoException.class));
		verificar("Rechaza símbolos raros", lanza(() -> Validador.nombreEquipo("Team<script>"), EquipoInvalidoException.class));
		verificar("Limpia espacios: '  Lima   Vipers ' -> 'Lima Vipers'",
				Validador.nombreEquipo("  Lima   Vipers ").equals("Lima Vipers"));
		verificar("Rechaza nivel 0 y 101", lanza(() -> Validador.nivel(0), EquipoInvalidoException.class)
				&& lanza(() -> Validador.nivel(101), EquipoInvalidoException.class));
		verificar("Rechaza el resultado 1-1 y 2-2", lanza(() -> Validador.resultado(1, 1), ResultadoInvalidoException.class)
				&& lanza(() -> Validador.resultado(2, 2), ResultadoInvalidoException.class));
		Torneo t = torneoCon(3, new SimulacionAleatoria());
		verificar("Rechaza equipo duplicado (sin importar mayúsculas)",
				lanza(() -> t.agregarEquipo(new Equipo("EQUIPO 1", 30)), EquipoDuplicadoException.class));
	}

	private interface Accion {
		void ejecutar() throws Exception;
	}

	private static boolean lanza(Accion accion, Class<? extends Exception> esperada) {
		try {
			accion.ejecutar();
			return false;
		} catch (Exception e) {
			return esperada.isInstance(e);
		}
	}

	private static void probarDao() throws TorneoException {
		System.out.println("== DAO con SQLite en memoria ==");
		TorneoDAO dao = new TorneoDAO("jdbc:sqlite::memory:");
		try {
			dao.conectar();
		} catch (PersistenciaException e) {
			verificar("Conectar a SQLite (¿está el .jar en el Build Path?)", false);
			return;
		}
		try {
			List<EstrategiaSimulacion> estrategias = List.of(new SimulacionPorNivel(), new SimulacionAleatoria());
			verificar("Base nueva sin torneo guardado", !dao.hayTorneoGuardado());

			Torneo original = torneoCon(6, estrategias.get(1));
			original.simular(original.getPartidas().get(0));
			original.simular(original.getPartidas().get(4));
			dao.guardar(original);

			Torneo cargado = new Torneo("Vacío", estrategias.get(0));
			boolean hubo = dao.cargar(cargado, estrategias);
			verificar("Guardar y cargar: mismos equipos y partidas", hubo
					&& cargado.getEquipos().size() == 6 && cargado.getPartidas().size() == 15);
			verificar("Guardar y cargar: misma tabla de posiciones", puntos(cargado).equals(puntos(original)));
			verificar("Guardar y cargar: misma estrategia", cargado.getEstrategia() == estrategias.get(1));
			verificar("Las partidas pendientes siguen pendientes (NULL en la BD)",
					cargado.getPendientes().size() == original.getPendientes().size());

			dao.guardarCampeon(new Campeon("Liga 1", "Andes Titans", 18, "Por nivel", LocalDateTime.now()));
			dao.guardarCampeon(new Campeon("Liga 2", "Lima Vipers", 15, "Aleatoria", LocalDateTime.now()));
			dao.guardarCampeon(new Campeon("Liga 3", "Andes Titans", 21, "Por nivel", LocalDateTime.now()));
			Map<String, Integer> titulos = dao.contarTitulos();
			verificar("Salón de la fama: 3 campeones guardados", dao.listarCampeones().size() == 3);
			verificar("GROUP BY: Andes Titans primero con 2 títulos",
					titulos.keySet().iterator().next().equals("Andes Titans") && titulos.get("Andes Titans") == 2);
			verificar("Vaciar el salón borra 3 registros", dao.borrarCampeones() == 3);
		} catch (PersistenciaException e) {
			verificar("DAO sin errores: " + e.getMessage(), false);
		} finally {
			dao.cerrar();
		}
	}
}
