package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import modelo.Equipo;
import modelo.EstrategiaSimulacion;
import modelo.Torneo;
import modelo.TorneoListener;

// Observador 2: cabecera con el nombre del torneo, su avance y el combo de estrategia (Strategy).
// El fondo degradado se dibuja con Graphics2D (Guia 10: graficos).
public class PanelCabecera extends JPanel implements TorneoListener {
	private static final long serialVersionUID = 1L;

	private final JLabel lblTorneo = new JLabel();
	private final JLabel lblResumen = new JLabel();
	private final JComboBox<EstrategiaSimulacion> comboEstrategia = new JComboBox<>();
	private final JLabel lblDescripcion = new JLabel();
	private boolean actualizando; // evita avisar al controlador cuando el cambio viene del modelo
	private Consumer<EstrategiaSimulacion> alCambiarEstrategia;

	public PanelCabecera(Torneo torneo, List<EstrategiaSimulacion> estrategias) {
		super(new BorderLayout(24, 0));
		setBorder(new EmptyBorder(14, 22, 14, 22));

		JLabel lblApp = new JLabel("E-SPORTS LEAGUE MANAGER");
		lblApp.setFont(lblApp.getFont().deriveFont(Font.BOLD, 12f));
		lblApp.setForeground(Colores.ACENTO);
		lblTorneo.setFont(lblTorneo.getFont().deriveFont(Font.BOLD, 24f));
		lblTorneo.setForeground(Color.WHITE);
		lblResumen.setFont(lblResumen.getFont().deriveFont(13f));
		lblResumen.setForeground(new Color(0xC7D2FE));

		JPanel izquierda = new JPanel();
		izquierda.setOpaque(false);
		izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));
		izquierda.add(lblApp);
		izquierda.add(lblTorneo);
		izquierda.add(lblResumen);

		for (EstrategiaSimulacion e : estrategias) {
			comboEstrategia.addItem(e); // el combo muestra e.toString()
		}
		comboEstrategia.addActionListener(evento -> {
			if (!actualizando && alCambiarEstrategia != null) {
				alCambiarEstrategia.accept((EstrategiaSimulacion) comboEstrategia.getSelectedItem());
			}
		});
		JLabel lblEstrategia = new JLabel("Estrategia de simulación");
		lblEstrategia.setForeground(Color.WHITE);
		lblEstrategia.setFont(lblEstrategia.getFont().deriveFont(Font.BOLD));
		lblDescripcion.setForeground(new Color(0xC7D2FE));
		lblDescripcion.setFont(lblDescripcion.getFont().deriveFont(11.5f));

		JPanel derecha = new JPanel(new GridBagLayout());
		derecha.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 4, 0);
		derecha.add(lblEstrategia, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		derecha.add(comboEstrategia, c);
		derecha.add(lblDescripcion, c);

		add(izquierda, BorderLayout.CENTER);
		add(derecha, BorderLayout.EAST);

		torneo.addListener(this);
		torneoActualizado(torneo);
	}

	public void setAlCambiarEstrategia(Consumer<EstrategiaSimulacion> accion) {
		this.alCambiarEstrategia = accion;
	}

	public void setEstrategiaHabilitada(boolean habilitada) {
		comboEstrategia.setEnabled(habilitada);
	}

	@Override
	public void torneoActualizado(Torneo torneo) {
		lblTorneo.setText(torneo.getNombre());
		lblResumen.setText(resumen(torneo));
		actualizando = true;
		comboEstrategia.setSelectedItem(torneo.getEstrategia());
		actualizando = false;
		String descripcion = torneo.getEstrategia().getDescripcion();
		lblDescripcion.setText("<html><div style='width:250px'>" + descripcion + "</div></html>");
		comboEstrategia.setToolTipText(descripcion);
	}

	private String resumen(Torneo torneo) {
		int equipos = torneo.getEquipos().size();
		if (torneo.getPartidas().isEmpty()) {
			return equipos + (equipos == 1 ? " equipo" : " equipos") + "  ·  agrega al menos " + Torneo.MIN_EQUIPOS
					+ " para generar el calendario";
		}
		String partidas = torneo.getPartidasJugadas() + "/" + torneo.getPartidas().size() + " partidas";
		if (torneo.isTerminado()) {
			Equipo campeon = torneo.getCampeon();
			return equipos + " equipos  ·  " + partidas + "  ·  Torneo terminado  ·  Campeón: " + campeon.getNombre();
		}
		return equipos + " equipos  ·  Jornada " + torneo.getJornadaActual() + " de " + torneo.getTotalJornadas()
				+ "  ·  " + partidas;
	}

	// fondo con degradado
	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setPaint(new GradientPaint(0, 0, Colores.PRIMARIO_OSCURO, getWidth(), getHeight(), Colores.PRIMARIO));
		g.fillRect(0, 0, getWidth(), getHeight());
		g.dispose();
	}
}
