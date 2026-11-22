import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controlador.TorneoController;
import modelo.EstrategiaSimulacion;
import modelo.SimulacionAleatoria;
import modelo.SimulacionPorNivel;
import modelo.SimulacionPorRacha;
import modelo.Torneo;
import persistencia.TorneoDAO;
import vista.VentanaPrincipal;

// Punto de entrada: es el UNICO lugar donde se crean y conectan Modelo, Vista y Controlador.
// Proyecto: E-Sports League Manager (Walter Figueroa, Gabriel Cruz y Octavio Peña).
public class App {

	public static void main(String[] args) {
		// toda la interfaz se crea en el hilo de eventos de Swing (EDT)
		SwingUtilities.invokeLater(() -> {
			usarAparienciaNimbus();

			// Strategy: las estrategias disponibles (el usuario elige una en tiempo de ejecucion)
			List<EstrategiaSimulacion> estrategias = List.of(new SimulacionPorNivel(), new SimulacionAleatoria(),
					new SimulacionPorRacha());

			Torneo torneo = new Torneo("Liga LP3", estrategias.get(0)); // Modelo
			VentanaPrincipal vista = new VentanaPrincipal(torneo, estrategias); // Vista
			TorneoDAO dao = new TorneoDAO("jdbc:sqlite:torneo.db"); // Persistencia
			TorneoController controlador = new TorneoController(torneo, vista, dao, estrategias); // Controlador
			controlador.iniciar();
		});
	}

	private static void usarAparienciaNimbus() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					return;
				}
			}
		} catch (Exception e) {
			// si Nimbus no esta disponible se usa la apariencia por defecto
		}
	}
}
