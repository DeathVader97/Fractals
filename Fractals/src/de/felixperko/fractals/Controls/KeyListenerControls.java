package de.felixperko.fractals.Controls;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import de.felixperko.fractals.WindowHandler;

public class KeyListenerControls implements KeyListener {
	
	WindowHandler windowHandler;
	
	boolean setSave = true;
	
	public KeyListenerControls(WindowHandler windowHandler) {
		this.windowHandler = windowHandler;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
		case '+':
			windowHandler.setQuality(windowHandler.quality*2);
			break;
		case '-':
			windowHandler.setQuality(windowHandler.quality*0.5);
			break;
		case 'c':
			windowHandler.loopColor(0.01f);
		}
		
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ENTER:
			windowHandler.setRedraw();
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (setSave && e.getKeyChar() == 's') {
			windowHandler.save = true;
			setSave = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == 's')
			setSave = true;
	}

}
