package util;

import java.util.ArrayList;

// Pila generica (Guia 05: Generics).
// HistorialComandos la usa dos veces: una pila para deshacer y otra para rehacer.
public class PilaGenerica<T> {
	private final ArrayList<T> elementos = new ArrayList<>();

	// pone un elemento en el tope
	public void apilar(T elemento) {
		elementos.add(elemento);
	}

	// saca y devuelve el elemento del tope
	public T desapilar() {
		if (estaVacia()) {
			throw new IllegalStateException("La pila está vacía");
		}
		return elementos.remove(elementos.size() - 1);
	}

	// devuelve el elemento del tope sin sacarlo
	public T verTope() {
		if (estaVacia()) {
			throw new IllegalStateException("La pila está vacía");
		}
		return elementos.get(elementos.size() - 1);
	}

	public boolean estaVacia() {
		return elementos.isEmpty();
	}

	public int tamano() {
		return elementos.size();
	}

	public void limpiar() {
		elementos.clear();
	}
}
