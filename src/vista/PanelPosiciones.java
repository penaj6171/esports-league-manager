package vista;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import modelo.Equipo;
import modelo.Torneo;
import modelo.TorneoListener;

// Observador 3: pestaña con la tabla de posiciones (JTable + TableModel propio + renderer + orden).
public class PanelPosiciones extends JPanel implements TorneoListener {
	private static final long serialVersionUID = 1L;

	private final TablaPosicionesModel modelo = new TablaPosicionesModel();
	private final JTable tabla = new JTable(modelo);
	private final JLabel lblLider = new JLabel();

	public PanelPosiciones(Torneo torneo) {
		super(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(12, 14, 10, 14));

		tabla.setRowSorter(new TableRowSorter<>(modelo)); // clic en un encabezado para ordenar
		PosicionRenderer renderer = new PosicionRenderer();
		tabla.setDefaultRenderer(Object.class, renderer);
		tabla.setDefaultRenderer(Integer.class, renderer);
		tabla.setRowHeight(28);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.getTableHeader().setReorderingAllowed(false);
		tabla.setFillsViewportHeight(true);
		TableColumnModel columnas = tabla.getColumnModel();
		columnas.getColumn(0).setPreferredWidth(40);
		columnas.getColumn(1).setPreferredWidth(240);
		for (int i = 2; i < columnas.getColumnCount(); i++) {
			columnas.getColumn(i).setPreferredWidth(60);
		}

		lblLider.setFont(lblLider.getFont().deriveFont(Font.BOLD, 15f));
		lblLider.setForeground(Colores.TEXTO);
		JLabel lblAyuda = new JLabel("PJ jugados · PG ganados · PP perdidos · MF/MC mapas a favor/en contra · "
				+ "victoria = " + Torneo.PUNTOS_VICTORIA + " pts · clic en un encabezado para ordenar");
		lblAyuda.setForeground(Colores.TEXTO_SUAVE);

		add(lblLider, BorderLayout.NORTH);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(lblAyuda, BorderLayout.SOUTH);

		torneo.addListener(this);
		torneoActualizado(torneo);
	}

	@Override
	public void torneoActualizado(Torneo torneo) {
		modelo.setDatos(torneo.getTabla(), torneo.isTerminado());
		if (torneo.getEquipos().isEmpty()) {
			lblLider.setText("Todavía no hay equipos. Usa Torneo > Agregar equipo (Ctrl+T) o Equipos de ejemplo.");
		} else if (torneo.isTerminado()) {
			lblLider.setText("Campeón: " + torneo.getCampeon().getNombre());
		} else if (torneo.hayResultados()) {
			Equipo lider = torneo.getTabla().get(0);
			lblLider.setText("Líder: " + lider.getNombre() + " con " + lider.getPuntos() + " puntos");
		} else {
			lblLider.setText("Tabla de posiciones · " + torneo.getEquipos().size() + " equipos");
		}
	}

	// equipo de la fila seleccionada (o null)
	public Equipo getEquipoSeleccionado() {
		int fila = tabla.getSelectedRow();
		return fila < 0 ? null : modelo.getEquipo(tabla.convertRowIndexToModel(fila));
	}
}
