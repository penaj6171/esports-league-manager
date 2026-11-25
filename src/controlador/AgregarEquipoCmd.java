package controlador;

import excepciones.TorneoException;
import modelo.Equipo;
import modelo.Torneo;

// Comando concreto: agregar un equipo. Deshacer lo quita del torneo.
public class AgregarEquipoCmd implements Comando {
	private final Torneo torneo; // receptor: quien hace el trabajo real
	private final Equipo equipo;

	public AgregarEquipoCmd(Torneo torneo, Equipo equipo) {
		this.torneo = torneo;
		this.equipo = equipo;
	}

	@Override
	public void ejecutar() throws TorneoException {
		torneo.agregarEquipo(equipo);
	}

	@Override
	public void deshacer() throws TorneoException {
		torneo.quitarEquipo(equipo);
	}

	@Override
	public String getDescripcion() {
		return "Agregar equipo " + equipo.getNombre();
	}
}
