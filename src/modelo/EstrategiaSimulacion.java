package modelo;

// Patron Strategy: interfaz comun de los algoritmos que deciden el resultado de una partida.
// Torneo (el contexto) solo conoce esta interfaz; no hay ningun if/switch sobre el tipo de estrategia.
public interface EstrategiaSimulacion {

	// nombre corto que se muestra en el combo y en el menu
	String getNombre();

	// explicacion del algoritmo (se muestra como ayuda)
	String getDescripcion();

	// devuelve {mapas del local, mapas del visitante} de una serie al mejor de 3
	int[] simular(Equipo local, Equipo visitante);
}
