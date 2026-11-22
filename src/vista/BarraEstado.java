package vista;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import modelo.Torneo;
import modelo.TorneoListener;

// Observador 1: muestra el ultimo mensaje del torneo en la parte inferior de la ventana.
public class BarraEstado extends JLabel implements TorneoListener {
	private static final long serialVersionUID = 1L;

	public BarraEstado(Torneo torneo) {
		setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		setForeground(Colores.TEXTO);
		torneo.addListener(this); // se registra solo
		torneoActualizado(torneo);
	}

	@Override
	public void torneoActualizado(Torneo torneo) {
		setText(torneo.getMensaje());
	}
}
