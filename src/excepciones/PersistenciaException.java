package excepciones;

// Envuelve los errores de JDBC (SQLException) o del driver.
// Asi la vista nunca ve detalles de la base de datos, solo un mensaje claro.
public class PersistenciaException extends Exception {
	private static final long serialVersionUID = 1L;

	public PersistenciaException(String mensaje, Throwable causa) {
		super(mensaje, causa); // conserva la excepcion original para depurar
	}
}
