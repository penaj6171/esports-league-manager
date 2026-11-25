package controlador;

import java.util.Map;

import modelo.Partida;
import modelo.Torneo;

// Comando concreto: vuelve a dejar todas las partidas pendientes (mismos equipos y calendario).
// Sirve para repetir el torneo con otra estrategia. Guarda una copia de los resultados para deshacer.
public class ReiniciarResultadosCmd implements Comando {
	private final Torneo torneo;
	private Map<Partida, int[]> respaldo;

	public ReiniciarResultadosCmd(Torneo torneo) {
		this.torneo = torneo;
	}

	@Override
	public void ejecutar() {
		respaldo = torneo.reiniciarResultados();
	}

	@Override
	public void deshacer() {
		torneo.restaurarResultados(respaldo);
	}

	@Override
	public String getDescripcion() {
		return "Reiniciar resultados";
	}
}
