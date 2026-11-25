package modelo;

import java.util.Random;

// Estrategia concreta 1: cada mapa es 50/50, el nivel no importa (torneo de sorpresas).
public class SimulacionAleatoria implements EstrategiaSimulacion {
	private final Random azar;

	public SimulacionAleatoria() {
		this(new Random());
	}

	// recibir el Random permite usar una semilla en las pruebas (resultados repetibles)
	public SimulacionAleatoria(Random azar) {
		this.azar = azar;
	}

	@Override
	public String getNombre() {
		return "Aleatoria";
	}

	@Override
	public String getDescripcion() {
		return "Cada mapa es 50/50: el nivel no importa, cualquiera puede ganar.";
	}

	@Override
	public int[] simular(Equipo local, Equipo visitante) {
		return Serie.mejorDeTres(0.5, azar);
	}

	@Override
	public String toString() {
		return getNombre();
	}
}
