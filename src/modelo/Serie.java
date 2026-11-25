package modelo;

import java.util.Random;

// Ayudante para jugar una serie al mejor de 3 mapas (Bo3): gana el primero que llega a 2 mapas.
// Lo reutilizan las estrategias que usan una probabilidad fija por mapa.
final class Serie {
	static final int MAPAS_PARA_GANAR = 2;

	private Serie() {
	}

	// probLocal = probabilidad de que el local gane cada mapa (entre 0 y 1)
	static int[] mejorDeTres(double probLocal, Random azar) {
		int local = 0;
		int visitante = 0;
		while (local < MAPAS_PARA_GANAR && visitante < MAPAS_PARA_GANAR) {
			if (azar.nextDouble() < probLocal) {
				local++;
			} else {
				visitante++;
			}
		}
		return new int[] { local, visitante };
	}
}
