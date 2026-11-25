package modelo;

import java.util.Random;

// Estrategia concreta 3: combina el nivel con la racha del equipo en el torneo
// y agrega "impulso": quien gana un mapa tiene ventaja en el siguiente.
// Es un algoritmo distinto, no solo una constante diferente.
public class SimulacionPorRacha implements EstrategiaSimulacion {
	private static final double IMPULSO = 0.10;
	private static final double PROB_MINIMA = 0.05;
	private static final double PROB_MAXIMA = 0.95;

	private final Random azar;

	public SimulacionPorRacha() {
		this(new Random());
	}

	public SimulacionPorRacha(Random azar) {
		this.azar = azar;
	}

	@Override
	public String getNombre() {
		return "Nivel + racha";
	}

	@Override
	public String getDescripcion() {
		return "Combina el nivel con el % de victorias en el torneo; quien gana un mapa gana impulso para el siguiente.";
	}

	@Override
	public int[] simular(Equipo local, Equipo visitante) {
		double fuerzaLocal = fuerza(local);
		double fuerzaVisitante = fuerza(visitante);
		double impulso = 0; // positivo favorece al local, negativo al visitante
		int mapasLocal = 0;
		int mapasVisitante = 0;
		while (mapasLocal < Serie.MAPAS_PARA_GANAR && mapasVisitante < Serie.MAPAS_PARA_GANAR) {
			double prob = fuerzaLocal / (fuerzaLocal + fuerzaVisitante) + impulso;
			prob = Math.max(PROB_MINIMA, Math.min(PROB_MAXIMA, prob));
			if (azar.nextDouble() < prob) {
				mapasLocal++;
				impulso = IMPULSO;
			} else {
				mapasVisitante++;
				impulso = -IMPULSO;
			}
		}
		return new int[] { mapasLocal, mapasVisitante };
	}

	// un equipo sin partidas tiene racha neutra (50 %)
	private double fuerza(Equipo equipo) {
		double racha = equipo.getJugados() == 0 ? 0.5 : equipo.getPorcentajeVictorias();
		return equipo.getNivel() * (0.5 + racha);
	}

	@Override
	public String toString() {
		return getNombre();
	}
}
