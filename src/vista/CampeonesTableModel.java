package vista;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import modelo.Campeon;

// TableModel del Salon de la Fama (datos leidos de la tabla "campeon" de SQLite).
public class CampeonesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMNAS = { "Fecha", "Torneo", "Campeón", "Puntos", "Estrategia" };
	private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private List<Campeon> campeones = new ArrayList<>();

	public void setDatos(List<Campeon> lista) {
		this.campeones = new ArrayList<>(lista);
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return campeones.size();
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
		return columna == 3 ? Integer.class : String.class;
	}

	@Override
	public Object getValueAt(int fila, int columna) {
		Campeon c = campeones.get(fila);
		switch (columna) {
		case 0:
			return c.getFecha().format(FORMATO);
		case 1:
			return c.getTorneo();
		case 2:
			return c.getEquipo();
		case 3:
			return c.getPuntos();
		default:
			return c.getEstrategia();
		}
	}
}
