package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import modelo.Validador;

// Dialogo modal para escribir el nombre y el nivel de un equipo.
// El JSlider y el JSpinner estan "enlazados" (binding de datos, Guia 10): mover uno cambia el otro.
// Solo recoge datos; la validacion la hace el modelo (Validador) a pedido del controlador.
public class DialogoEquipo extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTextField txtNombre = new JTextField(20);
	private final JSpinner spnNivel = new JSpinner(
			new SpinnerNumberModel(60, Validador.NIVEL_MINIMO, Validador.NIVEL_MAXIMO, 1));
	private final JSlider sldNivel = new JSlider(Validador.NIVEL_MINIMO, Validador.NIVEL_MAXIMO, 60);
	private final JLabel lblCategoria = new JLabel();
	private boolean aceptado;

	public DialogoEquipo(Frame duenio) {
		super(duenio, "Agregar equipo", true);

		// binding: el spinner actualiza al slider y el slider al spinner
		spnNivel.addChangeListener(e -> {
			sldNivel.setValue((Integer) spnNivel.getValue());
			actualizarCategoria();
		});
		sldNivel.addChangeListener(e -> spnNivel.setValue(sldNivel.getValue()));
		sldNivel.setMajorTickSpacing(25);
		sldNivel.setPaintTicks(true);
		lblCategoria.setForeground(Colores.PRIMARIO);
		lblCategoria.setFont(lblCategoria.getFont().deriveFont(Font.BOLD));

		JPanel formulario = new JPanel(new GridBagLayout());
		formulario.setBorder(new EmptyBorder(16, 18, 8, 18));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		formulario.add(new JLabel("Nombre del equipo:"), c);
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		formulario.add(txtNombre, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		formulario.add(new JLabel("Nivel (1 a 100):"), c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		formulario.add(sldNivel, c);
		c.gridx = 2;
		c.weightx = 0;
		formulario.add(spnNivel, c);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		formulario.add(lblCategoria, c);

		JLabel lblAyuda = new JLabel("<html><small>El nivel influye en la estrategia \"Por nivel\" y \"Nivel + racha\".</small></html>");
		lblAyuda.setForeground(Colores.TEXTO_SUAVE);
		c.gridy = 3;
		formulario.add(lblAyuda, c);

		JButton btnAceptar = new JButton("Agregar");
		JButton btnCancelar = new JButton("Cancelar");
		btnAceptar.addActionListener(e -> {
			aceptado = true;
			setVisible(false);
		});
		btnCancelar.addActionListener(e -> setVisible(false));
		JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		botones.setBorder(new EmptyBorder(0, 10, 8, 10));
		botones.add(btnCancelar);
		botones.add(btnAceptar);

		add(formulario, BorderLayout.CENTER);
		add(botones, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(btnAceptar); // Enter = Agregar
		getRootPane().registerKeyboardAction(e -> setVisible(false), // Esc = Cancelar
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		actualizarCategoria();
		pack();
		setResizable(false);
		setLocationRelativeTo(duenio);
	}

	// muestra el dialogo (se queda esperando) y devuelve true si el usuario presiono Agregar
	public boolean mostrar() {
		aceptado = false;
		txtNombre.selectAll();
		txtNombre.requestFocusInWindow();
		setVisible(true);
		return aceptado;
	}

	public String getNombre() {
		return txtNombre.getText();
	}

	public int getNivel() {
		return (Integer) spnNivel.getValue();
	}

	private void actualizarCategoria() {
		int nivel = getNivel();
		String categoria;
		if (nivel >= 85) {
			categoria = "Élite";
		} else if (nivel >= 65) {
			categoria = "Profesional";
		} else if (nivel >= 40) {
			categoria = "Semi-profesional";
		} else {
			categoria = "Amateur";
		}
		lblCategoria.setText("Categoría: " + categoria);
	}
}
