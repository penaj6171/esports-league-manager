package controlador;

import excepciones.TorneoException;
import modelo.Equipo;
import modelo.Torneo;

// Comando concreto: eliminar un equipo. Guarda su posicion para devolverlo al mismo lugar.
public class EliminarEquipoCmd implements Comando {
	private final Torneo torneo;
	private final Equipo equipo;
	private int posicion;

	public EliminarEquipoCmd(Torneo torneo, Equipo equipo) {
		this.torneo = torneo;
		this.equipo = equipo;
	}

	@Override
	public void ejecutar() throws TorneoException {
		posicion = torneo.quitarEquipo(equipo);
	}

	@Override
	public void deshacer() throws TorneoException {
		torneo.agregarEquipo(posicion, equipo);
	}

	@Override
	public String getDescripcion() {
		return "Eliminar equipo " + equipo.getNombre();
	}
}
