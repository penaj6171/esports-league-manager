package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import controlador.TorneoController;
import modelo.Campeon;
import modelo.Equipo;
import modelo.EstrategiaSimulacion;
import modelo.Partida;
import modelo.Torneo;
import modelo.TorneoListener;

// Ventana principal: JMenuBar con atajos, barra de botones, JTabbedPane con 4 pestañas,
// JProgressBar para la simulacion y los dialogos (JOptionPane, JFileChooser).
// La vista NO tiene reglas de negocio: cada boton o menu solo llama al controlador.
public class VentanaPrincipal extends JFrame implements TorneoListener {
	private static final long serialVersionUID = 1L;

	// respuestas de preguntarGuardarAntesDeSalir()
	public static final int GUARDAR = 0;
	public static final int NO_GUARDAR = 1;
	public static final int CANCELAR = 2;

	private static final String RUTA_APLAUSOS = "sonidos/aplausos.wav";

	private final Torneo torneo;
	private final List<EstrategiaSimulacion> estrategias;
	private TorneoController controlador;

	private final PanelCabecera cabecera;
	private final PanelPosiciones panelPosiciones;
	private final PanelPartidos panelPartidos;
	private final PanelEstadisticas panelEstadisticas;
	private final PanelCampeones panelCampeones;
	private final BarraEstado barraEstado;
	private final JTabbedPane pestanas = new JTabbedPane();
	private final JProgressBar barraProgreso = new JProgressBar(0, 100);
	private final JLabel lblAviso = new JLabel();
	private final Timer temporizadorAviso;
	private final JCheckBoxMenuItem itemSonido = new JCheckBoxMenuItem("Sonido al coronar campeón", true);
	private final List<JRadioButtonMenuItem> itemsEstrategia = new ArrayList<>();

	// cada accion se comparte entre el menu y la barra de herramientas:
	// al deshabilitarla se deshabilitan los dos a la vez
	private Accion accNuevo;
	private Accion accCargar;
	private Accion accGuardar;
	private Accion accExportar;
	private Accion accSalir;
	private Accion accDeshacer;
	private Accion accRehacer;
	private Accion accRenombrar;
	private Accion accAgregar;
	private Accion accEjemplo;
	private Accion accEliminar;
	private Accion accSimularUna;
	private Accion accSimularJornada;
	private Accion accSimularTodo;
	private Accion accCancelar;
	private Accion accReiniciar;
	private Accion accCampeon;

	private boolean simulando;
	private boolean puedeDeshacer;
	private boolean puedeRehacer;
	private boolean cambiosSinGuardar;
	private boolean baseDisponible = true;

	public VentanaPrincipal(Torneo torneo, List<EstrategiaSimulacion> estrategias) {
		super("E-Sports League Manager");
		this.torneo = torneo;
		this.estrategias = estrategias;

		// cada panel se registra solo como observador del torneo
		cabecera = new PanelCabecera(torneo, estrategias);
		panelPosiciones = new PanelPosiciones(torneo);
		panelPartidos = new PanelPartidos(torneo);
		panelEstadisticas = new PanelEstadisticas(torneo);
		panelCampeones = new PanelCampeones();
		barraEstado = new BarraEstado(torneo);

		temporizadorAviso = new Timer(6000, e -> lblAviso.setText(""));
		temporizadorAviso.setRepeats(false);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // el controlador pregunta antes de cerrar
		setSize(1180, 740);
		setMinimumSize(new Dimension(1060, 620));
		setLocationRelativeTo(null);
	}

