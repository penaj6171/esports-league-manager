package vista;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import modelo.Partida;

// Renderer de la tabla de partidas: resalta la jornada actual, pone en negrita al ganador
// y colorea el estado (verde = jugada, ambar = pendiente).
public class PartidoRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada, boolean foco,
			int fila, int columna) {
		super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
		PartidosTableModel modelo = (PartidosTableModel) tabla.getModel();
		Partida partida = modelo.getPartida(tabla.convertRowIndexToModel(fila));
		int col = tabla.convertColumnIndexToModel(columna);

		setBorder(new EmptyBorder(0, 8, 0, 8));
		boolean alCentro = col == 0 || col == PartidosTableModel.COL_RESULTADO || col == PartidosTableModel.COL_ESTADO;
		setHorizontalAlignment(alCentro ? SwingConstants.CENTER : SwingConstants.LEFT);
		if (col == PartidosTableModel.COL_LOCAL) {
			setHorizontalAlignment(SwingConstants.RIGHT);
		}

		boolean ganador = partida.isJugada()
				&& ((col == PartidosTableModel.COL_LOCAL && partida.getGanador() == partida.getLocal())
						|| (col == PartidosTableModel.COL_VISITANTE && partida.getGanador() == partida.getVisitante()));
		boolean negrita = ganador || col == PartidosTableModel.COL_RESULTADO;
		setFont(tabla.getFont().deriveFont(negrita ? Font.BOLD : Font.PLAIN));

		if (!seleccionada) {
			setForeground(Colores.TEXTO);
			if (col == PartidosTableModel.COL_ESTADO) {
				setForeground(partida.isJugada() ? Colores.VERDE : Colores.AMBAR);
			}
			if (!partida.isJugada() && partida.getJornada() == modelo.getJornadaActual()) {
				setBackground(Colores.AZUL_SUAVE); // proxima jornada a jugar
			} else if (partida.isJugada()) {
				setBackground(fila % 2 == 0 ? Colores.FONDO_TARJETA : Colores.FILA_ALTERNA);
			} else {
				setBackground(Colores.AMBAR_SUAVE);
			}
		}
		return this;
	}
}
