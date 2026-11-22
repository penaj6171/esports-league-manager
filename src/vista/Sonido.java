package vista;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// Multimedia (Guia 10): reproduce un archivo .wav con un Clip.
// Si el archivo no existe o la PC no tiene audio, solo suena un "beep".
final class Sonido {

	private Sonido() {
	}

	static void reproducir(String ruta) {
		File archivo = new File(ruta);
		if (!archivo.exists()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		try (AudioInputStream audio = AudioSystem.getAudioInputStream(archivo)) {
			Clip clip = AudioSystem.getClip();
			clip.open(audio);
			// cuando el sonido termina se libera el clip
			clip.addLineListener(evento -> {
				if (evento.getType() == LineEvent.Type.STOP) {
					clip.close();
				}
			});
			clip.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}
}
