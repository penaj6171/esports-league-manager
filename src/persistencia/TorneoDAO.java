package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import excepciones.PersistenciaException;
import modelo.Campeon;
import modelo.Equipo;
import modelo.EstrategiaSimulacion;
import modelo.Partida;
import modelo.Torneo;

// DAO (Data Access Object): todo el SQL del programa vive aqui (Guia 08: SQLite y JDBC).
// - PreparedStatement para todas las consultas con datos.
// - Transaccion (commit / rollback) al guardar el torneo: se guarda todo o nada.
// - Convierte SQLException en PersistenciaException: el resto del programa no conoce JDBC.
public class TorneoDAO {
	private static final String DRIVER = "org.sqlite.JDBC";

	private final String url;
	private Connection conexion;

	// url de ejemplo: "jdbc:sqlite:torneo.db" o "jdbc:sqlite::memory:" para pruebas
	public TorneoDAO(String url) {
		this.url = url;
	}

	public void conectar() throws PersistenciaException {
		try {
			Class.forName(DRIVER); // registra el driver de SQLite
			conexion = DriverManager.getConnection(url);
			crearTablas();
		} catch (ClassNotFoundException e) {
			throw new PersistenciaException("Falta el driver de SQLite.\n"
					+ "Agrega lib/sqlite-jdbc-3.42.0.0.jar al Build Path del proyecto.", e);
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo abrir la base de datos: " + e.getMessage(), e);
		}
	}

	public boolean estaConectado() {
		return conexion != null;
	}

	private void crearTablas() throws SQLException {
		try (Statement st = conexion.createStatement()) {
			st.execute("PRAGMA foreign_keys = ON");
			st.execute("CREATE TABLE IF NOT EXISTS torneo ("
					+ " id INTEGER PRIMARY KEY CHECK (id = 1),"
					+ " nombre TEXT NOT NULL,"
					+ " estrategia TEXT NOT NULL,"
					+ " guardado TEXT NOT NULL)");
			st.execute("CREATE TABLE IF NOT EXISTS equipo ("
					+ " id INTEGER PRIMARY KEY,"
					+ " nombre TEXT NOT NULL UNIQUE,"
					+ " nivel INTEGER NOT NULL CHECK (nivel BETWEEN 1 AND 100))");
			st.execute("CREATE TABLE IF NOT EXISTS partida ("
					+ " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " jornada INTEGER NOT NULL,"
					+ " local_id INTEGER NOT NULL REFERENCES equipo(id),"
					+ " visitante_id INTEGER NOT NULL REFERENCES equipo(id),"
					+ " mapas_local INTEGER,"
					+ " mapas_visitante INTEGER)");
			st.execute("CREATE TABLE IF NOT EXISTS campeon ("
					+ " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " torneo TEXT NOT NULL,"
					+ " equipo TEXT NOT NULL,"
					+ " puntos INTEGER NOT NULL,"
					+ " estrategia TEXT NOT NULL,"
					+ " fecha TEXT NOT NULL)");
		}
	}

	private void verificarConexion() throws PersistenciaException {
		if (conexion == null) {
			throw new PersistenciaException("No hay conexión con la base de datos.", null);
		}
	}

	// ===================== Torneo =====================

	public boolean hayTorneoGuardado() throws PersistenciaException {
		verificarConexion();
		try (Statement st = conexion.createStatement();
				ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM torneo")) {
			return rs.next() && rs.getInt(1) > 0;
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo consultar la base de datos: " + e.getMessage(), e);
		}
	}

	// guarda el torneo completo dentro de UNA transaccion
	public void guardar(Torneo torneo) throws PersistenciaException {
		verificarConexion();
		try {
			conexion.setAutoCommit(false); // empieza la transaccion

			try (Statement st = conexion.createStatement()) {
				st.executeUpdate("DELETE FROM partida");
				st.executeUpdate("DELETE FROM equipo");
				st.executeUpdate("DELETE FROM torneo");
			}

			try (PreparedStatement ps = conexion.prepareStatement(
					"INSERT INTO torneo (id, nombre, estrategia, guardado) VALUES (1, ?, ?, ?)")) {
				ps.setString(1, torneo.getNombre());
				ps.setString(2, torneo.getEstrategia().getNombre());
				ps.setString(3, LocalDateTime.now().toString());
				ps.executeUpdate();
			}

			Map<Equipo, Integer> ids = new HashMap<>();
			try (PreparedStatement ps = conexion.prepareStatement(
					"INSERT INTO equipo (id, nombre, nivel) VALUES (?, ?, ?)")) {
				int id = 1;
				for (Equipo e : torneo.getEquipos()) {
					ps.setInt(1, id);
					ps.setString(2, e.getNombre());
					ps.setInt(3, e.getNivel());
					ps.addBatch();
					ids.put(e, id);
					id++;
				}
				ps.executeBatch();
			}

			try (PreparedStatement ps = conexion.prepareStatement("INSERT INTO partida "
					+ "(jornada, local_id, visitante_id, mapas_local, mapas_visitante) VALUES (?, ?, ?, ?, ?)")) {
				for (Partida p : torneo.getPartidas()) {
					ps.setInt(1, p.getJornada());
					ps.setInt(2, ids.get(p.getLocal()));
					ps.setInt(3, ids.get(p.getVisitante()));
					if (p.isJugada()) {
						ps.setInt(4, p.getResultado()[0]);
						ps.setInt(5, p.getResultado()[1]);
					} else {
						ps.setNull(4, Types.INTEGER); // pendiente
						ps.setNull(5, Types.INTEGER);
					}
					ps.addBatch();
				}
				ps.executeBatch();
			}

			conexion.commit(); // todo salio bien: se confirma
		} catch (SQLException e) {
			try {
				conexion.rollback(); // algo fallo: se deshace todo
			} catch (SQLException ignorada) {
				// si el rollback falla no hay nada mas que hacer
			}
			throw new PersistenciaException("No se pudo guardar el torneo (no se cambió nada): " + e.getMessage(), e);
		} finally {
			try {
				conexion.setAutoCommit(true);
			} catch (SQLException ignorada) {
				// la conexion ya no se puede usar
			}
		}
	}

