package vista;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

// Renderer de la tabla de posiciones: pinta el podio (oro, plata, bronce),
// pone en negrita el equipo y los puntos, y muestra la diferencia con signo (+3, -1).
public class PosicionRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada, boolean foco,
			int fila, int columna) {
		super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
		TablaPosicionesModel modelo = (TablaPosicionesModel) tabla.getModel();
		int filaModelo = tabla.convertRowIndexToModel(fila); // por si el usuario ordeno la tabla
		int columnaModelo = tabla.convertColumnIndexToModel(columna);
		int posicion = filaModelo + 1;

		setBorder(new EmptyBorder(0, 8, 0, 8));
		setHorizontalAlignment(columnaModelo == TablaPosicionesModel.COL_EQUIPO ? SwingConstants.LEFT : SwingConstants.CENTER);
		boolean negrita = columnaModelo == TablaPosicionesModel.COL_EQUIPO || columnaModelo == TablaPosicionesModel.COL_PUNTOS;
		setFont(tabla.getFont().deriveFont(negrita ? Font.BOLD : Font.PLAIN));

		if (columnaModelo == TablaPosicionesModel.COL_DIFERENCIA && valor instanceof Integer && (Integer) valor > 0) {
			setText("+" + valor);
		}
		if (columnaModelo == TablaPosicionesModel.COL_EQUIPO && posicion == 1 && modelo.isTerminado()) {
			setText(valor + "   (campeón)");
		}

		if (!seleccionada) {
			setForeground(Colores.TEXTO);
			if (posicion == 1) {
				setBackground(Colores.ORO);
			} else if (posicion == 2) {
				setBackground(Colores.PLATA);
			} else if (posicion == 3) {
				setBackground(Colores.BRONCE);
			} else {
				setBackground(fila % 2 == 0 ? Colores.FONDO_TARJETA : Colores.FILA_ALTERNA);
			}
		}
		return this;
	}
}
