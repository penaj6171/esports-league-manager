package vista;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import modelo.Partida;

// TableModel propio para la lista de partidas del calendario.
public class PartidosTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMNAS = { "Jornada", "Local", "Resultado", "Visitante", "Estado" };
	static final int COL_LOCAL = 1;
	static final int COL_RESULTADO = 2;
	static final int COL_VISITANTE = 3;
	static final int COL_ESTADO = 4;

	private List<Partida> partidas = new ArrayList<>();
	private int jornadaActual;

	public void setDatos(List<Partida> lista, int jornada) {
		this.partidas = new ArrayList<>(lista);
		this.jornadaActual = jornada;
		fireTableDataChanged();
	}

	public Partida getPartida(int fila) {
		return partidas.get(fila);
	}

	public int getJornadaActual() {
		return jornadaActual;
	}

	@Override
	public int getRowCount() {
		return partidas.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNAS.length;
	}

	@Override
	public String getColumnName(int columna) {
		return COLUMNAS[columna];
	}

	@Override
	public Class<?> getColumnClass(int columna) {
		return columna == 0 ? Integer.class : String.class;
	}

	@Override
	public Object getValueAt(int fila, int columna) {
		Partida p = partidas.get(fila);
		switch (columna) {
		case 0:
			return p.getJornada();
		case 1:
			return p.getLocal().getNombre();
		case 2:
			return p.getMarcador();
		case 3:
			return p.getVisitante().getNombre();
		default:
			return p.isJugada() ? "Jugada" : "Pendiente";
		}
	}
}