	// carga el torneo guardado DENTRO del torneo recibido (asi los observadores siguen registrados).
	// Devuelve false si la base de datos no tiene ningun torneo.
	public boolean cargar(Torneo torneo, List<EstrategiaSimulacion> estrategias) throws PersistenciaException {
		verificarConexion();
		try {
			String nombre;
			String nombreEstrategia;
			try (Statement st = conexion.createStatement();
					ResultSet rs = st.executeQuery("SELECT nombre, estrategia FROM torneo WHERE id = 1")) {
				if (!rs.next()) {
					return false;
				}
				nombre = rs.getString("nombre");
				nombreEstrategia = rs.getString("estrategia");
			}

			Map<Integer, Equipo> equiposPorId = new LinkedHashMap<>();
			try (Statement st = conexion.createStatement();
					ResultSet rs = st.executeQuery("SELECT id, nombre, nivel FROM equipo ORDER BY id")) {
				while (rs.next()) {
					equiposPorId.put(rs.getInt("id"), new Equipo(rs.getString("nombre"), rs.getInt("nivel")));
				}
			}

			List<Partida> partidas = new ArrayList<>();
			try (Statement st = conexion.createStatement();
					ResultSet rs = st.executeQuery("SELECT jornada, local_id, visitante_id, mapas_local, "
							+ "mapas_visitante FROM partida ORDER BY id")) {
				while (rs.next()) {
					Equipo local = equiposPorId.get(rs.getInt("local_id"));
					Equipo visitante = equiposPorId.get(rs.getInt("visitante_id"));
					int mapasLocal = rs.getInt("mapas_local");
					boolean pendiente = rs.wasNull(); // NULL en la BD = partida pendiente
					int mapasVisitante = rs.getInt("mapas_visitante");
					int[] resultado = pendiente ? null : new int[] { mapasLocal, mapasVisitante };
					partidas.add(new Partida(rs.getInt("jornada"), local, visitante, resultado));
				}
			}

			EstrategiaSimulacion estrategia = null;
			for (EstrategiaSimulacion e : estrategias) {
				if (e.getNombre().equals(nombreEstrategia)) {
					estrategia = e;
				}
			}
			torneo.restaurar(nombre, estrategia, new ArrayList<>(equiposPorId.values()), partidas);
			return true;
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo cargar el torneo: " + e.getMessage(), e);
		}
	}

	// ===================== Salon de la fama =====================

	public void guardarCampeon(Campeon campeon) throws PersistenciaException {
		verificarConexion();
		String sql = "INSERT INTO campeon (torneo, equipo, puntos, estrategia, fecha) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement ps = conexion.prepareStatement(sql)) {
			ps.setString(1, campeon.getTorneo());
			ps.setString(2, campeon.getEquipo());
			ps.setInt(3, campeon.getPuntos());
			ps.setString(4, campeon.getEstrategia());
			ps.setString(5, campeon.getFecha().toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo registrar el campeón: " + e.getMessage(), e);
		}
	}

	public List<Campeon> listarCampeones() throws PersistenciaException {
		verificarConexion();
		List<Campeon> lista = new ArrayList<>();
		String sql = "SELECT torneo, equipo, puntos, estrategia, fecha FROM campeon ORDER BY id DESC";
		try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(new Campeon(rs.getString("torneo"), rs.getString("equipo"), rs.getInt("puntos"),
						rs.getString("estrategia"), LocalDateTime.parse(rs.getString("fecha"))));
			}
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo leer el Salón de la Fama: " + e.getMessage(), e);
		}
		return lista;
	}

	// cuantos titulos tiene cada equipo, de mayor a menor (GROUP BY)
	public Map<String, Integer> contarTitulos() throws PersistenciaException {
		verificarConexion();
		Map<String, Integer> titulos = new LinkedHashMap<>();
		String sql = "SELECT equipo, COUNT(*) AS titulos FROM campeon GROUP BY equipo ORDER BY titulos DESC, equipo";
		try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				titulos.put(rs.getString("equipo"), rs.getInt("titulos"));
			}
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo contar los títulos: " + e.getMessage(), e);
		}
		return titulos;
	}

	public int borrarCampeones() throws PersistenciaException {
		verificarConexion();
		try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM campeon")) {
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenciaException("No se pudo vaciar el Salón de la Fama: " + e.getMessage(), e);
		}
	}

	public void cerrar() {
		if (conexion != null) {
			try {
				conexion.close();
			} catch (SQLException ignorada) {
				// al salir del programa no importa
			}
			conexion = null;
		}
	}
}
