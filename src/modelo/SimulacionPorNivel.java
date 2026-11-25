package modelo;

import java.util.Random;

// Estrategia concreta 2: el equipo con mas nivel tiene mas probabilidad de ganar cada mapa.
// P(local gana un mapa) = nivelLocal² / (nivelLocal² + nivelVisitante²)
// Se usa el cuadrado para que la diferencia de nivel se note (88 vs 64 -> 65 % por mapa).
public class SimulacionPorNivel implements EstrategiaSimulacion {
	private final Random azar;

	public SimulacionPorNivel() {
		this(new Random());
	}

	public SimulacionPorNivel(Random azar) {
		this.azar = azar;
	}

	@Override
	public String getNombre() {
		return "Por nivel";
	}

	@Override
	public String getDescripcion() {
		return "El de más nivel es favorito: cada mapa se gana con probabilidad nivel² / (nivel² + nivel rival²).";
	}

	@Override
	public int[] simular(Equipo local, Equipo visitante) {
		double fuerzaLocal = Math.pow(local.getNivel(), 2);
		double fuerzaVisitante = Math.pow(visitante.getNivel(), 2);
		double probLocal = fuerzaLocal / (fuerzaLocal + fuerzaVisitante);
		return Serie.mejorDeTres(probLocal, azar);
	}

	@Override
	public String toString() {
		return getNombre();
	}
}
