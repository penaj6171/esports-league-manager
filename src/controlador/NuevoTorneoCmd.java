package controlador;

import java.util.ArrayList;
import java.util.List;

import modelo.Equipo;
import modelo.Partida;
import modelo.Torneo;

// Comando concreto: empezar un torneo vacio. Guarda una foto del torneo anterior para deshacer.
public class NuevoTorneoCmd implements Comando {
	private final Torneo torneo;
	private final String nuevoNombre;
	private String nombreAnterior;
	private List<Equipo> equiposAnteriores;
	private List<Partida> partidasAnteriores;

	public NuevoTorneoCmd(Torneo torneo, String nuevoNombre) {
		this.torneo = torneo;
		this.nuevoNombre = nuevoNombre;
	}

	@Override
	public void ejecutar() {
		nombreAnterior = torneo.getNombre();
		equiposAnteriores = new ArrayList<>(torneo.getEquipos());
		partidasAnteriores = new ArrayList<>(torneo.getPartidas()); // cada partida conserva su resultado
		torneo.restaurar(nuevoNombre, null, new ArrayList<>(), new ArrayList<>());
	}

	@Override
	public void deshacer() {
		torneo.restaurar(nombreAnterior, null, equiposAnteriores, partidasAnteriores);
	}

	@Override
	public String getDescripcion() {
		return "Nuevo torneo " + nuevoNombre;
	}
}
