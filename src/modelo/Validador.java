package modelo;

import excepciones.EquipoInvalidoException;
import excepciones.ResultadoInvalidoException;
import excepciones.TorneoException;

// Reglas de validacion del negocio. Viven en el modelo, no en la vista.
public final class Validador {
	public static final int LARGO_MINIMO_NOMBRE = 2;
	public static final int LARGO_MAXIMO_NOMBRE = 20;
	public static final int NIVEL_MINIMO = 1;
	public static final int NIVEL_MAXIMO = 100;

	private Validador() {
	}

	// devuelve el nombre limpio (sin espacios de sobra) o lanza la excepcion
	public static String nombreEquipo(String texto) throws EquipoInvalidoException {
		if (texto == null || texto.trim().isEmpty()) {
			throw new EquipoInvalidoException("El nombre del equipo no puede estar vacío.");
		}
		String nombre = texto.trim().replaceAll("\\s+", " ");
		if (nombre.length() < LARGO_MINIMO_NOMBRE) {
			throw new EquipoInvalidoException("El nombre debe tener al menos " + LARGO_MINIMO_NOMBRE + " caracteres.");
		}
		if (nombre.length() > LARGO_MAXIMO_NOMBRE) {
			throw new EquipoInvalidoException("El nombre puede tener como máximo " + LARGO_MAXIMO_NOMBRE + " caracteres.");
		}
		if (!nombre.matches("[\\p{L}0-9 ._-]+")) {
			throw new EquipoInvalidoException("Solo se permiten letras, números, espacios, punto, guion y guion bajo.");
		}
		return nombre;
	}

	public static int nivel(int nivel) throws EquipoInvalidoException {
		if (nivel < NIVEL_MINIMO || nivel > NIVEL_MAXIMO) {
			throw new EquipoInvalidoException("El nivel debe estar entre " + NIVEL_MINIMO + " y " + NIVEL_MAXIMO + ".");
		}
		return nivel;
	}

	// serie al mejor de 3: un equipo gana 2 mapas y el otro 0 o 1
	public static int[] resultado(int mapasLocal, int mapasVisitante) throws ResultadoInvalidoException {
		if (mapasLocal < 0 || mapasVisitante < 0) {
			throw new ResultadoInvalidoException("Los mapas no pueden ser negativos.");
		}
		boolean ganaLocal = mapasLocal == Serie.MAPAS_PARA_GANAR && mapasVisitante < Serie.MAPAS_PARA_GANAR;
		boolean ganaVisitante = mapasVisitante == Serie.MAPAS_PARA_GANAR && mapasLocal < Serie.MAPAS_PARA_GANAR;
		if (!ganaLocal && !ganaVisitante) {
			throw new ResultadoInvalidoException(
					"Serie al mejor de 3: un equipo debe ganar 2 mapas y el otro 0 o 1 (por ejemplo 2-0 o 1-2).");
		}
		return new int[] { mapasLocal, mapasVisitante };
	}

	public static String nombreTorneo(String texto) throws TorneoException {
		if (texto == null || texto.trim().length() < 3) {
			throw new TorneoException("El nombre del torneo debe tener al menos 3 caracteres.");
		}
		String nombre = texto.trim().replaceAll("\\s+", " ");
		if (nombre.length() > 40) {
			throw new TorneoException("El nombre del torneo puede tener como máximo 40 caracteres.");
		}
		return nombre;
	}
}
