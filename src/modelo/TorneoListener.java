package modelo;

// Patron Observer: interfaz del observador.
// El torneo (sujeto) avisa a todos los que la implementan cada vez que cambia su estado.
// Se pasa a si mismo para que cada observador consulte solo lo que necesita.
public interface TorneoListener {
	void torneoActualizado(Torneo torneo);
}
