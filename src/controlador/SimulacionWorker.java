package controlador;

import java.util.List;

import javax.swing.SwingWorker;

import modelo.Partida;
import modelo.Torneo;

// Simula todas las partidas pendientes en un hilo de fondo para que la ventana no se congele.
// - doInBackground(): hilo de fondo. Calcula resultados, NO toca componentes Swing ni cambia el torneo.
// - process() y done(): corren en el EDT; aqui si se aplica el resultado y se actualiza la vista.
// - setProgress() alimenta la JProgressBar (el controlador escucha la propiedad "progress").
public class SimulacionWorker extends SwingWorker<Integer, SimularPartidaCmd> {
	private final Torneo torneo;
	private final List<Partida> pendientes;
	private final TorneoController controlador;
	private final int pausaMs;

	public SimulacionWorker(Torneo torneo, List<Partida> pendientes, TorneoController controlador, int pausaMs) {
		this.torneo = torneo;
		this.pendientes = pendientes;
		this.controlador = controlador;
		this.pausaMs = pausaMs;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		int hechas = 0;
		for (Partida partida : pendientes) {
			if (isCancelled()) {
				break;
			}
			int[] resultado = torneo.calcularResultado(partida); // la estrategia decide
			Thread.sleep(pausaMs); // solo para que se vea el avance en la exposicion
			publish(new SimularPartidaCmd(torneo, partida, resultado, false));
			hechas++;
			setProgress(100 * hechas / pendientes.size());
		}
		return hechas;
	}

	@Override
	protected void process(List<SimularPartidaCmd> comandos) {
		if (isCancelled()) {
			return; // lo que llego despues de cancelar no se aplica
		}
		for (SimularPartidaCmd comando : comandos) {
			controlador.aplicarSimulado(comando);
		}
	}

	@Override
	protected void done() {
		controlador.simulacionTerminada(isCancelled());
	}
}
