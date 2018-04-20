package de.felixperko.fractals.Controls;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import de.felixperko.fractals.WindowHandler;

public class KeyListenerControls implements KeyListener {
	
	WindowHandler windowHandler;
	
	boolean setSave = true;
	boolean jumpToLocation = true;
	boolean saveLocation = true;
	
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
		if (jumpToLocation && e.getKeyCode() == KeyEvent.VK_TAB) {
			windowHandler.jumpToSavedLocation(e.isShiftDown());
			jumpToLocation = false;
		}
		if (saveLocation && e.getKeyChar() == 'l') {
			windowHandler.saveLocation();
			saveLocation = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == 's')
			setSave = true;
		if (e.getKeyCode() == KeyEvent.VK_TAB)
			jumpToLocation = true;
		if (e.getKeyChar() == 'l')
			saveLocation = true;
	}

}
