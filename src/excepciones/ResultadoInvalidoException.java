package excepciones;

// Se lanza cuando un resultado no es valido para una serie al mejor de 3 mapas
public class ResultadoInvalidoException extends TorneoException {
	private static final long serialVersionUID = 1L;

	public ResultadoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
