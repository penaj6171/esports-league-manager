package excepciones;

// Excepcion base de las reglas del torneo.
// Es comprobada (extends Exception): el compilador obliga al controlador a atenderla.
public class TorneoException extends Exception {
	private static final long serialVersionUID = 1L;

	public TorneoException(String mensaje) {
		super(mensaje);
	}
}
