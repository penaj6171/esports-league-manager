package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import modelo.Partida;
import modelo.Torneo;
import modelo.TorneoListener;

// Observador 4: pestaña con el calendario de partidas.
// Tiene un filtro (Todas / Pendientes / Jugadas) hecho con RowFilter y doble clic para registrar a mano.
public class PanelPartidos extends JPanel implements TorneoListener {
	private static final long serialVersionUID = 1L;

	private final PartidosTableModel modelo = new PartidosTableModel();
	private final JTable tabla = new JTable(modelo);
	private final TableRowSorter<PartidosTableModel> ordenador = new TableRowSorter<>(modelo);
	private final JComboBox<String> comboFiltro = new JComboBox<>(new String[] { "Todas", "Pendientes", "Jugadas" });
	private final JLabel lblJornada = new JLabel();
	private Consumer<Partida> alHacerDobleClic;

	public PanelPartidos(Torneo torneo) {
		super(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(12, 14, 10, 14));

		tabla.setRowSorter(ordenador);
		PartidoRenderer renderer = new PartidoRenderer();
		tabla.setDefaultRenderer(Object.class, renderer);
		tabla.setDefaultRenderer(Integer.class, renderer);
		tabla.setDefaultRenderer(String.class, renderer);
		tabla.setRowHeight(26);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabla.getTableHeader().setReorderingAllowed(false);
		tabla.setFillsViewportHeight(true);
		tabla.getColumnModel().getColumn(0).setPreferredWidth(70);
		tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
		tabla.getColumnModel().getColumn(2).setPreferredWidth(90);
		tabla.getColumnModel().getColumn(3).setPreferredWidth(220);
		tabla.getColumnModel().getColumn(4).setPreferredWidth(90);

		// doble clic en una fila: el controlador decide que hacer con esa partida
		tabla.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int fila = tabla.rowAtPoint(e.getPoint());
				if (e.getClickCount() == 2 && fila >= 0 && alHacerDobleClic != null) {
					alHacerDobleClic.accept(modelo.getPartida(tabla.convertRowIndexToModel(fila)));
				}
			}
		});

		comboFiltro.addActionListener(e -> aplicarFiltro());
		lblJornada.setFont(lblJornada.getFont().deriveFont(Font.BOLD, 15f));
		lblJornada.setForeground(Colores.TEXTO);
		JPanel filtro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		filtro.add(new JLabel("Mostrar:"));
		filtro.add(comboFiltro);
		JPanel arriba = new JPanel(new BorderLayout());
		arriba.add(lblJornada, BorderLayout.WEST);
		arriba.add(filtro, BorderLayout.EAST);

		JLabel lblAyuda = new JLabel("Doble clic en una partida pendiente para escribir el resultado a mano. "
				+ "En azul: la próxima jornada.");
		lblAyuda.setForeground(Colores.TEXTO_SUAVE);

		add(arriba, BorderLayout.NORTH);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(lblAyuda, BorderLayout.SOUTH);

		torneo.addListener(this);
		torneoActualizado(torneo);
	}

	public void setAlHacerDobleClic(Consumer<Partida> accion) {
		this.alHacerDobleClic = accion;
	}

	private void aplicarFiltro() {
		int opcion = comboFiltro.getSelectedIndex();
		if (opcion == 0) {
			ordenador.setRowFilter(null);
			return;
		}
		boolean soloPendientes = opcion == 1;
		ordenador.setRowFilter(new RowFilter<PartidosTableModel, Integer>() {
			@Override
			public boolean include(Entry<? extends PartidosTableModel, ? extends Integer> fila) {
				Partida p = fila.getModel().getPartida(fila.getIdentifier());
				return soloPendientes != p.isJugada();
			}
		});
	}

	@Override
	public void torneoActualizado(Torneo torneo) {
		modelo.setDatos(torneo.getPartidas(), torneo.getJornadaActual());
		if (torneo.getPartidas().isEmpty()) {
			lblJornada.setText("El calendario se genera solo al tener " + Torneo.MIN_EQUIPOS + " o más equipos.");
		} else if (torneo.isTerminado()) {
			lblJornada.setText("Calendario completo: " + torneo.getPartidas().size() + " partidas jugadas");
		} else {
			lblJornada.setText("Próxima: jornada " + torneo.getJornadaActual() + " de " + torneo.getTotalJornadas()
					+ "  ·  " + torneo.getPendientes().size() + " partidas pendientes");
		}
	}
}
