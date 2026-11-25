package modelo;

// Una serie entre dos equipos al mejor de 3 mapas (Bo3).
// resultado = {mapas del local, mapas del visitante}; es null mientras la partida esta pendiente.
public class Partida {
	private final int jornada;
	private final Equipo local;
	private final Equipo visitante;
	private int[] resultado;

	public Partida(int jornada, Equipo local, Equipo visitante) {
		this(jornada, local, visitante, null);
	}

	// constructor que usa el DAO al cargar una partida que ya tenia resultado
	public Partida(int jornada, Equipo local, Equipo visitante, int[] resultado) {
		if (local == null || visitante == null || local.equals(visitante)) {
			throw new IllegalArgumentException("Una partida necesita dos equipos distintos");
		}
		this.jornada = jornada;
		this.local = local;
		this.visitante = visitante;
		this.resultado = resultado == null ? null : resultado.clone();
	}

	// solo Torneo puede cambiar el resultado (mismo paquete)
	void setResultado(int[] resultado) {
		this.resultado = resultado == null ? null : resultado.clone();
	}

	public int getJornada() {
		return jornada;
	}

	public Equipo getLocal() {
		return local;
	}

	public Equipo getVisitante() {
		return visitante;
	}

	public boolean isJugada() {
		return resultado != null;
	}

	// devuelve una copia para que nadie cambie el resultado desde afuera
	public int[] getResultado() {
		return resultado == null ? null : resultado.clone();
	}

	public Equipo getGanador() {
		if (!isJugada()) {
			return null;
		}
		return resultado[0] > resultado[1] ? local : visitante;
	}

	public boolean participa(Equipo equipo) {
		return local.equals(equipo) || visitante.equals(equipo);
	}

	public String getMarcador() {
		return isJugada() ? resultado[0] + " - " + resultado[1] : "vs";
	}

	@Override
	public String toString() {
		return local + " " + getMarcador() + " " + visitante;
	}
}
