package excepciones;

// Se lanza cuando se intenta agregar un equipo con un nombre que ya existe en el torneo
public class EquipoDuplicadoException extends TorneoException {
	private static final long serialVersionUID = 1L;

	public EquipoDuplicadoException(String nombre) {
		super("Ya existe un equipo llamado \"" + nombre + "\".");
	}
}
