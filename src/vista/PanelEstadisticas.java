package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import modelo.Equipo;
import modelo.Partida;
import modelo.Torneo;
import modelo.TorneoListener;

// Observador 5: pestaña de estadisticas con tarjetas y un grafico de barras propio (Graphics2D, Guia 10).
public class PanelEstadisticas extends JPanel implements TorneoListener {
	private static final long serialVersionUID = 1L;

	private final JLabel lblJugadas = new JLabel();
	private final JLabel lblMapas = new JLabel();
	private final JLabel lblBarridas = new JLabel();
	private final JLabel lblLocal = new JLabel();
	private final GraficoPuntos grafico = new GraficoPuntos();

	public PanelEstadisticas(Torneo torneo) {
		super(new BorderLayout(0, 12));
		setBorder(new EmptyBorder(12, 14, 12, 14));

		JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
		tarjetas.add(tarjeta("Partidas jugadas", lblJugadas));
		tarjetas.add(tarjeta("Mapas jugados", lblMapas));
		tarjetas.add(tarjeta("Series 2-0 (barridas)", lblBarridas));
		tarjetas.add(tarjeta("Victorias del local", lblLocal));

		JScrollPane scroll = new JScrollPane(grafico);
		scroll.setBorder(BorderFactory.createLineBorder(Colores.BORDE));
		add(tarjetas, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);

		torneo.addListener(this);
		torneoActualizado(torneo);
	}

	private JPanel tarjeta(String titulo, JLabel valor) {
		JPanel panel = new JPanel(new BorderLayout(0, 4));
		panel.setBackground(Colores.FONDO_TARJETA);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Colores.BORDE),
				new EmptyBorder(10, 14, 10, 14)));
		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setForeground(Colores.TEXTO_SUAVE);
		valor.setFont(valor.getFont().deriveFont(Font.BOLD, 22f));
		valor.setForeground(Colores.PRIMARIO);
		panel.add(lblTitulo, BorderLayout.NORTH);
		panel.add(valor, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public void torneoActualizado(Torneo torneo) {
		int jugadas = 0;
		int mapas = 0;
		int barridas = 0;
		int ganaLocal = 0;
		for (Partida p : torneo.getPartidas()) {
			if (p.isJugada()) {
				int[] r = p.getResultado();
				jugadas++;
				mapas += r[0] + r[1];
				if (Math.min(r[0], r[1]) == 0) {
					barridas++;
				}
				if (r[0] > r[1]) {
					ganaLocal++;
				}
			}
		}
		lblJugadas.setText(jugadas + " / " + torneo.getPartidas().size());
		lblMapas.setText(String.valueOf(mapas));
		lblBarridas.setText(porcentaje(barridas, jugadas));
		lblLocal.setText(porcentaje(ganaLocal, jugadas));
		grafico.setDatos(torneo.getTabla(), torneo.isTerminado());
	}

	private String porcentaje(int parte, int total) {
		return total == 0 ? "—" : Math.round(100.0 * parte / total) + " %";
	}

	// grafico de barras horizontales: puntos de cada equipo, en el orden de la tabla
	private static class GraficoPuntos extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final int ALTO_FILA = 34;
		private static final int ANCHO_NOMBRES = 190;

		private List<Equipo> equipos = new ArrayList<>();
		private boolean terminado;

		GraficoPuntos() {
			setBackground(Color.WHITE);
		}

		void setDatos(List<Equipo> tabla, boolean torneoTerminado) {
			this.equipos = new ArrayList<>(tabla);
			this.terminado = torneoTerminado;
			revalidate(); // el alto depende de la cantidad de equipos
			repaint(); // nunca se llama a paintComponent directamente
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(600, 70 + equipos.size() * ALTO_FILA);
		}

		@Override
		protected void paintComponent(Graphics g0) {
			super.paintComponent(g0); // limpia el fondo
			Graphics2D g = (Graphics2D) g0.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g.setFont(getFont().deriveFont(Font.BOLD, 15f));
			g.setColor(Colores.TEXTO);
			g.drawString(terminado ? "Puntos finales por equipo" : "Puntos por equipo", 18, 30);

			g.setFont(getFont().deriveFont(13f));
			if (equipos.isEmpty()) {
				g.setColor(Colores.TEXTO_SUAVE);
				g.drawString("Agrega equipos para ver el gráfico.", 18, 62);
				g.dispose();
				return;
			}

			int maximo = 1;
			for (Equipo e : equipos) {
				maximo = Math.max(maximo, e.getPuntos());
			}
			int x0 = 18 + ANCHO_NOMBRES;
			int anchoBarras = Math.max(100, getWidth() - x0 - 80);
			FontMetrics fm = g.getFontMetrics();

			for (int i = 0; i < equipos.size(); i++) {
				Equipo e = equipos.get(i);
				int y = 50 + i * ALTO_FILA;
				int centroTexto = y + 14 + fm.getAscent() / 2 - 2;

				g.setColor(Colores.TEXTO);
				g.drawString(recortar((i + 1) + ".  " + e.getNombre(), fm, ANCHO_NOMBRES - 10), 18, centroTexto);

				g.setColor(new Color(0xEEF0F4)); // carril de fondo
				g.fillRoundRect(x0, y + 4, anchoBarras, 20, 10, 10);

				int ancho = (int) Math.round((double) e.getPuntos() / maximo * anchoBarras);
				if (e.getPuntos() > 0 && ancho < 8) {
					ancho = 8;
				}
				g.setColor(colorBarra(i));
				g.fillRoundRect(x0, y + 4, ancho, 20, 10, 10); // la barra

				g.setColor(Colores.TEXTO);
				g.drawString(e.getPuntos() + " pts", x0 + ancho + 8, centroTexto); // el valor
			}
			g.dispose();
		}

		private Color colorBarra(int posicion) {
			switch (posicion) {
			case 0:
				return Colores.ORO_FUERTE;
			case 1:
				return Colores.PLATA_FUERTE;
			case 2:
				return Colores.BRONCE_FUERTE;
			default:
				return Colores.PRIMARIO;
			}
		}

		private String recortar(String texto, FontMetrics fm, int anchoMaximo) {
			if (fm.stringWidth(texto) <= anchoMaximo) {
				return texto;
			}
			String corto = texto;
			while (corto.length() > 3 && fm.stringWidth(corto + "…") > anchoMaximo) {
				corto = corto.substring(0, corto.length() - 1);
			}
			return corto + "…";
		}
	}
}
