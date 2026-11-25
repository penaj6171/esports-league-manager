package modelo;

// Equipo de e-sports: nombre, nivel (1 a 100) y sus estadisticas en el torneo.
// Las estadisticas NO se editan a mano: Torneo las recalcula a partir de las partidas jugadas,
// por eso deshacer un resultado siempre deja la tabla correcta.
public class Equipo {
	private final String nombre;
	private final int nivel;

	private int jugados;
	private int ganados;
	private int perdidos;
	private int mapasFavor;
	private int mapasContra;
	private int puntos;

	public Equipo(String nombre, int nivel) {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del equipo es obligatorio");
		}
		this.nombre = nombre.trim();
		this.nivel = nivel;
	}

	// pone todas las estadisticas en cero (solo lo usa Torneo)
	void reiniciarEstadisticas() {
		jugados = 0;
		ganados = 0;
		perdidos = 0;
		mapasFavor = 0;
		mapasContra = 0;
		puntos = 0;
	}

	// suma una serie jugada: mapas ganados (favor) y perdidos (contra)
	void sumarSerie(int favor, int contra) {
		jugados++;
		mapasFavor += favor;
		mapasContra += contra;
		if (favor > contra) {
			ganados++;
			puntos += Torneo.PUNTOS_VICTORIA;
		} else {
			perdidos++;
		}
	}

	public String getNombre() {
		return nombre;
	}

	public int getNivel() {
		return nivel;
	}

	public int getJugados() {
		return jugados;
	}

	public int getGanados() {
		return ganados;
	}

	public int getPerdidos() {
		return perdidos;
	}

	public int getMapasFavor() {
		return mapasFavor;
	}

	public int getMapasContra() {
		return mapasContra;
	}

	public int getDiferencia() {
		return mapasFavor - mapasContra;
	}

	public int getPuntos() {
		return puntos;
	}

	// porcentaje de series ganadas entre 0.0 y 1.0
	public double getPorcentajeVictorias() {
		return jugados == 0 ? 0.0 : (double) ganados / jugados;
	}

	// dos equipos son iguales si tienen el mismo nombre (sin importar mayusculas)
	@Override
	public boolean equals(Object otro) {
		if (this == otro) {
			return true;
		}
		if (!(otro instanceof Equipo)) {
			return false;
		}
		return nombre.equalsIgnoreCase(((Equipo) otro).nombre);
	}

	@Override
	public int hashCode() {
		return nombre.toLowerCase().hashCode();
	}

	@Override
	public String toString() {
		return nombre;
	}
}
