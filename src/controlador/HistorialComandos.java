package controlador;

import excepciones.TorneoException;
import util.PilaGenerica;

// Invocador del patron Command: ejecuta los comandos y los guarda para deshacer y rehacer.
public class HistorialComandos {
	private final PilaGenerica<Comando> pilaDeshacer = new PilaGenerica<>();
	private final PilaGenerica<Comando> pilaRehacer = new PilaGenerica<>();

	// si el comando falla (lanza excepcion) no se apila nada
	public void ejecutar(Comando comando) throws TorneoException {
		comando.ejecutar();
		registrarEjecutado(comando);
	}

	// para comandos que ya se ejecutaron (por ejemplo, los que aplica el SwingWorker)
	public void registrarEjecutado(Comando comando) {
		pilaDeshacer.apilar(comando);
		pilaRehacer.limpiar(); // una accion nueva invalida lo que se podia rehacer
	}

	public Comando deshacer() throws TorneoException {
		Comando comando = pilaDeshacer.verTope();
		comando.deshacer(); // si falla, el comando se queda donde estaba
		pilaDeshacer.desapilar();
		pilaRehacer.apilar(comando);
		return comando;
	}

	public Comando rehacer() throws TorneoException {
		Comando comando = pilaRehacer.verTope();
		comando.ejecutar();
		pilaRehacer.desapilar();
		pilaDeshacer.apilar(comando);
		return comando;
	}

	public boolean puedeDeshacer() {
		return !pilaDeshacer.estaVacia();
	}

	public boolean puedeRehacer() {
		return !pilaRehacer.estaVacia();
	}

	public String getDescripcionDeshacer() {
		return puedeDeshacer() ? pilaDeshacer.verTope().getDescripcion() : "";
	}

	public String getDescripcionRehacer() {
		return puedeRehacer() ? pilaRehacer.verTope().getDescripcion() : "";
	}

	public int getCantidadDeshacer() {
		return pilaDeshacer.tamano();
	}

	public void limpiar() {
		pilaDeshacer.limpiar();
		pilaRehacer.limpiar();
	}
}
