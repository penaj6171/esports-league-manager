package modelo;

import java.time.LocalDateTime;

// Registro del Salon de la Fama: que equipo gano que torneo, con cuantos puntos y cuando.
public class Campeon {
	private final String torneo;
	private final String equipo;
	private final int puntos;
	private final String estrategia;
	private final LocalDateTime fecha;

	public Campeon(String torneo, String equipo, int puntos, String estrategia, LocalDateTime fecha) {
		this.torneo = torneo;
		this.equipo = equipo;
		this.puntos = puntos;
		this.estrategia = estrategia;
		this.fecha = fecha;
	}

	public String getTorneo() {
		return torneo;
	}

	public String getEquipo() {
		return equipo;
	}

	public int getPuntos() {
		return puntos;
	}

	public String getEstrategia() {
		return estrategia;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}
}