	// arma menus, barra de herramientas y pestañas cuando ya existe el controlador
	public void conectar(TorneoController nuevoControlador) {
		this.controlador = nuevoControlador;
		crearAcciones();
		setJMenuBar(crearMenu());

		JPanel norte = new JPanel(new BorderLayout());
		norte.add(cabecera, BorderLayout.NORTH);
		norte.add(crearBarraHerramientas(), BorderLayout.SOUTH);

		pestanas.addTab("Posiciones", panelPosiciones);
		pestanas.addTab("Partidos", panelPartidos);
		pestanas.addTab("Estadísticas", panelEstadisticas);
		pestanas.addTab("Salón de la fama", panelCampeones);
		pestanas.setToolTipTextAt(0, "Tabla de posiciones (Ctrl+1)");
		pestanas.setToolTipTextAt(1, "Calendario y resultados (Ctrl+2)");
		pestanas.setToolTipTextAt(2, "Gráfico y números del torneo (Ctrl+3)");
		pestanas.setToolTipTextAt(3, "Campeones guardados en SQLite (Ctrl+4)");

		add(norte, BorderLayout.NORTH);
		add(pestanas, BorderLayout.CENTER);
		add(crearBarraInferior(), BorderLayout.SOUTH);

		cabecera.setAlCambiarEstrategia(controlador::cambiarEstrategia);
		panelPartidos.setAlHacerDobleClic(controlador::registrarResultado);
		panelCampeones.setAcciones(controlador::actualizarCampeones, controlador::vaciarCampeones);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controlador.salir();
			}
		});

		torneo.addListener(this); // la ventana tambien observa: titulo y botones habilitados
		torneoActualizado(torneo);
	}

	// ===================== Construccion =====================

	private void crearAcciones() {
		int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		accNuevo = new Accion("Nuevo torneo…", "Empezar un torneo vacío", KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrl),
				controlador::nuevoTorneo);
		accCargar = new Accion("Cargar", "Cargar el torneo guardado en torneo.db",
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl), controlador::cargar);
		accGuardar = new Accion("Guardar", "Guardar el torneo en la base de datos SQLite",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl), controlador::guardar);
		accExportar = new Accion("Exportar CSV…", "Exportar la tabla y las partidas a un archivo CSV",
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ctrl), controlador::exportarCsv);
		accSalir = new Accion("Salir", "Cerrar el programa", KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl),
				controlador::salir);
		accDeshacer = new Accion("Deshacer", "Deshacer la última acción", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl),
				controlador::deshacer);
		accRehacer = new Accion("Rehacer", "Rehacer la acción deshecha", KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl),
				controlador::rehacer);
		accRenombrar = new Accion("Renombrar torneo…", "Cambiar el nombre del torneo",
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), controlador::renombrarTorneo);
		accAgregar = new Accion("Agregar equipo…", "Agregar un equipo con nombre y nivel",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, ctrl), controlador::agregarEquipo);
		accEjemplo = new Accion("Equipos de ejemplo", "Agregar 8 equipos de ejemplo para probar",
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ctrl), controlador::agregarEquiposDeEjemplo);
		accEliminar = new Accion("Eliminar equipo…", "Eliminar un equipo (solo antes de jugar)",
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), controlador::eliminarEquipo);
		accSimularUna = new Accion("Simular partida", "Simular la siguiente partida pendiente",
				KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), controlador::simularSiguiente);
		accSimularJornada = new Accion("Simular jornada", "Simular todas las partidas de la jornada actual",
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), controlador::simularJornada);
		accSimularTodo = new Accion("Simular todo", "Simular todo el torneo con barra de progreso (SwingWorker)",
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), controlador::simularTodo);
		accCancelar = new Accion("Cancelar", "Detener la simulación en curso",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), controlador::cancelarSimulacion);
		accReiniciar = new Accion("Reiniciar resultados", "Volver a dejar todas las partidas pendientes",
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl), controlador::reiniciarResultados);
		accCampeon = new Accion("Registrar campeón", "Guardar al campeón en el Salón de la Fama",
				KeyStroke.getKeyStroke(KeyEvent.VK_H, ctrl), controlador::registrarCampeon);
	}

	private JMenuBar crearMenu() {
		JMenuBar barra = new JMenuBar();

		JMenu archivo = menu("Archivo", KeyEvent.VK_A);
		archivo.add(new JMenuItem(accNuevo));
		archivo.add(new JMenuItem(accCargar));
		archivo.add(new JMenuItem(accGuardar));
		archivo.addSeparator();
		archivo.add(new JMenuItem(accExportar));
		archivo.addSeparator();
		archivo.add(new JMenuItem(accSalir));

		JMenu editar = menu("Editar", KeyEvent.VK_E);
		editar.add(new JMenuItem(accDeshacer));
		editar.add(new JMenuItem(accRehacer));
		editar.addSeparator();
		editar.add(new JMenuItem(accRenombrar));

		JMenu menuTorneo = menu("Torneo", KeyEvent.VK_T);
		menuTorneo.add(new JMenuItem(accAgregar));
		menuTorneo.add(new JMenuItem(accEjemplo));
		menuTorneo.add(new JMenuItem(accEliminar));
		menuTorneo.addSeparator();
		menuTorneo.add(new JMenuItem(accSimularUna));
		menuTorneo.add(new JMenuItem(accSimularJornada));
		menuTorneo.add(new JMenuItem(accSimularTodo));
		menuTorneo.add(new JMenuItem(accCancelar));
		menuTorneo.addSeparator();
		menuTorneo.add(new JMenuItem(accReiniciar));
		menuTorneo.add(new JMenuItem(accCampeon));
		menuTorneo.addSeparator();
		JMenu menuEstrategia = new JMenu("Estrategia de simulación");
		ButtonGroup grupo = new ButtonGroup();
		for (EstrategiaSimulacion estrategia : estrategias) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(estrategia.getNombre());
			item.setToolTipText(estrategia.getDescripcion());
			item.addActionListener(e -> controlador.cambiarEstrategia(estrategia));
			grupo.add(item);
			itemsEstrategia.add(item);
			menuEstrategia.add(item);
		}
		menuTorneo.add(menuEstrategia);

		JMenu ver = menu("Ver", KeyEvent.VK_V);
		int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		for (int i = 0; i < 4; i++) {
			final int indice = i;
			JMenuItem item = new JMenuItem(pestanaTitulo(i));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, ctrl));
			item.addActionListener(e -> pestanas.setSelectedIndex(indice));
			ver.add(item);
		}
		ver.addSeparator();
		ver.add(itemSonido);

		JMenu ayuda = menu("Ayuda", KeyEvent.VK_Y);
		JMenuItem itemAyuda = new JMenuItem("Cómo usar");
		itemAyuda.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		itemAyuda.addActionListener(e -> mostrarAyuda());
		JMenuItem itemAcerca = new JMenuItem("Acerca de");
		itemAcerca.addActionListener(e -> mostrarAcercaDe());
		ayuda.add(itemAyuda);
		ayuda.add(itemAcerca);

		barra.add(archivo);
		barra.add(editar);
		barra.add(menuTorneo);
		barra.add(ver);
		barra.add(ayuda);
		return barra;
	}

	private String pestanaTitulo(int indice) {
		String[] titulos = { "Posiciones", "Partidos", "Estadísticas", "Salón de la fama" };
		return titulos[indice];
	}

	private JMenu menu(String titulo, int mnemonico) {
		JMenu menu = new JMenu(titulo);
		menu.setMnemonic(mnemonico); // Alt + letra
		return menu;
	}

	// barra de botones: usa las MISMAS acciones que el menu
	private JPanel crearBarraHerramientas() {
		JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		barra.setBorder(new EmptyBorder(2, 8, 2, 8));
		barra.add(boton(accAgregar));
		barra.add(boton(accEjemplo));
		barra.add(boton(accEliminar));
		barra.add(separador());
		barra.add(boton(accSimularUna));
		barra.add(boton(accSimularJornada));
		barra.add(boton(accSimularTodo));
		barra.add(separador());
		barra.add(boton(accDeshacer));
		barra.add(boton(accRehacer));
		barra.add(separador());
		barra.add(boton(accGuardar));
		barra.add(boton(accCargar));
		barra.add(boton(accExportar));
		return barra;
	}

	private JButton boton(Accion accion) {
		JButton boton = new JButton(accion);
		boton.setFocusable(false);
		boton.putClientProperty("JComponent.sizeVariant", "small"); // Nimbus: boton compacto
		boton.updateUI(); // reinstala el estilo para que use el tamaño compacto
		return boton;
	}

	private JSeparator separador() {
		JSeparator separador = new JSeparator(SwingConstants.VERTICAL);
		separador.setPreferredSize(new Dimension(8, 24));
		return separador;
	}

	private JPanel crearBarraInferior() {
		barraProgreso.setStringPainted(true);
		barraProgreso.setString("Listo");
		barraProgreso.setPreferredSize(new Dimension(230, 20));
		lblAviso.setForeground(Colores.PRIMARIO);

		JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		derecha.add(lblAviso);
		derecha.add(barraProgreso);
		derecha.add(boton(accCancelar));

		JPanel sur = new JPanel(new BorderLayout(10, 0));
		sur.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
				new EmptyBorder(5, 10, 5, 6)));
		sur.add(barraEstado, BorderLayout.CENTER);
		sur.add(derecha, BorderLayout.EAST);
		return sur;
	}

	// ===================== Observer =====================

	@Override
	public void torneoActualizado(Torneo t) {
		actualizarTitulo();
		actualizarAcciones();
		int indice = estrategias.indexOf(t.getEstrategia());
		if (indice >= 0) {
			itemsEstrategia.get(indice).setSelected(true);
		}
	}

	private void actualizarTitulo() {
		setTitle((cambiosSinGuardar ? "* " : "") + torneo.getNombre() + " — E-Sports League Manager");
	}

	// habilita cada accion segun el estado del torneo y si hay una simulacion corriendo
	private void actualizarAcciones() {
		if (accAgregar == null) {
			return; // todavia no se conecto el controlador
		}
		boolean libre = !simulando;
		boolean sinResultados = !torneo.hayResultados();
		boolean hayEspacio = torneo.getEquipos().size() < Torneo.MAX_EQUIPOS;
		boolean pendientes = torneo.hayPendientes();

		accAgregar.setEnabled(libre && sinResultados && hayEspacio);
		accEjemplo.setEnabled(libre && sinResultados && hayEspacio);
		accEliminar.setEnabled(libre && sinResultados && !torneo.getEquipos().isEmpty());
		accSimularUna.setEnabled(libre && pendientes);
		accSimularJornada.setEnabled(libre && pendientes);
		accSimularTodo.setEnabled(libre && pendientes);
		accCancelar.setEnabled(simulando);
		accReiniciar.setEnabled(libre && torneo.hayResultados());
		accCampeon.setEnabled(libre && baseDisponible && torneo.isTerminado());
		accDeshacer.setEnabled(libre && puedeDeshacer);
		accRehacer.setEnabled(libre && puedeRehacer);
		accNuevo.setEnabled(libre);
		accRenombrar.setEnabled(libre);
		accGuardar.setEnabled(libre && baseDisponible);
		accCargar.setEnabled(libre && baseDisponible);
		accExportar.setEnabled(libre);
		cabecera.setEstrategiaHabilitada(libre);
		for (JRadioButtonMenuItem item : itemsEstrategia) {
			item.setEnabled(libre);
		}
	}

	// ===================== Metodos que usa el controlador =====================

	public DialogoEquipo crearDialogoEquipo() {
		return new DialogoEquipo(this);
	}

	public Equipo getEquipoSeleccionado() {
		return panelPosiciones.getEquipoSeleccionado();
	}

	public Equipo elegirEquipo(List<Equipo> equipos, Equipo sugerido) {
		Equipo[] opciones = equipos.toArray(new Equipo[0]);
		Object elegido = JOptionPane.showInputDialog(this, "¿Qué equipo quieres eliminar?", "Eliminar equipo",
				JOptionPane.QUESTION_MESSAGE, null, opciones, sugerido != null ? sugerido : opciones[0]);
		return (Equipo) elegido;
	}

	// dialogo con dos JSpinner (0 a 2 mapas); devuelve null si se cancela
	public int[] pedirResultado(Partida partida, int[] inicial) {
		JSpinner spnLocal = new JSpinner(new SpinnerNumberModel(inicial == null ? 2 : inicial[0], 0, 2, 1));
		JSpinner spnVisitante = new JSpinner(new SpinnerNumberModel(inicial == null ? 0 : inicial[1], 0, 2, 1));
		JPanel panel = new JPanel(new GridLayout(0, 2, 12, 8));
		panel.add(new JLabel("Jornada " + partida.getJornada() + " · al mejor de 3"));
		panel.add(new JLabel("Mapas"));
		panel.add(new JLabel(partida.getLocal().getNombre() + " (local)"));
		panel.add(spnLocal);
		panel.add(new JLabel(partida.getVisitante().getNombre() + " (visitante)"));
		panel.add(spnVisitante);
		int respuesta = JOptionPane.showConfirmDialog(this, panel, "Registrar resultado", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (respuesta != JOptionPane.OK_OPTION) {
			return null;
		}
		return new int[] { (Integer) spnLocal.getValue(), (Integer) spnVisitante.getValue() };
	}

	public String pedirTexto(String titulo, String mensaje, String inicial) {
		Object texto = JOptionPane.showInputDialog(this, mensaje, titulo, JOptionPane.QUESTION_MESSAGE, null, null,
				inicial);
		return texto == null ? null : texto.toString();
	}

	public boolean confirmar(String mensaje) {
		return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	public int preguntarGuardarAntesDeSalir() {
		Object[] opciones = { "Guardar", "No guardar", "Cancelar" };
		int respuesta = JOptionPane.showOptionDialog(this, "Hay cambios sin guardar. ¿Guardarlos antes de salir?",
				"Salir", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
		if (respuesta == 0) {
			return GUARDAR;
		}
		return respuesta == 1 ? NO_GUARDAR : CANCELAR;
	}

	public void mostrarError(String mensaje) {
		JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void mostrarInfo(String mensaje) {
		JOptionPane.showMessageDialog(this, mensaje, "E-Sports League Manager", JOptionPane.INFORMATION_MESSAGE);
	}

	// mensaje corto abajo a la derecha que desaparece solo
	public void mostrarAviso(String mensaje) {
		lblAviso.setText(mensaje);
		temporizadorAviso.restart();
	}

	public File elegirArchivoCsv(String nombreSugerido) {
		JFileChooser selector = new JFileChooser();
		selector.setDialogTitle("Exportar a CSV");
		selector.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
		selector.setSelectedFile(new File(nombreSugerido));
		if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return null; // cancelo
		}
		File archivo = selector.getSelectedFile();
		if (!archivo.getName().toLowerCase().endsWith(".csv")) {
			archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
		}
		if (archivo.exists() && !confirmar("El archivo " + archivo.getName() + " ya existe. ¿Sobrescribirlo?")) {
			return null;
		}
		return archivo;
	}

	public void setProgreso(int porcentaje) {
		barraProgreso.setValue(porcentaje);
		barraProgreso.setString("Simulando… " + porcentaje + " %");
	}

	public void setSimulando(boolean enCurso) {
		simulando = enCurso;
		barraProgreso.setValue(0);
		barraProgreso.setString(enCurso ? "Simulando… 0 %" : "Listo");
		if (enCurso) {
			pestanas.setSelectedComponent(panelPosiciones); // para ver como se mueve la tabla
		}
		actualizarAcciones();
	}

	public void setHistorial(boolean deshacer, String descripcionDeshacer, boolean rehacer, String descripcionRehacer) {
		puedeDeshacer = deshacer;
		puedeRehacer = rehacer;
		accDeshacer.setAyuda(deshacer ? "Deshacer: " + descripcionDeshacer : "Nada para deshacer");
		accRehacer.setAyuda(rehacer ? "Rehacer: " + descripcionRehacer : "Nada para rehacer");
		actualizarAcciones();
	}

	public void setCambiosSinGuardar(boolean hayCambios) {
		cambiosSinGuardar = hayCambios;
		actualizarTitulo();
	}

	public void setBaseDisponible(boolean disponible) {
		baseDisponible = disponible;
		panelCampeones.setBaseDisponible(disponible);
		actualizarAcciones();
	}

	public void mostrarCampeones(List<Campeon> campeones, Map<String, Integer> titulos) {
		panelCampeones.mostrar(campeones, titulos);
	}

	public void mostrarPestanaCampeones() {
		pestanas.setSelectedComponent(panelCampeones);
	}

	public void celebrarCampeon(Equipo campeon) {
		pestanas.setSelectedComponent(panelPosiciones);
		if (itemSonido.isSelected()) {
			Sonido.reproducir(RUTA_APLAUSOS);
		}
	}

	// ===================== Ayuda (solo vista) =====================

	private void mostrarAyuda() {
		JOptionPane.showMessageDialog(this, "<html><body style='width:380px'>"
				+ "<b>1.</b> Agrega equipos (Ctrl+T) o usa <i>Equipos de ejemplo</i> (Ctrl+M). "
				+ "El calendario todos-contra-todos se genera solo.<br><br>"
				+ "<b>2.</b> Elige la estrategia de simulación en la cabecera o en el menú Torneo.<br><br>"
				+ "<b>3.</b> Simula una partida (F5), una jornada (F6) o todo el torneo (F7, con barra de progreso). "
				+ "También puedes escribir un resultado con doble clic en la pestaña Partidos.<br><br>"
				+ "<b>4.</b> Ctrl+Z deshace y Ctrl+Y rehace cualquier acción.<br><br>"
				+ "<b>5.</b> Ctrl+S guarda en SQLite (torneo.db), Ctrl+O carga y Ctrl+E exporta a CSV.<br><br>"
				+ "Series al mejor de 3 mapas · victoria = " + Torneo.PUNTOS_VICTORIA + " puntos · "
				+ "desempate por diferencia de mapas.</body></html>", "Cómo usar", JOptionPane.INFORMATION_MESSAGE);
	}

	private void mostrarAcercaDe() {
		JOptionPane.showMessageDialog(this, "<html><body style='width:340px'>"
				+ "<h2 style='margin:0'>E-Sports League Manager</h2>"
				+ "Trabajo final de Lenguajes de Programación III<br>"
				+ "Universidad Católica de Santa María<br><br>"
				+ "<b>Integrantes:</b> Walter Figueroa, Gabriel Cruz y Octavio Peña<br><br>"
				+ "<b>Arquitectura:</b> MVC<br>"
				+ "<b>Patrones:</b> Observer (TorneoListener), Strategy (EstrategiaSimulacion), "
				+ "Command (HistorialComandos)<br>"
				+ "<b>Persistencia:</b> SQLite con JDBC (TorneoDAO)</body></html>", "Acerca de",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Action reutilizable: nombre, ayuda, atajo de teclado y la tarea a ejecutar
	private static class Accion extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final transient Runnable tarea;
		private final String atajo;

		Accion(String nombre, String ayuda, KeyStroke teclas, Runnable tarea) {
			super(nombre);
			this.tarea = tarea;
			this.atajo = teclas == null ? "" : textoAtajo(teclas);
			putValue(ACCELERATOR_KEY, teclas);
			setAyuda(ayuda);
		}

		void setAyuda(String ayuda) {
			putValue(SHORT_DESCRIPTION, atajo.isEmpty() ? ayuda : ayuda + "  (" + atajo + ")");
		}

		private static String textoAtajo(KeyStroke teclas) {
			String modificadores = KeyEvent.getModifiersExText(teclas.getModifiers());
			String tecla = KeyEvent.getKeyText(teclas.getKeyCode());
			return modificadores.isEmpty() ? tecla : modificadores + "+" + tecla;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			tarea.run();
		}
	}
}
