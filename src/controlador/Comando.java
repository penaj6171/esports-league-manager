package controlador;

import excepciones.TorneoException;

// Patron Command: cada accion del usuario se convierte en un objeto
// que sabe ejecutarse y deshacerse. HistorialComandos guarda esos objetos en pilas.
public interface Comando {
	void ejecutar() throws TorneoException;

	void deshacer() throws TorneoException;

	// texto que se muestra en el tooltip de Deshacer / Rehacer
	String getDescripcion();
}
