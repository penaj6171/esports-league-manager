package controlador;

import java.util.ArrayList;
import java.util.List;

import excepciones.TorneoException;

// Agrupa varios comandos para deshacerlos con un solo Ctrl+Z
// (simular una jornada, simular todo, cargar equipos de ejemplo).
public class ComandoCompuesto implements Comando {
	private final String descripcion;
	private final List<Comando> comandos;

	public ComandoCompuesto(String descripcion, List<Comando> comandos) {
		this.descripcion = descripcion;
		this.comandos = new ArrayList<>(comandos);
	}

	// si uno falla, se deshacen los que ya se ejecutaron: todo o nada
	@Override
	public void ejecutar() throws TorneoException {
		List<Comando> hechos = new ArrayList<>();
		try {
			for (Comando c : comandos) {
				c.ejecutar();
				hechos.add(c);
			}
		} catch (TorneoException e) {
			for (int i = hechos.size() - 1; i >= 0; i--) {
				hechos.get(i).deshacer();
			}
			throw e;
		}
	}

	// se deshace en orden inverso
	@Override
	public void deshacer() throws TorneoException {
		for (int i = comandos.size() - 1; i >= 0; i--) {
			comandos.get(i).deshacer();
		}
	}

	public int getCantidad() {
		return comandos.size();
	}

	@Override
	public String getDescripcion() {
		return descripcion;
	}
}
