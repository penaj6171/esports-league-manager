package persistencia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import modelo.Equipo;
import modelo.Partida;
import modelo.Torneo;

// Exporta la tabla de posiciones y las partidas a un archivo CSV que se abre en Excel.
// La vista elige el archivo (JFileChooser); esta clase sabe como escribirlo.
public final class ExportadorCsv {

	private ExportadorCsv() {
	}

	public static void exportar(Torneo torneo, File destino) throws IOException {
		// try-with-resources: el archivo se cierra solo, aun si hay un error
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)))) {
			out.print('﻿'); // marca BOM para que Excel lea bien las tildes
			out.println("Torneo," + celda(torneo.getNombre()));
			out.println("Estrategia," + celda(torneo.getEstrategia().getNombre()));
			out.println();

			out.println("posicion,equipo,nivel,pj,pg,pp,mapas_favor,mapas_contra,diferencia,puntos");
			List<Equipo> tabla = torneo.getTabla();
			for (int i = 0; i < tabla.size(); i++) {
				Equipo e = tabla.get(i);
				out.println((i + 1) + "," + celda(e.getNombre()) + "," + e.getNivel() + "," + e.getJugados() + ","
						+ e.getGanados() + "," + e.getPerdidos() + "," + e.getMapasFavor() + "," + e.getMapasContra()
						+ "," + e.getDiferencia() + "," + e.getPuntos());
			}
			out.println();

			out.println("jornada,local,mapas_local,mapas_visitante,visitante,estado");
			for (Partida p : torneo.getPartidas()) {
				int[] r = p.getResultado();
				out.println(p.getJornada() + "," + celda(p.getLocal().getNombre()) + ","
						+ (r == null ? "" : r[0]) + "," + (r == null ? "" : r[1]) + ","
						+ celda(p.getVisitante().getNombre()) + "," + (p.isJugada() ? "Jugada" : "Pendiente"));
			}

			if (out.checkError()) { // PrintWriter no lanza excepciones: hay que preguntar
				throw new IOException("No se pudo escribir el archivo " + destino.getName());
			}
		}
	}

	// si el texto tiene comas o comillas se encierra entre comillas
	private static String celda(String texto) {
		if (texto.contains(",") || texto.contains("\"")) {
			return "\"" + texto.replace("\"", "\"\"") + "\"";
		}
		return texto;
	}
}
