package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import modelo.Campeon;

// Pestaña "Salon de la fama": historial de campeones guardado en SQLite
// y cuantos titulos tiene cada equipo (consulta con GROUP BY).
public class PanelCampeones extends JPanel {
	private static final long serialVersionUID = 1L;

	private final CampeonesTableModel modelo = new CampeonesTableModel();
	private final JTable tabla = new JTable(modelo);
	private final DefaultListModel<String> modeloTitulos = new DefaultListModel<>();
	private final JList<String> listaTitulos = new JList<>(modeloTitulos);
	private final JLabel lblResumen = new JLabel();
	private final JButton btnActualizar = new JButton("Actualizar");
	private final JButton btnVaciar = new JButton("Vaciar salón…");

	public PanelCampeones() {
		super(new BorderLayout(12, 8));
		setBorder(new EmptyBorder(12, 14, 12, 14));

		tabla.setRowSorter(new TableRowSorter<>(modelo));
		tabla.setRowHeight(26);
		tabla.setFillsViewportHeight(true);
		tabla.getTableHeader().setReorderingAllowed(false);

		lblResumen.setFont(lblResumen.getFont().deriveFont(Font.BOLD, 15f));
		lblResumen.setForeground(Colores.TEXTO);
		JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		botones.add(btnActualizar);
		botones.add(btnVaciar);
		JPanel arriba = new JPanel(new BorderLayout());
		arriba.add(lblResumen, BorderLayout.WEST);
		arriba.add(botones, BorderLayout.EAST);

		JScrollPane scrollTitulos = new JScrollPane(listaTitulos);
		scrollTitulos.setBorder(BorderFactory.createTitledBorder("Títulos por equipo"));
		scrollTitulos.setPreferredSize(new Dimension(240, 100));

		add(arriba, BorderLayout.NORTH);
		add(new JScrollPane(tabla), BorderLayout.CENTER);
		add(scrollTitulos, BorderLayout.EAST);

		mostrar(List.of(), Map.of());
	}

	public void setAcciones(Runnable actualizar, Runnable vaciar) {
		btnActualizar.addActionListener(e -> actualizar.run());
		btnVaciar.addActionListener(e -> vaciar.run());
	}

	public void setBaseDisponible(boolean disponible) {
		btnActualizar.setEnabled(disponible);
		btnVaciar.setEnabled(disponible);
		if (!disponible) {
			lblResumen.setText("Base de datos no disponible: el Salón de la Fama necesita SQLite.");
		}
	}

	public void mostrar(List<Campeon> campeones, Map<String, Integer> titulos) {
		modelo.setDatos(campeones);
		modeloTitulos.clear();
		for (Map.Entry<String, Integer> entrada : titulos.entrySet()) {
			int n = entrada.getValue();
			modeloTitulos.addElement(entrada.getKey() + "  —  " + n + (n == 1 ? " título" : " títulos"));
		}
		if (campeones.isEmpty()) {
			lblResumen.setText("Aún no hay campeones. Termina un torneo para registrar al primero.");
		} else {
			Map.Entry<String, Integer> mejor = titulos.entrySet().iterator().next();
			String registrados = campeones.size() == 1 ? "1 torneo registrado" : campeones.size() + " torneos registrados";
			lblResumen.setText(registrados + "  ·  Más ganador: " + mejor.getKey() + " (" + mejor.getValue() + ")");
		}
	}
}
