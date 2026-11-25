package controlador;

import excepciones.TorneoException;
import modelo.Partida;
import modelo.Torneo;

// Comando concreto: jugar una partida (simulada por la estrategia o con un resultado escrito a mano).
// Guarda el resultado: al rehacer se repite EXACTAMENTE el mismo marcador, no se vuelve a sortear.
public class SimularPartidaCmd implements Comando {
	private final Torneo torneo;
	private final Partida partida;
	private final boolean manual;
	private int[] resultado;

	// la estrategia decidira el resultado la primera vez que se ejecute
	public SimularPartidaCmd(Torneo torneo, Partida partida) {
		this(torneo, partida, null, false);
	}

	// resultado ya conocido (escrito por el usuario o calculado por el SwingWorker)
	public SimularPartidaCmd(Torneo torneo, Partida partida, int[] resultado, boolean manual) {
		this.torneo = torneo;
		this.partida = partida;
		this.resultado = resultado == null ? null : resultado.clone();
		this.manual = manual;
	}

	@Override
	public void ejecutar() throws TorneoException {
		if (resultado == null) {
			resultado = torneo.calcularResultado(partida); // la estrategia elige UNA sola vez
		}
		torneo.registrarResultado(partida, resultado);
	}

	@Override
	public void deshacer() {
		torneo.anularResultado(partida);
	}

	public Partida getPartida() {
		return partida;
	}

	@Override
	public String getDescripcion() {
		String accion = manual ? "Registrar " : "Simular ";
		return accion + partida.getLocal() + " vs " + partida.getVisitante();
	}
}
