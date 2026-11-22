package vista;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import modelo.Equipo;

// TableModel propio para la tabla de posiciones: la JTable le pide los datos celda por celda.
public class TablaPosicionesModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMNAS = { "#", "Equipo", "Nivel", "PJ", "PG", "PP", "MF", "MC", "DIF", "PTS" };
	static final int COL_POSICION = 0;
	static final int COL_EQUIPO = 1;
	static final int COL_DIFERENCIA = 8;
	static final int COL_PUNTOS = 9;

	private List<Equipo> equipos = new ArrayList<>();
	private boolean terminado;

	// recibe la tabla ya ordenada por el modelo y avisa a la JTable que se redibuje
	public void setDatos(List<Equipo> tabla, boolean torneoTerminado) {
		this.equipos = new ArrayList<>(tabla);
		this.terminado = torneoTerminado;
		fireTableDataChanged();
	}

	public Equipo getEquipo(int fila) {
		return equipos.get(fila);
	}

	public boolean isTerminado() {
		return terminado;
	}

	@Override
	public int getRowCount() {
		return equipos.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNAS.length;
	}

	@Override
	public String getColumnName(int columna) {
		return COLUMNAS[columna];
	}

	// con la clase correcta el TableRowSorter ordena los numeros como numeros
	@Override
	public Class<?> getColumnClass(int columna) {
		return columna == COL_EQUIPO ? String.class : Integer.class;
	}

	@Override
	public Object getValueAt(int fila, int columna) {
		Equipo e = equipos.get(fila);
		switch (columna) {
		case 0:
			return fila + 1;
		case 1:
			return e.getNombre();
		case 2:
			return e.getNivel();
		case 3:
			return e.getJugados();
		case 4:
			return e.getGanados();
		case 5:
			return e.getPerdidos();
		case 6:
			return e.getMapasFavor();
		case 7:
			return e.getMapasContra();
		case 8:
			return e.getDiferencia();
		default:
			return e.getPuntos();
		}
	}
}
