package excepciones;

// Se lanza cuando el nombre o el nivel de un equipo no cumplen las reglas del Validador
public class EquipoInvalidoException extends TorneoException {
	private static final long serialVersionUID = 1L;

	public EquipoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
